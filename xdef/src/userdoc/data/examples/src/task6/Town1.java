package task6;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.xml.KXmlUtils;
import task6.components1.City;
import task6.components1.House;

public class Town1 {

    public static void main(String... args) throws IOException, ClassNotFoundException {
        new File("task6/output").mkdirs(); // ensure output directory exists

        // 1. Read the compiled XDPool object from the file
        ObjectInputStream os;
        os = new ObjectInputStream(new FileInputStream("src/task6/components1/Town1.xp"));
        XDPool xPool = (XDPool) os.readObject();
        os.close();

        // 2. Create XDDocument
        XDDocument xd = xPool.createXDDocument("A");

        // 3. Create an instance of the X-component City (unmarchall)
        // (Note the generated X-components are in the package "components".)
        City city = (City)xd.xparseXComponent("task6/input/town.xml",null,null);

        // 4. Save XML with addresses to the file data1.xml
        KXmlUtils.writeXml("task6/output/data1.xml", city.toXml(), true, false);
        // 5. Print out the contents of the object City
        System.out.println("City " + city.getName());
        for (City.Street street: city.listOfStreet()) {
            System.out.println("Street " + street.getName() + ":");
            for (House house: street.listOfHouse()) {
                System.out.print("House No. " + house.getNum() + ". ");
                if (!house.listOfPerson().isEmpty()) {
                    System.out.println("Tenants :");
                    for (House.Person citizen: house.listOfPerson()) {
                        System.out.println(citizen.getFirstName() + " " + citizen.getLastName());
                    }
                } else {
                    System.out.println("No tenants in this house.");
                }
            }
        }

        // 6. Update the address for each house and save result.
        KXmlUtils.writeXml("task6/output/data1.xml", city.toXml(), true, false);
        for (City.Street street: city.listOfStreet()) {
            for (House house: street.listOfHouse()) {
                house.setAddress(city.getName() + ", " + street.getName() + " " + house.getNum());
            }
        }
        System.out.println("OK, task6.Town1; see test6/output/data1.xml");
    }
}