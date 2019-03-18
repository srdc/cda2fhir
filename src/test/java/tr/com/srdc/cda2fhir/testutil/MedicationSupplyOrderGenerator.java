package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Product;
import org.openhealthtools.mdht.uml.cda.consol.MedicationSupplyOrder;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_INT;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassSupply;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;

public class MedicationSupplyOrderGenerator {

	private ManufacturedProductGenerator manProductGenerator;
	private CDAFactories factories;
	private BasicObjectGenerator basicObjectGenerator;

	private NullFlavor highNullFlvr;
	private NullFlavor lowNullFlvr;
	private String templateId;
	private String templateIdExt;
	private String rootId;
	private String statusCode;
	private NullFlavor statusCodeNullFlavor;
	private String effectiveTimeLow;
	private String effectiveTimeHigh;
	private Boolean effectiveTimeNull;
	private String quantityValue;
	private String quantityUnit;
	private String repeatNumber;
	private ManufacturedProduct manuProduct;

	static final public String DEFAULT_TEMPLATE_ID = "2.16.840.1.113883.10.20.22.4.17";
	static final public String DEFAULT_TEMPLATE_ID_EXT = "2019-01-01";
	static final public String DEFAULT_ROOT_ID = "12345678-1234-1234-1234-123456789012";
	static final public String DEFAULT_STATUS_CODE = "completed";
	static final public String DEFAULT_EFFECTIVE_TIME_LOW = "20190101000000.000-0500";
	static final public String DEFAULT_EFFECTIVE_TIME_HIGH = "20190101123456.000-0500";
	static final public String DEFAULT_QUANTITY_VALUE = "30.0";
	static final public String DEFAULT_QUANTITY_UNIT = "caps";
	static final public String DEFAULT_REPEAT_NUMBER = "1";

	public MedicationSupplyOrderGenerator(ManufacturedProductGenerator manProductGenerator, CDAFactories factories) {
		this.factories = factories;
		this.manProductGenerator = manProductGenerator;
		this.basicObjectGenerator = new BasicObjectGenerator(factories);

	}

	public MedicationSupplyOrderGenerator(CDAFactories factories) {
		this.factories = factories;
		this.basicObjectGenerator = new BasicObjectGenerator(factories);
		this.manProductGenerator = new ManufacturedProductGenerator(factories, basicObjectGenerator);

	}

	public MedicationSupplyOrderGenerator() {
		this.factories = CDAFactories.init();
		this.basicObjectGenerator = new BasicObjectGenerator(factories);
		this.manProductGenerator = new ManufacturedProductGenerator(factories, basicObjectGenerator);

	}

	public MedicationSupplyOrder generateDefaultMedicationSupplyOrder() {
		
		MedicationSupplyOrder supplyOrder = factories.consol.createMedicationSupplyOrder();
		
		supplyOrder.setClassCode(ActClassSupply.SPLY);
		supplyOrder.setMoodCode(x_DocumentSubstanceMood.INT);

		supplyOrder.getTemplateIds()
			.add(basicObjectGenerator.genTemplateId(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT));
		supplyOrder.getIds().add(generateId(DEFAULT_ROOT_ID));
		supplyOrder.setStatusCode(factories.datatype.createCS(DEFAULT_STATUS_CODE));
		supplyOrder.getEffectiveTimes()
			.add(basicObjectGenerator.generateEffectiveTime(DEFAULT_EFFECTIVE_TIME_LOW, DEFAULT_EFFECTIVE_TIME_HIGH));
		supplyOrder.setRepeatNumber(genRepeatValue(DEFAULT_REPEAT_NUMBER));
		supplyOrder.setQuantity(basicObjectGenerator.genQuantity(DEFAULT_QUANTITY_VALUE, DEFAULT_QUANTITY_UNIT));
		supplyOrder.setProduct(generateProduct(manProductGenerator.generateDefaultManufacturedProduct()));
		return supplyOrder;
	}

