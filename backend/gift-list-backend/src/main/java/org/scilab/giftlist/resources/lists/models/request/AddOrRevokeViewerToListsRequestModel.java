package org.scilab.giftlist.resources.lists.models.request;

import java.util.Collections;
import java.util.List;

public class AddOrRevokeViewerToListsRequestModel {
    private String viewerLogin;
    private List<String> listIds;

    public String getViewerLogin() {
        return viewerLogin;
    }

    public void setViewerLogin(String viewerLogin) {
        this.viewerLogin = viewerLogin;
    }

    public List<String> getListIds() {
        if(listIds==null){
            return Collections.emptyList();
        }
        return listIds;
    }

    public void setListIds(List<String> listIds) {
        this.listIds = listIds;
    }
}
