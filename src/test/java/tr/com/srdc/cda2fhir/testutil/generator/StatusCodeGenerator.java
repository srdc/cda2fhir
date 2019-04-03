package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.Map;

import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public class StatusCodeGenerator {
	private String statusCode;
	private String statusCodeNullFlavor;

	private Map<String, Object> map;
	private String defaultCode;

	public StatusCodeGenerator(Map<String, Object> map, String defaultCode) {
		this.map = map;
		this.defaultCode = defaultCode;
	}

	public StatusCodeGenerator(Map<String, Object> map) {
		this.map = map;
	}

	public void set(String statusCode) {
		this.statusCode = statusCode;
	}

	public CS generate(CDAFactories factories) {
		CS cs = factories.datatype.createCS();
		if (statusCode != null || statusCodeNullFlavor != null) {
			if (statusCode != null) {
				cs.setCode(statusCode);
			}
			if (statusCodeNullFlavor != null) {
				NullFlavor nf = CDAUtilExtension.toNullFlavor(statusCodeNullFlavor);
				cs.setNullFlavor(nf);
			}
		}
		return cs;
	}

	public void verify(String code) {
		if (statusCode == null || statusCodeNullFlavor != null) {
			Assert.assertNull("No status code", code);
		} else {
			String expected = (String) map.get(statusCode);
			if (expected == null) {
				expected = defaultCode;
			}
			Assert.assertEquals("Status code", expected, code);
		}

	}
}
