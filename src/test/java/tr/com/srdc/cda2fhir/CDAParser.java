package tr.com.srdc.cda2fhir;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.CDAPackage;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.impl.VitalSignObservationImpl;
import org.openhealthtools.mdht.uml.cda.consol.operations.VitalSignObservationOperations;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.rim.ActRelationship;
import org.openhealthtools.mdht.uml.hl7.rim.Participation;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImplTahsin;
import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

	public class CDAParser {
		ResourceTransformer rt= new ResourceTransformerImplTahsin();
	public CDAParser() {
        CDAUtil.loadPackages();
    }
	@Test
    public void traverseCCD(InputStream is) {
        try {
            // validate on load
            // create validation result to hold diagnostics
//            ValidationResult result = new ValidationResult();

            ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is);
           
            // print validation results
//            for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
//                System.out.println(diagnostic.getMessage());
//            }
            
            for(VitalSignsOrganizer vso:ccd.getVitalSignsSectionEntriesOptional().getVitalSignsOrganizers())
            {
            	
            	for(VitalSignObservation vsObs :vso.getVitalSignObservations())
            	{
            		
            		for(II ii: vsObs.getTemplateIds())
            		{
            			
            			if(ii.getRoot().equals("2.16.840.1.113883.10.20.22.4.27"))//This is the template Id for VitalSignObservation 
            			{
            					
            					
            					ca.uhn.fhir.model.dstu2.resource.Observation obs = rt.VitalSignObservation2Observation(vsObs);
            					ArrayList <String> idS= new ArrayList <String> ();
                    			for(II id: vsObs.getIds())
                    			{
                    				idS.add(id.getRoot());
//                    				System.out.println("CDA:"+id.getRoot());
                    			}
                    			ArrayList <String> fhirIds= new ArrayList <String>();
                    			for(IdentifierDt identifiers: obs.getIdentifier())
                    			{
                    				fhirIds.add(identifiers.getSystem());
//                    				System.out.println("FHIR:"+identifiers.getSystem());
                    			}
                    			DataTypesTransformer dtt = new DataTypesTransformerImpl();

                    			Assert.assertEquals("VitalSignObservation was not transformed", idS,fhirIds);
                    			ArrayList <String> codings = new ArrayList <String> ();
								for(CodingDt coding :obs.getCode().getCoding())
                    			{
									codings.add(coding.getCode());
                    			}
								ArrayList <String> testCodings= new ArrayList <String> ();
								for(CodingDt testCoding : dtt.CD2CodeableConcept(vsObs.getCode()).getCoding())
								{
									testCodings.add(testCoding.getCode());
								}
								Assert.assertEquals("Codings were not transformed", codings,testCodings); //Successfully transformed
								if(obs.getEffective() instanceof DateTimeDt)
								{
									DateTimeDt datetime= (DateTimeDt) obs.getEffective();
//									System.out.println("FHIR:"+datetime.getValueAsString());
//									System.out.println("CDA :"+vsObs.getEffectiveTime().getValue());
									TS ts=DatatypesFactory.eINSTANCE.createTS();
									ts.setValue(vsObs.getEffectiveTime().getValue());
									Assert.assertEquals("Effective time were not transformed",datetime.getValueAsString(),dtt.TS2DateTime(ts).getValueAsString());
								}
								else
								{
									PeriodDt period = (PeriodDt) obs.getEffective();
//									System.out.println("FHIR start:"+period.getStart().getDate());
//									System.out.println("CDA low:"+vsObs.getEffectiveTime().getLow().getValue());
//
//									System.out.println("FHIR end:"+period.getEnd().getDate());
//									System.out.println("CDA low:"+vsObs.getEffectiveTime().getHigh().getValue());
								}//end else
								for(ANY any : vsObs.getValues())
								{
									if(any instanceof PQ)
									{
										PQ pq= (PQ) any;
										QuantityDt quantity=(QuantityDt) obs.getValue();
										Assert.assertEquals("unit was not transformed", quantity.getUnit(),pq.getUnit());
										Assert.assertEquals("value was not transformed", quantity.getValue(),pq.getValue());
									}//end if, same test can be applied to all other examples with this manner
									
								}//end for
								
								Assert.assertEquals("StatusCode was not transformed","final", obs.getStatus());
								ArrayList <String> interpret= new ArrayList <String>();
								for(CE ce : vsObs.getInterpretationCodes())
								{
//									System.out.println("CDA"+" code:"+ce.getCode()+", codeSystem:"+ce.getCodeSystem());
									interpret.add(ce.getCode());
								}
								ArrayList <String> testInterpret = new ArrayList <String>();
								for(CodingDt coding : obs.getInterpretation().getCoding())
								{
//									System.out.println("FHIR"+" code:"+coding.getCode()+", system:"+coding.getSystem());
									testInterpret.add(coding.getCode());
								}
								Assert.assertEquals("InterpretationCode was not transformed",interpret,testInterpret);
								for(Author author :vsObs.getAuthors())
								{
									Assert.assertEquals("AUT.time was not transformed",dtt.TS2DateTime(author.getTime()).getValueAsString(),obs.getIssuedElement().getValueAsString() );
								}
								
            			}//end if
            			
            		}//end for
            	}//end for
            }//end for
            
            // get the allergies section from the document using domain-specific "getter" method
//            AllergiesSection allergiesSection = ccd.getAllergiesSection();

            // for each enclosing problem act
            /*for (AllergyProblemAct problemAct : allergiesSection.getConsolAllergyProblemActs()) {
                // look at subordinate observations
                // we don't have a domain-specific "getter" method here so we use
                // entry relationship
                for (EntryRelationship entryRelationship : problemAct.getEntryRelationships()) {
                    // check for alert observation
                    if (entryRelationship.getObservation() instanceof AllergyObservation) {
                        AllergyObservation allergyObservation = (AllergyObservation) entryRelationship.getObservation();
                        if (!allergyObservation.getValues().isEmpty() & allergyObservation.getValues().get(0) instanceof CD) {
                            CD value = (CD) allergyObservation.getValues().get(0);
                            System.out.println("allergy observation value: " + value.getCode() + ", " + value.getCodeSystem() + ", " + value.getDisplayName());
                        }
                        // get reaction observations using domain-specific "getter" method
                        for (ReactionObservation reactionObservation : allergyObservation.getProblemEntryReactionObservationContainers()) {
                            if (!reactionObservation.getValues().isEmpty() & allergyObservation.getValues().get(0) instanceof CD) {
                                CD value = (CD) reactionObservation.getValues().get(0);
                                System.out.println("reaction observation value: " + value.getCode() + ", " + value.getCodeSystem() + ", " + value.getDisplayName());
                            }
                        }
                    }
                }
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
