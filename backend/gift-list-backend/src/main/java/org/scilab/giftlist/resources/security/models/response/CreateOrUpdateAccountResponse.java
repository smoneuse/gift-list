package org.scilab.giftlist.resources.security.models.response;

import com.google.common.base.Strings;
import org.scilab.giftlist.internal.requests.ResponseStatus;

/**
 * Response for create or update an account
 */
public class CreateOrUpdateAccountResponse {
    private final String account;
    private final String status;
    private final String detail;

    public CreateOrUpdateAccountResponse(String login, ResponseStatus status, String detail){
        this.account=login;
        this.status=status.toString();
        this.detail= Strings.nullToEmpty(detail);
    }

    public String getStatus() {
        return status;
    }

    public String getAccount() {
        return account;
    }

    public String getDetail() {
        return detail;
    }
}
