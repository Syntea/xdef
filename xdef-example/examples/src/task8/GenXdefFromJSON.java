package task8;

import java.io.File;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.util.GenXDefinition;

/** Example of generation of X-definition from JSON data. */
public class GenXdefFromJSON {
	public static void main(String[] args) throws Exception {
		File json = new File("task8/input/data.json");
		File xdef = new File("task8/output/dataJSON.xdef");
		// 1. create X-definition from XML data.
		GenXDefinition.genXdef(json, xdef, "UTF-8", "XdefFromJSON");
		// 2. Check generated X-definition with given data.
		// if an error occurs an Exception will be thrown
		XDDocument xd = XDFactory.compileXD(null, xdef).createXDDocument();
		xd.jparse(json, null);
		System.out.println(
			"OK, see X-definition created to task8/output/dataJSON.xdef");
	}	
}
