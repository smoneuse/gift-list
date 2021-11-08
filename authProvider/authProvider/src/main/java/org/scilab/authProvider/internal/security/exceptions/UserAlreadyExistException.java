package org.scilab.authProvider.internal.security.exceptions;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;

/**
 * Thrown when a user already exists and not supposed to
 */
public class UserAlreadyExistException extends AuthProviderException {

    /**
     * Builds the exception
     * @param message issue detail
     */
    public UserAlreadyExistException(String message){
        super(message);
    }
}
