package tr.com.srdc.cda2fhir;

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

import java.math.BigDecimal;
import java.util.TimeZone;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Attachment;
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
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Range;
import org.hl7.fhir.dstu3.model.Ratio;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Timing;
import org.junit.Assert;
import org.junit.Test;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.PostalAddressUse;
import org.openhealthtools.mdht.uml.hl7.vocab.TelecommunicationAddressUse;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.DataTypesTransformerImpl;
import tr.com.srdc.cda2fhir.transform.IDataTypesTransformer;

public class DataTypesTransformerTest {
	IDataTypesTransformer dtt = new DataTypesTransformerImpl();

	@SuppressWarnings("deprecation")
	@Test
	public void testAD2Address() {
		// simple instance test
		AD ad = DatatypesFactory.eINSTANCE.createAD();
		// See https://www.hl7.org/fhir/valueset-address-use.html to see valuset of
		// address use
		ad.getUses().add(PostalAddressUse.H); // PostalAddressUse.H maps to home
		ad.getUses().add(PostalAddressUse.PST); // PST maps to postal
		ad.addText("theText");
		String[] lineArray = new String[2];
		lineArray[0] = "streetLine";
		lineArray[1] = "deliveryLine";
		ad.addStreetAddressLine(lineArray[0]);
		ad.addDeliveryAddressLine(lineArray[1]);
		ad.addCity("theCity");
		ad.addCounty("theDistrict"); // Notice that it is county, not country
		ad.addState("theState");
		ad.addPostalCode("thePostalCode");
		ad.addCountry("theCountry");

		SXCM_TS start = DatatypesFactory.eINSTANCE.createSXCM_TS();
		SXCM_TS end = DatatypesFactory.eINSTANCE.createSXCM_TS();
		start.setValue("19630516");
		end.setValue("20130721");
		ad.getUseablePeriods().add(start);
		ad.getUseablePeriods().add(end);

		Address address = dtt.AD2Address(ad);

		Assert.assertEquals("AD.use was not transformed", "home", address.getUse().toCode());
		Assert.assertEquals("AD.type was not transformed", "postal", address.getType().toCode());
		Assert.assertEquals("AD.text was not transformed", "theText", address.getText());

		// line array check
		int matchingElements = 0;
		for (StringType line : address.getLine()) {
			for (String line2 : lineArray) {
				if (line.getValue().equals(line2)) {
					matchingElements++;
				}
			}
		}

		Assert.assertTrue("AD.line was not transformed", matchingElements == lineArray.length);
		Assert.assertEquals("AD.city was not transformed", "theCity", address.getCity());
		Assert.assertEquals("AD.district was not transformed", "theDistrict", address.getDistrict());
		Assert.assertEquals("AD.state was not transformed", "theState", address.getState());
		Assert.assertEquals("AD.postalCode was not transformed", "thePostalCode", address.getPostalCode());
		Assert.assertEquals("AD.country was not transformed", "theCountry", address.getCountry());

		// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016
		// since 2016-1900 = 116.
		Assert.assertEquals("AD.period.start.year was not transformed", 1963 - 1900,
				address.getPeriod().getStart().getYear());
		// Notice that Date.getMonth() returns THE_MONTH - 1 (since the months are
		// indexed btw the range 0-11)
		Assert.assertEquals("AD.period.start.month was not transformed", 5 - 1,
				address.getPeriod().getStart().getMonth());
		Assert.assertEquals("AD.period.start.date was not transformed", 16, address.getPeriod().getStart().getDate());
		Assert.assertEquals("AD.period.end.year was not transformed", 2013 - 1900,
				address.getPeriod().getEnd().getYear());
		Assert.assertEquals("AD.period.end.month was not transformed", 7 - 1, address.getPeriod().getEnd().getMonth());
		Assert.assertEquals("AD.period.end.date was not transformed", 21, address.getPeriod().getEnd().getDate());

		// instance test: there exists an instance of ED but no setter is called
		AD ad4 = DatatypesFactory.eINSTANCE.createAD();
		Address address4 = dtt.AD2Address(ad4);
		Assert.assertNull("AD.use was not transformed", address4.getUse());
		Assert.assertNull("AD.type was not transformed", address4.getType());
		Assert.assertNull("AD.text was not transformed", address4.getText());

		Assert.assertTrue("AD.line was not transformed", address4.getLine().size() == 0);
		Assert.assertNull("AD.city was not transformed", address4.getCity());
		Assert.assertNull("AD.district was not transformed", address4.getDistrict());
		Assert.assertNull("AD.state was not transformed", address4.getState());
		Assert.assertNull("AD.postalCode was not transformed", address4.getPostalCode());
		Assert.assertNull("AD.country was not transformed", address4.getCountry());

		// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016
		// since 2016-1900 = 116.
		Assert.assertNull("AD.period.start was not transformed", address4.getPeriod().getStart());
		Assert.assertNull("AD.period.end was not transformed", address4.getPeriod().getEnd());

		// null instance test
		AD ad2 = null;
		Address address2 = dtt.AD2Address(ad2);
		Assert.assertNull("AD null instance transform failed", address2);

		// nullFlavor instance test
		AD ad3 = DatatypesFactory.eINSTANCE.createAD();
		ad3.setNullFlavor(NullFlavor.NI);
		Address address3 = dtt.AD2Address(ad3);
		Assert.assertNull("AD.nullFlavor set instance transform failed", address3);
	}

