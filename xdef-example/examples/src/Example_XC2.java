import components.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.component.XComponentUtil;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Example_XC2 {

	public static void main(String[] args) throws Exception {
		// 1. Get compiled XDPool
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
			"src/data/TownPool.xp"));
		XDPool XP = (XDPool) ois.readObject();
		ois.close();

		// 2. Create the XDDocument object created  from the compiled XDPool
		XDDocument xd = XP.createXDDocument("A");

		// 3. Create the instance of X-component City (unmarchall)
		City city = (City) xd.parseXComponent("temp/data1.xml", null, null);

		// 4. Transform it to the X-component Tenants
		Tenants tenants =
			(Tenants) XComponentUtil.toXComponent(city, XP, "C#Residents");

		// 5. save the transformed version to the file data2.xml
		Element el = tenants.toXml();
		KXmlUtils.writeXml("temp/data2.xml", el);
		System.out.println("Tenants written to: temp/data1.xml\n");

		// 6. Print tenants from the object "tenants"
		for (Tenant x: tenants.listOfResident()) {
			System.out.println(x.getFirstName()
			   + " " + x.getLastName() + "; " + x.getAddress());
		}
	}
}