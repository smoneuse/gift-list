package org.scilab.authProvider.internal.security.services;

import org.scilab.authProvider.internal.exceptions.AuthProviderException;
import org.scilab.authProvider.internal.security.enums.UserRole;
import org.scilab.authProvider.model.AuthUser;
import org.seedstack.business.Service;

@Service
public interface AuthUserService {
    /**
     * Registers a User to the database
     * @param login the user login
     * @param password the user password
     * @return AuthUser the registered user
     * @throws AuthProviderException issue happened during operation
     */
    AuthUser register(String login, String password, UserRole role) throws AuthProviderException;

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
     * @throws AuthProviderException @{@link org.scilab.authProvider.internal.security.exceptions.UnknownUserException} thrown if user is not registered
     */
    String getUserRole(String login) throws AuthProviderException;

    /**
     * Updates a user to a new Role
     * @param login the user login
     * @param password the current password
     * @param newRole new Role
     * @return AuthUser the updated user
     * @throws AuthProviderException :<br>
     * <ul>
     *     <li>@{@link org.scilab.authProvider.internal.security.exceptions.UnknownUserException} is the user is not found</li>
     *     <li>@{@link org.scilab.authProvider.internal.security.exceptions.InvalidCredentialsException} if password do not match</li>
     * </ul>
     */
    AuthUser updateUserRole(String login, String password, UserRole newRole) throws AuthProviderException;

    /**
     * Updates the user's password.
     * @param login User login
     * @param currentPassword current user password
     * @param newPassword new user password
     * @return AuthUser the updated user
     * @throws AuthProviderException :<br>
     * <ul>
     *     <li>@{@link org.scilab.authProvider.internal.security.exceptions.UnknownUserException} if user is not found</li>
     *     <li>@{@link org.scilab.authProvider.internal.security.exceptions.InvalidCredentialsException} if current password is not valid</li>
     * </ul>
     */
    AuthUser updatePassword(String login, String currentPassword, String newPassword) throws AuthProviderException;

    /**
     * Forces a password update (case user forgot its password)
     * @param login user login
     * @param newPassword new password to set
     * @return AuthUser the updated user
     * @throws AuthProviderException @{@link org.scilab.authProvider.internal.security.exceptions.UnknownUserException} if user could not be found
     */
    AuthUser forceUpdatePassword(String login, String newPassword) throws AuthProviderException;

    /**
     * Deletes a user
     * @param login The user login
     */
    void deleteUser(String login);
}
