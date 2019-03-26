package tr.com.srdc.cda2fhir.testutil.generator;

import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class IDGenerator {
	private static final String[] ROOTS = { "1.3.5.7", "4.5.2.4", "4.1.12.67", "43.45.78.12" };

	private static int NEXT_INDEX = 1;

	private String root;
	private String extension;

	public IDGenerator(String root, String extension) {
		this.root = root;
		this.extension = extension;
	}

	public II generate(CDAFactories factories) {
		if (extension == null) {
			return factories.datatype.createII(root);
		} else {
			return factories.datatype.createII(root, extension);
		}
	}

	public void verify(Identifier identifier) {
		if (root != null && extension != null) {
			Assert.assertEquals("Identifier system", "urn:oid:" + root, identifier.getSystem());
			Assert.assertEquals("Identifier value", extension, identifier.getValue());
		} else if (root != null) {
			Assert.assertEquals("Identifier value", root, identifier.getValue());
		} else {
			Assert.assertNull("No identifier", identifier);
		}
	}

	public static IDGenerator getNextInstance() {
		int rootIndex = NEXT_INDEX % 4;
		++NEXT_INDEX;

		String root = ROOTS[rootIndex];
		String extension = String.valueOf(1000 + rootIndex);

		return new IDGenerator(root, extension);
	}
}
