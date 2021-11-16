package org.scilab.giftlist.internal.gift;

import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.seedstack.business.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Gift related business service
 */
@Service
public interface GiftService {

    /**
     * Creates a gift and link it to a list
     * @param giftListId The gift list identifier
     * @param title The gift title
     * @param comment a comment on a gift
     * @param rating the gift rating
     * @param links the links to the gift
     * @return The created Gift
     * @throws GiftListException list doesn't exist / title is not set or already exist in the list
     */
    public Gift createGift(String giftListId, String title, String comment, int rating, List<String> links) throws GiftListException;

    /**
     * Provides a single gift by its identifier
     * @param giftId thr gift identifier
     * @return @{@link Optional}&lt;@{@link Gift}&gt; the required gift
     */
    public Optional<Gift> single(String giftId);

    /**
     * Deletes a gift
     * @param giftId the gift Identifier
     * @param listId the list identifier
     * @throws GiftListException Issue while removing the gift
     */
    public void remove(String listId,String giftId) throws GiftListException;

    /**
     * Updates a gift
     * @param giftListId the gift list identifier
     * @param giftId the gift identifier
     * @param newTitle the new title to apply
     * @param newComment the new comment to apply
     * @param newRating the new rating to apply
     * @return the updated @{@link Gift}
     * @throws GiftListException Gift identifier is not provided / Title already exist in the same list
     */
    public Gift update(String giftListId,String giftId, String newTitle, String newComment, int newRating) throws GiftListException;

    /**
     * Reserves a gift
     * @param giftId identifier of the gift to reserve
     * @param giverLogin giver login
     * @param offeringDate date when a gift is planned to be offered
     * @return the updated Gift
     * @throws GiftListException gift not found / user not found / Gift is not available
     */
    public Gift reserve(String giftId, String giverLogin, Date offeringDate) throws GiftListException;

    /**
     * Releases a reserved gift
     * @param giftId the gift identifier
     * @param giverLogin the giver login
     * @return @{@link Gift} the updated gift
     * @throws GiftListException Gift not found / user not found / Gift is not reserved / giver provided is not correct
     */
    public Gift release(String giftId, String giverLogin) throws GiftListException;

    /**
     * Sets the gift to given status
     * This method should be called automatically
     * @param giftId the gift identifier
     * @return @{@link Gift} updated gift
     * @throws GiftListException Gift could not be found
     */
    public Gift setOffered(String giftId) throws GiftListException;

    /**
     * Scan the repository for gifts with delivering dates older than the current date and set them offered
     * @return @{@link List}&lt;@{@link Gift}&gt; the updated gifts
     */
    public List<Gift> scanAndSetOffered();

    /**
     * Adds a link to a gift
     * @param giftId the gift identifier
     * @param link the link to add
     * @return @{@link Gift} the updated gift
     * @throws GiftListException Gift not found
     */
    public Gift addLink(String giftId, String link) throws GiftListException;


    /**
     * Removes a link to a gift
     * @param giftId the gift identifier
     * @param link the link to remove
     * @return @{@link Gift} the updated gift
     * @throws GiftListException Gift not found
     */
    public Gift removeLink(String giftId, String link) throws GiftListException;
}
