package org.scilab.giftlist.infra.exceptions.security;

import org.scilab.giftlist.infra.exceptions.GiftListException;

/**
 * Sub category like exception for Auth issues
 */
public class AuthException extends GiftListException {
    /**Exception builder*/
    public AuthException(String message){
        super(message);
    }
}
