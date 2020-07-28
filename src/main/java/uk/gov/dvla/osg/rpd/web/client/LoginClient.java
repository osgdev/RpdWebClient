package uk.gov.dvla.osg.rpd.web.client;

import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;
import uk.gov.dvla.osg.rpd.web.json.JsonUtils;

/**
 * Sends login request to the RPD webservice. Token is obtained from response to
 * authenticate user when submitting files.
 */
public class LoginClient {

    static final Logger LOGGER = LogManager.getLogger();
    
    private RpdErrorResponse error = new RpdErrorResponse();
    private final String url;

    /**
     * Gets a new instance of the RpdLoginClient
     * 
     * @param config
     * @return a new instance of the RpdLoginClient.
     */
    public static LoginClient getInstance() {
        return new LoginClient();
    }

    /**
     * Instantiates a new rpd login client.
     *
     * @param config the config
     */
    private LoginClient() {
        this.url = NetworkConfig.getInstance().getLoginUrl();
    }
    /**
     * Contacts RPD and attempts to retrieve a session token using the supplied
     * credentials.
     * 
     * @param userName the RPD login name of the user.
     * @param password the RPD password for the user.
     * @return a session token if the credentials are valid, for all other
     *         conditions an empty optional.
     */
    public Optional<String> getSessionToken(String userName, String password) {
        try (Response response = RestClient.rpdLogin(url, userName, password)) {

            String data = response.readEntity(String.class);
            // If RPD url is incorrect or RPD is not available an HTML response is returned
            MediaType mediaType = response.getMediaType();
            
            if (response.getStatus() == 200 && mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                LOGGER.trace(data);
                String token = JsonUtils.getTokenFromJson(data);
                return Optional.of(token);
            } else if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                // RPD provides clear error information, and so is mapped to model
                error = JsonUtils.getError(data);
            } else {
                error.setCode("Login Error:");
                error.setName(data);
                error.setMessage("Response is not valid JSON!");
                error.setAction("Please notify Dev Team.");
            }
        } catch (ProcessingException ex) {
            error.setCode("Login Error:");
            error.setName("Processing Exception");
            error.setMessage("Unable to connect to RPD web service. Connection timed out");
            error.setAction("Please wait a few minutes and then try again.");
            error.setException(ex);
        } catch (NullPointerException ex) {
            error.setCode("Login Error:");
            error.setName("NullPointerException");
            error.setMessage("Unable to connect to RPD web service. Invalid IP address for RPD");
            error.setAction("To resolve, check all parts of the login URL in the application config file.");
            error.setException(ex);
        } catch (IllegalArgumentException ex) {
            error.setCode("Login Error:");
            error.setName("IllegalArgumentException");
            error.setMessage("Invalid URL in config file [" + url + "]. Please check configuration.");
            error.setAction("To resolve, check all parts of the login URL in the application config file. This problem is usually caused by either a missing value in the URL or an illegal character.");
            error.setException(ex);
        } catch (Exception ex) {
            error.setCode("Login Error:");
            error.setName("General Exception");
            error.setMessage("An unknown error occured while attempting to login to RPD");
            error.setAction("Please notify Dev Team.");
            error.setException(ex);
        }
        return Optional.empty();
    }

    /**
     * Retrieves the error response if an empty optional was returned from the
     * getSessionToken method.
     * 
     * @return an error response object.
     */
    public RpdErrorResponse getErrorResponse() {
        return error;
    }

}
