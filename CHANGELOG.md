# Changelog
Initial Changelog added (changes made by Amida).

## [0.0.13] - 2019-05-01
### Added
- Added improved logging to flag unmapped OIDs and Sections.
- Added tests for each adjustment from DSTU2 to STU3.
- Added Jolt to comprehensively test each resource type.
- Added utility to automatically generate unmapped CCD fields.
- Fixed validation to use HAPI FHIR validation.
- Implemented Device, Provenance, and referenceDocument resources for Provenance.
- Removed DAF profile use in STU3.
- Created integration test to automatically post bundles to HAPI FHIR server.
- In bundle de-duplication for certain resources.
- ifNoneExists parameter support.
- References now show a common name for each reference.
- May now dictate supported sections without removing code.
- Added Device based authorship.
- Added custom Epic observation field support.