	@Test
	public void testBL2Boolean() {
		// simple instance test
		BL bl = DatatypesFactory.eINSTANCE.createBL();
		bl.setValue(true);
		BooleanType bool = dtt.tBL2Boolean(bl);
		Assert.assertEquals("BL.value was not transformed", true, bool.getValue());

		// null instance test
		BL bl2 = null;
		BooleanType bool2 = dtt.tBL2Boolean(bl2);
		Assert.assertNull("BL null instance transform failed", bool2);

	}

	@Test
	public void testCD2CodeableConcept() {
		// simple instance test
		CD cd = DatatypesFactory.eINSTANCE.createCD();

		cd.setCode("code");
		cd.setCodeSystem("codeSystem");
		cd.setCodeSystemVersion("codeSystemVersion");
		cd.setDisplayName("displayName");

		CodeableConcept codeableConcept = dtt.tCD2CodeableConcept(cd);

		Assert.assertEquals("CD.code transformation failed", "code", codeableConcept.getCoding().get(0).getCode());
		Assert.assertEquals("CD.codeSystem transformation failed", "urn:oid:codeSystem",
				codeableConcept.getCoding().get(0).getSystem());
		Assert.assertEquals("CD.codeSystemVersion transformation failed", "codeSystemVersion",
				codeableConcept.getCoding().get(0).getVersion());
		Assert.assertEquals("CD.displayName transformation failed", "displayName",
				codeableConcept.getCoding().get(0).getDisplay());

		// null instance test
		CD cd2 = null;
		CodeableConcept codeableConcept2 = dtt.tCD2CodeableConcept(cd2);
		Assert.assertNull("CD null instance transform failed", codeableConcept2);

		// nullFlavor instance test
		CD cd3 = DatatypesFactory.eINSTANCE.createCD();
		cd3.setNullFlavor(NullFlavor.NI);
		CodeableConcept codeableConcept3 = dtt.tCD2CodeableConcept(cd3);
		Assert.assertNull("CodeableConcept.nullFlavor set instance transform failed", codeableConcept3);
	}

	@Test
	public void testCV2Coding() {
		// simple instance test
		CV cv = DatatypesFactory.eINSTANCE.createCV();
		cv.setCodeSystem("theCodeSystem");
		cv.setCodeSystemVersion("theCodeSystemVersion");
		cv.setCode("theCode");
		cv.setDisplayName("theDisplayName");

		Coding coding = dtt.tCV2Coding(cv);

		Assert.assertEquals("CV.codeSystem was not transformed", "theCodeSystem", coding.getSystem());
		Assert.assertEquals("CV.codeSystemVersion was not transformed", "theCodeSystemVersion", coding.getVersion());
		Assert.assertEquals("CV.code was not transformed", "theCode", coding.getCode());
		Assert.assertEquals("CV.displayName was not transformed", "theDisplayName", coding.getDisplay());

		// instance test: there exists an instance of CV but no setter is called
		CV cv4 = DatatypesFactory.eINSTANCE.createCV();

		Coding coding4 = dtt.tCV2Coding(cv4);

		Assert.assertNull("CV.codeSystem null value was not transformed properly", coding4.getSystem());
		Assert.assertNull("CV.codeSystemVersion null value was not transformed properly", coding4.getVersion());
		Assert.assertNull("CV.code null value was not transformed properly", coding4.getCode());
		Assert.assertNull("CV.displayName null value was not transformed properly", coding4.getDisplay());

		// null instance test
		CV cv2 = null;
		Coding coding2 = dtt.tCV2Coding(cv2);
		Assert.assertNull("CV null instance transform failed", coding2);

		// nullFlavor instance test
		CV cv3 = DatatypesFactory.eINSTANCE.createCV();
		cv3.setNullFlavor(NullFlavor.NI);
		Coding coding3 = dtt.tCV2Coding(cv3);
		Assert.assertNull("CV.nullFlavor set instance transform failed", coding3);
	}

	@Test
	public void testED2Attachment() {
		// simple instance test
		ED ed = DatatypesFactory.eINSTANCE.createED();
		ed.setMediaType("theMediaType");
		ed.setLanguage("theLanguage");
		ed.addText("theData");
		TEL theTel = DatatypesFactory.eINSTANCE.createTEL();
		theTel.setValue("theUrl");
		ed.setReference(theTel);
		ed.setIntegrityCheck("theIntegrityCheck".getBytes());

		Attachment attachment = dtt.tED2Attachment(ed);
		Assert.assertEquals("ED.mediaType was not transformed", "theMediaType", attachment.getContentType());
		Assert.assertEquals("ED.language was not transformed", "theLanguage", attachment.getLanguage());
		Assert.assertArrayEquals("ED.data was not transformed", "theData".getBytes(), attachment.getData());
		Assert.assertEquals("ED.reference.literal was not transformed", "theUrl", attachment.getUrl());
		Assert.assertArrayEquals("ED.integrityCheck was not transformed", "theIntegrityCheck".getBytes(),
				attachment.getHash());

		// instance test: there exists an instance of ED but no setter is called
		ED ed4 = DatatypesFactory.eINSTANCE.createED();
		Attachment attachment4 = dtt.tED2Attachment(ed4);
		Assert.assertNull("ED.mediaType was not transformed", attachment4.getContentType());
		Assert.assertNull("ED.language was not transformed", attachment4.getLanguage());
		Assert.assertNull("ED.data was not transformed", attachment4.getData());
		Assert.assertNull("ED.reference.literal was not transformed", attachment4.getUrl());
		Assert.assertNull("ED.integrityCheck was not transformed", attachment4.getHash());

		// null instance test
		ED ed2 = null;
		Attachment attachment2 = dtt.tED2Attachment(ed2);
		Assert.assertNull("ED null instance transform failed", attachment2);

		// nullFlavor instance test
		ED ed3 = DatatypesFactory.eINSTANCE.createED();
		ed3.setNullFlavor(NullFlavor.NI);
		Attachment attachment3 = dtt.tED2Attachment(ed3);
		Assert.assertNull("ED.nullFlavor set instance transform failed", attachment3);
	}

