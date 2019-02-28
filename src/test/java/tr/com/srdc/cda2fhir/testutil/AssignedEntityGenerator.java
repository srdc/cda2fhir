package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.impl.PersonImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;

public class AssignedEntityGenerator {
	private String familyName;
	private List<String> givenNames = new ArrayList<String>();

	private String codeCode;
	private String codePrintName;

	private String organizationName;

	static final public String DEFAULT_CODE_CODE = "363LA2100X";
	static final public String DEFAULT_CODE_PRINTNAME = "Nurse Practitioner - Acute Care";

	public AssignedEntity generate(CDAFactories factories) {
		AssignedEntity entity = factories.base.createAssignedEntity();

		if (familyName != null || !givenNames.isEmpty()) {
			PN pn = factories.datatype.createPN();

			if (familyName != null) {
				pn.addFamily(familyName);
			}
			givenNames.stream().forEach(r -> pn.addGiven(r));

			PersonImpl person = (PersonImpl) factories.base.createPerson();
			person.getNames().add(pn);

			entity.setAssignedPerson(person);
		}

		if (codeCode != null) {
			CE ce = factories.datatype.createCE(codeCode, "2.16.840.1.11388 3.6.101",
					"Healthcare Provider Taxonomy (HIPAA)", codePrintName);
			entity.setCode(ce);
		}

		if (organizationName != null) {
			Organization organization = factories.base.createOrganization();
			ON on = factories.datatype.createON();
			on.addText(organizationName);
			organization.getNames().add(on);
			entity.getRepresentedOrganizations().add(organization);
		}

		return entity;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void addGivenName(String givenName) {
		givenNames.add(givenName);
	}

	public void setCode(String code, String printName) {
		codeCode = code;
		codePrintName = printName;
	}

	public void setCode() {
		setCode(DEFAULT_CODE_CODE, DEFAULT_CODE_PRINTNAME);
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}
}
