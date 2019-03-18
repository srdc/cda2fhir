package tr.com.srdc.cda2fhir;

import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceEntityRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentType;
import org.junit.Assert;
import org.junit.Test;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class ProvenanceTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	@Test
	public void testProvenance() throws Exception {
		Bundle testBundle = new Bundle();
		IdType orgId = (new IdType("Organization/1"));
		IdType medId = (new IdType("Medication/1"));
		IdType patientId = (new IdType("Patient/1"));

		testBundle.addEntry(new BundleEntryComponent().setResource(new Organization().setIdElement(orgId)));
		testBundle.addEntry(new BundleEntryComponent().setResource(new Medication().setIdElement(medId)));
		testBundle.addEntry(new BundleEntryComponent().setResource(new Patient().setIdElement(patientId)));

		Identifier assemblerDevice = new Identifier();
		assemblerDevice.setValue("Higgs");
		assemblerDevice.setSystem("http://www.amida.com");

		String documentBody = "<ClinicalDoc>Meowmeowmeowmeow</ClinicalDoc>";
		testBundle = rt.tProvenance(testBundle, documentBody, assemblerDevice);

		// Verifies bundle contains the initial resources.
		BundleUtil.findOneResource(testBundle, Organization.class);
		BundleUtil.findOneResource(testBundle, Medication.class);
		BundleUtil.findOneResource(testBundle, Patient.class);

		Binary binary = BundleUtil.findOneResource(testBundle, Binary.class);
		Assert.assertEquals(binary.getContentType(), "text/plain");

		Assert.assertEquals(binary.getContentElement().asStringValue(), documentBody);

		Device device = BundleUtil.findOneResource(testBundle, Device.class);
		Assert.assertEquals(device.getText().getStatusAsString().toLowerCase(),
				NarrativeStatus.GENERATED.toString().toLowerCase());
		Assert.assertEquals(device.getIdentifierFirstRep().getSystem().toLowerCase(),
				assemblerDevice.getSystem().toLowerCase());
		Assert.assertEquals(device.getIdentifierFirstRep().getValue().toLowerCase(),
				assemblerDevice.getValue().toLowerCase());

		Provenance provenance = BundleUtil.findOneResource(testBundle, Provenance.class);
		Assert.assertEquals(provenance.getTarget().get(0).getReference(), orgId.getValue());
		Assert.assertEquals(provenance.getTarget().get(1).getReference(), medId.getValue());
		Assert.assertEquals(provenance.getTarget().get(2).getReference(), patientId.getValue());
		Assert.assertEquals(provenance.getTarget().get(3).getReference().substring(0, 6), "Binary");
		Assert.assertEquals(provenance.getTarget().get(4).getReference().substring(0, 6), "Device");

		Coding roleDevice = provenance.getAgentFirstRep().getRelatedAgentType().getCodingFirstRep();
		Assert.assertEquals(roleDevice.getId().substring(0, 6), "Device");
		Assert.assertEquals(roleDevice.getSystem(), ProvenanceAgentType.DEVICE.getSystem());
		Assert.assertEquals(roleDevice.getCode(), ProvenanceAgentType.DEVICE.toCode());
		Assert.assertEquals(roleDevice.getDisplay(), ProvenanceAgentType.DEVICE.getDisplay());

		Coding roleAssembler = provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep();
		Assert.assertEquals(roleAssembler.getId().substring(0, 6), "Device");
		Assert.assertEquals(roleAssembler.getSystem(), ProvenanceAgentRole.ASSEMBLER.getSystem());
		Assert.assertEquals(roleAssembler.getCode(), ProvenanceAgentRole.ASSEMBLER.toCode());
		Assert.assertEquals(roleAssembler.getDisplay(), ProvenanceAgentRole.ASSEMBLER.getDisplay());

		Assert.assertEquals(provenance.getAgentFirstRep().getWhoReference().getReference().substring(0, 6), "Device");

		Assert.assertEquals(provenance.getEntityFirstRep().getId().substring(0, 6), "Binary");
		Assert.assertEquals(provenance.getEntityFirstRep().getRole(), ProvenanceEntityRole.SOURCE);
	}
}
