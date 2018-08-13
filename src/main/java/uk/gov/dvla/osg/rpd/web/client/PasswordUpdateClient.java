package uk.gov.dvla.osg.rpd.web.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class PasswordUpdateClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private RpdErrorResponse error = new RpdErrorResponse();
    private String url;
    private String appName;

    /**
     * Gets a new instance of the RpdLoginClient
     * 
     * @param config
     * @return a new instance of the RpdLoginClient.
     */
    public static PasswordUpdateClient getInstance(NetworkConfig config, String appName) {
        return new PasswordUpdateClient(config, appName);
    }

    /**
     * Instantiates a new rpd login client.
     *
     * @param config the config
     */
    private PasswordUpdateClient(NetworkConfig config, String appName) {
        this.url = config.getPasswordUpdateUrl() + appName;
        this.appName = appName;
    }

    /**
     * Request RPD updaes the password for the applicaiton. This may fail due to the
     * provided password being too similar to the previous one.
     * 
     * @param config Network configuration data required to build the URL
     * @param appName Application whose credentials are being updated
     * @param token Session token required by RPD
     * @param json RPD reqires the HTML body to be in JSON format
     * @return true if password succesfully updated in RPD
     */
    public boolean rpdUpdatePwd(String token, String json) {

        // Create an Apache HttpClient as Jersey client has no PATCH method
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
            HttpPatch httpPatch = new HttpPatch(url);
            // Add message headers
            httpPatch.addHeader("token", token);
            // Set content type and message body
            StringEntity params = new StringEntity(json);
            params.setContentType("application/json");
            httpPatch.setEntity(params);
            // Send the request to RPD
            try (CloseableHttpResponse response = httpclient.execute(httpPatch)) {
                int statusCode = response.getStatusLine().getStatusCode();
                LOGGER.trace("Response Code for" + appName + ": " + response.getStatusLine().getStatusCode());
                // Check the status of the response
                if (statusCode == 200) {
                    LOGGER.info(appName + " password updated");
                    return true;
                }
                error.setCode(String.valueOf(statusCode));
                error.setName("Update Password");
                error.setMessage("Unable to update password for " + appName + ", Error code = " + statusCode);
                error.setAction("Please notify Dev Team.");
            }
        } catch (HttpHostConnectException ex) {
            error.setCode("Password Update Error:");
            error.setName("HttpHostConnectException");
            error.setMessage("Unable to connect to RPD!");
            error.setAction("Please notify Dev Team.");
            error.setException(ex);
        } catch (Exception ex) {
            error.setCode("Password Update Error:");
            error.setName("General Exception");
            error.setMessage("An error occured while updating the password.");
            error.setAction("Please notify Dev Team.");
            error.setException(ex);
        }
        return false;
    }
}
