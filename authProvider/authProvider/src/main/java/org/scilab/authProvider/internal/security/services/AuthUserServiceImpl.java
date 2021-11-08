package org.scilab.authProvider.internal.security.services;


import com.google.common.base.Strings;
import org.scilab.authProvider.internal.exceptions.AuthProviderException;
import org.scilab.authProvider.internal.security.enums.UserRole;
import org.scilab.authProvider.internal.security.exceptions.CredentialsNotFoundException;
import org.scilab.authProvider.internal.security.exceptions.InvalidCredentialsException;
import org.scilab.authProvider.internal.security.exceptions.UnknownUserException;
import org.scilab.authProvider.internal.security.exceptions.UserAlreadyExistException;
import org.scilab.authProvider.model.AuthUser;
import org.seedstack.business.domain.Repository;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.crypto.Hash;
import org.seedstack.seed.crypto.HashingService;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * AuthUserService implementation
 */
public class AuthUserServiceImpl implements AuthUserService{

    @Logging
    private Logger logger;

    @Inject
    @Jpa
    Repository<AuthUser, String> authUserRepository;

    @Inject
    private HashingService hashingService;

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public AuthUser register(String login, String password, UserRole role) throws AuthProviderException {
        logger.info("Request for adding user {}", login);
        AuthUser newUser= new AuthUser(login);
        if(Strings.isNullOrEmpty(password)){
            throw new CredentialsNotFoundException("Can't register user without a password");
        }
        //Verifying the user does no already exist
        if(authUserRepository.get(login).isPresent()){
            throw new UserAlreadyExistException("User "+login+" already exists");
        }
        logger.info("User {} is not already known - hashing password.", login);
        Hash hash = hashingService.createHash(password.toCharArray());
        newUser.setCredentials(hash.getHashAsString(), hash.getSaltAsString());
        newUser.setRole(role.toString());
        logger.info("Adding user {} to the data base with role {}", login, role.toString());
        return authUserRepository.addOrUpdate(newUser);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public boolean checkUserKnown(String login) {
        return authUserRepository.get(login).isPresent();
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public boolean validateUserPassword(String login, String password) {
        if(Strings.isNullOrEmpty(login) || Strings.isNullOrEmpty(password)){
            logger.info("Can't validate password without data");
            return false;
        }
        try {
            AuthUser currentUser = authUserRepository.get(login).orElseThrow(()-> new UnknownUserException("User could not be found"));
            Hash currentUserHash = new Hash(currentUser.getHashPwd(), currentUser.getSaltPwd());
            return hashingService.validatePassword(password, currentUserHash);
        }
        catch(UnknownUserException ex){
            logger.info("Can't validate password of non registered user");
            return false;
        }
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public String getUserRole(String login) throws AuthProviderException {
        if(Strings.isNullOrEmpty(login)){
            logger.info("Can't retrieve role without data");
            throw new UnknownUserException("Could not retrieve user with empty login");
        }
        AuthUser currentUser = authUserRepository.get(login).orElseThrow(()-> new UnknownUserException("User could not be found"));
        return currentUser.getRole();
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public AuthUser updateUserRole(String login, String password, UserRole newRole) throws AuthProviderException {
        AuthUser currentUser = authUserRepository.get(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
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
    @Transactional
    @JpaUnit("appUnit")
    public AuthUser updatePassword(String login, String currentPassword, String newPassword) throws AuthProviderException {
        AuthUser currentUser = authUserRepository.get(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        if(!validateUserPassword(login, currentPassword)){
            throw new InvalidCredentialsException("Password provided is incorrect. Can't update user "+login);
        }
        Hash newPasswordHash =hashingService.createHash(newPassword);
        currentUser.setCredentials(newPasswordHash.getHashAsString(), newPasswordHash.getSaltAsString());
        return authUserRepository.update(currentUser);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public AuthUser forceUpdatePassword(String login, String newPassword) throws AuthProviderException {
        AuthUser currentUser = authUserRepository.get(login).orElseThrow(()-> new UnknownUserException("User not found : "+login));
        Hash newPasswordHash =hashingService.createHash(newPassword);
        currentUser.setCredentials(newPasswordHash.getHashAsString(), newPasswordHash.getSaltAsString());
        return authUserRepository.update(currentUser);
    }

    @Override
    @Transactional
    @JpaUnit("appUnit")
    public void deleteUser(String login) {
        if(!Strings.isNullOrEmpty(login) && authUserRepository.contains(login)) {
            authUserRepository.remove(login);
        }
    }
}
