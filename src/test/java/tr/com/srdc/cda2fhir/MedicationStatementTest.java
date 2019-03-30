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
import org.hl7.fhir.dstu3.model.Dosage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.impl.MedicationActivityImpl;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;

public class MedicationStatementTest {

	private static final ResourceTransformerImpl rt = new ResourceTransformerImpl();

	private static CDAFactories factories;

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
	}

	@Test
	public void testMedicationStatus() throws Exception {

		// Make a medication activity.
		MedicationActivityImpl medAct = (MedicationActivityImpl) factories.consol.createMedicationActivity();

		// Transform from CDA to FHIR.
		BundleInfo bundleInfo = new BundleInfo(rt);
		org.hl7.fhir.dstu3.model.Bundle fhirBundle = rt.tMedicationActivity2MedicationStatement(medAct, bundleInfo)
				.getBundle();

		org.hl7.fhir.dstu3.model.Resource fhirResource = fhirBundle.getEntry().get(0).getResource();
		List<Base> takenCodes = fhirResource.getNamedProperty("taken").getValues();

		// Make assertions.
		Assert.assertEquals("Taken code defaults to UNK", "unk", takenCodes.get(0).primitiveValue());

	}

	private Observation getCdaFrequencyObservation(String frequency) {
		Observation obs = factories.base.createObservation();
		ANY value = factories.datatype.createED(frequency);
		obs.setClassCode(ActClassObservation.OBS);
		obs.setMoodCode(x_ActMoodDocumentObservation.EVN);
		CD code = factories.datatype.createCD();
		code.setCode("FREQUENCY");
		obs.setCode(code);
		obs.getValues().add(value);
		return obs;
	}

	private EntryRelationship getFrequencyEntryRelationship(Observation observation) {
		EntryRelationship er = factories.base.createEntryRelationship();
		er.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
		er.setInversionInd(true);
		er.setObservation(observation);
		return er;
	}

	private SubstanceAdministration getFreeTextSignatureSubstanceAdministration(String sig) {
		SubstanceAdministration sa = factories.base.createSubstanceAdministration();
		sa.setClassCode(ActClass.SBADM);
		sa.setMoodCode(x_DocumentSubstanceMood.EVN);
		ED text = factories.datatype.createED();
		TEL sigRef = factories.datatype.createTEL("#" + sig);
		text.setReference(sigRef);
		sa.setText(text);
		return sa;

	}

	private EntryRelationship getFreeTextSignatureEntryRelationship(SubstanceAdministration sa) {
		EntryRelationship er = factories.base.createEntryRelationship();
		er.setTypeCode(x_ActRelationshipEntryRelationship.COMP);
		er.setSubstanceAdministration(sa);
		return er;
	}

	private IVL_PQ getDoseQuantity(String unit, Double value) {
		IVL_PQ doseQuantity = factories.datatype.createIVL_PQ();
		doseQuantity.setUnit("mg");
		doseQuantity.setValue(100.000);
		return doseQuantity;
	}

	@Test
	public void testMedicationDosage() throws Exception {
		String freeTextInstruction = "Take with 4 pints of rocky road icream and 3 cupcakes";
		String frequency = "2 times daily.";
		String sig = "sig";

		// Create substance administration signature reference
		SubstanceAdministration sa = getFreeTextSignatureSubstanceAdministration(sig);

		// Create entry relationship containing signature reference
		EntryRelationship freeTextEntryRelationship = getFreeTextSignatureEntryRelationship(sa);

		// Create Frequency Observation
		Observation obs = getCdaFrequencyObservation(frequency);

		// Create Entry Relationship containing frequency observation
		EntryRelationship frequencyEntryRelationship = getFrequencyEntryRelationship(obs);

		// Make a medication activity.
		MedicationActivityImpl medAct = (MedicationActivityImpl) factories.consol.createMedicationActivity();

		// Make Dosage Quantity
		IVL_PQ doseQuantity = getDoseQuantity("mg", 100.000);

		// Set Dosage
		medAct.setDoseQuantity(doseQuantity);
		// Set Signature Reference
		medAct.getEntryRelationships().add(freeTextEntryRelationship);
		// Set frequency observation
		medAct.getEntryRelationships().add(frequencyEntryRelationship);

		// Transform from CDA to FHIR.
		BundleInfo bundleInfo = new BundleInfo(rt);

		// Create signature to free text mapping in Bundle Info
		bundleInfo.getIdedAnnotations().put(sig, freeTextInstruction);

		org.hl7.fhir.dstu3.model.Bundle fhirBundle = rt.tMedicationActivity2MedicationStatement(medAct, bundleInfo)
				.getBundle();

		org.hl7.fhir.dstu3.model.Resource fhirResource = fhirBundle.getEntry().get(0).getResource();

		List<Base> doses = fhirResource.getNamedProperty("dosage").getValues().get(0).getNamedProperty("dose")
				.getValues();
		Dosage dosage = (Dosage) fhirResource.getNamedProperty("dosage").getValues().get(0);

		// Make assertions.
		Assert.assertEquals("URI attached for ucum", "UriType[http://unitsofmeasure.org/ucum.html]",
				doses.get(0).getNamedProperty("system").getValues().get(0).toString());

		Assert.assertEquals("sig1 free text instruction included in dosage text", freeTextInstruction,
				dosage.getText());

		Assert.assertEquals("sig1 free text instruction included in dosage patientInstruction", freeTextInstruction,
				dosage.getPatientInstruction());

		Assert.assertEquals("Frequency: " + frequency + " included in dosage timing timing",
				dosage.getTiming().getCode().getText(), frequency);

	}

}