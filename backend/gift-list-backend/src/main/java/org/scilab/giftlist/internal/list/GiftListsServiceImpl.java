package org.scilab.giftlist.internal.list;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.internal.gift.GiftService;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation for GiftListsService
 */
@Transactional(propagation = Propagation.REQUIRED)
@JpaUnit("appUnit")
public class GiftListsServiceImpl implements GiftListsService{

    @Inject
    @Jpa
    private Repository<GiftList, String> giftListRepository;

    @Inject
    private AuthUserService authUserService;

    @Inject
    private GiftService giftService;

    @Override
    public GiftList createList(String ownerId, String title, String description) throws GiftListException {
        AuthUser owner= authUserService.findAccount(ownerId).orElseThrow(()->new GiftListException("Can't create a list without an owner"));
        Specification<GiftList> sameOwnerSpec= giftListRepository.getSpecificationBuilder()
                .of(GiftList.class)
                .property("owner.login").equalTo(ownerId)
                .build();
        if(giftListRepository.get(sameOwnerSpec).filter(aList-> aList.getTitle().equals(title)).count()>0){
            throw new GiftListException("A user can't have different lists with same title");
        }
        GiftList createdList=new GiftList(UUID.randomUUID().toString());
        createdList.setTitleAndDescription(title, description);
        createdList.setOwner(owner);
        giftListRepository.add(createdList);
        return createdList;
    }

    @Override
    public void deleteList(String listId) throws GiftListException {
        if(!Strings.isNullOrEmpty(listId) && giftListRepository.contains(listId)){
            //Delete All this list gifts
            GiftList actualList = single(listId).orElseThrow(()->new GiftListException("Can't find list for deleting inner gifts :"+listId));
            List<String> giftIdsToDelete = new ArrayList<>();
            actualList.getGifts().forEach(aGift->giftIdsToDelete.add(aGift.getId()));
            for(String deleteGiftId : giftIdsToDelete){
                giftService.remove(listId, deleteGiftId);
            }
            //Once done, removing the list itself
            giftListRepository.remove(listId);
        }
    }

    @Override
    public Optional<GiftList> single(String listId) {
        if(Strings.isNullOrEmpty(listId)){
            return Optional.empty();
        }
        return giftListRepository.get(listId);
    }

    @Override
    public GiftList update(String listId, String newTitle, String newDescription) throws GiftListException {
        GiftList currentList = giftListRepository.get(listId).orElseThrow(()-> new GiftListException("The list to update could not be found"));
        Specification<GiftList> sameTitleSpec= giftListRepository.getSpecificationBuilder()
                .of(GiftList.class)
                .property("owner.login").equalTo(currentList.getOwner().getLogin())
                .and().property("title").equalTo(newTitle)
                .and().property("id").not().equalTo(listId)
                .build();
        if(giftListRepository.contains(sameTitleSpec)){
            throw new GiftListException("A user can't have different lists with same title");
        }
        currentList.setTitleAndDescription(newTitle, newDescription);
        return giftListRepository.update(currentList);
    }

    @Override
    public GiftList addGift(Gift gift, String giftListId) throws GiftListException {
        GiftList giftList = single(giftListId).orElseThrow(()-> new GiftListException("Can't create gift : gift list not found :"+giftListId));
        giftList.addGift(gift);
        return giftListRepository.update(giftList);
    }

    @Override
    public boolean checkUserIsOwner(String userToCheck, GiftList aList) {
        if(Strings.isNullOrEmpty(userToCheck) || aList==null){
            return false;
        }
        return aList.getOwner().getLogin().equals(userToCheck);
    }

    @Override
    public boolean checkUserIsViewerOrFriend(String userToCheck, GiftList aList) {
        if(Strings.isNullOrEmpty(userToCheck) || aList==null){
            return false;
        }
        for(AuthUser aViewer : aList.getViewers()){
            if(aViewer.getLogin().equals(userToCheck)){
                return true;
            }
        }
        //Check if it's a friend
        AuthUser listOwner =authUserService.findAccount(aList.getOwner().getLogin()).get();
        for(AuthUser aFriend : listOwner.getFriends()){
            if(aFriend.getLogin().equals(userToCheck)){
                return true;
            }
        }
        return false;
    }
}
