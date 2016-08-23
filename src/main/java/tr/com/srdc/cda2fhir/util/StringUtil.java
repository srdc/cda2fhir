package tr.com.srdc.cda2fhir.util;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * Created by mustafa on 8/23/2016.
 */
public class StringUtil {

    // oid one is very simple, not a complete validator
    private static final Pattern OID_PATTERN = Pattern.compile("([0-9]+\\.)*[0-9]+");
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public static boolean isOID(String oidString) {
        return isMatched(oidString, OID_PATTERN);
    }

    public static boolean isUUID(String uuidString) {
        return isMatched(uuidString, UUID_PATTERN);
    }

    private static boolean isMatched(String sourceText, Pattern pattern) {
        if(pattern.matcher(sourceText).matches())
            return true;
        else
            return false;
    }
}
