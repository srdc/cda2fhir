package tr.com.srdc.cda2fhir.testutil;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;

public class BundleUtil {
	static public <T extends Resource> T findOneResource(Bundle bundle, Class<T> type) throws Exception {
    	List<T> resources = bundle.getEntry().stream()
    			.map(b -> b.getResource())
    			.filter(r -> type.isInstance(r))
    			.map(r -> type.cast(r))
				.collect(Collectors.toList());
    	String msg = String.format("Multiple %s resources in the bundle", type.getName());
    	Assert.assertEquals(msg,  1, resources.size());
    	return resources.get(0);	
	}
}
