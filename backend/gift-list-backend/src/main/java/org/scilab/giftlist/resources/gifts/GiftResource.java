package org.scilab.giftlist.resources.gifts;

import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.domain.models.list.Gift;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.list.GiftTag;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.gift.GiftService;
import org.scilab.giftlist.internal.gift.GiftStatus;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.internal.misc.GLDateUtils;
import org.scilab.giftlist.internal.misc.LinksUtils;
import org.scilab.giftlist.resources.gifts.models.request.GiftCreateUpdateRequestModel;
import org.scilab.giftlist.resources.gifts.models.request.GiftReserveOrReleaseRequestModel;
import org.scilab.giftlist.resources.gifts.models.response.GiftResponseModel;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for gifts
 */
@Path("/gifts")
@Transactional
@JpaUnit("appUnit")
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
        Optional<GiftResponseModel> responseOpt = buildGiftResponse(requiredGift, requiredList);
        if(!responseOpt.isPresent()){
            Response errorResponse = Response.status(400).entity("You have no permission on that gift").build();
            throw new BadRequestException(errorResponse);
        }
        return responseOpt.get();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private Optional<GiftResponseModel> buildGiftResponse(Gift aGift, GiftList actualList){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftResponseModel response = new GiftResponseModel(aGift, actualList.getId());
        if (giftListsService.checkUserIsOwner(currentUserLogin, actualList)) {
            if(aGift.getStatus().equals(GiftStatus.RESERVED.toString())){
                response.setStatus(GiftStatus.AVAILABLE.toString());
            }
            else{
                response.setStatus(aGift.getStatus());
            }
            if(aGift.getStatus().equals(GiftStatus.GIVEN.toString())){
                response.setGenerousGiver(aGift.getGiver().getLogin());
                response.setOfferingDate(GLDateUtils.formatDate(aGift.getDeliveryDate()));
            }
        } else if (giftListsService.checkUserIsViewerOrFriend(currentUserLogin, actualList)) {
            if(aGift.getStatus().equals(GiftStatus.RESERVED.toString()) && currentUserLogin.equals(aGift.getGiver().getLogin())) {
                response.setGenerousGiver(currentUserLogin);
                response.setOfferingDate(GLDateUtils.formatDate(aGift.getDeliveryDate()));
            }
        }
        else {
            return Optional.empty();
        }
        return Optional.of(response);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRoles("userRole")
    public GiftResponseModel create(GiftCreateUpdateRequestModel giftModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(giftModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+giftModel.getListId()));
        if(!giftListsService.checkUserIsOwner(currentUserLogin, requiredList)){
            logger.warn("Request by user {} to create gift on non owned list {}", currentUserLogin, giftModel.getListId());
            Response errorResponse = Response.status(400).entity("You have no permission the list to create a gift").build();
            throw new BadRequestException(errorResponse);
        }
        Gift createdGift=null;
        try {
            createdGift = giftService.createGift(giftModel.getListId(), giftModel.getTitle(), giftModel.getComment(), giftModel.getRating(), giftModel.getLinks(), giftModel.getTags());
            Optional<GiftResponseModel> responseOpt = buildGiftResponse(createdGift, requiredList);
            if(!responseOpt.isPresent()){
                Response errorResponse = Response.status(400).entity("You have no permission on that gift").build();
                throw new BadRequestException(errorResponse);
            }
            return responseOpt.get();
        }
        catch (GiftListException gle){
            logger.warn("Exception while creating a new gift on list {} : {}", giftModel.getListId(), gle.getMessage());
            if(createdGift!=null){
                logger.warn("Deleting created gift");
                try {
                    giftService.remove(giftModel.getListId(),createdGift.getId());
                }
                catch (GiftListException gle2){
                    Response errorResponse = Response.status(500).entity(gle2.getMessage()).build();
                    throw new InternalServerErrorException(errorResponse);
                }
            }
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
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
            List<String> actualLinks= LinksUtils.toList(updated.getLinks());
            for(String modelLink : giftModel.getLinks()){
                if(!actualLinks.contains(modelLink)){
                    giftService.addLink(updated.getId(), modelLink);
                }
            }
            for(String giftLink : actualLinks){
                if(!giftModel.getLinks().contains(giftLink)){
                    giftService.removeLink(updated.getId(), giftLink);
                }
            }
            List<String> lowerCaseTags=getLowerCaseTags(giftModel);
            for (GiftTag actualTag :updated.getTags()){
                if(!lowerCaseTags.contains(actualTag.getName())){
                    giftService.removeTag(updated.getId(), actualTag.getName());
                }
            }
            for(String tagToAddOrPresent : lowerCaseTags){
                giftService.addTag(updated.getId(), tagToAddOrPresent);
            }
            Optional<GiftResponseModel> responseOpt = buildGiftResponse(updated, requiredList);
            if(!responseOpt.isPresent()){
                Response errorResponse = Response.status(400).entity("You have no permission on that gift").build();
                throw new BadRequestException(errorResponse);
            }
            return responseOpt.get();
        }
        catch (GiftListException gle){
            logger.warn("Exception while user {}, attempted to update gift {} : {}", currentUserLogin, giftModel.getGiftId(), gle.getMessage());
            throw new InternalServerErrorException("Exception while updating gift :"+gle.getMessage());
        }
    }

    private List<String> getLowerCaseTags(GiftCreateUpdateRequestModel giftModel){
        if(giftModel.getTags()==null || giftModel.getTags().isEmpty()){
            return Collections.emptyList();
        }
        List<String> lowerCaseList= new ArrayList<>();
        for(String aTag : giftModel.getTags()){
            if(StringUtils.isNotBlank(aTag.trim())){
                lowerCaseList.add(aTag.trim().toLowerCase());
            }
        }
        return lowerCaseList;
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
            giftService.remove(giftListId,giftId);
        }
        catch (GiftListInvalidParameterException glipe){
            //Just log
            logger.warn("Delete gift request received on non existing gift or list :{}", glipe.getMessage());
        }
        catch (GiftListException gle){
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
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
        if(!giftListsService.checkUserIsViewerOrFriend(currentUserLogin, requiredList)){
            Response errorResponse = Response.status(400).entity("You are not viewer/friend of this list").build();
            throw new InternalServerErrorException(errorResponse);
        }
        if(!requiredGift.getStatus().equals(GiftStatus.AVAILABLE.toString())){
            Response errorResponse = Response.status(400).entity("The gift is not AVAILABLE").build();
            throw new InternalServerErrorException(errorResponse);
        }
        try{
            giftService.reserve(reservationModel.getGiftId(), currentUserLogin,GLDateUtils.parseDate(reservationModel.getDeliveryDate()));
            Optional<GiftResponseModel> responseOpt = buildGiftResponse(requiredGift, requiredList);
            if(!responseOpt.isPresent()){
                Response errorResponse = Response.status(400).entity("You have no permission on that gift").build();
                throw new BadRequestException(errorResponse);
            }
            return responseOpt.get();
        }
        catch (GiftListException gle){
            logger.warn("Exception while reserving a gift "+gle.getMessage());
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/release")
    @RequiresRoles("userRole")
    public void release(GiftReserveOrReleaseRequestModel reservationModel){
        String currentUserLogin=securitySupport.getSimplePrincipalByName(Principals.IDENTITY).getValue();
        GiftList requiredList = giftListsService.single(reservationModel.getListId()).orElseThrow(()->new NotFoundException("No list with identifier "+reservationModel.getListId()));
        Gift requiredGift = giftService.single(reservationModel.getGiftId()).orElseThrow(()-> new NotFoundException("No gift with ID "+reservationModel.getGiftId()));
        if(!requiredGift.getStatus().equals(GiftStatus.RESERVED.toString())){
            Response errorResponse = Response.status(400).entity("The gift is not RESERVED").build();
            throw new BadRequestException(errorResponse);
        }
        if(!giftListsService.checkUserIsViewerOrFriend(currentUserLogin, requiredList) || !currentUserLogin.equals(requiredGift.getGiver().getLogin())){
            Response errorResponse = Response.status(400).entity("You are not viewer/friend or current giver of this list").build();
            throw new BadRequestException(errorResponse);
        }
        try{
            giftService.release(reservationModel.getGiftId(), currentUserLogin);
        }
        catch (GiftListException gle){
            logger.warn("Exception while releasing a gift "+gle.getMessage());
            Response errorResponse = Response.status(500).entity(gle.getMessage()).build();
            throw new InternalServerErrorException(errorResponse);
        }
    }
}
