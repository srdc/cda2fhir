package tr.com.srdc.cda2fhir.jolt.report;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.jolt.Utility;

public class Main {
	final private static String PATH = "src/test/resources/jolt/";

	public static Path getTemplatePath(String templateIdentifier) {
		String[] pieces = templateIdentifier.split("/");
		if (pieces.length < 2) { // default
			return Paths.get("data-type", pieces[0] + ".json");
		}
		return Paths.get(pieces[0], pieces[1] + ".json");
	}

	private static String getTemplateName(String templateIdentifier) {
		String[] pieces = templateIdentifier.split("/");
		if (pieces.length < 2) { // default
			return templateIdentifier;
		}
		return pieces[1];
	}

	@SuppressWarnings("unchecked")
	public static JoltTemplate readTemplate(String templateIdentifier) {
		Path templatePath = getTemplatePath(templateIdentifier);
		Path fullTemplatePath = Paths.get(PATH.toString(), templatePath.toString());
		Object content = JsonUtils.filepathToObject(fullTemplatePath.toString());
		if (!(content instanceof List)) {
			throw new ReportException("Invalid content for " + templateIdentifier + ".");
		}
		List<Object> contentAsList = (List<Object>) content;
		String name = getTemplateName(templateIdentifier);
		return JoltTemplate.getInstance(name, contentAsList);
	}

	private static Map<String, JoltTemplate> createHandlers() {
		Map<String, List<Object>> rawTemplates = Utility.readTemplates();
		if (rawTemplates == null) {
			return null;
		}
		return rawTemplates.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> JoltTemplate.getInstance(e.getKey(), e.getValue())));
	}

	public static Table transformationTable(String name) {
		Map<String, JoltTemplate> templateMap = createHandlers();
		if (templateMap != null) {
			JoltTemplate template = templateMap.get(name);
			Table table = template.createTable(templateMap);
			table.sort();
			return table;
		}
		return null;

	}

	public static String transformationCSV(String name) {
		Table table = transformationTable(name);
		if (table != null) {
			String output = table.toCsv();
			return output;
		}
		return null;
	}

	public static String transformationText(String name) {
		Table table = transformationTable(name);
		if (table != null) {
			String output = table.toString();
			return output;
		}
		return null;
	}

	public static void main(String[] args) {
		String output = transformationCSV("ProcedureActivityProcedure");
		System.out.print(output);
	}
}
