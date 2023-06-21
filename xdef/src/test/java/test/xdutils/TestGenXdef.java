package test.xdutils;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.util.GenXDefinition;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Test of generation of XDefinition from XML and JSON/XON data.
 * @author Vaclav Trojan
 */
public class TestGenXdef extends XDTester {
	public TestGenXdef() {super();}

	private void test(File[] files, String id) {
		ArrayReporter reporter = new ArrayReporter();
		String fname;
		String xname;
		XDPool xp;
		XDDocument xd;
		Object o,x;
		String tempDir = getTempDir();
		File f = null;
		try {
			for (int i = 0; i < files.length; i++) {
				fname = tempDir + id + i + ".xdef";
				xname = "Example";
				f = files[i];
				GenXDefinition.genXdef(f, fname, "UTF-8", xname);
				xp = XDFactory.compileXD(null, new File(fname));
				xd = xp.createXDDocument(xname);
				o = XonUtils.parseXON(files[i]);
				x = xd.jparse(f, reporter);
				if (reporter.errorWarnings()) {
					fail(id + ", " + f + ":\n" +reporter.printToString());
					reporter.clear();
				}
				assertTrue(XonUtils.xonEqual(o, x), id + ": \n" + f);
			}
		} catch (Exception ex) {
			fail(f.getAbsolutePath());
			fail(ex);
		}
	}
	private void test(String data, String id) {
		ArrayReporter reporter = new ArrayReporter();
		String fname;
		String xname;
		XDPool xp;
		XDDocument xd;
		Object o,x;
		String tempDir = getTempDir();
		try {
			data = "<A a='1f' b='b'><B>true</B><B/></A>";
			fname = tempDir + "A.xdef";
			xname = "Example";
			GenXDefinition.genXdef(data, fname, "UTF-8", xname);
			xp = XDFactory.compileXD(null, new File(fname));
			assertEq(data, parse(xp, xname, data, reporter), id);
			if (reporter.errorWarnings()) {
				fail(id + ":\n" +reporter.printToString());
			}
		} catch (Exception ex) {
			fail(id + ":");
			fail(ex);
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String dataDir = getDataDir() + "test/";
		if (dataDir == null) {
			fail("Data directory is missing, test canceled");
			return;
		}
		Element el;
		String xdef;
		String xml;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		for (String x: new String[] {
"<a/>\n",
"<a>\n"+
"  <b c='20190521214531'/>\n"+
"  <b c='1'/>\n"+
"  <b c='9223372036854775807'/>\n"+
"  <b c='-807' d='5dd302b28bd9b807728f081d7ca61a282d9377bd'/>\n"+
"  <b c='0' e='d302b28bd9b807728f081d7ca61a282d'/>\n"+
"  <b c='92233720368547758099' f='0deff0012000012345'/>\n"+
"</a>",
"<A a='a' b='bb' c='1' d='true'></A>",
"<A>\n"+
"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
"  <C a='125.6.1'/><C a='12.06.1987'/>\n"+
"  <D a='1.5 CZK'/><D a='12 USD'/>\n"+
"</A>",
"<T>\n"+
"  <R A='xx' B='aaa'/>\n"+
"  <R A='xxx' B='aa'/>\n"+
"  <R A='xxx' B='aaa'/>\n"+
"  <R A='xx' B='aa'/>\n"+
"</T>",
"<X xmlns=\"a.b.c\" x=\"123\">\n"+
"  <Y> x </Y>\n"+
"  <Y/>\n"+
"  <Z/>\n"+
"</X>",
"<x:X xmlns:x=\"a.b.c\" x:x=\"123\">\n"+
"  <x:Y x:y=\"z\"> x </x:Y>\n"+
"  <x:Y/>\n"+
"  <x:Z x:y=\"z\"/>\n"+
"</x:X>",
"<Contract Number = \"0123456789\" date = \"2011-12-13\" >\n"+
"  <Client Type = \"1\"\n"+
"          Name = \"Company LTD\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName  = \"John\"\n"+
"          LastName   = \"Smith\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type       = \"3\"\n"+
"          GivenName  = \"Bill\"\n"+
"          LastName   = \"White\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>",
"<A_ IdFlow=\"181131058\" date=\"20190521214531\">\n"+
"    <XXX IdDefPartner=\"163\"/>\n"+
"    <YYY FileKind=\"W1A\"/>\n"+
"    <A a=\"SLP\">\n"+
"        <B b=\"b\">\n"+
"            <Z x=\"3\" />\n"+
"            <Z x=\"4\" />\n"+
"        </B>\n"+
"   </A>\n"+
"</A_>",
"<a>\n"+
"  <DefParams>\n"+
"    <Param Name=\"Name\" Type=\"string()\" />\n"+
"    <Param Type=\"dec()\" Name=\"Height\"/>\n"+
"    <Param Name=\"Date of birth\" Type=\"xdatetime('dd.MM.yyyy')\" />\n"+
"  </DefParams>\n"+
"  <Params>\n"+
"    <Param Name=\"Name\" Value=\"John\"/>\n"+
"    <Param Name=\"Height\" Value=\"14.8\"/>\n"+
"    <Param Name=\"Date of birth\" Value=\"01.02.1987\"/>\n"+
"  </Params>\n"+
"  <Params>\n"+
"    <Param Value=\"14.8a\" Name=\"Height\"/>\n"+
"  </Params>\n"+
"</a>",
"<Values Ver=\"2.0\"\n"+
"       Valid=\"1.1.2000 00:00:01\"\n"+
"       Channel=\"A\"\n"+
"       Seq=\"1\"\n"+
"       SeqRef=\"1\"\n"+
"       Date=\"1.1.2000\">\n"+
"   <File Name=\"abcdef\"\n"+
"           Format=\"TXT\"\n"+
"           Kind=\"xyz\"\n"+
"           RecNum=\"12345678\"\n"+
"           ref=\"111\">\n"+
"       <CheckSum Type=\"MD5\"\n"+
"           Value=\"\n"+
"123456789A123456789A123456789A12123456789A123456789A123456789A12\n"+
"                 \"/>\n"+
"       <xxxx/>\n"+
"       <yyyy/>\n"+
"   </File>\n"+
"   <File Name=\"sss.bb\"\n"+
"           Format=\"TXT\"\n"+
"           Kind=\"\"\n"+
"           RecNum=\"12345678\">\n"+
"   </File>\n"+
"   xxxx\n"+
"   <y><fff attr=\"???\"/></y>\n"+
"   <log cttr=\"xxx\" />\n"+
"</Values>",
dataDir + "Matej3_out.xml",
dataDir + "TestValidate2.xml",
		}) {
			try {
				el = GenXDefinition.genXdef(x);
				xdef = KXmlUtils.nodeToString(el, true);
				xp = compile(xdef);
				assertEq(x, parse(xp, "", x, reporter));
				assertNoErrorwarnings(reporter);
			} catch (Exception ex) {fail(ex);}
		}
		try {
			xml =
"<xd:X xmlns:xd=\"a.b.c\" xd:x=\"123\">\n"+
"  <xd:Y xd:y=\"z\"> x </xd:Y>\n"+
"  <xd:Y y=\"z\"/>\n"+
"  <xd:Y/>\n"+
"</xd:X>";
			el = GenXDefinition.genXdef(xml);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XDEF881")) {
				fail(ex);
			}
		}
		try {
			xml =
"<d:X xmlns:d=\""+ XDConstants.XDEF31_NS_URI + "\" d:x=\"123\">\n"+
"  <d:Y d:y=\"z\"> x </d:Y>\n"+
"  <d:Y y=\"z\"/>\n"+
"  <d:Y/>\n"+
"</d:X>";
			el = GenXDefinition.genXdef(xml);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XDEF882")) {
				fail(ex);
			}
		}
		try {
			test("<A a='1f' b='b'><B>true</B><B/></A>", "A");
			test("{a:1,b:true,c:[1,true,null,\"null\",\"\",null],d:{x:-9}}", "B");
			test("t\"+420 234 567 890\"", "C");
			test(
"{ \"desc\" : \"Distances\", \"updated\" : \"02014\",\n" +
"  \"cities\" : { \"Brussels\": [\n" +
"      {\"to\": \"London\", \"distance\": 322},\n" +
"      {\"to\": \"Amsterdam\", \"distance\": 173}\n" +
"    ],\n" +
"    \"Amsterdam\": [\n" +
"      {\"to\": \"Brussels\", \"distance\": 173},\n" +
"      {\"to\": \"Paris\", \"distance\": 431}\n" +
"    ]\n" +
"  }\n" +
"}", "D");
			test(SUtils.getFileGroup(
				"src/test/resources/test/common/xon/data/*.json"), "F");
			test(SUtils.getFileGroup(
				"src/test/resources/test/xdef/data/json/*.json"), "G");
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}