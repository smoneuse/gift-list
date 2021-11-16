package org.scilab.giftlist.internal.misc;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LinksUtils {
    public static final String EMPTY="";
    private static final String SEPARATOR="<GL_STR_SEP>";

    public static String toLinkString(List<String> links){
        if(links==null){
            return EMPTY;
        }
        StringBuilder strBuild = new StringBuilder();
        for(String aLink:links){
            strBuild.append(aLink);
            strBuild.append(SEPARATOR);
        }
        return StringUtils.removeEnd(strBuild.toString(),SEPARATOR);
    }

    public static List<String> toList(String contactLinks){
        if(StringUtils.isBlank(contactLinks)){
            return Collections.emptyList();
        }
        String[] links = contactLinks.split(SEPARATOR);
        List<String> response = new ArrayList<>();
        response.addAll(Arrays.asList(links));
        return response;
    }
}
