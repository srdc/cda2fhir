package tr.com.srdc.cda2fhir.testutil;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class BundleUtil {
	private Bundle bundle;
	private IIdentifierMap<Resource> identifierMap;
	private Map<String, Resource> idMap;
	
	public BundleUtil(Bundle bundle) {
		this.bundle = bundle;
		identifierMap = IdentifierMapFactory.bundleToResource(bundle);
		idMap = FHIRUtil.getIdResourceMap(bundle);		
	}
	
	public IIdentifierMap<Resource> getIdentifierMap() {
		return identifierMap;
	}
	
	public void spotCheckAssignedPractitioner(String reference, String familyName, String roleCode, String organizationName) {
		Practitioner p = (Practitioner) idMap.get(reference);
		if (familyName == null) {
			Assert.assertFalse("Practioner has name", p.hasName());
		} else {
			Assert.assertEquals("The family name", familyName, p.getName().get(0).getFamily());
		}
		if (organizationName != null) {
			PractitionerRole role = BundleUtil.findPractitionersRole(bundle, p);
			String actualRoleCode = role.getCodeFirstRep().getCodingFirstRep().getCode();
			Assert.assertEquals("The role code", roleCode, actualRoleCode);				
			Assert.assertTrue("Role has a organization", role.hasOrganization());		
			String refOrg = role.getOrganization().getReference();
			Organization org = (Organization) idMap.get(refOrg);
			Assert.assertEquals("The organization name", organizationName, org.getName());
		} else {
			findPractitionersRoles(bundle, p, 0);
		}
	}
	
	public void spotCheckImmunizationPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		Immunization imm = (Immunization) identifierMap.get("Immunization", identifier);
		Assert.assertTrue("Immunization has a practitioner", imm.hasPractitioner());
		String ref = imm.getPractitioner().get(0).getActor().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}
		
	public void spotCheckEncounterPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		Encounter enc = (Encounter) identifierMap.get("Encounter", identifier);
		Assert.assertTrue("Encounter has a participant", enc.hasParticipant());
		String ref = enc.getParticipant().get(0).getIndividual().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}
		
	public void spotCheckProcedurePractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		Procedure proc = (Procedure) identifierMap.get("Procedure", identifier);
		Assert.assertTrue("Procedure has a performer", proc.hasPerformer());
		String ref = proc.getPerformer().get(0).getActor().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}

	public void spotCheckObservationPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		Observation obs = (Observation) identifierMap.get("Observation", identifier);
		Assert.assertTrue("Observation has a performer", obs.hasPerformer());
		String ref = obs.getPerformer().get(0).getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}

	public void spotCheckMedStatementPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		MedicationStatement obs = (MedicationStatement) identifierMap.get("MedicationStatement", identifier);
		Assert.assertTrue("Medication statement has an author", obs.hasInformationSource());
		String ref = obs.getInformationSource().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}
	
	public void spotCheckConditionPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		Condition cond = (Condition) identifierMap.get("Condition", identifier);
		Assert.assertTrue("Condition has an asserter", cond.hasAsserter());
		String ref = cond.getAsserter().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}
	
	public void spotCheckAllergyPractitioner(String identifier, String familyName, String roleCode, String organizationName) {
		AllergyIntolerance allergy = (AllergyIntolerance) identifierMap.get("AllergyIntolerance", identifier);
		Assert.assertTrue("Allergy has a recorder", allergy.hasRecorder());
		String ref = allergy.getRecorder().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);		
	}
	
	public void spotCheckPractitioner(String root, String extension, String familyName, String address) {
		Practitioner p = (Practitioner) identifierMap.get("Practitioner", root, extension);
		if (familyName == null) {
			Assert.assertFalse("Practioner has name", p.hasName());
		} else {
			Assert.assertEquals("The family name", familyName, p.getName().get(0).getFamily());
		}
		Assert.assertTrue("Practioner has address", p.hasAddress());
		Assert.assertEquals("The address line", address, p.getAddress().get(0).getLine().get(0).getValue());		
	}
	
	public void spotCheckAttesterPractitioner(Composition.CompositionAttestationMode mode, String familyName, String roleCode, String organizationName) {
		Composition composition = (Composition) bundle.getEntry().get(0).getResource();
		Optional<String> ref = composition.getAttester().stream().filter(a -> a.getMode().get(0).getValue().equals(mode)).map(a -> a.getParty().getReference()).findFirst();
		Assert.assertTrue("Attester exists", ref.isPresent());
		spotCheckAssignedPractitioner(ref.get(), familyName, roleCode, organizationName);				
	}
	
	public void spotCheckAuthorPractitioner(String familyName, String roleCode, String organizationName) {
		Composition composition = (Composition) bundle.getEntry().get(0).getResource();
		Assert.assertTrue("Has author", composition.hasAuthor());
		String ref = composition.getAuthor().get(0).getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);				
	}

	static public <T extends Resource> List<T> findResources(Bundle bundle, Class<T> type, int count) throws Exception {
		List<T> resources = FHIRUtil.findResources(bundle, type);
		String msg = String.format("Expect %d %s resources in the bundle", count, type.getSimpleName());
		Assert.assertEquals(msg, count, resources.size());
		return resources;
	}

	static public <T extends Resource> T findOneResource(Bundle bundle, Class<T> type) throws Exception {
		List<T> resources = findResources(bundle, type, 1);
		return resources.get(0);
	}

	public static Bundle generateSnippetBundle(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ClinicalDocument cda = CDAUtil.load(fis);
		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		Reference dummyPatientRef = new Reference(new IdType("Patient", "0"));
		ccdTransformer.setPatientRef(dummyPatientRef);
		Config.setGenerateDafProfileMetadata(false);
		Config.setGenerateNarrative(false); // TODO: Make this an argument to ccdTransformer
		Bundle bundle = ccdTransformer.transformDocument(cda, false);
		return bundle;
	}

	public static <T extends Resource> void printBundleResources(Bundle bundle, String sourceName, Class<T> cls)
			throws Exception {
		List<T> procedures = FHIRUtil.findResources(bundle, cls);
		String baseName = sourceName.substring(0, sourceName.length() - 4);
		String addlName = cls.getSimpleName();
		String outputName = String.format("src/test/resources/output/%s.%s.json", baseName, addlName);
		FHIRUtil.printJSON(procedures, outputName);
	}

	public static List<PractitionerRole> findPractitionersRoles(Bundle bundle, Practitioner practitioner, int count) {
		String id = practitioner.getId();
		List<PractitionerRole> roles = bundle.getEntry().stream().map(r -> r.getResource())
				.filter(r -> r instanceof PractitionerRole).map(r -> (PractitionerRole) r)
				.filter(r -> id.equals(r.getPractitioner().getReference())).collect(Collectors.toList());
		Assert.assertEquals("Practitioner role count", count, roles.size());
		return roles;
	}

	public static PractitionerRole findPractitionersRole(Bundle bundle, Practitioner practitioner) {
		List<PractitionerRole> roles = findPractitionersRoles(bundle, practitioner, 1);
		return roles.get(0);
	}
	
	public static void verifyIdsUnique(Bundle bundle) {
		Set<String> set = new HashSet<String>();
		for (BundleEntryComponent entry : bundle.getEntry()) {
			Resource resource = entry.getResource();
			String id = resource.getId();
			Assert.assertNotNull("Id is null for " + resource.getResourceType(), id);
			Assert.assertFalse("Previous resource with id: " + id, set.contains(id));
			set.add(id);
		}
	}
}
