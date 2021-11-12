package org.scilab.giftlist.resources.security.models.request;

/**
 * Request model for creating an account
 */
public class CreateAccountModel {
    public String login;
    public String password;
    public String role;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}
