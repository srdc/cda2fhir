package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Guardian;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationInformation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CR;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;

import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient.Contact;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;
import tr.com.srdc.cda2fhir.impl.ValueSetsTransformerImpl;

public class ResourceTransformerImplIsmail {

	private static int idHolder = 0;
	private static int getUniqueId(){
		return idHolder++;
	}
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	ValueSetsTransformer vst = new ValueSetsTransformerImpl();

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public List<Condition> ProblemConcernAct2Condition(ProblemConcernAct probAct) {
		
		if( probAct == null || probAct.isSetNullFlavor()) return null;
		
		List<Condition> conditionList = new ArrayList<Condition>();
		
		for(EntryRelationship entryRelationship : probAct.getEntryRelationships())
		{	
			Condition condition = new Condition();
			List<IdentifierDt> IdList = new ArrayList<IdentifierDt>();
		
			if( probAct.getIds() != null & !probAct.getIds().isEmpty() ){
				
				IdentifierDt identifier = dtt.II2Identifier( probAct.getIds().get(0) );
				IdList.add(identifier);
				
			}
			condition.setIdentifier( IdList );
			
			ResourceReferenceDt resourceReferencePatient = new ResourceReferenceDt();
			//IdentifierDt identifierPatient = dtt.II2Identifier( probAct.getSubject().getRole().getPlayer().getTypeId());
			resourceReferencePatient.setReference( "patient:" + getUniqueId() );
			condition.setPatient( resourceReferencePatient );
			
			ResourceReferenceDt resourceReferenceEncounter = new ResourceReferenceDt();
			//IdentifierDt identifierEncounter = dtt.II2Identifier( 
					//probAct.getEncounters().get(0).getInboundRelationships().get(0).getSource().getTypeId());
			resourceReferenceEncounter.setReference( "encounter:" + getUniqueId() );
			condition.setEncounter( resourceReferenceEncounter );
			
			ResourceReferenceDt resourceReferenceAsserter = new ResourceReferenceDt();
			//IdentifierDt identifierAsserter = dtt.II2Identifier( probAct.getAuthors().get(0).getRole().getPlayer().getTypeId());
			resourceReferenceAsserter.setReference( "asserter:" + getUniqueId() ); 
			condition.setAsserter( resourceReferenceAsserter );
			
			
			
			
			//DATE-RECORDED
			DateDt dateRecorded = dtt.TS2Date(entryRelationship.getObservation().getAuthors().get(0).getTime());
			condition.setDateRecorded( dateRecorded );
			/////
		
			
			
			/*if( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName().equals("Problem") ){
				
			codingForSetCode.setCode( cd.getTranslations().get(0).getCode() );
			codingForSetCode.setDisplay( cd.getTranslations().get(0).getDisplayName() );
			codingForSetCode.setSystem( oid2Url( cd.getTranslations().get(0).getCodeSystem() ) );
			
			codingForCategory.setCode( probAct.getEntryRelationships().get(0).getObservation().getCode().getCode() );
			codingForCategory.setDisplay( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( oid2Url( probAct.getEntryRelationships().get(0).getObservation().getCode().getCodeSystem() ) );
			
			codingForCategory2.setCode( "finding" );
			codingForCategory2.setDisplay( "Finding" );
			codingForCategory2.setSystem( "http://hl7.org/fhir/condition-category" );
			
			boundCodeableConceptDt.addCoding( codingForCategory );
			boundCodeableConceptDt.addCoding( codingForCategory2 );
			codeableConcept.addCoding( codingForSetCode );
			
			}*/
			
			//CODE AND CATEGORY
			CodingDt codingForCategory = new CodingDt();
			//CodingDt codingForCategory2 = new CodingDt();
			CodingDt codingForSetCode = new CodingDt();
			CodeableConceptDt codeableConcept = new CodeableConceptDt();
			BoundCodeableConceptDt boundCodeableConceptDt = new BoundCodeableConceptDt();
			//TODO: VALIDATE: Casting ANY to CD.
			if( entryRelationship.getObservation().getValues() != null ){
				CD cd = (CD) ( entryRelationship.getObservation().getValues().get(0) );
	
				codingForSetCode.setCode( cd.getCode() );
				codingForSetCode.setDisplay( cd.getDisplayName() );
				codingForSetCode.setSystem( vst.oid2Url( cd.getCodeSystem() ) );
				codeableConcept.addCoding( codingForSetCode );
			}
			
			codingForCategory.setCode( entryRelationship.getObservation().getCode().getCode() );
			codingForCategory.setDisplay(  entryRelationship.getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( vst.oid2Url( entryRelationship.getObservation().getCode().getCodeSystem() ) );
			boundCodeableConceptDt.addCoding( codingForCategory );
			
			condition.setCategory( boundCodeableConceptDt );
			condition.setCode( codeableConcept);
			/////
			
		
			
			//ONSET and ABATEMENT
			// It is not clear which effectiveTime getter to call: 
			//                  ...getObservation().getEffectiveTime()  OR  probAct.getEffectiveTime()
			if(entryRelationship.getObservation().getEffectiveTime() != null || 
					!entryRelationship.getObservation().getEffectiveTime().isSetNullFlavor())
			{
				PeriodDt period = dtt.IVL_TS2Period(entryRelationship.getObservation().getEffectiveTime());
				DateTimeDt dateStart = new DateTimeDt();
				DateTimeDt dateEnd = new DateTimeDt();
				if(period.getStart() != null)
				{
					dateStart.setValue( period.getStart() );
					
				}
		        
				if(period.getEnd() != null )
				{
			        dateEnd.setValue( period.getEnd() );
			        
				}
			    
				condition.setOnset(  dateStart );
				condition.setAbatement(  dateEnd );
			}
	        
	        
	        
	        // BODYSITE, SEVERITY and NOTES
	        CodeableConceptDt codeableConceptBodySite = new CodeableConceptDt();
	        //CodeableConceptDt codeableConceptSeverity = new CodeableConceptDt();
	        CodingDt codingBodysite = new CodingDt();
	        //CodingDt codingSeverity = new CodingDt();
	        
	        if( entryRelationship.getObservation().getValues() != null ){
				CD cd = (CD) ( entryRelationship.getObservation().getValues().get(0) );
				
		        if(cd.getQualifiers() != null && !cd.getQualifiers().isEmpty()){
		        	for(CR cr : cd.getQualifiers()){
				        if(cr.getName().getDisplayName().toLowerCase().equals("finding site")){
				        	
				        	codingBodysite.setDisplay( cr.getValue().getDisplayName() );
				        	codingBodysite.setCode( cr.getValue().getCode() );
				        	
				        }
//				        if(cr.getName().getDisplayName().toLowerCase().equals("problem severity")) {
//				        	
//				        	codingSeverity.setCode( cr.getValue().getCode() );
//				        	codingSeverity.setDisplay( cr.getValue().getDisplayName() );
//				        	
//				        }
		        	}
		        	
		        	//Notes
//		        	for(CD cdInner : cd.getTranslations() ){
//		        		
//		        		if(cdInner.getDisplayName().toLowerCase().equals("annotation")){
//		        			//TODO : What to write in Notes.
//		        			conditionList[i].setNotes( cdInner.getCode() );
//		        			
//		        		}
//		        		
//		        	}
		        		
		        }
	        }
	        
	        codeableConceptBodySite.addCoding( codingBodysite );
	        //codeableConceptSeverity.addCoding( codingSeverity );
	        condition.addBodySite(codeableConceptBodySite);
	        //condition.setSeverity(codeableConceptSeverity);
	        ////
	        
	        
	      //VERIFICATION_STATUS
//	        BoundCodeDt boundcodeVerif = new BoundCodeDt();
//	       
//	        for ( CR cr :entryRelationship.getObservation().getCode().getQualifiers()){
//	        	if(cr.getName().getDisplayName().toLowerCase().equals("verification status")
//	        			  || cr.getName().getDisplayName().toLowerCase().equals("verificationstatus")
//	        			  || cr.getName().getDisplayName().toLowerCase().equals("verification")
//	        				)
//	        	{
//	        		
//	        		boundcodeVerif.setValueAsString( cr.getValue().getDisplayName() );
//	        		
//	        	}
//	        }
	        
	        
	        
	        //TODO: Stage type does not exist.
	        conditionList.add(condition);
	        
		}
		
		return conditionList;
	}

	
	

	@SuppressWarnings("deprecation")
	public Medication Medication2Medication(MedicationInformation manPro) {
		
		if( manPro == null || manPro.isSetNullFlavor() ) return null;
		
		Medication medication = new Medication();
		
		// TODO : VALIDATE: manufacturedMaterial.code IS USED INSTEAD OF medication.code ( which does not exist).
		//CODE
		CodeableConceptDt codeableConcept = new CodeableConceptDt();
		CodingDt coding = new CodingDt();
		if(manPro.getManufacturedMaterial() != null & manPro.getManufacturedMaterial().getCode() != null
				& !manPro.getManufacturedMaterial().getCode().isSetNullFlavor()){
			
			CE ce = manPro.getManufacturedMaterial().getCode();
			if(ce.getCode() != null )
				coding.setCode( ce.getCode());
			if( ce.getDisplayName() != null )
			coding.setDisplay( ce.getDisplayName() );
			if( ce.getCodeSystem() != null )
				coding.setSystem( vst.oid2Url( ce.getCodeSystem() ) );
			if( ce.getCodeSystemVersion() != null )
				coding.setVersion( ce.getCodeSystemVersion() );
			
		}
		codeableConcept.addCoding(coding);
		for( CD cd : manPro.getManufacturedMaterial().getCode().getTranslations() )
		{
			CodingDt codingTrans = new CodingDt();
			if(cd.getCode() != null )
				codingTrans.setCode( cd.getCode());
			if( cd.getDisplayName() != null )
				codingTrans.setDisplay( cd.getDisplayName() );
			if( cd.getCodeSystem() != null )
				codingTrans.setSystem( vst.oid2Url( cd.getCodeSystem() ) );
			if( cd.getCodeSystemVersion() != null )
				codingTrans.setVersion( cd.getCodeSystemVersion() );
			codeableConcept.addCoding(codingTrans);
		}
		
		medication.setCode( codeableConcept );
		
		
		//IS_BRAND and MANUFACTURER
		ResourceReferenceDt resourceReferenceManu = new ResourceReferenceDt();
		if( manPro.getManufacturerOrganization() != null ){
			
			if( !manPro.getManufacturerOrganization().isSetNullFlavor()  )
			{
				medication.setIsBrand(true);
				
				//MANUFACTURER
				
				if( manPro.getManufacturerOrganization().getIds() != null ){
					if(manPro.getManufacturerOrganization().getIds().size() != 0){
						IdentifierDt identifierManu = dtt.II2Identifier( manPro.getManufacturerOrganization().getIds().get(0) );
						resourceReferenceManu.setReference( identifierManu.getId() );
					}
				}
				if( manPro.getManufacturerOrganization().getNames() != null){
					if( manPro.getManufacturerOrganization().getNames().size() != 0 )
						resourceReferenceManu.setDisplay( manPro.getManufacturerOrganization().getNames().get(0).getText() );
				}
				
			}
			else
			{
				medication.setIsBrand(false);
			}
		}else{
			medication.setIsBrand(false);
		}
		medication.setManufacturer( resourceReferenceManu );
		
		
		
		// IN CDA, STRENTGH, DOSE AND FORM ATTRIBUTES ARE Pre-COORDINATED INTO THE 'manufacturedMaterial.code' 
		//TODO Product. 
		//TODO Package.
		
		
		return medication;
	}

	public MedicationStatement MedicationActivity2MedicationStatement(
			MedicationActivity subAd) {
		
		if( subAd == null || subAd.isSetNegationInd() ) return null;
		
		if (subAd.getMoodCode() != x_DocumentSubstanceMood.EVN ) return null;
		
		//IDENNTIFER
		MedicationStatement medSt = new MedicationStatement();
		List<IdentifierDt> idList = new ArrayList<IdentifierDt>();
		medSt.setIdentifier( idList );
		if( subAd.getIds().isEmpty() == false){
			IdentifierDt identifier = dtt.II2Identifier( subAd.getIds().get(0) );
			medSt.addIdentifier( identifier ); 
		}
		//STATUS
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		medSt.setStatus( vst.StatusCode2MedicationStatementStatusEnum( subAd.getStatusCode().getDisplayName()) );
		
		
		//PATIENT
		
		//PRACTITIONER
//		if( subAd.getPerformers() != null && !subAd.getPerformers().isEmpty() ){
//			
//			if( !subAd.getPerformers().get(0).isSetNullFlavor() ){
//				
//				ResourceReferenceDt refPerf = new ResourceReferenceDt();
//				refPerf.setReference( "practitioner:" + getUniqueId() );
//				medSt.setPractitioner( refPerf );
//			}
//			
//		}
		
		
		//DOSAGE
		CodeableConceptDt route = new CodeableConceptDt();
		CodingDt routeCoding = new CodingDt();
		if( subAd.getRouteCode() != null && !subAd.getRouteCode().isSetNullFlavor() ){
			
			if( subAd.getRouteCode().getCode() != null )
				routeCoding.setCode( subAd.getRouteCode().getCode() );
			if( subAd.getRouteCode().getDisplayName() != null )
				routeCoding.setDisplay( subAd.getRouteCode().getDisplayName() );
			if( subAd.getRouteCode().getCodeSystem() != null )
				routeCoding.setSystem( vst.oid2Url( subAd.getRouteCode().getCodeSystem() ) );
			if( subAd.getRouteCode().getCodeSystemVersion() != null ){
				routeCoding.setVersion( subAd.getRouteCode().getCodeSystemVersion() );
			}
			
			route.addCoding( routeCoding );
			medSt.getDosage().get(0).setRoute( route );
			
			
		}
		
		SimpleQuantityDt quantityDose = new SimpleQuantityDt();
		if( subAd.getDoseQuantity() != null && !subAd.getDoseQuantity().isSetNullFlavor() ){
			
			quantityDose.setValue( subAd.getDoseQuantity().getValue() );
			if( subAd.getDoseQuantity().getUnit() != null )
				quantityDose.setUnit( subAd.getDoseQuantity().getUnit() );
			
		}
		medSt.getDosage().get(0).setQuantity( quantityDose );
		
		RangeDt rate = dtt.IVL_PQ2Range( subAd.getRateQuantity() );
		medSt.getDosage().get(0).setRate( rate );
		////DOSAGE END
		
		
		PeriodDt period = new PeriodDt();
		if( subAd.getEffectiveTimes().get(0) != null ){
			
			period = dtt.IVL_TS2Period( (IVL_TS) subAd.getEffectiveTimes().get(0) );
			
		}
		medSt.setEffective( period );
		
		
			
		return null;
	}

	public MedicationDispense MedicationDispense2MedicationDispense(org.openhealthtools.mdht.uml.cda.consol.MedicationDispense sup) {
		
		if( sup == null || sup.isSetNullFlavor() ) return null;
		
		if( sup.getMoodCode().getLiteral() == "EVN" ){
			
			MedicationDispense meDis = new MedicationDispense();
			
			if( sup.getIds() != null &  !sup.getIds().isEmpty() )
				meDis.setIdentifier( dtt.II2Identifier( sup.getIds().get(0) ) );
			
			ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
			meDis.setStatus( vst.StatusCode2MedicationDispenseStatusEnum( sup.getStatusCode().getDisplayName() ) );
			
			ResourceReferenceDt performerRef = new ResourceReferenceDt();
			performerRef.setReference( "practitioner:" + getUniqueId() );
			meDis.setDispenser( performerRef );
			
			
			CodeableConceptDt type = new CodeableConceptDt();
			CodingDt coding = new CodingDt();
			if(sup.getCode() != null & !sup.getCode().isSetNullFlavor()){
				
				CD cd = sup.getCode();
				if(cd.getCode() != null )
					coding.setCode( cd.getCode() );
				if(cd.getDisplayName() != null )
					coding.setDisplay( cd.getDisplayName() );
				if( cd.getCodeSystem() != null )
					coding.setSystem( vst.oid2Url( cd.getCodeSystem() ) );
				if( cd.getCodeSystemVersion() != null )
					coding.setVersion( cd.getCodeSystemVersion() );
				
				 for( CD trans : cd.getTranslations() ){
					
					CodingDt codingTr  = new CodingDt();
					
					if(trans.getCode() != null )
						codingTr.setCode( trans.getCode() );
					if(trans.getDisplayName() != null )
						codingTr.setDisplay( trans.getDisplayName() );
					if( trans.getCodeSystem() != null )
						codingTr.setSystem( vst.oid2Url( trans.getCodeSystem() ) );
					if( trans.getCodeSystemVersion() != null )
						codingTr.setVersion( trans.getCodeSystemVersion() );
					
					type.addCoding( codingTr );
				}
			}
			
			type.addCoding( coding );
			meDis.setType( type );
			
			SimpleQuantityDt quantity = new SimpleQuantityDt();
			if( sup.getQuantity().getValue() != null )
				quantity.setValue( sup.getQuantity().getValue() );
			if( sup.getQuantity().getUnit() != null)
				quantity.setUnit( sup.getQuantity().getUnit() );
			meDis.setQuantity( quantity );
			
			ResourceReferenceDt medRef = new ResourceReferenceDt();
			medRef.setReference( "medication:" + getUniqueId() );
			medRef.setDisplay( sup.getProduct().getManufacturedProduct().getManufacturedMaterial().getName().getText() );
			meDis.setMedication( medRef );
			
			DateTimeDt prepDate = new DateTimeDt();
			if( sup.getEffectiveTimes() != null & sup.getEffectiveTimes().size() != 0 ){
				prepDate.setValueAsString( sup.getEffectiveTimes().get(0).getValue() );
				
				if(sup.getEffectiveTimes().get(1) != null ){
					DateTimeDt handDate = new DateTimeDt();
					handDate.setValueAsString( sup.getEffectiveTimes().get(1).getValue() );
					meDis.setWhenHandedOver(handDate);
				}
				
			}
			meDis.setWhenPrepared( prepDate );
			
			// TODO : No dosageInstruction and Patient section exists in CCD example.
			meDis.setDosageInstruction(null);
			
			
			
			return meDis;
		}
		
		return null;
	}



}
