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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation for AccountService
 */
@Transactional
@JpaUnit("appUnit")
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
    public Set<GiftList> ownedLists(String ownerLogin) throws  GiftListException{
        AuthUser owner= authUserService.findAccount(ownerLogin).orElseThrow(()->new GiftListException("Can't retrieve lists without a valid owner"));
        return owner.getOwnedLists();
    }

    @Override
    public void addViewer(String viewerLogin, String giftListId) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't grant view permission : user not found :"+viewerLogin));
        GiftList giftList= giftListStringRepository.get(giftListId).orElseThrow(()->new GiftListException("Can't grant view permission :  list wasn't found"));
        if(giftList.getOwner().getLogin().equalsIgnoreCase(viewerLogin)){
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
    public void revokeViewer(String viewerLogin, String giftListId) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't revoke view permission : user not found :"+viewerLogin));
        GiftList giftList= giftListStringRepository.get(giftListId).orElseThrow(()->new GiftListException("Can't revoke view permission :  list wasn't found"));
        if(giftList.getOwner().getLogin().equalsIgnoreCase(viewerLogin)){
            throw new GiftListException("An owner can't revoke view permission to himself");
        }
        logger.info("Removing user {} view permission on list {}", viewerLogin, giftListId);
        viewer.removeFromAuthorizedList(giftList);
    }

    @Override
    public Set<GiftList> authorizedLists(String viewerLogin) throws GiftListException {
        AuthUser viewer = authUserService.findAccount(viewerLogin).orElseThrow(()-> new GiftListException("Can't find authorized lists : user not found :"+viewerLogin));
        Set<GiftList> authLists= new HashSet<>(viewer.getAuthorizedLists());

        Specification<AuthUser> friendFor= authUserStringRepository.getSpecificationBuilder()
                .of(AuthUser.class)
                .property("friends.login").equalTo(viewerLogin).ignoringCase()
                .build();
        Set<AuthUser> hasDeclaredViewerAsFriend=authUserStringRepository.get(friendFor).collect(Collectors.toSet());
        //Adding the lists owned by user with viewer declared as Friend
        for(AuthUser aFriend : hasDeclaredViewerAsFriend){
            authLists.addAll(this.ownedLists(aFriend.getLogin()));
        }
        return authLists;
    }

    @Override
    public Set<AuthUser> listViewers(String listId) throws GiftListException {
        GiftList giftList = giftListStringRepository.get(listId).orElseThrow(()-> new GiftListException("Can't find viewers of a non existing list"));
        return giftList.getViewers();
    }

    @Override
    public AuthUser addFriend(AuthUser user, String friendLogin) throws GiftListException {
        AuthUser theFriend=authUserService.findAccount(friendLogin).orElseThrow(()-> new GiftListException("Can't add friend, no user with login :"+friendLogin));
        user.addFriend(theFriend);
        return authUserStringRepository.update(user);
    }

    @Override
    public void removeFriend(AuthUser user, String friendLogin){
        Optional<AuthUser> friendOpt= authUserService.findAccount(friendLogin);
        if(friendOpt.isPresent()){
            user.removeFriend(friendOpt.get());
            authUserStringRepository.update(user);
        }
    }
}
