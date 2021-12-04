package org.scilab.giftlist.internal.gift;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.list.GiftTag;
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
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation for GiftService
 */
@Transactional(propagation = Propagation.REQUIRED)
@JpaUnit("appUnit")
public class GiftServiceImpl implements GiftService{
    @Logging
    private Logger logger;

    @Inject
    @Jpa
    private Repository<Gift, String> giftRepository;

    @Inject
    @Jpa
    private Repository<GiftList, String> giftListRepository;

    @Inject
    @Jpa
    private Repository<GiftTag, String> giftTagsRepository;

    @Inject
    private GiftListsService giftListsService;

    @Inject
    private AuthUserService authUserService;

    @Override
    public Gift createGift(String giftListId, String title, String comment, int rating, List<String> links, List<String> tags) throws GiftListException {
        Gift createdGift =new Gift(UUID.randomUUID().toString());
        try {
            createdGift.titleCommentRate(title, comment, rating);
            if(links!=null){
                for(String link : links){
                    createdGift.addLink(link);
                }
            }
            if(tags !=null){
                for(String aTag : tags){
                    createdGift.addTag(getOrCreateTag(aTag));
                }
            }
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

    private GiftTag getOrCreateTag(String tag) throws GiftListException {
        Optional<GiftTag> tagOpt = giftTagsRepository.get(tag.toLowerCase().trim());
        if(tagOpt.isPresent()){
            return tagOpt.get();
        }
        GiftTag newTag= new GiftTag(tag);
        giftTagsRepository.add(newTag);
        return newTag;
    }

    @Override
    public Optional<Gift> single(String giftId) {
        if(Strings.isNullOrEmpty(giftId)){
            return Optional.empty();
        }
        return giftRepository.get(giftId);
    }

    @Override
    public void remove(String listId, String giftId) throws GiftListException {
        if(!Strings.isNullOrEmpty(giftId) && giftRepository.contains(giftId)){
            //First remove from the list
            GiftList list = giftListsService.single(listId).orElseThrow(()->new GiftListException("Cannot delete Gift : can't find list"));
            list.removeGift(giftId);
            giftListRepository.update(list);
            //Deletes the gift
            giftRepository.remove(giftId);
        }
    }

    @Override
    public Gift update(String giftListId, String giftId, String newTitle, String newComment, int newRating) throws GiftListException {
        GiftList giftList= giftListsService.single(giftListId).orElseThrow(()->new GiftListException("Can't update gift : list not found :"+giftListId));
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't update gift : gift not found : "+giftId));
        actualGift.titleCommentRate(newTitle, newComment, newRating);
        return giftRepository.update(actualGift);
    }

    @Override
    public Gift reserve(String giftId, String giverLogin, Date offeringDate) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't reserve gift : gift not found : "+giftId));
        AuthUser giver = authUserService.findAccount(giverLogin).orElseThrow(()->new GiftListException("Can't reserve gift : giver unknown :"+giverLogin));
        if(!actualGift.getStatus().equals(GiftStatus.AVAILABLE.toString())){
            throw new GiftListException("Can't reserve gift : status is not AVAILABLE");
        }
        actualGift.setGiver(giver);
        actualGift.setDeliveryDate(offeringDate);
        actualGift.setStatus(GiftStatus.RESERVED);
        return giftRepository.update(actualGift);
    }

    @Override
    public Gift release(String giftId, String giverLogin) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't release gift : gift not found : "+giftId));
        AuthUser giver = authUserService.findAccount(giverLogin).orElseThrow(()->new GiftListException("Can't release gift : giver unknown :"+giverLogin));
        if(!actualGift.getStatus().equals(GiftStatus.RESERVED.toString())){
            throw new GiftListException("Can't release gift : status is not RESERVED");
        }
        if(!actualGift.getGiver().getLogin().equalsIgnoreCase(giverLogin) && !giver.getRole().equals(GiftListRoles.ADMIN.toString())){
            throw new GiftListException("Can't release gift : Gift can only be released by the same giver or an admin");
        }
        actualGift.setStatus(GiftStatus.AVAILABLE);
        actualGift.setGiver(null);
        actualGift.setDeliveryDate(null);
        return giftRepository.update(actualGift);
    }

    @Override
    public Gift setOffered(String giftId) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't set offered gift : gift not found : "+giftId));
        actualGift.setStatus(GiftStatus.GIVEN);
        return giftRepository.update(actualGift);
    }

    @Override
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
                if(reservedGift.getDeliveryDate().before(actualDate)) {
                    logger.info("Automatically set gift {} to GIVEN", reservedGift.getId());
                    setOffered(reservedGift.getId());
                    updatedGifts.add(reservedGift);
                }
            }
            catch (GiftListException gle){
                logger.warn("Issue while setting gift {} offered : {}", reservedGift.getId(), gle.getMessage());
            }
        }
        return updatedGifts;
    }

    @Override
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

    @Override
    public Gift addTag(String giftId, String aTag) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't add link : gift not found : "+giftId));
        actualGift.addTag(getOrCreateTag(aTag));
        return giftRepository.update(actualGift);
    }

    @Override
    public Gift removeTag(String giftId, String tagToRemove) throws GiftListException {
        Gift actualGift = giftRepository.get(giftId).orElseThrow(()-> new GiftListException("Can't add link : gift not found : "+giftId));
        if(StringUtils.isBlank(tagToRemove) || StringUtils.isBlank(tagToRemove.trim())){
            //Can't remove an empty tag
            return actualGift;
        }
        Optional<GiftTag> tagOpt = giftTagsRepository.get(tagToRemove.trim().toLowerCase());
        if(tagOpt.isPresent()){
            actualGift.removeTag(tagOpt.get());
            return giftRepository.update(actualGift);
        }
        return actualGift;
    }
}
