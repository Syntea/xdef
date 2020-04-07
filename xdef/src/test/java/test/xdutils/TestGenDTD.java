package test.xdutils;

import org.xdef.util.GenDTD;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import test.XDTester;

/** Test of generation of DTD from XDefinition.
 * @author Vaclav Trojan
 */
public class TestGenDTD extends XDTester {

	MyHandler _handler;

	public TestGenDTD() {super(); _handler = new MyHandler();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XMLReader reader = null;
		String xdef;
		String xml;
		String s;
		ByteArrayOutputStream bos;
		OutputStreamWriter wr;
		try {
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setFeature( //validace pres DTD
				"http://xml.org/sax/features/validation", //FeatureURI
				true);
			reader.setEntityResolver(_handler); // Register entity resolver
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def xd:name=\"a\" xd:root=\"aaa#log | bbb#mog | *\">\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"aaa\">\n" +
"<log bttr=\"required\"> </log>\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"bbb\">\n" +
"<mog attr=\"required\"> </mog>\n" +
"</xd:def>\n" +
"<xd:def xd:name=\"abc\"\n" +
"          xd:root=\"abc#Data | aaa#log\">\n" +
"<Data Verze=\"fixed '2.0'\"\n" +
"       PlatnostOd=\"optional datetime('d.M.yyyy HH:mm:ss');\n" +
"                    onTrue set('11')\"\n" +
"       Kanal=\"required; onAbsence setText('123')\"\n" +
"       Seq=\"required int\"\n" +
"       SeqRef=\"optional int\"\n" +
"       Date=\"required datetime('d.M.yyyy')\">\n" +
"   <xd:mixed>\n" +
"   <File Name=\"required string(256)\"\n" +
"         Format=\"required enum('TXT','XML','CTL')\"\n" +
"         Kind=\"required string(3,3)\"\n" +
"         RecNum=\"required num(8)\"\n" +
"         ref=\"optional\"\n" +
"         xd:script=\"occurs 0..\">\n" +
"       <xd:mixed>\n" +
"       <CheckSum Type=\"required enum('MD5','CRC')\"\n" +
"                Value=\"required string\"\n" +
"                xd:script=\"occurs 3..5\">\n" +
"       </CheckSum>\n" +
"       <x xd:script=\"occurs 1..5; ref empty.node\" />\n" +
"       </xd:mixed>\n" +
"   </File>\n" +
"   <xd:text>required string;\n" +
"              onTrue set('ahoj'); onAbsence set('nazdar')\n" +
"   </xd:text>\n" +
"   <y xd:script=\"ref y\" />\n" +
"   <log xd:script=\"ref log\" />\n" +
"   </xd:mixed>\n" +
"</Data>\n" +
"<log cttr=\"required\"> </log>\n" +
"<empty.node/>\n" +
"<qwert xd:script=\"ref y\" />\n" +
"<y xd:script=\"ref z\" />\n" +
"<z><fff attr=\"optional\"/></z>\n" +
"<zz attr=\"required\"/>\n" +
"<q xd:script=\"occurs 1..\" >\n" +
"  <xd:text>required</xd:text> </q>\n" +
"</xd:def>\n" +
"</xd:collection>\n" +
"";
			xml =
"<?xml version=\"1.0\"?>\n" +
"<!DOCTYPE Data SYSTEM \"Data.dtd\">\n" +
"<Data Verze=\"2.0\"\n" +
"       PlatnostOd=\"1.1.2000 00:00:01\"\n" +
"       Kanal=\"A\"\n" +
"       Seq=\"1\"\n" +
"       SeqRef=\"1\"\n" +
"       Date=\"1.1.2000\">\n" +
"   <File Name=\"abcdef\"\n" +
"           Format=\"TXT\"\n" +
"           Kind=\"xyz\"\n" +
"           RecNum=\"12345678\"\n" +
"           ref=\"111\">\n" +
"       <CheckSum Type=\"MD5\"\n" +
"                 Value=\"123456789A123456789A123456789A12" +
"123456789A123456789A123456789A12\"/>\n" +
"       <x/>\n" +
"       <x/>\n" +
"   </File>\n" +
"   ahoj\n" +
"   <y><fff attr=\"???\"/></y>\n" +
"   <log cttr=\"xxx\" />\n" +
"</Data>\n" +
"";
			InputStream[] streams = new InputStream[]
				{new java.io.ByteArrayInputStream(xdef.getBytes())};
			bos = new ByteArrayOutputStream();
			wr = new OutputStreamWriter(bos, "UTF-8");
			GenDTD.genDTD(streams, "abc#Data", wr);
			wr.close();
			bos.close();
			s = bos.toString();
			 // Parse
			InputSource inputSource = new InputSource(
				new java.io.ByteArrayInputStream(xml.getBytes()));
			inputSource.setSystemId("Data.xml");
			inputSource.setEncoding("UTF-8");
			_handler._dtdInput = new InputSource(
				new java.io.ByteArrayInputStream(s.getBytes()));
			_handler._dtdInput.setSystemId("Data.dtd");
			_handler._dtdInput.setEncoding("UTF-8");
			reader.parse(inputSource);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	private class MyHandler implements EntityResolver {

		InputSource _dtdInput = null;

		MyHandler() {}

		@Override
		public InputSource resolveEntity(String publicID, String systemID)
		throws IOException, SAXException {
			return _dtdInput;
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}