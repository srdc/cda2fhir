package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AuthorGenerator extends EntityGenerator {
	private static class CDAEntity implements ICDAEntity {
		private AssignedAuthor assignedAuthor;

		public CDAEntity(AssignedAuthor assignedAuthor) {
			this.assignedAuthor = assignedAuthor;
		}

		@Override
		public void addII(II ii) {
			assignedAuthor.getIds().add(ii);

		}

		@Override
		public void setCode(CE ce) {
			assignedAuthor.setCode(ce);

		}

		@Override
		public void setPerson(Person person) {
			assignedAuthor.setAssignedPerson(person);

		}

		@Override
		public void setOrganization(Organization organization) {
			assignedAuthor.setRepresentedOrganization(organization);

		}
	}

	public Author generate(CDAFactories factories) {
		Author author = factories.base.createAuthor();
		AssignedAuthor assignedAuthor = factories.base.createAssignedAuthor();
		fillEntity(factories, new CDAEntity(assignedAuthor));
		author.setAssignedAuthor(assignedAuthor);
		return author;
	}

	public static AuthorGenerator getDefaultInstance() {
		AuthorGenerator ag = new AuthorGenerator();
		fillDefaultInstance(ag);
		return ag;
	}

	public static AuthorGenerator getFullInstance() {
		AuthorGenerator ag = new AuthorGenerator();
		fillFullInstance(ag);
		return ag;
	}
}
