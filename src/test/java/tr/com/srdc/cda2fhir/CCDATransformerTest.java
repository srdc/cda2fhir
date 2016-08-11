package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import tr.com.srdc.cda2fhir.impl.CCDATransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

import java.io.FileInputStream;

/**
 * Created by mustafa on 8/4/2016.
 */
public class CCDATransformerTest {

    @BeforeClass
    public static void init() {
        CDAUtil.loadPackages();
    }

    @Test
    public void testReferenceCCDInstance() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");
        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml"); 
        
//        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(fis);
        CCDATransformer ccdaTransformer = new CCDATransformerImpl(CCDATransformer.IdGeneratorEnum.COUNTER);
        Bundle bundle = ccdaTransformer.transformCCD(ccd);
        FHIRUtil.printJSON(bundle);
    }
}
