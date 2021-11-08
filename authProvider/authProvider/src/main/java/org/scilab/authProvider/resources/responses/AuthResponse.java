package org.scilab.authProvider.resources.responses;

import com.google.common.base.Strings;
import org.scilab.authProvider.internal.enums.RequestStatus;

public class AuthResponse {
    private final String status;
    private final String login;
    private final String role;

    /**
     * Representation constructor
     * @param login request user login
     * @param role user registered role role
     * @param status request status
     */
    public AuthResponse(String login, String role, RequestStatus status){
        this.login= Strings.nullToEmpty(login);
        this.role=Strings.nullToEmpty(role);
        this.status=status.toString();
    }

    public String getRole() {
        return role;
    }

    public String getLogin() {
        return login;
    }

    public String getStatus() {
        return status;
    }
}
