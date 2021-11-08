package org.scilab.authProvider.resources;

import org.scilab.authProvider.internal.enums.RequestStatus;
import org.scilab.authProvider.internal.exceptions.AuthProviderException;
import org.scilab.authProvider.internal.security.enums.UserRole;
import org.scilab.authProvider.internal.security.exceptions.CredentialsNotFoundException;
import org.scilab.authProvider.internal.security.exceptions.InvalidCredentialsException;
import org.scilab.authProvider.internal.security.exceptions.UnknownUserException;
import org.scilab.authProvider.internal.security.exceptions.UserAlreadyExistException;
import org.scilab.authProvider.internal.security.services.AuthUserService;
import org.scilab.authProvider.resources.requests.UserRequest;
import org.scilab.authProvider.resources.requests.UserUpdateRequest;
import org.scilab.authProvider.resources.responses.AuthResponse;
import org.scilab.authProvider.resources.responses.StatusResponse;
import org.seedstack.seed.Logging;
import org.seedstack.seed.security.RequiresRoles;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("auth")
public class AuthResource {
    @Logging
    Logger logger;

    @Inject
    AuthUserService authUserService;

    @GET
    @Path("/authenticate")
    @RequiresRoles("adminRole")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticate(@QueryParam("login") String login, @QueryParam("password") String password){
        logger.info("Request for authentication of user {}", login);
        if(!authUserService.checkUserKnown(login)){
            logger.info("Request for authentication of user {} finished with UNKNOWN", login);
            return new AuthResponse(login, null, RequestStatus.UNKNOWN);
        }
        if(!authUserService.validateUserPassword(login, password)){
            logger.info("Request for authentication of user {} finished with INVALID", login);
            return new AuthResponse(login, null, RequestStatus.INVALID);
        }
       try{
           logger.info("Request for authentication of user {} finished with AUTHENTICATED", login);
            return new AuthResponse(login,authUserService.getUserRole(login), RequestStatus.AUTHENTICATED);
        }
       catch (AuthProviderException ape){
           logger.info("Request for authentication of user {} finished with FAILURE : {}", login, ape.getMessage());
           return new AuthResponse(login, null, RequestStatus.FAILURE);
       }
    }

