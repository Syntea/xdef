package task8;

import java.io.File;
import java.io.IOException;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.util.GenXDefinition;

/** Example of generation of X-definition from YAML data. */
public class GenXdefFromYAML {
    public static void main(String[] args) throws IOException {
        File yaml = new File("task8/input/data.yaml");
        File xdef = new File("task8/output/dataYAML.xdef");
        // 1. create X-definition fromYAML data.
        GenXDefinition.genXdef(yaml, xdef, "UTF-8", "XdefFromAML");
        // 2. Check generated X-definition with given data.
        // if an error occurs an Exception will be thrown
        XDDocument xd = XDFactory.compileXD(null, xdef).createXDDocument();
        xd.yparse(yaml, null);
        System.out.println("OK,task8.GenXdefFromYAML, see X-definition in task8/output/dataYAML.xdef");
    }
}