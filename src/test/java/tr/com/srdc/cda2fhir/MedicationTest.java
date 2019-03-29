package tr.com.srdc.cda2fhir;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2019 Amida Technology Solutions, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.UriType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.impl.CDAFactoryImpl;
import org.openhealthtools.mdht.uml.cda.impl.ManufacturedProductImpl;
import org.openhealthtools.mdht.uml.cda.impl.MaterialImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import com.helger.commons.collection.attr.StringMap;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class MedicationTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static DatatypesFactory cdaTypeFactory;
	private static CDAFactoryImpl cdaFactory;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		cdaTypeFactory = DatatypesFactoryImpl.init();
		cdaFactory = (CDAFactoryImpl) CDAFactoryImpl.init();
	}

	@Test
	public void testMedications() throws Exception {

		BundleInfo bundleInfo = new BundleInfo(rt);

		// Make a manufactured product.
		ManufacturedProductImpl product = (ManufacturedProductImpl) cdaFactory.createManufacturedProduct();
		MaterialImpl material = (MaterialImpl) cdaFactory.createMaterial();

		// get one test encoding.
		CE codeOne = cdaTypeFactory.createCE();
		codeOne.setCode("863669");
		codeOne.setCodeSystem("2.16.840.1.113883.6.88");
		codeOne.setCodeSystemName("RxNorm");
		codeOne.setDisplayName("Tamsulosin hydrochloride 0.4 MG Oral Capsule");

		// get second test translation.
		CE codeTwo = cdaTypeFactory.createCE();
		codeTwo.setCode("863671");
		codeTwo.setCodeSystem("2.16.840.1.113883.6.88");
		codeTwo.setCodeSystemName("RxNorm");
		codeTwo.setDisplayName("Tamsulosin hydrochloride 0.4 MG Oral Capsule [Flomax]");

		// set material on product.
		codeOne.getTranslations().add(codeTwo);
		material.basicSetCode(codeOne, null);
		product.setManufacturedMaterial(material);

		// Transform from CDA to FHIR.
		org.hl7.fhir.dstu3.model.Resource fhirResource = rt.tManufacturedProduct2Medication(product, bundleInfo)
				.getBundle().getEntryFirstRep().getResource();

		List<Base> fhirCodes = fhirResource.getNamedProperty("code").getValues();
		List<Base> fhirCodings = fhirCodes.get(0).getNamedProperty("coding").getValues();

		// Make assertions.
		Assert.assertEquals("Two Codes on FHIR Object", 2, fhirCodings.size());
		Assert.assertEquals("Medication Code One set on FHIR Object", codeOne.getDisplayName(),
				fhirCodings.get(0).getNamedProperty("display").getValues().get(0).toString());
		Assert.assertEquals("Medication Code Two set on FHIR Object", codeTwo.getDisplayName(),
				fhirCodings.get(1).getNamedProperty("display").getValues().get(0).toString());
		Assert.assertTrue("No ingredient present", fhirResource.getNamedProperty("ingredient").getValues().isEmpty());
	}

	@Test // UPMCFHIR-216
	public void testMultumMedications() {

		BundleInfo bundleInfo = new BundleInfo(rt);

		// Make a manufactured product.
		ManufacturedProductImpl product = (ManufacturedProductImpl) cdaFactory.createManufacturedProduct();
		MaterialImpl material = (MaterialImpl) cdaFactory.createMaterial();

		// get one test encoding.
		CE codeOne = cdaTypeFactory.createCE();
		codeOne.setCode("d00769");
		codeOne.setCodeSystem("2.16.840.1.113883.6.314");
		codeOne.setCodeSystemName("multum-drug-id");
		codeOne.setDisplayName("pseudoephedrine");

		// set material on product.
		material.basicSetCode(codeOne, null);
		product.setManufacturedMaterial(material);

		// Transform from CDA to FHIR.
		org.hl7.fhir.dstu3.model.Resource fhirResource = rt.tManufacturedProduct2Medication(product, bundleInfo)
				.getBundle().getEntryFirstRep().getResource();

		List<Base> fhirCodes = fhirResource.getNamedProperty("code").getValues();
		List<Base> fhirCodings = fhirCodes.get(0).getNamedProperty("coding").getValues();

		UriType systemUri = (UriType) fhirCodings.get(0).getNamedProperty("system").getValues().get(0);

		Assert.assertEquals("Medication Code One set on FHIR Object", codeOne.getDisplayName(),
				fhirCodings.get(0).getNamedProperty("display").getValues().get(0).toString());
		Assert.assertEquals("Medication System set on FHIR Object", "http://www.nlm.nih.gov/research/umls/mmsl",
				systemUri.asStringValue());
	}

	@Test // UPMCFHIR-216
	public void testNCIMedications() {

		BundleInfo bundleInfo = new BundleInfo(rt);

		// Make a manufactured product.
		ManufacturedProductImpl product = (ManufacturedProductImpl) cdaFactory.createManufacturedProduct();
		MaterialImpl material = (MaterialImpl) cdaFactory.createMaterial();

		// get one test encoding.
		CE codeOne = cdaTypeFactory.createCE();
		codeOne.setCode("C38288");
		codeOne.setCodeSystem("2.16.840.1.113883.3.26.1.1");
		codeOne.setCodeSystemName("NCI Thesaurus");

		// set material on product.
		material.basicSetCode(codeOne, null);
		product.setManufacturedMaterial(material);

		// Transform from CDA to FHIR.
		org.hl7.fhir.dstu3.model.Resource fhirResource = rt.tManufacturedProduct2Medication(product, bundleInfo)
				.getBundle().getEntryFirstRep().getResource();

		List<Base> fhirCodes = fhirResource.getNamedProperty("code").getValues();
		List<Base> fhirCodings = fhirCodes.get(0).getNamedProperty("coding").getValues();

		UriType systemUri = (UriType) fhirCodings.get(0).getNamedProperty("system").getValues().get(0);

		Assert.assertEquals("Medication Code One set on FHIR Object", codeOne.getCode(),
				fhirCodings.get(0).getNamedProperty("code").getValues().get(0).toString());
		Assert.assertEquals("Medication System set on FHIR Object", "http://www.nlm.nih.gov/research/umls/nci",
				systemUri.asStringValue());
	}

	@Test
	public void testMedicationOriginalText() throws Exception {

		// Make a manufactured product.
		ManufacturedProductImpl product = (ManufacturedProductImpl) cdaFactory.createManufacturedProduct();
		MaterialImpl material = (MaterialImpl) cdaFactory.createMaterial();

		BundleInfo bundleInfo = new BundleInfo(rt);
		String expectedValue = "freetext entry";
		String referenceValue = "fakeid1";
		CE ce = cdaTypeFactory.createCE();
		ED ed = cdaTypeFactory.createED();
		TEL tel = cdaTypeFactory.createTEL();
		tel.setValue("#" + referenceValue);
		ed.setReference(tel);
		ce.setCode("code");
		ce.setCodeSystem("codeSystem");
		ce.setOriginalText(ed);
		Map<String, String> idedAnnotations = new StringMap();
		idedAnnotations.put(referenceValue, expectedValue);
		bundleInfo.mergeIdedAnnotations(idedAnnotations);

		material.setCode(ce);
		product.setManufacturedMaterial(material);
		Bundle bundle = rt.tManufacturedProduct2Medication(product, bundleInfo).getBundle();
		Medication medication = BundleUtil.findOneResource(bundle, Medication.class);
		CodeableConcept cc = medication.getCode();
		Assert.assertEquals("Medication Code text value assigned", expectedValue, cc.getText());

	}

}