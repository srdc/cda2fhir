package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

public class CDAFactories {
	public ConsolFactoryImpl consol;
	public DatatypesFactory datatype;
	public CDAFactoryImpl base;

	private CDAFactories() {
	};

	static public CDAFactories init() {
		CDAFactories factories = new CDAFactories();

		factories.consol = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		factories.datatype = DatatypesFactoryImpl.init();
		factories.base = (CDAFactoryImpl) CDAFactoryImpl.init();

		return factories;
	}
}
