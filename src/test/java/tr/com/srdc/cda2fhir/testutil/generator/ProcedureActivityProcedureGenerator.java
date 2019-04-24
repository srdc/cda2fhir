package tr.com.srdc.cda2fhir.testutil.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedurePerformerComponent;
import org.junit.Assert;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.bazaarvoice.jolt.JsonUtils;

import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.TestSetupException;

public class ProcedureActivityProcedureGenerator {
	private static final Map<String, Object> PROCEDURE_STATUS = JsonUtils
			.filepathToMap("src/test/resources/jolt/value-maps/ProcedureStatus.json");

	private List<IDGenerator> idGenerators = new ArrayList<>();
	private IVL_TSPeriodGenerator ivlTsGenerator;
	private List<CDGenerator> targetSiteCodeGenerators = new ArrayList<>();

	private String statusCode;
	private String statusCodeNullFlavor;

	private CDGenerator codeGenerator;

	private List<CDGenerator> reasonCodeGenerators = new ArrayList<>();

	private List<PerformerGenerator> performerGenerators = new ArrayList<>();

	public ProcedureActivityProcedure generate(CDAFactories factories) {
		ProcedureActivityProcedure pap = factories.consol.createProcedureActivityProcedure();

		idGenerators.forEach(idGenerator -> {
			II ii = idGenerator.generate(factories);
			pap.getIds().add(ii);
		});

		if (ivlTsGenerator != null) {
			IVL_TS ivlTs = ivlTsGenerator.generate(factories);
			pap.setEffectiveTime(ivlTs);
		}

		targetSiteCodeGenerators.forEach(tcg -> {
			CD cd = tcg.generate(factories);
			pap.getTargetSiteCodes().add(cd);
		});

		if (statusCode != null || statusCodeNullFlavor != null) {
			CS cs = factories.datatype.createCS();
			if (statusCode != null) {
				cs.setCode(statusCode);
			}
			if (statusCodeNullFlavor != null) {
				NullFlavor nf = NullFlavor.get(statusCodeNullFlavor);
				if (nf == null) {
					throw new TestSetupException("Invalid null flavor enumeration.");
				}
				cs.setNullFlavor(nf);
			}
			pap.setStatusCode(cs);
		}

		if (codeGenerator != null) {
			CD cd = codeGenerator.generate(factories);
			pap.setCode(cd);
		}

		reasonCodeGenerators.forEach(rcg -> {
			EntryRelationship er = factories.base.createEntryRelationship();
			pap.getEntryRelationships().add(er);
			er.setTypeCode(x_ActRelationshipEntryRelationship.RSON);
			Indication indication = factories.consol.createIndication();
			er.setObservation(indication);
			CD cd = rcg.generate(factories);
			indication.setCode(cd);
		});

		performerGenerators.forEach(pg -> {
			Performer2 performer = pg.generate(factories);
			pap.getPerformers().add(performer);
		});

		return pap;
	}

	public static ProcedureActivityProcedureGenerator getDefaultInstance() {
		ProcedureActivityProcedureGenerator papg = new ProcedureActivityProcedureGenerator();

		papg.idGenerators.add(IDGenerator.getNextInstance());
		papg.ivlTsGenerator = IVL_TSPeriodGenerator.getDefaultInstance();
		papg.targetSiteCodeGenerators.add(CDGenerator.getNextInstance());
		papg.statusCode = "active";
		papg.codeGenerator = CDGenerator.getNextInstance();
		papg.reasonCodeGenerators.add(CDGenerator.getNextInstance());
		papg.performerGenerators.add(PerformerGenerator.getDefaultInstance());

		return papg;
	}

	public IDGenerator getIDGenerator(int index) {
		return idGenerators.get(index);
	}

	public void verify(Procedure procedure) {
		if (!idGenerators.isEmpty()) {
			for (int index = 0; index < idGenerators.size(); ++index) {
				idGenerators.get(index).verify(procedure.getIdentifier().get(index));
			}
		} else {
			Assert.assertTrue("No condition identifier", !procedure.hasIdentifier());
		}

		if (ivlTsGenerator == null) {
			Assert.assertTrue("Missing procedure performed", !procedure.hasPerformedPeriod());
		} else {
			ivlTsGenerator.verify(procedure.getPerformedPeriod());
		}

		if (targetSiteCodeGenerators.isEmpty()) {
			Assert.assertTrue("Missing procedure target site", !procedure.hasBodySite());
		} else {
			for (int index = 0; index < targetSiteCodeGenerators.size(); ++index) {
				targetSiteCodeGenerators.get(index).verify(procedure.getBodySite().get(index));
			}
		}

		if (statusCode == null || statusCodeNullFlavor != null) {
			Assert.assertTrue("Missing procedure status", !procedure.hasStatus());
		} else {
			String expected = (String) PROCEDURE_STATUS.get(statusCode);
			if (expected == null) {
				expected = "unknown";
			}
			Assert.assertEquals("Procedure status", expected, procedure.getStatus().toCode());
		}

		if (codeGenerator == null) {
			Assert.assertTrue("Missing procedure code", !procedure.hasCode());
		} else {
			codeGenerator.verify(procedure.getCode());
		}

		if (reasonCodeGenerators.isEmpty()) {
			Assert.assertTrue("Missing procedure reason code", !procedure.hasReasonCode());
		} else {
			for (int index = 0; index < reasonCodeGenerators.size(); ++index) {
				reasonCodeGenerators.get(index).verify(procedure.getReasonCode().get(index));
			}
		}

		if (performerGenerators.isEmpty()) {
			Assert.assertTrue("Missing procedure performer", !procedure.hasPerformer());
		} else {
			Assert.assertEquals("Procedure performer count", performerGenerators.size(),
					procedure.getPerformer().size());
		}
	}

	public void verify(Bundle bundle, Procedure procedure) throws Exception {
		verify(procedure);

		if (performerGenerators.isEmpty()) {
			Assert.assertTrue("Missing procedure performer", !procedure.hasPerformer());
		} else {
			BundleUtil util = new BundleUtil(bundle);

			for (int index = 0; index < performerGenerators.size(); ++index) {
				PerformerGenerator pg = performerGenerators.get(index);
				ProcedurePerformerComponent ppc = procedure.getPerformer().get(index);

				String practitionerId = ppc.getActor().getReference();
				pg.verifyFromPractionerId(bundle, practitionerId);

				Practitioner practitioner = util.getResourceFromReference(practitionerId, Practitioner.class);
				pg.verify(practitioner);

				PractitionerRole role = util.getPractitionerRole(practitionerId);
				pg.verify(role);

				Assert.assertTrue("Procedure performer has role", ppc.hasRole());
				Coding ppcRole = ppc.getRole().getCoding().get(0);
				Assert.assertEquals("Procedure performer has role", pg.getCodeCode(), ppcRole.getCode());

				if (!role.hasOrganization()) {
					pg.verify((Organization) null);
					Assert.assertTrue("No on behalf organization", !ppc.hasOnBehalfOf());
				} else {
					String reference = role.getOrganization().getReference();
					Organization organization = util.getResourceFromReference(reference, Organization.class);
					pg.verify(organization);
					Assert.assertEquals("Procedure on behalf organization", reference,
							ppc.getOnBehalfOf().getReference());
				}

			}
		}
	}

	public void verify(Bundle bundle) throws Exception {
		Procedure procedure = BundleUtil.findOneResource(bundle, Procedure.class);
		verify(bundle, procedure);
	}
}
