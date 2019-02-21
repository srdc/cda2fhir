package tr.com.srdc.cda2fhir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.hl7.fhir.dstu3.model.Procedure;
import org.json.JSONException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.skyscreamer.jsonassert.JSONAssert;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProceduresSectionSnippetTest {
	@BeforeClass
	public static void init() {
		// Load MDHT CDA packages to be able to use CCDA parsing.
		CDAUtil.loadPackages();
	}

	private static Procedure findProcedureById(List<Procedure> procedures, String id) {
		for (Procedure procedure : procedures) {
			for (Identifier identifier : procedure.getIdentifier()) {
				if (id.equals(identifier.getValue())) {
					return procedure;
				}
			}
		}
		return null;
	}

	private static void verifyProcedure(Procedure procedure, String expectedCode, String expectedAnnotation) {
		Assert.assertNotNull("Expect procedure has been found by id", procedure);

		CodeableConcept code = procedure.getCode();

		Assert.assertFalse("Expect no coding for procedure code", code.hasCoding());
		Assert.assertEquals("Expect the code text in the snippet for procedure code", expectedCode, code.getText());

		Assert.assertTrue("Expect procedure note", procedure.hasNote());
		String actualAnnotation = procedure.getNote().get(0).getText();
		Assert.assertEquals("Expect the annotation in the snippet for procedure note", expectedAnnotation,
				actualAnnotation);
	}

	private static void verifyGoldFile(List<Procedure> procedures, String sourceName)
			throws IOException, JSONException {
		String baseName = sourceName.substring(0, sourceName.length() - 4);

		String actual = FHIRUtil.encodeToJSON(procedures);
		FHIRUtil.printJSON(procedures, "src/test/resources/output/" + baseName + ".procedure.json");
		Path goldFilePath = Paths.get("src/test/resources/gold/" + baseName + ".procedure.json");
		String expected = new String(Files.readAllBytes(goldFilePath));
		JSONAssert.assertEquals("Expect procedure gold file fields to remain", expected, actual, false);
	}

	private static void replaceIdWithIdentifier(List<Procedure> procedures) {
		for (Procedure procedure : procedures) {
			List<Identifier> identifiers = procedure.getIdentifier();
			if (identifiers != null && !identifiers.isEmpty()) {
				String newId = identifiers.get(0).getValue();
				procedure.setId(newId);
			}
		}
	}

	@Test
	public void testTextWithReferenceDefinitions() throws Exception {
		Bundle bundle = BundleUtil.generateSnippetBundle("snippets/procedure_text.xml");
		List<Procedure> procedures = BundleUtil.findResources(bundle, Procedure.class, 3);

		// Looks like EMF queries messes up procedure order in the ccda so use ids
		// instead
		Procedure procedure0 = findProcedureById(procedures, "77baeec3-124e-4348-bcec-fbe2fd25e7ef");
		verifyProcedure(procedure0, "APPENDECTOMY LAPAROSCOPIC", "auto-populated from documented surgical case");
		Procedure procedure1 = findProcedureById(procedures, "4372357a-30bb-48d9-a612-8f459ae8c00c");
		verifyProcedure(procedure1, "REMOVAL IMPLANTED DEVICES FROM BONE",
				"auto-populated from documented surgical case");
		Procedure procedure2 = findProcedureById(procedures, "e39bdae7-7b35-4ceb-88b7-8891414c3bc2");
		verifyProcedure(procedure2, "REPAIR ELBOW", "auto-populated from documented surgical case");

		replaceIdWithIdentifier(procedures); // JSONAssert needs a unique key, id changes based on order
		verifyGoldFile(procedures, "snippets/procedure_text.xml");
	}
	
	@Ignore
	@Test
	public void testCerner() throws Exception {
		String file1 = "Cerner/Person-RAKIA_TEST_DOC00001 (1).XML";
		Bundle bundle1 = BundleUtil.generateSnippetBundle(file1);
		BundleUtil.printBundleResources(bundle1, file1, Procedure.class);
		String file2 = "Cerner/Encounter-RAKIA_TEST_DOC00001.XML";
		Bundle bundle2 = BundleUtil.generateSnippetBundle(file2);
		BundleUtil.printBundleResources(bundle2, file2, Procedure.class);
		BundleUtil.printBundleResources(bundle2, file2, Encounter.class);
		BundleUtil.printBundleResources(bundle2, file2, Practitioner.class);
		BundleUtil.printBundleResources(bundle2, file2, Location.class);
	}
}
