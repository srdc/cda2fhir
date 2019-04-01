package tr.com.srdc.cda2fhir.jolt.report;

public interface ILeafNode extends INode {
	String getTarget();

	void setTarget(String target);
}