    @GET
    @Path("/{login}/role")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public AuthResponse role(@PathParam("login") String login){
        logger.info("Request for role of user {}", login);
        try{
            logger.info("Request for role of user {} finished in SUCCESS", login);
            return new AuthResponse(login,authUserService.getUserRole(login), RequestStatus.SUCCESS);
        }
        catch (UnknownUserException uue){
            logger.info("Request for role of user {} finished in UNKNOWN", login);
            return new AuthResponse(login, null, RequestStatus.UNKNOWN);
        }
        catch (AuthProviderException ape){
            logger.info("Request for role of user {} finished in FAILURE : {}", login, ape.getMessage());
            return new AuthResponse(login, null, RequestStatus.FAILURE);
        }
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public StatusResponse register(UserRequest userReq){
        logger.info("Request for registering user {}", userReq.getUser());
        try {
            authUserService.register(userReq.getUser(), userReq.getPassword(), UserRole.valueOf(userReq.getRole()));
            logger.info("Request for registering user {} finished with SUCCESS", userReq.getUser());
            return new StatusResponse(RequestStatus.SUCCESS);
        }
        catch (CredentialsNotFoundException cnfe){
            logger.info("Request for registering user {} finished with INVALID_CREDENTIALS", userReq.getUser());
            return new StatusResponse(RequestStatus.INVALID_CREDENTIALS, cnfe.getMessage());
        }
        catch (UserAlreadyExistException uaee){
            logger.info("Request for registering user {} finished with ALREADY_KNOWN", userReq.getUser());
            return new StatusResponse(RequestStatus.ALREADY_KNOWN, uaee.getMessage());
        }
        catch (AuthProviderException ape){
            logger.info("Request for registering user {} finished with FAILURE : {}", userReq.getUser(), ape.getMessage());
            return new StatusResponse(RequestStatus.FAILURE, ape.getMessage());
        }
        catch (IllegalArgumentException iae){
            logger.info("Request for registering user {} finished with IllegalArgumentException : {}", userReq.getUser(), iae.getMessage());
            return new StatusResponse(RequestStatus.INVALID_ROLE, "The user role is not valid");
        }
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public StatusResponse deleteUser(@QueryParam("login") String login){
        logger.info("Request for deleting user {}", login);
        authUserService.deleteUser(login);
        return new StatusResponse(RequestStatus.SUCCESS);
    }

    @PUT
    @Path("/update/role")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public StatusResponse updateRole(UserUpdateRequest updatedUser){
        logger.info("Request for updating role of user {}", updatedUser.getLogin());
        try {
            authUserService.updateUserRole(updatedUser.getLogin(), updatedUser.getPassword(), UserRole.valueOf(updatedUser.getUpdatedRole()));
            logger.info("Request for updating role of user {} finished with SUCCESS", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.SUCCESS);
        }
        catch (UnknownUserException uue){
            logger.info("Request for updating role of user {} finished with UNKNOWN", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.UNKNOWN,uue.getMessage());
        }
        catch (InvalidCredentialsException ice){
            logger.info("Request for updating role of user {} finished with INVALID_CREDENTIALS", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.INVALID_CREDENTIALS,ice.getMessage());
        }
        catch (AuthProviderException ape){
            logger.info("Request for updating role of user {} finished with FAILURE : {}", updatedUser.getLogin(), ape.getMessage());
            return new StatusResponse(RequestStatus.FAILURE,ape.getMessage());
        }
        catch (IllegalArgumentException iae){
            return new StatusResponse(RequestStatus.INVALID_ROLE, "The updated role is not valid");
        }
    }

    @PUT
    @Path("/update/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public StatusResponse updatePassword(UserUpdateRequest updatedUser){
        logger.info("Request for update password of user {}", updatedUser.getLogin());
        try{
            authUserService.updatePassword(updatedUser.getLogin(), updatedUser.getPassword(),updatedUser.getUpdatedPassword());
            logger.info("Request SUCCESS for update password of user {}", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.SUCCESS);
        }
        catch (UnknownUserException uue){
            logger.info("Update password of UNKNOWN user {}", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.UNKNOWN,uue.getMessage());
        }
        catch (CredentialsNotFoundException | InvalidCredentialsException ice){
            logger.info("Update password invalid credentials for user {}", updatedUser.getLogin());
            return new StatusResponse(RequestStatus.INVALID_CREDENTIALS,ice.getMessage());
        }
        catch (AuthProviderException ape){
            logger.info("Update password for user {}, failure : {}", updatedUser.getLogin(), ape.getMessage());
            return new StatusResponse(RequestStatus.FAILURE,ape.getMessage());
        }
    }

    @PUT
    @Path("/force/update/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("adminRole")
    public StatusResponse forceUpdatePassword(UserUpdateRequest updatedUser){
        logger.info("Request for force updating password for user {}",updatedUser.getLogin());
        try{
            authUserService.forceUpdatePassword(updatedUser.getLogin(), updatedUser.getUpdatedPassword());
            logger.info("Force update password success for {}",updatedUser.getLogin());
            return new StatusResponse(RequestStatus.SUCCESS);
        }
        catch (UnknownUserException uue){
            logger.info("Force update password user unknown : {}",updatedUser.getLogin());
            return new StatusResponse(RequestStatus.UNKNOWN,uue.getMessage());
        }
        catch (CredentialsNotFoundException cnf){
            logger.info("Force update invalid new password for user : {}",updatedUser.getLogin());
            return new StatusResponse(RequestStatus.INVALID_CREDENTIALS,cnf.getMessage());
        }
        catch (AuthProviderException ape){
            logger.info("Force update password for user : {} failure : {}",updatedUser.getLogin(), ape.getMessage());
            return new StatusResponse(RequestStatus.FAILURE,ape.getMessage());
        }
    }
}
