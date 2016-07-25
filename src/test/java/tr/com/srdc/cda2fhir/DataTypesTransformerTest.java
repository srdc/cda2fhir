package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import tr.com.srdc.cda2fhir.impl.DataTypesTransformerImpl;

/**
 * Created by mustafa on 7/25/2016.
 */
public class DataTypesTransformerTest {

    DataTypesTransformer dtt = new DataTypesTransformerImpl();

    @Test
    public void testPQ2Quantity() {
        // simple instance test
        PQ pq = DatatypesFactory.eINSTANCE.createPQ();
        pq.setValue(120.0);
        pq.setUnit("mg");
        QuantityDt quantity = dtt.PQ2Quantity(pq);

        Assert.assertEquals("PQ.value was not transformed", quantity.getValue().doubleValue(), 120.0, 0.001);
        Assert.assertEquals("PQ.unit was not transformed", quantity.getUnit(), "mg");

        // null instance test
        PQ pq2 = null;
        QuantityDt quantity2 = dtt.PQ2Quantity(pq2);
        Assert.assertNull("PQ null instance transform failed", quantity2);

        // nullFlavor instance test
        PQ pq3 = DatatypesFactory.eINSTANCE.createPQ();
        pq3.setNullFlavor(NullFlavor.NI);
        QuantityDt quantity3 = dtt.PQ2Quantity(pq3);
        Assert.assertNull("PQ.nullFlavor set instance transform failed", quantity3);
    }

}
