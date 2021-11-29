package org.scilab.giftlist.resources.gifts.models.response;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftTag;
import org.scilab.giftlist.internal.misc.GLDateUtils;
import org.scilab.giftlist.internal.misc.LinksUtils;

import java.util.ArrayList;
import java.util.List;

public class GiftResponseModel {
    private final String giftId;
    private final String listId;
    private final String title;
    private final String comment;
    private final int rating;
    private final List<String> links;
    private final List<String> tags;
    private final String created;
    private final String lastUpdate;
    private String status;
    private String offeringDate;
    private String generousGiver;

    public GiftResponseModel(Gift aGift, String listId){
        this.giftId=aGift.getId();
        this.listId=listId;
        this.title=aGift.getTitle();
        this.comment= Strings.nullToEmpty(aGift.getComment());
        this.rating=aGift.getRating();
        this.links = LinksUtils.toList(aGift.getLinks());
        this.created= GLDateUtils.formatDate(aGift.getCreationDate());
        this.lastUpdate=GLDateUtils.formatDate(aGift.getLastUpdateDate());
        this.tags = new ArrayList<>();
        for(GiftTag giftTag :aGift.getTags()){
            this.tags.add(giftTag.getName());
        }
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

    public String getListId() {
        return listId;
    }

    public String getGiftId() {
        return giftId;
    }

    public List<String> getTags() {
        return tags;
    }
}
