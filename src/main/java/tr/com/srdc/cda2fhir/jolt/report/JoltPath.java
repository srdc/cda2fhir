package tr.com.srdc.cda2fhir.jolt.report;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class JoltPath {
	private LinkedList<String> paths = new LinkedList<String>();
	private String target;
	private String link;
	
	private JoltPath(String path, String target, String link) {
		paths.add(path);
		this.target = target;
		this.link = link;
	}
	
	private JoltPath(String path, String target) {
		paths.add(path);
		this.target = target;
	}
	
	public String getLink() {
		return link;
	}
	
	public void prependPath(String path) {
		paths.addFirst(path);
	}

	public void prependFrom(JoltPath source) {
		source.paths.forEach(path -> {
			paths.addFirst(path);
		});
		target = source.target + "." + target;
	}
	
	public void appendPath(String path) {
		paths.addLast(path);
	}
	
	public void setTarget(String target) {
		this.target = target; 
	}

	@Override
	public String toString() {
		String path = paths.stream().collect(Collectors.joining("."));
		String targetDisplay = target.length() == 0 ? "\"\"" : target;
		String display = String.format("%s -> %s", path, targetDisplay);
		return link != null ? String.format("%s (%s)", display, link) : display;
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
