package tr.com.srdc.cda2fhir.util;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import tr.com.srdc.cda2fhir.conf.Config;

public class FHIRUtil {

	private static IParser jsonParser;
	private static IParser xmlParser;

	private final static Logger logger = LoggerFactory.getLogger(FHIRUtil.class);

	static {
		jsonParser = Config.getFhirContext().newJsonParser();
		xmlParser = Config.getFhirContext().newXmlParser();
		jsonParser.setPrettyPrint(true);
		xmlParser.setPrettyPrint(true);
	}

	public static String encodeToJSON(IBaseResource res) {
		return jsonParser.encodeResourceToString(res);
	}

	public static <T extends IBaseResource> String encodeToJSON(Collection<T> resources) {
		String[] objects = resources.stream().map(r -> encodeToJSON(r)).toArray(String[]::new);
		return "[" + String.join(", ", objects) + "]";
	}

	public static String encodeToXML(IBaseResource res) {
		return xmlParser.encodeResourceToString(res);
	}

	public static void printJSON(IBaseResource res) {
		System.out.println(jsonParser.encodeResourceToString(res));
	}

	public static <T extends IBaseResource> void printJSON(Collection<T> resources) {
		System.out.println(encodeToJSON(resources));
	}

	public static void printXML(IBaseResource res) {
		System.out.println(xmlParser.encodeResourceToString(res));
	}

	public static void printJSON(IBaseResource res, String filePath) throws IOException {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		try {
			FileWriter fw = new FileWriter(f);
			jsonParser.encodeResourceToWriter(res, fw);
			fw.close();

		} catch (IOException ie) {
			logger.error("Could not print FHIR JSON to file", ie);
			throw new IOException(ie);
		} catch (DataFormatException de) {
			logger.error("Could not print FHIR JSON to file", de);
			throw new DataFormatException(de);
		}
	}

	public static <T extends IBaseResource> void printJSON(Collection<T> resources, String filePath) {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		try {
			String json = encodeToJSON(resources);
			FileWriter fw = new FileWriter(f);
			fw.write(json);
			fw.close();
		} catch (IOException e) {
			logger.error("Could not print FHIR JSON to file", e);
		}
	}

	public static void printXML(IBaseResource res, String filePath) {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		try {
			xmlParser.encodeResourceToWriter(res, new FileWriter(f));
		} catch (IOException e) {
			logger.error("Could not print FHIR XML to file", e);
		}
	}

	public static void printJSON(IBaseResource res, Writer writer) {
		try {
			jsonParser.encodeResourceToWriter(res, writer);
		} catch (IOException e) {
			logger.error("Could not print FHIR JSON to writer", e);
		}
	}

	public static void printXML(IBaseResource res, Writer writer) {
		try {
			xmlParser.encodeResourceToWriter(res, writer);
		} catch (IOException e) {
			logger.error("Could not print FHIR XML to writer", e);
		}
	}

	public static <T extends Resource> List<T> findResources(Bundle bundle, Class<T> type) {
		return bundle.getEntry().stream().map(b -> b.getResource()).filter(r -> type.isInstance(r))
				.map(r -> type.cast(r)).collect(Collectors.toList());
	}

	public static void mergeBundle(Bundle source, Bundle target) {
		if (source == null || target == null) {
			return;
		}

		for (BundleEntryComponent entry : source.getEntry()) {
			if (entry != null) {
				target.addEntry(entry);
			}
		}
	}

	public static Map<String, Resource> getIdResourceMap(Bundle bundle) {
		Map<String, Resource> result = new HashMap<String, Resource>();
		bundle.getEntry().stream().map(e -> e.getResource()).forEach(r -> result.put(r.getId(), r));
		return result;
	}

	interface ResourcePredicate {
		boolean get(Resource resource);
	}
}
