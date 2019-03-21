package tr.com.srdc.cda2fhir.jolt;

import java.io.File;
import java.io.FileWriter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.PerformerGenerator;

public class AuthorParticipationTest {
	private static CDAFactories factories;

	private static final String OUTPUT_PATH = "src/test/resources/output/jolt/author-participation/";
	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void test() throws Exception {
		PerformerGenerator g = new PerformerGenerator();
		Performer2 performer = g.generate(factories);
		File resultFile = new File(OUTPUT_PATH + "performer.xml");
		FileWriter fw = new FileWriter(resultFile);
		resultFile.getParentFile().mkdirs();
		
		CDAUtil.saveSnippet(performer, fw);
		
		fw.close();
	}
}
