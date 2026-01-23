package task6;

import java.io.IOException;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.xml.KXmlUtils;
import task6.components2.Tenants;
import task6.components2.Tenants.Resident;

public class CreateResidentList {

	/** Add residents to the list of residents in the X-component Tenants. */
	private static void addResident(Tenants tenants,
		String firstName, String lastName, String address) {
		// 1. Create an instance of X/component Resident.
		Resident resident = new Resident();

		// 2. Set values of Resident
		resident.setFirstName(firstName);
		resident.setLastName(lastName);
		resident.setAddress(address);

		// 3. Add created the resident to the list of residents
		tenants.addResident(resident);
	}

	public static void main(String... args) throws IOException {
		// 1. Create an instance of X-component (model of element Residents)
		Tenants tenants = new Tenants();
		List<Resident> list = tenants.listOfResident();
		Resident resident = new Resident();
		resident.setFirstName("Michal");
		resident.setLastName("Kotek");
		resident.setAddress("Praha, Balbinova 3");
		list.add(resident);

		// 2. Add residents to the X-component
		addResident(tenants, "Janes", "Smith", "Nonehill, Long 3");
		addResident(tenants, "Jeremy", "Smith", "Nonehill, Short 1");
		addResident(tenants, "Jane", "Smith", "Newmill, Innis st. 15");

		// 2. Add residents to the list of residents
		for (Tenants.Resident x: tenants.listOfResident()) {
			System.out.println(x.getFirstName() + " " + x.getLastName() + "; " + x.getAddress());
		}

		// 4. Create XML with residents to the file data3.xml
		Element el = tenants.toXml();
		KXmlUtils.writeXml("task6/output/data3.xml", el, true, false);
		System.out.println("\nTask6.CreateResidentList OK,  data written to test6/output/data3.xml");
	}
}