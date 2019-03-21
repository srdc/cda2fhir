package tr.com.srdc.cda2fhir.jolt.report;

public class ReportException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ReportException() {
		super();
	}

	public ReportException(String message) {
		super(message);
	}
}
