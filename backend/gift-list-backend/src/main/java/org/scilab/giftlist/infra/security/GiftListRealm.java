package org.scilab.giftlist.infra.security;

import org.scilab.giftlist.infra.exceptions.security.AuthException;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.seed.security.*;
import org.seedstack.seed.security.principals.PrincipalProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GiftListRealm implements Realm {

    private RoleMapping roleMapping;
    private RolePermissionResolver rolePermissionResolver;


    @Inject
    private AuthUserService authUserService;

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        if (authenticationToken instanceof UsernamePasswordToken) {
            UsernamePasswordToken token=(UsernamePasswordToken)authenticationToken;
            if(!authUserService.validateUserPassword(token.getUsername(),new String(token.getPassword()))){
                //First case : user is not known
                if(!authUserService.checkUserKnown(token.getUsername())){
                    throw new UnknownAccountException("Unknown user :"+token.getUsername());
                }
                //Second case : password provided is incorrect
                throw new IncorrectCredentialsException();
            }
            return new AuthenticationInfo(token.getUsername(), token.getPassword());
        }
        else {
            throw new UnsupportedTokenException("GiftListRealm only supports UsernamePasswordToken");
        }
    }

    /**
     * Setter roleMapping
     *
     * @param roleMapping the role mapping
     */
    @Inject
    public void setRoleMapping(@Named("GiftListRealm-role-mapping") RoleMapping roleMapping) {
        this.roleMapping = roleMapping;
    }

    /**
     * Setter rolePermissionResolver
     *
     * @param rolePermissionResolver the rolePermissionResolver
     */
    @Inject
    public void setRolePermissionResolver(@Named("GiftListRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
        this.rolePermissionResolver = rolePermissionResolver;
    }

    @Override
    public RoleMapping getRoleMapping() {
        return roleMapping;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return rolePermissionResolver;
    }

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
        try {
            Set<String> userRoles = new HashSet<>();
            userRoles.add(authUserService.getUserRole(identityPrincipal.get().toString()));
            return userRoles;
        }catch (AuthException ae){
            return Collections.emptySet();
        }
    }



    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return UsernamePasswordToken.class;
    }
}
