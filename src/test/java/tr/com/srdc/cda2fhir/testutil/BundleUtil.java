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
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
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
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
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

	public Bundle getBundle() {
		return bundle;
	}

	public Resource getFromJSONArray(String fhirType, List<Object> identifiers) {
		return identifierMap.getFromJSONArray(fhirType, identifiers);
	}

	public IIdentifierMap<Resource> getIdentifierMap() {
		return identifierMap;
	}

	public <T extends Resource> T getResourceFromReference(String reference, Class<T> type) {
		Resource resource = idMap.get(reference);
		if (resource == null) {
			return null;
		}
		return type.cast(resource);
	}

	public PractitionerRole getPractitionerRole(String practitionerId) {
		List<PractitionerRole> roles = FHIRUtil.findResources(bundle, PractitionerRole.class);
		Optional<PractitionerRole> result = roles.stream().filter(role -> {
			if (!role.hasOrganization() || !role.hasPractitioner()) {
				return false;
			}
			String localPractitionerId = role.getPractitioner().getReference();
			return practitionerId.equals(localPractitionerId);
		}).findFirst();
		return result.orElse(null);
	}

	public void spotCheckAssignedPractitioner(String reference, String familyName, String roleCode,
			String organizationName) {
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

	public <T extends Resource> void checkResourceCount(Class<T> type, int count) throws Exception {
		List<T> resources = FHIRUtil.findResources(bundle, type);
		String msg = String.format("Expect %d %s resources in the bundle", count, type.getSimpleName());
		Assert.assertEquals(msg, count, resources.size());
	}

	public List<Resource> getSectionResources(String sectionCode) {
		Composition composition = FHIRUtil.findFirstResource(bundle, Composition.class);
		List<String> references = composition.getSection().stream().filter(r -> {
			String code = r.getCode().getCodingFirstRep().getCode();
			return code.equals(sectionCode);
		}).flatMap(r -> r.getEntry().stream()).map(r -> r.getReference()).collect(Collectors.toList());
		return references.stream().map(r -> {
			return idMap.get(r);
		}).filter(r -> r != null).collect(Collectors.toList());
	}

	public void spotCheckImmunizationPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		Immunization imm = (Immunization) identifierMap.get("Immunization", identifier);
		Assert.assertTrue("Immunization has a practitioner", imm.hasPractitioner());
		String ref = imm.getPractitioner().get(0).getActor().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckEncounterPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		Encounter enc = (Encounter) identifierMap.get("Encounter", identifier);
		Assert.assertTrue("Encounter has a participant", enc.hasParticipant());
		String ref = enc.getParticipant().get(0).getIndividual().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckProcedurePractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		Procedure proc = (Procedure) identifierMap.get("Procedure", identifier);
		Assert.assertTrue("Procedure has a performer", proc.hasPerformer());
		String ref = proc.getPerformer().get(0).getActor().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckObservationPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		Observation obs = (Observation) identifierMap.get("Observation", identifier);
		Assert.assertTrue("Observation has a performer", obs.hasPerformer());
		String ref = obs.getPerformer().get(0).getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckMedStatementPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		MedicationStatement obs = (MedicationStatement) identifierMap.get("MedicationStatement", identifier);
		Assert.assertTrue("Medication statement has an author", obs.hasInformationSource());
		String ref = obs.getInformationSource().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckConditionPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
		Condition cond = (Condition) identifierMap.get("Condition", identifier);
		Assert.assertTrue("Condition has an asserter", cond.hasAsserter());
		String ref = cond.getAsserter().getReference();
		spotCheckAssignedPractitioner(ref, familyName, roleCode, organizationName);
	}

	public void spotCheckAllergyPractitioner(String identifier, String familyName, String roleCode,
			String organizationName) {
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

	public void spotCheckAttesterPractitioner(Composition.CompositionAttestationMode mode, String familyName,
			String roleCode, String organizationName) {
		Composition composition = (Composition) bundle.getEntry().get(0).getResource();
		Optional<String> ref = composition.getAttester().stream()
				.filter(a -> a.getMode().get(0).getValue().equals(mode)).map(a -> a.getParty().getReference())
				.findFirst();
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
		if (!resources.isEmpty())
			return resources.get(0);
		else
			return null;
	}

	private static Bundle generateBundle(String sourceName, boolean includeComposition) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());
		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		Reference dummyPatientRef = new Reference(new IdType("Patient", "0"));
		ccdTransformer.setPatientRef(dummyPatientRef);
		Config.setGenerateDafProfileMetadata(false);
		Config.setGenerateNarrative(false); // TODO: Make this an argument to ccdTransformer
		Bundle bundle = ccdTransformer.transformDocument(cda, includeComposition);
		return bundle;
	}

	public static Bundle generateSnippetBundle(String sourceName) throws Exception {
		return generateBundle(sourceName, false);
	}

	public static BundleUtil getInstance(String sourceName) throws Exception {
		Bundle bundle = generateBundle(sourceName, true);
		return new BundleUtil(bundle);
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
