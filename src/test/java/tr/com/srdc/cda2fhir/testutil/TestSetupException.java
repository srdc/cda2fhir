package tr.com.srdc.cda2fhir.testutil;

public class TestSetupException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TestSetupException() {
		super();
	}

	public TestSetupException(String message) {
		super(message);
	}
}
