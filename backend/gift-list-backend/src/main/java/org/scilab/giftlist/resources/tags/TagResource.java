package org.scilab.giftlist.resources.tags;

import org.scilab.giftlist.domain.models.list.GiftTag;
import org.seedstack.business.domain.Repository;
import org.seedstack.business.specification.Specification;
import org.seedstack.jpa.Jpa;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.security.RequiresRoles;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("tags")
@Transactional
@JpaUnit("appUnit")
public class TagResource {

    @Inject
    @Jpa
    private Repository<GiftTag, String> giftTagRepository;

    @GET
    @Path("/search/{literal}")
    @RequiresRoles("userRole")
    public Collection<String> searchTag(@PathParam("literal") String literal){
        Specification<GiftTag> nameSpec= this.giftTagRepository.getSpecificationBuilder()
                .of(GiftTag.class)
                .property("name").matching(literal.trim().toLowerCase()+"*").build();
        Set<String> result= new HashSet<>();
        this.giftTagRepository.get(nameSpec).forEach(aTag->result.add(aTag.getName()));
        return result;
    }

}
