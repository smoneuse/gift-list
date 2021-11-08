package org.scilab.authProvider.model;

import org.apache.commons.lang3.StringUtils;
import org.scilab.authProvider.internal.exceptions.AuthProviderException;
import org.scilab.authProvider.internal.security.exceptions.CredentialsNotFoundException;
import org.scilab.authProvider.internal.security.exceptions.InvalidRoleException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AuthUser extends BaseAggregateRoot<String> {
    @Id
    private String login;
    private String hashPwd;
    private String saltPwd;
    private String role;

    /**Required by hibernate*/
    public AuthUser(){}

    /**
     * Constructor
     * @param login the user login
     * @throws AuthProviderException @{@link CredentialsNotFoundException} missing login
     */
    public AuthUser(String login) throws AuthProviderException{
        if(StringUtils.isBlank(login)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty login");
        }
        this.login =login;
    }

    public void setCredentials(String hash, String salt) throws AuthProviderException {
        if(StringUtils.isBlank(hash)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty password hash");
        }
        if(StringUtils.isBlank(salt)){
            throw new CredentialsNotFoundException("Can't set credentials with an empty password salt");
        }
        this.login=login;
        this.hashPwd=hash;
        this.saltPwd=salt;
    }

    public void setRole(String role) throws AuthProviderException {
        if(StringUtils.isBlank(role)){
            throw new InvalidRoleException("Can't set an empty role to a user");
        }
        this.role = role;
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
}
