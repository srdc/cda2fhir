package tr.com.srdc.cda2fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.IParser;

/**
 * Created by mustafa on 8/4/2016.
 */
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

    public static void printXML(IResource res) {
        System.out.println(xmlParser.encodeResourceToString(res));
    }
}
