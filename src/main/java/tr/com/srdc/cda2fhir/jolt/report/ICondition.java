package tr.com.srdc.cda2fhir.jolt.report;

public interface ICondition {
	ICondition clone();

	void prependPath(String path);

	String toString(String path);

	ICondition not();
}
