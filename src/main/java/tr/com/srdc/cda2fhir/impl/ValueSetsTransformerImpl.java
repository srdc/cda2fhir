package tr.com.srdc.cda2fhir.impl;

import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;

import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;

public class ValueSetsTransformerImpl implements ValueSetsTransformer {
	
	public NameUseEnum EntityNameUse2NameUseEnum(EntityNameUse entityNameUse){
		
		switch(entityNameUse){
		case C: return NameUseEnum.USUAL;
		// TODO: Visit https://www.hl7.org/fhir/valueset-name-use.html
		// Trying: case OR: return NameUseEnum.OFFICIAL;
		// .. T, ANON, OLD, M.
		// However, these cases don't exist
		case P: return NameUseEnum.NICKNAME;
		default: return NameUseEnum.TEMP;
		}
	}

}
