package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.*;
import tr.com.srdc.cda2fhir.CCDATransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;

import java.util.UUID;

/**
 * Created by mustafa on 8/3/2016.
 */
public class CCDATransformerImpl implements CCDATransformer {

    private int counter;
    private IdGeneratorEnum idGenerator;
    private ResourceTransformer resTransformer;
    private IdDt patientId;

    public CCDATransformerImpl() {
        this.counter = 0;
        // The default resource id pattern is UUID
        this.idGenerator = IdGeneratorEnum.UUID;
        resTransformer = new ResourceTransformerImpl(this);
    }

    public CCDATransformerImpl(IdGeneratorEnum idGen) {
        this();
        this.idGenerator = idGen;
    }

    @Override
    public void setIdGenerator(IdGeneratorEnum idGen) {
        this.idGenerator = idGen;
    }

    @Override
    public synchronized String getUniqueId() {
        switch (this.idGenerator) {
            case COUNTER:
                return "" + (++counter);
            case UUID:
            default:
                return UUID.randomUUID().toString();
        }
    }

    @Override
    public IdDt getPatientId() {
        return patientId;
    }

    @Override
    public Bundle transformCCD(ContinuityOfCareDocument ccd) {
        if(ccd == null)
            return null;

        // create and init the global bundle and the composition resources
        Bundle ccdBundle = new Bundle();
        Composition ccdComposition = new Composition();
        ccdComposition.setId(new IdDt("Composition", getUniqueId()));
        ccdBundle.addEntry(new Bundle.Entry().setResource(ccdComposition));

        // transform the patient data and assign it to Composition.subject
        Patient subject = resTransformer.PatientRole2Patient(ccd.getRecordTargets().get(0).getPatientRole());
        patientId = subject.getId();
        ccdComposition.setSubject(new ResourceReferenceDt(patientId));
        ccdBundle.addEntry(new Bundle.Entry().setResource(subject));

        for(Section cdaSec: ccd.getSections()) {
            Composition.Section fhirSec = resTransformer.section2Section(cdaSec);
            ccdComposition.addSection(fhirSec);
            if(cdaSec instanceof AdvanceDirectivesSection) {

            }
            else if(cdaSec instanceof AllergiesSection) {

            }
            else if(cdaSec instanceof EncountersSection) {

            }
            else if(cdaSec instanceof FamilyHistorySection) {

            }
            else if(cdaSec instanceof FunctionalStatusSection) {

            }
            else if(cdaSec instanceof ImmunizationsSection) {

            }
            else if(cdaSec instanceof MedicalEquipmentSection) {

            }
            else if(cdaSec instanceof MedicationsSection) {

            }
            else if(cdaSec instanceof PayersSection) {

            }
            else if(cdaSec instanceof PlanOfCareSection) {

            }
            else if(cdaSec instanceof ProblemSection) {
                ProblemSection probSec = (ProblemSection) cdaSec;
                for(ProblemConcernAct pcAct: probSec.getConsolProblemConcerns()) {
                    for(Condition condition: resTransformer.ProblemConcernAct2Condition(pcAct)) {
                        ResourceReferenceDt ref = fhirSec.addEntry();
                        ref.setReference(condition.getId());
                        ccdBundle.addEntry(new Bundle.Entry().setResource(condition));
                    }
                }
            }
            else if(cdaSec instanceof ProceduresSection) {

            }
            else if(cdaSec instanceof ResultsSection) {

            }
            else if(cdaSec instanceof SocialHistorySection) {

            }
            else if(cdaSec instanceof VitalSignsSection) {

            }
        }

        return ccdBundle;
    }
}
