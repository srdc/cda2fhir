package tr.com.srdc.cda2fhir.jolt.report;

import java.util.Map;

public class Templates {
	private String rootResourceType;
	private Map<String, JoltTemplate> templates;
	private JoltFormat formatMap;
	
	public Templates(String rootResource, Map<String, JoltTemplate> templates, JoltFormat formatMap) {
		this.rootResourceType = rootResource;
		this.templates = templates;
		this.formatMap = formatMap;
	}

	public boolean doesGenerateResource(String name) {
		JoltTemplate template = templates.get(name);
		if (template != null) {
			return template.doesGenerateResource();
		}
		return false;
	}

	public String getRootResource() {
		return rootResourceType;
	}
	
	public String getFormat(String target) {
		String result = formatMap.get(target);
		return result == null ? "" : result;
	}
}
