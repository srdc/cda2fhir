package tr.com.srdc.cda2fhir.transform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.Supply;
import org.openhealthtools.mdht.uml.cda.consol.AdvanceDirectivesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.EncounterActivities;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSection;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistoryOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FamilyHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.FunctionalStatusSection;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicalEquipmentSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.NonMedicinalSupplyActivity;
import org.openhealthtools.mdht.uml.cda.consol.PayersSection;
import org.openhealthtools.mdht.uml.cda.consol.PlanOfCareSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.SocialHistorySection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignObservation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerImpl implements ICDATransformer, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int counter;
    private IdGeneratorEnum idGenerator;
    private IResourceTransformer resTransformer;
    private Reference patientRef;

    private final Logger logger = LoggerFactory.getLogger(CCDTransformerImpl.class);

    /**
     * Default constructor that initiates with a UUID resource id generator
     */
    public CCDTransformerImpl() {
        this.counter = 0;
        // The default resource id pattern is UUID
        this.idGenerator = IdGeneratorEnum.UUID;
        resTransformer = new ResourceTransformerImpl(this);
        this.patientRef = null;
    }

    /**
     * Constructor that initiates with the provided resource id generator
     * @param idGen The id generator enumeration to be set
     */
    public CCDTransformerImpl(IdGeneratorEnum idGen) {
        this();
        // Override the default resource id pattern
        this.idGenerator = idGen;
    }

    public Reference getPatientRef() {
        return patientRef;
    }
    
    public void setPatientRef(Reference patientRef) {
        this.patientRef = patientRef;
    }
    
    public synchronized String getUniqueId() {
        switch (this.idGenerator) {
            case COUNTER:
                return Integer.toString(++counter);
            case UUID:
            default:
                return UUID.randomUUID().toString();
        }
    }

    public void setIdGenerator(IdGeneratorEnum idGen) {
        this.idGenerator = idGen;
    }
    
   
    /**
     * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to be transformed
     * @param bundleType Desired type of the FHIR Bundle to be returned
     * @param patientRef Patient Reference of the given CDA Document
     * @param resourceProfileMap The mappings of default resource profiles to desired resource profiles. Used to set profile URI's of bundle entries or omit unwanted entries.
     * @return A FHIR Bundle that contains a Composition corresponding to the CCD document and all other resources but Patient that are referenced within the Composition.
     */
    public Bundle createTransactionBundle(Bundle bundle,  Map<String, String> resourceProfileMap, boolean addURLs) {
        Bundle resultBundle = new Bundle();

        for(BundleEntryComponent entry : bundle.getEntry()) {
            // Patient resource will not be added
            if (entry != null) {
                // Add request and fullUrl fields to entries
                addRequestToEntry(entry);
                if(addURLs) {
                	addFullUrlToEntry(entry);
                }
                // if resourceProfileMap is specified omit the resources with no profiles given
                // Empty profileUri means add with no change
                if (resourceProfileMap != null) {
                    String profileUri = resourceProfileMap.get(entry.getResource().getResourceType().name());
                    if (profileUri != null) {
                        if (!profileUri.isEmpty()) {
                            entry.getResource().getMeta().addProfile(profileUri);
                        }
                        resultBundle.addEntry(entry);
                    }
                } else {
                    resultBundle.addEntry(entry);
                }
            }
        }
        
        return resultBundle;
    }
    
    /**
     * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to a Bundle of corresponding FHIR resources
     * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to be transformed
     * @return A FHIR Bundle that contains a Composition corresponding to the CCD document and all other resources that are referenced within the Composition.
     * @throws Exception 
     */
    public Bundle transformDocument(String filePath, BundleType bt, Map<String, String> resourceProfileMap) throws Exception {
    	ClinicalDocument cda = getClinicalDocument(filePath);
    	Bundle bundle = transformDocument(cda, true);
    	bundle.setType(bt);
		if(bt.equals(BundleType.TRANSACTION)){
			return createTransactionBundle(bundle, resourceProfileMap, false);
		}
		return bundle;
    }

    /**
     * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to a Bundle of corresponding FHIR resources
     * @param filePath A file path string to a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) on file system
     * @return A FHIR Bundle that contains a Composition corresponding to the CCD document and all other resources that are referenced within the Composition.
     * @throws Exception 
     */
    public Bundle transformDocument(String filePath) throws Exception {
    	ClinicalDocument cda = getClinicalDocument(filePath);
    	return transformDocument(cda, true);
    }
    
    /**
     * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to a Bundle of corresponding FHIR resources
     * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to be transformed
     * @return A FHIR Bundle that contains a Composition corresponding to the CCD document and all other resources that are referenced within the Composition.
     */
    public Bundle transformDocument(ClinicalDocument cda) {
    	return transformDocument(cda, true);
    }
    
    /**
     * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to a Bundle of corresponding FHIR resources
     * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD) instance to be transformed
     * @param includeComposition Flag to include composition (required for document type bundles)
     * @return A FHIR Bundle
     */
    public Bundle transformDocument(ClinicalDocument cda, boolean includeComposition) { // TODO: Should be bundle type based.
        if(cda == null) {
            return null;
        }
        
        ContinuityOfCareDocument ccd = null;

        // first, cast the ClinicalDocument to ContinuityOfCareDocument
        try {
            ccd = (ContinuityOfCareDocument) cda;
        } catch (ClassCastException ex) {
            logger.error("ClinicalDocument could not be cast to ContinuityOfCareDocument. Returning null", ex);
            return null;
        }

        // init the global ccd bundle via a call to resource transformer, which handles cda header data (in fact, all except the sections)
        Bundle ccdBundle = resTransformer.tClinicalDocument2Bundle(ccd, includeComposition);
        
        // the first bundle entry is always the composition
        Composition ccdComposition = includeComposition ? (Composition)ccdBundle.getEntry().get(0).getResource() : null;
        
        // init the patient id reference if it is not given externally.
        if (patientRef == null) {
        	List<Patient> patients = FHIRUtil.findResources(ccdBundle, Patient.class);
        	if (patients.size() > 0) { 
        		patientRef = new Reference(patients.get(0).getId());
        	}
        } else if (ccdComposition != null) { // Correct the subject at composition with given patient reference.
            ccdComposition.setSubject(patientRef);
        }
            
        // transform the sections
        for(Section cdaSec: ccd.getSections()) {
        	
        	SectionComponent fhirSec = null; 		
        	
        	
        	//Conditional logic to only return supported sections.
        	if (cdaSec instanceof AllergiesSection || cdaSec instanceof ImmunizationsSection || cdaSec instanceof ProceduresSection || cdaSec instanceof ProblemSection || cdaSec instanceof MedicationsSection) {    		
            
        		fhirSec = resTransformer.tSection2Section(cdaSec);
        		
            	if(fhirSec == null) {
            		continue;
            	} else if (ccdComposition != null) {
                	ccdComposition.addSection(fhirSec);
            	}
            
        	}

            if(cdaSec instanceof AdvanceDirectivesSection) {

            }
            
            else if(cdaSec instanceof AllergiesSection) {
            	AllergiesSection allSec = (AllergiesSection) cdaSec;
            	for(AllergyProblemAct probAct : allSec.getAllergyProblemActs()) {
            		Bundle allBundle = resTransformer.tAllergyProblemAct2AllergyIntolerance(probAct);
                    mergeBundles(allBundle, ccdBundle, fhirSec, AllergyIntolerance.class);
            	}
            }
            /*else if(cdaSec instanceof EncountersSection) {
                EncountersSection encSec = (EncountersSection) cdaSec;
                for (EncounterActivities encAct : encSec.getConsolEncounterActivitiess()) {
                    Bundle encBundle = resTransformer.tEncounterActivity2Encounter(encAct);
                    mergeBundles(encBundle, ccdBundle, fhirSec, Encounter.class);
                }
            }*/
            /*else if(cdaSec instanceof FamilyHistorySection) {
                FamilyHistorySection famSec = (FamilyHistorySection) cdaSec;
                for(FamilyHistoryOrganizer fhOrganizer : famSec.getFamilyHistories()) {
                    FamilyMemberHistory fmh = resTransformer.tFamilyHistoryOrganizer2FamilyMemberHistory(fhOrganizer);
                    Reference ref = fhirSec.addEntry();
                    ref.setReference(fmh.getId());
                    ccdBundle.addEntry().setResource(fmh);
                }
            }*/
            /*else if(cdaSec instanceof FunctionalStatusSection) {
                FunctionalStatusSection funcSec = (FunctionalStatusSection) cdaSec;
                for(FunctionalStatusResultOrganizer funcOrganizer : funcSec.getFunctionalStatusResultOrganizers()) {
                    for(org.openhealthtools.mdht.uml.cda.Observation funcObservation : funcOrganizer.getObservations()) {
                        Bundle funcBundle = resTransformer.tFunctionalStatus2Observation(funcObservation);
                        mergeBundles(funcBundle, ccdBundle, fhirSec, Observation.class);
                    }
                }
            }*/
            else if(cdaSec instanceof ImmunizationsSection) {
            	ImmunizationsSection immSec = (ImmunizationsSection) cdaSec;
            	for(ImmunizationActivity immAct : immSec.getImmunizationActivities()) {
            		Bundle immBundle = resTransformer.tImmunizationActivity2Immunization(immAct);
                    mergeBundles(immBundle, ccdBundle, fhirSec, Immunization.class);
            	}
            }
            /*else if(cdaSec instanceof MedicalEquipmentSection) {
                MedicalEquipmentSection equipSec = (MedicalEquipmentSection) cdaSec;
                // Case 1: Entry is a Non-Medicinal Supply Activity (V2)
                for(NonMedicinalSupplyActivity supplyActivity : equipSec.getNonMedicinalSupplyActivities()) {
                	org.hl7.fhir.dstu3.model.Device fhirDevice = resTransformer.tSupply2Device(supplyActivity);
                    Reference ref = fhirSec.addEntry();
                    ref.setReference(fhirDevice.getId());
                    ccdBundle.addEntry().setResource(fhirDevice);
                }
                // Case 2: Entry is a Medical Equipment Organizer, which is indeed a collection of Non-Medicinal Supply Activity (V2)
                for(Organizer organizer : equipSec.getOrganizers()) {
                    for(Supply supply : organizer.getSupplies()) {
                        if(supply instanceof NonMedicinalSupplyActivity) {
                            org.hl7.fhir.dstu3.model.Device fhirDevice = resTransformer.tSupply2Device(supply);
                            Reference ref = fhirSec.addEntry();
                            ref.setReference(fhirDevice.getId());
                            ccdBundle.addEntry().setResource(fhirDevice);
                        }
                    }
                }
                // Case 3: Entry is a Procedure Activity Procedure (V2)
                for(org.openhealthtools.mdht.uml.cda.Procedure procedure : equipSec.getProcedures()) {
                    if(procedure instanceof ProcedureActivityProcedure) {
                        Bundle procBundle = resTransformer.tProcedure2Procedure(procedure);
                        mergeBundles(procBundle, ccdBundle, fhirSec, Procedure.class);
                    }
                }
            }*/
            else if(cdaSec instanceof MedicationsSection) {
                MedicationsSection medSec = (MedicationsSection) cdaSec;
                for(MedicationActivity medAct : medSec.getMedicationActivities()) {
                    Bundle medBundle = resTransformer.tMedicationActivity2MedicationStatement(medAct);
                    mergeBundles(medBundle, ccdBundle, fhirSec, MedicationStatement.class);
                }
            }
            else if(cdaSec instanceof PayersSection) {

            }
            else if(cdaSec instanceof PlanOfCareSection) {

            }
            else if(cdaSec instanceof ProblemSection) {
                ProblemSection probSec = (ProblemSection) cdaSec;
                for(ProblemConcernAct pcAct : probSec.getConsolProblemConcerns()) {
                    Bundle conBundle = resTransformer.tProblemConcernAct2Condition(pcAct);
                    mergeBundles(conBundle, ccdBundle, fhirSec, Condition.class);
                }
            }
            else if(cdaSec instanceof ProceduresSection) {
                ProceduresSection procSec = (ProceduresSection) cdaSec;
                Map<String, String> idedAnnotations = EMFUtil.findReferences(procSec.getText());
                List<ProcedureActivityProcedure> procs = procSec.getConsolProcedureActivityProcedures();
                for(ProcedureActivityProcedure proc : procs) {
                    Bundle procBundle = resTransformer.tProcedure2Procedure(proc, idedAnnotations);
                    mergeBundles(procBundle, ccdBundle, fhirSec, Procedure.class);
                }
            }
            /*else if(cdaSec instanceof ResultsSection) {
            	ResultsSection resultSec = (ResultsSection) cdaSec;
            	for(ResultOrganizer resOrg : resultSec.getResultOrganizers()) {
                    Bundle resBundle = resTransformer.tResultOrganizer2DiagnosticReport(resOrg);
                    mergeBundles(resBundle, ccdBundle, fhirSec, DiagnosticReport.class);
            	}
            }*/
            /*else if(cdaSec instanceof SocialHistorySection) {
                SocialHistorySection socialSec = (SocialHistorySection) cdaSec;*/
                /**
                 * The generic observation transformer should be able to transform all the possible entries:
                 *    Caregiver Characteristics
                 *    Characteristics of Home Environment
                 *    Cultural and Religious Observation
                 *    Pregnancy Observation
                 *    Smoking Status - Meaningful Use (V2)
                 *    Social History Observation (V3)
                 *    Tobacco Use (V2)
                 */
           /*     for(org.openhealthtools.mdht.uml.cda.Observation socialObs : socialSec.getObservations()) {
                    Bundle socialObsBundle = resTransformer.tObservation2Observation(socialObs);
                    mergeBundles(socialObsBundle, ccdBundle, fhirSec, Observation.class);
                }
            }*/
            /*else if(cdaSec instanceof VitalSignsSection) {
            	VitalSignsSection vitalSec = (VitalSignsSection) cdaSec;
            	for(VitalSignsOrganizer vsOrg : vitalSec.getVitalSignsOrganizers())	{
            		for(VitalSignObservation vsObs : vsOrg.getVitalSignObservations()) {
            			Bundle vsBundle = resTransformer.tVitalSignObservation2Observation(vsObs);
                        mergeBundles(vsBundle, ccdBundle, fhirSec, Observation.class);
            		}
            	}
            }*/
        }

        return ccdBundle;
    }

    /**
     * Copies all the entries from the source bundle to the target bundle, and at the same time adds a reference to the Section.Entry for each instance of the specified class
     * @param sourceBundle Source FHIR Bundle to be copied from
     * @param targetBundle Target FHIR Bundle to be copied into
     * @param fhirSec FHIR Section where the reference will be added
     * @param sectionRefCls Specific FHIR Resource Class among the resources in the sourceBundle, whose reference will be added to the FHIR Section
     */
    private void mergeBundles(Bundle sourceBundle, Bundle targetBundle, SectionComponent fhirSec, Class<?> sectionRefCls) {
    	if(sourceBundle != null) {
    		for(BundleEntryComponent entry : sourceBundle.getEntry()) {
    			if(entry != null) {
    				// Add all the resources returned from the source bundle to the target bundle
                    targetBundle.addEntry(entry);
                    // Add a reference to the section for each instance of requested class, e.g. Observation, Procedure ...
                    if(sectionRefCls.isInstance(entry.getResource())) {
                        Reference ref = fhirSec.addEntry();
                        ref.setReference(entry.getResource().getId());
                    }
    			}
            }
    	}
    }

    /**
     * Adds fullUrl field to the entry using it's resource id.
     * @param entry Entry which fullUrl field to be added.
     */
    private void addFullUrlToEntry(BundleEntryComponent entry) {
        //entry.setFullUrl("urn:uuid:" + entry.getResource().getId().getIdPart());
        entry.setFullUrl("urn:uuid:" + entry.getResource().getIdElement().getIdPart());
    }

    /**
     * Adds request field to the entry, method is POST, url is resource type.
     * @param entry Entry which request field to be added.
     */
    private void addRequestToEntry(BundleEntryComponent entry) {
        BundleEntryRequestComponent request = new BundleEntryRequestComponent();
        request.setMethod(HTTPVerb.POST);
        //request.setUrl(entry.getResource().getResourceName());
        request.setUrl(entry.getResource().getResourceType().name());
        entry.setRequest(request);
    }
    

    private ClinicalDocument getClinicalDocument(String filePath) throws Exception {
    	 FileInputStream fis = new FileInputStream(filePath);
	     ClinicalDocument cda = CDAUtil.load(fis);
	     fis.close();
	     return cda;
    }
}
