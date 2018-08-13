package uk.gov.dvla.osg.rpd.web.config;

import org.apache.commons.lang3.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * Session information for the logged in user.
 */
public class Session {
    
    /**
     * ****************************************************************************************
     *                              SINGLETON PATTERN
     * ****************************************************************************************.
     */

    private static class SingletonHelper {
        
        /** The Constant INSTANCE. */
        private static final Session INSTANCE = new Session();
    }

    /**
     * Gets the single instance of Session.
     *
     * @return single instance of Session
     */
    public static Session getInstance() {
        return SingletonHelper.INSTANCE;
    }
    
    /**
     * Instantiates a new session.
     */
    private Session() { }
    
    /** **************************************************************************************. */
    
    private String userName;
    
    /** The password. */
    private String password;
    
    /** The token. */
    private String token;
    
    /** The is admin. */
    private Boolean isAdmin;
    
    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the new user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token the new token
     */
    public void setToken(String token) {
        this.token = token;
    }

    public boolean isLoggedIn() {
        return StringUtils.isNotBlank(token);
    }
    
    /**
     * Checks if user is a member of the admin group.
     *
     * @return true if user is an administrator
     */
    public Boolean isAdmin() {
        return isAdmin;
    }



    /**
     * Sets the isAdmin property for the user.
     *
     * @param admin the new value for admin
     */
    public void setIsAdmin(Boolean admin) {
        this.isAdmin = admin;
    }
}
