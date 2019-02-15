package tr.com.srdc.cda2fhir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.impl.AllergyStatusObservationImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.MedicationActivityImpl;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;

public class MedicationStatementTest {
	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();
	
	private static ConsolFactoryImpl cdaObjFactory;
	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;
	
	private static List<String> iiUUIDs;
	private static List<String> iiOIDs;
	private static Map<String, String> statusCodes;
	
	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		
		cdaObjFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();		
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();
		
		iiUUIDs = new ArrayList<String>(Arrays.asList("cdbd33f0-6cde-11db-9fe1-0801200c9a66", "6c844c75-aa34-412c-b7bd-5e4a9f206f42"));
		iiOIDs = new ArrayList<String>(Arrays.asList("2.16.840.1.113883.10.20.22.4.16"));
		statusCodes = new HashMap<String, String>();
		statusCodes.put("active", "active");
		statusCodes.put("intended", "intended");
		statusCodes.put("completed", "completed");
		statusCodes.put("nullified", "entered-in-error");
		
	}
	
	
	static private MedicationStatement findOneResource(Bundle bundle) throws Exception {
    	List<MedicationStatement> medicationStatements = bundle.getEntry().stream()
    			.map(r -> r.getResource())
    			.filter(r -> (r instanceof MedicationStatement))
    			.map(r -> (MedicationStatement) r)
				.collect(Collectors.toList());
    	Assert.assertEquals("Multiple MedicationStatement resources in the bundle",  1, medicationStatements.size());
    	return medicationStatements.get(0);	
	}
	
	
	static private void verifyMedicationStatement(MedicationActivity medAct, String expected) throws Exception {
		Bundle bundle = rt.tMedicationActivity2MedicationStatement(medAct);
		MedicationStatement medStatement = findOneResource(bundle);
		
	}
	
	
	
	static private MedicationActivity createMedicationActivity(String activity) {
		MedicationActivity medAct = cdaObjFactory.createMedicationActivity();
		II templateId1 = cdaTypeFactory.createII("2.16.840.1.113883.10.20.22.4.16");
		templateId1.setExtension("2014-06-09");
		II templateId2 = cdaTypeFactory.createII("2.16.840.1.113883.10.20.22.4.16");
		II id = cdaTypeFactory.createII("cdbd33f0-6cde-11db-9fe1-0801200c9a66");
		
		
		CS cs = cdaTypeFactory.createCS("nullified"); 
		return medAct;
		

	}
	
}
