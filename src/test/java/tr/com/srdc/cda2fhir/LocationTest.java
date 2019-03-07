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
import org.openhealthtools.mdht.uml.cda.impl.ParticipantRoleImpl;
import org.openhealthtools.mdht.uml.cda.impl.PlayingEntityImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class LocationTest {

    private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	private static CDAFactoryImpl cdaFactory;

    @BeforeClass
	public static void init() {
		CDAUtil.loadPackages();			
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();		
    }
 

    @Test
    public void testLocations() throws Exception {
      
    	// Make a participant.
    	ParticipantRoleImpl pr = (ParticipantRoleImpl) cdaFactory.createParticipantRole();
    	PlayingEntityImpl	pe = (PlayingEntityImpl) cdaFactory.createPlayingEntity();
    	
    	pr.setPlayingEntity(pe);
    	
        // Transform from CDA to FHIR.
        org.hl7.fhir.dstu3.model.Location fhirLocation = rt.tParticipantRole2Location(pr);

        Assert.assertEquals("No identifier on FHIR object.",0,fhirLocation.getIdentifier().size());
        
    }


}