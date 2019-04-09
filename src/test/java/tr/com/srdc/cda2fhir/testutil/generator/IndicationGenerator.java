package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class IndicationGenerator {

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private String code;
	private String constCodeCode;
	private String constCodeDisplay;
	private String constCodeSystem;

	private List<CDGenerator> valueGenerators = new ArrayList<>();

	private EffectiveTimeGenerator effectiveTimeGenerator;

	private String statusCode;

	public Indication generate(CDAFactories factories) {
		Indication indication = factories.consol.createIndication();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			indication.getIds().add(ii);
		});

		if (code != null && constCodeCode != null) {
			CD cd = factories.datatype.createCD();
			cd.setCode(code);
			indication.setCode(cd);
		}

		valueGenerators.forEach(vg -> {
			CD value = vg.generate(factories);
			indication.getValues().add(value);
		});

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			indication.setEffectiveTime(ivlTs);
		}

		if (statusCode != null) {
			CS cs = factories.datatype.createCS(statusCode);
			indication.setStatusCode(cs);
		}

		return indication;
	}

	public void setConstantCode(String constCodeCode, String constCodeDisplay, String constCodeSystem) {
		this.constCodeCode = constCodeCode;
		this.constCodeDisplay = constCodeDisplay;
		this.constCodeSystem = constCodeSystem;
	}

	public static IndicationGenerator getDefaultInstance() {
		IndicationGenerator indication = new IndicationGenerator();

		indication.idGenerators.add(IDGenerator.getNextInstance());
		indication.code = "75321-0";
		indication.valueGenerators.add(CDGenerator.getNextInstance());
		indication.effectiveTimeGenerator = new EffectiveTimeGenerator("20171008");
		indication.statusCode = "active";

		return indication;
	}

	public void verify(Condition condition) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(condition.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !condition.hasIdentifier());
		}

		if (code != null && constCodeCode == null) {
			Assert.assertEquals("Condition category count", 1, condition.getCategory().size());
			// throw new NotImplementedException("NOt yet implemented");
		} else if (constCodeCode != null) {
			Assert.assertEquals("Condition category count", 1, condition.getCategory().size());
			Coding actual = condition.getCategory().get(0).getCoding().get(0);
			Assert.assertEquals("Condition category code", constCodeCode, actual.getCode());
			Assert.assertEquals("Condition category system", constCodeSystem, actual.getSystem());
			Assert.assertEquals("Condition category display", constCodeDisplay, actual.getDisplay());
		} else {
			Assert.assertTrue("No condition category", !condition.hasCategory());
		}

		if (valueGenerators.isEmpty()) {
			Assert.assertTrue("No condition code", !condition.hasCode());
		} else {
			CDGenerator valueGenerator = valueGenerators.get(valueGenerators.size() - 1);
			valueGenerator.verify(condition.getCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No condition onset", !condition.hasOnset());
		} else {
			String value = effectiveTimeGenerator.getLowOrValue();
			if (value == null) {
				Assert.assertTrue("No condition onset", !condition.hasOnset());
			} else {
				String actual = FHIRUtil.toCDADatetime(condition.getOnsetDateTimeType().asStringValue());
				Assert.assertEquals("Condition onset", value, actual);
			}
		}

	}
}
