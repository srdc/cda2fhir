package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Property;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.exceptions.FHIRException;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class IdentifierMapFactory {
	public static IIdentifierMap<String> bundleToIds(Bundle bundle) {
        IdentifierMap<String> identifierMap = new IdentifierMap<String>();
		for (BundleEntryComponent entry : bundle.getEntry()) {
			Resource resource = entry.getResource();
			Property property = resource.getNamedProperty("identifier");
			if (property != null) {
				List<Base> bases = property.getValues();
				if (!bases.isEmpty()) {
					for (Base base: bases) {
						try {
							Identifier identifier = resource.castToIdentifier(base);
							String fhirType = resource.fhirType();
							identifierMap.put(fhirType, identifier, resource.getId());
						} catch (FHIRException e) {}
					}
				}
			}
    	}
		return identifierMap;
	}
}
