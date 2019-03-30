package tr.com.srdc.cda2fhir.testutil;

import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;

public class CdaOrganizationGenerator {

	private CDAFactories factories;

	private BasicObjectGenerator basicObjectGenerator = new BasicObjectGenerator();
	private String name;

	private String street;
	private String city;
	private String state;
	private String postalCode;

	static final public String DEFAULT_NAME = "Aperture Science";
	static final public String DEFAULT_STREET = "100 Aperture Drive";
	static final public String DEFAULT_CITY = "Cleveland";
	static final public String DEFAULT_STATE = "Ohio";
	static final public String DEFAULT_POSTAL_CODE = "44101";

	public CdaOrganizationGenerator() {
		this.factories = CDAFactories.init();
	}

	public CdaOrganizationGenerator(CDAFactories factories) {
		this.factories = factories;

	}

	public Organization generateDefault() {
		Organization org = factories.base.createOrganization();
		org.getNames().add(basicObjectGenerator.getName(DEFAULT_NAME));
		org.getAddrs().add(generateDefaultAddress());
		return org;
	}

	public Organization generate() {
		Organization org = factories.base.createOrganization();
		org.getNames().add(basicObjectGenerator.getName(name == null ? DEFAULT_NAME : name));
		org.getAddrs().add(generateAddress());
		return org;
	}

	public void setbasicObjectGenerator(BasicObjectGenerator basicObjectGenerator) {
		this.basicObjectGenerator = basicObjectGenerator;
	}

	public AD generateDefaultAddress() {
		return basicObjectGenerator.getAddress(DEFAULT_STREET, DEFAULT_CITY, DEFAULT_STATE, DEFAULT_POSTAL_CODE);
	}

	public AD generateAddress() {
		AD addr = factories.datatype.createAD();
		addr.addStreetAddressLine(street == null ? DEFAULT_STREET : street);
		addr.addCity(city == null ? DEFAULT_CITY : city);
		addr.addState(state == null ? DEFAULT_STATE : state);
		addr.addPostalCode(postalCode == null ? DEFAULT_POSTAL_CODE : postalCode);
		return addr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostal_code() {
		return postalCode;
	}

	public void setPostal_code(String postalCode) {
		this.postalCode = postalCode;
	}

}
