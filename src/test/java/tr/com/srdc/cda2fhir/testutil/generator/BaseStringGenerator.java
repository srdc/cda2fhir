package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.List;

import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.CDAUtilExtension;

public abstract class BaseStringGenerator<T extends ED> {
	private static int INDEX = 1;

	private String value;
	private String nullFlavor;
	private boolean nullFlavorOK = false;

	public BaseStringGenerator(boolean nullFlavorOK) {
		this.nullFlavorOK = nullFlavorOK;
	}

	public BaseStringGenerator(String value) {
		this.value = value;
	}

	public BaseStringGenerator(String value, boolean nullFlavorOK) {
		this.value = value;
		this.nullFlavorOK = nullFlavorOK;
	}

	public void setNullFlavor(String nullFlavor) {
		this.nullFlavor = nullFlavor;
	}

	protected abstract T create(CDAFactories factories);

	public T generate(CDAFactories factories) {
		T t = create(factories);

		if (value != null) {
			t.addText(value);
		}

		if (nullFlavor != null) {
			NullFlavor nf = CDAUtilExtension.toNullFlavor(nullFlavor);
			t.setNullFlavor(nf);
		}

		return t;
	}

	public String getValue() {
		return this.value;
	}

	public static String getNextValue() {
		String value = "CDA Text " + INDEX;
		++INDEX;
		return value;
	}

	public void verify(String text) {
		if (!hasValue()) {
			Assert.assertNull("No text", text);
		} else {
			Assert.assertEquals("Text", value, text);
		}
	}

	public boolean hasValue() {
		return value != null && (nullFlavorOK || nullFlavor == null);
	}

	public static <T extends ED> void verifyList(List<StringType> actual,
			List<? extends BaseStringGenerator<T>> expected) {
		int actualIndex = 0;
		for (BaseStringGenerator<T> expectedElement : expected) {
			if (expectedElement.hasValue()) {
				StringType actualElement = actual.get(actualIndex);
				Assert.assertNotNull("String type element exists", actualElement);
				expectedElement.verify(actualElement.asStringValue());
				++actualIndex;
			}
		}
		if (actualIndex > 0) {
			Assert.assertEquals("String type count", actualIndex, actual.size());
		} else {
			Assert.assertTrue("Default element", actual.size() == 1);
			Assert.assertTrue("No string type", !actual.get(0).hasValue());
		}
	}
}
