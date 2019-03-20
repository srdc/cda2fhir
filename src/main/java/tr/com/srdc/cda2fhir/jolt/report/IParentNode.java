package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;

public interface IParentNode extends INode {
	void removeChild(INode node);

	void addChild(INode node);

	List<INode> getChildren();
}
