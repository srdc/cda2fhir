package tr.com.srdc.cda2fhir;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2019 Amida Technology Solutions, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.impl.OrganizationImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class entitiesTest {

    private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

    @BeforeClass
	public static void init() {
		CDAUtil.loadPackages();	
		cdaTypeFactory = DatatypesFactoryImpl.init();		
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();		
    }
 

    @Test
    public void organizationNameAlias() throws Exception {
        
    	String orgStringOne = "Fake Organization One";
    	String orgStringTwo = "Fake Organization Two";
    	
    	// Make an organization, add two names.
    	OrganizationImpl org = (OrganizationImpl) cdaFactory.createOrganization();

        ON orgNameOne = cdaTypeFactory.createON();
        orgNameOne.addText(orgStringOne);
        org.getNames().add(orgNameOne);
        
        ON orgNameTwo = cdaTypeFactory.createON();
        orgNameTwo.addText(orgStringTwo);      
        org.getNames().add(orgNameTwo);

        // Transform from CDA to FHIR.
        org.hl7.fhir.dstu3.model.Organization fhirOrganization = rt.tOrganization2Organization(org);
 
        // Make assertions.
        Assert.assertEquals("Organization name was set",orgStringOne,fhirOrganization.getName());
        Assert.assertEquals("Organization alias was set",orgStringTwo,fhirOrganization.getAlias().get(0).asStringValue());
        Assert.assertEquals("Only one organization alias",1,fhirOrganization.getAlias().size());
    }


}