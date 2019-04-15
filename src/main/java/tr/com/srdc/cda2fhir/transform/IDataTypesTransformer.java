package tr.com.srdc.cda2fhir.transform;

import java.util.Map;

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

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.Base64BinaryType;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Range;
import org.hl7.fhir.dstu3.model.Ratio;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Timing;
import org.hl7.fhir.dstu3.model.UriType;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.BIN;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.URL;

public interface IDataTypesTransformer {

	/**
	 * Transforms a CDA AD instance to a FHIR Address composite datatype instance.
	 *
	 * @param ad A CDA AD instance
	 * @return An Address composite datatype instance
	 */
	Address AD2Address(AD ad);

	/**
	 * Transforms a CDA BIN instance to a FHIR Base64BinaryType primitive datatype
	 * instance.
	 *
	 * @param bin A CDA BIN instance
	 * @return A Base64BinaryType primitive datatype instance
	 */
	Base64BinaryType tBIN2Base64Binary(BIN bin);

	/**
	 * Transforms a CDA BL instance to a FHIR BooleanType primitive datatype
	 * instance.
	 *
	 * @param bl A CDA BL instance
	 * @return A BooleanType primitive datatype instance
	 */
	BooleanType tBL2Boolean(BL bl);

	/**
	 * Transforms a CDA ED instance to a String based on ided annotations from
	 * section text.
	 *
	 * @param ed A CDA ED instance
	 * @idedAnnotations A id to value map for annotation
	 * @return A String
	 */
	String tED2Annotation(ED ed, Map<String, String> idedAnnotations);

	/**
	 * Transforms a CDA CD instance to a FHIR CodeableConcept composite datatype
	 * instance. Translations and original text of the CD instance are also
	 * included.
	 *
	 * @param cd A CDA CD instance
	 * @param A  id to value map of annotations that maybe referred from
	 *           originalText
	 * @return A CodeableConcept composite datatype instance
	 */
	CodeableConcept tCD2CodeableConcept(CD cd, Map<String, String> idedAnnotations);

	/**
	 * Transforms a CDA CD instance to a FHIR CodeableConcept composite datatype
	 * instance. Translations of the CD instance are also included.
	 *
	 * @param cd A CDA CD instance
	 * @return A CodeableConcept composite datatype instance
	 */
	CodeableConcept tCD2CodeableConcept(CD cd);

	/**
	 * Transforms a CDA CD instance to a FHIR CodeableConcept composite datatype
	 * instance. Translations of the CD instance are excluded but original text is
	 * included.
	 *
	 * @param cd A CDA CD instance
	 * @param A  id to value map of annotations that maybe referred from
	 *           originalText
	 * @return A CodeableConcept composite datatype instance
	 */
	CodeableConcept tCD2CodeableConceptExcludingTranslations(CD cd, Map<String, String> idedAnnotations);

	/**
	 * Transforms a CDA CD instance to a FHIR CodeableConcept composite datatype
	 * instance. Translations of the CD instance are excluded.
	 *
	 * @param cd A CDA CD instance
	 * @return A CodeableConcept composite datatype instance
	 */
	CodeableConcept tCD2CodeableConceptExcludingTranslations(CD cd);

	/**
	 * Transforms a CDA CV instance to a FHIR Coding composite datatype instance.
	 *
	 * @param cv A CDA CV instance
	 * @return A Coding composite datatype instance
	 */
	Coding tCV2Coding(CV cv);

	/**
	 * Transforms a CDA ED instance to a FHIR Attachment composite datatype
	 * instance.
	 *
	 * @param ed A CDA ED instance
	 * @return An Attachment composite datatype instance
	 */
	Attachment tED2Attachment(ED ed);

	/**
	 * Transforms a CDA EN instance to a FHIR HumanName composite datatype instance.
	 *
	 * @param en A CDA EN instance
	 * @return A HumanName composite datatype instance
	 */
	HumanName tEN2HumanName(EN en);

	/**
	 * Transforms a CDA II instance to a FHIR Identifier composite datatype
	 * instance.
	 *
	 * @param ii A CDA II instance
	 * @return A Identifier composite datatype instance
	 */
	Identifier tII2Identifier(II ii);

	/**
	 * Transforms a CDA INT instance to a FHIR IntegerType primitive datatype
	 * instance.
	 *
	 * @param myInt A CDA INT instance
	 * @return A IntegerType primitive datatype instance
	 */
	IntegerType tINT2Integer(INT myInt);