	@Test
	public void testEN2HumanName() {
		// simple instance test 1
		EN en = DatatypesFactory.eINSTANCE.createEN();
		// Notice that EntityNameUse.P maps to NameUseEnum.NICKNAME.
		en.getUses().add(EntityNameUse.P);
		en.addText("theText");
		en.addFamily("theFamily");
		en.addGiven("theGiven");
		en.addPrefix("thePrefix");
		en.addSuffix("theSuffix");

		// Data for ivl_ts: low: 19950127, high: 20160228
		IVL_TS ivl_ts = DatatypesFactory.eINSTANCE.createIVL_TS("19950115", "20160228");
		en.setValidTime(ivl_ts);

		HumanName humanName = dtt.tEN2HumanName(en);

		Assert.assertEquals("EN.use was not transformed", "nickname", humanName.getUse().toCode());
		Assert.assertEquals("EN.text was not transformed", "theText", humanName.getText());
		Assert.assertEquals("EN.family was not transformed", "theFamily", humanName.getFamily());
		Assert.assertEquals("EN.given was not transformed", "theGiven", humanName.getGiven().get(0).getValue());
		Assert.assertEquals("EN.prefix was not transformed", "thePrefix", humanName.getPrefix().get(0).getValue());
		Assert.assertEquals("EN.suffix was not transformed", "theSuffix", humanName.getSuffix().get(0).getValue());

		// EN.period tests for the simple instance test 1
		Period en_period = dtt.tIVL_TS2Period(ivl_ts);
		Assert.assertEquals("EN.period(low) was not transformed", en_period.getStart(),
				humanName.getPeriod().getStart());
		Assert.assertEquals("EN.period(high) was not transformed", en_period.getEnd(), humanName.getPeriod().getEnd());

		// instance test: there exists an instance of ED but no setter is called
		EN en4 = DatatypesFactory.eINSTANCE.createEN();
		HumanName humanName4 = dtt.tEN2HumanName(en4);
		Assert.assertNull("EN.use was not transformed", humanName4.getUse());
		Assert.assertNull("EN.text was not transformed", humanName4.getText());
		Assert.assertTrue("EN.family was not transformed", !humanName4.getFamilyElement().hasValue());
		Assert.assertTrue("EN.given was not transformed", humanName4.getGiven().size() == 0);
		Assert.assertTrue("EN.prefix was not transformed", humanName4.getPrefix().size() == 0);
		Assert.assertTrue("EN.suffix was not transformed", humanName4.getSuffix().size() == 0);

		// null instance test
		EN en2 = null;
		HumanName humanName2 = dtt.tEN2HumanName(en2);
		Assert.assertNull("ED null instance transform failed", humanName2);

		// nullFlavor instance test
		EN en3 = DatatypesFactory.eINSTANCE.createEN();
		en3.setNullFlavor(NullFlavor.NI);
		HumanName humanName3 = dtt.tEN2HumanName(en3);
		Assert.assertNull("EN.nullFlavor set instance transform failed", humanName3);
	}

	@Test
	public void testII2Identifier() {
		// simple instance test
		II ii = DatatypesFactory.eINSTANCE.createII();
		ii.setRoot("2.16.840.1.113883.19.5.99999.1");
		ii.setExtension("myIdentifierExtension");
		ii.setAssigningAuthorityName("assigning Authority Name");

		Identifier identifier = dtt.tII2Identifier(ii);
		Assert.assertEquals("II.root was not transformed", "urn:oid:2.16.840.1.113883.19.5.99999.1",
				identifier.getSystem());
		Assert.assertEquals("II.extension was not transformed", "myIdentifierExtension", identifier.getValue());
		Assert.assertEquals("II.assigningAuthorityName was not transformed", "assigning Authority Name",
				identifier.getAssigner().getDisplay());

		// test defaults

		Assert.assertEquals("II.use equals default use", Config.DEFAULT_IDENTIFIER_USE, identifier.getUse());
		Assert.assertEquals("II.type.", Config.DEFAULT_IDENTIFIER_USE, identifier.getUse());

		// null instance test

		II ii2 = null;
		Identifier identifier2 = dtt.tII2Identifier(ii2);
		Assert.assertNull("II null instance was not transformed", identifier2);

		// nullFlavor instance test
		II ii3 = DatatypesFactory.eINSTANCE.createII();
		ii3.setNullFlavor(NullFlavor.MSK);
		Identifier identifier3 = dtt.tII2Identifier(ii3);
		Assert.assertNull("II nullFlavor set instance transform failed", identifier3);

	}

