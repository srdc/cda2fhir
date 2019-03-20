package tr.com.srdc.cda2fhir.jolt.report;

public interface PathPredicate {
	boolean compare(String path);
}
