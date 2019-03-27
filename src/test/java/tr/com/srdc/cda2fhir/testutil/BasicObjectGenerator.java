package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

public class BasicObjectGenerator {

	private CDAFactories factories;

	public BasicObjectGenerator(CDAFactories factories) {
		this.factories = factories;
	}

	public BasicObjectGenerator() {
		this.factories = CDAFactories.init();
	}

	public II genTemplateId(String Id) {
		return factories.datatype.createII(Id);
	}

	public II genTemplateId(String Id, String IdExt) {
		return factories.datatype.createII(Id, IdExt);
	}

	public IVL_TS generateEffectiveTime(String low, String high) {
		return factories.datatype.createIVL_TS(low, high);
	}

	public IVL_TS generateEffectiveTime(String low) {
		IVL_TS lowEffTime = factories.datatype.createIVL_TS();
		IVXB_TS lowVal = factories.datatype.createIVXB_TS();
		lowVal.setValue(low);
		lowEffTime.setLow(lowVal);
		lowEffTime.setHigh(getNullHigh());
		return lowEffTime;
	}

	public IVL_TS generateEffectiveTime() {
		IVL_TS effTime = factories.datatype.createIVL_TS();
		effTime.setHigh(getNullHigh());
		return effTime;
	}

	private IVXB_TS getNullHigh() {
		IVXB_TS highVal = factories.datatype.createIVXB_TS();
		highVal.setNullFlavor(NullFlavor.NI);
		return highVal;
	}

	public CS genStatusCode(String code) {
		return factories.datatype.createCS(code);
	}

	public CS genStatusCodeNullFlavor(NullFlavor nf) {
		CS code = factories.datatype.createCS();
		code.setNullFlavor(nf);
		return code;
	}

	public PQ genQuantity(String value) {
		PQ quantity = factories.datatype.createPQ();
		quantity.setValue(Double.parseDouble(value));
		return quantity;
	}

	public PQ genQuantity(String value, String unit) {
		return factories.datatype.createPQ(Double.parseDouble(value), unit);
	}

	public TEL generateReference(String val) {
		return factories.datatype.createTEL(val);
	}

	public ED generateOriginalText(TEL ref) {
		ED originalText = factories.datatype.createED();
		originalText.setReference(ref);
		return originalText;
	}

	public ON getName(String nameStr) {
		ON nameON = factories.datatype.createON();
		nameON.addText(nameStr);
		return nameON;
	}

	public AD getAddress(String street, String city, String state, String postal) {
		AD addr = factories.datatype.createAD();
		addr.addStreetAddressLine(street);
		addr.addCity(city);
		addr.addState(state);
		addr.addPostalCode(postal);
		return addr;
	}

}
