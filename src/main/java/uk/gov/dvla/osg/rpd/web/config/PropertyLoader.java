package uk.gov.dvla.osg.rpd.web.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class PropertyLoader loads the properties from the configuration file.
 */
public class PropertyLoader {
    
    private static final Properties properties = new Properties();
    
    /**
     * Gets the intstance.
     *
     * @param filename the filename
     * @return the intstance
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static PropertyLoader getInstance(String filename) throws IOException {
        return new PropertyLoader(filename);
    }
    
    /**
     * Instantiates a new property loader.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private PropertyLoader(String filename) throws IOException {
        try (FileInputStream input = new FileInputStream(new File(filename))) {
            properties.load(input);
        }
    }
    
    /**
     * Gets the string property matching the provided key.
     *
     * @param key the key to match
     * @return the property for the key
     * @throws RuntimeException if the key is not present in the configuration file
     */
    public String getProperty(String key) throws RuntimeException {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        throw new RuntimeException("Unable to load property ["+key+"] from file.");
    }
    
    /**
     * Gets the int property matching the provided key.
     *
     * @param key the key to match
     * @return the property for the key
     * @throws RuntimeException if the key is not present in the configuration file or is not a valid integer
     */
    public int getPropertyInt(String key) throws RuntimeException {
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            if (StringUtils.isNumeric(value)) {
                return Integer.parseInt(value);
            }
            throw new RuntimeException("Value ["+value+"] is not valid for the property ["+key+"].");
        }
        throw new RuntimeException("Unable to load property ["+key+"] from Production Configuration file.");
    }
}
