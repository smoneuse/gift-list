package org.scilab.giftlist.infra.exceptions.security;

/**
 * Exception thrown when registering a user already present
 */
public class UserAlreadyExistException extends AuthException{

    /**
     * Exception builder
     * @param message issue detail
     */
    public UserAlreadyExistException(String message){
        super(message);
    }
}
