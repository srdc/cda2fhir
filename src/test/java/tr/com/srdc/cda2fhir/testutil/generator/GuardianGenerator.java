package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Patient.ContactComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class GuardianGenerator {
	private static final Map<String, Object> CONTACT_RLATIONSHIP = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ContactRelationship.json");

	private List<ADGenerator> addrGenerators = new ArrayList<>();
	private List<TELGenerator> telecomGenerators = new ArrayList<>();
	private List<PNGenerator> personNameGenerators = new ArrayList<>();
	private CECodeGenerator codeGenerator;

	public Guardian generate(CDAFactories factories) {
		Guardian guardian = factories.base.createGuardian();

		addrGenerators.forEach(g -> {
			AD ad = g.generate(factories);
			guardian.getAddrs().add(ad);
		});

		telecomGenerators.forEach(g -> {
			TEL tel = g.generate(factories);
			guardian.getTelecoms().add(tel);
		});

		if (!personNameGenerators.isEmpty()) {
			Person person = factories.base.createPerson();
			guardian.setGuardianPerson(person);
			personNameGenerators.forEach(g -> {
				PN pn = g.generate(factories);
				person.getNames().add(pn);
			});
		}

		if (codeGenerator != null) {
			CE ce = codeGenerator.generate(factories);
			guardian.setCode(ce);
		}

		return guardian;
	}

	public static GuardianGenerator getDefaultInstance() {
		GuardianGenerator gg = new GuardianGenerator();

		gg.addrGenerators.add(ADGenerator.getDefaultInstance());
		gg.telecomGenerators.add(TELGenerator.getDefaultInstance());
		gg.personNameGenerators.add(PNGenerator.getDefaultInstance());
		gg.codeGenerator = new CECodeGenerator(CONTACT_RLATIONSHIP);
		gg.codeGenerator.set("work");

		return gg;
	}

	public void verify(ContactComponent contactComponent) {
		if (addrGenerators.isEmpty()) {
			Assert.assertTrue("No addresses", !contactComponent.hasAddress());
		} else {
			ADGenerator addrGenerator = addrGenerators.get(0);
			addrGenerator.verify(contactComponent.getAddress());
		}

		if (telecomGenerators.isEmpty()) {
			Assert.assertTrue("No telecoms", !contactComponent.hasTelecom());
		} else {
			TELGenerator.verifyList(contactComponent.getTelecom(), telecomGenerators);
		}

		if (personNameGenerators.isEmpty()) {
			Assert.assertTrue("No names", !contactComponent.hasName());
		} else {
			PNGenerator pnGenerator = personNameGenerators.get(personNameGenerators.size() - 1);
			pnGenerator.verify(contactComponent.getName());
		}

		if (codeGenerator == null) {
			Assert.assertTrue("No code", !contactComponent.hasRelationship());
		} else {
			codeGenerator.verify(contactComponent.getRelationship().get(0));
		}
	}

	public static void verifyList(List<ContactComponent> actual, List<GuardianGenerator> expected) {
		Assert.assertEquals("Guardian contact", expected.size(), actual.size());
		for (int index = 0; index < actual.size(); ++index) {
			expected.get(index).verify(actual.get(index));
		}
	}
}
