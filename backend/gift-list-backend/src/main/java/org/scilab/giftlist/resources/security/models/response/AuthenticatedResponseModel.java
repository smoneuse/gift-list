package org.scilab.giftlist.resources.security.models.response;

/**
 * Response model for isAuthenticated
 */
public class AuthenticatedResponseModel {
    private final boolean authenticated;
    
    public AuthenticatedResponseModel(boolean authenticated){
        this.authenticated=authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
