package org.scilab.giftlist.resources.users;

import com.google.common.base.Strings;
import org.scilab.giftlist.domain.models.User;
import org.scilab.giftlist.infra.exceptions.GiftListDataAlreadyExistException;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.resources.users.representation.SingleUserRepresentation;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Logging;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Path("user")
public class UserResource {
    @Logging
    private Logger logger;

    @Inject
    @Jpa
    private Repository<User, String> userRepo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/id/{id}")
    @Transactional
    @JpaUnit("appUnit")
    public SingleUserRepresentation getSingle(@PathParam("id") String identifier){
        logger.info("Try retrieving user with ID {}", identifier);
        Optional<User> userOpt =userRepo.get(identifier);
        return new SingleUserRepresentation(userOpt.orElseThrow(()->new NotFoundException("User with ID not found : "+identifier)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    @Transactional
    @JpaUnit("appUnit")
    public List<SingleUserRepresentation> search(@QueryParam("name") String userName){
        List<SingleUserRepresentation> response = new ArrayList<>();
        if(Strings.isNullOrEmpty(userName)){
            return response;
        }
        Specification<User> searchByNameSpec = userRepo.getSpecificationBuilder()
                .of(User.class)
                .property("name")
                .matching("*"+userName+"*")
                .build();

        userRepo.get(searchByNameSpec).forEach(user -> {
            response.add(new SingleUserRepresentation(user));
        });
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    @Transactional
    @JpaUnit("appUnit")
    public List<SingleUserRepresentation> all(){
        Specification<User> allUsersSpec =userRepo.getSpecificationBuilder().of(User.class).all().build();
        List<SingleUserRepresentation> response = new ArrayList<>();
        userRepo.get(allUsersSpec).forEach(user -> {
            response.add(new SingleUserRepresentation(user));
        });
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @JpaUnit("appUnit")
    public SingleUserRepresentation createUser(SingleUserRepresentation userToCreate){
        User newUser = new User(UUID.randomUUID().toString());
        try {
            checkNewOrUpdateData(userToCreate);
            newUser.setName(userToCreate.getName());
        }
        catch (GiftListException gfe){
            throw  new BadRequestException(gfe.getMessage());
        }
        newUser.seteMail(userToCreate.geteMail());
        userRepo.add(newUser);
        return getSingle(newUser.getId());
    }

    @DELETE
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @JpaUnit("appUnit")
    public void deleteUser(@PathParam("id") String userId){
        userRepo.get(userId).ifPresent(user -> {
            logger.info("Received request for deleting user with ID : {}", userId);
            userRepo.remove(user);
        });
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @JpaUnit("appUnit")
    public SingleUserRepresentation update(SingleUserRepresentation updatedUser){
        User userToUpdate = userRepo.get(updatedUser.getId()).orElseThrow(()-> new NotFoundException("User with could not be found : "+updatedUser.getId()));
        logger.info("Received request for updating user with ID : {}", updatedUser.getId());
        try {
            checkNewOrUpdateData(updatedUser);
            userToUpdate.setName(updatedUser.getName());
            userToUpdate.seteMail(updatedUser.geteMail());
        }
        catch(GiftListException gle){
            throw new BadRequestException(gle.getMessage());
        }
        return new SingleUserRepresentation(userRepo.update(userToUpdate));
    }


    private void checkNewOrUpdateData(SingleUserRepresentation userToCheck) throws GiftListException{
        Specification<User> alreadyExistSpec = userRepo.getSpecificationBuilder()
                .of(User.class)
                .property("name").equalTo(userToCheck.getName())
                .or()
                .property("eMail").equalTo(userToCheck.geteMail())
                .and()
                .property("id").not().equalTo(userToCheck.getId())
                .build();

        if(userRepo.contains(alreadyExistSpec)){
            userRepo.get(alreadyExistSpec).forEach(user1 -> {
                logger.error("checkNewOrUpdateData fail with existing user ID : {}", user1.getId());
            });
            throw new GiftListDataAlreadyExistException("Data can't be processed : Same value already exist");
        }
    }
}
