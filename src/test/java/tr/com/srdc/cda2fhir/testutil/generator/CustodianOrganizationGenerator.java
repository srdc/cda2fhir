package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Identifier;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class CustodianOrganizationGenerator {
	private static final String NAME = "The Organization";

	private String nullFlavor;

	private String name;

	private List<IDGenerator> idGenerators = new ArrayList<>();
	private ADGenerator adGenerator;
	private TELGenerator telGenerator;

	public CustodianOrganizationGenerator() {
	}

	public CustodianOrganizationGenerator(String name) {
		this.name = name;
	}

	public void setADGenerator(ADGenerator adGenerator) {
		this.adGenerator = adGenerator;
	}

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public boolean isNullFlavor() {
		return nullFlavor != null;
	}

	public CustodianOrganization generate(CDAFactories factories) {
		CustodianOrganization organization = factories.base.createCustodianOrganization();

		if (name != null) {
			ON on = factories.datatype.createON();
			on.addText(name);
			organization.setName(on);
		}

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			organization.getIds().add(ii);
		});

		if (adGenerator != null) {
			AD ad = adGenerator.generate(factories);
			organization.setAddr(ad);
		}

		if (telGenerator != null) {
			TEL tel = telGenerator.generate(factories);
			organization.setTelecom(tel);
		}

		if (nullFlavor != null) {
			NullFlavor nf = NullFlavor.get(nullFlavor);
			if (nf == null) {
				throw new TestSetupException("Invalid null flavor enumeration.");
			}
			organization.setNullFlavor(nf);
		}

		return organization;
	}

	public static CustodianOrganizationGenerator getDefaultInstance() {
		CustodianOrganizationGenerator og = new CustodianOrganizationGenerator();

		og.name = NAME;

		og.adGenerator = ADGenerator.getDefaultInstance();
		og.telGenerator = TELGenerator.getDefaultInstance();

		return og;
	}

	public static CustodianOrganizationGenerator getFullInstance() {
		CustodianOrganizationGenerator og = new CustodianOrganizationGenerator();

		og.name = NAME;

		og.idGenerators.add(IDGenerator.getNextInstance());
		og.idGenerators.add(IDGenerator.getNextInstance());
		og.idGenerators.add(IDGenerator.getNextInstance());

		og.adGenerator = ADGenerator.getDefaultInstance();
		og.telGenerator = TELGenerator.getDefaultInstance();

		return og;
	}

	public void verify(org.hl7.fhir.dstu3.model.Organization organization) {
		Assert.assertEquals("Organization name", name, organization.getName());

		List<Identifier> identifiers = organization.getIdentifier();
		Assert.assertEquals("Organization identifier count", identifiers.size(), idGenerators.size());
		for (int index = 0; index < idGenerators.size(); ++index) {
			idGenerators.get(index).verify(identifiers.get(index));
		}

		if (adGenerator == null) {
			Assert.assertTrue("No address", !organization.hasAddress());
		} else {
			Assert.assertEquals("Address count", 1, organization.getAddress().size());
			adGenerator.verify(organization.getAddress().get(0));
		}

		if (telGenerator == null) {
			Assert.assertTrue("No telecom", !organization.hasTelecom());
		} else {
			Assert.assertEquals("Telecom count", 1, organization.getTelecom().size());
			telGenerator.verify(organization.getTelecom().get(0));
		}
	}
}