	@Test
	public void testINT2Integer() {
		// simple instance test
		INT myInt = DatatypesFactory.eINSTANCE.createINT();
		myInt.setValue(65);
		IntegerType integer = dtt.tINT2Integer(myInt);

		Assert.assertEquals("INT.value was not transformed", 65.0, integer.getValue().doubleValue(), 0.001);

		// null instance test
		INT int2 = null;
		IntegerType integer2 = dtt.tINT2Integer(int2);
		Assert.assertNull("INT null instance transform failed", integer2);

	}

	@Test
	public void testIVL_PQ2Range() {
		// simple instance test
		IVL_PQ ivlpq = DatatypesFactory.eINSTANCE.createIVL_PQ();
		IVXB_PQ ivxbpqH = DatatypesFactory.eINSTANCE.createIVXB_PQ();
		ivxbpqH.setValue(0.2);
		ivxbpqH.setUnit("unit");
		IVXB_PQ ivxbpqL = DatatypesFactory.eINSTANCE.createIVXB_PQ();
		ivxbpqL.setValue(0.1);
		ivxbpqL.setUnit("unit");

		ivlpq.setHigh(ivxbpqH);
		ivlpq.setLow(ivxbpqL);

		Range range = dtt.tIVL_PQ2Range(ivlpq);

		Assert.assertEquals("IVL_PQ.high.unit was not transformed", ivlpq.getHigh().getUnit(),
				range.getHigh().getUnit());
		Assert.assertEquals("IVL_PQ.high.value was not transformed", ivlpq.getHigh().getValue(),
				range.getHigh().getValue());
		Assert.assertEquals("IVL_PQ.low.unit was not transformed", ivlpq.getLow().getUnit(), range.getLow().getUnit());
		Assert.assertEquals("IVL_PQ.low.value was not transformed", ivlpq.getLow().getValue(),
				range.getLow().getValue());

		// null instance test
		IVL_PQ ivlpq2 = null;
		Range range2 = dtt.tIVL_PQ2Range(ivlpq2);
		Assert.assertNull("IVL_PQ null instance transform failed", range2);

		// nullFlavor instance test
		IVL_PQ ivlpq3 = DatatypesFactory.eINSTANCE.createIVL_PQ();
		ivlpq3.setNullFlavor(NullFlavor.NI);
		Range range3 = dtt.tIVL_PQ2Range(ivlpq3);
		Assert.assertNull("IVL_PQ.nullFlavor set instance transform failed", range3);

		IVL_PQ ivlpq5 = DatatypesFactory.eINSTANCE.createIVL_PQ();
		IVXB_PQ ivxbpqH_2 = DatatypesFactory.eINSTANCE.createIVXB_PQ();
		ivxbpqH_2.setNullFlavor(NullFlavor.NI);
		ivlpq5.setHigh(ivxbpqH_2);

		Range range5 = dtt.tIVL_PQ2Range(ivlpq5);
		Assert.assertNull("IVL_PQ.nullFlavor set instance transform failed", range5.getHigh().getValue());

		// non-null empty instance test
		IVL_PQ ivlpq4 = DatatypesFactory.eINSTANCE.createIVL_PQ();
		Range range4 = dtt.tIVL_PQ2Range(ivlpq4);
		Assert.assertNull("IVL_PQ.high.value transform failed", range4.getHigh().getValue());
		Assert.assertNull("IVL_PQ.low.value transform failed", range4.getLow().getValue());
		Assert.assertNull("IVL_PQ.high.unit transform failed", range4.getHigh().getUnit());
		Assert.assertNull("IVL_PQ.low.unit transform failed", range4.getLow().getUnit());

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testIVL_TS2Period() {
		// simple instance test 1
		IVL_TS ivl_ts = DatatypesFactory.eINSTANCE.createIVL_TS();

		IVXB_TS ivxb_tsLow = DatatypesFactory.eINSTANCE.createIVXB_TS();
		IVXB_TS ivxb_tsHigh = DatatypesFactory.eINSTANCE.createIVXB_TS();

		ivxb_tsLow.setValue("19630116");
		ivxb_tsHigh.setValue("20151122");

		ivl_ts.setLow(ivxb_tsLow);
		ivl_ts.setHigh(ivxb_tsHigh);

		Period period = dtt.tIVL_TS2Period(ivl_ts);

		// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016
		// since 2016-1900 = 116.
		Assert.assertEquals("IVL_TS.low(year) was not transformed", 1963 - 1900, period.getStart().getYear());
		// Notice that Date.getMonth() returns THE_MONTH - 1 (since the months are
		// indexed btw the range 0-11)
		Assert.assertEquals("IVL_TS.low(month) was not transformed", 1 - 1, period.getStart().getMonth());
		Assert.assertEquals("IVL_TS.low(date[1-31]) was not transformed", 16, period.getStart().getDate());
		Assert.assertEquals("IVL_TS.high(year) was not transformed", 2015 - 1900, period.getEnd().getYear());
		Assert.assertEquals("IVL_TS.high(month) was not transformed", 11 - 1, period.getEnd().getMonth());
		Assert.assertEquals("IVL_TS.high(date[1-31]) was not transformed", 22, period.getEnd().getDate());

		// instance test: there exists an instance of ED but no setter is called
		IVL_TS ivl_ts4 = DatatypesFactory.eINSTANCE.createIVL_TS();
		Period period4 = dtt.tIVL_TS2Period(ivl_ts4);
		Assert.assertNull("IVL_TS.low was not transformed", period4.getStart());
		Assert.assertNull("IVL_TS.high(year) was not transformed", period4.getEnd());

		// null instance test
		IVL_TS ivl_ts2 = null;
		Period period2 = dtt.tIVL_TS2Period(ivl_ts2);
		Assert.assertNull("IVL_TS null instance transform failed", period2);

		// nullFlavor instance test
		IVL_TS ivl_ts3 = DatatypesFactory.eINSTANCE.createIVL_TS();
		ivl_ts3.setNullFlavor(NullFlavor.NI);
		Period period3 = dtt.tIVL_TS2Period(ivl_ts3);
		Assert.assertNull("IVL_TS.nullFlavor set instance transform failed", period3);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testPIVL_TS2Timing() {
		// null instance test
		PIVL_TS pivlNull = null;
		Timing timingNull = dtt.tPIVL_TS2Timing(pivlNull);
		Assert.assertNull("PIVL_TS null instance transform failed", timingNull);

		// nullFlavor instance test
		PIVL_TS pivlNullFlavor = DatatypesFactory.eINSTANCE.createPIVL_TS();
		pivlNullFlavor.setNullFlavor(NullFlavor.NA);
		Timing timingNF = dtt.tPIVL_TS2Timing(pivlNullFlavor);
		Assert.assertNull("PIVL_TS.nullFlavor set instance transform failed", timingNF);

		// simple instance tests

		// 1
		PIVL_TS pivl1 = DatatypesFactory.eINSTANCE.createPIVL_TS();
		// period of pivl1
		PQ pq1 = DatatypesFactory.eINSTANCE.createPQ();
		pq1.setValue(123.4);
		pq1.setUnit("h");
		pivl1.setPeriod(pq1);

		// phase of pivl1
		IVL_TS ivlts1 = DatatypesFactory.eINSTANCE.createIVL_TS();
		// low
		IVXB_TS ivxbLow1 = DatatypesFactory.eINSTANCE.createIVXB_TS();
		ivxbLow1.setValue("20140523");
		ivlts1.setLow(ivxbLow1);
		// high
		IVXB_TS ivxbHigh1 = DatatypesFactory.eINSTANCE.createIVXB_TS();
		ivxbHigh1.setValue("20161201");
		ivlts1.setHigh(ivxbHigh1);
		pivl1.setPhase(ivlts1);

		Timing timing1 = dtt.tPIVL_TS2Timing(pivl1);

		BigDecimal bigDecimal = new BigDecimal(123.4);
		Assert.assertTrue("PIVL_TS.period.value was not transformed", 123 == bigDecimal.longValue());
		Period period = (Period) timing1.getRepeat().getBounds();
		// Notice that Date.getYear() returns THE_YEAR - 1900. It returns 116 for 2016
		// since 2016-1900 = 116.
		Assert.assertEquals("PIVL_TS.phase.low.year was not transformed", 2014 - 1900, period.getStart().getYear());
		// Notice that Date.getMonth() returns THE_MONTH - 1 (since the months are
		// indexed btw the range 0-11)
		Assert.assertEquals("PIVL_TS.phase.low.month was not transformed", 5 - 1, period.getStart().getMonth());
		Assert.assertEquals("PIVL_TS.phase.low.date was not transformed", 23, period.getStart().getDate());

		Assert.assertEquals("PIVL_TS.phase.high.year was not transformed", 2016 - 1900, period.getEnd().getYear());
		Assert.assertEquals("PIVL_TS.phase.high.month was not transformed", 12 - 1, period.getEnd().getMonth());
		Assert.assertEquals("PIVL_TS.phase.high.date was not transformed", 1, period.getEnd().getDate());
	}

	@Test
	public void testPQ2Quantity() {
		// simple instance test
		PQ pq = DatatypesFactory.eINSTANCE.createPQ();
		pq.setValue(120.0);
		pq.setUnit("mg");
		Quantity quantity = dtt.tPQ2Quantity(pq);

		Assert.assertEquals("PQ.value was not transformed", 120.0, quantity.getValue().doubleValue(), 0.001);
		Assert.assertEquals("PQ.unit was not transformed", "mg", quantity.getUnit());

		// null instance test
		PQ pq2 = null;
		Quantity quantity2 = dtt.tPQ2Quantity(pq2);
		Assert.assertNull("PQ null instance transform failed", quantity2);

		// nullFlavor instance test
		PQ pq3 = DatatypesFactory.eINSTANCE.createPQ();
		pq3.setNullFlavor(NullFlavor.NI);
		Quantity quantity3 = dtt.tPQ2Quantity(pq3);
		Assert.assertNull("PQ.nullFlavor set instance transform failed", quantity3);

		PQ pq4 = DatatypesFactory.eINSTANCE.createPQ();
		pq4.setValue(25.0);
		pq4.setUnit(null);

		Quantity quantity4 = dtt.tPQ2Quantity(pq4);
		Assert.assertEquals("PQ.value was not transformed", 25.0, quantity4.getValue().doubleValue(), 0.001);
		Assert.assertNull("PQ.unit null was not transformed", quantity4.getUnit());
	}// end Quantity test

	@Test
	public void testREAL2DecimalType() {
		// simple instance test
		REAL real = DatatypesFactory.eINSTANCE.createREAL();
		real.setValue(78965.0);
		DecimalType decimal = dtt.tREAL2DecimalType(real);
		Assert.assertEquals("REAL.value was not transformed", 78965.0, decimal.getValue().doubleValue(), 0.001);

		// null instance test
		REAL real2 = null;
		DecimalType decimal2 = dtt.tREAL2DecimalType(real2);
		Assert.assertNull("REAL null instance transform failed", decimal2);

	}

	@Test
	public void testRTO2Ratio() {
		// simple instance test
		RTO rto = DatatypesFactory.eINSTANCE.createRTO();
		REAL real = DatatypesFactory.eINSTANCE.createREAL();
		real.setValue(65.0);
		REAL real2 = DatatypesFactory.eINSTANCE.createREAL();
		real2.setValue(137.6);
		rto.setNumerator(real);
		rto.setDenominator(real2);
		Ratio ratio = dtt.tRTO2Ratio(rto);
		Assert.assertEquals("RTO.numerator was not transformed", 65.0, ratio.getNumerator().getValue().doubleValue(),
				0.001);
		Assert.assertEquals("RTO.denominator was not transformed", 137.6,
				ratio.getDenominator().getValue().doubleValue(), 0.001);
		// null instance test

		RTO rto2 = null;
		Ratio ratio2 = dtt.tRTO2Ratio(rto2);
		Assert.assertNull("RTO null instance set was failed", ratio2);

		// nullFlavor instance test
		RTO rto3 = DatatypesFactory.eINSTANCE.createRTO();
		rto3.setNullFlavor(NullFlavor.NINF);
		Ratio ratio3 = dtt.tRTO2Ratio(rto3);
		Assert.assertNull("RTO nullFlavor instance set was failed", ratio3);
	}

	@Test
	public void testST2String() {
		// simple instance test
		ST st = DatatypesFactory.eINSTANCE.createST();
		st.addText("theText");
		StringType string = dtt.tST2String(st);
		Assert.assertEquals("ST.text was not transformed", "theText", string.getValue());

		// null instance test
		ST st2 = null;
		StringType string2 = dtt.tST2String(st2);
		Assert.assertNull("ST null instance transform failed", string2);
	}

	@Test
	public void testString2DateTime() {
		// null instance test
		String nullStr = null;
		DateTimeType dateTimeNull = dtt.tString2DateTime(nullStr);
		Assert.assertNull("TS null instance set was failed", dateTimeNull);

		// simple instance tests

		// 1 yyyy
		String str1 = "2016";
		DateTimeType dateTime1 = dtt.tString2DateTime(str1);

		Assert.assertEquals("TS.value was not transformed", "2016", dateTime1.getValueAsString());

		// 2 yyyymm
		String str2 = "201605";
		DateTimeType dateTime2 = dtt.tString2DateTime(str2);

		Assert.assertEquals("TS.value was not transformed", "2016-05", dateTime2.getValueAsString());

		// 3 yyyymmdd
		String str3 = "20160527";
		DateTimeType dateTime3 = dtt.tString2DateTime(str3);
		Assert.assertEquals("TS.value was not transformed", "2016-05-27", dateTime3.getValueAsString());

		// 4 yyyymmddhhmm
		String str4 = "201605271540";
		DateTimeType dateTime4 = dtt.tString2DateTime(str4);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00" + getLocalTimeZoneString(),
				dateTime4.getValueAsString());

		// 5 +timezone
		String str5 = "201605271540+0800";
		DateTimeType dateTime5 = dtt.tString2DateTime(str5);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00+08:00", dateTime5.getValueAsString());

		// 6 -timezone
		String str6 = "201605271540-0800";
		DateTimeType dateTime6 = dtt.tString2DateTime(str6);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00-08:00", dateTime6.getValueAsString());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testTEL2ContactPoint() {

		TEL tel = DatatypesFactory.eINSTANCE.createTEL();

		tel.setValue("tel:+1(555)555-1004");

		SXCM_TS sxcmts = DatatypesFactory.eINSTANCE.createSXCM_TS();
		sxcmts.setValue("19950424");
		SXCM_TS sxcmts2 = DatatypesFactory.eINSTANCE.createSXCM_TS();
		sxcmts2.setValue("19950427");

		tel.getUseablePeriods().add(sxcmts);
		tel.getUseablePeriods().add(sxcmts2);

		tel.getUses().add(TelecommunicationAddressUse.H);

		ContactPoint contactPoint = dtt.tTEL2ContactPoint(tel);
		Assert.assertEquals("Tel.system failed", "phone", contactPoint.getSystem().toCode());
		Assert.assertEquals("Tel.value failed", "+1(555)555-1004", contactPoint.getValue());
		Assert.assertEquals("Tel.periodStart getYear failed", 95, contactPoint.getPeriod().getStart().getYear());
		Assert.assertEquals("Tel.periodStart getMonth failed", 3, contactPoint.getPeriod().getStart().getMonth());
		Assert.assertEquals("Tel.periodStart getMonth failed", 24, contactPoint.getPeriod().getStart().getDate());
		Assert.assertEquals("Tel.periodEnd getYear failed", 95, contactPoint.getPeriod().getEnd().getYear());
		Assert.assertEquals("Tel.periodEnd getMonth failed", 3, contactPoint.getPeriod().getEnd().getMonth());
		Assert.assertEquals("Tel.periodEnd getMonth failed", 27, contactPoint.getPeriod().getEnd().getDate());
		Assert.assertEquals("Tel.use failed", "home", contactPoint.getUse().toCode());

		// null instance test
		TEL tel2 = null;
		ContactPoint contactPoint2 = dtt.tTEL2ContactPoint(tel2);
		Assert.assertNull("TEL null instance transform failed", contactPoint2);

		// nullFlavor instance test
		TEL tel3 = DatatypesFactory.eINSTANCE.createTEL();
		tel3.setNullFlavor(NullFlavor.NI);
		ContactPoint contactPoint3 = dtt.tTEL2ContactPoint(tel3);
		Assert.assertNull("ContactPoint.nullFlavor set instance transform failed", contactPoint3);

		// instance test: non-null empty instance
		TEL tel4 = DatatypesFactory.eINSTANCE.createTEL();
		ContactPoint contactPoint4 = dtt.tTEL2ContactPoint(tel4);

		Assert.assertNull("TEL.value transformation failed", contactPoint4.getValue());
		Assert.assertNull("TEL.period.Start transformation failed", contactPoint4.getPeriod().getStart());
		Assert.assertNull("TEL.period.End transformation failed", contactPoint4.getPeriod().getEnd());
	}

	@Test
	public void testTS2Date() {
		// simple instance test yyyymmdd
		TS ts = DatatypesFactory.eINSTANCE.createTS();
		ts.setValue("20160923");
		DateType date = dtt.tTS2Date(ts);

		Assert.assertEquals("TS.value was not transformed", "2016-09-23", date.getValueAsString());

		// simple instance test 2 yyyymm
		TS ts4 = DatatypesFactory.eINSTANCE.createTS();
		ts4.setValue("201506");
		DateType date4 = dtt.tTS2Date(ts4);
		Assert.assertEquals("TS.value was not transformed", "2015-06", date4.getValueAsString());

		// simple instance test 3 yyyy
		TS ts5 = DatatypesFactory.eINSTANCE.createTS();
		ts5.setValue("2010");
		DateType date5 = dtt.tTS2Date(ts5);
		Assert.assertEquals("TS.value was not transformed", "2010", date5.getValueAsString());

		// simple instance test 4 yyyymmddhhmm
		TS ts6 = DatatypesFactory.eINSTANCE.createTS();
		ts6.setValue("201305141317");
		DateType date6 = dtt.tTS2Date(ts6);
		Assert.assertEquals("TS.value was not transformed", "2013-05-14", date6.getValueAsString());

		// simple instance test 5 yyyymmddhhmmss.s
		TS ts7 = DatatypesFactory.eINSTANCE.createTS();
		ts7.setValue("20130514131719.6");
		DateType date7 = dtt.tTS2Date(ts7);
		Assert.assertEquals("TS.value was not transformed", "2013-05-14", date7.getValueAsString());

		// null instance test
		TS ts2 = null;
		DateType date2 = dtt.tTS2Date(ts2);
		Assert.assertNull("TS null was not transformed", date2);

		// nullFlavor instance test
		TS ts3 = DatatypesFactory.eINSTANCE.createTS();
		ts3.setNullFlavor(NullFlavor.UNK);
		DateType date3 = dtt.tTS2Date(ts3);
		Assert.assertNull("TS.nullFlavor was not transformed", date3);
	}

	@Test
	public void testTS2DateTime() {
		// simple instance test,yyyy
		TS ts = DatatypesFactory.eINSTANCE.createTS();
		ts.setValue("2016");
		DateTimeType datetime = dtt.tTS2DateTime(ts);
		Assert.assertEquals("TS.value was not transformed", "2016", datetime.getValueAsString());

		// simple instance test,yyyymm
		TS ts2 = DatatypesFactory.eINSTANCE.createTS();
		ts2.setValue("201605");
		DateTimeType datetime2 = dtt.tTS2DateTime(ts2);
		Assert.assertEquals("TS.value was not transformed", "2016-05", datetime2.getValueAsString());

		// simple instance test,yyyymmdd
		TS ts3 = DatatypesFactory.eINSTANCE.createTS();
		ts3.setValue("20160527");
		DateTimeType datetime3 = dtt.tTS2DateTime(ts3);
		Assert.assertEquals("TS.value was not transformed", "2016-05-27", datetime3.getValueAsString());

		// simple instance test,yyyymmddhhmm
		TS ts4 = DatatypesFactory.eINSTANCE.createTS();
		ts4.setValue("201605271540");
		DateTimeType datetime4 = dtt.tTS2DateTime(ts4);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00" + getLocalTimeZoneString(),
				datetime4.getValueAsString());

		// complex instance test,with +timezone
		TS ts5 = DatatypesFactory.eINSTANCE.createTS();
		ts5.setValue("201605271540+0800");
		DateTimeType datetime5 = dtt.tTS2DateTime(ts5);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00+08:00", datetime5.getValueAsString());

		// complex instance test,with -timezone
		TS ts7 = DatatypesFactory.eINSTANCE.createTS();
		ts7.setValue("201605271540-0800");
		DateTimeType datetime7 = dtt.tTS2DateTime(ts7);

		Assert.assertEquals("TS.value was not transformed", "2016-05-27T15:40:00-08:00", datetime7.getValueAsString());

		// null instance test
		TS ts6 = null;
		DateTimeType datetime6 = dtt.tTS2DateTime(ts6);
		Assert.assertNull("TS null instance set was failed", datetime6);
	}

