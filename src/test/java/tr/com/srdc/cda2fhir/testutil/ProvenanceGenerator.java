package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.dstu3.model.codesystems.ProvenanceAgentType;

public class ProvenanceGenerator {
	public Provenance generate(Organization organization) {
		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		pac.setId(organization.getId());

		Coding agentTypeCoding = new Coding(ProvenanceAgentType.ORGANIZATION.getSystem(),
				ProvenanceAgentType.ORGANIZATION.toCode(), ProvenanceAgentType.ORGANIZATION.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentTypeCoding));

		Coding agentRoleCoding = new Coding(ProvenanceAgentRole.ASSEMBLER.getSystem(),
				ProvenanceAgentRole.ASSEMBLER.toCode(), ProvenanceAgentRole.ASSEMBLER.getDisplay());
		pac.addRole(new CodeableConcept().addCoding(agentRoleCoding));

		Provenance provenance = new Provenance();
		provenance.addAgent(pac);
		provenance.addTarget(new Reference(organization));
		return provenance;
	}
}