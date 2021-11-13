package org.scilab.giftlist.internal.gift;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation for GiftService
 */
public class GiftServiceImpl implements GiftService{
    @Logging
    private Logger logger;

    @Inject
    @Jpa
    private Repository<Gift, String> giftRepository;

    @Inject
    private GiftListsService giftListsService;

    @Inject
    private AuthUserService authUserService;

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift createGift(String giftListId, String title, String comment, int rating) throws GiftListException {
        Gift createdGift =new Gift(UUID.randomUUID().toString());
        try {
            createdGift.titleCommentRate(title, comment, rating);
            giftRepository.add(createdGift);
            giftListsService.addGift(createdGift,giftListId);
            return createdGift;
        }
        catch (GiftListException gle){
            logger.warn("Problem while adding the created gift to the list - deleting");
            if(giftRepository.contains(createdGift.getId())){
                giftRepository.remove(createdGift.getId());
            }
            throw gle;
        }
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Optional<Gift> single(String giftId) {
        if(Strings.isNullOrEmpty(giftId)){
            return Optional.empty();
        }
        return giftRepository.get(giftId);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public void remove(String giftId) {
        if(!Strings.isNullOrEmpty(giftId) && giftRepository.contains(giftId)){
            giftRepository.remove(giftId);
        }
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift update(String giftListId, String giftId, String newTitle, String newComment, int newRating) throws GiftListException {
        GiftList giftList= giftListsService.single(giftListId).orElseThrow(()->new GiftListException("Can't update gift : list not found :"+giftListId));
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't update gift : gift not found : "+giftId));
        if(giftList.getGifts().stream().filter(aGift-> aGift.getTitle().equals(newTitle) && !aGift.getId().equals(actualGift.getId())).count()!=0){
            throw new GiftListException("Can't update gift : another gift with same title already exist in the list");
        }
        actualGift.titleCommentRate(newTitle, newComment, newRating);
        return giftRepository.update(actualGift);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift reserve(String giftId, String giverLogin, Date offeringDate) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't reserve gift : gift not found : "+giftId));
        AuthUser giver = authUserService.findAccount(giverLogin).orElseThrow(()->new GiftListException("Can't reserve gift : giver unknown :"+giverLogin));
        if(!actualGift.getStatus().equals(GiftStatus.AVAILABLE)){
            throw new GiftListException("Can't reserve gift : status is not AVAILABLE");
        }
        actualGift.setGiver(giver);
        actualGift.setDeliveryDate(offeringDate);
        actualGift.setStatus(GiftStatus.RESERVED);
        return giftRepository.update(actualGift);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift release(String giftId, String giverLogin) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't release gift : gift not found : "+giftId));
        AuthUser giver = authUserService.findAccount(giverLogin).orElseThrow(()->new GiftListException("Can't release gift : giver unknown :"+giverLogin));
        if(!actualGift.getStatus().equals(GiftStatus.RESERVED)){
            throw new GiftListException("Can't release gift : status is not RESERVED");
        }
        if(!actualGift.getGiver().getLogin().equals(giverLogin) && !giver.getRole().equals(GiftListRoles.ADMIN)){
            throw new GiftListException("Can't release gift : Gift can only be released by the same giver or an admin");
        }
        actualGift.setStatus(GiftStatus.AVAILABLE);
        actualGift.setGiver(null);
        actualGift.setDeliveryDate(null);
        return giftRepository.update(actualGift);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift setOffered(String giftId) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't set offered gift : gift not found : "+giftId));
        actualGift.setStatus(GiftStatus.GIVEN);
        return giftRepository.update(actualGift);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public List<Gift> scanAndSetOffered() {
        Date actualDate = new Date(System.currentTimeMillis());
        Specification<Gift> reservedGifts = giftRepository.getSpecificationBuilder()
                .of(Gift.class)
                .property("status").equalTo("RESERVED")
                .build();
        List<Gift> reservedGiftList=giftRepository.get(reservedGifts).collect(Collectors.toList());
        List<Gift> updatedGifts= new ArrayList<>();
        for(Gift reservedGift : reservedGiftList){
            try {
                setOffered(reservedGift.getId());
                updatedGifts.add(reservedGift);
            }
            catch (GiftListException gle){
                logger.warn("Issue while setting gift {} offered : {}", reservedGift.getId(), gle.getMessage());
            }
        }
        return updatedGifts;
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public Gift addLink(String giftId, String link) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't add link : gift not found : "+giftId));
        if(Strings.isNullOrEmpty(link)){
            throw new GiftListException("Can't add an empty link to a gift");
        }
        actualGift.addLink(link);
        return giftRepository.update(actualGift);
    }

    @Override
    public Gift removeLink(String giftId, String link) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't add link : gift not found : "+giftId));
        actualGift.removeLink(link);
        return giftRepository.update(actualGift);
    }
}
