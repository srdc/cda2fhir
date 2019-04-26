package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class OrganizationGenerator {
	private static final String NAME = "The Organization";

	private String nullFlavor;

	private String name;

	private List<IDGenerator> idGenerators = new ArrayList<>();
	private List<ADGenerator> adGenerators = new ArrayList<>();
	private List<TELGenerator> telGenerators = new ArrayList<>();

	public OrganizationGenerator() {
	}

	public OrganizationGenerator(String name) {
		this.name = name;
	}

	public void setADGenerator(ADGenerator adGenerator) {
		adGenerators.clear();
		adGenerators.add(adGenerator);
	}

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public boolean isNullFlavor() {
		return nullFlavor != null;
	}

	public boolean shouldExists() {
		return nullFlavor == null && (name != null || !idGenerators.isEmpty());
	}

	public CustodianOrganization generateCustodianOrg(CDAFactories factories) {
		CustodianOrganization custodianOrg = factories.base.createCustodianOrganization();
		Organization org = generate(factories);
		if (org.getNames() != null) {

			for (int i = 0; i < org.getNames().size(); i++) {
				custodianOrg.setName(org.getNames().get(i));
			}
		}

		if (org.getIds() != null) {
			custodianOrg.getIds().addAll(org.getIds());
		}

		if (org.getAddrs() != null) {
			for (int i = 0; i < org.getAddrs().size(); i++) {
				custodianOrg.setAddr(org.getAddrs().get(i));
			}
		}

		if (org.getTelecoms() != null) {
			for (int i = 0; i < org.getTelecoms().size(); i++) {
				custodianOrg.setTelecom(org.getTelecoms().get(i));
			}
		}

		if (org.getNullFlavor() != null) {
			custodianOrg.setNullFlavor(org.getNullFlavor());
		}

		return custodianOrg;
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

		og.idGenerators.add(IDGenerator.getNextInstance());

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