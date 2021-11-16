package org.scilab.giftlist.internal.misc;

import org.scilab.giftlist.infra.exceptions.GiftListException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GLDateUtils {
    private static final String GL_RESPONSES_DTE_FORMAT="dd/MM/yyyy";
    private static final SimpleDateFormat SDF = new SimpleDateFormat(GL_RESPONSES_DTE_FORMAT);

    public static String formatDate(Date aDate){
        return SDF.format(aDate);
    }

    public static Date parseDate(String dateStr) throws GiftListException {
        try {
            return SDF.parse(dateStr);
        }
        catch (ParseException pe){
            throw new GiftListException("Can't parse date "+dateStr);
        }
    }
}
