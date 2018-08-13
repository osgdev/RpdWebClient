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
import uk.gov.dvla.osg.rpd.web.xml.xmlUtils;
import uk.gov.dvla.osg.vault.data.VaultStock;

/**
 * Sends login request to the RPD webservice. Token is obtained from response to
 * authenticate user when submitting files.
 */
public class VaultStockClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private RpdErrorResponse error = new RpdErrorResponse();
    private final String url;

    /**
     * Creates a new instance of VaultStockClient
     * 
     * @param config NetworkConfig object holding the vault url information.
     * @return a new instance of VaultStockClient
     */
    public static VaultStockClient getInstance(NetworkConfig config) {
        return new VaultStockClient(config);
    }

    private VaultStockClient(NetworkConfig config) {
        this.url = config.getvaultUrl();
    }

    /**
     * Requests VaultStock data from RPD. If successful the response is converted to
     * a VaultStock object, otherwise the error response is saved.
     * 
     * @param token the session token to authenticate with RPD.
     * @return VaultStock if session token is valid, an empty optional for error
     *         conditions.
     */
    public Optional<VaultStock> getStock(String token) {
        try (Response response = RestClient.vaultStock(url)) {

            String data = response.readEntity(String.class);
            if (response.getStatus() == 200) {
                LOGGER.trace(data);
                return Optional.ofNullable(JsonUtils.loadStockFile(data));
            }
            MediaType mediaType = response.getMediaType();
            // If RPD has been contacted an RPD error response is recieved in XML format
            if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
                error = new xmlUtils().getXmlError(data);
            } else {
                error.setCode("Login Error:");
                error.setMessage("Response is not JSON!");
                error.setAction("Please notify Dev Team.");
            }
        } catch (ProcessingException ex) {
            error.setCode("Vault Connection Error:");
            error.setMessage("Unable to connect to RPD web service. Connection timed out");
            error.setAction("Please wait a few minutes and then try again. If the problem persits, please contact Dev team.");
        } catch (NullPointerException ex) {
            error.setCode("Vault Connection Error:");
            error.setMessage("Unable to connect to RPD web service. Invalid IP address for RPD");
            error.setAction("To resolve, check all parts of the login URL in the application config file.");
        } catch (IllegalArgumentException ex) {
            error.setCode("Vault Connection Error:");
            error.setMessage("Invalid URL in config file [" + url + "]. Please check configuration.");
            error.setAction("To resolve, check all parts of the login URL in the application config file. " + "This problem is usually caused by either a missing value in the URL or an illegal character.");
        } catch (Exception ex) {
            error.setCode("Vault Connection Error:");
            error.setMessage("Unable to connect to vault.");
            error.setAction("Please contact Dev Team");
        }
        return Optional.empty();
    }

    /**
     * Retrieves the error response if an empty optional was returned from the
     * getStock method.
     * 
     * @return an error response object.
     */
    public RpdErrorResponse getErrorResponse() {
        return error;
    }

}
