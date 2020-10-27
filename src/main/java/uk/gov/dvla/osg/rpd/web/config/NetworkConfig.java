package uk.gov.dvla.osg.rpd.web.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Class NetworkConfig holds the RPD Rest API URL's.
 * It is loaded from a network configuration properties file which is stored
 * in the local file system. 
 */
public class NetworkConfig {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    /** ****************************************************************************************              SINGLETON PATTERN ****************************************************************************************. */
   private static String filename;

   /**
    * The Class SingletonHelper.
    */
   private static class SingletonHelper {
       
       private static final NetworkConfig INSTANCE = new NetworkConfig();
   }

   /**
    * Gets the single instance of NetworkConfig.
    * If the network properties cannot be loaded to the file an error is logged and the application is terminated.
    *
    * @return single instance of NetworkConfig
    * @throws RuntimeException if the method is called before initialising with the network configuration file
    */
   public static NetworkConfig getInstance() throws RuntimeException {
       if (StringUtils.isBlank(filename)) {
           throw new RuntimeException("Application Configuration not initialised before use");
       }
       return SingletonHelper.INSTANCE;
   }

   /**
    * Initialises the NetworkConfig with the network configuration file.
    *
    * @param file the network configuration file
    * @throws RuntimeException if the configuration file does not exist or if NetworkConfig has already been initialised.
    */
   public static void init(String file) throws RuntimeException {
       if (StringUtils.isBlank(filename)) {
           if (new File(file).isFile()) {
               filename = file;
           } else {
               throw new RuntimeException("Application Configuration File " + filename + " does not exist on filepath.");
           }
       } else {
           throw new RuntimeException("Application Configuration has already been initialised");
       }
   }
   
   /** **************************************************************************************. */

    private String vaultUrl = "";    
    private String loginUrl = "";    
    private String logoutUrl = "";
    private String checkIfAdminUrl = "";    
    private String despatchUrl = "";
    private String submitJobUrl;
    private String passwordUpdateUrl = "";
  
    /**
     * Instantiates a new network config from the fields in the property file.
     */
    private NetworkConfig() {
        
        // PropertyLoader loads the properties from the configuration file and validates each entry
        try {
            PropertyLoader loader = PropertyLoader.getInstance(filename);
            String protocol = loader.getProperty("protocol");
            String host = loader.getProperty("host");
            String port = loader.getProperty("port");
            String urlBase = protocol + host + ":" + port;
            loginUrl = urlBase + loader.getProperty("loginUrl");
            logoutUrl = urlBase + loader.getProperty("logoutUrl");
            vaultUrl = urlBase + loader.getProperty("vaultUrl");
            checkIfAdminUrl = urlBase + loader.getProperty("checkIfAdminUrl");
            //despatchUrl = urlBase + loader.getProperty("despatchUrl");
            submitJobUrl = urlBase + loader.getProperty("submitJobUrl");
            //passwordUpdateUrl = urlBase + loader.getProperty("updateUrl");
        } catch (IOException ex) {
            LOGGER.fatal("Unable to load properties from Network Configuration File {}", filename);
            System.exit(1);
        } catch (RuntimeException ex) {
            // Property value is missing from the file
            LOGGER.fatal("Unable to load properties from Network Configuration File {}", filename);
            LOGGER.fatal(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Gets the RPD vault url.
     *
     * @return the vault url
     */
    public String getvaultUrl() {
        return vaultUrl;
    }
    
    /**
     * Gets the login url.
     *
     * @return the login url
     */
    public String getLoginUrl() {
        return loginUrl;
    }
    
    /**
     * Gets the logout url.
     *
     * @return the logout url
     */
    public String getLogoutUrl() {
        return logoutUrl;
    }
    
    /**
     * Gets the checkIfAdmin url.
     *
     * @return the check if admin url
     */
    public String getCheckIfAdminUrl() {
        return checkIfAdminUrl;
    }

    /**
     * Gets the dispatch url.
     *
     * @return the dispatch url
     */
    public String getDispatchUrl() {
        return despatchUrl;
    }

    /**
     * Gets the submit job url.
     *
     * @return the submit job url
     */
    public String getSubmitJobUrl() {
        return submitJobUrl;
    }
    
    public String getPasswordUpdateUrl() {
        return passwordUpdateUrl;
    }
}
