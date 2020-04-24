import components.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import org.xdef.xml.KXmlUtils;
import org.xdef.*;
import org.w3c.dom.Element;

public class Example_XC1 {

	public static void main(String[] args) throws Exception {

		// 1. Get compiled XDPool
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
			"src/data/TownPool.xp"));
		XDPool XP = (XDPool) ois.readObject();
		ois.close();

		// 2. Create XDDocument
		XDDocument xd = XP.createXDDocument("A");

		// 3. Create the instace of the X-component City (unmarchall)
		// (Note the generated X-components are in the package "components".)
		City city =(City)xd.parseXComponent("src/Example_XCdata.xml",null,null);

		// 4. Print out contents of the object City
		System.out.println("City " + city.getName());
		for (City.Street ulice: city.listOfStreet()) {
			System.out.println("Street " + ulice.getName() + ":");
			for (House house: ulice.listOfHouse()) {
				System.out.print("House No. " + house.getNum() + ". ");
				if (house.listOfPerson().size() > 0) {
					System.out.println("Tenants :");
					for (Citizen citizen: house.listOfPerson()){
						System.out.println(citizen.getFirstName()
							+ " " + citizen.getLastName());
					}
				} else {
					System.out.println("No tenants in this house.");
				}
			}
		}

		// 5. Update addresses to each house.
		for (City.Street street: city.listOfStreet()) {
			for (House house: street.listOfHouse()) {
				house.setAddress(city.getName() + ", " + street.getName()
					+ " " + house.getNum());
			}
		}

		// 6. Save XML with addresses to the file data.xml (marshall)
		Element el = city.toXml();
		KXmlUtils.writeXml("temp/data1.xml", el);
		System.out.println("\nElement City written to: temp/data1.xml");

		// 7. Print how many "Smith" family names are in the town
		System.out.println("Number of the name 'Smith' in the town: "
			+ city.getNumSmith());

	}
}