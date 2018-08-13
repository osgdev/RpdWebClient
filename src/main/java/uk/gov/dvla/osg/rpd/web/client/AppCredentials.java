package uk.gov.dvla.osg.rpd.web.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppCredentials {
    static final Logger LOGGER = LogManager.getLogger();
    private final String userName = "DespatchApp";
    private String password = "";
    private String token = "";
    private String passwordsFile = "J:\\OSG\\OIs\\RPD\\reprint\\config.txt";

    public AppCredentials() {
        try {
            byte[] fileContents = Files.readAllBytes(Paths.get(passwordsFile));
            InputStream reader = new ByteArrayInputStream(fileContents);
            Properties appPasswords = new Properties();
            appPasswords.load(reader);
            this.password = (String) appPasswords.get(userName);
        } catch (Exception ex) {
            LOGGER.fatal("Unable to load application password file.", ex);
        }
    }

    /**
     * @return name of app in RPD
     */
    public String getUsername() {
        return this.userName;
    }

    /**
     * Retrieves current password from config file.
     * 
     * @return current password
     */
    public String getPassword() {

        return this.password;
    }

    /**
     * Sets the App's session token.
     *
     * @param token the new token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the App's session token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

}
