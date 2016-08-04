package tr.com.srdc.cda2fhir;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.CDAPackage;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ReactionObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.impl.VitalSignObservationImpl;
import org.openhealthtools.mdht.uml.cda.consol.operations.VitalSignObservationOperations;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.rim.ActRelationship;
import org.openhealthtools.mdht.uml.hl7.rim.Participation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.parser.IParser;

import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;

import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;

	public class CDAParser {
		ResourceTransformer rt= new ResourceTransformerImpl();
		DataTypesTransformer dtt = new DataTypesTransformerImpl();
		private static final FhirContext myCtx = FhirContext.forDstu2();
	public CDAParser() {
        CDAUtil.loadPackages();
    }
	@Test
    public void traverseCCD(InputStream is) throws Exception {
		ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) CDAUtil.load(is);
		testVitalSignObservation2Observation(ccd);
		testResultObservation2Observation(ccd);
		testAllergyObservation2AllergyIntolerance(ccd);
		testSubstanceAdministration2Immunization(ccd);
    }
    @Test
	public void testVitalSignObservation2Observation(ContinuityOfCareDocument ccd)
	{
		 try {
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
	                    		}
	                    		ArrayList <String> fhirIds= new ArrayList <String>();
	                    		for(IdentifierDt identifiers: obs.getIdentifier())
	                    		{
	                    			fhirIds.add(identifiers.getSystem());
	                    		}
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
									TS ts=DatatypesFactory.eINSTANCE.createTS();
									ts.setValue(vsObs.getEffectiveTime().getValue());
									Assert.assertEquals("Effective time were not transformed",datetime.getValueAsString(),dtt.TS2DateTime(ts).getValueAsString());
								}
								else
								{
									PeriodDt period = (PeriodDt) obs.getEffective();
									Assert.assertEquals("VitalSignObservation.effectiveTime was not transformed",period,dtt.IVL_TS2Period(vsObs.getEffectiveTime()));
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
									interpret.add(ce.getCode());
								}
								ArrayList <String> testInterpret = new ArrayList <String>();
								for(CodingDt coding : obs.getInterpretation().getCoding())
								{
									testInterpret.add(coding.getCode());
								}
								Assert.assertEquals("InterpretationCode was not transformed",interpret,testInterpret);
								for(Author author :vsObs.getAuthors())
								{
									Assert.assertEquals("AUT.time was not transformed",dtt.TS2DateTime(author.getTime()).getValueAsString(),obs.getIssuedElement().getValueAsString() );
								}
