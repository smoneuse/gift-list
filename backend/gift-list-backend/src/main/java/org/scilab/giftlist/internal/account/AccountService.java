package org.scilab.giftlist.internal.account;

import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.seedstack.business.Service;

import java.util.List;

/**
 * Business service for account - list relations
 */
@Service
public interface AccountService {

    /**
     * Retrieves a user owned lists
     * @param ownerLogin the user login
     * @return @{@link List}&lt;@{@link GiftList}&gt; the user's owned lists
     */
    public List<GiftList> ownedLists(String ownerLogin) throws GiftListException;

    /**
     * Grants the view permission to a user on a particular list
     * @param viewerLogin viewer identifier
     * @param giftListId gift list identifier
     * @throws GiftListException User not found / List not found
     */
    public void addViewer(String viewerLogin, String giftListId) throws GiftListException;

    /**
     * Revokes the view permission for a user on a particular list
     * @param viewerLogin the viewer login
     * @param giftListId the gift list identifier
     * @throws GiftListException User not found / List not found
     */
    public void revokeViewer(String viewerLogin, String giftListId) throws GiftListException;

    /**
     * Retrieves all lists a user has view on
     * @param viewerLogin the viewer login
     * @return @{@link List}&lt;@{@link GiftList}&gt; gift Lists the user has view permission granted on
     * @throws GiftListException User not found
     */
    public List<GiftList> authorizedLists(String viewerLogin) throws GiftListException;

    /**
     * Retrieves all viewers of a particular list
     * @param listId the list identifier
     * @return @{@link List}&lt;@{@link AuthUser}&gt; the list viewers
     * @throws GiftListException list not found
     */
    public List<AuthUser> listViewers(String listId) throws GiftListException;
}
