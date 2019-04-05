package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public abstract class CodeGenerator<T extends CE> {
	private String code;
	private String nullFlavor;

	private Map<String, Object> map;
	private String defaultCode;

	public CodeGenerator(String code) {
		this.code = code;
	}

	public CodeGenerator(Map<String, Object> map, String defaultCode) {
		this.map = map;
		this.defaultCode = defaultCode;
	}

	public CodeGenerator(Map<String, Object> map) {
		this.map = map;
	}

	public void set(String code) {
		this.code = code;
	}

	protected abstract T create(CDAFactories factories);

	public T generate(CDAFactories factories) {
		T result = create(factories);
		if (code != null || nullFlavor != null) {
			if (code != null) {
				result.setCode(code);
			}
			if (nullFlavor != null) {
				NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
				result.setNullFlavor(nf);
			}
		}
		return result;
	}

	public void verify(String code) {
		if (this.code == null || nullFlavor != null) {
			Assert.assertNull("No code", code);
		} else {
			String expected = map != null ? (String) map.get(this.code) : code;
			if (expected == null) {
				expected = defaultCode;
			}
			Assert.assertEquals("Code", expected, code);
		}

	}

	@SuppressWarnings("unchecked")
	public void verify(CodeableConcept codeableConcept) {
		if (this.code == null || nullFlavor != null) {
			Assert.assertNull("No code", code);
		} else {
			List<Object> expectedParent = (List<Object>) map.get(this.code);
			if (expectedParent == null) {
				expectedParent = (List<Object>) map.get("_");
			}
			Map<String, Object> expected = (Map<String, Object>) expectedParent.get(0);

			Coding coding = codeableConcept.getCoding().get(0);
			Assert.assertEquals("Code", expected.get("code"), coding.getCode());
			Assert.assertEquals("System", expected.get("system"), coding.getSystem());
			Assert.assertEquals("Display", expected.get("display"), coding.getDisplay());
		}
	}
}
