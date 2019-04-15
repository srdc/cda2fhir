package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;

public interface ICDAEntity {
	void addII(II ii);

	void setCode(CE ce);

	void setPerson(Person person);

	void setOrganization(Organization organization);

	void addAD(AD ad);

	void addTEL(TEL tel);
}
