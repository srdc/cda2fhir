package tr.com.srdc.cda2fhir.util;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.regex.Pattern;

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
