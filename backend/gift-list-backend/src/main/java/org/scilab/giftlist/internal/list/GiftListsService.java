package org.scilab.giftlist.internal.list;

import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.seedstack.business.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business service for list management
 */
@Service
public interface GiftListsService {

    /**
     * Creates a list to be associated to the user
     * @param ownerId the user identifier
     * @param description The list title
     * @param comment comment on the list
     * @return @{@link GiftList} the created list
     * @throws GiftListException problem creating the giftList
     */
    public GiftList createList(String ownerId, String description, String comment) throws GiftListException;

    /**
     * Retrieves a single list by its id
     * @param listId the list identifier
     * @return @{@link Optional}&lt;@{@link GiftList}&gt; the list found
     */
    public Optional<GiftList> single(String listId);

    /**
     * Updates the gift List and add the gift to the list
     * @param gift the gift to add
     * @param giftListId the gist list identifier
     * @return the updated Gift list
     * @throws GiftListException gift list doesn't exist
     */
    public GiftList addGift(Gift gift, String giftListId) throws GiftListException;
    /**
     * Updates title and/or description of a gift list
     * @param listId the list identifier
     * @param newTitle the new title to apply to the list
     * @param newDescription the new description to apply to the list
     * @return @{@link GiftList} the updated list
     * @throws GiftListException Issue if list is not found or other with same title exists
     */
    public GiftList update(String listId, String newTitle, String newDescription) throws GiftListException;

    /**
     * Deletes a list;
     * @param listId list identifier
     */
    public void deleteList(String listId) throws GiftListException;

    /**
     * Checks if a user is the owner of a list
     * @param userToCheck the user to check
     * @param aList a list
     * @return boolean
     */
    public boolean checkUserIsOwner(String userToCheck, GiftList aList);

    /**
     * Checks if a user is authorized as viewer of a list
     * @param userToCheck the user to check
     * @param aList a list
     * @return boolean
     */
    public  boolean checkUserIsViewerOrFriend(String userToCheck, GiftList aList);
}
