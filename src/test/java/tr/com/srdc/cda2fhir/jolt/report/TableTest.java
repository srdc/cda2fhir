package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import tr.com.srdc.cda2fhir.jolt.report.impl.NotNullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.NullCondition;
import tr.com.srdc.cda2fhir.jolt.report.impl.OrCondition;

public class TableTest {
	private void compare(Table actual, Table expected) {
		int count = expected.rowCount();
		Assert.assertEquals("Table row count", count, actual.rowCount());
		List<TableRow> actualRows = actual.getRows();
		List<TableRow> expectedRows = expected.getRows();
		for (int index = 0; index < count; ++index) {
			TableRow actualRow = actualRows.get(index);
			TableRow expectedRow = expectedRows.get(index);
			Assert.assertEquals("Table row " + index, expectedRow.toString(), actualRow.toString());
		}
	}

	private static Table getExpectedIndicationEffectiveTable() {
		Table result = new Table();

		TableRow row0 = new TableRow("high.value", "abatementDateTime");
		ICondition condition00 = new NullCondition("high.nullFlavor");
		ICondition condition01 = new NullCondition("nullFlavor");
		row0.addCondition(condition00);
		row0.addCondition(condition01);
		result.addRow(row0);

		TableRow row1 = new TableRow("low.value", "onsetDateTime");
		ICondition condition10 = new NullCondition("low.nullFlavor");
		ICondition condition11 = new NullCondition("nullFlavor");
		row1.addCondition(condition10);
		row1.addCondition(condition11);
		result.addRow(row1);

		TableRow row3 = new TableRow("value", "onsetDateTime");
		ICondition condition30a = new NotNullCondition("low.nullFlavor");
		ICondition condition30b = new NullCondition("low");
		ICondition condition30 = new OrCondition(condition30a, condition30b);
		ICondition condition31 = new NullCondition("nullFlavor");
		row3.addCondition(condition30);
		row3.addCondition(condition31);
		result.addRow(row3);

		TableRow row4 = new TableRow("#high", "clinicalStatus");
		ICondition condition40 = new NullCondition("high.nullFlavor");
		ICondition condition41 = new NullCondition("nullFlavor");
		row4.addCondition(condition40);
		row4.addCondition(condition41);
		row4.setFormat("conditionClinicalStatusAdapter");
		result.addRow(row4);

		TableRow row5 = new TableRow("#low", "clinicalStatus");
		ICondition condition50 = new NullCondition("low.nullFlavor");
		ICondition condition51 = new NullCondition("nullFlavor");
		row5.addCondition(condition50);
		row5.addCondition(condition51);
		row5.setFormat("conditionClinicalStatusAdapter");
		result.addRow(row5);

		TableRow row6 = new TableRow("#value", "clinicalStatus");
		ICondition condition60a = new NotNullCondition("low.nullFlavor");
		ICondition condition60b = new NullCondition("low");
		ICondition condition60 = new OrCondition(condition60a, condition60b);
		ICondition condition61 = new NullCondition("nullFlavor");
		row6.addCondition(condition60);
		row6.addCondition(condition61);
		row6.setFormat("conditionClinicalStatusAdapter");
		result.addRow(row6);

		result.sort();
		return result;
	}

	@Test
	public void testIndicationEffectiveTime() throws Exception {
		JoltTemplate template = Main.readTemplate("intermediate/IndicationEffectiveTime");
		Table actual = template.createTable(Collections.<String, JoltTemplate>emptyMap());
		actual.sort();
		Table expected = getExpectedIndicationEffectiveTable();
		compare(actual, expected);
	}
}
