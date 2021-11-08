package org.scilab.authProvider.internal.security.exceptions;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;

/**
 * Exception for incorrect login <-> password match
 */
public class InvalidCredentialsException extends AuthProviderException {

    /**
     * Exception constructor
     * @param message Issue detail
     */
    public InvalidCredentialsException(String message){
        super(message);
    }
}
