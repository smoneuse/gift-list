package org.scilab.authProvider.internal.security.exceptions;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;

/**
 * Exception thrown when user is no registered
 */
public class UnknownUserException extends AuthProviderException {

    /**
     * Builds the exception
     * @param message issue detail
     */
    public UnknownUserException(String message){
        super(message);
    }
}
