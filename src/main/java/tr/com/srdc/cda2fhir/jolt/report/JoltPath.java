package tr.com.srdc.cda2fhir.jolt.report;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoltPath {
	private LinkedList<JoltPath> children = new LinkedList<JoltPath>();
	private String path;
	private String target;
	private String link;
	
	private JoltPath(String path, String target, String link) {
		this.path = path;
		this.target = target;
		this.link = link;
	}
	
	private JoltPath(String path, String target) {
		this.path = path;
		this.target = target;
	}
	
	public String getLink() {
		return link;
	}
	
	public void addChild(JoltPath child) {
		children.add(child);
	}
	
	public void addChildren(List<JoltPath> children) {
		this.children.addAll(children);
	}
	
	public void setTarget(String target) {
		this.target = target; 
	}

	public void expandLinks(Map<String, List<JoltPath>> linkMap) {
		if (link == null) {
			children.forEach(c -> c.expandLinks(linkMap));
			return;
		}
		
		List<JoltPath> linkPaths = linkMap.get(link);
		if (linkPaths != null) {
			linkPaths.forEach(lp -> lp.expandLinks(linkMap));
			children.addAll(linkPaths);
			link = null;
		}
	}
	
	public List<TableRow> toTableRows() {
		if (children.size() < 1) {
			TableRow row = new TableRow();
			row.path = path;
			row.target = target;
			row.link = link;
			List<TableRow> result = new ArrayList<TableRow>();
			result.add(row);
			return result;
		}
		
		List<TableRow> rows = children.stream().map(jp -> jp.toTableRows()).flatMap(List::stream).collect(Collectors.toList());
		rows.forEach(row -> {
			row.path = path + "." + row.path;
			if (target != null) {
				row.target = target + "." + row.target;
			}
		});
		return rows;
	}
	
	public static JoltPath getInstance(String path, String target) {
		if (target == null) {
			return new JoltPath(path, null);			
		}		
		String[] pieces = target.split("\\.");		
		int length = pieces.length;
		String lastPiece = pieces[length-1];
		if (!lastPiece.startsWith("->")) {
			return new JoltPath(path, target);						
		}
		String link = lastPiece.substring(2);
		if (length == 1) {
			return new JoltPath(path, "", link);
		}
		String reducedTarget = pieces[0];
		for (int index = 1; index < length - 1; ++index) {
			reducedTarget += "." + pieces[index];
		}
		return new JoltPath(path, reducedTarget, link);
	}
}
