package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

public class Templates {
	private String rootResourceType;
	private Map<String, JoltTemplate> templates;
	private JoltFormat formatMap;

	public Templates() {
	}

	public Templates(JoltFormat formatMap) {
		this.formatMap = formatMap;
	}

	public Templates(String rootResource, Map<String, JoltTemplate> templates, JoltFormat formatMap) {
		this.rootResourceType = rootResource;
		this.templates = templates;
		this.formatMap = formatMap;
	}

	public boolean doesGenerateResource(String name) {
		if (templates != null) {
			JoltTemplate template = templates.get(name);
			if (template != null) {
				return template.doesGenerateResource();
			}
		}
		return false;
	}

	public String getRootResource() {
		return rootResourceType;
	}

	public String getFormat(String target) {
		if (formatMap == null) {
			return "";
		}
		String result = formatMap.get(target);
		if (result != null) {
			return result;
		}
		if (target.indexOf('[') >= 0) {
			String singular = target.split("\\[")[0];
			result = formatMap.get(singular);
			if (result != null) {
				return result;
			}
			if (target.indexOf("[0]") >= 0) {
				String cleanTarget = target.replace("[0]", "[]");
				result = formatMap.get(cleanTarget);
				if (result != null) {
					return result;
				}
			}
		}
		return "";
	}
}
