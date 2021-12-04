package org.scilab.giftlist.resources.lists;

import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.scilab.giftlist.resources.lists.models.request.AddOrRevokeViewerToListsRequestModel;
import org.scilab.giftlist.resources.lists.models.request.GiftListCreateUpdateRequestModel;
import org.scilab.giftlist.resources.lists.models.response.GiftListResponseModel;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entry point for Gift list management
 */
@Path("list")
@Transactional
@JpaUnit("appUnit")
public class GiftListResource {
    @Logging
    private Logger logger;

    @Inject
    private SecuritySupport securitySupport;

    @Inject
    private GiftListsService giftListsService;
    @Inject
    private AccountService accountService;

    @Inject
    private AuthUserService authUserService;


    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public List<GiftListResponseModel> allUserList(){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        List<GiftListResponseModel> response= new ArrayList<>();
        try {
            Set<GiftList> userGiftLists = accountService.ownedLists(currentUserLogin);
            for(GiftList aList: userGiftLists){
                GiftListResponseModel responsePart =new GiftListResponseModel(aList);
                aList.getViewers().stream().distinct().forEach(viewer -> responsePart.addViewer(viewer.getLogin()));
                aList.getGifts().stream().distinct().forEach(responsePart::addGiftResponseForOwner);
                response.add(responsePart);
            }
            return response;
        }
        catch (GiftListException gle){
            logger.warn("Exception while user {} requested its lists", currentUserLogin);
            throw new InternalServerErrorException(gle.getMessage());
        }
    }
    @GET
    @Path("/authorized")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public Set<GiftListResponseModel> allAuthorizedLists(){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        try {
            Set<GiftList> authorizedLists = accountService.authorizedLists(currentUserLogin);
            Set<GiftListResponseModel> response= new HashSet<>();
            for(GiftList aList: authorizedLists){
                GiftListResponseModel responsePart =new GiftListResponseModel(aList);
                aList.getGifts().stream().distinct().forEach(aGift-> responsePart.addGiftResponseForViewer(aGift, currentUserLogin));
                response.add(responsePart);
            }
            return response;
        }
        catch (GiftListException gle){
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @GET
    @Path("/detail/{listId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftListResponseModel detail(@PathParam("listId") String listId){
        GiftList requiredList = giftListsService.single(listId).orElseThrow(()->new NotFoundException("No list with identifier "+listId));
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftListResponseModel response = new GiftListResponseModel(requiredList);

        Set<Gift> sortedGiftList=requiredList.getGifts().stream().sorted((gift1, gift2) -> gift1.getTitle().toLowerCase().compareTo(gift2.getTitle().toLowerCase())).collect(Collectors.toSet());
        if(giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            sortedGiftList.forEach(response::addGiftResponseForOwner);
            requiredList.getViewers().stream().distinct().forEach(viewer ->response.addViewer(viewer.getLogin()));
        }
        else if(giftListsService.checkUserIsViewerOrFriend(currentUserLogin, requiredList)){
            sortedGiftList.forEach(aGift->response.addGiftResponseForViewer(aGift,currentUserLogin));
        }
        else{
            throw new NotFoundException("No list to show");
        }
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftListResponseModel create(GiftListCreateUpdateRequestModel giftListModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        try {
            GiftList gList=giftListsService.createList(currentUserLogin, giftListModel.getTitle(), giftListModel.getDescription());
            return detail(gList.getId());
        }
        catch (GiftListException gle){
            logger.warn("Problem while creating a gift list for user {}", currentUserLogin);
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftListResponseModel update(GiftListCreateUpdateRequestModel giftListModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListModel.getId()).orElseThrow(()->new NotFoundException("No list with identifier "+giftListModel.getId()));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            Response errorResponse = Response.status(400).entity("You have no permission on this gift list").build();
            throw new BadRequestException(errorResponse);
        }
        try {
            GiftList updatedList=giftListsService.update(giftListModel.getId(), giftListModel.getTitle(), giftListModel.getDescription());
            return detail(updatedList.getId());
        }
        catch (GiftListException gle){
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    @Path("/{giftListId}/viewers/{viewerId}")
    public GiftListResponseModel addViewer(@PathParam("giftListId") String giftListId,@PathParam("viewerId") String viewerId){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListId).orElseThrow(()->new NotFoundException("No list with identifier "+giftListId));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            Response errorResponse = Response.status(400).entity("You are not owner of this Gift List").build();
            throw new BadRequestException(errorResponse);
        }
        if(!authUserService.checkUserKnown(viewerId)){
            Response errorResponse = Response.status(400).entity("No user with login :"+viewerId).build();
            throw new BadRequestException(errorResponse);
        }
        try {
            accountService.addViewer(viewerId, giftListId);
            logger.info("Viewer {} was added to list {}", viewerId, giftListId);
            return detail(giftListId);
        }
        catch (GiftListException gle){
            logger.warn("Exception while adding viewer {} to list {} : {}",viewerId, giftListId, gle.getMessage() );
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/all/viewers")
    @RequiresRoles("userRole")
    public List<GiftListResponseModel> addViewerToAllLists(AddOrRevokeViewerToListsRequestModel request){
        if(StringUtils.isBlank(request.getViewerLogin())){
            Response errorResponse = Response.status(400).entity("Viewer login not provided").build();
            throw new BadRequestException(errorResponse);
        }
        List<GiftListResponseModel> response = new ArrayList<>();
        for(String listId : request.getListIds()){
            response.add(addViewer(listId, request.getViewerLogin()));
        }
        return response;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/all/viewers")
    @RequiresRoles("userRole")
    public void revokeViewerToAllLists(AddOrRevokeViewerToListsRequestModel request){
        if(StringUtils.isBlank(request.getViewerLogin())){
            Response errorResponse = Response.status(400).entity("Viewer login not provided").build();
            throw new BadRequestException(errorResponse);
        }
        List<GiftListResponseModel> response = new ArrayList<>();
        for(String listId : request.getListIds()){
            revokeViewer(listId, request.getViewerLogin());
        }
    }

    @DELETE
    @RequiresRoles("userRole")
    @Path("/{giftListId}/viewers/{viewerId}")
    public void revokeViewer(@PathParam("giftListId") String giftListId,@PathParam("viewerId") String viewerId){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListId).orElseThrow(()->new NotFoundException("No list with identifier "+giftListId));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            //Current user is not owner
            //Only owner and a viewer can revoke itself
            if(!currentUserLogin.equalsIgnoreCase(viewerId) || !giftListsService.checkUserIsViewerOrFriend(currentUserLogin, requiredList)){
                Response errorResponse = Response.status(400).entity("You have no permission on this list : Revocation by owner or viewer himself").build();
                throw new BadRequestException(errorResponse);
            }
        }
        try {
            accountService.revokeViewer(viewerId, giftListId);
            logger.info("User {} has been revoked from {}", viewerId, giftListId);
        }
        catch(GiftListException gle){
            logger.warn("Exception while revoking {} from {} viewers. {}", viewerId, giftListId, gle.getMessage());
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @DELETE
    @RequiresRoles("userRole")
    @Path("/{giftListId}")
    public void delete(@PathParam("giftListId") String giftListId){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListId).orElseThrow(()->new NotFoundException("No list with identifier "+giftListId));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            throw new BadRequestException("You have no permission on this list");
        }
        try {
            giftListsService.deleteList(giftListId);
        }
        catch (GiftListException gle){
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }
}
