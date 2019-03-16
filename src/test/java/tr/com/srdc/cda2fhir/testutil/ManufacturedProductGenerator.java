package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassManufacturedMaterial;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminerDetermined;

public class ManufacturedProductGenerator {

	private BasicObjectGenerator basicObjectGenerator;
	private CDAFactories factories;

	private String manuMaterialCodeCode;
	private String manuMaterialCodeSystem;
	private String manuMaterialDisplayName;
	private String originalTextReference;

	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.4.23";
	static final public String DEFAULT_TEMPLATE_ID_EXT = "2014-06-09";
	static final public String DEFAULT_ROOT_ID = "2a620155-9d11-439e-92b3-5d9815ff4ee8";
	static final public String DEFAULT_MANU_MATERIAL_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.4.23";
	static final public String DEFAULT_MANU_MATERIAL_TEMPLATE_ID_EXT = "2.16.840.1.113883.10.20.22.4.23";
	static final public String DEFAULT_MANU_MATERIAL_CODE_CODE = "123456";
	static final public String DEFAULT_MANU_MATERIAL_CODE_SYSTEM = "2.16.840.1.113883.6.88";
	static final public String DEFAULT_MANU_MATERIAL_DISPLAY_NAME = "default display";
	static final public String DEFAULT_ORIGINAL_TEXT_REFERENCE = "#MEDPROD12345";
	static final public String DEFAULT_TRANSLATION_CODE = "a1b2c3";
	static final public String DEFAULT_TRANSLATION_CODE_SYSTEM = "2.16.840.1.113883.6.314";
	static final public String DEFAULT_TRANSLATION_CODE_SYSTEM_NAME = "code system display name";
	static final public String DEFAULT_TRANSLATION_DISPLAY_NAME = "drug display name";

	private List<CE> translationCodes = new ArrayList<CE>();

	public ManufacturedProductGenerator(CDAFactories factories, BasicObjectGenerator basicObjectGenerator) {
		this.factories = factories;
		this.basicObjectGenerator = basicObjectGenerator;
	}

	public ManufacturedProductGenerator(BasicObjectGenerator basicObjectGenerator) {
		this.basicObjectGenerator = basicObjectGenerator;
	}

	public ManufacturedProductGenerator() {
		this.factories = CDAFactories.init();
		this.basicObjectGenerator = new BasicObjectGenerator(factories);
	}

	public ManufacturedProduct generateManufacturedProduct() {
		ManufacturedProduct manProd = factories.base.createManufacturedProduct();
		manProd.getTemplateIds().add(basicObjectGenerator.genTemplateId(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT));
		manProd.getIds().add(factories.datatype.createII(DEFAULT_ROOT_ID));
		manProd.setManufacturedMaterial(generateManuMaterial());
		return manProd;
	}

	public ManufacturedProduct generateDefaultManufacturedProduct() {
		ManufacturedProduct manProd = factories.base.createManufacturedProduct();
		manProd.getTemplateIds().add(basicObjectGenerator.genTemplateId(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT));
		manProd.getIds().add(factories.datatype.createII(DEFAULT_ROOT_ID));
		manProd.setManufacturedMaterial(generateDefautManuMaterial());
		return manProd;
	}

	public Material generateDefautManuMaterial() {
		CE defaultManuMaterialCode = factories.datatype.createCE(DEFAULT_MANU_MATERIAL_CODE_CODE,
				DEFAULT_MANU_MATERIAL_CODE_SYSTEM);
		defaultManuMaterialCode.setDisplayName(DEFAULT_MANU_MATERIAL_DISPLAY_NAME);
		TEL defaultRef = basicObjectGenerator.generateReference(DEFAULT_ORIGINAL_TEXT_REFERENCE);
		ED defaultOriginalText = basicObjectGenerator.generateOriginalText(defaultRef);
		defaultManuMaterialCode.setOriginalText(defaultOriginalText);
		II defaultTempId = basicObjectGenerator.genTemplateId(DEFAULT_MANU_MATERIAL_TEMPLATE_ID,
				DEFAULT_MANU_MATERIAL_TEMPLATE_ID_EXT);
		Material manuMaterial = genManuMaterial(defaultTempId, defaultManuMaterialCode);
		manuMaterial.getCode().getTranslations().add(generateDefaultTranslation());
		return manuMaterial;
	}

	private Material generateManuMaterial() {
		CE manuMaterialCode = factories.datatype.createCE();

		manuMaterialCode.setCode(manuMaterialCodeCode != null ? manuMaterialCodeCode : DEFAULT_MANU_MATERIAL_CODE_CODE);
		manuMaterialCode
				.setCodeSystem(manuMaterialCodeSystem != null ? manuMaterialCodeSystem : DEFAULT_MANU_MATERIAL_CODE_SYSTEM);
		manuMaterialCode.setDisplayName(
				manuMaterialDisplayName != null ? manuMaterialDisplayName : DEFAULT_MANU_MATERIAL_DISPLAY_NAME);

		TEL ref = basicObjectGenerator.generateReference(
				originalTextReference != null ? originalTextReference : DEFAULT_ORIGINAL_TEXT_REFERENCE);
		ED originalText = basicObjectGenerator.generateOriginalText(ref);
		manuMaterialCode.setOriginalText(originalText);

		II tempId = basicObjectGenerator.genTemplateId(DEFAULT_MANU_MATERIAL_TEMPLATE_ID, DEFAULT_MANU_MATERIAL_TEMPLATE_ID_EXT);

		Material manuMaterial;
		if (translationCodes == null && !translationCodes.isEmpty()) {
			manuMaterial = genManuMaterial(tempId, manuMaterialCode);
		} else {
			manuMaterial = genManuMaterial(tempId, manuMaterialCode, translationCodes);
		}

		return manuMaterial;
	}

	private Material genManuMaterial(II tempId, CE code, List<CE> translationList) {
		Material manuMaterial = genManuMaterial(tempId, code);
		manuMaterial.getCode().getTranslations().addAll(translationList);
		return manuMaterial;
	}

	private Material genManuMaterial(II tempId, CE code) {
		Material manuMaterial = factories.base.createMaterial();
		factories.vocab.getEntityClassManufacturedMaterial();
		manuMaterial.setClassCode(EntityClassManufacturedMaterial.MMAT);
		manuMaterial.setDeterminerCode(EntityDeterminerDetermined.KIND);
		manuMaterial.setCode(code);
		return manuMaterial;

	}

	private CD generateDefaultTranslation() {
		CD defaultTranslation = factories.datatype.createCD();
		defaultTranslation.setCode(DEFAULT_TRANSLATION_CODE);
		defaultTranslation.setCodeSystem(DEFAULT_TRANSLATION_CODE_SYSTEM);
		defaultTranslation.setCodeSystemName(DEFAULT_TRANSLATION_CODE_SYSTEM_NAME);
		defaultTranslation.setDisplayName(DEFAULT_TRANSLATION_DISPLAY_NAME);
		return defaultTranslation;

	}

	public void addTranslationCode(CE code) {
		translationCodes.add(code);
	}

	public void addTranslationCodes(List<CE> code) {
		translationCodes.addAll(code);
	}

	public void clearTranslationCodes() {
		translationCodes.clear();
	}
}
