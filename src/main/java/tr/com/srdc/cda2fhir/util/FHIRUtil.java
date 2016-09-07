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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.IParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FHIRUtil {



    private static final FhirContext myCtx = FhirContext.forDstu2();
    private static IParser jsonParser = myCtx.newJsonParser();
    private static IParser xmlParser = myCtx.newXmlParser();

    static {
        jsonParser.setPrettyPrint(true);
        xmlParser.setPrettyPrint(true);
    }

    public static void printJSON(IResource res) {
        System.out.println(jsonParser.encodeResourceToString(res));
    }

    public static void printJSON(IResource res, String filePath) {
        File f = new File(filePath);
        f.getParentFile().mkdirs();
        try {
            jsonParser.encodeResourceToWriter(res, new FileWriter(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printXML(IResource res) {
        System.out.println(xmlParser.encodeResourceToString(res));
    }
    
    public static String getXML(IResource res) {
    	return xmlParser.encodeResourceToString(res);
    }

    public static void printXML(IResource res, String filePath) {
        File f = new File(filePath);
        f.getParentFile().mkdirs();
        try {
            xmlParser.encodeResourceToWriter(res, new FileWriter(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
