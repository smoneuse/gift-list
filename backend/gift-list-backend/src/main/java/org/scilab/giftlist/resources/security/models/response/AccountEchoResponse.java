package org.scilab.giftlist.resources.security.models.response;

import com.google.common.base.Strings;
import org.scilab.giftlist.internal.requests.ResponseStatus;

/**
 * Model for account echoing request response
 */
public class AccountEchoResponse {
    private final String login;
    private final String echo;
    private final String status;

    public AccountEchoResponse(String login, String echo, ResponseStatus status){
        this.login= Strings.nullToEmpty(login);
        this.echo=Strings.nullToEmpty(echo);
        this.status=status.toString();
    }

    public String getLogin() {
        return login;
    }

    public String getEcho() {
        return echo;
    }


    public String getStatus() {
        return status;
    }
}
