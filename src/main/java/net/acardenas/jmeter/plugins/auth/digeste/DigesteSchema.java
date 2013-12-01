package net.acardenas.jmeter.plugins.auth.digeste;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * User: acardenas
 */
public class DigesteSchema extends DigestScheme
{
    private static final Logger LOG = LoggingManager.getLoggerForClass();

    private static final String REALM_PARAMETER_KEY = "realm";
    private static final String NONCE_PARAMETER_KEY = "nonce";
    private static final String OPAQUE_PARAMETER_KEY = "opaque";
    private static final String ALGORITHM_PARAMETER_KEY = "algorithm";
    private static final String RESPONSE_PARAMETER_KEY = "response";
    private static final String USERNAME_PARAMETER_KEY = "username";

    @Override
    public String getSchemeName()
    {
        return "digeste";
    }

    private String hashEncode(String aPassword) throws IOException,
            NoSuchAlgorithmException
    {
        String password = aPassword;
        String myReturn = null;
        final MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1);
        ByteArrayOutputStream pwsalt = new ByteArrayOutputStream();
        pwsalt.write(password.getBytes("UTF-8"));
        byte[] unhashedBytes = pwsalt.toByteArray();
        byte[] digestVonPassword = md.digest(unhashedBytes);
        myReturn = convertToHexString(digestVonPassword);
        return myReturn;
    }

    private static String convertToHexString(byte[] data)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++)
        {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int myTwoHalfs = 0;
            do
            {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                {
                    buf.append((char) ('0' + halfbyte));
                } 
                else
                {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (myTwoHalfs++ < 1);
        }
        return buf.toString();
    }

    public Header authenticate(final Credentials credentials,
            final HttpRequest request) throws AuthenticationException
    {
        String realm = getParameter(REALM_PARAMETER_KEY);
        String nonce = getParameter(NONCE_PARAMETER_KEY);
        String opaque = getParameter(OPAQUE_PARAMETER_KEY);
        String algorithm = getParameter(ALGORITHM_PARAMETER_KEY);
        String uname = credentials.getUserPrincipal().getName();
        String myPassword = credentials.getPassword();

        String myResponseHeader = null;
        try
        {
            myPassword = hashEncode(myPassword);
            String ha1 = hashEncode(credentials.getUserPrincipal().getName()
                    + ":" + realm + ":" + myPassword);
            myResponseHeader = hashEncode(ha1 + ":" + nonce);
        } 
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        } 
        catch (NoSuchAlgorithmException e)
        {
            LOG.error(e.getMessage(), e);
        }

        CharArrayBuffer buffer = new CharArrayBuffer(128);
        if (isProxy())
        {
            buffer.append(AUTH.PROXY_AUTH_RESP);
        } 
        else
        {
            buffer.append(AUTH.WWW_AUTH_RESP);
        }
        buffer.append(": DigestE ");

        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(20);
        params.add(new BasicNameValuePair(USERNAME_PARAMETER_KEY, uname));
        params.add(new BasicNameValuePair(REALM_PARAMETER_KEY, realm));
        params.add(new BasicNameValuePair(NONCE_PARAMETER_KEY, nonce));
        params.add(new BasicNameValuePair(RESPONSE_PARAMETER_KEY, myResponseHeader));

        if (algorithm != null)
        {
            params.add(new BasicNameValuePair(ALGORITHM_PARAMETER_KEY, algorithm));
        }

        if (opaque != null)
        {
            params.add(new BasicNameValuePair(OPAQUE_PARAMETER_KEY, opaque));
        }

        for (int i = 0; i < params.size(); i++)
        {
            BasicNameValuePair param = params.get(i);
            if (i > 0)
            {
                buffer.append(", ");
            }
            boolean noQuotes = "nc".equals(param.getName())
                    || "qop".equals(param.getName());
            BasicHeaderValueFormatter.DEFAULT.formatNameValuePair(buffer,
                    param, !noQuotes);
        }
        return new BufferedHeader(buffer);
    }
}