	// tTS2Instant, tTS2Date and tTS2DateTime are based on tTS2BaseDateTimeType
	// The most comprehensive test is testTS2Instant
	@Test
	public void testTS2Instant() {
		// null instance test
		TS nullTs = null;
		InstantType nullInstant1 = dtt.tTS2Instant(nullTs);
		Assert.assertNull("TS null was not transformed", nullInstant1);

		// nullFlavor instance test
		TS nullFlavorTs = DatatypesFactory.eINSTANCE.createTS();
		nullFlavorTs.setNullFlavor(NullFlavor.NA);
		InstantType nullInstant2 = dtt.tTS2Instant(nullFlavorTs);
		Assert.assertNull("TS.nullFlavor was not transformed", nullInstant2);

		// simple instance tests

		// 1 yyyy
//    	TS ts1 = DatatypesFactory.eINSTANCE.createTS();
//    	ts1.setValue("2013");
//    	InstantType instant1 = dtt.tTS2Instant(ts1);
//
//    	Assert.assertEquals("TS.value was not transformed","2013",instant1.getValueAsString());

		// 2 yyyymm
//    	TS ts2 = DatatypesFactory.eINSTANCE.createTS();
//    	ts2.setValue("199711");
//    	InstantType instant2 = dtt.tTS2Instant(ts2);
//
//    	Assert.assertEquals("TS.value was not transformed","1997-11",instant2.getValueAsString());

		// 3 yyyymmdd
		TS ts3 = DatatypesFactory.eINSTANCE.createTS();
		ts3.setValue("20160514");
		InstantType instant3 = dtt.tTS2Instant(ts3);
		Assert.assertEquals("TS.value was not transformed", "2016-05-14T00:00:00" + getLocalTimeZoneString(),
				instant3.getValueAsString());

		// 4 yyyymmddhhmm
		TS ts4 = DatatypesFactory.eINSTANCE.createTS();
		ts4.setValue("201305141317");
		InstantType instant4 = dtt.tTS2Instant(ts4);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:00" + getLocalTimeZoneString(),
				instant4.getValueAsString());

