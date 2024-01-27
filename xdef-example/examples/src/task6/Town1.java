package task6;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.xml.KXmlUtils;
import task6.components1.City;
import task6.components1.House;

public class Town1 {

	public static void main(String... args) throws Exception {
		// 1. Read the compiled XDPool object from the file
		ObjectInputStream ois = new ObjectInputStream(
			new FileInputStream("src/task6/components1/Town1.xp"));
		XDPool xPool = (XDPool) ois.readObject();
		ois.close();

		// 2. Create XDDocument
		XDDocument xd = xPool.createXDDocument("A");

		// 3. Create an instance of the X-component City (unmarchall)
		// (Note the generated X-components are in the package "components".)
		City city = (City)xd.xparseXComponent("task6/input/town.xml",null,null);

		// 4. Print out the contents of the object City
		System.out.println("City " + city.getName());
		for (City.Street street: city.listOfStreet()) {
			System.out.println("Street " + street.getName() + ":");
			for (House house: street.listOfHouse()) {
				System.out.print("House No. " + house.getNum() + ". ");
				if (house.listOfPerson().size() > 0) {
					System.out.println("Tenants :");
					for (House.Person citizen: house.listOfPerson()) {
						System.out.println(citizen.getFirstName()
							+ " " + citizen.getLastName());
					}
				} else {
					System.out.println("No tenants in this house.");
				}
			}
		}

		// 5. Update the address for each house.
		for (City.Street street: city.listOfStreet()) {
			for (House house: street.listOfHouse()) {
				house.setAddress(city.getName() + ", " + street.getName()
					+ " " + house.getNum());
			}
		}

		// 6. Save XML with addresses to the file data1.xml
		Element el = city.toXml();
		new File("task6/output").mkdirs();		
		KXmlUtils.writeXml("task6/output/data1.xml", el, true, false);
		System.out.println("\nElement City written to: test6/output/data1.xml");
	}
}