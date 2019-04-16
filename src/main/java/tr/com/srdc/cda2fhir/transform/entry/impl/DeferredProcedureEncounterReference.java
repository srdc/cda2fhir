package tr.com.srdc.cda2fhir.transform.entry.impl;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;

import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;

public class DeferredProcedureEncounterReference implements IDeferredReference {
	private Procedure procedure;
	private Identifier identifier;

	public DeferredProcedureEncounterReference(Procedure procedure, Identifier identifier) {
		this.procedure = procedure;
		this.identifier = identifier;
	}

	@Override
	public String getFhirType() {
		return "Encounter";
	}

	@Override
	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public Resource getResource() {
		return procedure;
	}

	@Override
	public void resolve(Reference reference) {
		procedure.setContext(reference);
	}
}
