package tr.com.srdc.cda2fhir;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;

import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;

public interface ValueSetsTransformer {
	NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse);
}
