package org.scilab.authProvider.internal.security.exceptions;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;

/**
 * Exception for credentials not found
 */
public class CredentialsNotFoundException extends AuthProviderException {

    /**
     * Constructor
     * @param message issue detail
     */
    public CredentialsNotFoundException(String message){
        super(message);
    }
}
