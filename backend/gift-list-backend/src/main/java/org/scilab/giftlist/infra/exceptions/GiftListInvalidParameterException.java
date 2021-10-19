package org.scilab.giftlist.infra.exceptions;

/**
 * Gift list exception for invalid parameter
 */
public class GiftListInvalidParameterException extends GiftListException{

    /**
     * Exception constructor
     * @param message the exception message
     */
    public GiftListInvalidParameterException(String message){
        super(message);
    }
}
