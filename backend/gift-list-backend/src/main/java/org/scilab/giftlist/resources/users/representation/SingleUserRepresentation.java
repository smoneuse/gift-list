package org.scilab.giftlist.resources.users.representation;

import org.scilab.giftlist.domain.models.User;

/**
 * Resource representation of a user
 */
public class SingleUserRepresentation {
    private String name;
    private String id;
    private String eMail;

    /**For creation process*/
    public SingleUserRepresentation(){}

    /**
     * Builds a representation from an existing user
     * @param user user to build from
     */
    public SingleUserRepresentation(User user){
        this.name= user.getName();
        this.eMail=user.geteMail();
        this.id= user.getId();
    }

    public String geteMail() {
        return eMail;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public void setId(String id) {
        this.id = id;
    }
}
