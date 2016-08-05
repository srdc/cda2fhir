package tr.com.srdc.cda2fhir;


import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.*;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;



import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.parser.IParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;

public class ResourceTransformerTestIsmail {
	
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	ResourceTransformer rt = new ResourceTransformerImpl();
	private static final FhirContext myCtx = FhirContext.forDstu2();
	
	
	FileInputStream fisCCD = null;
	
	
	@Test
	public void test() throws Exception{
		
		CDAUtil.loadPackages();
        
        try {
            fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
          //traverseCCDProblemAct( fisCCD );
            //traverseCCDManuPro( fisCCD );
            //traverseCCDMedicationDispense( fisCCD );
            traverseCCDMedicationActivity( fisCCD );
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
       
        
		
   
	}
	
	
	public void traverseCCDProblemAct(InputStream is) {
        try {
            // validate on load
            // create validation result to hold diagnostics
            ValidationResult result = new ValidationResult();

            ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is, result);

            // print validation results
//            for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
//                System.out.println(diagnostic.getMessage());
//            }

            // get the allergies section from the document using domain-specific "getter" method
            ProblemSection problemSection = ccd.getProblemSection();

            // for each enclosing problem act
            for (ProblemConcernAct problemAct : problemSection.getConsolProblemConcerns()) {
                // look at subordinate observations
                // we don't have a domain-specific "getter" method here so we use
                // entry relationship
            	List<Condition> conditionList = rt.ProblemConcernAct2Condition(problemAct);
            	int i = 0;
            	
                for (EntryRelationship entryRelationship : problemAct.getEntryRelationships()) {
                    // check for alert observation
                    if (entryRelationship.getObservation() instanceof ProblemObservation) {
                    	
                        ProblemObservation problemObservation = (ProblemObservation) entryRelationship.getObservation();
 
                        CD value = (CD) problemObservation.getValues().get(0);
                        Assert.assertEquals("probAct.value transformation failed" , value.getCode() , conditionList.get(i).getCode().getCoding().get(0).getCode() );
                        Assert.assertEquals("probAct.value transformation failed" , 
                        		value.getDisplayName() , conditionList.get(i).getCode().getCoding().get(0).getDisplay() );
                        
                        DateDt date = dtt.TS2Date( problemObservation.getAuthors().get(0).getTime() );
                        Assert.assertEquals("probAct.dateRecoreded transformation failed" , date ,  conditionList.get(i).getDateRecordedElement() );
                        
                        PeriodDt period = dtt.IVL_TS2Period( problemObservation.getEffectiveTime() );
                        Assert.assertEquals("probAct.Onset transformation failed", period.getStart(), 
                        			((DateTimeDt) conditionList.get(i).getOnset()).getValue() );
                        Assert.assertEquals("probAct.abatement transformation failed", period.getEnd()
                        		, ((DateTimeDt) conditionList.get(i).getAbatement()).getValue() );
                        
                        if(problemAct.getIds() != null & !problemAct.getIds().isEmpty()){
	                        IdentifierDt identifier = dtt.II2Identifier( problemAct.getIds().get(0) );
	                        Assert.assertEquals("probAct.id transformation failed" + i  , identifier.getPeriod().getStart()  , conditionList.get(i).getIdentifier().get(0).getPeriod().getStart());
	                        
                        }
                        
                        printJSON( conditionList.get(i) );
                        
                    }
                    i++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	
	
	

	public void traverseCCDManuPro( InputStream is) throws Exception{
		
		try{
			// validate on load
	        // create validation result to hold diagnostics
	        ValidationResult result = new ValidationResult();

	        ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is, result);

	        // print validation results
//	        for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
//	            System.out.println(diagnostic.getMessage());
//	        }

	        // get the medication section from the document using domain-specific "getter" method
	        MedicationsSection medicationSection = ccd.getMedicationsSection();
	        int i = 0;
	        
	        for( MedicationActivity medicationActivity  : medicationSection.getMedicationActivities() )
	        {
	        	ManufacturedProduct manPro = medicationActivity.getConsumable().getManufacturedProduct();
	        	Medication medication = rt.Medication2Medication( manPro );
	        	if(medication == null ) {
	        		System.out.println("null");
	        		break;
	        	}
	        	if( medication.getCode().getCoding() != null )
	        		if( medication.getCode().getCoding().size() != 0 ){
		        		Assert.assertEquals( "manPro.code transformation failed", manPro.getManufacturedMaterial().getCode().getCode(), 
		        			medication.getCode().getCoding().get(0).getCode() );
	        		}
	        	
	        	if(manPro.getManufacturerOrganization() != null ){
	        		if( !manPro.getManufacturerOrganization().isSetNullFlavor() ){
		        		Assert.assertEquals("manPro.manufacturer transformation failed "  ,
		        				manPro.getManufacturerOrganization().getNames().get(0).getText() ,
			        			medication.getManufacturer().getDisplay().getValue() );
		        		
		        		Assert.assertTrue( "manPro.isBrand transformation failed", medication.getIsBrand() );
	        		}else
	        			Assert.assertFalse( "manPro.isBrand transformation failed", medication.getIsBrand() );
	        	}
	        		
	        	else
	        		Assert.assertFalse( "manPro.isBrand transformation failed", medication.getIsBrand() );
	        	
	        	printJSON(medication);
	        	i++;
	        }
		}
		finally{
			
		}
	
		
	}
	

	public void traverseCCDMedicationDispense(InputStream is) throws Exception {
		
		try{
			// validate on load
	        // create validation result to hold diagnostics
	        ValidationResult result = new ValidationResult();

	        ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is, result);

	        // print validation results
//	        for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
//	            System.out.println(diagnostic.getMessage());
//	        }

	        // get the medication section from the document using domain-specific "getter" method
	        MedicationsSection medicationSection = ccd.getMedicationsSection();
	       
	        
	        for( MedicationActivity medicationActivity  : medicationSection.getMedicationActivities() )
	        {
	        	for(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense meDisCda : medicationActivity.getMedicationDispenses()){
	
		        	if( meDisCda.getMoodCode() != x_DocumentSubstanceMood.EVN ) continue;
		        	
		        	MedicationDispense meDis = rt.MedicationDispense2MedicationDispense( meDisCda ); 
		        	Assert.assertEquals( "dispense.Identifier failed" , dtt.II2Identifier(meDisCda.getIds().get(0)).getValue(), meDis.getIdentifier().getValue());
		        	//Assert.assertEquals( "dispense.code failed", dtt.CD2CodeableConcept(meDisCda.getCode()).getCoding().get(0).getCode() , meDis.getType().getCoding().get(0).getCode()   );
		        	Assert.assertEquals( "dispense.statusCode failed" , meDisCda.getStatusCode().getDisplayName() , meDis.getStatus() );
		        	Assert.assertEquals( "dispense.quantity failed" , meDisCda.getQuantity().getValue() , meDis.getQuantity().getValue() );
		        	if( meDisCda.getEffectiveTimes() != null & meDisCda.getEffectiveTimes().size() != 0 ){
		        		Assert.assertEquals( "dispense.whenPrepared failed" , dtt.TS2DateTime( meDisCda.getEffectiveTimes().get(0) ).getValue(), meDis.getWhenPrepared());
		        		if(meDisCda.getEffectiveTimes().size() != 1 )
		        			Assert.assertEquals( "dispense.whenHandedOver failed" , dtt.TS2DateTime( meDisCda.getEffectiveTimes().get(1) ).getValue(), meDis.getWhenHandedOver());
		        	}
		        	
		        	printJSON(meDis);
		        	break;
	        	}
	        	break;
	        	
	        }
		}
		finally{
			
		}
		
	}
	
public void traverseCCDMedicationActivity(InputStream is) throws Exception {
		
		try{
			// validate on load
	        // create validation result to hold diagnostics
	        ValidationResult result = new ValidationResult();

	        ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is, result);

	        // print validation results
//	        for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
//	            System.out.println(diagnostic.getMessage());
//	        }

	        // get the medication section from the document using domain-specific "getter" method
	        MedicationsSection medicationSection = ccd.getMedicationsSection();
	       
	        
	        for( SubstanceAdministration medAc  : medicationSection.getMedicationActivities().get(0).getSubstanceAdministrations() )
	        {
	        	MedicationStatement medSt = rt.MedicationActivity2MedicationSatement( medAc );
	        	Assert.assertEquals( "medStatement.identifier failed" , dtt.II2Identifier(medAc.getIds().get(0)), medSt.getIdentifier().get(0) );
	        	Assert.assertEquals( "medStatement.status failed" , medAc.getStatusCode().getDisplayName() , medSt.getStatus()  );
	        	Assert.assertEquals( dtt.IVL_PQ2Range(medAc.getRateQuantity()).getHigh() , ((RangeDt) medSt.getDosage().get(0).getRate()).getHigh() );
	        	Assert.assertEquals( "medStatement.effective failed" ,  dtt.IVL_TS2Period( (IVL_TS) medAc.getEffectiveTimes().get(0) ).getStart() , ((PeriodDt) medSt.getEffective()).getStart() );
	        	
	        	for( EntryRelationship ers : medAc.getEntryRelationships() ){
	    			
	    			if( ers.getTypeCode() == x_ActRelationshipEntryRelationship.RSON ){
	    				if( ers.getObservation() != null  && !ers.getObservation().isSetNullFlavor()){
							if(ers.getObservation().getValues() != null && ers.getObservation().getValues()!=null){
								Assert.assertEquals( "medStatement.reason failed" , false , medSt.getWasNotTaken() );
							}
	    				}
	    				
	    				
	    			}
	    			
	        	}	
	        	printJSON(medSt);
	        	break;
	        	
	        }
		}
		finally{
			
		}
		
	}

	private void printJSON(IResource res) {
	    IParser jsonParser = myCtx.newJsonParser();
	    jsonParser.setPrettyPrint(true);
	    System.out.println(jsonParser.encodeResourceToString(res));
	}
	
}
