package tr.com.srdc.cda2fhir.testutil;

import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.CCDTransformerImpl;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class BundleUtil {
	static public <T extends Resource> List<T> findResources(Bundle bundle, Class<T> type) throws Exception {
		return bundle.getEntry().stream().map(b -> b.getResource()).filter(r -> type.isInstance(r))
				.map(r -> type.cast(r)).collect(Collectors.toList());
	}

	static public <T extends Resource> List<T> findResources(Bundle bundle, Class<T> type, int count) throws Exception {
		List<T> resources = findResources(bundle, type);
		String msg = String.format("Expect %d %s resources in the bundle", count, type.getName());
		Assert.assertEquals(msg, count, resources.size());
		return resources;
	}

	static public <T extends Resource> T findOneResource(Bundle bundle, Class<T> type) throws Exception {
		List<T> resources = findResources(bundle, type, 1);
		return resources.get(0);
	}

	public static Bundle generateSnippetBundle(String sourceName) throws Exception {
		FileInputStream fis = new FileInputStream("src/test/resources/" + sourceName);
		ClinicalDocument cda = CDAUtil.load(fis);
		CCDTransformerImpl ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
		Reference dummyPatientRef = new Reference(new IdType("Patient", "0"));
		ccdTransformer.setPatientRef(dummyPatientRef);
		Config.setGenerateDafProfileMetadata(false);
		Config.setGenerateNarrative(false); // TODO: Make this an argument to ccdTransformer
		Bundle bundle = ccdTransformer.transformDocument(cda, false);
		return bundle;
	}
}
