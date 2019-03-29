package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.HumanName;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.JoltUtil;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class PNGenerator {
	private static Map<String, Object> NAME_USE = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/NameUse.json");

	private static final String USE = "C";
	private static final String GIVEN = "JOE";
	private static final String FAMILY = "DOE";
	private static final String PREFIX = "Dr";
	private static final String SUFFIX = "Jr";

	private String nullFlavor;
	private List<String> uses = new ArrayList<>();;
	private String text;

	private String family;
	private List<String> givens = new ArrayList<>();

	private List<String> prefixes = new ArrayList<>();
	private List<String> suffixes = new ArrayList<>();

	private IVL_TSPeriodGenerator ivlTsGenerator;

	public PNGenerator() {
	}

	@SuppressWarnings("unchecked")
	public PNGenerator(Map<String, Object> json) {
		JoltUtil.copyStringArray(json, uses, "use");

		family = (String) json.get("family");

		JoltUtil.copyStringArray(json, givens, "given");

		JoltUtil.copyStringArray(json, prefixes, "prefix");
		JoltUtil.copyStringArray(json, suffixes, "suffix");

		text = (String) json.get("content");

		if (json.containsKey("validTime")) {
			ivlTsGenerator = new IVL_TSPeriodGenerator((Map<String, Object>) json.get("validTime"));
		}
	}

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public void addUse(String use) {
		this.uses.add(use);
	}

	public boolean hasNullFlavor() {
		return nullFlavor != null;
	}

	public PN generate(CDAFactories factories) {
		PN pn = factories.datatype.createPN();

		uses.forEach(use -> {
			EntityNameUse enu = EntityNameUse.get(use);
			if (enu == null) {
				throw new TestSetupException("Invalid 'use' value for PN.");
			}
			pn.getUses().add(enu);
		});

		if (family != null) {
			pn.addFamily(family);
		}

		givens.forEach(given -> pn.addGiven(given));
		prefixes.forEach(prefix -> pn.addPrefix(prefix));
		suffixes.forEach(suffix -> pn.addSuffix(suffix));

		if (nullFlavor != null) {
			NullFlavor nf = NullFlavor.get(nullFlavor);
			if (nf == null) {
				throw new TestSetupException("Invalid null flavor enumeration.");
			}
			pn.setNullFlavor(nf);
		}

		if (ivlTsGenerator != null) {
			IVL_TS ivlTs = ivlTsGenerator.generate(factories);
			pn.setValidTime(ivlTs);
		}

		if (text != null) {
			pn.addText(text);
		}

		return pn;
	}

	public static PNGenerator getDefaultInstance() {
		PNGenerator pn = new PNGenerator();

		pn.family = FAMILY;
		pn.givens.add(GIVEN);

		return pn;
	}

	public static PNGenerator getFullInstance() {
		PNGenerator pn = new PNGenerator();

		pn.uses.add(USE);
		pn.family = FAMILY;
		pn.givens.add(GIVEN + "_1");
		pn.givens.add(GIVEN + "_2");
		pn.prefixes.add(PREFIX + "_1");
		pn.prefixes.add(PREFIX + "_2");
		pn.suffixes.add(SUFFIX + "_1");
		pn.suffixes.add(SUFFIX + "_2");
		pn.text = "The Text";

		pn.ivlTsGenerator = IVL_TSPeriodGenerator.getFullInstance();

		return pn;
	}

	public void verify(HumanName humanName) {
		if (nullFlavor != null) {
			Assert.assertNull("Human name", humanName);
			return;
		}

		if (uses.isEmpty()) {
			Assert.assertTrue("Missing name use", !humanName.hasUse());
		} else {
			String lastUse = uses.get(uses.size() - 1);
			String expected = (String) NAME_USE.get(lastUse);
			String actual = humanName.getUse().toCode();
			if (expected == null) {
				Assert.assertEquals("Name use", actual, "usual");
			} else {
				Assert.assertEquals("Name use", actual, expected);
			}
		}

		if (family == null) {
			Assert.assertTrue("Missing family name", !humanName.hasFamily());
		} else {
			Assert.assertEquals("Family name", family, humanName.getFamily());
		}

		if (givens.isEmpty()) {
			Assert.assertTrue("Missing given name", !humanName.hasGiven());
		} else
			for (int index = 0; index < givens.size(); ++index) {
				String given = givens.get(index);
				String actual = humanName.getGiven().get(index).asStringValue();
				String msg = String.format("Given name (%s)", index);
				Assert.assertEquals(msg, given, actual);
				++index;
			}

		if (prefixes.isEmpty()) {
			Assert.assertTrue("Missing name prefix", !humanName.hasPrefix());
		} else
			for (int index = 0; index < prefixes.size(); ++index) {
				String prefix = prefixes.get(index);
				String actual = humanName.getPrefix().get(index).asStringValue();
				String msg = String.format("Name prefix (%s)", index);
				Assert.assertEquals(msg, prefix, actual);
				++index;
			}

		if (suffixes.isEmpty()) {
			Assert.assertTrue("Missing name suffix", !humanName.hasSuffix());
		} else
			for (int index = 0; index < suffixes.size(); ++index) {
				String suffix = suffixes.get(index);
				String actual = humanName.getSuffix().get(index).asStringValue();
				String msg = String.format("Name suffix (%s)", index);
				Assert.assertEquals(msg, suffix, actual);
				++index;
			}

		if (ivlTsGenerator == null) {
			Assert.assertTrue("Missing name valid time", !humanName.hasPeriod());
		} else {
			ivlTsGenerator.verify(humanName.getPeriod());
		}

		if (text == null) {
			Assert.assertTrue("Missing name text", !humanName.hasText());
		} else {
			Assert.assertEquals("Name text", text, humanName.getText());

		}
	}

	public Map<String, Object> toJson() {
		Map<String, Object> result = new LinkedHashMap<>();

		if (!uses.isEmpty() && nullFlavor == null) {
			String lastUse = uses.get(uses.size() - 1);
			String field = (String) NAME_USE.get(lastUse);
			if (field == null) {
				field = "usual";
			}
			result.put("use", field);
		}

		if (family != null && nullFlavor == null) {
			result.put("family", family);
		}

		if (!givens.isEmpty() && nullFlavor == null) {
			result.put("given", new ArrayList<>(givens));
		}

		if (!prefixes.isEmpty() && nullFlavor == null) {
			result.put("prefix", new ArrayList<>(prefixes));
		}

		if (!suffixes.isEmpty() && nullFlavor == null) {
			result.put("suffix", new ArrayList<>(suffixes));
		}
		if (text != null && nullFlavor == null) {
			result.put("text", text);
		}
		if (ivlTsGenerator != null) {
			Map<String, Object> ivlTsValue = ivlTsGenerator.toJson();
			result.put("period", ivlTsValue);
		}
		if (result.isEmpty())
			return null;
		return result;
	}

	public static Set<String> getAvailableUses() {
		return Collections.unmodifiableSet(NAME_USE.keySet());
	}
}
