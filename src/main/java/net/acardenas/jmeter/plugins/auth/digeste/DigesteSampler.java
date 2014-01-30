package net.acardenas.jmeter.plugins.auth.digeste;

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
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
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
        HTTPSampleResult httpSamplerResult = new HTTPSampleResult();
        httpSamplerResult.setSuccessful(false);
        httpSamplerResult.setResponseCode("000");
        httpSamplerResult.setSampleLabel(getName());
        httpSamplerResult.setDataEncoding("UTF-8");
        httpSamplerResult.setDataType("text/xml");
        httpSamplerResult.setMonitor(isMonitor());
        httpSamplerResult.sampleStart();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();

        ResponseHandler<String> responseHandler = null;
        try
        {
            String myUrlString = getUrl().toString();

            myUrlString = toValidUrl(myUrlString);
            LOG.debug(myUrlString);
            HTTPSampleResult myHttpSampleResult = sample(getUrl(), POST, false, 0);
            //HttpPost myHttpPost = new HttpPost(myUrlString);
            HttpPost myHttpPost = null;

            // Initial request without credentials returns
            // "HTTP/1.1 401 Unauthorized"
            //HttpResponse response = httpclient.execute(myHttpPost);
            HttpResponse response = null;
            //httmlSamplerResult.setURL(getUrl());
            //httmlSamplerResult.setHTTPMethod(myHttpPost.getMethod());
            LOG.debug(myHttpSampleResult.getResponseCode());
            if (myHttpSampleResult.getResponseCode().equals("401"))
            {

                // Get current current "WWW-Authenticate" header from response
                // WWW-Authenticate:Digest realm="My Test Realm", qop="auth",
                // nonce="cdcf6cbe6ee17ae0790ed399935997e8",
                // opaque="ae40d7c8ca6a35af15460d352be5e71c"
                String[] myHeaders = myHttpSampleResult.getResponseHeaders().split("\n");
                Header myHeader = null;
                for (String myHeaderString : myHeaders)
                {
                    LOG.debug(myHeaderString);
                    if (myHeaderString.startsWith("Www-Authenticate"))
                    {
                        String[] myHeaderSplit = myHeaderString.split(":");
                        myHeader = new BasicHeader(myHeaderSplit[0], myHeaderSplit[1]);

                    }
                }

                Header authHeader = myHeader;
                LOG.debug("Start : sample DigestESampler");

                DigesteSchema digestScheme = new DigesteSchema();

                // Parse realm, nonce sent by server.
                digestScheme.processChallenge(authHeader);

                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
                        getPropertyAsString(USER_KEY),
                        getPropertyAsString(USER_SECRET));

                myHeader = digestScheme.authenticate(creds );
                LOG.debug(myHeader.getName());
                LOG.debug(myHeader.getValue());

                getHeaderManager().add(new org.apache.jmeter.protocol.http.control.Header(myHeader.getName(), myHeader.getValue()));
//                myHttpPost.addHeader(digestScheme.authenticate(creds,
//                        myHttpPost));

                responseHandler = new BasicResponseHandler();
            }
            else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                httpSamplerResult.setResponseCode(Integer.toString(response
                        .getStatusLine().getStatusCode()));
                httpSamplerResult.setSuccessful(isSuccessCode(response
                        .getStatusLine().getStatusCode()));
                httpSamplerResult.setResponseMessage(response.getStatusLine()
                        .getReasonPhrase());
                return httpSamplerResult;
            }
        }
        catch (MalformedURLException e)
        {

        }
        catch (MalformedChallengeException e)
        {

        }
        catch (AuthenticationException e)
        {

        }

        return httpSamplerResult;
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