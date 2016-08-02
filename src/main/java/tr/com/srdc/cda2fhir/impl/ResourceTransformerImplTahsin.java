package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Informant12;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.impl.ReferenceRangeImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.PQImpl;
import org.openhealthtools.mdht.uml.hl7.rim.ActRelationship;
import org.openhealthtools.mdht.uml.hl7.rim.Participation;
import org.openhealthtools.mdht.uml.hl7.rim.Role;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipType;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;

public class ResourceTransformerImplTahsin implements ResourceTransformer {
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	static int counter=0;
	/*@Override
	public Observation ResultObservation2Observation(ResultObservation resObs) {
		if(resObs!=null &&  !resObs.isSetNullFlavor())
		{
			Observation observation= new Observation();
			if(resObs.getIds()!=null)
			{
				
				ArrayList <IdentifierDt> allIds = new ArrayList <IdentifierDt> ();
				for(II myIds : resObs.getIds())
				{
					IdentifierDt identifier= new IdentifierDt();
					identifier.setValue(myIds.getRoot());
					allIds.add(identifier);
				}
				observation.setIdentifier(allIds);
			}//end if
		}//end if
		return null;
	}//end function*/
	
	
	public Observation VitalSignObservation2Observation(VitalSignObservation vsObs) {
		if(vsObs!=null && !vsObs.isSetNullFlavor()){
			Observation observation= new Observation();
			
			
			if(vsObs.getIds()!=null & !vsObs.getIds().isEmpty())
			{
				for(II myIds : vsObs.getIds())
				{
//					System.out.println("Implementation side/CDA "+myIds.getRoot());
					observation.addIdentifier(dtt.II2Identifier(myIds));
//					System.out.println("Implementation side/FHIR "+dtt.II2Identifier(myIds).getSystem());
				}//end for
				
			}//end if
			if(vsObs.getStatusCode()!=null && !vsObs.getStatusCode().isSetNullFlavor())
			{
				if(vsObs.getStatusCode().getCode().equals("completed"))
				{
					observation.setStatus(ObservationStatusEnum.FINAL);
				}//end if
			}
			if(vsObs.getInterpretationCodes()!=null && !vsObs.getInterpretationCodes().isEmpty())
			{
				for(CE ce: vsObs.getInterpretationCodes())
				{
					CD cd=(CD) ce;
					observation.setInterpretation(dtt.CD2CodeableConcept(cd));
				}//end for
			}//end if
			if(vsObs.getAuthors()!=null && !vsObs.getAuthors().isEmpty())
			{
				for(Author author : vsObs.getAuthors())
				{
					if(author.getTime()!=null && !author.getTime().isSetNullFlavor())
					{
						observation.setIssued(dtt.TS2Instant(author.getTime()));
					}//end if
				}//end for
			}//end if
			if(!vsObs.getEffectiveTime().isSetNullFlavor() && vsObs.getEffectiveTime()!=null)
			{
				if(vsObs.getEffectiveTime().getValue()!=null)
				{
					TS ts=DatatypesFactory.eINSTANCE.createTS();
					ts.setValue(vsObs.getEffectiveTime().getValue());
					observation.setEffective(dtt.TS2DateTime(ts));
				}
				else
				{
					observation.setEffective(dtt.IVL_TS2Period(vsObs.getEffectiveTime()));
				}
			}//end if
			if(!vsObs.getValues().isEmpty() && vsObs.getValues()!=null)
			{
				for(ANY any : vsObs.getValues())
				{
					if(any.isSetNullFlavor())
					{
						CodeableConceptDt cd = new CodeableConceptDt();
						cd.setText(any.getNullFlavor().getLiteral());
						observation.setDataAbsentReason(cd);
					}
					else
					{
						if(any instanceof PQ)
						{
							PQ pq=(PQ) any;
							observation.setValue(dtt.PQ2Quantity(pq));
						}
						else if(any instanceof ST)
						{
							ST st=(ST) any;
							observation.setValue(dtt.ST2String(st));
						}
						else if(any instanceof CD)
						{
							CD cd=(CD) any;
							observation.setValue(dtt.CD2CodeableConcept(cd));
						}
						else if(any instanceof IVL_PQ)
						{
							IVL_PQ ivlpq=(IVL_PQ) any;
							observation.setValue(dtt.IVL_PQ2Range(ivlpq));
						}
						else if(any instanceof RTO)
						{
							RTO rto=(RTO) any;
							observation.setValue(dtt.RTO2Ratio(rto));
						}
						else if(any instanceof ED)
						{
							ED ed=(ED) any;
							observation.setValue(dtt.ED2Attachment(ed));
							
						}//end else if
						else if(any instanceof TS)
						{
							TS ts=(TS) any;
							if(ts.getValue().length()>12)
							{
								observation.setValue(dtt.TS2DateTime(ts));
							}//end if
							else
							{
								observation.setValue(dtt.TS2Date(ts));
							}//end else
						}//end else if
					}//END ELSE
				}//end for
			}//end if
			if(vsObs.getTargetSiteCodes()!=null && !vsObs.getTargetSiteCodes().isEmpty())
			{
				for(CD cd : vsObs.getTargetSiteCodes())
				{
					if(!cd.isSetNullFlavor())
						observation.setBodySite(dtt.CD2CodeableConcept(cd));
				}//end for
			}//end if
			
			if(vsObs.getMethodCodes()!=null && !vsObs.getMethodCodes().isEmpty())
			{
				for(CE ce : vsObs.getMethodCodes())
				{
					if(!ce.isSetNullFlavor())
					{
						CD cd= (CD) ce;
						observation.setMethod(dtt.CD2CodeableConcept(cd));
					}
				}//end for
			}//end if
			if(vsObs.getCode()!=null && !vsObs.getCode().isSetNullFlavor())
			{
				observation.setCode(dtt.CD2CodeableConcept(vsObs.getCode()));
			}//end if
			if(vsObs.getInformants()!=null && !vsObs.getInformants().isEmpty())
			{
				ArrayList <ResourceReferenceDt> myResources = new ArrayList <ResourceReferenceDt> ();
				for(Informant12 informant : vsObs.getInformants())
				{
					ResourceReferenceDt resourceReference= new ResourceReferenceDt();
					resourceReference.setReference("Practitioner/"+counter++);
					myResources.add(resourceReference);
					Practitioner practitioner=Informant2Practitioner(informant,"Practitioner/"+counter);
				}
				if(!myResources.isEmpty())
					observation.setPerformer(myResources);
			}
			
			
			/*
			if(!vsObs.getOutboundRelationships().isEmpty() && vsObs.getOutboundRelationships()!=null)
			{
				ArrayList <ReferenceRange> refRanges= new ArrayList <ReferenceRange>();
				for(ActRelationship actRelationShip : vsObs.getOutboundRelationships())
				{
					if(actRelationShip.getTypeCode()==ActRelationshipType.COMP)
					{
						if(actRelationShip.getTarget().getClassCode()==ActClass.LIST && actRelationShip.getTarget().getMoodCode()==ActMood.EVN){
							if(actRelationShip.getTarget().getRealmCodes()!=null && !actRelationShip.getTarget().getRealmCodes().isEmpty())
							{
								CodeableConceptDt cd = new CodeableConceptDt ();
								for(CS cs : actRelationShip.getTarget().getRealmCodes())
								{
									CodingDt coding = new CodingDt();
									if( cs.getCodeSystem() != null ){
						        		coding.setSystem( cs.getCodeSystem() );
						        		
						        	}
						        	if( cs.getCode() !=null ){
						        		coding.setCode( cs.getCode() );
						        	}
						        	if( cs.getCodeSystemVersion() !=null ){
						        		coding.setVersion( cs.getCodeSystemVersion() );
						        	}
						        	if( cs.getDisplayName() != null ){
						        		coding.setDisplay( cs.getDisplayName() );
						        	}
									cd.addCoding(coding);
								}//end for
								observation.setCategory(cd);
							}//end if		
						}//end if
					}//end if
					else if(actRelationShip.getTypeCode()==ActRelationshipType.REFV)
					{
						/*TODO:Need to find a way to parse and receive a time interval */
						
						/*ReferenceRange refRange= new ReferenceRange();
						if(vsObs.getValues()!=null && !vsObs.getValues().isEmpty())
						{
							for(ANY any: vsObs.getValues())
							{
								if(any instanceof IVL_PQ)
								{
									IVL_PQ myInterval= (IVL_PQ) any;
									SimpleQuantityDt simpleQuantityL=new SimpleQuantityDt();
									SimpleQuantityDt simpleQuantityH=new SimpleQuantityDt();
									
									simpleQuantityL.setValue(myInterval.getLow().getValue());
									simpleQuantityH.setValue(myInterval.getHigh().getValue());
									refRange.setLow(simpleQuantityL);
									refRange.setHigh(simpleQuantityH);
								}
								if(any instanceof ST)
								{
									ST st=(ST) any;
									refRange.setText(dtt.ST2String(st));
								}
							}
						}//end getValues if
						if(vsObs.getInterpretationCodes()!=null && !vsObs.getInterpretationCodes().isEmpty())
						{
							CodeableConceptDt cd = new CodeableConceptDt();
							for(CE ce : vsObs.getInterpretationCodes())
							{
								if(!ce.isSetNullFlavor())
								{
									CV cv= (CV) ce;
									cd.addCoding(dtt.CV2Coding(cv));
								}
								//If typecast would be succesfull then it is convenient to do that.
							}
							refRange.setMeaning(cd);
							
						}//end if
						refRanges.add(refRange);
					}//end else if
				}//end for
				observation.setReferenceRange(refRanges);
			}//end if*/
			
			/*if(vsObs.getInboundRelationships()!=null && !vsObs.getInboundRelationships().isEmpty()){
				for(ActRelationship actRelationShip : vsObs.getInboundRelationships()){
					if(actRelationShip.getTypeCode()==ActRelationshipType.COMP){
						if(actRelationShip.getSource().getClassCode()==ActClass.ENC && actRelationShip.getSource().getMoodCode()==ActMood.EVN){
							ResourceReferenceDt resourceReference= new ResourceReferenceDt();
							actRelationShip.getSource().getTypeId();
							IdentifierDt identifier=dtt.II2Identifier(actRelationShip.getSource().getTypeId());
							resourceReference.setReference(identifier.getElementSpecificId());
							observation.setEncounter(resourceReference);
							//not sure to set the true thing.
						}//end if
					}//end if
				}//end for
			}//end if*/
			
			return observation;
		}//end if
		else
		{
			return null;
		}
	}//end FHIR func


	


	@Override
	public Practitioner Performer2Practitioner(Performer2 performer,String id) {
		Practitioner practitioner = new Practitioner();
		practitioner.setId(id);
		if(performer.isSetNullFlavor())
			return null;
		else
		{
			if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor())
			{
				AssignedEntity assignedEntity = performer.getAssignedEntity();
				if(assignedEntity.getIds()!=null && !assignedEntity.getIds().isEmpty())
				{
					ArrayList <IdentifierDt> idS= new ArrayList <IdentifierDt>();
					for(II ii : assignedEntity.getIds())
					{
						idS.add(dtt.II2Identifier(ii));
					}
					practitioner.setIdentifier(idS);
				}
				
			}
			practitioner.setIdentifier();
			return null;
		}
	}
	
	
	

}
