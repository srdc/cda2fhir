package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.*;
import tr.com.srdc.cda2fhir.CCDATransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

import java.util.UUID;

/**
 * Created by mustafa on 8/3/2016.
 */
public class CCDATransformerImpl implements CCDATransformer {

    private int counter;
    private IdGeneratorEnum idGenerator;
    private ResourceTransformer resTransformer;
    private ResourceReferenceDt patientRef;

    /**
     * Default constructor that initiates with a UUID resource id generator
     */
    public CCDATransformerImpl() {
        this.counter = 0;
        // The default resource id pattern is UUID
        this.idGenerator = IdGeneratorEnum.UUID;
        resTransformer = new ResourceTransformerImpl(this);
    }

    /**
     * Constructor that initiates with the provided resource id generator
     * @param idGen The id generator enumeration to be set
     */
    public CCDATransformerImpl(IdGeneratorEnum idGen) {
        this();
        // Override the default resource id pattern
        this.idGenerator = idGen;
    }

    public void setIdGenerator(IdGeneratorEnum idGen) {
        this.idGenerator = idGen;
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

    public ResourceReferenceDt getPatientRef() {
        return patientRef;
    }

    public Bundle transformCCD(ContinuityOfCareDocument ccd) {
        if(ccd == null)
            return null;

        // init the global ccd bundle via a call to resource transformer, which handles cda header data (in fact, all except the sections)
        Bundle ccdBundle = resTransformer.tClinicalDocument2Composition(ccd);
        // the first bundle entry is always the composition
        Composition ccdComposition = (Composition)ccdBundle.getEntry().get(0).getResource();
        // init the patient id reference. the patient is always the 2nd bundle entry
        patientRef = new ResourceReferenceDt(ccdBundle.getEntry().get(1).getResource().getId());

        // transform the sections
        for(Section cdaSec: ccd.getSections()) {
            Composition.Section fhirSec = resTransformer.tSection2Section(cdaSec);
            ccdComposition.addSection(fhirSec);
            if(cdaSec instanceof AdvanceDirectivesSection) {

            }
            else if(cdaSec instanceof AllergiesSection) {
            	AllergiesSection allSec = (AllergiesSection) cdaSec;
            	for(AllergyProblemAct probAct : allSec.getAllergyProblemActs()) {
            		Bundle allBundle = resTransformer.tAllergyProblemAct2AllergyIntolerance(probAct);
                    mergeBundles(allBundle, ccdBundle, fhirSec, AllergyIntolerance.class);
            	}
            }
            else if(cdaSec instanceof EncountersSection) {
                EncountersSection encSec = (EncountersSection) cdaSec;
                for (EncounterActivities encAct : encSec.getConsolEncounterActivitiess()) {
                    Bundle encBundle = resTransformer.tEncounterActivity2Encounter(encAct);
                    mergeBundles(encBundle, ccdBundle, fhirSec, Encounter.class);
                }
            }
            else if(cdaSec instanceof FamilyHistorySection) {
                FamilyHistorySection famSec = (FamilyHistorySection) cdaSec;
                for(FamilyHistoryOrganizer fhOrganizer : famSec.getFamilyHistories()) {
                    FamilyMemberHistory fmh = resTransformer.tFamilyHistoryOrganizer2FamilyMemberHistory(fhOrganizer);
                    ResourceReferenceDt ref = fhirSec.addEntry();
                    ref.setReference(fmh.getId());
                    ccdBundle.addEntry(new Bundle.Entry().setResource(fmh));
                }
            }
            else if(cdaSec instanceof FunctionalStatusSection) {
                FunctionalStatusSection funcSec = (FunctionalStatusSection) cdaSec;
                for(FunctionalStatusResultOrganizer funcOrganizer : funcSec.getFunctionalStatusResultOrganizers()) {
                    for(org.openhealthtools.mdht.uml.cda.Observation funcObservation : funcOrganizer.getObservations()) {
                        Bundle funcBundle = resTransformer.tFunctionalStatus2Observation(funcObservation);
                        mergeBundles(funcBundle, ccdBundle, fhirSec, Observation.class);
                    }
                }
            }
            else if(cdaSec instanceof ImmunizationsSection) {
            	ImmunizationsSection immSec = (ImmunizationsSection) cdaSec;
            	for(ImmunizationActivity immAct : immSec.getImmunizationActivities()) {
            		Bundle immBundle = resTransformer.tImmunizationActivity2Immunization(immAct);
                    mergeBundles(immBundle, ccdBundle, fhirSec, Immunization.class);
            	}
            }
            else if(cdaSec instanceof MedicalEquipmentSection) {

            }
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
                for(ProcedureActivityProcedure proc : procSec.getConsolProcedureActivityProcedures()) {
                    Bundle procBundle = resTransformer.tProcedure2Procedure(proc);
                    mergeBundles(procBundle, ccdBundle, fhirSec, Procedure.class);
                }
            }
            else if(cdaSec instanceof ResultsSection) {
            	ResultsSection resultSec = (ResultsSection) cdaSec;
            	for(ResultOrganizer resOrg : resultSec.getResultOrganizers()) {
                    Bundle resBundle = resTransformer.tResultOrganizer2DiagnosticReport(resOrg);
                    mergeBundles(resBundle, ccdBundle, fhirSec, DiagnosticReport.class);
            	}
            }
            else if(cdaSec instanceof SocialHistorySection) {
                SocialHistorySection socialSec = (SocialHistorySection) cdaSec;
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
                for(org.openhealthtools.mdht.uml.cda.Observation socialObs : socialSec.getObservations()) {
                    Bundle socialObsBundle = resTransformer.tObservation2Observation(socialObs);
                    mergeBundles(socialObsBundle, ccdBundle, fhirSec, Observation.class);
                }
            }
            else if(cdaSec instanceof VitalSignsSection) {
            	VitalSignsSection vitalSec = (VitalSignsSection) cdaSec;
            	for(VitalSignsOrganizer vsOrg : vitalSec.getVitalSignsOrganizers())	{
            		for(VitalSignObservation vsObs : vsOrg.getVitalSignObservations()) {
            			Bundle vsBundle = resTransformer.tVitalSignObservation2Observation(vsObs);
                        mergeBundles(vsBundle, ccdBundle, fhirSec, Observation.class);
            		}
            	}
            }
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
    private void mergeBundles(Bundle sourceBundle, Bundle targetBundle, Composition.Section fhirSec, Class<?> sectionRefCls) {
        for(Entry entry : sourceBundle.getEntry()) {
            // Add all the resources returned from the source bundle to the target bundle
            targetBundle.addEntry(entry);
            // Add a reference to the section for each instance of requested class, e.g. Observation, Procedure ...
            if(sectionRefCls.isInstance(entry.getResource())) {
                ResourceReferenceDt ref = fhirSec.addEntry();
                ref.setReference(entry.getResource().getId());
            }
        }
    }
}
