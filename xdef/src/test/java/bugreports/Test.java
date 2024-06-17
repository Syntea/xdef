package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;

/** Test. */
public class Test {
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		String xdef =
"<xd:def\n" +
"    xmlns:xd     = \"http://www.xdef.org/xdef/4.0\"\n" +
"    xmlns:s      = \"http://www.w3.org/2003/05/soap-envelope\"\n" +
"    xmlns:s11    = \"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
"    xmlns:ws     = \"http://www.supin.cz/soap/2023/04\"\n" +
"    xd:name      = \"XDefSOAPStd\"\n" +
"    xd:root      = \"s:Envelope | s11:Envelope\">\n" +
"    <xd:macro name=\"moreAll\">options moreAttributes, moreElements, moreText</xd:macro>\n" +
"    <xd:declaration scope=\"local\">\n" +
"  /**" +
"      external method{\n" +
"        void cz.syntea.gam.or.nor.std.STDChkParser.setVersion(XXElement);\n" +
"        void cz.syntea.gam.or.nor.std.STDChkParser.setHeaderEl(XXElement);\n" +
"        void cz.syntea.gam.or.nor.std.STDChkParser.setFault(XXElement, String, String, String, String);\n" +
"        void cz.syntea.gam.or.nor.std.STDChkParser.setRequestEl(XXElement);\n" +
"        void cz.syntea.gam.or.nor.std.STDChkParser.checkMustUnderstand(XXElement, String);\n" +
"      }\n" +
"  /**/" +
"      void setVersion() {}\n" +
"      void setHeaderEl() {}\n" +
"      void setFault(String a, String b, String c, String d) {}\n" +
"      void setRequestEl() {}\n" +
"      void checkMustUnderstand(String a) {}\n" +
"  /**/" +
"    </xd:declaration>\n" +
"\n" +
"    <s11:Envelope xd:script=\"occurs 1; init setVersion();\">\n" +
"        <s11:Header xd:script=\"occurs 0..1; finally setHeaderEl()\">\n" +
"            <xd:mixed>\n" +
"                <ws:Request xd:script  = \"required; options moreAttributes, moreElements; onAbsence setFault('MustUnderstand', '0', '/Envelope/Header/Request', 'Chybí povinný hlavičkový element Request'); finally setRequestEl();\"\n" +
"                    s11:mustUnderstand = \"optional enum('0', '1')\"\n" +
"                    KodPartnera        = \"illegal\" />\n" +
"                <xd:any xd:script=\"occurs 0..; ${moreAll}; finally checkMustUnderstand('Server nedokázal zpracovat hlavičkový element s atributem mustUnderstand.')\" />\n" +
"            </xd:mixed>\n" +
"        </s11:Header>\n" +
"        <s11:Body>\n" +
"            <xd:any xd:script=\"occurs 1; ${moreAll}\" />\n" +
"        </s11:Body>\n" +
"    </s11:Envelope>\n" +
"    <s:Envelope xd:script=\"occurs 1; init setVersion();\">\n" +
"        <s:Header xd:script=\"occurs 0..1; finally setHeaderEl()\">\n" +
"            <xd:mixed>\n" +
"                <ws:Request xd:script = \"required; options moreAttributes, moreElements; onAbsence setFault('MustUnderstand', '0', '/Envelope/Header/Request', 'Chybí povinný hlavičkový element Request'); finally setRequestEl();\"\n" +
"                    s:mustUnderstand  = \"optional enum('true', 'false')\"\n" +
"                    KodPartnera       = \"illegal\" />\n" +
"                <xd:any xd:script=\"occurs 0..; ${moreAll}; finally checkMustUnderstand('Server nedokázal zpracovat hlavičkový element s atributem mustUnderstand.')\" />\n" +
"            </xd:mixed>\n" +
"        </s:Header>\n" +
"        <s:Body>\n" +
"            <xd:any xd:script=\"occurs 1; ${moreAll}\" />\n" +
"        </s:Body>\n" +
"    </s:Envelope>\n" +
"</xd:def>";
		XDPool xp = XDFactory.compileXD(null, xdef);
		String xml =
"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
"  xmlns:ws=\"http://www.supin.cz/soap/2023/04\"\n" +
"  xmlns:p1ws=\"http://ws.ckp.cz/pis/ps/P1WS/2023/09\">\n" +
"    <s:Header>\n" +
"        <ws:Request IdentZpravy=\"1\" Mode=\"DEV\" KodPartnera=\"0011\"/>\n" +
"    </s:Header>\n" +
"    <s:Body><ws:Ping/></s:Body>\n" +
"</s:Envelope>";
		XDDocument xd = xp.createXDDocument("XDefSOAPStd");
		ArrayReporter reporter = new ArrayReporter();
		Element el = xd.xparse(xml, reporter);
		System.out.println(reporter);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}