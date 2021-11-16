package org.scilab.giftlist.resources.security;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.security.*;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.requests.ResponseStatus;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.scilab.giftlist.resources.security.models.request.CreateAccountModel;
import org.scilab.giftlist.resources.security.models.request.UpdateAccountModel;
import org.scilab.giftlist.resources.security.models.response.AccountEchoResponse;
import org.scilab.giftlist.resources.security.models.response.CreateOrUpdateAccountResponse;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.crypto.HashingService;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

/**
 * Resource class for Accounts management
 */
@Path("account")
@Transactional
@JpaUnit("appUnit")
public class AccountsResource {

    @Logging
    private Logger logger;

    @Inject
    private AuthUserService authUserService;

    @Inject
    private AccountService accountService;

    @Inject
    private HashingService hashingService;

    @Inject
    private SecuritySupport securitySupport;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public CreateOrUpdateAccountResponse createAccount(CreateAccountModel user){
        CreateOrUpdateAccountResponse response=new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.SUCCESS,null);
        try {
            authUserService.register(user.getLogin(), user.getPassword(), GiftListRoles.valueOf(user.getRole()));
        }
        catch (CredentialsNotFoundException cnfe){
            response =new CreateOrUpdateAccountResponse(user.login, ResponseStatus.INVALID_DATA, cnfe.getMessage());
        }
        catch (UserAlreadyExistException uae){
            response =new CreateOrUpdateAccountResponse(user.login, ResponseStatus.ALREADY_PRESENT, uae.getMessage());
        }
        catch (AuthException ae){
            response =new CreateOrUpdateAccountResponse(user.login, ResponseStatus.FAILURE, ae.getMessage());
        }
        catch (IllegalArgumentException iae){
            response=new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.INVALID_DATA,"The provided role is not supported :"+user.getRole());
        }
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public CreateOrUpdateAccountResponse register(CreateAccountModel user){
        try {
            authUserService.register(user.getLogin(), user.getPassword(), GiftListRoles.USER);
        }
        catch (CredentialsNotFoundException cnfe){
            return new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.INVALID_DATA, cnfe.getMessage());
        }
        catch (UserAlreadyExistException uae){
            return new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.ALREADY_PRESENT, uae.getMessage());
        }
        catch (AuthException ae){
            return new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.FAILURE, ae.getMessage());
        }
        //Logs the user after successful registration
        return login(user);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/friends/{friend}")
    @RequiresRoles("userRole")
    public Set<String> addFriend(@PathParam("friend") String friendLogin){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        AuthUser currentUSer =this.authUserService.findAccount(currentUserLogin).orElseThrow(()->new NotFoundException("Can't find actual user "+currentUserLogin));
        try {
            AuthUser updated = this.accountService.addFriend(currentUSer, friendLogin);
            Set<String> response =new HashSet<>();
            for(AuthUser aFriend : updated.getFriends()){
                response.add(aFriend.getLogin());
            }
            return response;
        }catch (GiftListException gle){
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/friends")
    @RequiresRoles("userRole")
    public Set<String> getFriends(){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        AuthUser currentUSer =this.authUserService.findAccount(currentUserLogin).orElseThrow(()->new NotFoundException("Can't find actual user "+currentUserLogin));
        Set<String> response =new HashSet<>();
        for(AuthUser aFriend : currentUSer.getFriends()){
            response.add(aFriend.getLogin());
        }
        return response;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/friends/{friend}")
    @RequiresRoles("userRole")
    public void removeFriend(@PathParam("friend") String friendLogin){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        AuthUser currentUSer =this.authUserService.findAccount(currentUserLogin).orElseThrow(()->new NotFoundException("Can't find actual user "+currentUserLogin));
        this.accountService.removeFriend(currentUSer, friendLogin);
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public CreateOrUpdateAccountResponse login(CreateAccountModel user){
        if(authUserService.checkUserKnown(user.getLogin() )&& authUserService.validateUserPassword(user.getLogin(), user.getPassword()))
        {
            AuthenticationToken token = new UsernamePasswordToken(user.getLogin(), user.getPassword());
            securitySupport.login(token);
            return new CreateOrUpdateAccountResponse(user.getLogin(), ResponseStatus.SUCCESS,null);
        }
        else{
            throw new javax.ws.rs.NotAuthorizedException("Incorrect credentials");
        }
    }



    @POST
    @Path("/logout")
    public void logout(){
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/echo/{phrase}")
    @RequiresRoles("userRole")
    public AccountEchoResponse echo(@PathParam("phrase") String phrase){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        return new AccountEchoResponse(currentUserLogin, phrase, ResponseStatus.SUCCESS);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/echoAdmin/{phrase}")
    @RequiresRoles("adminRole")
    public AccountEchoResponse echoAdmin(@PathParam("phrase") String phrase){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        return new AccountEchoResponse(currentUserLogin, phrase, ResponseStatus.SUCCESS);
    }

    @DELETE
    @Path("/{accountLogin}")
    @RequiresRoles("adminRole")
    public void deleteAccount(@PathParam("accountLogin") String login){
        if(authUserService.checkUserKnown(login)){
            authUserService.deleteUser(login);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/force-update")
    @RequiresRoles("adminRole")
    public CreateOrUpdateAccountResponse forceUpdate(UpdateAccountModel updatedUser){
        CreateOrUpdateAccountResponse response = new CreateOrUpdateAccountResponse(updatedUser.getLogin(),ResponseStatus.SUCCESS,null);
        try {
            if (!Strings.isNullOrEmpty(updatedUser.getNewPassword())) {
                authUserService.forceUpdatePassword(updatedUser.getLogin(), updatedUser.getNewPassword());
            }
            if(!Strings.isNullOrEmpty(updatedUser.getNewRole())){
                authUserService.forceUpdateRole(updatedUser.getLogin(),GiftListRoles.valueOf(updatedUser.getNewRole()));
            }
        }
        catch (UnknownUserException uue){
            response= new CreateOrUpdateAccountResponse(updatedUser.getLogin(), ResponseStatus.UNKNOWN, uue.getMessage());
        }
        catch (CredentialsNotFoundException cnfe){
            response= new CreateOrUpdateAccountResponse(updatedUser.getLogin(), ResponseStatus.INVALID_DATA, cnfe.getMessage());
        }
        catch(IllegalArgumentException iae){
            response = new CreateOrUpdateAccountResponse(updatedUser.getLogin(), ResponseStatus.INVALID_DATA,"The role is not supported :"+updatedUser.getNewRole());
        }
        catch (AuthException ae){
            response =new CreateOrUpdateAccountResponse(updatedUser.getLogin(), ResponseStatus.FAILURE, ae.getMessage());
        }
        return response;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public CreateOrUpdateAccountResponse userUpdate(UpdateAccountModel updatedUser){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        if(!updatedUser.getLogin().equals(currentUserLogin)){
            return new CreateOrUpdateAccountResponse(currentUserLogin, ResponseStatus.FAILURE,"Provided login do not match with you current session login");
        }
        CreateOrUpdateAccountResponse response = new CreateOrUpdateAccountResponse(currentUserLogin,ResponseStatus.SUCCESS,null);
        try {
            if (!Strings.isNullOrEmpty(updatedUser.getNewPassword())) {
                authUserService.updatePassword(currentUserLogin, updatedUser.getCurrentPassword(), updatedUser.getNewPassword());
            }
        }
        catch (UnknownUserException uue){
            response= new CreateOrUpdateAccountResponse(currentUserLogin, ResponseStatus.UNKNOWN, uue.getMessage());
        }
        catch (InvalidCredentialsException ice){
            response= new CreateOrUpdateAccountResponse(currentUserLogin, ResponseStatus.INVALID_CREDENTIALS, ice.getMessage());
        }
        catch (AuthException ae){
            response= new CreateOrUpdateAccountResponse(currentUserLogin, ResponseStatus.FAILURE, ae.getMessage());
        }
        return response;
    }

}