	public MedicationSupplyOrder generateMedicationSupplyOrder() {

		MedicationSupplyOrder supplyOrder = factories.consol.createMedicationSupplyOrder();
		
		supplyOrder.setClassCode(ActClassSupply.SPLY);
		supplyOrder.setMoodCode(x_DocumentSubstanceMood.INT);
		if (templateId != null && templateIdExt != null) {
			supplyOrder.getTemplateIds().add(basicObjectGenerator.genTemplateId(templateId, templateIdExt));
		} else if (templateId != null && templateIdExt == null) {
			supplyOrder.getTemplateIds().add(basicObjectGenerator.genTemplateId(templateId));
		} else if (templateId == null && templateIdExt != null) {
			supplyOrder.getTemplateIds().add(basicObjectGenerator.genTemplateId(DEFAULT_TEMPLATE_ID, templateIdExt));
		} else {
			supplyOrder.getTemplateIds()
				.add(basicObjectGenerator.genTemplateId(DEFAULT_TEMPLATE_ID, DEFAULT_TEMPLATE_ID_EXT));
		}

		supplyOrder.getIds().add(generateId(rootId == null ? DEFAULT_ROOT_ID : rootId));
		if (statusCodeNullFlavor != null) {
			supplyOrder.setStatusCode(basicObjectGenerator.genStatusCodeNullFlavor(statusCodeNullFlavor));
		} else {
			supplyOrder.setStatusCode(
					basicObjectGenerator.genStatusCode(statusCode != null ? statusCode : DEFAULT_STATUS_CODE));
		}

		if (effectiveTimeNull) {
			supplyOrder.getEffectiveTimes().add(basicObjectGenerator.generateEffectiveTime());
		} else if (effectiveTimeLow != null && effectiveTimeHigh != null) {
			supplyOrder.getEffectiveTimes()
			.add(basicObjectGenerator.generateEffectiveTime(effectiveTimeLow, effectiveTimeHigh));
		} else if (effectiveTimeLow != null && effectiveTimeHigh == null) {
			supplyOrder.getEffectiveTimes().add(basicObjectGenerator.generateEffectiveTime(effectiveTimeLow));
		} else if (effectiveTimeLow == null && effectiveTimeHigh == null) {
			supplyOrder.getEffectiveTimes()
			.add(basicObjectGenerator.generateEffectiveTime(DEFAULT_EFFECTIVE_TIME_LOW, DEFAULT_EFFECTIVE_TIME_HIGH));
		}

		supplyOrder.setRepeatNumber(genRepeatValue(repeatNumber != null ? repeatNumber : DEFAULT_REPEAT_NUMBER));

		if (quantityValue != null && quantityUnit != null) {
			supplyOrder.setQuantity(basicObjectGenerator.genQuantity(quantityValue, quantityUnit));
		} else if (quantityValue != null && quantityUnit == null) {
			supplyOrder.setQuantity(basicObjectGenerator.genQuantity(quantityValue));
		}
		supplyOrder.setProduct(generateProduct(
				manuProduct == null ? manProductGenerator.generateDefaultManufacturedProduct() : manuProduct));

		return supplyOrder;
	}

	public II generateId(String id) {
		return factories.datatype.createII(id);
	}

	public Product generateProduct(ManufacturedProduct manuProd) {
		Product prod = factories.base.createProduct();
		prod.setManufacturedProduct(manuProd);
		return prod;
	}

	public IVL_INT genRepeatValue(String value) {
		IVL_INT repeatValue = factories.datatype.createIVL_INT();
		repeatValue.setValue(Integer.parseInt(value));
		return repeatValue;
	}

	public NullFlavor getHighNullFlvr() {
		return highNullFlvr;
	}

	public void setHighNullFlvr(NullFlavor highNullFlvr) {
		this.highNullFlvr = highNullFlvr;
	}

	public ManufacturedProductGenerator getManuProductGenerator() {
		return manProductGenerator;
	}

	public void setManuProductGenerator(ManufacturedProductGenerator manProductGenerator) {
		this.manProductGenerator = manProductGenerator;
	}

	public NullFlavor getLowNullFlvr() {
		return lowNullFlvr;
	}

	public void setLowNullFlvr(NullFlavor lowNullFlvr) {
		this.lowNullFlvr = lowNullFlvr;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateIdExt() {
		return templateIdExt;
	}

	public void setTemplateIdExt(String templateIdExt) {
		this.templateIdExt = templateIdExt;
	}

	public String getRootId() {
		return rootId;
	}

	public void setRootId(String rootId) {
		this.rootId = rootId;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getEffectiveTimeLow() {
		return effectiveTimeLow;
	}

	public void setEffectiveTimeLow(String effectiveTimeLow) {
		this.effectiveTimeLow = effectiveTimeLow;
	}

	public String getEffectiveTimeHigh() {
		return effectiveTimeHigh;
	}

	public void setEffectiveTimeHigh(String effectiveTimeHigh) {
		this.effectiveTimeHigh = effectiveTimeHigh;
	}

	public ManufacturedProduct getManufacturedProduct() {
		return manuProduct;
	}

	public void setManufacturedProduct(ManufacturedProduct manuProduct) {
		this.manuProduct = manuProduct;
	}

	public String getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public String getQuantityValue() {
		return quantityValue;
	}

	public void setQuantityValue(String quantityValue) {
		this.quantityValue = quantityValue;
	}

	public String getRepeatNumber() {
		return repeatNumber;
	}

	public void setRepeatNumber(String repeatNumber) {
		this.repeatNumber = repeatNumber;
	}

	public NullFlavor getStatusCodeNullFlavor() {
		return statusCodeNullFlavor;
	}

	public void setStatusCodeNullFlavor(NullFlavor statusCodeNullFlavor) {
		this.statusCodeNullFlavor = statusCodeNullFlavor;
	}

	public Boolean getEffectiveTimeNull() {
		return effectiveTimeNull;
	}

	public void setEffectiveTimeNull(Boolean effectiveTimeNull) {
		this.effectiveTimeNull = effectiveTimeNull;
	}
	
	

}
