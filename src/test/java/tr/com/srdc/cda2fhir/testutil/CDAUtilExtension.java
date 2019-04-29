package tr.com.srdc.cda2fhir.testutil;

import java.io.File;
import java.io.FileWriter;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.rim.InfrastructureRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.transform.ValueSetsTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.IdentifierMap;

public class CDAUtilExtension {
	public static final String ALLERGIES_CODE = "48765-2";
	public static final String MEDICATIONS_CODE = "10160-0";
	public static final String IMMUNIZATIONS_CODE = "11369-6";
	public static final String RESULTS_CODE = "30954-2";
	public static final String VITALS_CODE = "8716-3";
	public static final String CONDITIONS_CODE = "11450-4";
	public static final String ENCOUNTERS_CODE = "46240-8";
	public static final String PROCEDURES_CODE = "47519-4";

	public static File writeAsXML(InfrastructureRoot infrastructureRoot, String outputPath, String caseName)
			throws Exception {
		File xmlFile = new File(outputPath, caseName + ".xml");
		xmlFile.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(xmlFile);
		CDAUtil.saveSnippet(infrastructureRoot, fw);
		fw.close();
		return xmlFile;
	}

	public static NullFlavor toNullFlavor(String nullFlavor) {
		NullFlavor nf = NullFlavor.get(nullFlavor);
		if (nf == null) {
			throw new TestSetupException("Invalid null flavor enumeration.");
		}
		return nf;
	}

	public static int idValue(String fhirType, EList<II> iis, IdentifierMap<Integer> orderMap) {
		ValueSetsTransformerImpl vst = new ValueSetsTransformerImpl();
		II ii = iis.get(0);
		String root = ii.getRoot();
		String extension = ii.getExtension();
		if (extension == null) {
			Integer index = orderMap.get(fhirType, root);
			return index.intValue();
		} else {
			String value = extension;
			String system = vst.tOid2Url(root);
			Integer index = orderMap.get(fhirType, system, value);
			return index.intValue();
		}
	}

	public static EncountersSectionEntriesOptional getEncountersSection(ContinuityOfCareDocument cda) {
		EncountersSectionEntriesOptional section = cda.getEncountersSection();
		if (section != null) {
			return section;
		}
		Optional<EncountersSectionEntriesOptional> sectionOptional = cda.getSections().stream()
				.filter(s -> s instanceof EncountersSectionEntriesOptional)
				.map(s -> (EncountersSectionEntriesOptional) s).findFirst();
		if (!sectionOptional.isPresent()) {
			return null;
		}
		return sectionOptional.get();
	}

	public static VitalSignsSectionEntriesOptional getVitalSignsSection(ContinuityOfCareDocument cda) {
		VitalSignsSectionEntriesOptional section = cda.getVitalSignsSectionEntriesOptional();
		if (section != null) {
			return section;
		}
		Optional<VitalSignsSection> sectionOptional = cda.getSections().stream()
				.filter(s -> s instanceof VitalSignsSection).map(s -> (VitalSignsSection) s).findFirst();
		if (!sectionOptional.isPresent()) {
			return null;
		}
		return sectionOptional.get();
	}

	public static ImmunizationsSection getImmunizationsSection(ContinuityOfCareDocument cda) {
		Optional<ImmunizationsSection> sectionOptional = cda.getSections().stream()
				.filter(s -> s instanceof ImmunizationsSection).map(s -> (ImmunizationsSection) s).findFirst();
		if (!sectionOptional.isPresent()) {
			return null;
		}
		return sectionOptional.get();
	}
}
