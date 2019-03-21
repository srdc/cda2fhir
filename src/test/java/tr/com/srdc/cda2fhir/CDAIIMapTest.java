package tr.com.srdc.cda2fhir;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class CDAIIMapTest {
	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	private static List<II> generateIIList(String root, String extension) {
		II ii = factories.datatype.createII(root, extension);
		List<II> list = new ArrayList<II>();
		list.add(ii);
		return list;
	}

	@Test
	public void testBasic() {
		CDAIIMap<String> testObject = new CDAIIMap<String>();

		List<II> lid1 = generateIIList("2.1.4.5", "3344");
		List<II> lid2 = generateIIList("2.2.4.5", "3344");
		List<II> lid3 = generateIIList("2.2.6.5", "3355");

		testObject.put(lid1, "value1");
		Assert.assertEquals("The first value", "value1", testObject.get(lid1));
		testObject.put(lid2, "value2");
		Assert.assertEquals("The second value", "value2", testObject.get(lid2));
		Assert.assertEquals("The first value", "value1", testObject.get(lid1));
		testObject.put(lid3, "value3");
		Assert.assertEquals("The second value", "value2", testObject.get(lid2));
		Assert.assertEquals("The first value", "value1", testObject.get(lid1));
		Assert.assertEquals("The third value", "value3", testObject.get(lid3));

		CDAIIMap<String> testObject2 = new CDAIIMap<String>();
		List<II> lid21 = generateIIList("2.1.4.5", "4444");
		List<II> lid22 = generateIIList("2.2.4.5", "4544");
		List<II> lid23 = generateIIList("2.2.7.5", "5555");
		testObject2.put(lid21, "value21");
		testObject2.put(lid22, "value22");
		testObject2.put(lid23, "value23");

		testObject.put(testObject2);
		Assert.assertEquals("The first value", "value1", testObject.get(lid1));
		Assert.assertEquals("The second value", "value2", testObject.get(lid2));
		Assert.assertEquals("The third value", "value3", testObject.get(lid3));
		Assert.assertEquals("The fourth value", "value21", testObject.get(lid21));
		Assert.assertEquals("The fifth value", "value22", testObject.get(lid22));
		Assert.assertEquals("The sixth value", "value23", testObject.get(lid23));
	}
}