		// 5 yyyymmddhhmmss.s
		TS ts5 = DatatypesFactory.eINSTANCE.createTS();
		ts5.setValue("20130514131719.6");
		InstantType instant5 = dtt.tTS2Instant(ts5);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19.600" + getLocalTimeZoneString(),
				instant5.getValueAsString());

		// 6 yyyymmddhhmmss.ss
		TS ts6 = DatatypesFactory.eINSTANCE.createTS();
		ts6.setValue("20130514131719.67");
		InstantType instant6 = dtt.tTS2Instant(ts6);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19.670" + getLocalTimeZoneString(),
				instant6.getValueAsString());

		// 7 yyyymmddhhmmss.sss
		TS ts7 = DatatypesFactory.eINSTANCE.createTS();
		ts7.setValue("20130514131719.673");
		InstantType instant7 = dtt.tTS2Instant(ts7);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19.673" + getLocalTimeZoneString(),
				instant7.getValueAsString());

		// 8 yyyymmddhhmmss.sss+ZZzz
		TS ts8 = DatatypesFactory.eINSTANCE.createTS();
		ts8.setValue("20130514131719.673+0107");
		InstantType instant8 = dtt.tTS2Instant(ts8);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19.673+01:07",
				instant8.getValueAsString());

		// 9 yyyymmddhhmmss.ss-ZZzz
		TS ts9 = DatatypesFactory.eINSTANCE.createTS();
		ts9.setValue("20130514131719.12-0253");
		InstantType instant9 = dtt.tTS2Instant(ts9);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19.120-02:53",
				instant9.getValueAsString());

		// 10 yyyymmddhhmmss
		TS ts10 = DatatypesFactory.eINSTANCE.createTS();
		ts10.setValue("20130514131719");
		InstantType instant10 = dtt.tTS2Instant(ts10);

		Assert.assertEquals("TS.value was not transformed", "2013-05-14T13:17:19" + getLocalTimeZoneString(),
				instant10.getValueAsString());
	}

	// LocalTimeZone is used for the tests of TS2DateTime and TS2Instant
	private String getLocalTimeZoneString() {
		int timeZoneOffset = TimeZone.getDefault().getRawOffset();
		int hour = 0, min = 0;
		String hourString, minString;
		String sign = "+";

		timeZoneOffset /= (1000 * 60); // millisec to minutes
		min = timeZoneOffset % 60;
		hour = timeZoneOffset / 60;

		if (timeZoneOffset < 0) {
			hour *= -1;
			min *= -1;
			sign = "-";
		}

		hourString = hour > 9 ? "" + hour : "0" + hour; // transform "h" to "0h"
		minString = min > 9 ? "" + min : "0" + min; // transform "m" to "0m"

		return sign + hourString + ":" + minString;
	}
}
