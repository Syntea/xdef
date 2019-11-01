package test.xdef;

import builtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDContainer;
import org.xdef.proc.XXElement;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test000 extends XDTester {

	public Test000() {super();}

	private static int _myError = 0;

	@Override
	/** Run tests and print error information. */
	public void test() {
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		Element el;
		XDPool xp;
		String s;
		XDDocument xd;
		StringWriter strw;
		final String dataDir = getDataDir() + "test/";
		final String tempDir = getTempDir();
		try {
			xdef = "<xd:def xmlns:xd='"+_xdNS+"' root='root'><root/></xd:def>";
			xml = "<?A A?><root><?B B?></root><?C C?>";//processing instructions
			assertFalse(test(xdef,xml,"",'P',"<root><?B B?></root>",""));
			assertNoErrors(reporter);
			xdef = //toString(obj) method
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='finally if (!toString(12).equals(\"12\")) error(toString(12));'/>\n"+
"</xd:def>";
			xml = "<a a='x'/>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required string; finally if (!(toString(from(\"@a\"))==\"x\"))\n"+
"       error(toString(from(\"@a\")));'/>\n"+
"</xd:def>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required string; finally if (!toString(from(\"@b\")).equals(\"\"))\n"+
"       error(toString(from(\"@b\")));'/>\n"+
"</xd:def>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='finally if (!toString(from(getElement(),\"@a\")).equals(\"x\"))\n"+
"         error(toString(from(\"@a\")));'/>\n"+
"</xd:def>";
			assertFalse(test(xdef, xml, "",'P'));
			// declaration of variables in different XDefinitions in collection
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "' impl-ver='1.0'>\n"+
"<xd:def xd:name='z'>\n"+
"  <xd:declaration scope = 'global'> int k = 1; int j = k; </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:name='test' xd:root = 'a' >\n"+
"  <a xd:script=\"finally {h(); if (k != 1) outln('error');}\"/>\n"+
"  <xd:declaration scope='global'>\n"+
"    int i = 0;\n"+
"    void h() {\n"+
"      if (i != 0) outln('error');\n"+
"      if (k != 1) outln('error');\n"+
"    }\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xml = "<a/>";
			assertFalse(test(xdef, xml, "test",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  String targetNS='';\n"+
"  String unq='';\n"+
"  boolean chkUnique() {\n"+
"    if (!NCName()) {\n"+
"      return error('Value is not NCName');\n"+
"    }\n"+
"    String s=getText();\n"+
"    if (unq.indexOf(' ' + s + ' ') GE 0) {\n"+
"      return error('Not Unique');\n"+
"    } else {\n"+
"      unq+=s + ' ';\n"+
"      return true;\n"+
"    }\n"+
"  }\n"+
"  boolean chkRef() {\n"+
"    if (!QName()) {\n"+
"      return error('Value is not QName');\n"+
"    }\n"+
"    String prefix=getQnamePrefix(getText());\n"+
"    String localName=getQnameLocalpart(getText());\n"+
"    String s=getNamespaceURI(prefix);\n"+
"    if (targetNS NE s) {\n"+
"      return error('Incorrect namespace: \\'' + s + '\\', ' + targetNS);\n"+
"    }\n"+
"    if (unq.indexOf(' ' + localName + ' ') GT 0) {\n"+
"      outln('targetNamespace=\"' + targetNS + '\", localName=\"' +\n"+
			"localName + '\", prefix=\"' + prefix + '\"');\n"+
"      return true;\n"+
"    } else {\n"+
"      return error('Unknown local name');\n"+
"    }\n"+
"  }\n"+
"</xd:declaration>\n"+
"\n"+
"<a>\n"+
"<meta xd:script=\"init {targetNS=@targetNS; unq=' ';} finally out(unq)\"\n"+
"     targetNS=\"required uri()\" >\n"+
"  <msg xd:script=\"occurs 1..\" name=\"required chkUnique()\" />\n"+
"  <input>\n"+
"    <msg xd:script=\"occurs 1..\" name=\"required chkRef()\" />\n"+
"  </input>\n"+
"</meta>\n"+
"</a>\n"+
"</xd:def>\n";
			xml =
"<a xmlns:ab=\"cde\">" +
"<meta targetNS=\"abc\" >" +
"<msg name=\"A\" />" +
"<msg name=\"B\" />" +
"<msg name=\"C\" />" +
"<msg name=\"D\" />" +
"<input xmlns:ab=\"abc\" xmlns:b=\"abc\" >" +
"<msg name=\"ab:B\" />" +
"<msg name=\"b:D\" />" +
"<msg xmlns:c=\"abc\" name=\"c:C\" />" +
"</input>" +
"</meta>" +
"</a>";
			assertFalse(test(xdef, xml, "",'P',
				xml,
				"targetNamespace=\"abc\", localName=\"B\", prefix=\"ab\"\n"+
				"targetNamespace=\"abc\", localName=\"D\", prefix=\"b\"\n"+
				"targetNamespace=\"abc\", localName=\"C\", prefix=\"c\"\n"+
				" A B C D "));
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='a' xd:root='a' >\n"+
"<a>\n"+
"  <b attr=\"required string()\"\n"+
"           xd:script=\"occurs 0..1; finally outln(@attr)\" />\n"+
"  optional string();finally{outln('text: '+getText());setText('text1');}\n"+
"  <c xd:script=\"occurs 1; onAbsence setElement(newElement('c'))\"/>\n"+
"  optional string(); finally {outln('text: '+getText()); setText('text2');\n"+
"    hanoi(3); if ((test!=1) | (k!=1)) outln('error');}\n"+
"</a>\n"+
"<xd:declaration>\n"+
"  int test=1;\n"+
"  int i=0, q=i;\n"+
"  int j=i;\n"+
"  int x;\n"+
"  void move(int v, int o, int k, int h) {\n"+
"    if (v GT 0) {\n"+
"      move(v -1, o, h, k);\n"+
"      outln(o + '->' + k);\n"+
"      move(v -1, h, k, o);\n"+
"    }\n"+
"    if ((test!=1) | (j!=0)) outln('error');\n"+
"    for(int i=1, j=1; i LT j; i++);\n"+
"  }\n"+
"  int r;\n"+
"  int s=1, f=r=1;\n"+
"  int y;\n"+
"  int z;\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:name='y'>\n"+
"<xd:declaration>\n"+
"  void hanoi(int v) {\n"+
"    outln('v=' + v + ':');\n"+
"    move(v, 1, 2, 3);\n"+
"  }\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:name='z'>\n"+
"<xd:declaration>int k=1;</xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xml = "<a><b attr='x' />orig1</a>\n";
			assertFalse(test(xdef, xml, "a",'P',
				"<a><b attr='x'/>text1<c/>text2</a>",
				"x\ntext: orig1\ntext: \nv=3:\n"+
				"1->2\n1->3\n2->3\n1->2\n3->1\n3->2\n1->2\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='ref X;' z=\"tokens('I|S|X')\">\n"+
"    <b xd:script='1'>\n"+
"      <xd:choice xd:script='?'>\n"+
"        <c xd:script=\"match (xpath('../../@z').toString() EQ 'I');\"\n"+
"           c=\"num();\"/>\n"+
"        <c xd:script=\"match(xpath('../../@z').toString()=='S');occurs 1\"\n"+
"           d='num();'/>\n"+
"      </xd:choice>\n"+
"     </b>\n"+
"  </a>\n"+
"  <X x='string();' y='? string();' z='? string();' />\n"+
"</xd:def>";
			xml = "<a x='a' z='X'><b/></a>";
			parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='xx' z='I'><b><c c='10'/></b></a>";
			parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='xx' y='yy' z='S'><b><c d='10'/></b></a>";
			parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			xdef = //test of recursion in X-definition
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a x='optional int();' y='optional string()'>\n"+
"    <b xd:script='occurs 0..'/>\n"+
"    <a xd:script='occurs 0..; ref a'/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a x='12' y='ab'/>";
			assertFalse(test(xdef, xml, "",'P'));
			xml =
"<a x='1' y='a'><b/><a><a><a><a x='2' y='b'><b/><a/></a></a></a></a></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='People'>\n"+
"<xd:declaration>int n=0; float sum=0.0; </xd:declaration>\n"+
"<xd:macro name=\"tiskPerson\">finally {n++; outln('Person '+@FirstName\n"+
"  + ' ' + @LastName + ' earns ' + @Salary + ' (' + getTextContent() + ')');\n"+
" sum+=int.parse(toString(@Salary)).intValue();}\n"+
"</xd:macro>\n"+
"<xd:macro name=\"tisk:Total\">finally outln('Number of people = ' + n + \n"+
" ', average salary = ' + (sum/n));</xd:macro>\n"+
"<People xd:script=\"${tisk:Total}\" >\n"+
"  <Person xd:script=\"occurs 0..; ${tiskPerson}\" \n"+
"        FirstName=\"required string(1, 50)\"\n"+
"        LastName=\"required string(1, 50)\"\n"+
"        BirthDate=\"required xdatetime('d.m.yyyy')\"\n"+
"        Salary=\"optional int(2500, 1000000)\" >\n"+
"    optional\n"+
"  </Person>\n"+
"</People>\n"+
"</xd:def>";
			xml =
"<People>" +
"<Person FirstName='John' LastName='Brown' BirthDate='1.4.1970' Salary='9500'>"+
"Good boy" +
"</Person>" +
"<Person FirstName='Mary' LastName='Brown' BirthDate='8.2.1980' Salary='2500'>"+
"It's a monster" +
"</Person>" +
"</People>";
			assertFalse(test(xdef, xml, "", 'P', xml,
				"Person John Brown earns 9500 (Good boy)\n"+
				"Person Mary Brown earns 2500 (It's a monster)\n"+
				"Number of people = 2, average salary = 6000.0\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <AAA xd:script=\"occurs 0..\" xd:text=\"* string()\">\n"+
"      <CCC xd:script=\"occurs 0..\"/>\n"+
"    </AAA>\n"+
"  </a>\n"+
"</xd:def>";
			xml =
"<a>" +
  "<AAA>" +
	"<CCC/>" +
  "</AAA>" +
  "<AAA>" +
	"text" +
  "</AAA>" +
  "<AAA>" +
	"<CCC/>\n"+
	"text" +
  "</AAA>" +
  "<AAA>" +
	"text" +
	"<CCC/>" +
  "</AAA>" +
  "<AAA>" +
	"text1" +
	"<CCC/>" +
	"text2" +
  "</AAA>" +
"</a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef = //test of occurrence in sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence occurs='0..'>\n"+
"      <AAA/><BBB/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><AAA/><BBB/><AAA/><BBB/></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"string(1,8);onTrue{if (!'1'.equals(toString(@a)))error('á');}\"/>\n"+
"</xd:def>";
			xml = "<a a=\"1\"/>";
			assertFalse(test(xdef, xml, "",'P'));
			//test ignoring of attributes from namespace XMLschema-instance.
			xd = XDFactory.xparse(dataDir + "Test000_00.xml", null);
			if (!"empty".equals(xd.getElement().getNodeName()) ||
				xd.getElement().getChildNodes().getLength() != 0) {
				fail(KXmlUtils.nodeToString(xd.getElement()));
			}
			//Lubor
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <EndPrgInfo Verze=\"fixed '2.0'\" IdProces='int()' Kanal='num(2,2)'/>\n"+
"  <Complex ver=\"fixed '1.0'\">\n"+
"    <inside xd:script=\"occurs 1..; ref EndPrgInfo\" />\n"+
"    <x xd:script=\"occurs 0..1\"> string() </x>\n"+
"  </Complex>\n"+
"</xd:def>";
			xml =
"<Complex>\n"+
"  <inside IdProces=\"123\" Kanal=\"22\"/>\n"+
"  <x>test</x>\n"+
"  <x>test1</x>\n"+
"</Complex>\n";
			el = create(compile(xdef), "", "Complex", reporter, xml);
			assertEq("<Complex ver='1.0'><inside Kanal='22'"
				+ " IdProces='123' Verze='2.0'/><x>test</x></Complex>", el);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { //test ignoring of DTD - test switch illegal DOCTYPE.
			xdef = dataDir + "Test000_01.xdef";
			xml = dataDir + "Test000_01.xml";
			Properties props = new Properties();
			//"xdef.doctype", "false"
			props.setProperty(XDConstants.XDPROPERTY_DOCTYPE,
				XDConstants.XDPROPERTYVALUE_DOCTYPE_FALSE);
			xp = XDFactory.compileXD(props, xdef);
			setProperty(XDConstants.XDPROPERTY_DOCTYPE,
				XDConstants.XDPROPERTYVALUE_DOCTYPE_FALSE);
			parse(xp, "root", xml, reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			//XML099 = DOCTYPE is set as not allowed&
			assertTrue(ex.getMessage().indexOf("XML099")>0, ex.getMessage());
		}
		resetProperties();
		try { //test ignoring of DTD.
			xdef = dataDir + "Test000_01.xdef";
			xp = compile(xdef);
			xml = dataDir + "Test000_01.xml";
			parse(xp, "root", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {  //test DTD fail
			xdef = dataDir + "Test000_01_1.xdef";
			xml = dataDir + "Test000_01.xml";
			xp = compile(xdef, getClass());
			parse(xp, "root", xml, reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (ex.getMessage() == null || !ex.getMessage().contains("SYS076")){
				fail(ex);
			}
		}
		try {
			xdef = dataDir + "Test000_01_2.xdef";
			xml = dataDir + "Test000_01_2.xml";
			xp = compile(xdef);
			el = parse(xp, "root", xml, reporter);
			assertNoErrors(reporter, xml);
			assertEq("entity e", el.getAttribute("a"));
			org.w3c.dom.DocumentType dt = el.getOwnerDocument().getDoctype();
			if (dt == null) {
				fail("Document type missing");
			} else {
				org.w3c.dom.NamedNodeMap nm = dt.getEntities();
				org.w3c.dom.Node n = nm.getNamedItem("e");
				assertFalse(n == null, "missing entity");
			}
			xdef = dataDir + "Test000_02.xdef";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = dataDir + "Test000_02.xml";
			parse(xp, "test", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"(FF0102030405060708090A0B0C0D0E0B, abc, false, 16, 4.56)\n"+
				"(FF0102030405060708090B, 11)\n"+
				"(05060708090B, 6)\n"+
				"(060708090B, 5)\n"+
				"(06070809, 4)\n"+
				"(0607, 2)\n"+
				"(0607, 2)\n"+
				"(, 0)\n"+
				"(007B02C804, 5)\n"+
				"(, 0)\n"+
				"()\n");
			strw = new StringWriter();
			xml = dataDir + "Test000_02_1.xml";
			parse(xp, "test", xml, reporter, strw, null, null);
			assertEq(strw.toString(),
				"(FF0102030405060708090A0B0C0D0E0B, abc, false, 16, 4.56)\n"+
				"(FF0102030405060708090B, 11)\n"+
				"(05060708090B, 6)\n"+
				"(060708090B, 5)\n"+
				"(06070809, 4)\n"+
				"(0607, 2)\n"+
				"(0607, 2)\n"+
				"(, 0)\n"+
				"(007B02C804, 5)\n"+
				"(, 0)\n"+
				"()\n");
			if (reporter.errors()) {
				if ((rep = reporter.getReport()) != null) {
					if (!"XDEF523".equals(rep.getMsgID()) &&
						!"XDEF515".equals(rep.getMsgID()) ||
						rep.getModification() == null ||
						rep.getModification().indexOf("/a/c[1]/d[3]/@a1") < 0) {
						fail(rep.toString());
					}
				} else {
					fail();
				}
				if ((rep = reporter.getReport()) != null) {
					if (!"XDEF523".equals(rep.getMsgID()) &&
						!"XDEF515".equals(rep.getMsgID()) ||
						rep.getModification() == null ||
						rep.getModification().indexOf("/a/c[2]/d[3]/@a2") < 0) {
						fail(rep.toString());
					}
				} else {
					fail();
				}
				if ((rep = reporter.getReport()) != null) {
					if (!"XDEF522".equals(rep.getMsgID()) ||
						rep.getModification() == null ||
						rep.getModification().indexOf("/a/c[2]/e[1]/@a3") < 0) {
						fail(rep.toString());
					}
				} else {
					fail();
				}
				if ((rep = reporter.getReport()) != null) {
					if (!"XDEF522".equals(rep.getMsgID()) ||
						rep.getModification() == null ||
						(rep.getModification().indexOf("/a/f[1]/@a4") < 0 &&
						rep.getModification().indexOf("/a/b[1]/@a") < 0)) {
						fail(rep.toString());
					}
				} else {
					fail();
				}
				if ((rep = reporter.getReport()) != null) {
					if (!"XDEF522".equals(rep.getMsgID()) ||
						rep.getModification() == null ||
						(rep.getModification().indexOf("/a/f[1]/@a4") < 0 &&
						rep.getModification().indexOf("/a/b[1]/@a") < 0)) {
						fail(rep.toString());
					}
				} else {
					fail();
				}
				while((rep = reporter.getReport()) != null) {
					fail(rep.toString());
				}
			} else {
				fail("Error not reported");
			}
			parse(dataDir + "Test000_03.xdef", //recursive reference, unique set
				"", dataDir + "Test000_03.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_04.xdef",
				"", dataDir + "Test000_04.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus.xdef", //test encoding - russian
				"test", dataDir + "Test000_rus.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_1.xdef",
				"test", dataDir + "Test000_rus_1.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_2.xdef",
				"test", dataDir + "Test000_rus_2.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_3.xdef",
				"test", dataDir + "Test000_rus_3.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_4.xdef",
				"test", dataDir + "Test000_rus_4.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_5.xdef", //UTF-16
				"test", dataDir + "Test000_rus_5.xml", reporter);
			assertNoErrors(reporter);
			parse(dataDir + "Test000_rus_6.xdef",
				"test", dataDir + "Test000_rus_6.xml", reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { //Matej2
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Vozidlo'>\n"+
"  <Vozidlo SPZ=\"optional unknown() OR regex('[A-Z0-9]{6,7}');\"\n"+
"    VIN       =\"optional known() AND string(1,26);\"\n"+
"    CisloTP   =\"optional unknown() OR pic('AA999999');\" />\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef, getClass());
			xml = "<Vozidlo SPZ='ZA384CP' VIN='VF1C066MG19952957' "+
				"CisloTP='SB746826'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter, xml);
		} catch (Exception ex) {fail(ex);}
		try {
			String defName = "RegistraceSU";
			File[] defFiles = SUtils.getFileGroup(dataDir + "Test000_05*.xdef");
			File dataFile = new File(dataDir + "Test000_05.xml");
			String errFile = tempDir + "Test000_05.err";
			String lstFile = tempDir + "Test000_05.lst";
			DecimalFormat df = new DecimalFormat("0.00");
			df.setDecimalSeparatorAlwaysShown(true);
			ReportWriter repw;
			ReportReader repIn;
			FileInputStream fis = new FileInputStream(dataFile);
			String fname = dataFile.getAbsolutePath();
			long t = System.nanoTime();
			XDBuilder xb = XDFactory.getXDBuilder(null);
			xb.setExternals(getClass());
			xb.setSource(defFiles);
			xp = xb.compileXD();
			double duration = (System.nanoTime()- t) / 1000000000.0;
			String durationInfo = "(compile " + df.format(duration) + "s";
			xd = xp.createXDDocument(defName);
			FileOutputStream fw = new FileOutputStream(errFile);
			repw = new FileReportWriter(fw);
			t = System.nanoTime();
			xd.xparse(fis, fname, repw);
			duration = (System.nanoTime()- t) / 1000000000.0;
			double kb = dataFile.length()/1000.0;
			durationInfo += "; process " + df.format(kb) +	"KB " +
				df.format(duration) + "s (" + df.format(kb/duration) + "KB/s))";
			fis.close();
			InputStreamReader isr = new InputStreamReader(
				new FileInputStream(dataFile));
			fw.close();
			FileReader fr = new FileReader(errFile);
			repIn = new FileReportReader(fr, true);
			OutputStreamWriter lst =
				new OutputStreamWriter(new FileOutputStream(lstFile));
			ReportPrinter.printListing(lst,isr,repIn, false); //no line numbers
			fr.close();
			isr.close();
			lst.close();
			setResultInfo(durationInfo);
		} catch (Exception ex) {fail(ex);}
		if (getFailCount() == 0) {
			try {
				FUtils.deleteAll(tempDir, true);
			} catch (Exception ex) {fail(ex);}
		}
		try {//Matej3
			xp = compile(dataDir + "Test000_06*.xdef", getClass());
			el=parse(xp,"RegistraceSU_",dataDir+"Test000_06_out.xml",reporter);
			assertEq(el, dataDir + "Test000_06_out.xml");
			assertNoErrors(reporter, dataDir + "Test000_06_out.xml");
			//Igor
			File[] files = SUtils.getFileGroup(dataDir + "Test000_08*.xdef");
			xp = XDFactory.compileXD(null, files, getClass());
			parse(xp, "SoapRequestB1", dataDir + "Test000_08.xml", reporter);
			assertErrors(reporter);
			//Igor2
			_myError = 0;
			xdef = dataDir + "Test000_07.xdef";
			xp = compile(xdef, getClass());
			xml = dataDir + "Test000_07_1.xml";
			parse(xp,"XDefSOAP", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				if (reporter.getErrorCount() != 4) {
					assertNoErrors(reporter, xml);
				}
				if (_myError != 2) {
					fail("Error (_myError != 2): " + _myError);
				}
			}
			_myError = 0;
			xml = dataDir + "Test000_07_2.xml";
			parse(xp,"XDefSOAP", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				if (reporter.getErrorCount() != 3) {
					assertNoErrors(reporter, xml);
				}
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xmlns:s='soap' xmlns:b='request' name='a' root='s:Envelope'>\n"+
" <s:Envelope\n"+
"   s:encodingStyle=\"fixed 'encoding'\">\n"+
"   <s:Header>\n"+
"     <b:User xd:script=\"occurs 1; ref BM#User\"\n"+
"       s:understand=\"fixed 'true'\" s:actor=\"illegal\"/>\n"+
"     <b:Request xd:script=\"occurs 1; ref BM#Request\"\n"+
"       s:understand=\"fixed 'true'\" s:actor=\"illegal\"/>\n"+
"   </s:Header>\n"+
"   <s:Body>\n"+
"	  <xd:choice>\n"+
"       <b:Ping xd:script=\"occurs 1; ref BM#Ping\"/>\n"+
"       <b:PingFlow xd:script=\"occurs 1; ref BM#PingFlow\"/>\n"+
"	  </xd:choice>\n"+
"   </s:Body>\n"+
" </s:Envelope>\n"+
"</xd:def>\n"+
"<xd:def xd:name='BM'>\n"+
"  <User IdentUser=\"required string(1,32)\"/>\n"+
"  <Request IdentZpravy=\"required string(1,32)\"\n"+
"     ReqMsgId=\"optional int()\" Mode=\"optional enum('STD', 'TST')\"/>\n"+
"  <Ping/>\n"+
"  <PingFlow Flow=\"required enum('B1B','B1')\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml =
"<s:Envelope xmlns:s='soap' xmlns:b='request' s:encodingStyle='encoding'>\n"+
"   <s:Header>\n"+
"     <b:User IdentUser=\"string 1,32\" s:understand=\"xxx\" />\n"+
"     <b:Request s:understand=\"true\"\n"+
"       IdentZpravy=\"string 1,32\"\n"+
"       ReqMsgId=\"123\"\n"+
"       Mode=\"TST\"/>\n"+
"   </s:Header>\n"+
"   <s:Body>\n"+
"     <b:PingFlow Flow=\"B1\"/>\n"+
"   </s:Body>\n"+
"</s:Envelope>\n";
			assertEq(
"<s:Envelope xmlns:s='soap' xmlns:b='request' s:encodingStyle='encoding'>"+
"<s:Header>"+
"<b:User IdentUser=\"string 1,32\" s:understand=\"true\"/>"+
"<b:Request s:understand='true' IdentZpravy='string 1,32' ReqMsgId='123'\n"+
" Mode='TST'/>"+
"</s:Header>"+
"<s:Body>"+
"<b:PingFlow Flow='B1'/>"+
"</s:Body>"+
"</s:Envelope>", parse(xdef, "a", xml, reporter));
			if (!reporter.errors()) {
				fail("error not reported");
			} else {
				rep = reporter.getReport();
				if (!"XDEF515".equals(rep.getMsgID())) {
					fail(rep.toString());
				}
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='PSP'\n"+
"		xmlns        =\"http://ws.ckp.cz/pis/B1/2007/04\">\n"+
"	<PSP ORD          =\"optional int()\"\n"+
"		InfoDatum     =\"required xdatetime('yyyyMMdd')\"\n"+
"		KodPojistitele=\"required num(4)\"\n"+
"		CisloSmlouvy  =\"required string(1,35)\"\n"+
"		PoradiVozidla =\"required num(1,10)\"\n"+
"		StavSmlouvy   =\"optional enum('T','U')\"\n"+
"		Blokace       =\"optional num(4)\"\n"+
"		AltIdentPSP   =\"optional enum('Y')\" >\n"+
"		<UsekPojisteni xd:script=\"occurs 1..\"\n"+
"			KodPojistitele  =\"required num(4)\"\n"+
"			CisloSmlouvy    =\"required string(1,35)\"\n"+
"			PoradiVozidla   =\"required num(1,10)\"\n"+
"			DruhVozidla     =\"required num(2)\"\n"+
"			UsekPojisteniOd =\"required xdatetime('yyyyMMdd')\"\n"+
"			UsekPojisteniDo =\"required xdatetime('yyyyMMdd')\" >\n"+
"			<RozhodnaUdalost xd:script=\"occurs 0..\"\n"+
"				Rezerva             =\"required int()\"\n"+
"				Vyplaceno           =\"required int()\"\n"+
"				DatumCasSU          =\"required xdatetime('yyyyMMdd0000')\"\n"+
"				KlasifikovanaUdalost=\"required enum('A','N')\"\n"+
"			/>\n"+
"		</UsekPojisteni>\n"+
"	</PSP>\n"+
"</xd:def>";
			xml =
"<c:PSP xmlns:c=\"http://ws.ckp.cz/pis/B1/2007/04\""+
" InfoDatum=\"20080319\" PoradiVozidla=\"1\" ORD=\"-19\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030331580\">"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070915\""
			+ " UsekPojisteniDo=\"20071214\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030331580\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070615\""
			+ " UsekPojisteniDo=\"20070914\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030315711\">"+
"<c:RozhodnaUdalost DatumCasSU=\"200709050000\" Rezerva=\"0\""
			+ " KlasifikovanaUdalost=\"A\" Vyplaceno=\"46879\"/>"+
"</c:UsekPojisteni>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070315\""
			+ " UsekPojisteniDo=\"20070614\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030302032\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070215\""
			+ " UsekPojisteniDo=\"20070314\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030296016\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070115\""
			+ " UsekPojisteniDo=\"20070214\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030289842\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20061115\""
			+ " UsekPojisteniDo=\"20070114\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030275736\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20061013\""
			+ " UsekPojisteniDo=\"20061112\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030265978\"/>"+
"<c:UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20060714\""
			+ " UsekPojisteniDo=\"20061012\" PoradiVozidla=\"1\""
			+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030247163\"/>"+
"</c:PSP>";
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter, xml);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter, xml);
			xml =
"<PSP xmlns=\"http://ws.ckp.cz/pis/B1/2007/04\""+
" InfoDatum=\"20080319\" PoradiVozidla=\"1\" ORD=\"-19\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030331580\">"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070915\""
				+ " UsekPojisteniDo=\"20071214\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030331580\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070615\""
				+ " UsekPojisteniDo=\"20070914\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030315711\">"+
"<RozhodnaUdalost DatumCasSU=\"200709050000\" Rezerva=\"0\""
				+ " KlasifikovanaUdalost=\"A\" Vyplaceno=\"46879\"/>"+
"</UsekPojisteni>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070315\""
				+ " UsekPojisteniDo=\"20070614\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030302032\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070215\""
				+ " UsekPojisteniDo=\"20070314\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030296016\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20070115\""
				+ " UsekPojisteniDo=\"20070214\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030289842\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20061115\""
				+ " UsekPojisteniDo=\"20070114\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030275736\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20061013\""
				+ " UsekPojisteniDo=\"20061112\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030265978\"/>"+
"<UsekPojisteni DruhVozidla=\"03\" UsekPojisteniOd=\"20060714\""
				+ " UsekPojisteniDo=\"20061012\" PoradiVozidla=\"1\""
				+ " KodPojistitele=\"0034\" CisloSmlouvy=\"3030247163\"/>"+
"</PSP>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter, xml);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter, xml);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='PSP'\n"+
"		xmlns='http://ws.ckp.cz/pis/B1/2007/04'>\n"+
"	<PSP ORD          =\"optional int()\"\n"+
"		InfoDatum     =\"required xdatetime('yyyyMMdd')\"\n"+
"		KodPojistitele=\"required num(4)\"\n"+
"		CisloSmlouvy  =\"required string(1,35)\"\n"+
"		PoradiVozidla =\"required num(1,10)\"\n"+
"		StavSmlouvy   =\"optional enum('T','U')\"\n"+
"		Blokace       =\"optional num(4)\"\n"+
"		AltIdentPSP   =\"optional enum('Y')\" >\n"+
"		<xd:choice>\n"+
"			<UsekPojisteni xd:script=\"occurs 1..; ref UsekPojisteni\"/>\n"+
"		</xd:choice>\n"+
"	</PSP>\n"+
"	<UsekPojisteni xd:script='occurs 1..'\n"+
"		KodPojistitele =\"required num(4)\"\n"+
"		CisloSmlouvy   =\"required string(1,35)\"\n"+
"		PoradiVozidla  =\"required num(1,10)\"\n"+
"		DruhVozidla    =\"required num(2)\"\n"+
"		UsekPojisteniOd=\"required xdatetime('yyyyMMdd')\"\n"+
"		UsekPojisteniDo=\"required xdatetime('yyyyMMdd')\" >\n"+
"		<xd:choice occurs=\"?\">\n"+
"			<RozhodnaUdalost xd:script=\"occurs 0..; ref RozhodnaUdalost\"/>\n"+
"		</xd:choice>\n"+
"	</UsekPojisteni>\n"+
"	<RozhodnaUdalost xd:script='occurs 0..'\n"+
"		Rezerva='required int()'\n"+
"		Vyplaceno='required int()'\n"+
"		DatumCasSU=\"required xdatetime('yyyyMMdd0000')\"\n"+
"		KlasifikovanaUdalost=\"required enum('A','N')\"\n"+
"	/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter, xml);
			assertEq("", strw.toString());
			assertEq(parse(xp, "", xml, reporter), xml);
			assertNoErrors(reporter, xml);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xmlns:s='soap' xmlns:b='request' name='a' root='s:Envelope'>\n"+
" <s:Envelope xd:script=\"init out('&lt;s:Envelope xmlns:s=&quot;soap&quot;');"
				+ " finally out('&lt;/s:Envelope&gt;')\"\n"+
"   s:encodingStyle=\"fixed 'encoding'; onTrue "
				+ "out(' s:encodingStyle = &quot;encoding&quot; &gt;')\">\n"+
"   <s:Header xd:script=\"init out('&lt;s:Header>');"
				+ " finally out('&lt;/s:Header>')\">\n"+
"     <b:User xd:script=\"occurs 1; init out('&lt;s:User'); onStartElement"
				+ " out('>'); ref BM#User; finally out('&lt;/s:User>')\"\n"+
"       s:understand=\"fixed 'true';"
				+ " onTrue out(' s:understand = &quot;true&quot;');\"\n"+
"       s:actor='illegal' />\n"+
"     <b:Request xd:script='occurs 1; ref BM#Request'\n"+
"       s:understand=\"fixed 'true';"
				+ " onTrue out(' s:understand = &quot;true&quot;')\"\n"+
"       s:actor='illegal'/>\n"+
"   </s:Header>\n"+
"   <s:Body>\n"+
"	  <xd:choice>\n"+
"       <b:Ping xd:script='occurs 1; ref BM#Ping'/>\n"+
"       <b:PingFlow xd:script='occurs 1; ref BM#PingFlow'/>\n"+
"	  </xd:choice>\n"+
"   </s:Body>\n"+
" </s:Envelope>\n"+
"</xd:def>\n"+
"<xd:def xd:name='BM'>\n"+
"  <User IdentUser=\"required string(1,32)\"/>\n"+
"  <Request IdentZpravy=\"required string(1,32)\"\n"+
"     ReqMsgId=\"optional int()\"\n"+
"     Mode=\"optional enum('STD', 'TST')\"/>\n"+
"  <Ping/>\n"+
"  <PingFlow Flow=\"required enum('B1B','B1')\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml =
"<s:Envelope xmlns:s='soap' xmlns:b='request' s:encodingStyle='encoding'>\n"+
"   <s:Header>\n"+
"     <b:User IdentUser='string 1,32'/>\n"+
"     <b:Request s:understand='true'\n"+
"       IdentZpravy='string 1,32' ReqMsgId='123' Mode='TST'/>\n"+
"   </s:Header>\n"+
"   <s:Body><b:PingFlow Flow='B1'/></s:Body>\n"+
"</s:Envelope>\n";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq(
"<s:Envelope xmlns:s='soap' xmlns:b='request' s:encodingStyle='encoding'>"+
"<s:Header>"+
"<b:User IdentUser='string 1,32' s:understand='true'/>"+
"<b:Request s:understand='true'\n"+
" IdentZpravy='string 1,32'\n"+
" ReqMsgId='123'\n"+
" Mode='TST'/>"+
"</s:Header>"+
"<s:Body>"+
"<b:PingFlow Flow='B1'/>"+
"</s:Body>"+
"</s:Envelope>", parse(xp, "a", xml, reporter, strw, null, null));
			assertEq("<s:Envelope xmlns:s=\"soap\" "+
				"s:encodingStyle = \"encoding\" >" +
				"<s:Header><s:User></s:User> s:understand = \"true\"" +
				"</s:Header></s:Envelope>", strw.toString());
			assertNoErrors(reporter, xml);
			//Honzuv problem
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def xd:name=\"EndRec\" xd:root=\"EndRec\" >\n"+
"   <EndRec IdPrace=\"required int()\"\n"+
"      DruhSouboru =\"required regex('[A-Z]\\\\d[A-Z]{1,2}')\"\n"+
"      SeqRec      =\"required int()\" >\n"+
"      <OperaceP1 xd:script=\"occurs 0..; ref OperaceP1#OperaceP1\"/>\n"+
"   </EndRec>\n"+
"</xd:def>\n"+
"<xd:def xd:name=\"OperaceP1\" root=\"OperaceP1\" >\n"+
"   <OperaceP1 VozidloPlatnostOd=\"optional xdatetime('yyyyMMdd')\" >\n"+
"      <Chyby xd:script=\"occurs 0..1; ref VR_common#Chyby\"/>\n"+
"   </OperaceP1>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xmlns:xd='" + _xdNS + "' name=\"VR_common\" >\n"+
"   <Osoba StatPrislusnos=\"optional string(1,3)\" >\n"+
"      <Pobyt xd:script  =\"occurs 0..1; ref Adresa\" />\n"+
"   </Osoba>\n"+
"   <Organizace NazevOrganizace=\"optional string(1,100)\"\n"+
"      IC                      =\"optional string(1,12)\"\n"+
"      StatPrislusnost         =\"optional string(1,3)\" >\n"+
"      <Sidlo xd:script=\"occurs 0..1; ref Adresa\" />\n"+
"   </Organizace>\n"+
"   <OSVC StatPrislusnost=\"optional string(1,3)\" >\n"+
"      <Sidlo xd:script=\"occurs 0..1; ref Adresa\" />\n"+
"   </OSVC>\n"+
"   <Adresa StatAdresa=\"optional string(1,3)\" />\n"+
"   <FirmaText NazevText=\"optional string(1,200)\"\n"+
"      IC               =\"optional int(0,99999999)\" >\n"+
"      <AdresaText xd:script=\"occurs 0..1\" >\n"+
"         required string(1,200)\n"+
"      </AdresaText>\n"+
"   </FirmaText>\n"+
"   <Vozidlo PocetMistCelkem=\"optional int(1,999)\" />\n"+
"   <VozidloPS PojistneBM=\"optional int(1,999999)\" />\n"+
"   <ZelenaKarta DatumZneplatneni=\"optional xdatetime('yyyyMMdd')\"/>\n"+
"   <Pojisteni PojisteniOd=\"optional xdatetime('yyyyMMdd')\"\n"+
"      PojisteniDo        =\"optional xdatetime('yyyyMMdd')\" />\n"+
"   <Pojistnik PlatnostOd=\"optional xdatetime('yyyyMMdd')\" >\n"+
"      <xd:choice>\n"+
"         <Osoba xd:script=\"occurs 1; ref Osoba\" />\n"+
"         <OSVC xd:script=\"occurs 1; ref OSVC\" />\n"+
"         <Organizace xd:script=\"occurs 1; ref Organizace\" />\n"+
"      </xd:choice>\n"+
"   </Pojistnik>\n"+
"   <Drzitel PlatnostOd=\"optional xdatetime('yyyyMMdd')\" >\n"+
"      <xd:choice>\n"+
"         <Osoba xd:script=\"occurs 1; ref Osoba\" />\n"+
"         <OSVC xd:script=\"occurs 1; ref OSVC\" />\n"+
"         <Organizace xd:script=\"occurs 1; ref Organizace\" />\n"+
"      </xd:choice>\n"+
"   </Drzitel>\n"+
"   <Vlastnik PlatnostOd=\"optional xdatetime('yyyyMMdd')\">\n"+
"      <xd:choice>\n"+
"         <Osoba xd:script=\"occurs 1; ref Osoba\" />\n"+
"         <OSVC xd:script=\"occurs 1; ref OSVC\" />\n"+
"         <Organizace xd:script=\"occurs 1; ref Organizace\" />\n"+
"      </xd:choice>\n"+
"   </Vlastnik>\n"+
"   <Chyby>\n"+
"      <Chyba xd:script=\"occurs 1..\" Popis=\"optional string()\" />\n"+
"   </Chyby>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			el = parse(xp, "EndRec",
 "<EndRec IdPrace=\"1\" SeqRec=\"3\" DruhSouboru=\"L1D\">\n"
+" <OperaceL1 Operace=\"ZmenaSU\"\n"
+"   IdentZaznamu=\"500000000003\"\n"
+"   CisloSmlouvy=\"???\"\n"
+"   PoradiVozidla=\"???\"/>\n"
+"</EndRec>", reporter);
			if ((rep = reporter.getReport()) == null) {
				fail("Error not reported");
			} else if (!"XDEF501".equals(rep.getMsgID())) {
				fail("Incorrect error: " + reporter.toString());
			} else if ((rep = reporter.getReport()) != null) {
				fail("Unexpected error: " + rep.toString());
			}
			if (el == null) {
				fail("Root element is not available");
			}
			// external methods - error in declaration
			//force compilation of test.xdef.Test002;
			test.xdef.Test002.class.getClass();
			xdef = //missing close bracket in method declaration
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method boolean test.xdef.Test002.tab(String, String\n"+
"</xd:declaration>\n"+
"<a xd:script=\"finally {out(tab('a', 'b')); pp()}\" />\n"+
"<xd:declaration>\n"+
"  void pp() {out(','+tab('a', 'b')+','+test.xdef.Test002.tab('a','b'));}\n"+
"</xd:declaration>\n"+
"</xd:def>\n";
			XDFactory.compileXD(null, xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || s.indexOf("XDEF410") < 0) {
				fail(ex);
			}
		}
		try {// error *
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='RegistracePN'>\n"+
" <RezervaPU Rezerva=\"required int(0,99999999);\"\n"+
"   Mena=\"onAbsence setText('CZK')\"/>\n"+
"   <Plneni PodtypSkody=\"required string();\"\n"+
"     Vyplaceno=\"required int(0,99999999);\"\n"+
"     Mena     =\"required string(); onAbsence setText('CZK')\"/>\n"+
"   <PN xd:script=\"finally out('PN')\"\n"+
"     IdentZaznamu   =\"required string(1,12)\"\n"+
"     CisloPU        =\"required string(3,35)\"\n"+
"     PodtypSkody    =\"optional string()\"\n"+
"     PoradiVozidlaSU=\"optional int(1,999)\"\n"+
"     DatumUcinnosti =\"required xdatetime('yyyyMMddHHmm');\">\n"+
"     <RezervaPU xd:script=\"occurs 1..;  ref RezervaPU\"/>\n"+
"     <Plneni xd:script=\"occurs 0..;  ref Plneni\"/>\n"+
"     <SkodaVozidla xd:script=\"occurs 0..;  ref SkodaVozidla\"/>\n"+
"  </PN>\n"+
"  <SkodaVozidla\n"+
"     Mena=\"optional string();onAbsence{if(@Skoda)setText('CZK');}\"\n"+
"    KodOpravny=\"optional string()\" />\n"+
"  <RegistracePN  xd:script=\"ref PN\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			el = parse(xp, "",
"<RegistracePN IdentZaznamu=\"500000000004\""+
" CisloPU=\"2000000001/4\" PodtypSkody=\"B1\""+
" PoradiVozidlaSU=\"4\" DatumUcinnosti=\"200002020456\">\n"+
"  <Plneni PodtypSkody=\"B1\" Vyplaceno=\"7942\" />\n"+
"</RegistracePN>",
				reporter, strw, null, null);
			if ((rep = reporter.getReport()) == null){
				fail("Error not reported");
			} else if (!"XDEF539".equals(rep.getMsgID())) {
				fail("Incorrect error: " + rep.toString());
			}
			assertEq("PN", strw.toString());
			if (el != null) {
				el = (Element)el.getElementsByTagName("Plneni").item(0);
				s = el.getAttribute("Mena");
				if (!"CZK".equals(s)) {
					fail("Incorrect attribute 'Mena': '" + s +"'");
				}
			} else {
				fail("Root element is not available");
			}
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='a' root='aaa#log|bbb#mog'/>\n"+
"<xd:def xd:name='aaa'>\n"+
"<log bttr='required'> </log>\n"+
"</xd:def>\n"+
"<xd:def xd:name='bbb'>\n"+
"<mog attr='required'></mog>\n"+
"</xd:def>\n"+
"<xd:def name='abc' root='abc#Davka'>\n"+
"<Davka Verze=\"fixed '2.0'\" xmlns:a='abc'\n"+
"       a:Kanal=\"required\"\n"+
"       Seq=\"required int()\"\n"+
"       SeqRef=\"optional int()\"\n"+
"       Date=\"required xdatetime('d.M.y')\"\n"+
"       xd:script=\"options moreAttributes\">\n"+
"   <File Name=\"required string(1,256)\"\n"+
"         Format=\"required tokens('TXT|XML|CTL')\"\n"+
"         Kind=\"required string(3,3)&amp;(eq('abc')|eq('xyz'))\"\n"+
"         RecNum=\"required num(8)\"\n"+
"         xd:script=\"occurs 1..\">\n"+
"       <xd:mixed>\n"+
"       <CheckSum Type=\"required tokens('MD5|CRC')\"\n"+
"                 Value=\"required string()\"\n"+
"                 xd:script=\"occurs 1\">\n"+
"         optional\n"+
"       </CheckSum>\n"+
"       <x xd:script=\"occurs 1..5; ref empty.node\"/>\n"+
"       </xd:mixed>\n"+
"   </File>\n"+
"   <y xd:script=\"ref y\" />\n"+
"   <xd:choice>\n"+
"	    <Osoba xd:script=\"occurs 0..1; ref Osoba\" />\n"+
"		<OSVC  xd:script=\"occurs 0..1; ref OSVC\"/>\n"+
"		<O xd:script=\"1..2; ref O; init out('&lt;'+getElementName())\"/>\n"+
"   </xd:choice>\n"+
"</Davka>\n"+
"<Osoba jmeno=\"required string()\"/>\n"+
"<OSVC nazev=\"required string()\"/>\n"+
"<O adr=\"onTrue out(' '+getAttrName()+'=\\''+getText()+'\\'/&gt;');\"/>\n"+
"<empty.node/>\n"+
"<qwert xd:script=\"ref y\" />\n"+
"<y xd:script=\"ref z\" />\n"+
"<z><fff attr=\"optional\"/></z>\n"+
"<zz attr=\"1\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef, getClass());
			xml =
"<Davka xmlns:x=\"abc\"\n"+
"       x:Kanal=\"AB\"\n"+
"       Date=\"1.1.2003\"\n"+
"       Seq=\"987654321\">\n"+
"   <File Name=\"abc.dat\"\n"+
"         Format=\"TXT\"\n"+
"         Kind=\"xyz\"\n"+
"         RecNum=\"12345678\">\n"+
"       <CheckSum Type=\"MD5\"\n"+
"         Value=\"0xfadb8701a\">\n"+
"         text\n"+
"       </CheckSum>\n"+
"       <x/>\n"+
"   </File>\n"+
"   <y>\n"+
"     <fff attr=\"attr\"/> \n"+
"   </y> \n"+
"   <O adr=\"ulice1\" />\n"+
"   <O adr=\"ulice2\" />\n"+
"</Davka>\n";
			strw = new StringWriter();
			el = parse(xp, "abc", xml, reporter, strw, null, null);
			if (reporter.errorWarnings()) {
				assertNoErrors(reporter, xml);
				fail(el != null ? KXmlUtils.nodeToString(el, true) : "el=null");
			}
			assertEq(strw.toString(), "<O adr='ulice1'/><O adr='ulice2'/>");
		} catch (Exception ex) {fail(ex);}
		try {//ParseResult created with check method and incomplete sequence
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='global'>\n"+
"  ParseResult p=int.check('123');\n"+
"</xd:declaration>\n"+
"<A a='p.matches()'>\n" +
"    <xd:sequence>\n" +
"        <A/>\n" +
"        <xd:sequence xd:script=\"occurs ?\">\n" +
"            <B/>\n" +
"            <C/>\n" +
"        </xd:sequence>\n" +
"        <D/>\n" +
"    </xd:sequence>\n" +
"</A>\n" +
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A a='1'><A/><B/><C/><D/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A a='1'><A/><B/><D/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <A> <X x='optional boolean()'/></A>\n" +
"  <B>\n" +
"    <C xd:script=\"create from('X')\"\n" +
"      y = \"required enum('A', 'N'); create convertBoolean(from('@x'))\"/>\n" +
"  </B>\n" +
"</xd:def>";
			xp = compile(xdef, this.getClass());
			xml = "<A><X x='0'/></A>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(xd.xcreate("B", reporter), "<B><C y='N'/></B>");
			assertNoErrors(reporter);
 			xml = "<A><X x='1'/></A>";
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(xd.xcreate("B", reporter), "<B><C y='A'/></B>");
			assertNoErrors(reporter);
 			xml = "<A><X/></A>";
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(xd.xcreate("B", reporter), "<B><C y='N'/></B>");
			assertNoErrors(reporter);			
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

////////////////////////////////////////////////////////////////////////////////
// methods and objects for mytest5
////////////////////////////////////////////////////////////////////////////////
	private static String d(final XDValue... x) {
		if (x == null) {
			return "(null)";
		} else if (x.length == 0) {
			return "()";
		} else {
			String s = "(" + x[0].toString();
			for (int i = 1; i < x.length; i++) {
				s += ", " + x[i];
			}
			return s + ")";
		}
	}
	public static String b(XXNode c, XDValue[] x) {return d(x);}
	public static String bx(XDValue[] x) {return "b" + d(x);}
////////////////////////////////////////////////////////////////////////////////
// methods and objects for Matej2
////////////////////////////////////////////////////////////////////////////////
	public static void fail(XXNode c, XDValue[] p) {}
	public static void setErr(XXNode c, XDValue... p) {_myError++;}
	public static boolean unknown(XXNode c, XDValue[] e) {return false;}
	public static boolean known(XXNode c, XDValue... e) {return true;}
	public static boolean tab(XXNode c, XDValue... p) {return true;}
	public static void setDefault_ifEx(XXNode c, XDValue... p){}
	public static void emptySubjHasAddr(XXNode c, XDValue... p){}
	public static void notEmptyMisto(XXNode c, XDValue[] p){}
	public static void isEqual(XXNode c, XDValue[] p) {}
	public static boolean fil0(XXNode c, XDValue[] p) {return true;}
	public static boolean kvadrant(XXNode c, XDValue[] p) {return true;}
	public static void exactlyOneAttr(XXNode c, XDValue[] p){}
	public static void outputIVR(XXNode c, XDValue[] p) {}
	public static String getIdAdresa(XXNode c, XDValue[] p) {return "-1";}
	public static String getIdOsoba(XXNode c, XDValue[] p) {return "-1";}
	public static String getIdSubjekt(XXNode c,XDValue[] p) {return "-1";}
	public static String getTab(XXNode c, XDValue[] p) {return "-1";}
	public static String getNonEmptyAttr(XXNode c,XDValue[]p){return"-1";}
	public static String getIdModelText(XXNode c,XDValue[] p){return"-1";}
	public static String getIdBarvaText(XXNode c,XDValue[]p){return "-1";}
	public static String getIdPojistitel(XXNode c,XDValue[]p){return"-1";}
	public static String getIdPrace(XXNode c,XDValue[] p) {return "-1";}
	public static String getSeqRec(XXNode c, XDValue[] p) {return "-1";}
////////////////////////////////////////////////////////////////////////////////
// methods and objects for Matej3
////////////////////////////////////////////////////////////////////////////////
	public static String getTabId(XXNode c, XDValue[] p) {return "-1";}
////////////////////////////////////////////////////////////////////////////////
// methods and objects for Igor02
////////////////////////////////////////////////////////////////////////////////
	public static String getTab(XXNode x, String s1, String s2,String s3){
		return "-1";
	}
	public static String getIdPrace(XXNode x) {return "-1";}
	public static String getAppName(XXNode x) {return "-1";}
	public static String getAppVersion(XXNode x) {return "-1";}
	public static String getAppInstallationDate(XXNode x){return "-1";}
	public static String getRequestAccepted(XXNode x) {return "-1";}
	public static String getPending(XXNode x) {return "-1";}
	public static void outputIOR(XXNode x, String s) {}
	public static void setPreRequest(XXNode x) {}
	public static void setPreBody(XXNode x) {}
	public static void answerIOR(XXNode x, String s) {}
	public static void ctlIOR(XXNode x) {}
    public static String convertBoolean(XXElement el, XDContainer boolContainer){
        return boolContainer.getXDItemsNumber() == 1 
			&& "1".equals(boolContainer.getXDItem(0).toString()) ? "A" : "N";
    }
////////////////////////////////////////////////////////////////////////////////
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
