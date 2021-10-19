package org.scilab.giftlist.domain.models;

import com.google.common.base.Strings;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Model for a User
 */
@Entity
public class User extends BaseAggregateRoot<String> {
    @Id
    private String id;
    private String name;
    private String eMail;

    @OneToMany(cascade = CascadeType.DETACH)
    private Set<GiftList> userGiftLists;

    @OneToMany(cascade = CascadeType.DETACH)
    private Set<User> foreignListAccess;

    /**
     * Hibernate requirement
     */
    private User(){
    }

    /**
     * Model object constructor
     * @param id identifier
     */
    public User(String id){
        this.id=id;
    }
    /**
     * Sets the name of the user
     * @param name <strong>user name</strong>
     * @throws GiftListInvalidParameterException Thrown if blank or null name
     */
    public void setName(String name) throws GiftListInvalidParameterException {
        if(Strings.isNullOrEmpty(name)){
            throw new GiftListInvalidParameterException("The parameter 'name' can not be null or empty");
        }
        this.name = name;
    }

    /**
     * Sets the user email
     * @param email user e-mail
     */
    public void seteMail(String email){
        this.eMail=email;
    }

    public String getName() {
        return name;
    }

    public String geteMail() {
        return eMail;
    }

    @Override
    public String getId() {
        return id;
    }

    public Set<GiftList> getUserGiftLists() {
        return userGiftLists;
    }

    public Set<User> getForeignListAccess() {
        return foreignListAccess;
    }
}
