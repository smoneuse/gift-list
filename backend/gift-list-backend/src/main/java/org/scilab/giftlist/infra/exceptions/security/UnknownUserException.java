package org.scilab.giftlist.infra.exceptions.security;

/**
 * Thrown when a non registered user tries to connect
 */
public class UnknownUserException extends AuthException{

    /**
     * Exception builder
     * @param message issue detail
     */
    public UnknownUserException(String message){
        super(message);
    }
}
