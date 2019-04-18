package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.Map;

import org.openhealthtools.mdht.uml.hl7.datatypes.CS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CSCodeGenerator extends CodeGenerator<CS> {
	public CSCodeGenerator(String code) {
		super(code);
	}

	public CSCodeGenerator(Map<String, Object> map, String defaultCode) {
		super(map, defaultCode);
	}

	public CSCodeGenerator(Map<String, Object> map) {
		super(map);
	}

	@Override
	protected CS create(CDAFactories factories) {
		return factories.datatype.createCS();
	}

	public static CSCodeGenerator getInstanceWithValue(Map<String, Object> map, String value) {
		CSCodeGenerator result = new CSCodeGenerator(map);
		result.set(value);
		return result;
	}
}
