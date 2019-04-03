package tr.com.srdc.cda2fhir.transform.util;

import java.util.List;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Property;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;

import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class IdentifierMapFactory {
	interface ResourceInfo<T> {
		T get(Resource resource);
	}

	public static <T> IIdentifierMap<T> bundleToResourceInfo(Bundle bundle, ResourceInfo<T> resourceInfo) {
		IdentifierMap<T> identifierMap = new IdentifierMap<T>();
		for (BundleEntryComponent entry : bundle.getEntry()) {
			Resource resource = entry.getResource();
			Property property = resource.getNamedProperty("identifier");
			if (property != null) {
				List<Base> bases = property.getValues();
				if (!bases.isEmpty()) {
					for (Base base : bases) {
						try {
							Identifier identifier = resource.castToIdentifier(base);
							String fhirType = resource.fhirType();
							T info = resourceInfo.get(resource);
							if (info != null) {
								identifierMap.put(fhirType, identifier, info);
							}
						} catch (FHIRException e) {
						}
					}
				}
			}
		}
		return identifierMap;
	}

	public static <T> IIdentifierMap<T> resourcesToResourceInfo(List<? extends Resource> resources,
			ResourceInfo<T> resourceInfo) {
		IdentifierMap<T> identifierMap = new IdentifierMap<T>();
		for (Resource resource : resources) {
			Property property = resource.getNamedProperty("identifier");
			if (property != null) {
				List<Base> bases = property.getValues();
				if (!bases.isEmpty()) {
					for (Base base : bases) {
						try {
							Identifier identifier = resource.castToIdentifier(base);
							String fhirType = resource.fhirType();
							T info = resourceInfo.get(resource);
							if (info != null) {
								identifierMap.put(fhirType, identifier, info);
							}
						} catch (FHIRException e) {
						}
					}
				}
			}
		}
		return identifierMap;
	}

	public static IIdentifierMap<String> bundleToIds(Bundle bundle) {
		return bundleToResourceInfo(bundle, r -> r.getId());
	}

	public static IIdentifierMap<Resource> bundleToResource(Bundle bundle) {
		return bundleToResourceInfo(bundle, r -> r);
	}
}
