package org.scilab.giftlist.domain.models.list;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.infra.exceptions.list.GiftListDataAlreadyExistException;
import org.scilab.giftlist.internal.gift.GiftConstants;
import org.scilab.giftlist.internal.gift.GiftStatus;
import org.scilab.giftlist.internal.misc.LinksUtils;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Gift extends BaseAggregateRoot<String> {
    @Id
    private String id;

    private String title;
    private String comment;
    private int rating;
    private String links;
    private String status;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "login")
    private AuthUser giver;
    private Date deliveryDate;
    private Date creationDate;
    private Date lastUpdateDate;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Gifts_Tags_Associations",
            joinColumns = @JoinColumn( name = "giftId" ),
            inverseJoinColumns = @JoinColumn( name = "tagName" ) )
    private Set<GiftTag> tags;

    /**Empty constructor required for hibernate*/
    public Gift(){}

    public Gift(String giftId){
        this.id=giftId;
        this.status =GiftStatus.AVAILABLE.toString();
        this.creationDate=new Date(System.currentTimeMillis());
        this.lastUpdateDate= new Date(System.currentTimeMillis());
        this.tags = new HashSet<>();
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

    /**
     * Adds a link ti the gift
     * @param link link to add
     * @throws GiftListException <ul><li>@{@link GiftListInvalidParameterException} if provided link is empty</li><li>@{@link GiftListDataAlreadyExistException} if provided link is already associated to this gift</li></ul>
     */
    public void addLink(String link) throws GiftListException{
        if(Strings.isNullOrEmpty(link)){
            throw new GiftListInvalidParameterException("Can't add an empty link");
        }
        if(Strings.isNullOrEmpty(this.links)){
            this.links=link;
            this.lastUpdateDate=new Date(System.currentTimeMillis());
            return;
        }
        List<String> actualLinks= LinksUtils.toList(this.links);
        if(actualLinks.contains(link)){
            throw new GiftListDataAlreadyExistException("This links is already associated to this gift");
        }
        actualLinks.add(link);
        this.links=LinksUtils.toLinkString(actualLinks);
        this.lastUpdateDate=new Date(System.currentTimeMillis());
    }

    /**
     * Removes a link to the current links list
     * @param link the link to remove
     */
    public void removeLink(String link){
        if(Strings.isNullOrEmpty(link) || Strings.isNullOrEmpty(this.links)){
            return;
        }
        List<String> actualLinks= LinksUtils.toList(this.links);
        actualLinks.remove(link);
        this.links = LinksUtils.toLinkString(actualLinks);
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

    /**
     * Adds a tag to a gift
     * @param aTag a gift tag
     * @throws GiftListException tag in parameter is not valid
     */
    public void addTag(GiftTag aTag) throws GiftListException{
        if(aTag==null){
            throw new GiftListInvalidParameterException("Cannot add an empty tag to a gift");
        }
        if(this.tags==null){
            this.tags= new HashSet<>();
        }
        this.tags.add(aTag);
    }

    /**
     * Removes a tag from a Gift
     * @param aTag the tag to remove
     * @throws GiftListException null tag provided
     */
    public void removeTag(GiftTag aTag) throws GiftListException{
        if(aTag==null){
            throw new GiftListInvalidParameterException("Cannot remove a non provided tag");
        }
        if(this.tags==null|| this.tags.isEmpty()){
            return;
        }
        this.tags.removeIf(someTag-> someTag.getName().equalsIgnoreCase(aTag.getName()));
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

    public String getLinks() {
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

    public void setTags(Set<GiftTag> tags) {
        this.tags = tags;
    }

    public Set<GiftTag> getTags() {
        return tags;
    }

    @Override
    public String getId() {
        return id;
    }
}
