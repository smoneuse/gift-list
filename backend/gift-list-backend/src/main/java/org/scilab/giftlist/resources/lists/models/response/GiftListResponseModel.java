package org.scilab.giftlist.resources.lists.models.response;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.internal.gift.GiftStatus;
import org.scilab.giftlist.internal.misc.GLDateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Response model for gift list
 */
public class GiftListResponseModel {

    private final String id;
    private final String title;
    private final String description;
    private final List<String> authorizedViewers;
    private final List<GiftListGiftResponseModel> gifts;

    public GiftListResponseModel(GiftList giftList){
        this.id=giftList.getId();
        this.title=giftList.getTitle();
        this.description= Strings.nullToEmpty(giftList.getDescription());
        this.authorizedViewers=new ArrayList<>();
        this.gifts = new ArrayList<>();
    }

    /**
     * The owner should not see if someone has reserved his gift, so hiding status giver and delivery date when reserved
     * @param aGift the gift to add
     */
    public void addGiftResponseForOwner(Gift aGift){
        GiftListGiftResponseModel giftResponse = new GiftListGiftResponseModel(aGift);
        if(aGift.getStatus().equals(GiftStatus.RESERVED.toString())){
            giftResponse.setStatus(GiftStatus.AVAILABLE.toString());
        }
        else{
            giftResponse.setStatus(aGift.getStatus());
        }
        if(aGift.getStatus().equals(GiftStatus.GIVEN)){
            giftResponse.setGenerousGiver(aGift.getGiver().getLogin());
            giftResponse.setOfferingDate(GLDateUtils.formatDate(aGift.getDeliveryDate()));
        }
        gifts.add(giftResponse);
    }

    /**
     * A viewer can see all details
     * @param aGift gift to add
     */
    public void addGiftResponseForViewer(Gift aGift){
        GiftListGiftResponseModel giftResponse = new GiftListGiftResponseModel(aGift);
        giftResponse.setStatus(aGift.getStatus());
        giftResponse.setGenerousGiver(aGift.getGiver().getLogin());
        giftResponse.setOfferingDate(GLDateUtils.formatDate(aGift.getDeliveryDate()));
        gifts.add(giftResponse);
    }

    /**
     * Only the owner can see who has access to his list
     * @param viewer viewer to add
     */
    public void addViewer(String viewer){
        this.authorizedViewers.add(viewer);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthorizedViewers() {
        return authorizedViewers;
    }

    public List<GiftListGiftResponseModel> getGifts() {
        return gifts;
    }

    private class GiftListGiftResponseModel{
        private final String title;
        private final String comment;
        private final int rating;
        private final List<String> links;
        private final String created;
        private final String lastUpdate;
        private String status;
        private String offeringDate;
        private String generousGiver;


        public GiftListGiftResponseModel(Gift aGift){
            this.title=aGift.getTitle();
            this.comment=Strings.nullToEmpty(aGift.getComment());
            this.rating=aGift.getRating();
            this.links = aGift.getLinks();
            this.created= GLDateUtils.formatDate(aGift.getCreationDate());
            this.lastUpdate=GLDateUtils.formatDate(aGift.getLastUpdateDate());
        }

        public void setGenerousGiver(String generousGiver) {
            this.generousGiver = generousGiver;
        }

        public void setOfferingDate(String offeringDate) {
            this.offeringDate = offeringDate;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getComment() {
            return comment;
        }

        public int getRating() {
            return rating;
        }

        public List<String> getLinks() {
            return links;
        }

        public String getCreated() {
            return created;
        }

        public String getLastUpdate() {
            return lastUpdate;
        }

        public String getStatus() {
            return status;
        }

        public String getOfferingDate() {
            return offeringDate;
        }

        public String getGenerousGiver() {
            return generousGiver;
        }
    }
}
