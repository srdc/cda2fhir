package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Informant12;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.consol.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.impl.ReferenceRangeImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
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
import ca.uhn.fhir.model.dstu2.resource.Practitioner.PractitionerRole;
import ca.uhn.fhir.model.dstu2.resource.Procedure.Performer;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;

public class ResourceTransformerImplTahsin implements ResourceTransformer {
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	private static int idHolder=0;
	private static int getUniqueId(){
		return idHolder++;
	}
	public Observation VitalSignObservation2Observation(VitalSignObservation vsObs) {
		if(vsObs!=null && !vsObs.isSetNullFlavor()){
			Observation observation= new Observation();
			observation.setId("Observation/"+ getUniqueId());
			
			if(vsObs.getIds()!=null & !vsObs.getIds().isEmpty())
			{
				for(II myIds : vsObs.getIds())
				{
					observation.addIdentifier(dtt.II2Identifier(myIds));
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

			return observation;
		}//end if
		else
		{
			return null;
		}
	}//end FHIR func

	@Override
	public Observation ResultObservation2Observation(ResultObservation resObs) {
		if(resObs!=null &&  !resObs.isSetNullFlavor())
		{
			Observation observation= new Observation();
			observation.setId("Observation/"+getUniqueId());
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
			if(resObs.getStatusCode()!=null && !resObs.getStatusCode().isSetNullFlavor())
			{
				if(resObs.getStatusCode().getCode().equals("completed"))
				{
					observation.setStatus(ObservationStatusEnum.FINAL);
				}//end if
			}//end if
			if(resObs.getCode()!=null && !resObs.getCode().isSetNullFlavor())
			{
				observation.setCode(dtt.CD2CodeableConcept(resObs.getCode()));
			}//end if
			if(resObs.getTargetSiteCodes()!=null && !resObs.getTargetSiteCodes().isEmpty())
			{
				for(CD cd : resObs.getTargetSiteCodes())
				{
					if(!cd.isSetNullFlavor())
						observation.setBodySite(dtt.CD2CodeableConcept(cd));
				}//end for
			}//end if
			
			if(resObs.getMethodCodes()!=null && !resObs.getMethodCodes().isEmpty())
			{
				for(CE ce : resObs.getMethodCodes())
				{
					if(!ce.isSetNullFlavor())
					{
						CD cd= (CD) ce;
						observation.setMethod(dtt.CD2CodeableConcept(cd));
					}
				}//end for
			}//end if
			if(resObs.getInterpretationCodes()!=null && !resObs.getInterpretationCodes().isEmpty())
			{
				for(CE ce: resObs.getInterpretationCodes())
				{
					CD cd=(CD) ce;
					observation.setInterpretation(dtt.CD2CodeableConcept(cd));
				}//end for
			}//end if
			if(resObs.getAuthors()!=null && !resObs.getAuthors().isEmpty())
			{
				for(Author author : resObs.getAuthors())
				{
					if(author.getTime()!=null && !author.getTime().isSetNullFlavor())
					{
						observation.setIssued(dtt.TS2Instant(author.getTime()));
					}//end if
				}//end for
			}//end if
			if(!resObs.getEffectiveTime().isSetNullFlavor() && resObs.getEffectiveTime()!=null)
			{
				if(resObs.getEffectiveTime().getValue()!=null)
				{
					TS ts=DatatypesFactory.eINSTANCE.createTS();
					ts.setValue(resObs.getEffectiveTime().getValue());
					observation.setEffective(dtt.TS2DateTime(ts));
				}
				else
				{
					observation.setEffective(dtt.IVL_TS2Period(resObs.getEffectiveTime()));
				}
			}//end if
			if(!resObs.getValues().isEmpty() && resObs.getValues()!=null)
			{
				for(ANY any : resObs.getValues())
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
			if(resObs.getReferenceRanges()!=null && !resObs.getReferenceRanges().isEmpty())
			{
				for(org.openhealthtools.mdht.uml.cda.ReferenceRange CDArefRange : resObs.getReferenceRanges())
				{
					ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange	FHIRrefRange = new ca.uhn.fhir.model.dstu2.resource.Observation.ReferenceRange ();
					FHIRrefRange.setText(CDArefRange.getObservationRange().getText().getText());
				}//end for
			}//end if
			
			if(resObs.getPerformers()!=null && !resObs.getPerformers().isEmpty())
			{
				for(Performer2 performer : resObs.getPerformers())
				{
					if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor())
					{
						Practitioner practitioner=Performer2Practitioner(performer,getUniqueId());
					}
				}
			}//end if
			return observation;
		}//end if
		else
			return null;
	}//end function
	@Override
	public Practitioner Performer2Practitioner(Performer2 performer, int id) {
		Practitioner practitioner = new Practitioner();
		practitioner.setId("Practitioner/"+id);
		if(performer.getAssignedEntity()!=null && !performer.getAssignedEntity().isSetNullFlavor())
		{
			AssignedEntity assignedEntity= performer.getAssignedEntity();
			if(assignedEntity.getIds()!=null && !performer.getAssignedEntity().isSetNullFlavor())
			{
				ArrayList <IdentifierDt> idS = new ArrayList <IdentifierDt> ();
				for(II ii : assignedEntity.getIds())
				{
					idS.add(dtt.II2Identifier(ii));
				}//end for
				practitioner.setIdentifier(idS);
			}//end if
			if(assignedEntity.getAddrs()!=null && !performer.getAssignedEntity().getAddrs().isEmpty())
			{
				for(AD ad : assignedEntity.getAddrs())
				{
					practitioner.addAddress(dtt.AD2Address(ad));
				}//end for
			}//end if
			if(assignedEntity.getTelecoms()!=null && !assignedEntity.getTelecoms().isEmpty())
			{
				for(TEL tel : assignedEntity.getTelecoms())
				{
					practitioner.addTelecom(dtt.TEL2ContactPoint(tel));
				}//end for
			}//end if
			if(assignedEntity.getAssignedPerson()!=null && !assignedEntity.getAssignedPerson().isSetNullFlavor())
			{
				Person person=assignedEntity.getAssignedPerson();
				if(person.getNames()!=null && !person.getNames().isEmpty())
				{
					for(PN pn : person.getNames())
					{
						EN en =(EN) pn;
						practitioner.setName(dtt.EN2HumanName(en));
					}//end for
				}//end if
			}//end if
			if(assignedEntity.getRepresentedOrganizations()!=null && !assignedEntity.getRepresentedOrganizations().isEmpty())
			{
				ArrayList <PractitionerRole> prRoles= new ArrayList <PractitionerRole>();
				for(Organization organization : assignedEntity.getRepresentedOrganizations())
				{
					PractitionerRole prRole= new PractitionerRole();
					ResourceReferenceDt resourceReference = new ResourceReferenceDt();
					int newId=getUniqueId();
					resourceReference.setReference("Organization/" + newId);
					prRole.setManagingOrganization(resourceReference);
					prRoles.add(prRole);
					ca.uhn.fhir.model.dstu2.resource.Organization FHIROrganization = Organization2Organization(organization,newId);
				}//end for
				
				practitioner.setPractitionerRole(prRoles);
			}
			
		}//end assignedEntity if
		return null;
	}
	
	
}	
	
