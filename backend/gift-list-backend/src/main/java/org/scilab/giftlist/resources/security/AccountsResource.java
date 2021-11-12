package org.scilab.giftlist.resources.security;

import com.google.common.base.Strings;
import org.scilab.giftlist.infra.exceptions.security.*;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.internal.requests.ResponseStatus;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.scilab.giftlist.resources.security.models.request.CreateAccountModel;
import org.scilab.giftlist.resources.security.models.request.UpdateAccountModel;
import org.scilab.giftlist.resources.security.models.response.AccountEchoResponse;
import org.scilab.giftlist.resources.security.models.response.CreateOrUpdateAccountResponse;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.principals.Principals;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Resource class for Accounts management
 */
@Path("account")
public class AccountsResource {

    @Inject
    private AuthUserService authUserService;

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
