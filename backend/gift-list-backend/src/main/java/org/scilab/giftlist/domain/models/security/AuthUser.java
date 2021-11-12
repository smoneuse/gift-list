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
import java.util.ArrayList;
import java.util.List;

@Entity
public class AuthUser extends BaseAggregateRoot<String>     {
    @Id
    private String login;
    private String hashPwd;
    private String saltPwd;
    private String role;
    @ManyToMany
    @JoinTable( name = "T_Users_Lists_Associations",
            joinColumns = @JoinColumn( name = "login" ),
            inverseJoinColumns = @JoinColumn( name = "id" ) )
    private List<GiftList> authorizedLists;
    @OneToMany
    @JoinTable( name = "T_Owners_Lists_Associations",
            joinColumns = @JoinColumn( name = "login" ),
            inverseJoinColumns = @JoinColumn( name = "id" ) )
    private List<GiftList> ownedLists;

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
        this.authorizedLists =new ArrayList<>();
        this.ownedLists= new ArrayList<>();
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

    public List<GiftList> getAuthorizedLists() {
        return authorizedLists;
    }
}
