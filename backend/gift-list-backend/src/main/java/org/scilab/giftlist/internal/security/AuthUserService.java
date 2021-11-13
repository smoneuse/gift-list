package org.scilab.giftlist.internal.security;

import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.security.AuthException;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.seedstack.business.Service;

import java.util.Optional;

/**
 * Business service for user authentication
 */
@Service
public interface AuthUserService {

    /**
     * Registers a User to the database
     * @param login the user login
     * @param password the user password
     * @return AuthUser the registered user
     * @throws AuthException
     * <ul>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.CredentialsNotFoundException} Credentials not provided</li>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.UserAlreadyExistException} Registering a user already registered</li>
     * </ul>
     */
    AuthUser register(String login, String password, GiftListRoles role) throws AuthException;

    /**
     * Finds an account
     * @param login the account login
     * @return @{@link Optional}&lt;@{@link AuthUser};gt; user found
     */
    Optional<AuthUser> findAccount(String login);

    /**
     * Checks that a user is registered in the database
     * @param login user login
     * @return boolean true is known, false otherwise
     */
    boolean checkUserKnown(String login);

    /**
     * Validates a user's password
     * @param login the user login
     * @param password the user password to test
     * @return boolean true if password matches, false is non match or user is not registered
     */
    boolean validateUserPassword(String login, String password);

    /**
     * Provides the role of a registered password
     * @param login the user to seek the role from
     * @return @{@link String} user's role
     * @throws AuthException @{@link org.scilab.giftlist.infra.exceptions.security.UnknownUserException} thrown if user is not registered
     */
    String getUserRole(String login) throws AuthException;

    /**
     * Updates a user to a new Role
     * @param login the user login
     * @param password the current password
     * @param newRole new Role
     * @return AuthUser the updated user
     * @throws AuthException :<br>
     * <ul>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.UnknownUserException} is the user is not found</li>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.InvalidCredentialsException} if password do not match</li>
     * </ul>
     */
    AuthUser updateUserRole(String login, String password, GiftListRoles newRole) throws AuthException;

    /**
     * Updates the user's password.
     * @param login User login
     * @param currentPassword current user password
     * @param newPassword new user password
     * @return AuthUser the updated user
     * @throws AuthException :<br>
     * <ul>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.UnknownUserException} if user is not found</li>
     *     <li>@{@link org.scilab.giftlist.infra.exceptions.security.InvalidCredentialsException} if current password is not valid</li>
     * </ul>
     */
    AuthUser updatePassword(String login, String currentPassword, String newPassword) throws AuthException;

    /**
     * Forces a password update (case user forgot its password)
     * @param login user login
     * @param newPassword new password to set
     * @return AuthUser the updated user
     * @throws AuthException @{@link org.scilab.giftlist.infra.exceptions.security.UnknownUserException} if user could not be found
     */
    AuthUser forceUpdatePassword(String login, String newPassword) throws AuthException;

    /**
     * Forces the user role to be updated
     * @param login user login
     * @param newRole new role to set
     * @return @{@link AuthUser} updated user
     * @throws AuthException
     */
    AuthUser forceUpdateRole(String login, GiftListRoles newRole) throws AuthException;
    /**
     * Deletes a user
     * @param login The user login
     */
    void deleteUser(String login);
}