//								printJSON(obs);
	            			}//end if
	            		}//end for
	            	}//end for
	            }//end for
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	}//end VitalSignObservation test
    @Test
    public void testResultObservation2Observation(ContinuityOfCareDocument ccd)
    {
    	
    	for(ResultOrganizer resOrg :ccd.getResultsSection().getResultOrganizers())
    	{
    		for(ResultObservation resObs : resOrg.getResultObservations())
    		{
				ca.uhn.fhir.model.dstu2.resource.Observation obs = rt.ResultObservation2Observation(resObs);
				
				ArrayList <String> idS= new ArrayList <String> ();
        		for(II id: resObs.getIds())
        		{
        			idS.add(id.getRoot());
        		}
        		ArrayList <String> fhirIds= new ArrayList <String>();
        		for(IdentifierDt identifier: obs.getIdentifier())
        		{
        			fhirIds.add(identifier.getSystem());
        		}
        		Assert.assertEquals("ResultObservation.id was not transformed", idS,fhirIds);
        		ArrayList <String> codings = new ArrayList <String> ();
				for(CodingDt coding :obs.getCode().getCoding())
        		{
					codings.add(coding.getCode());
        		}
				ArrayList <String> testCodings= new ArrayList <String> ();
				for(CodingDt testCoding : dtt.CD2CodeableConcept(resObs.getCode()).getCoding())
				{
					testCodings.add(testCoding.getCode());
				}
				Assert.assertEquals("Codings were not transformed", codings,testCodings); //Successfully transformed
				if(obs.getEffective() instanceof DateTimeDt)
				{
					DateTimeDt datetime= (DateTimeDt) obs.getEffective();
					TS ts=DatatypesFactory.eINSTANCE.createTS();
					ts.setValue(resObs.getEffectiveTime().getValue());
					Assert.assertEquals("Effective time were not transformed",datetime.getValueAsString(),dtt.TS2DateTime(ts).getValueAsString());
				}
				else
				{
					PeriodDt period = (PeriodDt) obs.getEffective();
					Assert.assertEquals("VitalSignObservation.effectiveTime was not transformed",period,dtt.IVL_TS2Period(resObs.getEffectiveTime()));
				}//end else
				for(ANY any : resObs.getValues())
				{
					
					if(any instanceof PQ)
					{
						
						PQ pq= (PQ) any;
						if(!pq.isSetNullFlavor())
						{	QuantityDt quantity=(QuantityDt) obs.getValue();
							Assert.assertEquals("unit was not transformed", quantity.getUnit(),pq.getUnit());
							Assert.assertEquals("value was not transformed", quantity.getValue(),pq.getValue());
						}//end if
					}//end if, same test can be applied to all other examples with this manner
				}//end for
				if(obs.getStatus()!=null)
					Assert.assertEquals("StatusCode was not transformed","final", obs.getStatus());
				
				ArrayList <String> interpret= new ArrayList <String>();
				for(CE ce : resObs.getInterpretationCodes())
				{
					interpret.add(ce.getCode());
				}
				ArrayList <String> testInterpret = new ArrayList <String>();
				for(CodingDt coding : obs.getInterpretation().getCoding())
				{
					testInterpret.add(coding.getCode());
				}
				Assert.assertEquals("InterpretationCode was not transformed",interpret,testInterpret);
				if(resObs.getPerformers()!=null && !resObs.getPerformers().isEmpty())
				{
					for(Performer2 performer : resObs.getPerformers())
					{
						Practitioner practitioner = rt.Performer2Practitioner(performer, 0);
						if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor())
						{
							ArrayList <String> CDAidS= new ArrayList <String>();
							AssignedEntity assignedEntity= performer.getAssignedEntity();
							if(assignedEntity.getIds()!=null && !performer.getAssignedEntity().isSetNullFlavor())
							{
								for(II ii : assignedEntity.getIds())
								{
									CDAidS.add(ii.getRoot());
								}//end for
							}//end if
							ArrayList <String> FHIRidS = new ArrayList <String>();
							if(practitioner.getIdentifier()!=null && !practitioner.getIdentifier().isEmpty())
							{
								for(IdentifierDt identifier : practitioner.getIdentifier())
								{
									FHIRidS.add(identifier.getSystem());
								}//end for
							}//end if
							Assert.assertEquals("performer.id was not transformed",CDAidS, FHIRidS);
							ArrayList <String> CDAAddress = new ArrayList <String>();
//							printJSON(practitioner);
						}//end if
					}//end for
				}//end if
				
//				printJSON(obs);
    		}//end resObs for
    	}
    }//end ResultObservation test
    
    public void testAllergyObservation2AllergyIntolerance(ContinuityOfCareDocument ccd)
    {
    	 // get the allergies section from the document using domain-specific "getter" method
        AllergiesSection allergiesSection = ccd.getAllergiesSection();
        
        // for each enclosing problem act
        for (AllergyProblemAct problemAct : allergiesSection.getConsolAllergyProblemActs()) {
            
        	AllergyIntolerance allergyIntolerance = rt.AllergyProblemAct2AllergyIntolerance(problemAct);
            for (EntryRelationship entryRelationship : problemAct.getEntryRelationships()) {
                // check for alert observation
                if (entryRelationship.getObservation() instanceof AllergyObservation) 
                {
                    AllergyObservation allergyObservation = (AllergyObservation) entryRelationship.getObservation();
                    ArrayList <String> CDAidS = new ArrayList <String> ();
                    if(allergyObservation.getIds()!=null && !allergyObservation.getIds().isEmpty())
                    {
                    	for(II ii : allergyObservation.getIds())
                    	{
                    		CDAidS.add(ii.getRoot());
                    	}
                    }//end if
                    ArrayList <String> FHIRidS = new ArrayList <String>();
                    if(allergyIntolerance.getIdentifier()!=null && !allergyIntolerance.getIdentifier().isEmpty())
                    {
                    	for(IdentifierDt identifier : allergyIntolerance.getIdentifier())
                    	{
                    		FHIRidS.add(identifier.getSystem());
                    	}//end for
                    }//end if
                    Assert.assertEquals("allergyObservation.id was not transformed",CDAidS, FHIRidS);
                    ArrayList <CodingDt> manifestations=new ArrayList <CodingDt>();
                   	if(allergyIntolerance.getReaction()!=null && !allergyIntolerance.getReaction().isEmpty())
                   	{
                        for(Reaction reaction : allergyIntolerance.getReaction())
                        {
                            if(reaction.getManifestation()!=null && !reaction.getManifestation().isEmpty())
                            {
                            	for(CodeableConceptDt codeableConcept : reaction.getManifestation())
                            	{
                            		for(CodingDt coding : codeableConcept.getCoding())
                           			{
                            			manifestations.add(coding);
//                            			System.out.println("FHIR: code="+coding.getCode()+" display= "+coding.getDisplay()+" system= "+coding.getSystem());
                           			}//end for
                            	}//end for
                         	}//end if
                         }//end for
                   	}//end if
                }//end if
            }//end for
//            printJSON(allergyIntolerance);
        }//end for
    }//end  AllergyIntolerance test
    @Test
    public void testSubstanceAdministration2Immunization(ContinuityOfCareDocument ccd)
    {
    	for(SubstanceAdministration substanceAdministration : ccd.getImmunizationsSectionEntriesOptional().getSubstanceAdministrations())
    	{
    		Immunization immunization = rt.SubstanceAdministration2Immunization(substanceAdministration);
    		printJSON(immunization);
    	}
    }
	private void printJSON(IResource res) {
	    IParser jsonParser = myCtx.newJsonParser();
	    jsonParser.setPrettyPrint(true);
	    System.out.println(jsonParser.encodeResourceToString(res));
	}
}
	
