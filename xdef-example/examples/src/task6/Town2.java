package task6;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponentUtil;
import task6.components2.House;
import task6.components2.City;
import task6.components2.Tenants;

public class Town2 {

	public static void main(String... args) throws Exception {
		// 1. Get compiled XDPool from the file
		ObjectInputStream ois = new ObjectInputStream(
			new FileInputStream("src/task6/components2/Town2.xp"));
		XDPool xpool = (XDPool) ois.readObject();
		ois.close();

		// 2. Create XDDocument
		XDDocument xd = xpool.createXDDocument("A");

		// 3. Create the instance of the X-component City (unmarchall)
		// (Note the generated X-components are in the package "components".)
		City city = (City)xd.xparseXComponent("task6/input/town.xml",null,null);

		// 4. Update the address for each house.
		for (City.Street street: city.listOfStreet()) {
			for (House house: street.listOfHouse()) {
				house.setAddress(city.getName() + ", " + street.getName()
					+ " " + house.getNum());
			}
		}

		// 5. Transform it to the X-component Tenants
		Tenants tenants =
			(Tenants) XComponentUtil.toXComponent(city, xpool, "C#Residents");

		// 6. Print tenants from the object "tenants"
		for (Tenants.Resident x: tenants.listOfResident()) {
			System.out.println(x.getFirstName()
			   + " " + x.getLastName() + "; " + x.getAddress());
		}

		// 7. save the transformed version to the file data2.xml
		Element el = tenants.toXml();
		KXmlUtils.writeXml("task6/output/data2.xml", el, true, false);
		System.out.println("Tenants written to: task6/output/data2.xml");
	}
}