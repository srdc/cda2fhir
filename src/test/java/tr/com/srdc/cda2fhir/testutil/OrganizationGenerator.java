package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

public class OrganizationGenerator {
	private static final String NAME = "The Organization";

	private String name;

	private List<ADGenerator> adGenerators = new ArrayList<>();
	private List<TELGenerator> telGenerators = new ArrayList<>();

	public Organization generate(CDAFactories factories) {
		Organization organization = factories.base.createOrganization();

		if (name != null) {
			ON on = factories.datatype.createON();
			on.addText(name);
			organization.getNames().add(on);
		}

		adGenerators.forEach(adGenerator -> {
			AD ad = adGenerator.generate(factories);
			organization.getAddrs().add(ad);
		});

		telGenerators.forEach(telGenerator -> {
			TEL tel = telGenerator.generate(factories);
			organization.getTelecoms().add(tel);
		});

		return organization;
	}

	public static OrganizationGenerator getDefaultInstance() {
		OrganizationGenerator og = new OrganizationGenerator();

		og.name = NAME;

		ADGenerator adGenerator = ADGenerator.getDefaultInstance();
		TELGenerator telGenerator = TELGenerator.getDefaultInstance();

		og.adGenerators.add(adGenerator);
		og.telGenerators.add(telGenerator);

		return og;
	}

	public static OrganizationGenerator getFullInstance() {
		OrganizationGenerator og = new OrganizationGenerator();

		og.name = NAME;

		ADGenerator adGenerator = ADGenerator.getFullInstance();
		TELGenerator telGenerator = TELGenerator.getFullInstance();

		og.adGenerators.add(adGenerator);
		og.telGenerators.add(telGenerator);

		return og;
	}

	public void verify(org.hl7.fhir.dstu3.model.Organization organization) {
		Assert.assertEquals("Organization name", name, organization.getName());

		List<Address> addresses = organization.getAddress();
		Assert.assertEquals("Organization address count", addresses.size(), adGenerators.size());
		for (int index = 0; index < adGenerators.size(); ++index) {
			adGenerators.get(index).verify(addresses.get(index));
		}

		List<ContactPoint> contactPoints = organization.getTelecom();
		Assert.assertEquals("Organization telecom count", contactPoints.size(), telGenerators.size());
		for (int index = 0; index < telGenerators.size(); ++index) {
			telGenerators.get(index).verify(contactPoints.get(index));
		}
	}
}