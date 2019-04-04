package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public abstract class ObservationGenerator {
	private static final Map<String, Object> OBSERVATION_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ObservationStatus.json");
	private static final Map<String, Object> OBSERVATION_INTERPRETATION = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ObservationInterpretation.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private CDGenerator codeGenerator;

	private StatusCodeGenerator statusCodeGenerator;

	private IVL_TSPeriodGenerator effectiveTimeGenerator;

	private List<CDGenerator> targetSiteCodeGenerators = new ArrayList<>();

	private List<AnyGenerator> valueGenerators = new ArrayList<>();

	private List<AuthorGenerator> authorGenerators = new ArrayList<>();

	private List<CEGenerator> methodCodeGenerators = new ArrayList<>();

	private List<String> interpretationCodeGenerators = new ArrayList<>();

	private List<ReferenceRangeGenerator> referenceRangeGenerators = new ArrayList<>();

	public void replaceValueGenerator(PQGenerator pqGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(pqGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(STGenerator stGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(stGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(IVL_PQRangeGenerator ivlPqRangeGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(ivlPqRangeGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(RTOGenerator rtoGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(rtoGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(EDGenerator edGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(edGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(TSGenerator tsGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(tsGenerator);
		valueGenerators.add(ag);
	}

	public void replaceValueGenerator(BLGenerator blGenerator) {
		valueGenerators.clear();
		AnyGenerator ag = new AnyGenerator(blGenerator);
		valueGenerators.add(ag);
	}

	public abstract Observation createForGenerate(CDAFactories factories);

	public Observation generate(CDAFactories factories) {
		Observation obs = createForGenerate(factories);

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			obs.getIds().add(ii);
		});

		if (codeGenerator != null) {
			CD code = codeGenerator.generate(factories);
			obs.setCode(code);
		}

		if (statusCodeGenerator != null) {
			CS cs = statusCodeGenerator.generate(factories);
			obs.setStatusCode(cs);
		}

		if (effectiveTimeGenerator != null) {
			IVL_TS ivlTs = effectiveTimeGenerator.generate(factories);
			obs.setEffectiveTime(ivlTs);
		}

		targetSiteCodeGenerators.forEach(tscg -> {
			CD cd = tscg.generate(factories);
			obs.getTargetSiteCodes().add(cd);
		});

		valueGenerators.forEach(vg -> {
			ANY any = vg.generate(factories);
			obs.getValues().add(any);
		});

		authorGenerators.forEach(ag -> {
			Author author = ag.generate(factories);
			obs.getAuthors().add(author);
		});

		methodCodeGenerators.forEach(mcg -> {
			CE ce = mcg.generate(factories);
			obs.getMethodCodes().add(ce);
		});

		interpretationCodeGenerators.forEach(code -> {
			CE ce = factories.datatype.createCE();
			ce.setCode(code);
			obs.getInterpretationCodes().add(ce);
		});

		referenceRangeGenerators.forEach(rrg -> {
			ReferenceRange rr = rrg.generate(factories);
			obs.getReferenceRanges().add(rr);
		});

		return obs;
	}

	public static void fillDefaultInstance(ObservationGenerator obs) {
		obs.idGenerators.add(IDGenerator.getNextInstance());
		obs.codeGenerator = CDGenerator.getNextInstance();
		obs.statusCodeGenerator = new StatusCodeGenerator(OBSERVATION_STATUS, "unknown");
		obs.statusCodeGenerator.set("active");
		obs.effectiveTimeGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		obs.targetSiteCodeGenerators.add(CDGenerator.getNextInstance());
		obs.valueGenerators.add(new AnyGenerator(CDGenerator.getNextInstance()));

		AuthorGenerator authorGenerator = AuthorGenerator.getDefaultInstance();
		authorGenerator.setTimeGenerator(new TSGenerator("20170122103500-0400"));
		obs.authorGenerators.add(authorGenerator);

		obs.methodCodeGenerators.add(CEGenerator.getNextInstance());
		obs.interpretationCodeGenerators.add("CAR");
		obs.referenceRangeGenerators.add(ReferenceRangeGenerator.getDefaultInstance());
	}

	@SuppressWarnings("unchecked")
	public void verify(org.hl7.fhir.dstu3.model.Observation observation) {
		if (idGenerators.isEmpty()) {
			Assert.assertTrue("No observation identifier", !observation.hasIdentifier());
		} else {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(observation.getIdentifier().get(index));
			}
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No observation status", !observation.hasCode());
		} else {
			codeGenerator.verify(observation.getCode());
		}

		if (statusCodeGenerator == null) {
			Assert.assertTrue("No observation status", !observation.hasStatus());
		} else {
			statusCodeGenerator.verify(observation.getStatus().toCode());
		}

		if (effectiveTimeGenerator == null) {
			Assert.assertTrue("No observation effective period", !observation.hasEffectivePeriod());
		} else {
			effectiveTimeGenerator.verify(observation.getEffectivePeriod());
		}

		if (targetSiteCodeGenerators.isEmpty()) {
			Assert.assertTrue("No observation target site code", !observation.hasBodySite());
		} else {
			CDGenerator cdg = targetSiteCodeGenerators.get(targetSiteCodeGenerators.size() - 1);
			cdg.verify(observation.getBodySite());
		}

		if (valueGenerators.isEmpty()) {
			Assert.assertTrue("No observation value", !observation.hasValue());
		} else {
			AnyGenerator ag = valueGenerators.get(valueGenerators.size() - 1);
			if (observation.hasValueCodeableConcept()) {
				ag.verify(observation.getValueCodeableConcept());
			} else if (observation.hasValueQuantity()) {
				ag.verify(observation.getValueQuantity());
			} else if (observation.hasValueStringType()) {
				ag.verify(observation.getValueStringType().getValueAsString());
			} else if (observation.hasValueRange()) {
				ag.verify(observation.getValueRange());
			} else if (observation.hasValueRatio()) {
				ag.verify(observation.getValueRatio());
			} else if (observation.hasValueAttachment()) {
				ag.verify(observation.getValueAttachment());
			} else if (observation.hasValueDateTimeType()) {
				ag.verify(observation.getValueDateTimeType());
			} else if (observation.hasValueBooleanType()) {
				ag.verify(observation.getValueBooleanType().booleanValue());
			} else {
				throw new TestSetupException("Invalid observation value");
			}
		}

		if (methodCodeGenerators.isEmpty()) {
			Assert.assertTrue("No observation method", !observation.hasMethod());
		} else {
			CEGenerator ceg = methodCodeGenerators.get(methodCodeGenerators.size() - 1);
			ceg.verify(observation.getMethod());
		}

		if (authorGenerators.isEmpty()) {
			Assert.assertTrue("No issued element", !observation.hasIssuedElement());
		} else {
			AuthorGenerator ag = authorGenerators.get(authorGenerators.size() - 1);
			TSGenerator timeGenerator = ag.getTimeGenerator();
			timeGenerator.verify(observation.getIssuedElement().asStringValue());
		}

		if (interpretationCodeGenerators.isEmpty()) {
			Assert.assertTrue("No interpretation", !observation.hasInterpretation());
		} else {
			String input = interpretationCodeGenerators.get(interpretationCodeGenerators.size() - 1);
			String expected = (String) ((Map<String, Object>) OBSERVATION_INTERPRETATION.get(input)).get("code");
			String actual = observation.getInterpretation().getCoding().get(0).getCode();
			Assert.assertEquals("Observation interpretation", expected, actual);
		}

		if (referenceRangeGenerators.isEmpty()) {
			Assert.assertTrue("No reference range", !observation.hasReferenceRange());
		} else {
			int count = referenceRangeGenerators.size();
			Assert.assertEquals("Reference range count", count, observation.getReferenceRange().size());
			for (int index = 0; index < count; ++index) {
				referenceRangeGenerators.get(index).verify(observation.getReferenceRange().get(index));
			}
		}
	}

	public void verify(Bundle bundle) throws Exception {
		org.hl7.fhir.dstu3.model.Observation obs = BundleUtil.findOneResource(bundle,
				org.hl7.fhir.dstu3.model.Observation.class);
		verify(obs);

		if (authorGenerators.isEmpty()) {
			Assert.assertTrue("No practitioner", !obs.hasPerformer());
		} else {
			Assert.assertEquals("Performer count", authorGenerators.size(), obs.getPerformer().size());
			for (int index = 0; index < authorGenerators.size(); ++index) {
				String practitionerId = obs.getPerformer().get(index).getReference();
				AuthorGenerator ag = authorGenerators.get(index);
				ag.verifyFromPractionerId(bundle, practitionerId);
			}
		}
	}
}
