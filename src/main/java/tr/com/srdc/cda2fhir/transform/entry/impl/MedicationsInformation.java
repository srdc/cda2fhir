package tr.com.srdc.cda2fhir.transform.entry.impl;

import java.util.List;

import org.hl7.fhir.dstu3.model.Medication;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import tr.com.srdc.cda2fhir.transform.entry.IMedicationsInformation;
import tr.com.srdc.cda2fhir.transform.util.impl.CDACDMap;
import tr.com.srdc.cda2fhir.transform.util.impl.CDAIIMap;

public class MedicationsInformation implements IMedicationsInformation {

	private CDACDMap<MedicationContainer> medContainers = new CDACDMap<MedicationContainer>();

	private class MedicationContainer {
		private CD code;

		CDAIIMap<Medication> orgIIMap = null;

		private Medication medication = null;

		public MedicationContainer(Medication medication, CD code, List<II> iis) {
			this.code = code;
			if (iis != null) {
				orgIIMap = new CDAIIMap<Medication>();
				orgIIMap.put(iis, medication);
			}
		}

		public MedicationContainer(Medication medication, CD code) {
			this.code = code;
			this.medication = medication;
		}

		private boolean checkIds(List<II> iis) {
			if (iis == null && this.medication != null) {
				return true;
			} else if (orgIIMap == null || iis == null) {
				return false;
			}

			return orgIIMap.get(iis) != null;

		}

		public boolean isMedication(CD cd, List<II> iis) {
			if (cd == null || code == null) {
				return false;
			}

			return checkIds(iis);

		}

		public Medication getMedication(List<II> iis) {
			if (iis != null && orgIIMap.get(iis) != null) {
				return orgIIMap.get(iis);
			} else if (iis == null && this.medication != null) {
				return this.medication;
			}
			return null;
		}

	}

	public MedicationsInformation(Medication fhirMedication, CD cd, List<II> orgIds) {

		MedicationContainer medContainer;
		if (orgIds == null) {
			medContainer = new MedicationContainer(fhirMedication, cd);
		} else {
			medContainer = new MedicationContainer(fhirMedication, cd, orgIds);
		}
		medContainers.put(cd, medContainer);
	}

	@Override
	public boolean containsMedication(CD cd, List<II> iis) {
		MedicationContainer medContainer = medContainers.get(cd);

		if (medContainer != null) {
			return medContainer.isMedication(cd, iis);
		}
		return false;
	}

	@Override
	public Medication getMedication(CD cd, List<II> iis) {
		MedicationContainer medContainer = medContainers.get(cd);

		if (medContainer != null) {
			if (medContainer.isMedication(cd, iis)) {
				return medContainer.getMedication(iis);
			}
		}
		return null;

	}

	@Override
	public void putMedication(Medication med, CD code, List<II> iis) {
		if (!containsMedication(code, iis)) {
			MedicationContainer medContainer = new MedicationContainer(med, code, iis);
			medContainers.put(code, medContainer);
		}
	}

	@Override
	public void putMedication(Medication med, CD code) {
		if (!containsMedication(code, null)) {
			MedicationContainer medContainer = new MedicationContainer(med, code);
			medContainers.put(code, medContainer);
		}
	}

}
