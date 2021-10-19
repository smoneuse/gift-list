package org.scilab.giftlist.infra.exceptions;

import java.rmi.server.ExportException;

/**
 * Generic gift list application exception
 */
public class GiftListException extends Exception {
    /**
     * Generic Gift list exception constructor
     * @param message the exception message
     */
    public GiftListException(String message){
        super(message);
    }
}
