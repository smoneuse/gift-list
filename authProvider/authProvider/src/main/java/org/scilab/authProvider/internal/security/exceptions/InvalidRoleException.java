package org.scilab.authProvider.internal.security.exceptions;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;

/**
 * Security exception for Invalid Role
 */
public class InvalidRoleException extends AuthProviderException {

    /**
     * Constructor
     * @param message issue details
     */
    public InvalidRoleException(String message){
        super(message);
    }
}
