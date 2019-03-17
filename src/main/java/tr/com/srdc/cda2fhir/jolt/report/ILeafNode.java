package tr.com.srdc.cda2fhir.jolt.report;

public interface ILeafNode extends INode {
	String getLink();
	
	String getTarget();
}
