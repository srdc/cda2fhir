package tr.com.srdc.cda2fhir.testutil.generator;

import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;

public class AssignedEntityGenerator extends EntityGenerator {
	private static class CDAEntity implements ICDAEntity {
		private AssignedEntity entity;

		public CDAEntity(AssignedEntity entity) {
			this.entity = entity;
		}

		@Override
		public void addII(II ii) {
			entity.getIds().add(ii);

		}

		@Override
		public void setCode(CE ce) {
			entity.setCode(ce);

		}

		@Override
		public void setPerson(Person person) {
			entity.setAssignedPerson(person);

		}

		@Override
		public void setOrganization(Organization organization) {
			entity.getRepresentedOrganizations().add(organization);

		}
	}

	public AssignedEntity generate(CDAFactories factories) {
		AssignedEntity entity = factories.base.createAssignedEntity();
		fillEntity(factories, new CDAEntity(entity));
		return entity;
	}

	public static AssignedEntityGenerator getDefaultInstance() {
		AssignedEntityGenerator aeg = new AssignedEntityGenerator();
		fillDefaultInstance(aeg);
		return aeg;
	}

	public static AssignedEntityGenerator getFullInstance() {
		AssignedEntityGenerator aeg = new AssignedEntityGenerator();
		fillFullInstance(aeg);
		return aeg;
	}
}
