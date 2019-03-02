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

import java.util.List;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.impl.OrganizationImpl;
import org.openhealthtools.mdht.uml.cda.impl.AuthorImpl;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.impl.AssignedAuthorImpl;
import org.openhealthtools.mdht.uml.cda.impl.PersonImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.testutil.AssignedEntityGenerator;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntityResult;

public class EntitiesTest {

    private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;

	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

    @BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
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
    
    @Test
    public void practitionerNameMultiple() throws Exception {
        
    	String authorStringOne = "Name One";
    	String authorStringTwo = "Name Two";
    	
    	// Make an author, add two names.
    	AuthorImpl auth = (AuthorImpl) cdaFactory.createAuthor();
    	AssignedAuthorImpl assAuth = (AssignedAuthorImpl) cdaFactory.createAssignedAuthor();
    	PersonImpl pers = (PersonImpl) cdaFactory.createPerson();
    	
    	auth.basicSetAssignedAuthor(assAuth, null);
    	auth.getAssignedAuthor().setAssignedPerson(pers);
    	
    	
    	PN authorNameOne = cdaTypeFactory.createPN();
    	authorNameOne.addText(authorStringOne);
    	auth.getAssignedAuthor().getAssignedPerson().getNames().add(authorNameOne);
    	
    	
    	PN authorNameTwo = cdaTypeFactory.createPN();
    	authorNameTwo.addText(authorStringTwo);
    	auth.getAssignedAuthor().getAssignedPerson().getNames().add(authorNameTwo);


        // Transform from CDA to FHIR.
        IEntityResult entityResult = rt.tAuthor2Practitioner(auth);
        org.hl7.fhir.dstu3.model.Resource fhirResource = entityResult.getPractitioner();
        List<Base> fhirNames = fhirResource.getNamedProperty("name").getValues();
                
        // Make assertions.
        Assert.assertEquals("Multiple Name for Practitioner Supported",2,fhirNames.size());
        Assert.assertEquals("Practitioner Name One Set",authorStringOne, fhirNames.get(0).getNamedProperty("text").getValues().get(0).toString());;
        Assert.assertEquals("Practitioner Name Two Set",authorStringTwo, fhirNames.get(1).getNamedProperty("text").getValues().get(0).toString());;
    }


    @Test
    public void testAssignedEntityBasic() throws Exception {    	
    	AssignedEntityGenerator aeg = AssignedEntityGenerator.getDefaultInstance();
    	
    	AssignedEntity ae = aeg.generate(factories);
    	IEntityResult entityResult = rt.tAssignedEntity2Practitioner(ae);
    	
    	Practitioner practitioner = entityResult.getPractitioner();
    	aeg.verify(practitioner);
    	
    	PractitionerRole role = entityResult.getPractitionerRole();
    	aeg.verify(role);
 
    	Organization org = entityResult.getOrganization();
    	aeg.verify(org);
    }
}