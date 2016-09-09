<!--
Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

cda2fhir [![License Info](http://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/srdc/cda2fhir/blob/master/LICENSE.txt)
===

cda2fhir is a Java library to transform HL7 CDA R2 instances to HL7 FHIR resources

## Installation

Apache Maven is required to build the cda2fhir. Please visit http://maven.apache.org/ in order to install Maven on your system.

Under the root directory of the cda2fhir project run the following:

	$ cda2fhir> mvn install

In order to make a clean install run the following:

	$ cda2fhir> mvn clean install

These will build the cda2fhir library and also run a number of test cases, which will transform some C-CDA Continuity of Care Document (CCD) instances,
and some manually crafted CDA artifacts (e.g. entry class instances) and datatype instances to corresponding FHIR resources, wherever possible using the DAF profile.

## Transforming a CDA document to a Bundle of corresponding FHIR resources

```java
FileInputStream fis = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");

ClinicalDocument cda = CDAUtil.load(fis);
ICDATransformer ccdTransformer = new CCDTransformerImpl(IdGeneratorEnum.COUNTER);
Config.setGenerateDafProfileMetadata(true);
Config.setGenerateNarrative(true);
Bundle bundle = ccdTransformer.transformDocument(cda);
if(bundle != null)
    FHIRUtil.printJSON(bundle, "src/test/resources/output/C-CDA_R2-1_CCD-w-daf.json");
```

## Transforming a CDA artifact (e.g. an entry class) to the corresponding FHIR resource(s)