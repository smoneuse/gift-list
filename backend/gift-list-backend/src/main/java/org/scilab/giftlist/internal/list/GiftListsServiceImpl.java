package org.scilab.giftlist.internal.list;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation for GiftListsService
 */
public class GiftListsServiceImpl implements GiftListsService{

    @Inject
    @Jpa
    private Repository<GiftList, String> giftListRepository;

    @Inject
    private AuthUserService authUserService;

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public GiftList createList(String ownerId, String title, String description) throws GiftListException {
        AuthUser owner= authUserService.findAccount(ownerId).orElseThrow(()->new GiftListException("Can't create a list without an owner"));
        Specification<GiftList> sameTitleSpec= giftListRepository.getSpecificationBuilder()
                .of(GiftList.class)
                .property("owner.login").equalTo(ownerId)
                .and().property("title").equalTo(description)
                .build();
        if(giftListRepository.contains(sameTitleSpec)){
            throw new GiftListException("A user can't have different lists with same title");
        }
        GiftList createdList=new GiftList(UUID.randomUUID().toString());
        createdList.setTitleAndDescription(title, description);
        createdList.setOwner(owner);
        giftListRepository.add(createdList);
        return createdList;
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public void deleteList(String listId) {
        if(!Strings.isNullOrEmpty(listId) && giftListRepository.contains(listId)){
            giftListRepository.remove(listId);
        }
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Optional<GiftList> single(String listId) {
        if(Strings.isNullOrEmpty(listId)){
            return Optional.empty();
        }
        return giftListRepository.get(listId);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public GiftList update(String listId, String newTitle, String newDescription) throws GiftListException {
        GiftList currentList = giftListRepository.get(listId).orElseThrow(()-> new GiftListException("The list to update could not be found"));
        Specification<GiftList> sameTitleSpec= giftListRepository.getSpecificationBuilder()
                .of(GiftList.class)
                .property("owner.login").equalTo(currentList.getOwner().getLogin())
                .and().property("title").equalTo(newTitle)
                .build();
        if(giftListRepository.contains(sameTitleSpec)){
            throw new GiftListException("A user can't have different lists with same title");
        }
        currentList.setTitleAndDescription(newTitle, newDescription);
        return giftListRepository.update(currentList);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public GiftList addGift(Gift gift, String giftListId) throws GiftListException {
        GiftList giftList = single(giftListId).orElseThrow(()-> new GiftListException("Can't create gift : gift list not found :"+giftListId));
        if(giftList.getGifts().stream().filter(aGift-> aGift.getTitle().equals(gift.getTitle())).count()!=0){
            throw new GiftListException("Can't create Gift : a gift with same title already exist in the same list");
        }
        giftList.addGift(gift);
        return giftListRepository.update(giftList);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public boolean checkUserIsOwner(String userToCheck, GiftList aList) {
        if(Strings.isNullOrEmpty(userToCheck) || aList==null){
            return false;
        }
        return aList.getOwner().getLogin().equals(userToCheck);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public boolean checkUserIsViewer(String userToCheck, GiftList aList) {
        if(Strings.isNullOrEmpty(userToCheck) || aList==null){
            return false;
        }
        for(AuthUser aViewer : aList.getViewers()){
            if(aViewer.getLogin().equals(userToCheck)){
                return true;
            }
        }
        return false;
    }
}
