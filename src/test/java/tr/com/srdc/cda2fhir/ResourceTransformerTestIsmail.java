package tr.com.srdc.cda2fhir;


import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.eclipse.emf.common.util.Diagnostic;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationSection;
import org.openhealthtools.mdht.uml.cda.consol.*;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;



import org.openhealthtools.mdht.uml.hl7.datatypes.CD;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.parser.IParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;

public class ResourceTransformerTestIsmail {
	
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	ResourceTransformer rt = new ResourceTransformerImpl();
	private static final FhirContext myCtx = FhirContext.forDstu2();

	
	
	@Ignore
	public void testProblemConcernAct2Condition(){
		
		CDAUtil.loadPackages();
		FileInputStream fisCCD = null;
        
        try {
            fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
       
        traverseCCDProblemAct( fisCCD );
		
   
	}
	
	private void traverseCCDProblemAct(InputStream is) {
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
	
	
	
	@Test
	public void testManufacturedProduct2Medication(){
		
		CDAUtil.loadPackages();
		FileInputStream fisCCD = null;
        
        try {
            fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
       
        try {
			traverseCCDManuPro( fisCCD );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void traverseCCDManuPro( InputStream is) throws Exception{
		
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
	        	Medication medication = rt.ManufacturedProduct2Medication( manPro );
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
	        	
	        	i++;
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
