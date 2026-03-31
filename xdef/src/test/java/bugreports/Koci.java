package bugreports;

import java.math.BigInteger;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import java.util.Properties;
import org.xdef.XDBNFGrammar;
import org.xdef.XDBNFRule;
import org.xdef.XDParseResult;
import org.xdef.xml.KXmlUtils;

public class Koci {

    public static void bigInt(BigInteger val) {System.out.println("bugInt: " + val);}

    public static void validateWithRule(XDBNFGrammar val, String rule, String inputString) {
        XDBNFRule xrule = val.getRule(rule);
        XDParseResult result = xrule.perform(inputString);
        System.out.println("String: " + inputString);
        System.out.println("Rule: " + rule);
        System.out.println("Result: " + result.matches());
    }

    public static void main(String[] args) {
        Properties props = System.getProperties();
        XDPool xdpool = XDFactory.compileXD(props,
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"root\" name=\"Example1\">\n" +
"  <xd:BNFGrammar name='g'>\n" +
"    InpItem ::= 'CisloZml' | 'CisloDoc1' | 'CisloDoc2' | 'CisloZK' | 'DatumCasSU'\n" +
"    wspaces  ::= [#9#10#13#32]\n" +
"    separ    ::= \",\" wspaces*\n" +
"    inpItemList   ::= '-' | '*' | (InpItem (separ InpItem)*)\n" +
"  </xd:BNFGrammar>\n" +
"  <xd:declaration scope=\"local\">\n" +
"    external method void bugreports.Koci.validateWithRule(XDBNFGrammar, String, String);\n" +
"    external method void bugreports.Koci.bigInt(BigInteger);\n" +
"    type inpItemList g.rule('inpItemList');\n" +
"    BigInteger bi = 0;\n" +
"  </xd:declaration>\n" +
"  <root inpItemList =\"? inpItemList(); onTrue {validateWithRule(g, 'inpItemList', getText()); bigInt(bi)}\" />\n" +
"</xd:def>");
        XDDocument xdoc = xdpool.createXDDocument("Example1");
        ArrayReporter reporter = new ArrayReporter();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root inpItemList=\"CisloZml\" />";
        Element result = xdoc.xparse(xml, reporter);
        if (reporter.errors()) {
            Report report = reporter.getReport();
            System.out.println("Error: " + report.getMsgID() + " " + report.getLocalizedText()
                + " xpath: " + report.getParameter("xpath") + " value: " + report.getParameter("xpath")
            );
        }
        System.out.println(KXmlUtils.nodeToString(result, true));
    }
}
