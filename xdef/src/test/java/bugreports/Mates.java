package bugreports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import org.yaml.snakeyaml.Yaml;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class Mates extends XDTester {

    public Mates() {super();}

    private static Object yamlToJson(final Object o) {
        if (o == null) return null;
        if (o instanceof Map) {
            Map m = (Map) o;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Object x: m.entrySet()) {
                Map.Entry en = (Map.Entry) x;
                Object y = en.getKey();
                String key = (y instanceof byte[]) ? new String((byte[])y) : (String) y;
                Object z = yamlToJson(en.getValue());
                result.put(key, z);
            }
            return result;
        } else if (o instanceof List) {
            List list = (List) o;
            List<Object> result = new ArrayList<>();
            for (Object x: list) {
                result.add(yamlToJson(x));
            }
            return result;
        } else if (o instanceof byte[]) {
            return new String((byte[]) o);
        }
        return o;
    }

    /** Run test and display error information. */
    @Override
    @SuppressWarnings("unchecked")
    public void test() {

////////////////////////////////////////////////////////////////////////////////
        boolean T = false; // If the value is false, all tests are run; otherwise, only the first one is run
////////////////////////////////////////////////////////////////////////////////
        System.out.println("X-definition version: " + XDFactory.getXDVersion());
        System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
        setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
        setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
        Object o,x;
        String json, s, xdef, yaml;
        XDDocument xd;
        XDPool xp;
        ArrayReporter reporter = new ArrayReporter();
        try {
            xdef = getSourceDir() + "/Mates.xdef";
            xp = compile(xdef);
            xd = xp.createXDDocument("SynPLscript");
            yaml = getSourceDir() + "/Mates.yaml";
            try (Reader in = new InputStreamReader(new FileInputStream(yaml), "windows-1250")) {
                x = new Yaml().load(in);
                s = XonUtils.toJsonString(yamlToJson(x), true);
            }
            json = getSourceDir() + "/Mates.json";
            FUtils.writeString(new File(json), s, "UTF-8");
            x = xd.jparse(json, reporter);
            assertNoErrorsAndClear(reporter);
            o = xd.yparse(yaml, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(o, x), XonUtils.xonDiff(o, x));
        } catch (IOException | SException | SRuntimeException ex) {fail(ex);}
if(T)return;

        clearTempDir(); // delete temporary files.
    }

    /** Run test
     * @param args the command line arguments
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {
            System.exit(1);
        }
    }
}
