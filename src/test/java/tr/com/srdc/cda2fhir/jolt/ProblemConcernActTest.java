package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.generator.ProblemConcernActGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ProblemConcernActTest {
	private static CDAFactories factories;
	private static ResourceTransformerImpl rt;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/ProblemConcernAct/";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		rt = new ResourceTransformerImpl();
	}

	private static void reorderActObservations(ProblemConcernAct act, List<Condition> conditions) {
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		IdentifierMap<Integer> orderMap = IdentifierMapFactory.resourcesToOrder(conditions);
		List<ProblemObservation> observations = new ArrayList<>();
		conditions.forEach(r -> observations.add(null));
		act.getProblemObservations().forEach(r -> {
			II ii = r.getIds().get(0);
			String root = ii.getRoot();
			String extension = ii.getExtension();
			if (extension == null) {
				Integer index = orderMap.get("Condition", root);
				observations.set(index.intValue(), r);
			} else {
				String value = extension;
				String system = vst.tOid2Url(root);
				Integer index = orderMap.get("Condition", system, value);
				observations.set(index.intValue(), r);
			}
		});

		act.getEntryRelationships().clear();
		observations.forEach(po -> {
			EntryRelationship er = factories.base.createEntryRelationship();
			act.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			er.setObservation(po);
		});
	}

	private static void runTest(ProblemConcernAct pca, String caseName, ProblemConcernActGenerator generator)
			throws Exception {
		Config.setGenerateNarrative(false);
		Config.setGenerateDafProfileMetadata(false);

		IEntryResult cda2FhirResult = rt.tProblemConcernAct2Condition(pca, new BundleInfo(rt));

		Bundle bundle = cda2FhirResult.getBundle();
		Assert.assertNotNull("Problem bundle", bundle);
		List<Condition> conditions = FHIRUtil.findResources(bundle, Condition.class);
		for (int index = 0; index < conditions.size(); ++index) {
			Condition condition = conditions.get(index);
			String filepath = String.format("%s%s%s%s%s", OUTPUT_PATH, caseName, "CDA2FHIRCondition",
					index == 0 ? "" : index, "json");
			FHIRUtil.printJSON(condition, filepath);
		}
		if (generator != null) {
			generator.verify(bundle);
		}

		// CDAUtil reorders randomly, follow its order for easy comparison
		reorderActObservations(pca, conditions);
		File xmlFile = CDAUtilExtension.writeAsXML(pca, OUTPUT_PATH, caseName);

		List<Object> joltResult = JoltUtil.findJoltResult(xmlFile, "ProblemConcernAct", caseName);
		JoltUtil joltUtil = new JoltUtil(joltResult, bundle, caseName, OUTPUT_PATH);

		List<Map<String, Object>> joltConditions = TransformManager.chooseResources(joltResult, "Condition");
		if (conditions.isEmpty()) {
			Assert.assertTrue("No conditions", joltConditions.isEmpty());
		} else {
			for (int index = 0; index < conditions.size(); ++index) {
				Condition condition = conditions.get(index);
				joltUtil.verify(condition, joltConditions.get(index));
			}
		}
	}

	private static void runTest(ProblemConcernActGenerator generator, String caseName) throws Exception {
		ProblemConcernAct pca = generator.generate(factories);
		runTest(pca, caseName, generator);
	}

	@Test
	public void testDefault() throws Exception {
		ProblemConcernActGenerator generator = ProblemConcernActGenerator.getDefaultInstance();
		runTest(generator, "defaultCase");
	}

	@Test
	public void testFull() throws Exception {
		ProblemConcernActGenerator generator = ProblemConcernActGenerator.getFullInstance();
		runTest(generator, "fullCase");
	}
}
