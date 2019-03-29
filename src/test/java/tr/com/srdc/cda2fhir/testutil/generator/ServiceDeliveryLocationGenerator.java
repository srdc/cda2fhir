package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Location;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class ServiceDeliveryLocationGenerator {
	private static int INDEX = 1;

	String name;

	private List<TELGenerator> telGenerators = new ArrayList<>();
	private List<ADGenerator> adGenerators = new ArrayList<>();

	public ParticipantRole generate(CDAFactories factories) {
		ParticipantRole pr = factories.base.createParticipantRole();

		if (name != null) {
			PlayingEntity pe = factories.base.createPlayingEntity();
			PN pn = factories.datatype.createPN();
			pn.addText(name);
			pe.getNames().add(pn);
			pr.setPlayingEntity(pe);
		}

		telGenerators.forEach(tg -> {
			TEL tel = tg.generate(factories);
			pr.getTelecoms().add(tel);
		});

		adGenerators.forEach(adg -> {
			AD ad = adg.generate(factories);
			pr.getAddrs().add(ad);
		});

		return pr;
	}

	public static ServiceDeliveryLocationGenerator getDefaultInstance() {
		ServiceDeliveryLocationGenerator sdg = new ServiceDeliveryLocationGenerator();

		sdg.name = "name_" + (++INDEX);
		sdg.telGenerators.add(TELGenerator.getDefaultInstance());
		sdg.adGenerators.add(ADGenerator.getDefaultInstance());

		return sdg;
	}

	public void verify(Location location) {
		if (name == null) {
			Assert.assertTrue("No location name", !location.hasName());
		} else {
			Assert.assertEquals("Location name", name, location.getName());
		}

		if (telGenerators.isEmpty()) {
			Assert.assertTrue("No location telecoms", !location.hasTelecom());
		} else {
			int count = telGenerators.size();
			Assert.assertEquals("Location telecom count", count, location.getTelecom().size());
			for (int index = 0; index < count; ++index) {
				telGenerators.get(index).verify(location.getTelecom().get(index));
			}
		}

		if (adGenerators.isEmpty()) {
			Assert.assertTrue("No location addresses", !location.hasAddress());
		} else {
			ADGenerator adGenerator = adGenerators.get(adGenerators.size() - 1);
			adGenerator.verify(location.getAddress());

		}
	}
}