	/**
	 * Transforms a CDA IVL_PQ instance to a FHIR Range composite datatype instance.
	 *
	 * @param ivlpq A CDA IVL_PQ instance
	 * @return A Range composite datatype instance
	 */
	Range tIVL_PQ2Range(IVL_PQ ivlpq);

	/**
	 * Transforms a CDA IVL_TS instance to a FHIR Period composite datatype
	 * instance.
	 *
	 * @param ivlts A CDA IVL_TS instance
	 * @return A Period composite datatype instance
	 */
	Period tIVL_TS2Period(IVL_TS ivlts);

	/**
	 * Transforms a CDA PIVL_TS instance to a FHIR Timing composite datatype
	 * instance.
	 *
	 * @param pivlts A CDA PIVL_TS instance
	 * @return A Timing composite datatype instance
	 */
	Timing tPIVL_TS2Timing(PIVL_TS pivlts);

	/**
	 * Transforms a CDA PQ instance to a FHIR Quantity composite datatype instance.
	 *
	 * @param pq A CDA PQ instance
	 * @return A Quantity composite datatype instance
	 */
	Quantity tPQ2Quantity(PQ pq);

	/**
	 * Transforms a CDA PQ instance to a FHIR SimpleQuantity composite datatype
	 * instance.
	 *
	 * @param pq A CDA PQ instance
	 * @return A SimpleQuantity composite datatype instance
	 */
	SimpleQuantity tPQ2SimpleQuantity(PQ pq);

	/**
	 * Transforms a CDA REAL instance to a FHIR DecimalType primitive datatype
	 * instance.
	 *
	 * @param real A CDA REAL instance
	 * @return A DecimalType primitive datatype instance
	 */
	DecimalType tREAL2DecimalType(REAL real);

	/**
	 * Transforms a CDA REAL instance to a FHIR String primitive datatype instance.
	 *
	 * @param real a CDA REAL instance
	 * @return A String primitive datatype instance
	 */
	StringType tREAL2String(REAL real);

	/**
	 * Transforms a CDA RTO instance to a FHIR Ratio composite datatype instance.
	 *
	 * @param rto A CDA RTO instance
	 * @return A Ratio composite datatype instance
	 */
	Ratio tRTO2Ratio(RTO rto);

	/**
	 * Transforms a CDA ST instance to a FHIR StringType primitive datatype
	 * instance.
	 *
	 * @param st A CDA ST instance
	 * @return A StringType datatype
	 */
	StringType tST2String(ST st);

	/**
	 * Transforms a String that includes a date in CDA format to a FHIR DateTimeType
	 * primitive datatype instance.
	 *
	 * @param date A String that includes a date in CDA format
	 * @return A DateTimeType primitive datatype instance
	 */
	DateTimeType tString2DateTime(String date);

	/**
	 * Transforms a CDA StrucDocText instance to a FHIR Narrative composite datatype
	 * instance.
	 *
	 * @param sdt A CDA StrucDocText instance
	 * @return A Narrative composite datatype instance
	 */
	Narrative tStrucDocText2Narrative(StrucDocText sdt);

	/**
	 * Transforms a CDA TEL instance to a FHIR ContactPoint composite datatype
	 * instance.
	 *
	 * @param tel A CDA TEL instance
	 * @return A ContactPoint composite datatype instance
	 */
	ContactPoint tTEL2ContactPoint(TEL tel);

	/**
	 * Transforms a CDA TS instance to a FHIR DateType primitive datatype instance.
	 *
	 * @param ts A CDA TS instance
	 * @return A DateType primitive datatype instance
	 */
	DateType tTS2Date(TS ts);

	/**
	 * Transforms a CDA TS instance to a FHIR DateTimeType primitive datatype
	 * instance.
	 *
	 * @param ts A CDA TS instance
	 * @return A DateTimeType primitive datatype instance
	 */
	DateTimeType tTS2DateTime(TS ts);

	/**
	 * Transforms a CDA TS instance to a FHIR InstantType primitive datatype
	 * instance.
	 *
	 * @param ts A CDA TS instance
	 * @return A InstantType primitive datatype instance
	 */
	InstantType tTS2Instant(TS ts);

	/**
	 * Transforms a CDA URL instance to a FHIR UriType primitive datatype instance.
	 *
	 * @param url A CDA URL instance
	 * @return A UriType primitive datatype instance
	 */
	UriType tURL2Uri(URL url);

}
