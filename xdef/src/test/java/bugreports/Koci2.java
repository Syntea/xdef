package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import java.util.Properties;

public class Koci2 {

    public static XDContainer getData(XXData xData) {
        XDContainer v = (XDContainer) xData.getXDContext();
        v = (v != null) ? XDFactory.createXDContainer(v.getElement()) : XDFactory.createXDContainer("content of c");
        System.out.println(v);
        return v;
    }

    public static void main(String[] args) {
        Properties props = System.getProperties();
        XDPool xdpool = XDFactory.compileXD(props,
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"A\" name=\"Example\">\n" +
"  <xd:declaration scope=\"local\">\n" +
"    external method XDContainer bugreports.Koci2.getData(XXData);\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script=\"+; create getData();\">\n" +
"      <C a='? string();'/>\n" +
"    </B>\n" +
"  </A>\n" +
"</xd:def>");
        XDDocument xdoc = xdpool.createXDDocument("Example");
        String xml =
"<A>\n" +
"  <B>\n" +
"    <C a='1'/>\n" +
"  </B>\n" +
"  <B>\n" +
"    <C a='2'/>\n" +
"  </B>\n" +
"  <B>\n" +
"    <C a='3'/>\n" +
"  </B>\n" +
"</A>";
        xdoc.setXDContext(xml);
        ArrayReporter reporter = new ArrayReporter();
        Element result = xdoc.xcreate("A", reporter);
        if (reporter.errors()) {
            Report report = reporter.getReport();
            System.out.println("Error: " + report.getMsgID() + " " + report.getLocalizedText() + " xpath: "
                + report.getParameter("xpath") + " value: " + report.getParameter("xpath"));
        }
        System.out.println("***\n" + KXmlUtils.nodeToString(result, true));
    }
}
