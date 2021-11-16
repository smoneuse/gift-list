package org.scilab.giftlist.resources.security.models.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model for updating an account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAccountModel {
    private String login;
    private String currentPassword;
    private String newPassword;
    private String newRole;

    public String getLogin() {
        return login;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewRole() {
        return newRole;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }
}
