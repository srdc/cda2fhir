package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Medication;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.testutil.generator.MedicationActivityGenerator;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;

public class DeduplicationTest {
	static CCDTransformerImpl ccdTransformer;
	static CDAFactories factories;
	static ClinicalDocumentMetadataGenerator metadataGenerator;
	static MedicationActivityGenerator medActGenerator;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		ccdTransformer = new CCDTransformerImpl();
		factories = CDAFactories.init();
		metadataGenerator = new ClinicalDocumentMetadataGenerator();
		medActGenerator = new MedicationActivityGenerator();
	}

	@Ignore
	@Test
	public void testMedicationDeduplication() throws Exception {
		ContinuityOfCareDocument clinicalDoc = metadataGenerator.generateClinicalDoc(factories);
		MedicationActivity medAct = medActGenerator.generate(factories);
		Section section = factories.base.createSection();
		Entry entry = factories.base.createEntry();
		entry.setSubstanceAdministration(medAct);
		section.getEntries().add(entry);
		clinicalDoc.addSection(section);

		Bundle resultBundle = ccdTransformer.transformDocument(clinicalDoc);
		BundleUtil.findResources(resultBundle, Medication.class, 1);
//		Assert.assertEquals()

	}

}
