package org.scilab.authProvider.resources.requests;

/**
 * Model for update request
 */
public class UserUpdateRequest {
    String login;
    String password;
    String updatedPassword;
    String updatedRole;

    /**
     * Constructor
     */
    public UserUpdateRequest(){
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUpdatedPassword() {
        return updatedPassword;
    }

    public void setUpdatedPassword(String updatedPassword) {
        this.updatedPassword = updatedPassword;
    }

    public String getUpdatedRole() {
        return updatedRole;
    }

    public void setUpdatedRole(String updatedRole) {
        this.updatedRole = updatedRole;
    }
}
