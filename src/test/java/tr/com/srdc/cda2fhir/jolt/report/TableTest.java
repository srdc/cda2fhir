package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

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
		JoltCondition condition00 = new JoltCondition("high.nullFlavor", "isnull");
		JoltCondition condition01 = new JoltCondition("nullFlavor", "isnull");
		row0.addCondition(condition00.toString());
		row0.addCondition(condition01.toString());
		result.addRow(row0);

		TableRow row1 = new TableRow("low.value", "onsetDateTime");
		JoltCondition condition10 = new JoltCondition("low.nullFlavor", "isnull");
		JoltCondition condition11 = new JoltCondition("nullFlavor", "isnull");
		row1.addCondition(condition10.toString());
		row1.addCondition(condition11.toString());
		result.addRow(row1);

		TableRow row2 = new TableRow("value", "onsetDateTime");
		JoltCondition condition20 = new JoltCondition("low", "isnull");
		JoltCondition condition21 = new JoltCondition("nullFlavor", "isnull");
		row2.addCondition(condition20.toString());
		row2.addCondition(condition21.toString());
		result.addRow(row2);

		TableRow row3 = new TableRow("value", "onsetDateTime");
		JoltCondition condition30 = new JoltCondition("low.nullFlavor", "isnotnull");
		JoltCondition condition31 = new JoltCondition("nullFlavor", "isnull");
		row3.addCondition(condition30.toString());
		row3.addCondition(condition31.toString());
		result.addRow(row3);

		TableRow row4 = new TableRow("#high", "abatementDateTime");
		JoltCondition condition40 = new JoltCondition("high.nullFlavor", "isnull");
		JoltCondition condition41 = new JoltCondition("nullFlavor", "isnull");
		row4.addCondition(condition40.toString());
		row4.addCondition(condition41.toString());
		result.addRow(row4);

		TableRow row5 = new TableRow("#low", "onsetDateTime");
		JoltCondition condition50 = new JoltCondition("low.nullFlavor", "isnull");
		JoltCondition condition51 = new JoltCondition("nullFlavor", "isnull");
		row5.addCondition(condition50.toString());
		row5.addCondition(condition51.toString());
		result.addRow(row5);

		TableRow row6 = new TableRow("#value", "onsetDateTime");
		JoltCondition condition60 = new JoltCondition("low", "isnull");
		JoltCondition condition61 = new JoltCondition("nullFlavor", "isnull");
		row6.addCondition(condition60.toString());
		row6.addCondition(condition61.toString());
		result.addRow(row6);

		TableRow row7 = new TableRow("#value", "onsetDateTime");
		JoltCondition condition70 = new JoltCondition("low.nullFlavor", "isnotnull");
		JoltCondition condition71 = new JoltCondition("nullFlavor", "isnull");
		row7.addCondition(condition70.toString());
		row7.addCondition(condition71.toString());
		result.addRow(row7);

		result.sort();
		return result;
	}

	@Ignore
	@Test
	public void testIndicationEffectiveTime() throws Exception {
		JoltTemplate template = Main.readTemplate("intermediate/IndicationEffectiveTime");
		Table actual = template.createTable(Collections.<String, JoltTemplate>emptyMap());
		actual.sort();
		Table expected = getExpectedIndicationEffectiveTable();
		compare(actual, expected);
	}
}
