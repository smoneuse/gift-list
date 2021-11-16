package org.scilab.giftlist.domain.models.list;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.infra.exceptions.list.GiftListDataAlreadyExistException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gift List model
 */
@Entity
public class GiftList extends BaseAggregateRoot<String> {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Owners_Lists_Associations",
            joinColumns = @JoinColumn( name = "id" ),
            inverseJoinColumns = @JoinColumn( name = "login" ) )
    private AuthUser owner;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Users_Lists_Associations",
            joinColumns = @JoinColumn( name = "id" ),
            inverseJoinColumns = @JoinColumn( name = "login" ) )
    private Set<AuthUser> viewers;
    private String title;
    private String description;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Gifts_Lists_Associations",
            joinColumns = @JoinColumn( name = "listId" ),
            inverseJoinColumns = @JoinColumn( name = "giftId"))
    private Set<Gift> gifts;

    /**Hibernate required*/
    public GiftList(){}

    /**
     * Builds a new list with a given identifier
     * @param id list identifier
     */
    public GiftList(String id){
        this.id=id;
        this.gifts= new HashSet<>();
        this.viewers = new HashSet<>();
    }

    /**
     * Sets the title and description properties of this list
     * @param title list title
     * @param description list Description
     * @throws GiftListInvalidParameterException Thrown if title is blank or null
     */
    public void setTitleAndDescription(String title, String description) throws GiftListException {
        if(StringUtils.isBlank(title)){
            throw new GiftListInvalidParameterException("Can't create a list without a title");
        }
        this.title = title;
        this.description= Strings.nullToEmpty(description);
    }

    /**
     * Adds a viewer to the current list
     * @param user the user to add
     * @throws GiftListException <ul><li>@{@link GiftListInvalidParameterException} Viewer non provided</li><li>@{@link GiftListDataAlreadyExistException} Viewer already added to this list</li></ul>
     *
     */
    public void addViewer(AuthUser user) throws GiftListException {
        if(user==null){
            throw new GiftListInvalidParameterException("Can't add a non provided viewer");
        }
        if(viewers.stream().filter(viewer-> viewer.getLogin().equals(user.getLogin())).count()!=0){
            throw new GiftListDataAlreadyExistException("Viewer already added to the list");
        }
        this.viewers.add(user);
    }

    /**
     * Adds a gift to a list
     * @param addedGift the git to add
     * @throws GiftListException <ul><li>@{@link GiftListInvalidParameterException} Gift non provided</li><li>@{@link GiftListDataAlreadyExistException} Gift already added to this list</li></ul>
     */
    public void addGift(Gift addedGift) throws GiftListException{
        if(addedGift==null){
            throw new GiftListInvalidParameterException("Can't add a non provided gift");
        }
        if(gifts.stream().filter(aGift-> aGift.getId().equals(addedGift.getId())).count()!=0){
            throw new GiftListDataAlreadyExistException("Gift already added to the list");
        }
        this.gifts.add(addedGift);
    }

    public void removeGift(String aGiftId) throws GiftListInvalidParameterException {
        if(aGiftId==null){
            throw new GiftListInvalidParameterException("Can't remove a non provided gift");
        }
        this.gifts.removeIf( someGift-> someGift.getId().equals(aGiftId));
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setOwner(AuthUser owner) {
        this.owner = owner;
    }

    public void setViewers(Set<AuthUser> viewers) {
        this.viewers = viewers;
    }

    public void setGifts(Set<Gift> gifts) {
        this.gifts = gifts;
    }

    public AuthUser getOwner() {
        return owner;
    }

    public Set<AuthUser> getViewers() {
        return viewers;
    }

    public Set<Gift> getGifts() {
        return gifts;
    }
}
