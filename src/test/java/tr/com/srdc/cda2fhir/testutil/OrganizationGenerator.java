package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

public class OrganizationGenerator {
	private static final String NAME = "The Organization";

	private String nullFlavor;

	private String name;

	private List<IDGenerator> idGenerators = new ArrayList<>();
	private List<ADGenerator> adGenerators = new ArrayList<>();
	private List<TELGenerator> telGenerators = new ArrayList<>();

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public boolean isNullFlavor() {
		return nullFlavor != null;
	}

	public Organization generate(CDAFactories factories) {
		Organization organization = factories.base.createOrganization();

		if (name != null) {
			ON on = factories.datatype.createON();
			on.addText(name);
			organization.getNames().add(on);
		}

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			organization.getIds().add(ii);
		});

		adGenerators.forEach(adGenerator -> {
			AD ad = adGenerator.generate(factories);
			organization.getAddrs().add(ad);
		});

		telGenerators.forEach(telGenerator -> {
			TEL tel = telGenerator.generate(factories);
			organization.getTelecoms().add(tel);
		});

		if (nullFlavor != null) {
			NullFlavor nf = NullFlavor.get(nullFlavor);
			if (nf == null) {
				throw new TestSetupException("Invalid null flavor enumeration.");
			}
			organization.setNullFlavor(nf);
		}

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

		og.idGenerators.add(IDGenerator.getNextInstance());
		og.idGenerators.add(IDGenerator.getNextInstance());
		og.idGenerators.add(IDGenerator.getNextInstance());

		og.adGenerators.add(ADGenerator.getFullInstance());
		og.telGenerators.add(TELGenerator.getFullInstance());
		og.adGenerators.add(ADGenerator.getDefaultInstance());
		og.telGenerators.add(TELGenerator.getDefaultInstance());

		return og;
	}

	public void verify(org.hl7.fhir.dstu3.model.Organization organization) {
		Assert.assertEquals("Organization name", name, organization.getName());

		List<Identifier> identifiers = organization.getIdentifier();
		Assert.assertEquals("Organization identifier count", identifiers.size(), idGenerators.size());
		for (int index = 0; index < idGenerators.size(); ++index) {
			idGenerators.get(index).verify(identifiers.get(index));
		}

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