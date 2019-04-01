package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;

import tr.com.srdc.cda2fhir.testutil.generator.AssignedEntityGenerator;

public class AuthorGenerator {
	private AssignedEntityGenerator assignedEntityGenerator;
	private CDAFactories factories;

	private String time;

	static final private String DEFAULT_TIME = "20190101";

	public AuthorGenerator() {
		this.factories = CDAFactories.init();
		this.assignedEntityGenerator = new AssignedEntityGenerator();
	}

	public AuthorGenerator(CDAFactories factories) {
		this.factories = factories;

	}

	public AuthorGenerator(AssignedEntityGenerator assignedEntityGenerator) {
		this.assignedEntityGenerator = assignedEntityGenerator;
	}

	public AuthorGenerator(AssignedEntityGenerator assignedEntityGenerator, CDAFactories factories) {
		this.factories = factories;
		this.assignedEntityGenerator = assignedEntityGenerator;
	}

	public Author generateDefaultAuthor() {
		Author author = factories.base.createAuthor();
		author.setTime(generateTime(DEFAULT_TIME));
		author.setAssignedAuthor(generateAssignedAuthor());
		return author;
	}

	public Author generateAuthor() {
		Author author = factories.base.createAuthor();
		author.setTime(time == null ? generateTime(DEFAULT_TIME) : generateTime(time));
		author.setAssignedAuthor(generateAssignedAuthor());
		return author;
	}

	private AssignedAuthor generateAssignedAuthor() {
		AssignedEntity entity = assignedEntityGenerator.generate(factories);
		AssignedAuthor assignedAuthor = factories.base.createAssignedAuthor();
		if (entity.getAssignedPerson() != null) {
			assignedAuthor.setAssignedPerson(entity.getAssignedPerson());
		}
		if (entity.getIds() != null) {
			assignedAuthor.getIds().addAll(entity.getIds());
		}
		if (entity.getRepresentedOrganizations() != null && !entity.getRepresentedOrganizations().isEmpty()) {
			assignedAuthor.setRepresentedOrganization(entity.getRepresentedOrganizations().get(0));
		}
		if (entity.getAddrs() != null) {
			assignedAuthor.getAddrs().addAll(entity.getAddrs());
		}
		if (entity.getTelecoms() != null) {
			assignedAuthor.getTelecoms().addAll(entity.getTelecoms());
		}
		return assignedAuthor;
	}

	private TS generateTime(String time) {
		return factories.datatype.createTS(time);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
