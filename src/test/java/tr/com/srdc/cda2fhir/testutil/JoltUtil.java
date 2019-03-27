package tr.com.srdc.cda2fhir.testutil;

import java.util.Map;

import org.junit.Assert;

public class JoltUtil {
	@SuppressWarnings("unchecked")
	public static void checkReference(Map<String, Object> resource, String path, String id) {
		Map<String, Object> parent = (Map<String, Object>) resource.get(path);
		Assert.assertNotNull("Resource " + path, parent);
		String actualId = (String) parent.get("reference");
		Assert.assertEquals("Id for " + path, id, actualId);
	}
}
