package uk.gov.dvla.osg.rpd.web.client;


import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;
import uk.gov.dvla.osg.rpd.web.json.JsonUtils;

/**
 * Access to admin area granted to dev team only.
 * Retrieves logged in user's group from RPD (json response).
 * Passes response to utility function to check if user is a member of the Dev group.
 */
public class CheckGroupClient {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private RpdErrorResponse error = new RpdErrorResponse();
    private String url;
	
    /**
     * Gets a new instance of the RpdLoginClient
     * 
     * @param config
     * @return a new instance of the RpdLoginClient.
     */
    public static CheckGroupClient getInstance(NetworkConfig config) {
        return new CheckGroupClient(config);
    }

    /**
     * Instantiates a new rpd login client.
     *
     * @param config the config
     */
    private CheckGroupClient(NetworkConfig config) {
        this.url = config.getCheckIfAdminUrl() + Session.getInstance().getUserName();
    }
    
	/**
	 * Checks if is user admin.
	 *
	 * @return the optional
	 */
	public Optional<Boolean> IsUserAdmin() {
	    
		try (Response response = RestClient.rpdGroup(url)) {
		    MediaType mediaType = response.getMediaType();
		    String data = response.readEntity(String.class);
		    
			if (response.getStatus() == 200 && mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
			    LOGGER.trace(data);
				return Optional.of(JsonUtils.isUserInDevGroup(data));
			 } else if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
	                // RPD provides clear error information, and so is mapped to model
	                error = JsonUtils.getError(data);
	            } else {
	                error.setCode("Check Group Error:");
	                error.setMessage("Response is not JSON!");
	                error.setAction("Please notify Dev Team.");
	            }
	        } catch (ProcessingException ex) {
	            error.setCode("Check Group Error:");
	            error.setMessage("Unable to connect to RPD web service. Connection timed out");
	            error.setAction("Please wait a few minutes and then try again.");
	            error.setException(ex);
	        } catch (NullPointerException ex) {
	            error.setCode("Check Group Error:");
	            error.setMessage("Unable to connect to RPD web service. Invalid IP address for RPD");
	            error.setAction("To resolve, check all parts of the login URL in the application config file.");
	            error.setException(ex);
	        } catch (IllegalArgumentException ex) {
	            error.setCode("Check Group Error:");
	            error.setMessage("Invalid URL in config file [" + url + "]. Please check configuration.");
	            error.setAction("To resolve, check all parts of the login URL in the application config file. This problem is usually caused by either a missing value in the URL or an illegal character.");
	            error.setException(ex);
	        } catch (Exception ex) {
	            error.setCode("Check Group Error:");
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
