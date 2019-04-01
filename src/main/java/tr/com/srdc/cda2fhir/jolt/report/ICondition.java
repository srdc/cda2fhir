package tr.com.srdc.cda2fhir.jolt.report;

public interface ICondition extends Comparable<ICondition> {
	ICondition clone();

	default ICondition clone(String path) {
		ICondition result = clone();
		result.prependPath(path);
		return result;
	}

	void prependPath(String path);

	String toString(String path);

	ICondition not();
}
