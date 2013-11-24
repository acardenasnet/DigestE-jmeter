package net.acardenas.jmeter.plugins.authDigeste;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * User: acardenas
 */
public class DigesteSampler extends HTTPSampler
{

    private static final long serialVersionUID = 3302785804254990944L;

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    public static final String USER_KEY = "DigestESampler.user_key";
    public static final String USER_SECRET = "DigestESampler.user_secret";


    public void setUserKey(String aUserKey)
    {
        setProperty(USER_KEY, aUserKey);
    }

    public void setUserSecret(String aUserSecret)
    {
        setProperty(USER_SECRET, aUserSecret);
    }
    
    public String getUserKey()
    {
        return getPropertyAsString(USER_KEY);
    }
    
    public String getUserSecret()
    {
        return getPropertyAsString(USER_SECRET);
    }
    
    @Override
    public SampleResult sample()
    {
        HTTPSampleResult httmlSamplerResult = new HTTPSampleResult();
        httmlSamplerResult.setSuccessful(false);
        httmlSamplerResult.setResponseCode("000");
        httmlSamplerResult.setSampleLabel(getName());
        httmlSamplerResult.setDataEncoding("UTF-8");
        httmlSamplerResult.setDataType("text/xml");
        httmlSamplerResult.setMonitor(isMonitor());
        httmlSamplerResult.sampleStart();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();

        ResponseHandler<String> responseHandler = null;
        try
        {
            String myUrlString = getUrl().toString();
            
            myUrlString = toValidUrl(myUrlString);
            HttpPost myHttpPost = new HttpPost(
                    myUrlString);

            // Initial request without credentials returns
            // "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(myHttpPost);
            httmlSamplerResult.setURL(getUrl());
            httmlSamplerResult.setHTTPMethod(myHttpPost.getMethod());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
            {

                // Get current current "WWW-Authenticate" header from response
                // WWW-Authenticate:Digest realm="My Test Realm", qop="auth",
                // nonce="cdcf6cbe6ee17ae0790ed399935997e8",
                // opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                LOG.debug("Start : sample DigestESampler");
                
                DigestScheme digestScheme = new DigesteSchema();

                // Parse realm, nonce sent by server.
                digestScheme.processChallenge(authHeader);

                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
                        getPropertyAsString(USER_KEY),
                        getPropertyAsString(USER_SECRET));
                myHttpPost.addHeader(digestScheme.authenticate(creds, myHttpPost));

                responseHandler = new BasicResponseHandler();
                httpclient2.execute(myHttpPost, responseHandler);
            }
            else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                httmlSamplerResult.setResponseCode(Integer.toString(response.getStatusLine().getStatusCode()));
                httmlSamplerResult.setSuccessful(isSuccessCode(response.getStatusLine().getStatusCode()));
                httmlSamplerResult.setResponseMessage(response.getStatusLine().getReasonPhrase());
                return httmlSamplerResult;
            }


        httmlSamplerResult.sampleEnd();

        httmlSamplerResult.setResponseCode(Integer.toString(200));
        httmlSamplerResult.setSuccessful(isSuccessCode(200));

        String myHeaderRsponse = "";
        Header[] myHeaders = myHttpPost.getAllHeaders();
        for (Header myHeader : myHeaders)
        {
            myHeaderRsponse += myHeader.getName() + "=" + myHeader.getValue() + ",";
            org.apache.jmeter.protocol.http.control.Header myHeaderJmeter = new org.apache.jmeter.protocol.http.control.Header(
                    myHeader.getName(), myHeader.getValue());

            getHeaderManager().add(myHeaderJmeter);
        }

        // get cookieStore
        CookieStore cookieStore = httpclient2.getCookieStore();
        // get Cookies
        List<Cookie> cookies = cookieStore.getCookies();
        String cookiesString = "";
        for (Cookie myCookie : cookies)
        {
            cookiesString += myCookie.getName() + "=" + myCookie.getValue()
                    + "; ";
            org.apache.jmeter.protocol.http.control.Cookie myJmeterCookie = new org.apache.jmeter.protocol.http.control.Cookie(
                    myCookie.getName(), myCookie.getValue(),
                    myCookie.getDomain(), myCookie.getPath(), false, myCookie
                            .getExpiryDate().getTime());
            getCookieManager().add(myJmeterCookie);
        }

        httmlSamplerResult.setCookies(cookiesString);
        httmlSamplerResult.setResponseHeaders(myHeaderRsponse);
        httmlSamplerResult.setContentType("application/json");

        LOG.debug("End : sample");

        }
        catch (MalformedChallengeException e)
        {
            LOG.error(e.getMessage(), e);
        }
        catch (AuthenticationException e)
        {
        	LOG.error(e.getMessage(), e);
        }
        catch (HttpResponseException e)
        {
        	LOG.warn(e.getMessage(), e);
            HttpResponseException myException = (HttpResponseException) e;
            String myCode = String.valueOf(myException.getStatusCode());
            httmlSamplerResult.setResponseCode(myCode);
            httmlSamplerResult.setSuccessful(isSuccessCode(myException.getStatusCode()));
        }
        catch (ClientProtocolException e)
        {
        	LOG.error(e.getMessage(), e);
        }
        catch (IOException e)
        {

        	LOG.error(e.getMessage(), e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }

        return httmlSamplerResult;
    }

    private String toValidUrl(String u) throws MalformedURLException
    {
        URL url = new URL(u);
        String urlStr = url.toString();
        if (urlStr.endsWith("/"))
        {
            url = toURL(urlStr.substring(0, urlStr.length() - 1));
            urlStr = url.toString();
        }
        return urlStr;
    }

    private URL toURL(String u)
    {
        try
        {
            return new URL(u);
        }
        catch (MalformedURLException e)
        {
        	LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
