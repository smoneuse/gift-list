package org.scilab.giftlist.internal.account;

import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.list.GiftListDataAlreadyExistException;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for AccountService
 */
public class AccountServiceImpl implements AccountService{
    @Logging
    private Logger logger;

    @Inject
    private AuthUserService authUserService;

    @Inject
    @Jpa
    private Repository<GiftList,String> giftListStringRepository;

    @Inject
    @Jpa
    private Repository<AuthUser, String> authUserStringRepository;

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public List<GiftList> ownedLists(String ownerLogin) throws  GiftListException{
        AuthUser owner= authUserService.findAccount(ownerLogin).orElseThrow(()->new GiftListException("Can't retrieve lists without a valid owner"));
        return owner.getOwnedLists();
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public void addViewer(String viewerLogin, String giftListId) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't grant view permission : user not found :"+viewerLogin));
        GiftList giftList= giftListStringRepository.get(giftListId).orElseThrow(()->new GiftListException("Can't grant view permission :  list wasn't found"));
        if(giftList.getOwner().getLogin().equals(viewerLogin)){
            throw new GiftListException("An owner can't grant view permission to himself");
        }
        try {
            viewer.addAuthorizedList(giftList);
            authUserStringRepository.update(viewer);
        }
        catch(GiftListDataAlreadyExistException glde){
            //Just log
            logger.info("Grant view permission for user {} on list {} : Already granted !", viewerLogin, giftListId);
        }
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public void revokeViewer(String viewerLogin, String giftListId) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't revoke view permission : user not found :"+viewerLogin));
        GiftList giftList= giftListStringRepository.get(giftListId).orElseThrow(()->new GiftListException("Can't revoke view permission :  list wasn't found"));
        if(giftList.getOwner().getLogin().equals(viewerLogin)){
            throw new GiftListException("An owner can't revoke view permission to himself");
        }
        logger.info("Removing user {} view permission on list {}", viewerLogin, giftListId);
        viewer.removeFromAuthorizedList(giftList);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public List<GiftList> authorizedLists(String viewerLogin) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't find authorized lists : user not found :"+viewerLogin));
        return viewer.getAuthorizedLists();
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public List<AuthUser> listViewers(String listId) throws GiftListException {
        GiftList giftList = giftListStringRepository.get(listId).orElseThrow(()-> new GiftListException("Can't find viewers of a non existing list"));
        return giftList.getViewers();
    }
}
