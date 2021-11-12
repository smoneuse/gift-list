package org.scilab.giftlist.domain.models.list;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.infra.exceptions.list.GiftListDataAlreadyExistException;
import org.scilab.giftlist.internal.gift.GiftConstants;
import org.scilab.giftlist.internal.gift.GiftStatus;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Gift extends BaseAggregateRoot<String> {
    @Id
    private String id;

    private String title;
    private String comment;
    private int rating;
    @ElementCollection
    private List<String> links;
    private String status;
    @OneToOne
    @JoinColumn(name = "login")
    private AuthUser giver;
    private Date deliveryDate;
    private Date creationDate;
    private Date lastUpdateDate;

    /**Empty constructor required for hibernate*/
    public Gift(){}

    public Gift(String giftId){
        this.id=giftId;
        this.status =GiftStatus.AVAILABLE.toString();
        this.links= new ArrayList<>();
        this.creationDate=new Date(System.currentTimeMillis());
        this.lastUpdateDate= new Date(System.currentTimeMillis());
    }

    public void titleCommentRate(String title, String comment, int rating) throws GiftListException {
        if(Strings.isNullOrEmpty(title)){
            throw new GiftListInvalidParameterException("A gift must have a title");
        }
        this.rating=boundRating(rating);
        this.title=title;
        this.comment= Strings.nullToEmpty(comment);
        this.lastUpdateDate=new Date(System.currentTimeMillis());
    }

    public void addLink(String link) throws GiftListException{
        if(Strings.isNullOrEmpty(link)){
            throw new GiftListInvalidParameterException("Can't add an empty link");
        }
        if(links.stream().filter(aLink->aLink.equals(link)).count()!=0){
            throw new GiftListDataAlreadyExistException("This links is already associated to this gift");
        }
        links.add(link);
        this.lastUpdateDate=new Date(System.currentTimeMillis());
    }

    private int boundRating(int rating){
        if(rating> GiftConstants.MAX_RATING){
            return GiftConstants.MAX_RATING;
        }
        if(rating < GiftConstants.MIN_RATING){
            return GiftConstants.MIN_RATING;
        }
        return rating;
    }

    public void setStatus(GiftStatus giftStatus) {
        this.status = giftStatus.toString();
        this.lastUpdateDate=new Date(System.currentTimeMillis());
    }

    public void setGiver(AuthUser giver) {
        this.giver = giver;
        this.lastUpdateDate=new Date(System.currentTimeMillis());
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
        this.lastUpdateDate=new Date(System.currentTimeMillis());
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

    public String getStatus() {
        return status;
    }

    public AuthUser getGiver() {
        return giver;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public String getId() {
        return id;
    }
}
