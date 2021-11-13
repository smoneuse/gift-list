package org.scilab.giftlist.resources.lists;

import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.resources.lists.models.request.GiftListCreateUpdateRequestModel;
import org.scilab.giftlist.resources.lists.models.response.GiftListResponseModel;
import org.seedstack.seed.Logging;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.principals.Principals;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for Gift list management
 */
@Path("list")
public class GiftListResource {
    @Logging
    private Logger logger;

    @Inject
    private SecuritySupport securitySupport;

    @Inject
    private GiftListsService giftListsService;
    @Inject
    private AccountService accountService;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public List<GiftListResponseModel> allUserList(){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        List<GiftListResponseModel> response= new ArrayList<>();
        try {
            List<GiftList> userGiftLists = accountService.ownedLists(currentUserLogin);
            for(GiftList aList: userGiftLists){
                GiftListResponseModel responsePart =new GiftListResponseModel(aList);
                aList.getViewers().forEach(viewer -> responsePart.addViewer(viewer.getLogin()));
                aList.getGifts().forEach(responsePart::addGiftResponseForOwner);
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
    @Path("/detail/{listId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftListResponseModel detail(@PathParam("listId") String listId){
        GiftList requiredList = giftListsService.single(listId).orElseThrow(()->new NotFoundException("No list with identifier "+listId));
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftListResponseModel response = new GiftListResponseModel(requiredList);
        if(giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            requiredList.getGifts().forEach(response::addGiftResponseForOwner);
            requiredList.getViewers().forEach(viewer ->response.addViewer(viewer.getLogin()));
        }
        else if(giftListsService.checkUserIsViewer(currentUserLogin, requiredList)){
            requiredList.getGifts().forEach(response::addGiftResponseForViewer);
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
            throw new InternalServerErrorException(gle.getMessage());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftListResponseModel update(GiftListCreateUpdateRequestModel giftListModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+giftListModel.getListId()));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            throw new BadRequestException("You have no permission on this gift list");
        }
        try {
            GiftList updatedList=giftListsService.update(giftListModel.getListId(), giftListModel.getTitle(), giftListModel.getDescription());
            return detail(updatedList.getId());
        }
        catch (GiftListException gle){
            throw new InternalServerErrorException(gle.getMessage());
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
            throw new BadRequestException("You are not owner of this Gift List");
        }
        try {
            accountService.addViewer(viewerId, giftListId);
            logger.info("Viewer {} was added to list {}", viewerId, giftListId);
            return detail(giftListId);
        }
        catch (GiftListException gle){
            logger.warn("Exception while adding viewer {} to list {} : {}",viewerId, giftListId, gle.getMessage() );
            throw new InternalServerErrorException(gle.getMessage());
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
            if(!currentUserLogin.equals(viewerId) || !giftListsService.checkUserIsViewer(currentUserLogin, requiredList)){
                throw new BadRequestException("You have no permission on this list : Revocation by owner or viewer himself");
            }
        }
        try {
            accountService.revokeViewer(viewerId, giftListId);
            logger.info("User {} has been revoked from {}", viewerId, giftListId);
        }
        catch(GiftListException gle){
            logger.warn("Exception while revoking {} from {} viewers. {}", viewerId, giftListId, gle.getMessage());
            throw new InternalServerErrorException(gle.getMessage());
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
        giftListsService.deleteList(giftListId);
    }
}
