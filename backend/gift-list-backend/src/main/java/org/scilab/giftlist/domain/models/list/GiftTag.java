package org.scilab.giftlist.domain.models.list;

import org.apache.commons.lang3.StringUtils;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.exceptions.GiftListInvalidParameterException;
import org.seedstack.business.domain.BaseAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * DB model for Gift tag
 */
@Entity
public class GiftTag extends BaseAggregateRoot<String> {
    @Id
    private String name;

    /**Empty constructor for hibernate*/
    public GiftTag(){}

    /**
     * Constructor for a new tag
     * @param name The tag's name
     * @throws GiftListException Exception for invalid name
     */
    public GiftTag(String name) throws GiftListException {
        this.setName(name);
    }

    /**
     * Sets the tag name
     * @param name The tag's name
     * @throws GiftListException Exception for invalid name
     */
    public void setName(String name) throws GiftListException{
        if(StringUtils.isBlank(name)){
            throw new GiftListInvalidParameterException("A tag name cannot be empty");
        }
        if(StringUtils.isBlank(name.trim())){
            throw new GiftListInvalidParameterException("A tag name cannot be only spaces");
        }
        this.name = name.trim().toLowerCase();
    }

    @Override
    public String getId() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }
}
