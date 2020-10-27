package uk.gov.dvla.osg.rpd.web.client;

import java.io.File;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;
import uk.gov.dvla.osg.rpd.web.json.JsonUtils;
import uk.gov.dvla.osg.rpd.web.xml.xmlUtils;

public class SubmitJobClient {

    static final Logger LOGGER = LogManager.getLogger();

    private RpdErrorResponse error = new RpdErrorResponse();
    private String url;

    /**
     * Gets a new instance of the RpdLoginClient
     * 
     * @param config
     * @return a new instance of the RpdLoginClient.
     */
    public static SubmitJobClient getInstance() {
        return new SubmitJobClient();
    }

    /**
     * Instantiates a new rpd login client.
     *
     * @param config the config
     */
    private SubmitJobClient() {
        this.url = NetworkConfig.getInstance().getSubmitJobUrl();
    }

    public boolean submit(String filename) {
        return trySubmit(new File(filename));
    }

    /**
     * Constructs the header and file body for the HTML message as a MultiPart
     * object and then passes it to the RestClient to send to RPD.
     *
     * @param filename Full path to the file in the working directory
     * @return true, if successful
     */
    public boolean trySubmit(File file) {
        // construct html body with file as attachment
        FileDataBodyPart fdbp = new FileDataBodyPart("file", file, MediaType.TEXT_PLAIN_TYPE);
        
            try (MultiPart multiPart = new MultiPart(MediaType.MULTIPART_FORM_DATA_TYPE);
                    MultiPart body = multiPart.bodyPart(fdbp); 
                    Response response = RestClient.rpdSubmit(url, body)) {
                MediaType mediaType = response.getMediaType();
                String data = response.readEntity(String.class);
                // 202 response means file received by RPD
                if (response.getStatus() == 200) {
                    // File received by RPD, file can be safely deleted
                    FileUtils.deleteQuietly(file);
                    return true;
                } else if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                    error = JsonUtils.getError(data);
                } else if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
                    error = new xmlUtils().getXmlError(data);
                } else {
                    error.setCode("Submit Job Error:");
                    error.setMessage(data);
                    error.setAction("Please notify Dev Team.");
                }
            } catch (ProcessingException ex) {
                error.setCode("Submit Job Error:");
                error.setMessage("Unable to connect to RPD web service. Connection timed out");
                error.setAction("Please wait a few minutes and then try again.");
                error.setException(ex);
            } catch (NullPointerException ex) {
                error.setCode("Submit Job Error:");
                error.setMessage("Unable to connect to RPD web service. Invalid IP address for RPD");
                error.setAction("To resolve, check all parts of the login URL in the application config file.");
                error.setException(ex);
            } catch (IllegalArgumentException ex) {
                error.setCode("Submit Job Error:");
                error.setMessage("Invalid URL in config file [" + url + "]. Please check configuration.");
                error.setAction("To resolve, check all parts of the login URL in the application config file. This problem is usually caused by either a missing value in the URL or an illegal character.");
                error.setException(ex);
            } catch (Exception ex) {
                error.setCode("Submit Job Error:");
                error.setMessage("An unknown error occured while attempting to submit a file to RPD");
                error.setAction("Please notify Dev Team.");
                error.setException(ex);
            }
        return false;
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
