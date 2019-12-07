package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDContainer;
import org.xdef.proc.XXData;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test002 extends XDTester {

	public Test002() {super();}

	private static int _errorCount;
	private static int _errorCode;
	private static final String OTAZNIKY3 = "???"; // rizene nevyplnena hodnota

	public static boolean fil0(long i) {return true;}
	public static boolean tab(String s1, String s2) {return true;}
	public static void setErr(XXNode chkel, long i) {
		_errorCount++;
		_errorCode = (int) i;
		chkel.clearTemporaryReporter();
	}
	public static void setNullIfZero() {}
	public static String modelVozidlaNeuveden() {return "Neuvedeno";}
	public static String getTab(String s1,String s2,String s3){return s1+s2+s3;}
	public static String getIdPrace() {return "1";}
	public static boolean known(XXData c) {
		return !OTAZNIKY3.equals(c.getTextValue());
	}
	public static boolean unknown(XXData c, XDValue[] params){
		  return OTAZNIKY3.equals(c.getTextValue());
	}
	public static void initData() {}
	public static void output(String data) {}
	public static void setHeaderEl() {}
	public static void setRequestEl() {}
	public static void setKodPartnera() {}
	public static String testGetXPos(XXNode x) {return x.getXPos();}

	@Override
	/** Run tests. */
	public void test() {
		String xdef;
		String xml;
		XDPool xp;
		XDDocument xd;
		StringWriter strw;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		Element el;
		String s;
		final String dataDir = getDataDir() + "test/";
		try {
			xp = compile(dataDir + "Test002_1.xdef");
			xml = dataDir + "Test002_1.xml";
			parse(xp, "BPEL_com", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				if (reporter.getErrorCount() != 2) {
					while ((rep = reporter.getReport()) != null) {
						fail(rep.toString());
					}
					ReportPrinter.printListing(
						System.err, new File(xml), reporter, true);
				} else {
					rep= reporter.getReport();
					if ("XDEF501".equals(rep.getMsgID())) {
						rep= reporter.getReport();
						if(!"XDEF539".equals(rep.getMsgID())) {
							fail(rep.toString());
						}
					} else if ("XDEF539".equals(rep.getMsgID())) {
						rep= reporter.getReport();
						if(!"XDEF501".equals(rep.getMsgID())) {
							fail(rep.toString());
						}
					} else {
						fail(rep.toString());
					}
				}
			}
			xp = compile(dataDir + "Test002_3.xdef");
			strw = new StringWriter();
			xml = dataDir + "Test002_3.xml";
			parse(xp, "Example", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq("Example - start\n"+
					"partnerLinks\n"+
					"{{if(true)if(false);\n"+
					"else if(false);else\n"+
					"while(false){if(false);else\n"+
					"{;;}\n"+
					";}\n"+
					"\n"+
					";}}Example - end\n", strw.toString());
			xdef = dataDir + "Test002_4.xdef";
			xp = compile(xdef, getClass());
			xml = dataDir + "Test002_4_1.xml";
			strw = new StringWriter();
			parse(xp, "DefSystemStatistics", xml, reporter, strw,null,null);
			assertNoErrors(reporter);
			xml = dataDir + "Test002_4_2.xml";
			strw = new StringWriter();
			parse(xp, "DefSystemStatistics", xml, reporter, strw,null,null);
			assertNoErrors(reporter);
			xdef = dataDir + "Test002_5.xdef";
			xml = dataDir + "Test002_5.xml";
			strw = new StringWriter();
			parse(xdef, "instance", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			xdef =  //reference to element with match expression.
"<xdef:def xmlns:xdef='" + _xdNS + "' xdef:root='Select'>\n"+
"    <Select> \n"+
"        <inetOrgPerson xdef:script=\"ref inetOrgPersonDef; occurs 1\" />\n"+
"    </Select>\n"+
"    <inetOrgPersonDef>\n"+
"        <user xdef:script=\"ref userCertificateDef; occurs 0..\" />\n"+
"    </inetOrgPersonDef>\n"+
"    <userCertificateDef i.SerialNum=\"optional hexBinary()\"\n"+
"                        i.Issuer=\"optional string()\"\n"+
"                        xdef:script=\"match @i.SerialNum AAND @i.Issuer\"/>\n"+
"</xdef:def>";
			xml =
"<Select>\n"+
" <inetOrgPerson>\n"+
"  <user i.SerialNum=\"198b11d13f9a8ffe69a0\"/>\n"+
" </inetOrgPerson>\n"+
"</Select>";
			parse(xdef, "", xml, reporter);
			if (reporter.errors()) {
				if (!"XDEF501".equals(reporter.getReport().getMsgID())) {
					fail(reporter.printToString());
				}
			} else {
				fail("error not reported");
			}
			xdef = //boolean operators
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"</xd:declaration>\n"+
"  <A a='t1() OR t2'>list(int(1,10))</A>\n"+
"</xd:def>";
			xml = "<A a='99'>1 2</A>";
			strw = new StringWriter();
			el = parse(xdef, "", xml, reporter, strw, null, null);
			assertEq(xml, el);
			assertNoErrors(reporter);
			strw.close();
			assertEq("t1t2", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"</xd:declaration>\n"+
"  <A a='t1() OR t2'>list(%item=int(1,10))</A>\n"+
"</xd:def>";
			xml = "<A a='99'>1 2</A>";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			strw.close();
			assertEq("t1t2", strw.toString());
			try {
				xdef = //test ListOf with XDEF_2_0
"<xd:def xmlns:xd='"+ XDConstants.XDEF20_NS_URI + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"</xd:declaration>\n"+
"  <A a='t1() OR t2'>ListOf(int(1,10))</A>\n"+
"</xd:def>";
				xml = "<A a='99'>1 2</A>";
				strw = new StringWriter();
				setProperty(XDConstants.XDPROPERTY_WARNINGS,
					XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
				el = parse(xdef, "", xml, reporter, strw, null, null);
				assertEq(xml, el);
				assertNoErrors(reporter);
				strw.close();
				assertEq("t1t2", strw.toString());
				setProperty(XDConstants.XDPROPERTY_WARNINGS,
					XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
				compile(xdef);
			} catch (Exception ex) {
				s = ex.getMessage();
				if (s == null || !s.contains("W XDEF998")) {fail(ex);}
			}
			setProperty(XDConstants.XDPROPERTY_WARNINGS,
				XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"</xd:declaration>\n"+
"  <A a='t1() OOR t2'>list(%item=int(1,10))</A>\n"+
"</xd:def>";
			xml = "<A a='99'>1 2</A>";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			strw.close();
			assertEq("t1t2", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"  int i = 1, j = 10;\n"+
"</xd:declaration>\n"+
"  <A a='t1() AND t2()'>list(%item=int(i,j))</A>\n"+
"</xd:def>";
			xml = "<A a='99'>1 2</A>";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, "", xml, reporter, strw, null, null));
			assertErrors(reporter);
			strw.close();
			assertEq("t1t2", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  boolean t1() {out('t1'); return false;}\n"+
"  boolean t2() {out('t2'); return true;}\n"+
"</xd:declaration>\n"+
"  <A a='t1() AAND t2'>list(%item=int(1,10))</A>\n"+
"</xd:def>";
			xml = "<A a='99'>1 2</A>";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, "", xml, reporter, strw, null, null));
			assertErrors(reporter);
			strw.close();
			assertEq("t1", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='a'>\n"+
"<a atr=\"optional string(); default 'abcd'\">\n"+
"  <xd:choice ref='action'/>\n"+
"</a>\n"+
"<xd:choice name='action'>\n"+
"<B xd:script='ref B'/>\n"+
"<C xd:script='ref C'/>\n"+
"</xd:choice>\n"+
"<B xd:script=\"finally outln('B')\"/>\n"+
"<C xd:script=\"finally outln('C')\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><C/></a>\n";
			strw = new StringWriter();
			el = parse(xp, "A", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			if (!"C\n".equals(strw.toString())) {
				fail(strw.toString());
			}
			if (!"abcd".equals(el.getAttribute("atr"))) {
				fail(el.getAttribute("atr"));
			}
			xml = "<a atr='wxyz'><C/></a>\n";
			strw = new StringWriter();
			el = parse(xp, "A", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq("C\n", strw.toString());
			if (!"wxyz".equals(el.getAttribute("atr"))) {
				fail(el.getAttribute("atr"));
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a>\n"+
"      <ZmenaPS xd:script=\"occurs 1..; finally " +
"           {int i=errors(); if (i==6) out('OK'); else out('ERROR: ' + i);}\"" +
"           IdentZaznamu='required int()'\n"+
"           CisloSmlouvy='required int()'\n"+
"           CisloDokladuPojisteni='required int()'\n"+
"           PoradiVozidla='required int()'\n"+
"           DatumZmeny='required int()' >\n"+
"           <b/>\n"+
"      </ZmenaPS>\n"+
"   </a>\n"+
"</xd:def>\n";
			xml = "<a><ZmenaPS>\n IdentZaznamu='1'\n"+
"CisloSmlouvy='3000074848' CisloDokladuPojisteni='3000074848'\n"+
" PoradiVozidla='1' DatumZmeny='20060101'>\n"+
"<b/>" +
"</ZmenaPS>" +
"</a>";
			strw = new StringWriter();
			parse(xdef, "", xml, reporter, strw, null, null);
			assertEq(6, reporter.getErrorCount());
			assertTrue(reporter.printToString().indexOf("XDEF526") > 0 &&
				reporter.printToString().indexOf("XDEF534") > 0,
				"Error not reported");
			assertEq("OK", strw.toString());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xmlns:s='http://www.w3c.org/2003/05/soap-envelope'\n"+
"        xmlns:p='http://ws.ckp.cz/pis/B1/2006/10'\n"+
"        impl-version='0.0.1' impl-date='18.9.2006' root='s:Envelope'>\n"+
"<xd:macro\n"+
"name=\"moreAll\">options moreAttributes,moreElements,moreText;</xd:macro>\n"+
"<s:Envelope xd:script=\"occurs 1\"\n"+
"        s:encodingStyle=\"fixed 'http://www.syntea.cz/ckp/pis/encoding'\">\n"+
"    <s:Header xd:script=\"occurs 0..1; finally setHeaderEl()\">\n"+
"     <p:Request xd:script=\"occurs 1;\n"+
"             options moreAttributes,moreElements; finally setRequestEl()\"\n"+
"           KodPartnera=\"optional; onTrue setKodPartnera()\"\n"+
"           s:mustUnderstand=\"required enum('true','1')\"/>\n"+
"    </s:Header>\n"+
"    <s:Body xd:script=\"occurs 1\">\n"+
"    <xd:choice>\n"+
"        <p:Ping xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:PingFlow xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Get_PSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Get_VolnePSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Get_NositelPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Get_SmlouvyPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Set_PrenosPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Set_MultiPrenosPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Set_BlokujPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"        <p:Set_UvolniPSP xd:script=\"occurs 1; ${moreAll}\"/>\n"+
"      </xd:choice>\n"+
"    </s:Body>\n"+
"</s:Envelope>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef, getClass());
			xml =
"<?xml version=\"1.0\"?>\n"+
"<s:Envelope xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"  xmlns:p=\"http://ws.ckp.cz/pis/B1/2006/10\"\n"+
"  s:encodingStyle=\"http://www.syntea.cz/ckp/pis/encoding\">\n"+
"  <s:Header>\n"+
"    <p:Request KodPartnera=\"0001\" IdentZpravy=\"154\"\n"+
"      RefMsgID=\"123\" s:mustUnderstand=\"true\" />\n"+
"  </s:Header>\n"+
"  <s:Body>\n"+
"    <p:Ping/>\n"+
"  </s:Body>\n"+
"</s:Envelope>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml =
"<q:Envelope xmlns:q=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"  xmlns:p=\"http://ws.ckp.cz/pis/B1/2006/10\"\n"+
"  q:encodingStyle=\"http://www.syntea.cz/ckp/pis/encoding\">\n"+
"  <p:Header>\n"+
"    <q:Request KodPartnera=\"0001\" IdentZpravy=\"154\"\n"+
"      RefMsgID=\"123\" q:mustUnderstand=\"true\" />\n"+
"  </p:Header>\n"+
"  <q:Body>\n"+
"    <q:Ping/>\n"+
"  </q:Body>\n"+
"</q:Envelope>";
			reporter = new ArrayReporter();
			parse(xp, "", xml, reporter);
			if (!reporter.errorWarnings()) {
				fail("No errors reported!");
			} else if ((rep = reporter.getReport()) == null ||
				rep.getModification().indexOf(
				"&{0}p:Header&{xpath}/q:Envelope") < 0 ||
				(rep = reporter.getReport()) == null ||
				rep.getModification().indexOf(
				"&{xpath}/q:Envelope/q:Body") < 0 ||
				(rep = reporter.getReport()) == null ||
				rep.getModification().indexOf(
				"&{0}q:Ping&{xpath}/q:Envelope/q:Body") < 0 ||
				reporter.getReport() != null) {
				reporter.reset();
				fail(reporter.printToString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a a=\"required; create '1'\" >\n"+
"  <b xd:script=\"create from('/b')\"\n"+
"     y=\"required; create from('/b/@x')\">\n"+
"    <c xd:script = \"create from('c')\"\n"+
"       z=\"required; create from('@x')\"\n"+ //tady by melo vzit 3
"       c=\"required\"/>\n"+
"  </b>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<b x=\"2\"><c c=\"c\" x=\"3\"/></b>";
			assertEq(create(xdef, "", "a", reporter, xml),
				"<a a=\"1\"><b y=\"2\"><c z=\"3\" c=\"c\"/></b></a>");
			assertNoErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='test' impl-version='2.4.1.0' impl-date='28.12.2006'>\n"+
"<Set_BlokujPSP_\n"+
"  IdPrace=\"required int(); create getIdPrace()\" >\n"+
"  <Set_BlokujPSP xd:script=\"occurs 1; ref B1_common_#Set_BlokujPSP\" />\n"+
"</Set_BlokujPSP_>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name= 'B1_common_'>\n"+
"<Set_BlokujPSP xd:script=\"create from('/Set_BlokujPSP')\"\n"+
"  IdPojistitel     =\"required; create from('@KodPojistitele')\" >\n"+
"  <ZdrojovaSmlouva xd:script=\"occurs 1; ref Id_Smlouva;\n"+
"                   create from('ZdrojovaSmlouva')\" />\n"+
"</Set_BlokujPSP>\n"+
"<Id_Smlouva\n"+
//tady by melo vzit 62
"  IdPojistitel=\"required; create from('@KodPojistitele')\"\n"+
"  CisloSmlouvy=\"required\"\n"+
"  PoradiVozidla=\"required\"/>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			reporter = new ArrayReporter();
			xp = compile(xdef, getClass());
			xml = "<Set_BlokujPSP KodPojistitele=\"0002\">" +
				"<ZdrojovaSmlouva PoradiVozidla=\"1\"" +
				" CisloSmlouvy=\"3060000030\"" +
				" KodPojistitele=\"0062\"/>" +
				"</Set_BlokujPSP>";
			strw  = new StringWriter();
			el = create(xp, "test", "Set_BlokujPSP_",reporter,xml,strw,null);
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			s = ((Element) el.getChildNodes().item(0).
				getChildNodes().item(0)).getAttribute("IdPojistitel");
			assertEq("0062", s, KXmlUtils.nodeToString(el, true));

			// we try array of xdefinitions
			String xdef1, xdef2, xdef3, data;
			xdef1 =
"<x:def xmlns:x='" + _xdNS + "' name='P1_common'>\n"+
"<Vozidlo\n"+
"  CisloTP         =\"optional pic('AA999999'); onFalse setErr(4208)\"\n"+
"  VIN             =\"optional string(1,26); onFalse setErr(4208)\"\n"+
"  SPZ             =\"optional string(2,12); onFalse setErr(4208)\"\n"+
"  DruhVozidla     =\"required fil0(2) AND tab(\n"+
"                     'CC_DruhVozidla','KodDruhuVozidla');\n"+
"                     onFalse setErr(4226); onAbsence setErr(4202)\"\n"+
"  ZnackaText      =\"optional string(1,30); onFalse setErr(4208)\"\n"+
"  ModelText       =\"optional string(1,40); onFalse setErr(4208)\"\n"+
"  KodModeluVozidla=\"required tab('CC_ModelVozidla','KodModeluVozidla');\n"+
"                     onFalse setErr(4225);\n"+
"                     onAbsence setText(modelVozidlaNeuveden())\"\n"+
"  RokDoProvozu    =\"optional xdatetime('yyyy'); onFalse setErr(4208)\"\n"+
"  BarvaText       =\"optional string(1,20); onFalse setErr(4208)\"\n"+
"  ZdvihovyObjem   =\"optional int(0,99_999); onTrue setNullIfZero();\n"+
"                     onFalse setErr(4208)\"\n"+
"  VykonMotoru     =\"optional int(0,9_999);  onTrue setNullIfZero();\n"+
"                     onFalse setErr(4208)\"\n"+
"  CisloMotoru     =\"optional string(1,26); onFalse setErr(4208)\"\n"+
"  CelkovaHmotnost =\"optional int(1,999_999); onFalse setErr(4208)\"\n"+
"  PocetMistCelkem =\"optional int(0,999); onTrue setNullIfZero();\n"+
"                     onFalse setErr(4208)\"/>\n"+
"</x:def>";
			xdef2 =
"<x:def xmlns:x='" + _xdNS + "' name='test' root='a'>\n"+
"<a>\n"+
"<Vozidlo x:script='occurs 0..1; ref P1_common#Vozidlo'\n"+
"   KodModeluVozidla=\"optional tab('CC_ModelVozidla','KodModeluVozidla');\n"+
"                     onFalse setErr(4225)\" />\n"+
"</a>\n"+
"</x:def>";
			xdef3 =
"<x:def xmlns:x='" + _xdNS + "' name='test1' root='a'>\n"+
"<a>\n"+
"  <Vozidlo x:script='occurs 0..1; ref P1_common#Vozidlo'\n"+
"    DruhVozidla=\"optional fil0(2) AND tab(\n"+
"                  'CC_DruhVozidla','KodDruhuVozidla');\n"+
"                  onFalse setErr(4225)\"\n"+
"    KodModeluVozidla=\"optional tab('CC_ModelVozidla','KodModeluVozidla');\n"+
"                      onFalse setErr(4225)\" />\n"+
"</a>\n"+
"</x:def>\n";
			String[] xdefs = new String[]{xdef1, xdef2, xdef3};
			data = "<a><Vozidlo SPZ=\"6A84013\" /></a>";
			_errorCount = 0;
			_errorCode = 0;
			//should be reported 4202
			if (test(xdefs, data, "test",'P',
				"<a><Vozidlo SPZ=\"6A84013\" /></a>", "", getClass())) {
				fail();
			}
			if (_errorCount != 1) {
				fail("Errors: " + _errorCount);
			}
			if (_errorCode != 4202) {
				fail("ErrorCode: " + _errorCode);
			}
			data = "<a><Vozidlo SPZ=\"6A84013\" /></a>";
			_errorCount = 0;
			_errorCode = 0;
			if (test(xdefs, data, "test1",'P',
				"<a><Vozidlo SPZ=\"6A84013\" /></a>", "", getClass())) {
				fail();
			}
			if (_errorCount != 0) {
				fail("Errors: " + _errorCount);
			}
			if (_errorCode != 0) {
				fail("ErrorCode: " + _errorCode);
			}
			data = "<a><Vozidlo SPZ=\"6A84013\" /></a>";
			_errorCount = 0;
			_errorCode = 0;
			if (test(xdefs, data, "test1",'P',
				"<a><Vozidlo SPZ=\"6A84013\" /></a>", "", getClass())) {
				fail();
			}
			if (_errorCount != 0) {
				fail("Errors: " + _errorCount);
			}
			if (_errorCode != 0) {
				fail("ErrorCode: " + _errorCode);
			}
			/////////// now we check files and input stream
			File[] files =
				SUtils.getFileGroup(dataDir + "test002_2_*.xdef");
			InputStream[] xdefs1 = new InputStream[files.length];
			for (int i = 0; i < xdefs1.length; i++) {
				try {
					xdefs1[i] = new FileInputStream(files[i]);
				} catch (Exception ex) {
					fail(ex);
					return;
				}
			}
			InputStream data1 = new FileInputStream(dataDir + "test002_2.xml");
			_errorCount = 0;
			_errorCode = 0;
			//should be reported 4202
			test(xdefs1, data1, "SouborP1A", null, reporter, 'P', getClass());
			if (_errorCount != 1 || _errorCode != 4202) {
				fail("ErrorCount:"+_errorCount+", errorCode:"+_errorCode);
			}
			_errorCount = 0;
			_errorCode = 0;
			xp = compile(dataDir + "test002_2a_1.xdef", getClass());
			parse(xp, "SouborP1A", dataDir + "test002_2.xml", reporter);
			//should be reported 4202
			if (_errorCount != 1 || _errorCode != 4202) {
				fail("ErrorCount:"+_errorCount+", errorCode:"+_errorCode);
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a1=\"optional unknown() OOR pic('AA999999'); onFalse out('ERR1')\"\n"+
"   a2=\"optional NOT known()||pic('AA999999'); onFalse out('ERR2')\"/>\n"+
"</xd:def>\n";
			xml = "<a a1='???' a2='???'/>";
			assertFalse(test(xdef, xml, "",'P', xml, "", getClass()));
			xml = "<a a1='AA999991' a2='AA999992'/>";
			assertFalse(test(xdef, xml, "",'P', xml, "", getClass()));
			xdef = dataDir + "Test002_6.xdef"; //test macros, errors etc.
			xml = dataDir + "Test002_6.xml";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, "LDN", xml, reporter, strw, null, null));
			assertEq(strw.toString(),
				"XDOUT: ISDN_LDN - ISDN4202/DnSearch/@KodNdnDuvodLustrace\n");
			s = reporter.printToString("slk");
			assertTrue(s.indexOf("ISDN4202:") > 0 &&
//				s.indexOf("XDEF526:") > 0 && s.indexOf("riadok") > 0, s);
				s.indexOf("XDEF526:") > 0, s);
			xdef = // external methods
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A xd:script=\"finally {out(tab('a', 'b')); pp()}\" />\n"+
"<xd:declaration>\n"+
"  external method boolean test.xdef.Test002.tab(String, String);\n"+
"  void pp() {\n"+
"    out(',' + tab('a', 'b') + ',' + test.xdef.Test002.tab('a', 'b'));\n"+
"  }\n"+
"</xd:declaration>\n"+
"</xd:def>\n";
			strw = new StringWriter();
			assertEq("<A/>", parse(xdef,"","<A/>",reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "true,true,true");
			//force compilation of test.xdef.TestExtenalMethods_1;
			test.xdef.TestExtenalMethods_1.class.getClass();
			//force compilation of test.xdef.Test002;
			test.xdef.Test002.class.getClass();
			xdef = // external methods in global declaration section
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration scope='global'>\n"+
"  external method {\n"+
"     void test.xdef.TestExtenalMethods_1.m00() as x;\n"+
"     void test.xdef.TestExtenalMethods_1.m00(long) as x;\n"+
"     boolean test.xdef.Test002.tab(String, String) as x;\n"+
"  }\n"+
"  void pp() {\n"+
"    x(); x(1);\n"+
"    out(x('a','b')+','+ x('a','b')+','+ test.xdef.Test002.tab('a','b'));\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a xd:script=\"finally {x(); x(2); x('a','b'); pp()}\" />\n"+
"</xd:def>\n";
			strw = new StringWriter();
			assertEq("<a/>", parse(xdef,"","<a/>",reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "true,true,true");
			xdef = // external methods in local declaration section
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration scope='local'>\n"+
"  external method {\n"+
"     void test.xdef.TestExtenalMethods_1.m00() as x;\n"+
"     void test.xdef.TestExtenalMethods_1.m00(long) as x;\n"+
"     boolean test.xdef.Test002.tab(String, String) as x;\n"+
"  }\n"+
"  void pp() {\n"+
"    x(); x(1);\n"+
"    out(x('a','b')+','+x('a','b')+','+ test.xdef.Test002.tab('a','b'));\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a xd:script=\"finally {x(); x(2); x('a','b'); pp()}\" />\n"+
"</xd:def>\n";
			strw = new StringWriter();
			assertEq("<a/>", parse(xdef,"","<a/>",reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "true,true,true");
			//xpos
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root>\n"+
"  <A b='required xdatetime(\"dd.MM.yyyy\")'/>\n"+
"  <A b='required xdatetime(\"yyyy\")' />\n"+
"  <A b='required xdatetime(\"yyyy.MM.dd\")' />\n"+
"</root>\n"+
"</xd:def>";
			xml =
"<root>\n"+
"  <A b='12.02.2011' />\n"+
"  <A b='12.02.2011' />\n"+
"  <A b='12.02.2011' />\n"+
"</root>";
			parse(xdef, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			assertEq(reporter.size(), 2);
			if(reporter.size() == 2) {
				String xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/A[2]/@b");
				xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/A[3]/@b");
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root>\n"+
"  <B>" +
"    <A b='required xdatetime(\"yyyy.MM.dd\")' />\n"+
"  </B>" +
"  <B>" +
"    <A b='required xdatetime(\"yyyy.MM.dd\")' />\n"+
"  </B>" +
"</root>\n"+
"</xd:def>";
			xml =
"<root>\n"+
"  <B><A b='12.02.2011' /></B>\n"+
"  <B><A b='12.02.2011' /></B>\n"+
"</root>";
			parse(xdef, "", xml, reporter);
			assertTrue(reporter.errorWarnings());
			assertEq(reporter.size(), 2);
			if(reporter.size() == 2) {
				String xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/B[1]/A[1]/@b");
				xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/B[2]/A[1]/@b");
			}
			//create mode
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root>\n"+
"  <A b='required xdatetime(\"dd.MM.yyyy\"); create \"12.02.2011\" '/>\n"+
"  <A b='required xdatetime(\"yyyy\"); create \"12.02.2011\"' />\n"+
"  <A b='required xdatetime(\"yyyy.MM.dd\"); create \"12.02.2011\"' />\n"+
"</root>\n"+
"</xd:def>";
			create(xdef, "", "root", reporter, null);
			assertTrue(reporter.errorWarnings());
			assertEq(reporter.size(), 2);
			if(reporter.size() == 2) {
				String xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/A[2]/@b");
				xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/A[3]/@b");
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root>\n"+
"  <B>" +
"    <A b='required xdatetime(\"yyyy.MM.dd\"); create \"12.02.2011\"' />\n"+
"  </B>" +
"  <B>" +
"    <A b='required xdatetime(\"yyyy.MM.dd\"); create \"12.02.2011\"' />\n"+
"  </B>" +
"</root>\n"+
"</xd:def>";
			create(xdef, "", "root", reporter, null);
			assertTrue(reporter.errorWarnings());
			assertEq(reporter.size(), 2);
			if(reporter.size() == 2) {
				String xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/B[1]/A[1]/@b");
				xpath = reporter.getReport().getParameter("xpath");
				assertEq(xpath, "/root/B[2]/A[1]/@b");
			}
			xdef = //errors of missing external methods
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <A i=\"int; onTrue oti(@i); onFalse ofi(1, '1', @i)\"\n"+
"    xd:script=\"onAbsence oaa(); onExcess oea()\"/>\n"+
"  <xd:declaration>\n"+
"    void oaa() {\n"+
"      Container c = [];\n"+
"      Element e = new Element('B');"+
"      ext(c, e);\n"+
"    }\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			compile(xdef);
			fail("Error not reported");
		} catch (Exception ex) {
			if ((s = ex.getMessage()) == null) {
				fail(ex);
			} else {
				// parts of error messages that should be in the message
				assertTrue(s.indexOf("oti(String)")>=0, ex.getMessage());
				assertTrue(s.indexOf("ofi(int,String,String)")>=0);
				assertTrue(s.indexOf("ext(Container,Element)")>=0);
				assertTrue(s.indexOf("oea()")>=0);
			}
		}
		try {// XDef compilator error: Ambiguous
			xml = "<a><A/><A/><A/><A/></a>";
			assertEq(xml, parse( // this is OK
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <A xd:script='2..2'/>\n"+
"  <A/>\n"+
"  <A/>\n"+
"</a>\n"+
"</xd:def>", "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			XDBuilder xb = XDFactory.getXDBuilder(null);
			xb.setSource(
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='root'>\n"+
"<root xd:script='init i = 1; finally out(i);'>\n"+
"  <a xd:script='1..2; ref b#A; finally {out(j); j = i; i = 2;}'>\n"+
"  </a>\n"+
"</root>\n"+
"</xd:def>");
			xb.setSource(
"<xd:def xmlns:xd='" + _xdNS + "' name='b'>\n"+
"  <A xd:script='init j = 3;'/>\n"+
"<xd:declaration>\n"+
"  int i;\n"+
"  int j;\n"+
"</xd:declaration>\n"+
"</xd:def>");
			xp = checkExtObjects(xb.compileXD());
			strw = new StringWriter();
			xml = "<root><a/><a/></root>";
			assertEq(xml, parse(xp, "a", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("332", strw.toString());
			xdef = //check declaration sequence independence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int j = k; </xd:declaration>\n"+
"  <xd:declaration>\n"+
"    int k = 1; int y = x, i = 0;\n"+
"    void h() {if (i != 0 || k != 1 || x != 1 ||  y != 1) outln('error');}\n"+
"  </xd:declaration>\n"+
"  <a xd:script = \"finally {h(); if (k != 1) outln('error');}\"/>\n"+
"  <xd:declaration> int x = 1; </xd:declaration>\n"+
"</xd:def>";
			strw = new StringWriter();
			xml = "<a/>";
			assertEq(xml, parse(xdef, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='a'>\n"+
"  <xd:declaration> int j = k; </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name='b' root='a'>\n"+
"  <a xd:script = \"finally {h(); if (k != 1) outln('error');}\"/>\n"+
"  <xd:declaration>\n"+
"    int k = 1, y = x, i = 0;\n"+
"    void h() {if (i != 0 || k != 1 || x != 1 ||  y != 1) outln('error');}\n"+
"  </xd:declaration>\n"+
"  <xd:declaration> int x = 1; </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			strw = new StringWriter();
			xml = "<a/>";
			assertEq(xml, parse(xdef, "b", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			// fixed
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root ='a'>\n"+
"  <a a=\"fixed 'abc'\" />\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a a='123'/>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF515") > 0, s);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root ='a'>\n"+
"  <a a='fixed xx;'/>\n"+
"  <xd:declaration>\n"+
"    String xx = 'abc';\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a a='123'/>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF515") > 0, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='fixed xx;'/>\n"+
"  <xd:declaration>\n"+
"    String xx = 'abc';\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a a='123'/>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF515") > 0, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root ='a'>\n"+
"  <a a=\"fixed {return 'abc';}\" />\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a a='abc'/>", parse(xp, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a a='abc'/>", parse(xp, "", "<a a='123'/>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF515") > 0, s);
			// test expression in validation section
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='float OR int OR boolean'></a>"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='3.14'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='3'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='true'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='X'/>", reporter);
			assertTrue(reporter.errorWarnings()); //E XDEF515: Chybná hodnota
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='float OOR int OOR boolean'></a>"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='3.14'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='3'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='true'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='X'/>", reporter);
			assertTrue(reporter.errorWarnings()); //E XDEF515: Chybná hodnota
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='float OR int OR boolean'></a>"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='3.14'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='3'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='true'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='X'/>", reporter);
			assertTrue(reporter.errorWarnings()); //E XDEF515: Chybná hodnota
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='float OOR int OOR boolean'></a>"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='3.14'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='3'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='true'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='X'/>", reporter);
			assertTrue(reporter.errorWarnings()); //E XDEF515: Chybná hodnota
			// test getXPos
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a x = 'string; finally out(getXPos())'>\n"+
"  <b y = 'string; finally out(getXPos())'>\n"+
"    string; finally out(getXPos())\n"+
"  </b>\n"+
"</a>\n"+
"</x:def>";
			xp = compile(xdef, getClass());
			xml = "<a x='a'><b y='b'>c</b></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "/a/b[1]/text()/a/b[1]/@y/a/@x");
			strw = new StringWriter();
			assertEq(xml, create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "/a/b[1]/text()/a/b[1]/@y/a/@x");
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a x = 'string; finally out(testGetXPos())'>\n"+
"  <b y = 'string; finally out(testGetXPos())'>\n"+
"    string; finally out(testGetXPos())\n"+
"  </b>\n"+
"</a>\n"+
"</x:def>";
			xp = compile(xdef, getClass());
			xml = "<a x='a'><b y='b'>c</b></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "/a/b[1]/text()/a/b[1]/@y/a/@x");
			xdef = // Create document from model of element.
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <b><a x='required'>required;</a></b>\n"+
"</xd:def>";
			xp = compile(xdef);
			XMDefinition xmd = xp.getXMDefinition();
			XMElement xmel =
				(XMElement) xmd.getModel("", "b").getChildNodeModels()[0];
			xd = xmel.createXDDocument();
			xml = "<a x='x'>x</a>";
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='xxx'>xxx</a>";
			xd = xp.createXDDocument();
			xd.setRootModel(xmel);
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			xdef = // test MD5
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a x='MD5'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='e4d909c290d0fb1ca068ffaddf22cbd0'/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='   e4d909c290d0fb1ca068ffaddf22cbd0   '/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a x='e4d909c290d0fb1c a068ffaddf22cbd0'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a x='e4d909c290d0fb1ca 068ffaddf22cbd0'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a x='e4d909c290d0fb1ca068ffaddf22cbd'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a x='e4d909c290d0fb1c a068ffaddf22cbd'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a x='e4d909c290d0fb1ca068ffaddf22cb'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a x=''/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xdef = //test hex
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a><b xd:script='+' a=\"hex()\" /></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b a='0' /></a>";
			el = parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, "<a><b a='0'/></a>");
			xml = "<a><b a='01'/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a='0 1'/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a='012'/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a='01 2'/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a='0 12'/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a=' 0 1 2 '/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a=' a B c '/></a>";
			parse(xp, null, xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><b a='XX'/></a>";
			parse(xp, null, xml, reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef = // test XMPool.findModel
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a|b#b'>\n"+
"<a a='int'>\n"+
"  int\n"+
"  <b b='int'>int<c/>int<d/><c/></b>\n"+
"  int\n"+
"  <b c='int'><c a='int'/>float</b>\n"+
"</a>\n"+
"</xd:def>\n"+
"<xd:def name='b'>\n"+
"  <b c='int'>int<c/>int<d/><c/></b>\n"+
"</xd:def>\n"+
"<xd:def name='c'>\n"+
"  <b c='int'>\n"+
"    <b>string</b>\n"+
"    <b xd:script='match @a' a='?'/>\n"+
"    <xd:mixed>\n"+
"      <b xd:script='match @b' b='?'/>\n"+
"      <b>string</b>\n"+
"      <c>\n"+
"        <b xd:script='match @c' c='?'/>\n"+
"        <xd:mixed>\n"+
"          <b xd:script='match @d' d='?'/>\n"+
"          <b/>\n"+
"          <c/>\n"+
"          <xd:choice>\n"+
"            <d/>\n"+
"            <e/>\n"+
"          </xd:choice>\n"+
"        </xd:mixed>\n"+
"      </c>\n"+
"    </xd:mixed>\n"+
"    <b xd:script='match @e' e='?'/>\n"+
"    <xd:mixed>\n"+
"      <b xd:script='match @f' f='?'/>\n"+
"      <b>string</b>\n"+
"      <d/>\n"+
"    </xd:mixed>\n"+
"    <b xd:script='match @g' g='?'/>\n"+
"  </b>\n"+
"</xd:def>\n"+
"<xd:def name='d'>\n"+
"  <xd:mixed name='dd'>\n"+
"    <b xd:script='match @f' f='?'/>\n"+
"    <b>string</b>\n"+
"    <d/>\n"+
"  </xd:mixed>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			assertEq("#",xp.findModel("#").getXDPosition());
			assertEq("#",xp.findModel("").getXDPosition());
			assertEq("b#",xp.findModel("b#").getXDPosition());
			assertEq("b#",xp.findModel("b").getXDPosition());
			assertEq("c#",xp.findModel("c#").getXDPosition());
			assertEq("#a",xp.findModel("#a").getXDPosition());
			assertEq("#a",xp.findModel("#/a").getXDPosition());
			assertEq("#a/$text",xp.findModel("#/a/$text").getXDPosition());
			assertEq("#a/$text",xp.findModel("#/a/$text[1]").getXDPosition());
			assertEq("#a/$text[2]",
				xp.findModel("#/a/$text[2]").getXDPosition());
			assertEq("#a/b",xp.findModel("#/a/b").getXDPosition());
			assertEq("#a/b",xp.findModel("#/a/b[1]").getXDPosition());
			assertEq("#a/b/@b",xp.findModel("#/a/b/@b").getXDPosition());
			assertEq("#a/b/@b",xp.findModel("#/a/b[1]/@b").getXDPosition());
			assertEq("#a/b[2]/@c",xp.findModel("#/a/b[2]/@c").getXDPosition());
			assertEq("#a/b/c",xp.findModel("#/a/b/c").getXDPosition());
			assertEq("#a/b/c",xp.findModel("#/a[1]/b[1]/c[1]").getXDPosition());
			assertEq("#a/b/c[2]",xp.findModel("#/a[1]/b/c[2]").getXDPosition());
			assertEq("#a/b/$text",xp.findModel("#a/b/$text").getXDPosition());
			assertEq("#a/b/$text",
				xp.findModel("#a/b/$text[1]").getXDPosition());
			assertEq("#a/b/$text[2]",
				xp.findModel("#a/b/$text[2]").getXDPosition());
			assertEq("b#b", xp.findModel("b#/b").getXDPosition());
			assertEq("b#b/@c", xp.findModel("b#b/@c").getXDPosition());
			assertEq("b#b/c", xp.findModel("b#/b/c").getXDPosition());
			assertEq("b#b/c", xp.findModel("b#/b[1]/c[1]").getXDPosition());
			assertEq("b#b/c[2]", xp.findModel("b#b/c[2]").getXDPosition());
			assertEq("b#b/$text", xp.findModel("b#/b/$text").getXDPosition());
			assertEq("c#b/b", xp.findModel("c#b/b").getXDPosition());
			assertEq("c#b/b", xp.findModel("c#b/b").getXDPosition());
			assertEq("c#b/b[2]", xp.findModel("c#b/b[2]").getXDPosition());
			assertEq("c#b/b[3]", xp.findModel("c#b/b[3]").getXDPosition());
			assertEq("c#b/b[4]", xp.findModel("c#b/b[4]").getXDPosition());
			assertEq("c#b/$mixed/c",
				xp.findModel("c#b/$mixed/c").getXDPosition());
			assertEq("c#b/$mixed/c/$mixed/$choice/d",
				xp.findModel("c#b/$mixed/c/$mixed/$choice/d").getXDPosition());
			assertEq("c#b/$mixed/b",
				xp.findModel("c#b/$mixed/b").getXDPosition());
			assertEq("c#b/$mixed/b[2]",
				xp.findModel("c#b/$mixed/b[2]").getXDPosition());
			assertEq("c#b/b[3]", xp.findModel("c#b/b[3]").getXDPosition());
			assertEq("c#b/b[4]", xp.findModel("c#b/b[4]").getXDPosition());
			assertEq("c#b/$mixed", xp.findModel("c#b/$mixed").getXDPosition());
			assertEq("c#b/$mixed[2]",
				xp.findModel("c#b/$mixed[2]").getXDPosition());
			assertEq("c#b/$mixed[2]/b",
				xp.findModel("c#b/$mixed[2]/b").getXDPosition());
			assertEq("c#b/$mixed[2]/b[2]",
				xp.findModel("c#b/$mixed[2]/b[2]").getXDPosition());
			assertEq("c#b/$mixed[2]/d",
				xp.findModel("c#b/$mixed[2]/d").getXDPosition());
			assertEq("c#b/b/$text",
				xp.findModel("c#/b/b[1]/$text").getXDPosition());
			assertEq("c#b/b/$text",
				xp.findModel("c#b/b/$text").getXDPosition());
			assertEq("c#b/b[2]/@a",xp.findModel("c#b/b[2]/@a").getXDPosition());
			assertEq("d#dd$mixed", xp.findModel("d#dd").getXDPosition());
//			assertEq("d#dd$mixed/$mixed",
//				xp.findModel("d#dd/$mixed").getXDPosition());
			assertEq("d#dd$mixed/$mixed/b[2]",
				xp.findModel("d#dd$mixed/$mixed/b[2]").getXDPosition());
			assertEq("d#dd$mixed/$mixed/b[2]",
				xp.findModel("d#dd/b[2]").getXDPosition());
			/////////////////
			xp = compile(dataDir + "TestXComponent_Z.xdef");
			assertEq("SouborD1A#ZaznamPDN/$mixed/Protokol",
				xp.findModel("SouborD1A#ZaznamPDN/$mixed/Protokol")
					.getXDPosition());
			assertEq("SouborD1A#ZaznamPDN/$mixed/Protokol",
				xp.findModel("SouborD1A#SouborD1A/ZaznamPDN/$mixed/Protokol")
					.getXDPosition());
			assertEq("SouborD1A#ProtokolDN/@KodUtvaruPolicie",
				xp.findModel(
					"SouborD1A#ProtokolDN/@KodUtvaruPolicie").getXDPosition());
			assertEq("SouborD1A#ProtokolDN/@KodUtvaruPolicie",
				xp.findModel(
			"SouborD1A#SouborD1A/ZaznamPDN/$mixed/Protokol/@KodUtvaruPolicie")
					.getXDPosition());
			xdef = //test xpointer to xd:any, mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a | b | m/n | x'>\n"+
"   <xd:any xd:name='x' b='int()' />\n"+
"   <xd:mixed xd:name='m'>\n"+
"     <n/>\n"+
"     <o/>\n"+
"   </xd:mixed>\n"+
"   <a>\n"+
"     <xd:mixed xd:script='ref m' />\n"+
"   </a>\n"+
"   <b>\n"+
"     <xd:any xd:script='ref x' />\n"+
"   </b>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><o/><n/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<x b = '123'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b><q b = '123'/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b><q b = 'aaa'/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter); // b has incorrect value (int expected)
			xml = "<n/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script=\"finally out(from('b').getLength());\"><b xd:script='?'/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<a><b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("1", strw.toString());
			strw = new StringWriter();
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("0", strw.toString());
			xdef = // should not throw null pointer exception!
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> Element $e = null; </xd:declaration>\n"+
"  <a b=\"optional string; create from($e,'@b')\"/>\n"+
"</xd:def>";
			assertEq("<a/>", create(compile(xdef), null, reporter, "<a/>"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"  xmlns:wsdl='http://www' root='wsdl:portType'>\n"+
"<wsdl:portType name =\"required string()\">\n"+
"  <wsdl:operation xd:script=\"occurs 1..;\"\n"+
"    name=\"required string()\">\n"+
"    <xd:choice>\n"+
"	   <xd:sequence>\n"+
"        <wsdl:output name=\"optional NCName()\"\n"+
"          message=\"required string()\"/>\n"+
"        <wsdl:input xd:script=\"occurs 0..1\"\n"+
"          name=\"optional NCName()\"\n"+
"          message=\"required string()\"/>\n"+
"        <wsdl:fault xd:script=\"occurs 0..\"\n"+
"          name=\"required string()\"\n"+
"          message=\"required string()\"/>\n"+
"      </xd:sequence>\n"+
"      <xd:sequence>\n"+
"        <wsdl:input name=\"optional NCName()\"\n"+
"          message=\"required  string()\"/>\n"+
"        <wsdl:output xd:script=\"occurs 0..1\"\n"+
"          name=\"optional NCName()\"\n"+
"          message=\"required  string()\"/>\n"+
"        <wsdl:fault xd:script=\"occurs 0..\"\n"+
"          name=\"required string()\"\n"+
"          message=\"required string()\"/>\n"+
"      </xd:sequence>\n"+
"    </xd:choice>\n"+
"  </wsdl:operation>\n"+
"</wsdl:portType>\n"+
"</xd:def>";
			xml =
"<wsd:portType xmlns:wsd=\"http://www\" name=\"WlePt\">\n"+
"  <wsd:operation name=\"WLTaskInsertOp\">\n"+
"    <wsd:input name=\"WLTaskInsert\"\n"+
"      message=\"tns:WLTaskInsertMsg\"/>\n"+
"    <wsd:output name=\"WLTaskInsertResponse\"\n"+
"      message=\"tns:WLTaskInsertResponseMsg\"/>\n"+
"  </wsd:operation>\n"+
"  <wsd:operation name=\"WLTaskDoneOp\">\n"+
"    <wsd:input name=\"WLTaskDone\"\n"+
"      message=\"tns:WLTaskDoneMsg\"/>\n"+
"    <wsd:output name=\"WLTaskDoneResponse\"\n"+
"      message=\"tns:WLTaskDoneResponseMsg\"/>\n"+
"  </wsd:operation>\n"+
"  <wsd:operation name=\"WLTaskCancelOp\">\n"+
"    <wsd:input name=\"WLTaskCancel\"\n"+
"      message=\"tns:WLTaskCancelMsg\"/>\n"+
"    <wsd:output name=\"WLWLTaskCancelResponse\"\n"+
"      message=\"tns:WLTaskCancelResponseMsg\"/>\n"+
"  </wsd:operation>\n"+
"  <wsd:operation name=\"WLTaskEndOp\">\n"+
"    <wsd:output name=\"WLTaskEndResponse\"\n"+
"      message=\"tns:WLTaskEndResponseMsg\"/>\n"+
"    <wsd:input name=\"WLTaskEnd\"\n"+
"      message=\"tns:WLTaskEndMsg\"/>\n"+
"  </wsd:operation>\n"+
"</wsd:portType>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			xml =
"<wsdl:portType xmlns:wsdl=\"http://www\" name=\"WlePt\">\n"+
"  <wsdl:operation name=\"WLTaskInsertOp\">\n"+
"    <wsdl:input name=\"WLTaskInsert\"\n"+
"      message=\"tns:WLTaskInsertMsg\"/>\n"+
"    <wsdl:output name=\"WLTaskInsertResponse\"\n"+
"      message=\"tns:WLTaskInsertResponseMsg\"/>\n"+
"  </wsdl:operation>\n"+
"  <wsdl:operation name=\"WLTaskDoneOp\">\n"+
"    <wsdl:input name=\"WLTaskDone\"\n"+
"      message=\"tns:WLTaskDoneMsg\"/>\n"+
"    <wsdl:output name=\"WLTaskDoneResponse\"\n"+
"      message=\"tns:WLTaskDoneResponseMsg\"/>\n"+
"  </wsdl:operation>\n"+
"  <wsdl:operation name=\"WLTaskCancelOp\">\n"+
"    <wsdl:input name=\"WLTaskCancel\"\n"+
"      message=\"tns:WLTaskCancelMsg\"/>\n"+
"    <wsdl:output name=\"WLWLTaskCancelResponse\"\n"+
"      message=\"tns:WLTaskCancelResponseMsg\"/>\n"+
"  </wsdl:operation>\n"+
"  <wsdl:operation name=\"WLTaskEndOp\">\n"+
"    <wsdl:output name=\"WLTaskEndResponse\"\n"+
"      message=\"tns:WLTaskEndResponseMsg\"/>\n"+
"    <wsdl:input name=\"WLTaskEnd\"\n"+
"      message=\"tns:WLTaskEndMsg\"/>\n"+
"  </wsdl:operation>\n"+
"  <!-- here should be error -->\n"+
"  <wsdl:operation name=\"WLTaskEndOp\" />\n"+
"</wsdl:portType>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertEq("", strw.toString());
			if (!reporter.errors()) {
				fail("error not reported");
			} else {
				if (reporter.getErrorCount() != 1) {
					strw = new StringWriter();
					ReportPrinter.printListing(strw, xml, reporter, true);
					fail(strw.toString());
				} else {
					rep = reporter.getReport();
					if (rep.getModification().indexOf(
						"/wsdl:portType/wsdl:operation[5]") < 0) {
						//Missing required element in a section in &{xpath}
						fail(rep.toString() +
							"\n"+ rep.getModification());
					}
				}
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
" <root>\n"+
"   <xd:choice occurs = \"?\">\n"+
"     <xd:sequence>\n"+
"       <b/>\n"+
"       <c/>\n"+
"       <d xd:script = \"occurs 0..1\"/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <b/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <c/>\n"+
"       <b/>\n"+
"       <d xd:script = \"occurs 0..1\"/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <c/>\n"+
"     </xd:sequence>\n"+
"   </xd:choice>\n"+
" </root>\n"+
"</xd:def>";
			xml ="<root/>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertEq("", strw.toString());
			if (reporter.errors()) {
				while ((rep = reporter.getReport()) != null) {
					fail(rep.toString() + "\n"+ rep.getModification());
				}
				reporter.reset();
				strw = new StringWriter();
				ReportPrinter.printListing(strw, xml, reporter, true);
				fail(strw.toString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a>\n"+
"   <xd:choice occurs = '?'>\n"+
"     <xd:sequence>\n"+
"       <b/>\n"+
"       <c/>\n"+
"       <d xd:script = \"occurs ?\"/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <b/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <c/>\n"+
"       <b/>\n"+
"       <d xd:script = \"occurs ?\"/>\n"+
"     </xd:sequence>\n"+
"     <xd:sequence>\n"+
"       <c/>\n"+
"     </xd:sequence>\n"+
"   </xd:choice>\n"+
" </a>\n"+
"</xd:def>";
			xml ="<a><c/><b/><d/></a>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertEq("", strw.toString());
			assertNoErrors(reporter);
			xml = "<a><c/><b/><xxx/></a>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertEq("", strw.toString());
			if (!reporter.errors()) {
				fail("error not reported");
			} else {
				if (!"XDEF501".equals(
					(rep = reporter.getReport()).getMsgID())) {
					fail(rep.toString() + "\n"+ rep.getModification());
				}
				while ((rep = reporter.getReport()) != null) {
					fail(rep.toString() + "\n"+ rep.getModification());
				}
			}
			xml = "<a><c/></a>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw);
			assertEq("", strw.toString());
			assertNoErrors(reporter);
			if (reporter.errors()) {
				while ((rep = reporter.getReport()) != null) {
					fail(rep.toString() + "\n"+ rep.getModification());
				}
				reporter.reset();
				strw = new StringWriter();
				ReportPrinter.printListing(strw, xml, reporter, true);
				fail(strw.toString());
			}
			xdef = // test of reentrance (variable ids)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Container ids=[];\n"+
"  void code(String result) {\n"+
"    ids.addItem(result);\n"+
"  }\n"+
"</xd:declaration>\n"+
"<a>\n"+
"  string; onTrue code(getText())\n"+
"  <b/>\n"+
"  string; onTrue code(getText())\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			assertNull(((XDContainer) xd.getVariable("ids")));
			xml = "<a>1<b/>2</a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertEq(2, ((XDContainer) xd.getVariable("ids")).getXDItemsNumber());
			assertEq("1",
				((XDContainer) xd.getVariable("ids")).getXDItem(0).toString());
			xd = xp.createXDDocument();
			assertNull(((XDContainer) xd.getVariable("ids")));
			xml = "<a>3<b/>4</a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertEq(2, ((XDContainer) xd.getVariable("ids")).getXDItemsNumber());
			assertEq("3",
				((XDContainer) xd.getVariable("ids")).getXDItem(0).toString());
			xdef = // test onAbsence
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A a=\"? string; onAbsence out('A')\">\n"+
"  <B xd:script=\"occurs +; onAbsence out('B')\"/>\n"+
"  <C xd:script=\"occurs *; onAbsence out('C')\"/>\n"+ //onAbsence called!!!
"  ? string; onAbsence out('D')\n"+  //onAbsence called!!!
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, null, "<A/>", reporter, strw, null, null);
			assertEq(strw.toString(), "ABCD");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A>\n"+
"  <xd:choice script=\"onAbsence out('B')\"><B/><C/></xd:choice>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, null, "<A/>", reporter, strw, null, null);
			assertEq(strw.toString(), "B");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A>\n"+
"  <xd:choice script=\"?; onAbsence out('B')\"><B/><C/></xd:choice>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, null, "<A/>", reporter, strw, null, null);
			assertEq(strw.toString(), "B");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A>\n"+
"  <xd:mixed script=\"onAbsence out('B')\"><B/><C/></xd:mixed>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, null, "<A/>", reporter, strw, null, null);
			assertEq(strw.toString(), "B");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A>\n"+
"  <xd:mixed script=\"?; onAbsence out('B')\"><B/><C/></xd:mixed>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, null, "<A/>", reporter, strw, null, null);
			assertEq(strw.toString(), "B");
			assertNoErrors(reporter);
			xdef = //variable x with leading "$"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String $x = 'xyz'; </xd:declaration>\n"+
"  <a xd:script = \"create from('b[@a=$x]')\">string</a>\n"+
"</xd:def>";
			xml = "<w><b a='x'/><b a='xyz'>zxy</b><b>xx</b></w>";
			assertEq("<a>zxy</a>", create(xdef, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef = //integer variable x without leading "$"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int x = 123; </xd:declaration>\n"+
"  <a xd:script = \"create from('*[@a=\\\''+x+'\\\']')\"> string </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<w><b a='x'/><b a='123'>zxy</b><b>xx</b></w>";
			assertEq("<a>zxy</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xp = compile( // test absence and default
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a a='int(); onAbsence setText(\"1\");'\n"+
"      b='int(); default 2;'\n"+
"      c='int(); onAbsence setText(\"0.1\");'\n"+
"      d='int(); default 0.2;'>\n"+
"      int(); onAbsence setText('3');\n"+
"      <b/>\n"+
"      int(); default 4;\n"+
"      <c/>\n"+
"      int(); onAbsence setText('0.3');\n"+
"      <d/>\n"+
"      int(); default 0.4;\n"+
"   </a>\n"+
"</xd:def>");
			xml = "<a>\n<b/>\n<c/>\n<d/>\n</a>";
			assertEq("<a a='1' b='2' c='0.1' d='0.2'>3<b/>4<c/>0.3<d/>0.4</a>",
				parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 4
				&& s.contains("E XDEF804:")	&& s.contains("=/a/@c")
				&& s.contains("=/a/@d")	&& s.contains("=/a/text()[3]")
				&& s.contains("=/a/text()[4]"), s);
			xdef =
"<xd:collection xmlns:xd=\"http://www.xdef.org/xdef/3.2\">\n" +
"  <xd:def xd:root=\"A\">\n" +
"    <A xd:script=\"ref RegistraceSU_RegistraceSU_cType\"/>\n" +
"    <RegistraceSU_RegistraceSU_cType\n"+
"       xd:script=\"ref L1_common_SU_cType\"/>\n" +
"    <L1_common_Misto_cType xd:text=\"? string()\">\n" +
"      <GPS xd:script=\"ref L1_common_GPS_cType; occurs ?\"/>\n" +
"      <Adresa xd:script=\"ref L1_common_Adresa_cType; occurs ?\"/>\n" +
"      <Vozovka xd:script=\"ref L1_common_Vozovka_cType; occurs ?\"/>\n" +
"    </L1_common_Misto_cType>\n" +
"    <L1_common_SU_cType>\n" +
"      <Misto xd:script=\"ref L1_common_Misto_cType\"/>\n" +
"    </L1_common_SU_cType>\n" +
"    <L1_common_Adresa_cType CisloDomu=\"optional string(1, 10)\"\n" +
"      Obec=\"optional string(1, 36)\"\n" +
"      Okres=\"optional string(1, 36)\"\n" +
"      PSC=\"optional string(1, 16)\"\n" +
"      Stat=\"required string()\"\n" +
"      Ulice=\"optional string(1, 36)\"/>\n" +
"    <L1_common_Vozovka_cType/>\n" +
"    <L1_common_GPS_cType/>\n" +
"  </xd:def>\n" +
"</xd:collection>";
			xd = compile(xdef).createXDDocument();
//"Testovací data OK\n" +
			xml =
"<A>\n" +
"    <Misto>\n" +
"      awdad\n" + // here
"      <Adresa Okres=\"PRAHA 9\" Stat=\"CZ\"/>\n" +
"    </Misto>\n" +
"</A>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
//"Testovací data chyba\n" +
			xd = XDFactory.compileXD(null, xdef).createXDDocument();
			xml =
"<A>\n" +
"    <Misto>\n" +
"      <Adresa Okres=\"PRAHA 9\" Stat=\"CZ\"/>\n" +
"        awdad\n" +
"    </Misto>\n" +
"</A>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:x='a.b.c' root='M'>\n" +
"<M xd:script=\"var String s = '';\">\n" +
"    <Measurement xd:script=\"occurs 1..*;\n" +
"      var { int count = 0; float total = 0; }\n"+
"      finally s+=@x:date +';n=' +count+ ',average='+ (total/count)+' ';\"\n"+
	"      x:date = \"required dateTime\" >\n" +
"      <Value xd:script = \"occurs 1..*; finally count++;\">\n" +
"        required double; onTrue total += (float) getParsedValue();\n" +
"      </Value>\n" +
"    </Measurement>\n" +
"    string; onAbsence addText(s);\n" +
"  </M>\n" +
"</xd:def>");
			xml =
"<M>" +
"<Measurement xmlns:x = \"a.b.c\" x:date=\"2017-08-10T11:31:05\">" +
"<Value>10</Value><Value>11.8</Value><Value>9.4</Value>" +
"</Measurement>" +
"<Measurement xmlns:x = \"a.b.c\" x:date=\"2017-08-10T13:01:27\">" +
"<Value>12.35</Value>" +
"</Measurement>" +
"</M>";
			assertEq(
"<M>" +
"<Measurement xmlns:x = \"a.b.c\" x:date=\"2017-08-10T11:31:05\">" +
"<Value>10</Value><Value>11.8</Value><Value>9.4</Value>" +
"</Measurement>" +
"<Measurement xmlns:x = \"a.b.c\" x:date=\"2017-08-10T13:01:27\">" +
"<Value>12.35</Value>" +
"</Measurement>" +
"2017-08-10T11:31:05;n=3,average=10.4 2017-08-10T13:01:27;n=1,average=12.35 " +
"</M>",
				parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
