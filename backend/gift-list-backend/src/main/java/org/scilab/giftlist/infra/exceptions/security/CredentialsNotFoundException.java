package org.scilab.giftlist.infra.exceptions.security;

/**
 * Missing credential for registering / updating user auth data
 */
public class CredentialsNotFoundException extends AuthException{
    /**
     * Exception builder
     * @param message the issue details
     */
    public CredentialsNotFoundException(String message){
        super(message);
    }
}
