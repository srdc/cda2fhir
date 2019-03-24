package tr.com.srdc.cda2fhir.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.HumanName;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityNameUse;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import com.bazaarvoice.jolt.JsonUtils;

public class PNGenerator {
	private static Map<String, Object> NAME_USE = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/NameUse.json");

	private static final String USE = "C";
	private static final String GIVEN = "JOE";
	private static final String FAMILY = "DOE";
	private static final String PREFIX = "Dr";
	private static final String SUFFIX = "Jr";

	private String nullFlavor;
	private String use;

	private String family;
	private List<String> givens = new ArrayList<>();

	private List<String> prefixes = new ArrayList<>();
	private List<String> suffixes = new ArrayList<>();

	public PNGenerator() {
	}

	public PNGenerator(Map<String, Object> json) {
		use = (String) json.get("use");

		family = (String) json.get("family");

		OrgJsonUtil.copyStringArray(json, givens, "given");

		OrgJsonUtil.copyStringArray(json, prefixes, "prefix");
		OrgJsonUtil.copyStringArray(json, suffixes, "suffix");
	}

	public void setNullFlavor() {
		nullFlavor = "UNK";
	}

	public void setUse(String use) {
		this.use = use;
	}

	public boolean hasNullFlavor() {
		return nullFlavor != null;
	}

	public PN generate(CDAFactories factories) {
		PN pn = factories.datatype.createPN();

		if (use != null) {
			EntityNameUse enu = EntityNameUse.get(use);
			if (enu == null) {
				throw new TestSetupException("Invalid 'use' value for PN.");
			}
			pn.getUses().add(enu);
		}

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

		pn.use = USE;
		pn.family = FAMILY;
		pn.givens.add(GIVEN + "_1");
		pn.givens.add(GIVEN + "_2");
		pn.prefixes.add(PREFIX + "_1");
		pn.prefixes.add(PREFIX + "_2");
		pn.suffixes.add(SUFFIX + "_1");
		pn.suffixes.add(SUFFIX + "_2");

		return pn;
	}

	public void verify(HumanName humanName) {
		if (nullFlavor != null) {
			Assert.assertNull("Human name", humanName);
			return;
		}

		if (use != null) {
			String expected = (String) NAME_USE.get(use);
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
	}

	public Map<String, Object> toJson() {
		Map<String, Object> result = new LinkedHashMap<>();

		if (use != null && nullFlavor == null) {
			String field = (String) NAME_USE.get(use);
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
		if (result.isEmpty())
			return null;
		return result;
	}

	public static Set<String> getAvailableUses() {
		return Collections.unmodifiableSet(NAME_USE.keySet());
	}
}
