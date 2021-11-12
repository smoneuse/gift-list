package org.scilab.giftlist.infra.exceptions.security;

/**
 * Exception for invalid role provided when registering/updating user auth data
 */
public class InvalidRoleException extends AuthException{

    /**
     * Exception builder
     * @param message issue detail
     */
    public InvalidRoleException(String message){
        super(message);
    }
}
