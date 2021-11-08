package org.scilab.authProvider.resources.responses;

import com.google.common.base.Strings;
import org.scilab.authProvider.internal.enums.RequestStatus;

/**
 * Response with only status
 */
public class StatusResponse {
    private String status;
    private String detail;

    public StatusResponse(RequestStatus status, String detail){
        this.status=status.toString();
        this.detail= Strings.nullToEmpty(detail);
    }

    public StatusResponse(RequestStatus status){
        this.status=status.toString();
        this.detail="";
    }

    public String getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
