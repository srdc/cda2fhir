package tr.com.srdc.cda2fhir.conf;

import java.nio.charset.Charset;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Composition.CompositionStatus;
import org.hl7.fhir.dstu3.model.Condition.ConditionVerificationStatus;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Identifier.IdentifierUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;

public class Config {

	private static FhirContext fhirCtx;

	// Default values for some mandatory attributes, which cannot be retrieved from
	// CDA document
	public static final String DEFAULT_COMMUNICATION_LANGUAGE_CODE_SYSTEM = "urn:ietf:bcp:47";
	public static final ConditionVerificationStatus DEFAULT_CONDITION_VERIFICATION_STATUS = ConditionVerificationStatus.CONFIRMED;
	public static final CompositionStatus DEFAULT_COMPOSITION_STATUS = CompositionStatus.PRELIMINARY;
	public static final IdentifierUse DEFAULT_IDENTIFIER_USE = IdentifierUse.OFFICIAL;
	public static final ContactPointSystem DEFAULT_CONTACT_POINT_SYSTEM = ContactPointSystem.PHONE;
	public static final Coding DEFAULT_ENCOUNTER_PARTICIPANT_TYPE_CODE = new Coding()
			.setSystem("http://hl7.org/fhir/v3/ParticipationType").setCode("PART").setDisplay("Participation");
	public static final EncounterStatus DEFAULT_ENCOUNTER_STATUS = EncounterStatus.FINISHED;
	public static final Coding DEFAULT_DIAGNOSTICREPORT_PERFORMER_DATA_ABSENT_REASON_CODE = new Coding()
			.setSystem("http://hl7.org/fhir/data-absent-reason").setCode("unknown").setDisplay("Unknown");
	public static final boolean DEFAULT_IMMUNIZATION_REPORTED = false;

	public static final String NARRATIVE_PROPERTIES_FILE_PATH = "file:src/main/resources/narrative/customnarrative.properties";

	public static final String MEDICATION_CODE_SYSTEM = null;

	private static boolean generateNarrative = false;
	private static INarrativeGenerator narrativeGenerator;

	private static boolean generateDafProfileMetadata = false;

	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	static {
		fhirCtx = FhirContext.forDstu3();
		narrativeGenerator = new CustomThymeleafNarrativeGenerator(NARRATIVE_PROPERTIES_FILE_PATH);
		if (generateNarrative)
			fhirCtx.setNarrativeGenerator(narrativeGenerator);

		logger.info("System file encoding is: " + Charset.defaultCharset().displayName());
	}

	public static FhirContext getFhirContext() {
		return fhirCtx;
	}

	public static void setGenerateNarrative(boolean generateNar) {
		generateNarrative = generateNar;
		if (generateNarrative)
			fhirCtx.setNarrativeGenerator(narrativeGenerator);
		else
			fhirCtx.setNarrativeGenerator(null);
	}

	public static boolean getGenerateNarrative() {
		return generateNarrative;
	}

	public static void setGenerateDafProfileMetadata(boolean generateDafProfileMeta) {
		generateDafProfileMetadata = generateDafProfileMeta;
	}

	public static boolean isGenerateDafProfileMetadata() {
		return generateDafProfileMetadata;
	}

}
