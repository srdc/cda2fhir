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

import org.hl7.fhir.dstu3.model.Base;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.impl.ConsolFactoryImpl;
import org.openhealthtools.mdht.uml.cda.consol.impl.MedicationActivityImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.DatatypesFactoryImpl;

import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class MedicationStatementTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static ConsolFactoryImpl consolFactory;
	private static DatatypesFactory cdaTypeFactory;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		consolFactory = (ConsolFactoryImpl) ConsolFactoryImpl.init();
		cdaTypeFactory = DatatypesFactoryImpl.init();
	}

	@Test
	public void testMedicationStatus() throws Exception {

		// Make a medication activity.
		MedicationActivityImpl medAct = (MedicationActivityImpl) consolFactory.createMedicationActivity();

		// Transform from CDA to FHIR.
		BundleInfo bundleInfo = new BundleInfo(rt);
		org.hl7.fhir.dstu3.model.Bundle fhirBundle = rt.tMedicationActivity2MedicationStatement(medAct, bundleInfo)
				.getBundle();

		org.hl7.fhir.dstu3.model.Resource fhirResource = fhirBundle.getEntry().get(0).getResource();
		List<Base> takenCodes = fhirResource.getNamedProperty("taken").getValues();

		// Make assertions.
		Assert.assertEquals("Taken code defaults to UNK", "unk", takenCodes.get(0).primitiveValue());

	}

	@Test
	public void testMedicationDosage() throws Exception {

		// Make a medication activity.
		MedicationActivityImpl medAct = (MedicationActivityImpl) consolFactory.createMedicationActivity();

		IVL_PQ doseQuantity = cdaTypeFactory.createIVL_PQ();
		doseQuantity.setUnit("mg");
		doseQuantity.setValue(100.000);
		medAct.setDoseQuantity(doseQuantity);

		// Transform from CDA to FHIR.
		BundleInfo bundleInfo = new BundleInfo(rt);
		org.hl7.fhir.dstu3.model.Bundle fhirBundle = rt.tMedicationActivity2MedicationStatement(medAct, bundleInfo)
				.getBundle();

		org.hl7.fhir.dstu3.model.Resource fhirResource = fhirBundle.getEntry().get(0).getResource();

		List<Base> doses = fhirResource.getNamedProperty("dosage").getValues().get(0).getNamedProperty("dose")
				.getValues();

		// Make assertions.
		Assert.assertEquals("URI attached for ucum", "UriType[http://unitsofmeasure.org/ucum.html]",
				doses.get(0).getNamedProperty("system").getValues().get(0).toString());

	}

}