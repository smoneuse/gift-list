package org.scilab.giftlist.domain.models;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

/**
 * Gift List model
 */
@Entity
public class GiftList extends BaseAggregateRoot<String> {
    @Id
    public String id;
    public String title;
    public String description;

    /**Hibernate required*/
    private GiftList(){}

    /**
     * Builds a new list with a given identifier
     * @param id list identifier
     */
    public GiftList(String id){
        this.id=id;
    }

    /**
     * Sets the title and description properties of this list
     * @param title list title
     * @param description list Description
     * @throws GiftListInvalidParameterException Thrown if title is blank or null
     */
    public void setTitleAndDescription(String title, String description) throws GiftListInvalidParameterException {
        if(StringUtils.isBlank(title)){
            throw new GiftListInvalidParameterException("Can't create a list without a title");
        }
        this.title = title;
        this.description= Optional.ofNullable(description).orElse(StringUtils.EMPTY);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
