package test.xdutils;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import org.xdef.xml.KXmlUtils;
import org.xdef.util.GenDTD2XDEF;
import org.xdef.XDPool;
import org.xdef.sys.SUtils;
import org.xdef.util.DTDToXdef;
import java.io.File;
import org.w3c.dom.Element;
import buildtools.XDTester;

/** Test of conversion of DTD to X-definition.
 * @author Vaclav Trojan
 */
public class TestDTDToXdef extends XDTester {

	public TestDTDToXdef() {super();}

	/** display 0 => no display,
	 * 1 .. display DTD,
	 * 2 .. display xdef.
	 * 4 .. display xml data.
	 */
	private void test(String dtdData,
		String root,
		String data,
		int display) {
		try {
			GenDTD2XDEF parser = new GenDTD2XDEF(dtdData);
			Element elxd = parser.genRootXd(root).getDocumentElement();
			if (elxd == null) {
				fail("Error - XDefinition is null");
				return;
			} else {
				if ((display & 2) == 2) {
					System.out.println("====================================");
					System.out.println(KXmlUtils.nodeToString(elxd, true));
				}
			}
			XDPool xdp;
			try {
				xdp = compile(KXmlUtils.nodeToString(elxd, true));
			} catch (Exception e) {
				fail(e);
				fail(KXmlUtils.nodeToString(elxd, true));
				return;
			}
			try {
				ArrayReporter reporter = new ArrayReporter();
				parse(xdp, root, data, reporter);
				reporter.checkAndThrowErrorWarnings();
				if ((display & 4) == 4) {
					System.out.println("====================================");
					System.out.println(KXmlUtils.nodeToString(
						KXmlUtils.parseXml(data)));
				}
			} catch (Exception ex) {
				fail(ex);
				System.out.println(KXmlUtils.nodeToString(elxd, true));
				System.out.println(KXmlUtils.nodeToString(
					KXmlUtils.parseXml(data), true));
			}
		} catch (Exception e) {
			fail(e);
		}
	}

