package uk.gov.dvla.osg.rpd.web.json;

import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.google.gson.*;

import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;
import uk.gov.dvla.osg.vault.data.VaultStock;

/**
 * Utility methods to extract information from the JSON data responses that are
 * returned from the RPD REST api.
 */
public class JsonUtils {
    
    private static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

    /**
     * Extracts the user token from message body of a successful RPD login request
     * 
     * @param jsonString RPD login request message body
     * @return session token, or blank string if token not available
     */
    public static String getTokenFromJson(String jsonString) throws IllegalStateException, JsonSyntaxException {
        return new JsonParser().parse(jsonString).getAsJsonObject().get("token").getAsString();
    }

    /**
     * Deserializes the Json read from the supplied file.
     * 
     * @param jsonFile File retrieved from the Vault WebService.
     * @return Stock information from the Vault.
     */
    public static VaultStock loadStockFile(String jsonFile) throws JsonIOException, JsonSyntaxException {
        Gson gsonBldr = new GsonBuilder().registerTypeAdapter(VaultStock.class, new EmptyStringAsNullTypeAdapter()).create();
        if (DEBUG_MODE) {
            try (FileReader fr = new FileReader(jsonFile)) {
                return gsonBldr.fromJson(fr, VaultStock.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
        return gsonBldr.fromJson(jsonFile, VaultStock.class);
    }
    
    /**
     * Deserializes the error response received from RPD
     *
     * @param data the JSON string containing the error data
     * @return the error
     * @throws JsonIOException the json IO exception
     * @throws JsonSyntaxException the json syntax exception
     */
    public static RpdErrorResponse getError(String data) throws JsonIOException, JsonSyntaxException {
        Gson gsonBldr = new GsonBuilder().registerTypeAdapter(RpdErrorResponse.class, new RpdErrorTypeAdapter()).create();
        return gsonBldr.fromJson(data, RpdErrorResponse.class);
    }
    
    /**
     * Extracts the user group from the message body of a request.
     * @param jsonString Json to search in.
     * @return true if developer, otherwise false.
     * @throws JsonSyntaxException the json syntax exception
     */
    public static boolean isUserInDevGroup(String jsonString) throws JsonSyntaxException {
        try {
            JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
            // loop through and check all groups the user is a member of
            if (json.has("User.Groups") && json.get("User.Groups").isJsonArray()) {
                for (JsonElement group : json.get("User.Groups").getAsJsonArray()) {
                    if (group.getAsString().equalsIgnoreCase("dev")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }
}