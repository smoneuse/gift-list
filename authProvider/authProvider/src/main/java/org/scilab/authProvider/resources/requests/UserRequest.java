package org.scilab.authProvider.resources.requests;

/**
 * Represents a user for register request
 */
public class UserRequest {
    private String user;
    private String password;
    private String role;

    /**
     * Constructor
     */
    public UserRequest(){
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
