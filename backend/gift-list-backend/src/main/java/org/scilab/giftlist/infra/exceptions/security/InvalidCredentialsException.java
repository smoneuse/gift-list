package org.scilab.giftlist.infra.exceptions.security;

/**
 * Exception thrown when a user tries to connect with wrong password
 */
public class InvalidCredentialsException extends AuthException{

    /**
     * Exception builder
     * @param message issue details
     */
    public InvalidCredentialsException(String message){
        super(message);
    }
}
