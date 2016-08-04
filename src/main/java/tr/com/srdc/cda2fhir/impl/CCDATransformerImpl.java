package tr.com.srdc.cda2fhir.impl;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import tr.com.srdc.cda2fhir.CCDATransformer;
import tr.com.srdc.cda2fhir.ResourceTransformer;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

import java.util.UUID;

/**
 * Created by mustafa on 8/3/2016.
 */
public class CCDATransformerImpl implements CCDATransformer {

    private int counter;
    private IdGeneratorEnum idGenerator;
    private ResourceTransformer resTransformer;

    public CCDATransformerImpl() {
        this.counter = 0;
        // The default resource id pattern is UUID
        this.idGenerator = IdGeneratorEnum.UUID;
        resTransformer = new ResourceTransformerImpl();
    }

    public CCDATransformerImpl(IdGeneratorEnum idGen) {
        super();
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
    public Bundle transformCCD(ContinuityOfCareDocument ccd) {
        if(ccd == null)
            return null;

        Bundle ccdBundle = new Bundle();
        Composition ccdComposition = new Composition();
        ccdBundle.addEntry(new Bundle.Entry().setResource(ccdComposition));

        Patient subject = resTransformer.PatientRole2Patient(ccd.getRecordTargets().get(0).getPatientRole());
        ccdComposition.setSubject(new ResourceReferenceDt(subject.getId()));
        ccdBundle.addEntry(new Bundle.Entry().setResource(subject));

        return ccdBundle;
    }
}
