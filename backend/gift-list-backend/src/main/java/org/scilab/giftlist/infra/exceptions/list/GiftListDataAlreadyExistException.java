package org.scilab.giftlist.infra.exceptions.list;

import org.scilab.giftlist.infra.exceptions.GiftListException;

public class GiftListDataAlreadyExistException extends GiftListException {

    /**
     * GiftListDataAlreadyExistException constructor
     * @param message Exception message
     */
    public GiftListDataAlreadyExistException(String message){
        super(message);
    }
}
