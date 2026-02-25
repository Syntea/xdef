package task6;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponentUtil;
import task6.components2.House;
import task6.components2.City;
import task6.components2.Tenants;

public class Town2 {

    public static void main(String... args) throws IOException, ClassNotFoundException {
        new File("task6/output").mkdirs(); // ensure output directory exists

        // 1. Get compiled XDPool from the file
        ObjectInputStream os;
        os = new ObjectInputStream(new FileInputStream("src/task6/components2/Town2.xp"));
        XDPool xpool = (XDPool) os.readObject();
        os.close();

        // 2. Create XDDocument
        XDDocument xd = xpool.createXDDocument("A");

        // 3. Create the instance of the X-component City (unmarchall)
        // (Note the generated X-components are in the package "components".)
        City city = (City)xd.xparseXComponent("task6/input/town.xml",null,null);

        // 4. Update the address for each house.
        for (City.Street street: city.listOfStreet()) {
            for (House house: street.listOfHouse()) {
                house.setAddress(city.getName() + ", " + street.getName() + " " + house.getNum());
            }
        }

        // 5. Transform it to the X-component Tenants
        Tenants tenants = (Tenants) XComponentUtil.toXComponent(city, xpool, "C#Residents");
        // 6. save the transformed version to the file data2.xml
        KXmlUtils.writeXml("task6/output/data2.xml",tenants.toXml(),true,false);
        // 7. Print result"
        System.out.println("Tenants:");
        for (Tenants.Resident x: tenants.listOfResident()) {
            System.out.println(x.getFirstName()  + " " + x.getLastName() + "; " + x.getAddress());
        }
        System.out.println("OK, task6.Town2 see task6/output/data2.xml");
    }
}