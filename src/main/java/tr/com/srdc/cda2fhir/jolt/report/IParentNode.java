package tr.com.srdc.cda2fhir.jolt.report;

import java.util.List;

public interface IParentNode extends INode {
	void removeChild(INode node);

	void addChild(INode node);

	void addChildren(List<INode> children);

	List<INode> findChildren(String path);

	List<IParentNode> separateChildLines(String path);

	IParentNode cloneEmpty();

	List<INode> getChildren();
}
