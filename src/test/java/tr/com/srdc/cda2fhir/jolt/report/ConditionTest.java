package tr.com.srdc.cda2fhir.jolt.report;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import tr.com.srdc.cda2fhir.jolt.report.impl.EqualCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NotEqualCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NotNullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NullCondition;

public class ConditionTest {
	@Test
	public void testSet() throws Exception {
		ICondition a = new NullCondition("a");
		ICondition b = new NotNullCondition("b");
		ICondition c = new EqualCondition("c", "x");
		ICondition d = new NotEqualCondition("d", "y");

		ICondition a2 = new NullCondition("a");
		ICondition b2 = new NotNullCondition("b");
		ICondition c2 = new EqualCondition("c", "x");
		ICondition d2 = new NotEqualCondition("d", "y");

		Set<ICondition> set = new HashSet<>();

		set.add(a);
		set.add(b);
		set.add(c);
		set.add(d);

		set.add(a2);
		set.add(b2);
		set.add(c2);
		set.add(d2);

		Assert.assertEquals("Set count", 4, set.size());
	}
}
