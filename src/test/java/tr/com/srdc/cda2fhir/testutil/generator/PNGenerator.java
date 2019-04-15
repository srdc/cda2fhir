package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	private ENXPGenerator family;
	private List<ENXPGenerator> givens = new ArrayList<>();

	private List<ENXPGenerator> prefixes = new ArrayList<>();
	private List<ENXPGenerator> suffixes = new ArrayList<>();

	private IVL_TSPeriodGenerator ivlTsGenerator;

	public PNGenerator() {
	}

	@SuppressWarnings("unchecked")
	public static void copyStringArray(Map<String, Object> source, List<ENXPGenerator> target, String key) {
		List<Object> sourceArray = (List<Object>) source.get(key);
		if (sourceArray != null) {
			sourceArray.forEach(e -> {
				String value = (String) e;
				if (value != null) {
					ENXPGenerator g = new ENXPGenerator(value);
					target.add(g);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	public PNGenerator(Map<String, Object> json) {
		JoltUtil.copyStringArray(json, uses, "use");

		family = new ENXPGenerator((String) json.get("family"));

		copyStringArray(json, givens, "given");

		copyStringArray(json, prefixes, "prefix");
		copyStringArray(json, suffixes, "suffix");

		text = (String) json.get("content");

		if (json.containsKey("validTime")) {
			ivlTsGenerator = new IVL_TSPeriodGenerator((Map<String, Object>) json.get("validTime"));
		}
	}

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public void setFamilyNullFlavor() {
		if (family == null) {
			family = ENXPGenerator.getNextInstance(true);
		}
		family.setNullFlavor("UNK");
	}

	public void setGivensNullFlavor() {
		if (givens.isEmpty()) {
			givens.add(ENXPGenerator.getNextInstance(true));
		}
		givens.forEach(given -> given.setNullFlavor("UNK"));
	}

	public void setFamilyGenerator(ENXPGenerator generator) {
		family = generator;
	}

	public void setGivensGenerator(ENXPGenerator generator) {
		givens.clear();
		givens.add(generator);
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
			pn.getFamilies().add(family.generate(factories));
		}

		givens.forEach(given -> pn.getGivens().add(given.generate(factories)));
		prefixes.forEach(prefix -> pn.getPrefixes().add(prefix.generate(factories)));
		suffixes.forEach(suffix -> pn.getSuffixes().add(suffix.generate(factories)));

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

		pn.family = new ENXPGenerator(FAMILY, true);
		pn.givens.add(new ENXPGenerator(GIVEN, true));

		return pn;
	}

	public static PNGenerator getFullInstance() {
		PNGenerator pn = new PNGenerator();

		pn.uses.add(USE);
		pn.family = new ENXPGenerator(FAMILY, true);
		pn.givens.add(new ENXPGenerator(GIVEN + "_1", true));
		pn.givens.add(new ENXPGenerator(GIVEN + "_2", true));
		pn.prefixes.add(new ENXPGenerator(PREFIX + "_1", true));
		pn.prefixes.add(new ENXPGenerator(PREFIX + "_2", true));
		pn.suffixes.add(new ENXPGenerator(SUFFIX + "_1", true));
		pn.suffixes.add(new ENXPGenerator(SUFFIX + "_2", true));
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
			family.verify(humanName.getFamily());
		}

		if (givens.isEmpty()) {
			Assert.assertTrue("Missing given name", !humanName.hasGiven());
		} else {
			BaseStringGenerator.verifyList(humanName.getGiven(), givens);
		}

		if (prefixes.isEmpty()) {
			Assert.assertTrue("Missing name prefix", !humanName.hasPrefix());
		} else {
			BaseStringGenerator.verifyList(humanName.getPrefix(), prefixes);
		}

		if (suffixes.isEmpty()) {
			Assert.assertTrue("Missing name suffix", !humanName.hasSuffix());
		} else {
			BaseStringGenerator.verifyList(humanName.getSuffix(), suffixes);
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
			result.put("family", family.getValue());
		}

		if (!givens.isEmpty() && nullFlavor == null) {
			List<Object> values = givens.stream().map(r -> r.getValue()).collect(Collectors.toList());
			result.put("given", new ArrayList<>(values));
		}

		if (!prefixes.isEmpty() && nullFlavor == null) {
			List<Object> values = prefixes.stream().map(r -> r.getValue()).collect(Collectors.toList());
			result.put("prefix", new ArrayList<>(values));
		}

		if (!suffixes.isEmpty() && nullFlavor == null) {
			List<Object> values = suffixes.stream().map(r -> r.getValue()).collect(Collectors.toList());
			result.put("suffix", new ArrayList<>(values));
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

	public static void verifyList(List<HumanName> actual, List<PNGenerator> expected) {
		Assert.assertEquals("Human name count", expected.size(), actual.size());
		for (int index = 0; index < actual.size(); ++index) {
			expected.get(index).verify(actual.get(index));
		}
	}
}
