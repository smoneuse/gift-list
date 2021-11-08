package org.scilab.authProvider.internal.exceptions;

/**
 * Global exception for Authprovider application
 */
public class AuthProviderException extends Exception {

    /**
     * Exception constructor
     * @param message issue details
     */
    public AuthProviderException(String message){
        super(message);
    }
}
