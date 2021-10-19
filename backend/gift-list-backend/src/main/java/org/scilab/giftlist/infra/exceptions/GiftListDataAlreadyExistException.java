package org.scilab.giftlist.infra.exceptions;

public class GiftListDataAlreadyExistException extends GiftListException{

    /**
     * GiftListDataAlreadyExistException constructor
     * @param message Exception message
     */
    public GiftListDataAlreadyExistException(String message){
        super(message);
    }
}
