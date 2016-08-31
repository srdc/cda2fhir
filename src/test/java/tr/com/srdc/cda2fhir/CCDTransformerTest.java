package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import tr.com.srdc.cda2fhir.impl.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

import java.io.FileInputStream;

/**
 * Created by mustafa on 8/4/2016.
 */
public class CCDTransformerTest {

    @BeforeClass
    public static void init() {
        // Load MDHT CDA packages. Otherwise ContinuityOfCareDocument and similar documents will not be recognised.
        // This has to be called before loading the document; otherwise will have no effect.
        CDAUtil.loadPackages();
    }

    @Test
    public void testReferenceCCDInstance() throws Exception {
//        FileInputStream fis = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");
        FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");

        ClinicalDocument cda = CDAUtil.load(fis);
        CDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
        Bundle bundle = ccdTransformer.transformDocument(cda);
        if(bundle != null)
            FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD.json");
    }
}
