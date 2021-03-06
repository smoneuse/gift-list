package org.scilab.giftlist.internal.security;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.security.*;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.crypto.Hash;
import org.seedstack.seed.crypto.HashingService;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Optional;
@Transactional(propagation = Propagation.REQUIRED)
@JpaUnit("appUnit")
public class AuthUserServiceImpl implements AuthUserService{

    @Logging
    private Logger logger;

    @Inject
    @Jpa
    Repository<AuthUser, String> authUserRepository;

    @Inject
    private HashingService hashingService;

    @Override
    public AuthUser register(String login, String password, GiftListRoles role) throws AuthException {
        logger.info("Request for adding user {}", login);
        if(Strings.isNullOrEmpty(password)){
            throw new CredentialsNotFoundException("Can't register user without a password");
        }
        //Verifying the user does no already exist
        if(findAccount(login).isPresent()){
            throw new UserAlreadyExistException("User "+login+" already exists");
        }
        AuthUser newUser= new AuthUser(login);
        logger.info("User {} is not already known - hashing password.", login);
        Hash hash = hashingService.createHash(password.toCharArray());
        newUser.setCredentials(hash.getHashAsString(), hash.getSaltAsString());
        newUser.setRole(role.toString());
        logger.info("Adding user {} to the data base with role {}", login, role.toString());
        return authUserRepository.addOrUpdate(newUser);
    }

    @Override
    public boolean checkUserKnown(String login) {
        return findAccount(login).isPresent();
    }

    @Override
    public boolean validateUserPassword(String login, String password) {
        if(Strings.isNullOrEmpty(login) || Strings.isNullOrEmpty(password)){
            logger.info("Can't validate password without data");
            return false;
        }
        try {
            AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User could not be found"));
            Hash currentUserHash = new Hash(currentUser.getHashPwd(), currentUser.getSaltPwd());
            return hashingService.validatePassword(password, currentUserHash);
        }
        catch(UnknownUserException ex){
            logger.info("Can't validate password of non registered user");
            return false;
        }
    }

    @Override
    public String getUserRole(String login) throws AuthException {
        if(Strings.isNullOrEmpty(login)){
            logger.info("Can't retrieve role without data");
            throw new UnknownUserException("Could not retrieve user with empty login");
        }
        AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User could not be found"));
        return currentUser.getRole();
    }

    @Override
    public AuthUser updateUserRole(String login, String password, GiftListRoles newRole) throws AuthException {
        AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        if(!validateUserPassword(login, password)){
            throw new InvalidCredentialsException("Password provided is incorrect. Can't update user "+login);
        }
        if(!newRole.toString().equals(currentUser.getRole())){
            currentUser.setRole(newRole.toString());
            authUserRepository.update(currentUser);
        }
        //No need to update, the role is the same
        return currentUser;
    }

    @Override
    public AuthUser updatePassword(String login, String currentPassword, String newPassword) throws AuthException {
        AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        if(!validateUserPassword(login, currentPassword)){
            throw new InvalidCredentialsException("Password provided is incorrect. Can't update user "+login);
        }
        Hash newPasswordHash =hashingService.createHash(newPassword);
        currentUser.setCredentials(newPasswordHash.getHashAsString(), newPasswordHash.getSaltAsString());
        return authUserRepository.update(currentUser);
    }

    @Override
    public AuthUser forceUpdatePassword(String login, String newPassword) throws AuthException {
        AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        Hash newPasswordHash =hashingService.createHash(newPassword);
        currentUser.setCredentials(newPasswordHash.getHashAsString(), newPasswordHash.getSaltAsString());
        return authUserRepository.update(currentUser);
    }

    @Override
    public AuthUser forceUpdateRole(String login, GiftListRoles newRole) throws AuthException {
        AuthUser currentUser = findAccount(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        currentUser.setRole(newRole.toString());
        return authUserRepository.update(currentUser);
    }

    @Override
    public void deleteUser(String login) {
        Optional<AuthUser> userToDelete=findAccount(login);
        userToDelete.ifPresent(authUser -> authUserRepository.remove(authUser));
    }

    @Override
    public Optional<AuthUser> findAccount(String login) {
        if(Strings.isNullOrEmpty(login)){
            return Optional.empty();
        }
        Specification<AuthUser> fetchUserSpec=authUserRepository.getSpecificationBuilder().of((AuthUser.class)).property("login").equalTo(login).ignoringCase().build();
        return authUserRepository.get(fetchUserSpec).findFirst();
    }
}
