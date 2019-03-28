package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AssignedEntityGenerator {
	static final public String DEFAULT_CODE_CODE = "363LA2100X";
	static final public String DEFAULT_CODE_PRINTNAME = "Nurse Practitioner - Acute Care";

	private List<IDGenerator> idGenerators = new ArrayList<>();

	private String codeCode;
	private String codePrintName;

	private PNGenerator pnGenerator;

	private OrganizationGenerator organizationGenerator;

	public AssignedEntity generate(CDAFactories factories) {
		AssignedEntity entity = factories.base.createAssignedEntity();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			entity.getIds().add(ii);
		});

		if (pnGenerator != null) {
			PN pn = pnGenerator.generate(factories);
			Person person = factories.base.createPerson();
			person.getNames().add(pn);
			entity.setAssignedPerson(person);
		}

		if (codeCode != null) {
			CE ce = factories.datatype.createCE(codeCode, "2.16.840.1.11388 3.6.101",
					"Healthcare Provider Taxonomy (HIPAA)", codePrintName);
			entity.setCode(ce);
		}

		if (organizationGenerator != null) {
			Organization organization = organizationGenerator.generate(factories);
			entity.getRepresentedOrganizations().add(organization);
		}

		return entity;
	}

	public void setCode(String code, String printName) {
		codeCode = code;
		codePrintName = printName;
	}

	public PNGenerator getPNGenerator() {
		return pnGenerator;
	}

	public static AssignedEntityGenerator getDefaultInstance() {
		AssignedEntityGenerator aeg = new AssignedEntityGenerator();

		aeg.idGenerators.add(IDGenerator.getNextInstance());

		aeg.setCode(DEFAULT_CODE_CODE, DEFAULT_CODE_PRINTNAME);

		aeg.pnGenerator = PNGenerator.getDefaultInstance();
		aeg.organizationGenerator = OrganizationGenerator.getDefaultInstance();

		return aeg;
	}

	public void verify(Practitioner practitioner) {
		if (pnGenerator == null || pnGenerator.hasNullFlavor()) {
			Assert.assertTrue("Missing practioner name", !practitioner.hasName());
		} else {
			HumanName humanName = practitioner.getName().get(0);
			pnGenerator.verify(humanName);
		}

		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(practitioner.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No practitioner identifier", !practitioner.hasIdentifier());
		}
	}

	public void verify(PractitionerRole role) {
		if (organizationGenerator.isNullFlavor()) {
			Assert.assertNull("Role when null flavored org", role);
		} else {
			Coding code = role.getCode().get(0).getCoding().get(0);
			Assert.assertEquals("Role code", codeCode, code.getCode());
			Assert.assertEquals("Role print name", codePrintName, code.getDisplay());
		}
	}

	public void verify(org.hl7.fhir.dstu3.model.Organization organization) {
		if (organizationGenerator == null || organizationGenerator.isNullFlavor()) {
			Assert.assertNull("Author organization", organization);
			return;
		}
		organizationGenerator.verify(organization);
	}
}
