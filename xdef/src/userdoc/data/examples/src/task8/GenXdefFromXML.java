package task8;

import java.io.File;
import java.io.IOException;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.util.GenXDefinition;

/** Example of generation of X-definition from XML data. */
public class GenXdefFromXML {
    public static void main(String[] args) throws IOException {
        File xml = new File("task8/input/data.xml");
        File xdef = new File("task8/output/dataXML.xdef");
        // 1. create X-definition from XML data.
        GenXDefinition.genXdef(xml, xdef, "UTF-8", "XdefFromXML");
        // 2. Check generated X-definition with given data.
        // if an error occurs an Exception will be thrown
        XDDocument xd = XDFactory.compileXD(null, xdef).createXDDocument();
        xd.xparse(xml, null);
        System.out.println("OK, tesk8.GenXdefFromXML, see X-definition in task8/output/dataXML.xdef");
    }
}