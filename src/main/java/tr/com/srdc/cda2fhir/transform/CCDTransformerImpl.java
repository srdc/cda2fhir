package tr.com.srdc.cda2fhir.transform;

import java.io.FileInputStream;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerImpl implements ICDATransformer, Serializable {

	private static final long serialVersionUID = 1L;

	private int counter;
	private IdGeneratorEnum idGenerator;
	private IResourceTransformer resTransformer;
	private Reference patientRef;

	private List<CDASectionTypeEnum> supportedSectionTypes = new ArrayList<CDASectionTypeEnum>();

	private final Logger logger = LoggerFactory.getLogger(CCDTransformerImpl.class);

	/**
	 * Default constructor that initiates with a UUID resource id generator
	 */
	public CCDTransformerImpl() {
		this.counter = 0;
		// The default resource id pattern is UUID
		this.idGenerator = IdGeneratorEnum.UUID;
		resTransformer = new ResourceTransformerImpl(this);
		this.patientRef = null; // TODO: Not thread safe?

		supportedSectionTypes.add(CDASectionTypeEnum.ALLERGIES_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.IMMUNIZATIONS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.MEDICATIONS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.PROBLEM_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.PROCEDURES_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.ENCOUNTERS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.ENCOUNTERS_SECTION_ENTRIES_OPTIONAL);
	}

	/**
	 * Constructor that initiates with the provided resource id generator
	 *
	 * @param idGen The id generator enumeration to be set
	 */
	public CCDTransformerImpl(IdGeneratorEnum idGen) {
		this();
		// Override the default resource id pattern
		this.idGenerator = idGen;
	}

	@Override
	public Reference getPatientRef() {
		return patientRef;
	}

	public void setPatientRef(Reference patientRef) {
		this.patientRef = patientRef;
	}

	@Override
	public synchronized String getUniqueId() {
		switch (this.idGenerator) {
		case COUNTER:
			return Integer.toString(++counter);
		case UUID:
		default:
			return UUID.randomUUID().toString();
		}
	}

	@Override
	public void setIdGenerator(IdGeneratorEnum idGen) {
		this.idGenerator = idGen;
	}

	public void addSection(CDASectionTypeEnum sectionEnum) {
		supportedSectionTypes.add(sectionEnum);
	}

	/**
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param bundleType         Desired type of the FHIR Bundle to be returned
	 *
	 * @param patientRef         Patient Reference of the given CDA Document
	 *
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources but Patient that are referenced
	 *         within the Composition.
	 */
	public Bundle createTransactionBundle(Bundle bundle, Map<String, String> resourceProfileMap, boolean addURLs) {
		Bundle resultBundle = new Bundle();

		for (BundleEntryComponent entry : bundle.getEntry()) {
			// Patient resource will not be added
			if (entry != null) {
				// Add request and fullUrl fields to entries
				addRequestToEntry(entry);
				if (addURLs) {
					addFullUrlToEntry(entry);
				}
				// if resourceProfileMap is specified omit the resources with no profiles given
				// Empty profileUri means add with no change
				if (resourceProfileMap != null) {
					String profileUri = resourceProfileMap.get(entry.getResource().getResourceType().name());
					if (profileUri != null) {
						if (!profileUri.isEmpty()) {
							entry.getResource().getMeta().addProfile(profileUri);
						}
						resultBundle.addEntry(entry);
					}
				} else {
					resultBundle.addEntry(entry);
				}
			}
		}

		return resultBundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param bundleType         The type of bundle to create, currently only
	 *                           supports transaction bundles.
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @param documentBody       The decoded documentBody of the document, to be
	 *                           included in a provenance object. >>>>>>> fhir-stu3
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */

	public Bundle transformDocument(String filePath, BundleType bundleType, Map<String, String> resourceProfileMap,
			String documentBody, Identifier assemblerDevice) throws Exception {
		ContinuityOfCareDocument cda = getClinicalDocument(filePath);
		Bundle bundle = transformDocument(cda, true);
		bundle.setType(bundleType);
		if (assemblerDevice != null && !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}

		if (bundleType.equals(BundleType.TRANSACTION)) {
			return createTransactionBundle(bundle, resourceProfileMap, false);
		}
		return bundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param filePath A file path string to a Consolidated CDA (C-CDA) 2.1
	 *                 Continuity of Care Document (CCD) on file system
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */
	public Bundle transformDocument(String filePath) throws Exception {
		ContinuityOfCareDocument cda = getClinicalDocument(filePath);
		return transformDocument(cda, true);
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 *            instance to be transformed
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 */

	public Bundle transformDocument(ContinuityOfCareDocument cda) {
		return transformDocument(cda, true);
	}

	/**
	 * @param cda                A
	 *
	 *                           Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           >>>>>>> fhir-stu3 Document (CCD) instance to be
	 *                           transformed
	 * @param bundleType         The type of bundle to create, currently only
	 *                           supports transaction bundles.
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @param documentBody       The decoded base64 document that would be included
	 *                           in the provenance object if provided.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */

	public Bundle transformDocument(ContinuityOfCareDocument cda, BundleType bundleType,
			Map<String, String> resourceProfileMap, String documentBody, Identifier assemblerDevice) throws Exception {
		Bundle bundle = transformDocument(cda, true);
		bundle.setType(bundleType);
		if (assemblerDevice != null && !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}

		if (bundleType.equals(BundleType.TRANSACTION)) {
			return createTransactionBundle(bundle, resourceProfileMap, false);
		}
		return bundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda          A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                     Document (CCD) instance to be transformed
	 * @param documentBody The decoded base64 document that would be included in the
	 *                     provenance object if provided.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 */
	@Override
	public Bundle transformDocument(ContinuityOfCareDocument cda, String documentBody, Identifier assemblerDevice) {
		Bundle bundle = transformDocument(cda, true);
		if (assemblerDevice != null & !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}
		return bundle;
	}

	private ICDASection findCDASection(Section section) {
		for (CDASectionTypeEnum sectionType : supportedSectionTypes) {
			if (sectionType.supports(section)) {
				return sectionType.toCDASection(section);
			}
		}
		return null;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param includeComposition Flag to include composition (required for document
	 *                           type bundles)
	 * @return A FHIR Bundle
	 */
	public Bundle transformDocument(ContinuityOfCareDocument ccd, boolean includeComposition) { // TODO: Should be
																								// bundle type based.
		if (ccd == null) {
			return null;
		}

		// init the global ccd bundle via a call to resource transformer, which handles
		// cda header data (in fact, all except the sections)
		IEntryResult entryResult = resTransformer.tClinicalDocument2Bundle(ccd, includeComposition);
		Bundle ccdBundle = entryResult.getBundle();
		if (ccdBundle == null) {
			ccdBundle = new Bundle();
		}

		// the first bundle entry is always the composition
		Composition ccdComposition = includeComposition ? (Composition) ccdBundle.getEntry().get(0).getResource()
				: null;

		// init the patient id reference if it is not given externally.
		if (patientRef == null) {
			List<Patient> patients = FHIRUtil.findResources(ccdBundle, Patient.class);
			if (patients.size() > 0) {
				patientRef = new Reference(patients.get(0).getId());
			}
		} else if (ccdComposition != null) { // Correct the subject at composition with given patient reference.
			ccdComposition.setSubject(patientRef);
		}

		BundleInfo bundleInfo = new BundleInfo(resTransformer);
		bundleInfo.updateFrom(entryResult);
		List<IDeferredReference> deferredReferences = new ArrayList<IDeferredReference>();

		// transform the sections
		for (Section cdaSec : ccd.getSections()) {
			ICDASection section = findCDASection(cdaSec);
			if (section != null) {
				SectionComponent fhirSec = resTransformer.tSection2Section(cdaSec);

				if (fhirSec == null) {
					continue;
				}

				if (ccdComposition != null) {
					ccdComposition.addSection(fhirSec);
				}

				Map<String, String> idedAnnotations = EMFUtil.findReferences(cdaSec.getText());
				bundleInfo.mergeIdedAnnotations(idedAnnotations);

				ISectionResult sectionResult = section.transform(bundleInfo);
				if (sectionResult != null) {
					FHIRUtil.mergeBundle(sectionResult.getBundle(), ccdBundle);
					if (fhirSec != null) {
						List<? extends Resource> resources = sectionResult.getSectionResources();
						for (Resource resource : resources) {
							Reference ref = fhirSec.addEntry();
							ref.setReference(resource.getId());
						}
					}
					if (sectionResult.hasDefferredReferences()) {
						deferredReferences.addAll(sectionResult.getDeferredReferences());
					}
					bundleInfo.updateFrom(sectionResult);
				}
			}
		}

		IIdentifierMap<String> identifierMap = IdentifierMapFactory.bundleToIds(ccdBundle);

		if (!deferredReferences.isEmpty()) {
			for (IDeferredReference dr : deferredReferences) {
				String id = identifierMap.get(dr.getFhirType(), dr.getIdentifier());
				if (id != null) {
					Reference reference = new Reference(id);
					dr.resolve(reference);
				} else {
					String msg = String.format("%s %s is referred but not found", dr.getFhirType(),
							dr.getIdentifier().getValue());
					logger.error(msg);
				}
			}
		}

		return ccdBundle;
	}

	/**
	 * Adds fullUrl field to the entry using it's resource id.
	 *
	 * @param entry Entry which fullUrl field to be added.
	 */
	private void addFullUrlToEntry(BundleEntryComponent entry) {
		// entry.setFullUrl("urn:uuid:" + entry.getResource().getId().getIdPart());
		entry.setFullUrl("urn:uuid:" + entry.getResource().getIdElement().getIdPart());
	}

	/**
	 * Adds request field to the entry, method is POST, url is resource type.
	 *
	 * @param entry Entry which request field to be added.
	 */
	private void addRequestToEntry(BundleEntryComponent entry) {
		BundleEntryRequestComponent request = new BundleEntryRequestComponent();
		request.setMethod(HTTPVerb.POST);
		// request.setUrl(entry.getResource().getResourceName());
		request.setUrl(entry.getResource().getResourceType().name());
		entry.setRequest(request);
	}

	private ContinuityOfCareDocument getClinicalDocument(String filePath) throws Exception {
		FileInputStream fis = new FileInputStream(filePath);
		// ClinicalDocument cda = CDAUtil.load(fis);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());
		fis.close();
		return cda;
	}
}
