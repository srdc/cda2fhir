package tr.com.srdc.cda2fhir.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CR;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.rim.Entity;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.Medication.Product;
import ca.uhn.fhir.model.dstu2.resource.Medication.Package;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.ConditionCategoryCodesEnum;
import ca.uhn.fhir.model.primitive.BoundCodeDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import tr.com.srdc.cda2fhir.ResourceTransformer;
import tr.com.srdc.cda2fhir.DataTypesTransformer;
import tr.com.srdc.cda2fhir.ValueSetsTransformer;
import tr.com.srdc.cda2fhir.impl.ValueSetsTransformerImpl;

public class ResourceTransformerImplIsmail implements ResourceTransformer {
	
	static int counter = 0;
	DataTypesTransformer dtt = new DataTypesTransformerImpl();
	
	@Override
	public Patient PatientRole2Patient(PatientRole patRole) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	@Override
	public List<Condition> ProblemConcernAct2Condition(ProblemConcernAct probAct) {
		
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
			resourceReferencePatient.setReference( "patient/" + counter );
			condition.setPatient( resourceReferencePatient );
			
			ResourceReferenceDt resourceReferenceEncounter = new ResourceReferenceDt();
			//IdentifierDt identifierEncounter = dtt.II2Identifier( 
					//probAct.getEncounters().get(0).getInboundRelationships().get(0).getSource().getTypeId());
			resourceReferenceEncounter.setReference( "encounter/" + counter );
			condition.setEncounter( resourceReferenceEncounter );
			
			ResourceReferenceDt resourceReferenceAsserter = new ResourceReferenceDt();
			//IdentifierDt identifierAsserter = dtt.II2Identifier( probAct.getAuthors().get(0).getRole().getPlayer().getTypeId());
			resourceReferenceAsserter.setReference( "asserter/" + counter ); 
			condition.setAsserter( resourceReferenceAsserter );
			
			counter++;
			
			
			//DATE-RECORDED
			DateDt dateRecorded = dtt.TS2Date(entryRelationship.getObservation().getAuthors().get(0).getTime());
			condition.setDateRecorded( dateRecorded );
			/////
		
			
			
			/*if( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName().equals("Problem") ){
				
			codingForSetCode.setCode( cd.getTranslations().get(0).getCode() );
			codingForSetCode.setDisplay( cd.getTranslations().get(0).getDisplayName() );
			codingForSetCode.setSystem( codeSystem2System( cd.getTranslations().get(0).getCodeSystem() ) );
			
			codingForCategory.setCode( probAct.getEntryRelationships().get(0).getObservation().getCode().getCode() );
			codingForCategory.setDisplay( probAct.getEntryRelationships().get(0).getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( codeSystem2System( probAct.getEntryRelationships().get(0).getObservation().getCode().getCodeSystem() ) );
			
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
				codingForSetCode.setSystem( codeSystem2System( cd.getCodeSystem() ) );
				codeableConcept.addCoding( codingForSetCode );
			}
			
			codingForCategory.setCode( entryRelationship.getObservation().getCode().getCode() );
			codingForCategory.setDisplay(  entryRelationship.getObservation().getCode().getDisplayName() );
			codingForCategory.setSystem( codeSystem2System( entryRelationship.getObservation().getCode().getCodeSystem() ) );
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
	        CodeableConceptDt codeableConceptSeverity = new CodeableConceptDt();
	        CodingDt codingBodysite = new CodingDt();
	        CodingDt codingSeverity = new CodingDt();
	        
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

	@Override
	public Observation ResultObservation2Observation(ResultObservation resObs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observation VitalSignObservation2Observation(
			VitalSignObservation vsObs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String codeSystem2System(String codeSystem){
		String system = null;
		switch (codeSystem) {
        case "2.16.840.1.113883.6.96":
            system = "http://snomed.info/sct";
            break;
        case "2.16.840.1.113883.6.88":
            system = "http://www.nlm.nih.gov/research/umls/rxnorm";
            break;
        case "2.16.840.1.113883.6.1":
            system = "http://loinc.org";
            break;
        case "2.16.840.1.113883.6.8":
            system = "http://unitsofmeasure.org";
            break;
        case "2.16.840.1.113883.3.26.1.2":
            system = "http://ncimeta.nci.nih.gov";
            break;
        case "2.16.840.1.113883.6.12":
            system = "http://www.ama-assn.org/go/cpt";
            break;
        case "2.16.840.1.113883.6.209":
            system = "http://hl7.org/fhir/ndfrt";
            break;
        case "2.16.840.1.113883.4.9":
            system = "http://fdasis.nlm.nih.gov";
            break;
        case "2.16.840.1.113883.12.292":
            system = "http://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=cvx";
            break;
        case "1.0.3166.1.2.2":
            system = "urn:iso:std:iso:3166";
            break;
        case "2.16.840.1.113883.6.301.5":
            system = "http://www.nubc.org/patient-discharge";
            break;
        case "2.16.840.1.113883.6.256":
            system = "http://www.radlex.org";
            break;
        case "2.16.840.1.113883.6.3":
            system = "http://hl7.org/fhir/sid/icd-10";
            break;
        case "2.16.840.1.113883.6.4":
            system = "http://www.icd10data.com/icd10pcs";
            break;
        case "2.16.840.1.113883.6.42":
            system = "http://hl7.org/fhir/sid/icd-9";
            break;
        case "2.16.840.1.113883.6.73":
            system = "http://www.whocc.no/atc";
            break;
        case "2.16.840.1.113883.6.24":
            system = "urn:std:iso:11073:10101";
            break;
        case "1.2.840.10008.2.16.4":
            system = "http://nema.org/dicom/dicm";
            break;
        case "2.16.840.1.113883.6.281":
            system = "http://www.genenames.org";
            break;
        case "2.16.840.1.113883.6.280":
            system = "http://www.ncbi.nlm.nih.gov/nuccore";
            break;
        case "2.16.840.1.113883.6.282":
            system = "http://www.hgvs.org/mutnomen";
            break;
        case "2.16.840.1.113883.6.284":
            system = "http://www.ncbi.nlm.nih.gov/projects/SNP";
            break;
        case "2.16.840.1.113883.3.912":
            system = "http://cancer.sanger.ac.uk/cancergenome/projects/cosmic";
            break;
        case "2.16.840.1.113883.6.283":
            system = "http://www.hgvs.org/mutnomen";
            break;
        case "2.16.840.1.113883.6.174":
            system = "http://www.omim.org";
            break;
        case "2.16.840.1.113883.13.191":
            system = "http://www.ncbi.nlm.nih.gov/pubmed";
            break;
        case "2.16.840.1.113883.3.913":
            system = "http://www.pharmgkb.org";
            break;
        case "2.16.840.1.113883.3.1077":
            system = "http://clinicaltrials.gov";
            break;

        default:
            system = "urn:oid:" + codeSystem;
            break;
        }
		return system;
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public Medication ManufacturedProduct2Medication(ManufacturedProduct manPro) {
		
		
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
				coding.setSystem( codeSystem2System( ce.getCodeSystem() ) );
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
				codingTrans.setSystem( codeSystem2System( cd.getCodeSystem() ) );
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
		
		
		
		
		//TODO Product.
		//TODO Package.
		
		
		return medication;
	}

	@Override
	public MedicationAdministration SubstanceAdministration2MedicationAdministration(
			SubstanceAdministration subAd) {
		
		MedicationAdministration medAd = new MedicationAdministration();
		List<IdentifierDt> idList = new ArrayList<IdentifierDt>();
		medAd.setIdentifier( idList );
		if( subAd.getIds().isEmpty() == false){
			IdentifierDt identifier = dtt.II2Identifier( subAd.getIds().get(0) );
			medAd.addIdentifier( identifier ); 
		}
		//MedicationAdministrationStatusEnum
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		medAd.setStatus( vst.StatusCode2MedicationAdministrationStatusEnum( subAd.getStatusCode().getDisplayName()) );
		
		// TODO : Complete.
			
		return null;
	}

	@Override
	public MedicationDispense Supply2MedicationDispense(Supply sup) {
		
		if( sup.getMoodCode().getLiteral() == "EVN" ){
			
			MedicationDispense meDis = new MedicationDispense();
			
			if( sup.getIds() != null &  !sup.getIds().isEmpty() )
				meDis.setIdentifier( dtt.II2Identifier( sup.getIds().get(0) ) );
			
			ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
			meDis.setStatus( vst.StatusCode2MedicationADispenseStatusEnum( sup.getStatusCode().getDisplayName() ) );
			
			ResourceReferenceDt performerRef = new ResourceReferenceDt();
			performerRef.setReference( "practitioner/" + counter );
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
					coding.setSystem( codeSystem2System( cd.getCodeSystem() ) );
				if( cd.getCodeSystemVersion() != null )
					coding.setVersion( cd.getCodeSystemVersion() );
				
				 for( CD trans : cd.getTranslations() ){
					
					CodingDt codingTr  = new CodingDt();
					
					if(trans.getCode() != null )
						codingTr.setCode( trans.getCode() );
					if(trans.getDisplayName() != null )
						codingTr.setDisplay( trans.getDisplayName() );
					if( trans.getCodeSystem() != null )
						codingTr.setSystem( codeSystem2System( trans.getCodeSystem() ) );
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
			medRef.setReference( "medication/" + counter );
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
			
			// TODO : No dosage section exists in CCD example.
			meDis.setDosageInstruction(null);
						
		}
		
		return null;
	}

	
	
	
	
}
