package org.scilab.giftlist.domain.models.security;

import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.infra.exceptions.list.GiftListDataAlreadyExistException;
import org.scilab.giftlist.infra.exceptions.security.AuthException;
import org.scilab.giftlist.infra.exceptions.security.CredentialsNotFoundException;
import org.scilab.giftlist.infra.exceptions.security.InvalidRoleException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.*;
import java.util.*;

@Entity
public class AuthUser extends BaseAggregateRoot<String>     {
    @Id
    private String login;
    private String hashPwd;
    private String saltPwd;
    private String role;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Users_Lists_Associations",
            joinColumns = @JoinColumn( name = "login" ),
            inverseJoinColumns = @JoinColumn( name = "id" ) )
    private Set<GiftList> authorizedLists;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Owners_Lists_Associations",
            joinColumns = @JoinColumn( name = "login" ),
            inverseJoinColumns = @JoinColumn( name = "id" ) )
    private Set<GiftList> ownedLists;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "T_Users_Friends_Associations",
            joinColumns = @JoinColumn( name = "login" ),
            inverseJoinColumns = @JoinColumn( name = "friendLogin" ) )
    private Set<AuthUser> friends;

    /**Required by hibernate*/
    public AuthUser(){}

    /**
     * Constructor
     * @param login the user login
     * @throws AuthException @{@link CredentialsNotFoundException} missing login
     */
    public AuthUser(String login) throws AuthException {
        if(StringUtils.isBlank(login)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty login");
        }
        this.login =login;
        this.authorizedLists =new HashSet<>();
        this.ownedLists= new HashSet<>();
        this.friends= new HashSet<>();
    }

    /**
     * Adds a friend to the user
     * @param aFriend the friend to add
     * @throws GiftListException If the provided friend is not valid
     */
    public void addFriend(AuthUser aFriend) throws GiftListException{
        if(aFriend==null){
            throw new GiftListInvalidParameterException("Can't add a non provided authorized list.");
        }
        if(this.friends==null){
            this.friends=new HashSet<>();
        }
        this.friends.add(aFriend);
    }

    /**
     * Removes a friend to the user
     * @param aFriend a friend to remove
     */
    public void removeFriend(AuthUser aFriend){
        if(aFriend==null){
            return;
        }
        for(AuthUser someFriend : this.getFriends()){
            if(someFriend.getLogin().equals(aFriend.getLogin())){
                this.friends.remove(someFriend);
                break;
            }
        }
    }
    /**
     * Sets the user credentials on the entity
     * @param hash password hash
     * @param salt password salt
     * @throws AuthException @{@link CredentialsNotFoundException} hash or salt no provided
     */
    public void setCredentials(String hash, String salt) throws AuthException {
        if(StringUtils.isBlank(hash)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty password hash");
        }
        if(StringUtils.isBlank(salt)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty password salt");
        }
        this.hashPwd=hash;
        this.saltPwd=salt;
    }

    /**
     * Sets the role for a user
     * @param role user role to set
     * @throws AuthException @{@link InvalidRoleException} role is blank or no supported
     */
    public void setRole(String role) throws AuthException {
        if(StringUtils.isBlank(role)){
            throw new InvalidRoleException("Can't set an empty role to a user");
        }
        this.role = role;
    }

    /**
     * Adds a list to the Authorized lists
     * @param list the list to add
     * @throws GiftListException <ul><li>@{@link GiftListInvalidParameterException} When the list is not provided</li><li>@{@link GiftListDataAlreadyExistException} user is already a viewer of this list</li></ul>
     */
    public void addAuthorizedList(GiftList list) throws GiftListException {
        if(list==null){
            throw new GiftListInvalidParameterException("Can't add a non provided authorized list.");
        }
        if(authorizedLists.stream().filter(existingList-> existingList.getId().equals(list.getId())).count()!=0){
            throw new GiftListDataAlreadyExistException("list is already authorized");
        }
        this.authorizedLists.add(list);
    }

    /**
     * Removes a list from the view permissions
     * @param list the list to remove
     */
    public void removeFromAuthorizedList(GiftList list){
        if(list==null || authorizedLists==null){
            return;
        }
        for(GiftList currentAuthorized : authorizedLists){
            if(currentAuthorized.getId().equals(list.getId())){
                authorizedLists.remove(currentAuthorized);
                break;
            }
        }
    }

    @Override
    public String getId() {
        return login;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public String getSaltPwd() {
        return saltPwd;
    }

    public String getHashPwd() {
        return hashPwd;
    }

    public Set<GiftList> getAuthorizedLists() {
        if(authorizedLists==null){
            authorizedLists=Collections.emptySet();
        }
        return authorizedLists;
    }

    public Set<GiftList> getOwnedLists() {
        if(ownedLists==null){
            ownedLists=Collections.emptySet();
        }
        return ownedLists;
    }

    public void setFriends(Set<AuthUser> friends) {
        this.friends = friends;
    }

    public Set<AuthUser> getFriends() {
        if(this.friends==null){
            return Collections.emptySet();
        }
        return friends;
    }
}