	@Override
	public void test() {
		String dataDir = getDataDir();
		String tempDir = dataDir + "temp/";
		File f = new File(tempDir);
		f.mkdirs();
		String xmlData;
		String dtdData;
//		//0..no display, 1 .. display DTD, 2 .. display XDEF, 4 .. display XML
		int display = 0;
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT payment (note)*>\n"+
"<!ATTLIST payment type CDATA \"check\">\n"+
"<!ELEMENT note (to, from, heading, body, payment)>\n"+
"<!ELEMENT to      (#PCDATA)>\n"+
"<!ELEMENT from    (#PCDATA)>\n"+
"<!ELEMENT heading (#PCDATA)>\n"+
"<!ELEMENT body    (#PCDATA)>";
		xmlData =
"<payment>\n" +
"  <note>\n" +
"   <to>      a  </to>\n" +
"   <from>    b  </from>\n" +
"   <heading> c  </heading>\n" +
"   <body>    d  </body>\n" +
"   <payment/>\n" +
"  </note>\n" +
"  <note>\n" +
"   <to>      e  </to>\n" +
"   <from>    f  </from>\n" +
"   <heading> g  </heading>\n" +
"   <body>    h  </body>\n" +
"   <payment type=\"i\" />\n" +
"  </note>\n" +
"</payment>";
		test(dtdData, "payment", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ENTITY tutorial '\"tutorial\"'>\n" +
"<!ELEMENT tutorial (#PCDATA)>";
		xmlData =
"<!DOCTYPE tutorial [<!ENTITY tutorial '\"tutorial\"'>\n" +
"<!ELEMENT tutorial (#PCDATA)>]>\n" +
"<tutorial>&tutorial;</tutorial>";
		test(dtdData, "tutorial", xmlData, display);
		xmlData = "<tutorial>text</tutorial>";
		test(dtdData, "tutorial", xmlData, display);

// START fails in Java 1.9 and higher //////////////////////////////////////////
if (SUtils.JAVA_RUNTIME_VERSION_ID < 109) {
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA , BBB)>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>";
		xmlData = "<XXX><AAA/><BBB/></XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA*, BBB)>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>";
		xmlData = "<XXX><AAA/><AAA/><BBB/></XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA, BBB)*>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>";
		xmlData = "<XXX><AAA/><BBB/><AAA/><BBB/></XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT root (A, B?, (C | D)* )>\n" +
"<!ELEMENT A EMPTY>\n" +
"<!ELEMENT B EMPTY>\n" +
"<!ELEMENT C EMPTY>\n" +
"<!ELEMENT D EMPTY>";
		xmlData =
"<root>\n" +
"<A/>\n" +
"<C/>\n" +
"<D/>\n" +
"<C/>\n" +
"</root>";
		test(dtdData, "root", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
//"<!DOCTYPE XXX [\n"+
"<!ELEMENT XXX (AAA+ , BBB)>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>\n" +
//"]>\n"+
//"<XXX/>\n" +
"";
		xmlData =
"<XXX>\n" +
"<AAA/>\n" +
"<AAA/>\n" +
"<BBB/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA? , BBB)>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>";
		xmlData =
"<XXX>\n" +
"<BBB/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA? , BBB+)>\n" +
"<!ELEMENT AAA (CCC? , DDD*)>\n" +
"<!ELEMENT BBB (CCC , DDD)>\n" +
"<!ELEMENT CCC EMPTY>\n" +
"<!ELEMENT DDD EMPTY>";
		xmlData =
"<XXX>\n" +
"  <AAA/>\n" +
"  <BBB>\n" +
"    <CCC/>\n" +
"    <DDD/>\n" +
"  </BBB>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		xmlData =
"<XXX>\n" +
"  <BBB>\n" +
"    <CCC/>\n" +
"    <DDD/>\n" +
"  </BBB>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA , BBB)>\n" +
"<!ELEMENT AAA (CCC , DDD)>\n" +
"<!ELEMENT BBB (CCC | DDD)>\n" +
"<!ELEMENT CCC EMPTY>\n" +
"<!ELEMENT DDD EMPTY>";
		xmlData =
"<XXX>\n" +
"  <AAA>\n" +
"    <CCC/>\n" +
"    <DDD/>\n" +
"  </AAA>\n" +
"  <BBB>\n" +
"    <CCC/>\n" +
"  </BBB>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+ , BBB+)>\n" +
"<!ELEMENT AAA (BBB | CCC)>\n" +
"<!ELEMENT BBB (#PCDATA | CCC)*>\n" +
"<!ELEMENT CCC (#PCDATA)*>";
		xmlData =
"<XXX>\n" +
"  <AAA>\n" +
"    <CCC>text1</CCC>\n" +
"  </AAA>\n" +
"  <AAA>\n" +
"    <BBB>\n" +
"      <CCC>text2</CCC>\n" +
"    </BBB>\n" +
"  </AAA>\n" +
"  <AAA>\n" +
"    <BBB>\n" +
"      text3\n" +
"      <CCC/>\n" +
"      text4\n" +
"    </BBB>\n" +
"  </AAA>\n" +
"  <BBB>\n" +
"    <CCC/>\n" +
"    <CCC/>\n" +
"    <CCC/>\n" +
"  </BBB>\n" +
"  <BBB/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+ , BBB+)>\n" +
"<!ELEMENT AAA (BBB | CCC)>\n" +
"<!ELEMENT BBB (#PCDATA | CCC)*>\n" +
"<!ELEMENT CCC (#PCDATA)*>";
		xmlData =
"<XXX>\n" +
"  <AAA>\n" +
"    <BBB>text1<CCC>text2</CCC>text3</BBB>\n" +
"  </AAA>\n" +
"  <AAA>\n" +
"    <CCC>text4</CCC>\n" +
"  </AAA>\n" +
"  <BBB/>\n" +
"  <BBB>text5<CCC>text6</CCC>text7</BBB>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+ , BBB+ , CCC+)>\n"+
"<!ELEMENT AAA EMPTY>\n"+
"<!ELEMENT BBB EMPTY>\n"+
"<!ELEMENT CCC EMPTY>\n"+
"<!ATTLIST AAA\n"+
"     id ID #REQUIRED>\n"+
"<!ATTLIST BBB \n"+
"     code ID #IMPLIED\n"+
"     list NMTOKENS #IMPLIED>\n"+
"<!ATTLIST CCC \n"+
"     X ID #REQUIRED\n"+
"     Y NMTOKEN #IMPLIED>";
		xmlData =
"<XXX>\n" +
"  <AAA id = 'a1'/>\n" +
"  <AAA id = 'a2'/>\n" +
"  <BBB code = 'b'/>\n" +
"  <BBB list = 'a1 c'/>\n" +
"  <CCC X = 'c' Y = '123'/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+ , BBB+, CCC+, DDD+)>\n"+
"<!ELEMENT AAA EMPTY>\n"+
"<!ELEMENT BBB EMPTY>\n"+
"<!ELEMENT CCC EMPTY>\n"+
"<!ELEMENT DDD EMPTY>\n"+
"<!ATTLIST AAA \n"+
"     mark ID #REQUIRED>\n"+
"<!ATTLIST BBB \n"+
"     id ID #REQUIRED>\n"+
"<!ATTLIST CCC \n"+
"     ref IDREF #REQUIRED>\n"+
"<!ATTLIST DDD \n"+
"     ref IDREFS #REQUIRED>";
		xmlData =
"<XXX>\n" +
"  <AAA mark = 'a1'/>\n" +
"  <AAA mark = 'a2'/>\n" +
"  <BBB id = 'b1'/>\n" +
"  <BBB id = 'b2'/>\n" +
"  <CCC ref = 'a1'/>\n" +
"  <CCC ref = 'b2'/>\n" +
"  <DDD ref = 'b2 b1 a2 a1'/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+, BBB+)>\n"+
"<!ELEMENT AAA EMPTY>\n"+
"<!ELEMENT BBB EMPTY>\n"+
"<!ATTLIST AAA \n"+
"     true ( yes | no ) #REQUIRED>\n"+
"<!ATTLIST BBB \n"+
"     month (1|2|3|4|5|6|7|8|9|10|11|12) #IMPLIED>";
		xmlData =
"<XXX>\n" +
"  <AAA true = 'no'/>\n" +
"  <AAA true = 'yes'/>\n" +
"  <BBB month = '1'/>\n" +
"  <BBB month = '12'/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT root (child*)>\n"+
"<!ELEMENT child EMPTY>\n"+
"<!ATTLIST child\n" +
"     fix CDATA #FIXED 'fixed'\n"+
"     default (X|Y) 'X'\n"+
"     key ID #IMPLIED\n"+
"     ref IDREFS #IMPLIED\n"+
"     name NMTOKEN #IMPLIED\n"+
"     names NMTOKENS #IMPLIED>";
		xmlData =
"<root>\n" +
"  <child fix='fixed'\n" +
"         default='Y'\n" +
"         key='c1'\n" +
"         name=' a '/>\n" +
"  <child fix='fixed'\n" +
"         key='   c2   '\n" +
"         names=' 1 2 '/>\n" +
"  <child fix='fixed'\n" +
"         default='X'\n" +
"         ref=' c1 c2 '/>\n" +
"</root>";
		test(dtdData, "root", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		//parameter entities
		dtdData =
"<!ELEMENT XXX (AAA,BBB,CCC?)>\n" +
"<!ATTLIST XXX \n" +
"     type CDATA #FIXED ''>\n" +
"<!ATTLIST XXX ns CDATA #FIXED 'XCBL30.sox'>\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>\n" +
"<!ELEMENT CCC EMPTY>";
		xmlData =
"<XXX type='' ns = 'XCBL30.sox'>\n" +
"<AAA/>\n" +
"<BBB/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT attributes (#PCDATA)*>\n"+
"<!ATTLIST attributes\n"+
"     aaa CDATA #REQUIRED\n"+
"     bbb CDATA #IMPLIED>";
		xmlData =
"<attributes\n" +
"  aaa = 'aaa'>\n" +
"  text\n" +
"</attributes>";
		test(dtdData, "attributes", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT attributes EMPTY>\n"+
"<!ATTLIST attributes\n"+
"     aaa CDATA #IMPLIED\n"+
"     bbb NMTOKEN #REQUIRED\n"+
"     ccc NMTOKENS #REQUIRED>";
		xmlData =
"<attributes\n" +
"  bbb = '123a'\n" +
"  ccc = 'aaa b:c'/>";
		test(dtdData, "attributes", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA+, BBB+)>\n"+
"<!ELEMENT AAA EMPTY>\n"+
"<!ELEMENT BBB EMPTY>\n"+
"<!ATTLIST AAA \n"+
"     true ( yes | no ) \"yes\">\n"+
"<!ATTLIST BBB \n"+
"     month NMTOKEN \"1\">";
		xmlData =
"<XXX>\n" +
"  <AAA/>\n" +
"  <AAA true = 'yes'/>\n" +
"  <BBB/>\n" +
"  <BBB month = '12'/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA*,BBB)>\n" +
"<!ELEMENT AAA (#PCDATA)>\n" +
"<!ELEMENT BBB (#PCDATA)>";
		xmlData =
"<XXX>\n" +
"  <AAA>text1</AAA>\n" +
"  <AAA/>\n" +
"  <AAA/>\n" +
"  <BBB>text2</BBB>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA) >\n" +
"<!ELEMENT AAA (BBB|CCC) >\n" +
"<!ELEMENT BBB EMPTY >\n" +
"<!ATTLIST BBB\n" +
"     a1 CDATA #FIXED 'a1'\n"+
"     a2 CDATA 'a2'>\n"+
"<!ELEMENT CCC EMPTY >";
		xmlData =
"<XXX>\n" +
"  <AAA>\n" +
"    <BBB a1 = 'a1' a2 = 'x'/>\n" +
"  </AAA>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT XXX (AAA*|(BBB*,(CCC|(DDD,EEE+)*|FFF?), AAA*))? >\n" +
"<!ELEMENT AAA EMPTY>\n" +
"<!ELEMENT BBB EMPTY>\n" +
"<!ELEMENT CCC EMPTY>\n" +
"<!ELEMENT DDD EMPTY>\n" +
"<!ELEMENT EEE EMPTY>\n" +
"<!ELEMENT FFF EMPTY>";
		xmlData =
"<XXX>\n" +
"  <BBB/>\n" +
"  <DDD/>\n" +
"  <EEE/>\n" +
"</XXX>";
		test(dtdData, "XXX", xmlData, display);
}
// END - fails in Java 1.9 /////////////////////////////////////////////////////
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ELEMENT TVSCHEDULE (CHANNEL+)>\n"+
"<!ELEMENT CHANNEL (BANNER,DAY+)>\n"+
"<!ELEMENT BANNER (#PCDATA)>\n"+
"<!ELEMENT DAY (DATE,(HOLIDAY|PROGRAMSLOT+)+)>\n"+
"<!ELEMENT HOLIDAY (#PCDATA)>\n"+
"<!ELEMENT DATE (#PCDATA)>\n"+
"<!ELEMENT PROGRAMSLOT (TIME,TITLE,DESCRIPTION?)>\n"+
"<!ELEMENT TIME (#PCDATA)>\n"+
"<!ELEMENT TITLE (#PCDATA)> \n"+
"<!ELEMENT DESCRIPTION (#PCDATA)>\n"+
"\n"+
"<!ATTLIST TVSCHEDULE NAME CDATA #REQUIRED>\n"+
"<!ATTLIST CHANNEL CHAN CDATA #REQUIRED>\n"+
"<!ATTLIST PROGRAMSLOT VTR CDATA #IMPLIED>\n"+
"<!ATTLIST TITLE RATING CDATA #IMPLIED>\n"+
"<!ATTLIST TITLE LANGUAGE CDATA #IMPLIED>";
		xmlData =
"<TVSCHEDULE NAME='A'>\n" +
"  <CHANNEL CHAN='1'>\n" +
"   <BANNER>  a  </BANNER>\n" +
"   <DAY>\n" +
"     <DATE> c  </DATE>\n" +
"     <HOLIDAY> g  </HOLIDAY>\n" +
"     <PROGRAMSLOT VTR='vtr'>\n" +
"       <TIME>  x  </TIME>\n" +
"       <TITLE> y  </TITLE>\n" +
"     </PROGRAMSLOT>\n" +
"     <HOLIDAY> g  </HOLIDAY>\n" +
"     <PROGRAMSLOT VTR='vtr'>\n" +
"       <TIME>  x  </TIME>\n" +
"       <TITLE> y  </TITLE>\n" +
"     </PROGRAMSLOT>\n" +
"     <PROGRAMSLOT VTR='vtr'>\n" +
"       <TIME>  x  </TIME>\n" +
"       <TITLE> y  </TITLE>\n" +
"     </PROGRAMSLOT>\n" +
"   </DAY>\n" +
"  </CHANNEL>\n" +
"  <CHANNEL CHAN='2'>\n" +
"   <BANNER>  b  </BANNER>\n" +
"   <DAY>\n" +
"     <DATE> c  </DATE>\n" +
"     <HOLIDAY> h </HOLIDAY>\n" +
"     <PROGRAMSLOT VTR='vtr'>\n" +
"       <TIME>  x  </TIME>\n" +
"       <TITLE> y  </TITLE>\n" +
"     </PROGRAMSLOT>\n" +
"     <PROGRAMSLOT VTR='vtr'>\n" +
"       <TIME>  x  </TIME>\n" +
"       <TITLE> y  </TITLE>\n" +
"     </PROGRAMSLOT>\n" +
"   </DAY>\n" +
"  </CHANNEL>\n" +
"</TVSCHEDULE>";
		test(dtdData, "TVSCHEDULE", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!-- comment -->\n"+
"<!ELEMENT NEWSPAPER (ARTICLE+)>\n"+
"<!ELEMENT ARTICLE (HEADLINE,BYLINE,LEAD,BODY,NOTES)>\n"+
"<!ELEMENT HEADLINE (#PCDATA)>\n"+
"<!ELEMENT BYLINE (#PCDATA)>\n"+
"<!ELEMENT LEAD (#PCDATA)>\n"+
"<!ELEMENT BODY (#PCDATA)>\n"+
"<!ELEMENT NOTES (#PCDATA)>\n"+
"<!ATTLIST ARTICLE AUTHOR CDATA #REQUIRED>\n"+
"<!ATTLIST ARTICLE EDITOR CDATA #IMPLIED>\n"+
"<!ATTLIST ARTICLE DATE CDATA #IMPLIED>\n"+
"<!ATTLIST ARTICLE EDITION CDATA #IMPLIED>\n"+
"<!ENTITY NEWSPAPER \"Vervet Logic Times\">\n"+
"<!ENTITY PUBLISHER \"Vervet Logic Press\">\n"+
"<!ENTITY COPYRIGHT \"Copyright 1998 Vervet Logic Press\">\n"+
"<!-- comment -->";
		xmlData =
"<NEWSPAPER>\n" +
"  <ARTICLE AUTHOR='1' EDITOR='2' DATE='3' EDITION='4'>\n" +
"    <HEADLINE>  a  </HEADLINE>\n" +
"    <BYLINE>    b  </BYLINE>\n" +
"    <LEAD>      c  </LEAD>\n" +
"    <BODY>      d  </BODY>\n" +
"    <NOTES>     e  </NOTES>\n" +
"  </ARTICLE>\n" +
"  <ARTICLE AUTHOR='5'>\n" +
"    <HEADLINE>  f  </HEADLINE>\n" +
"    <BYLINE>    g  </BYLINE>\n" +
"    <LEAD>      h  </LEAD>\n" +
"    <BODY>      i  </BODY>\n" +
"    <NOTES>     j  </NOTES>\n" +
"  </ARTICLE>\n" +
"</NEWSPAPER>";
		test(dtdData, "NEWSPAPER", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		dtdData =
"<!ENTITY AUTHOR \"John Doe\">\n"+
"<!ENTITY COMPANY \"JD Power Tools, Inc.\">\n"+
"<!ENTITY EMAIL \"jd@jd-tools.com\">\n"+
"\n"+
"<!ELEMENT CATALOG (PRODUCT+)>\n"+
"\n"+
"<!ELEMENT PRODUCT\n"+
"(SPECIFICATIONS+,OPTIONS?,PRICE+,NOTES?)>\n"+
"<!ATTLIST PRODUCT\n"+
"     NAME CDATA #IMPLIED\n"+
"     CATEGORY (HandTool|Table|Shop-Professional) \"HandTool\"\n"+
"     PARTNUM CDATA #IMPLIED\n"+
"     PLANT (Pittsburgh|Milwaukee|Chicago) \"Chicago\"\n"+
"     INVENTORY (InStock|Backordered|Discontinued) \"InStock\">\n"+
"\n"+
"<!ELEMENT SPECIFICATIONS (#PCDATA)>\n"+
"<!ATTLIST SPECIFICATIONS\n"+
"     WEIGHT CDATA #IMPLIED\n"+
"     POWER CDATA #IMPLIED>\n"+
"\n"+
"<!ELEMENT OPTIONS (#PCDATA)>\n"+
"<!ATTLIST OPTIONS\n"+
"     FINISH (Metal|Polished|Matte) \"Matte\" \n"+
"     ADAPTER (Included|Optional|NotApplicable) \"Included\"\n"+
"     CASE (HardShell|Soft|NotApplicable) \"HardShell\">\n"+
"\n"+
"<!ELEMENT PRICE (#PCDATA)>\n"+
"<!ATTLIST PRICE\n"+
"     MSRP CDATA #IMPLIED\n"+
"     WHOLESALE CDATA #IMPLIED\n"+
"     STREET CDATA #IMPLIED\n"+
"     SHIPPING CDATA #IMPLIED>\n"+
"\n"+
"<!ELEMENT NOTES (#PCDATA)>";
		xmlData =
"<CATALOG>\n" + //"(SPECIFICATIONS+,OPTIONS?,PRICE+,NOTES?)>\n"+
"  <PRODUCT NAME='y'>\n" +
"    <SPECIFICATIONS>  a  </SPECIFICATIONS>\n" +
"    <SPECIFICATIONS>  b  </SPECIFICATIONS>\n" +
"    <PRICE>  1  </PRICE>\n" +
"    <NOTES>  e  </NOTES>\n" +
"  </PRODUCT>\n" +
"  <PRODUCT>\n" +
"    <SPECIFICATIONS>  a  </SPECIFICATIONS>\n" +
"    <OPTIONS CASE='Soft'>   o   </OPTIONS>\n" +
"    <PRICE>  2  </PRICE>\n" +
"  </PRODUCT>\n" +
"</CATALOG>\n" +
"";
		test(dtdData, "CATALOG", xmlData, display);
////////////////////////////////////////////////////////////////////////////////
		DTDToXdef.main(new String[]{"-in",
			dataDir + "dtds/a.xml",
			"-out",
			tempDir + "generated_a.xdef",
			"-r",
			"root"});
		DTDToXdef.main(new String[]{"-in",
			dataDir + "dtds/TV.xml",
			"-out",
			tempDir + "generated_TV.xdef",
			"-r",
			"TVSCHEDULE"});
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
//		dtdData =
//"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'\n" +
//"   'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n" +
//"<html/>";
//		xmlData =
//"<html>\n" +
//"  <head>\n" +
//"    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n" +
//"    <title>Kapitola 1. X-definice step-by-step: model XML elementu</title>\n" +
//"  </head>\n" +
//"  <body>\n" +
//"   <b>Obsah</b>\n" +
//"  </body>\n" +
//"</html>";
//		test(dtdData, "html", xmlData, 2);
////		test(dtdData, "html", xmlData, display);
//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
		try {
			FUtils.deleteAll(tempDir, true);
		} catch (SException ex) {
			throw new RuntimeException("Could not delete temporary files!", ex);
		}
////////////////////////////////////////////////////////////////////////////////
	}
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}

}