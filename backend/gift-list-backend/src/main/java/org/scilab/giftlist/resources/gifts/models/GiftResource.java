package org.scilab.giftlist.resources.gifts.models;

import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.gift.GiftService;
import org.scilab.giftlist.internal.gift.GiftStatus;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.internal.misc.GLDateUtils;
import org.scilab.giftlist.resources.gifts.models.request.GiftCreateUpdateRequestModel;
import org.scilab.giftlist.resources.gifts.models.request.GiftReserveOrReleaseRequestModel;
import org.scilab.giftlist.resources.gifts.models.response.GiftResponseModel;
import org.seedstack.seed.Logging;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.principals.Principals;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Entry point for gifts
 */
@Path("/gifts")
public class GiftResource {

    @Logging
    private Logger logger;

    @Inject
    private SecuritySupport securitySupport;
    @Inject
    private GiftListsService giftListsService;
    @Inject
    private GiftService giftService;
    @Inject
    private AccountService accountService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/detail/{giftListId}/{giftId}")
    @RequiresRoles("userRole")
    public GiftResponseModel detail(@PathParam("giftListId") String giftListId, @PathParam("giftId") String giftId){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftListId).orElseThrow(()->new NotFoundException("No list with identifier "+giftListId));
        Gift requiredGift = giftService.single(giftId).orElseThrow(()-> new NotFoundException("No gift with ID "+giftId));
        GiftResponseModel response = new GiftResponseModel(requiredGift);
        if (giftListsService.checkUserIsOwner(currentUserLogin, requiredList)) {
            if(requiredGift.getStatus().equals(GiftStatus.RESERVED.toString())){
                response.setStatus(GiftStatus.AVAILABLE.toString());
            }
            else{
                response.setStatus(requiredGift.getStatus());
            }
            if(requiredGift.getStatus().equals(GiftStatus.GIVEN.toString())){
                response.setGenerousGiver(requiredGift.getGiver().getLogin());
                response.setOfferingDate(GLDateUtils.formatDate(requiredGift.getDeliveryDate()));
            }
        } else if (giftListsService.checkUserIsViewer(currentUserLogin, requiredList) && (currentUserLogin.equals(requiredGift.getGiver().getLogin()))) {
            response.setGenerousGiver(currentUserLogin);
            response.setOfferingDate(GLDateUtils.formatDate(requiredGift.getDeliveryDate()));
        }
        else {
            throw new BadRequestException("You have no permission on that gift");
        }
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftResponseModel create(GiftCreateUpdateRequestModel giftModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+giftModel.getListId()));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            logger.warn("Request by user {} toi create gift on non owned list {}", currentUserLogin, giftModel.getListId());
            throw new BadRequestException("You have no permission the list to create a gift");
        }
        Gift createdGift=null;
        try {
            createdGift = giftService.createGift(giftModel.getListId(), giftModel.getTitle(), giftModel.getComment(), giftModel.getRating());
            for(String link : giftModel.getLinks()){
                giftService.addLink(createdGift.getId(), link);
            }
            return detail(giftModel.getListId(),createdGift.getId());
        }
        catch (GiftListException gle){
            logger.warn("Exception while creating a new gift on list {} : {}", giftModel.getListId(), gle.getMessage());
            if(createdGift!=null){
                logger.warn("Deleting created gift");
                giftService.remove(createdGift.getId());
            }
            throw new InternalServerErrorException(gle.getMessage());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftResponseModel update(GiftCreateUpdateRequestModel giftModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+giftModel.getListId()));
        Gift requiredGift = giftService.single(giftModel.getGiftId()).orElseThrow(()-> new NotFoundException("No gift with ID "+giftModel.getGiftId()));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            throw new BadRequestException("You have no permission on the gift's list");
        }
        try{
            Gift updated=giftService.update(giftModel.getListId(), giftModel.getGiftId(), giftModel.getTitle(), giftModel.getComment(), giftModel.getRating());
            for(String modelLink : giftModel.getLinks()){
                if(!updated.getLinks().contains(modelLink)){
                    giftService.addLink(updated.getId(), modelLink);
                }
            }
            for(String giftLink : updated.getLinks()){
                if(!giftModel.getLinks().contains(giftLink)){
                    giftService.removeLink(updated.getId(), giftLink);
                }
            }
            return detail(giftModel.getListId(), updated.getId());
        }
        catch (GiftListException gle){
            logger.warn("Exception while user {}, attempted to update gift {} : {}", currentUserLogin, giftModel.getGiftId(), gle.getMessage());
            throw new InternalServerErrorException("Exception while updating gift :"+gle.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/remove/{giftListId}/{giftId}")
    @RequiresRoles("userRole")
    public void delete(@PathParam("giftListId") String giftListId, @PathParam("giftId") String giftId){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        try {
            GiftList giftList = giftListsService.single(giftListId).orElseThrow(() -> new GiftListInvalidParameterException("No list with identifier " + giftListId));
            Gift requiredGift = giftService.single(giftId).orElseThrow(() -> new GiftListInvalidParameterException("No gift with ID " + giftId));
            if(!giftListsService.checkUserIsOwner(currentUserLogin,giftList )){
                throw new BadRequestException("No permission on that list :"+giftListId);
            }
            giftService.remove(giftId);
        }
        catch (GiftListInvalidParameterException glipe){
            //Just log
            logger.warn("Delete gift request received on non existing gift or list :{}", glipe.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/reservation")
    @RequiresRoles("userRole")
    public GiftResponseModel reserve(GiftReserveOrReleaseRequestModel reservationModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(reservationModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+reservationModel.getListId()));
        Gift requiredGift = giftService.single(reservationModel.getGiftId()).orElseThrow(()-> new NotFoundException("No gift with ID "+reservationModel.getGiftId()));
        if(!giftListsService.checkUserIsViewer(currentUserLogin, requiredList)){
            throw new BadRequestException("You are not viewer of this list");
        }
        if(requiredGift.getStatus().equals(GiftStatus.AVAILABLE.toString())){
            throw new BadRequestException("The gift is not AVAILABLE");
        }
        try{
            giftService.reserve(reservationModel.getGiftId(), currentUserLogin,GLDateUtils.parseDate(reservationModel.getDeliveryDate()));
            return detail(requiredList.getId(), requiredGift.getId());
        }
        catch (GiftListException gle){
            logger.warn("Exception while reserving a gift "+gle.getMessage());
            throw new InternalServerErrorException("Exception while reserving a gift "+gle.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/reservation")
    @RequiresRoles("userRole")
    public GiftResponseModel release(GiftReserveOrReleaseRequestModel reservationModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(reservationModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+reservationModel.getListId()));
        Gift requiredGift = giftService.single(reservationModel.getGiftId()).orElseThrow(()-> new NotFoundException("No gift with ID "+reservationModel.getGiftId()));
        if(requiredGift.getStatus().equals(GiftStatus.RESERVED.toString())){
            throw new BadRequestException("The gift is not RESERVED");
        }
        if(!giftListsService.checkUserIsViewer(currentUserLogin, requiredList) || !currentUserLogin.equals(requiredGift.getGiver().getLogin())){
            throw new BadRequestException("You are not viewer or current giver of this list");
        }
        try{
            giftService.release(reservationModel.getGiftId(), currentUserLogin);
            return detail(requiredList.getId(), requiredGift.getId());
        }
        catch (GiftListException gle){
            logger.warn("Exception while releasing a gift "+gle.getMessage());
            throw new InternalServerErrorException("Exception while releasing a gift "+gle.getMessage());
        }
    }
}
