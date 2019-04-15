package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.Organization;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class CustodianOrganizationGenerator extends OrganizationGenerator {

	@Override
	public CustodianOrganization generate(CDAFactories factories) {
		Organization org = super.generate(factories);
		CustodianOrganization custodianOrg = factories.base.createCustodianOrganization();

	}

}
