package org.scilab.authProvider.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scilab.authProvider.internal.exceptions.AuthProviderException;
import org.scilab.authProvider.internal.security.enums.UserRole;
import org.scilab.authProvider.internal.security.exceptions.InvalidCredentialsException;
import org.scilab.authProvider.internal.security.exceptions.UnknownUserException;
import org.scilab.authProvider.internal.security.services.AuthUserService;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import javax.inject.Inject;

@RunWith(SeedITRunner.class)
public class AuthUserServiceTest {

    @Inject
    private AuthUserService authUserService;

    @Test
    public void commonTests() {
        String loginTest="loginTest";
        String unknownLogin="ThisLoginDoesNotExist";
        String passwdTest="passwordTest";
        String badPassword="ThisIsNotTheCorrectPassword";
        UserRole roleTest= UserRole.ADMIN;
        try{
            authUserService.register(loginTest, passwdTest, roleTest);
            Assert.assertFalse("Found a user that should not be present",authUserService.checkUserKnown(unknownLogin));
            Assert.assertTrue("Legit user not found",authUserService.checkUserKnown(loginTest));
            Assert.assertTrue("Legit password detected as bad", authUserService.validateUserPassword(loginTest,passwdTest));
            Assert.assertFalse("Invalid password detected as ok", authUserService.validateUserPassword(loginTest,badPassword));
        }
        catch (AuthProviderException ape){
            Assert.fail("AuthProviderException while testing testUnknownUser : "+ape.getMessage());
        }
        finally {
            authUserService.deleteUser(loginTest);
        }
    }

    @Test
    public void updateTests(){
        String loginTest="loginTest";
        String passwdTest="passwordTest";
        UserRole roleTest= UserRole.ADMIN;
        String updatedPassword="updatedPasswordTest";
        UserRole updateRole= UserRole.USER;

        try{
            authUserService.register(loginTest, passwdTest, roleTest);
            Assert.assertTrue("Legit password detected as bad", authUserService.validateUserPassword(loginTest,passwdTest));
            Assert.assertEquals("Admin Role is not the one retrieved",roleTest.toString(), authUserService.getUserRole(loginTest));
            authUserService.updateUserRole(loginTest, passwdTest, updateRole);
            Assert.assertEquals("updated user Role is not the one retrieved",updateRole.toString(), authUserService.getUserRole(loginTest));
            authUserService.updatePassword(loginTest, passwdTest, updatedPassword);
            Assert.assertTrue("Legit updated password detected as bad", authUserService.validateUserPassword(loginTest,updatedPassword));
        }
        catch (AuthProviderException ape){
            Assert.fail("AuthProviderException while testing testUnknownUser : "+ape.getMessage());
        }
        finally {
            authUserService.deleteUser(loginTest);
        }
    }

    @Test
    public void testValidateUnknownUser(){
        Assert.assertFalse(authUserService.validateUserPassword("DoNoExist","FakePassword"));
    }

    @Test(expected = UnknownUserException.class)
    public void testUpdateRoleUnknownUser() throws AuthProviderException{
        authUserService.updateUserRole("UnknownUser","fakePassword", UserRole.USER);
    }

    @Test(expected = UnknownUserException.class)
    public void testUpdatePasswordUnknownUser() throws AuthProviderException{
        authUserService.updatePassword("UnknownUser","fakePassword","fakePassword2");
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testUpdateRoleIncorrectPassword() throws AuthProviderException{
        String loginTest="loginTest";
        String passwdTest="passTest";
        UserRole roleTest=UserRole.USER;
        try{
            authUserService.register(loginTest, passwdTest, roleTest);
            authUserService.updateUserRole(loginTest,"badPassword",UserRole.ADMIN);
        }
        finally {
            authUserService.deleteUser(loginTest);
        }
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testUpdatePasswordIncorrectPassword() throws AuthProviderException{
        String loginTest="loginTest";
        String passwdTest="passTest";
        UserRole roleTest=UserRole.ADMIN;
        try{
            authUserService.register(loginTest, passwdTest, roleTest);
            authUserService.updatePassword(loginTest,"badPassword","alsoBadPassword");
        }
        finally {
            authUserService.deleteUser(loginTest);
        }
    }

    @Test
    public void testForceUpdatePassword(){
        String loginTest="loginTest";
        String passwdTest="passTest";
        UserRole roleTest=UserRole.ADMIN;
        String updatedPassword="updatedPass";
        try {
            authUserService.register(loginTest, passwdTest, roleTest);
            authUserService.forceUpdatePassword(loginTest, updatedPassword);
            Assert.assertTrue("Legit updated password detected as bad", authUserService.validateUserPassword(loginTest, updatedPassword));
        }
        catch (AuthProviderException ape){
            Assert.fail("Failed to force update password :"+ape.getMessage());
        }
        finally {
            authUserService.deleteUser(loginTest);
        }
    }
}
