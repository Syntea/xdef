package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDXmlOutStream;
import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;

/** TestDebug provides testing of XML writer.
 * @author Trojan
 */
public final class TestXmlWriter extends XDTester {

	public TestXmlWriter() {super();}

	@Override
	public void test() {
		XDPool xp;
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		String tempDir = super.getTempDir();
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='books'>\n"+
"  <books>\n"+
"    <book xd:script='+; forget' isbn='int'>\n"+
"      <title>string</title>\n"+
"      <edited xd:script='?'>gYear</edited>\n"+
"      <author xd:script='*'>string</author>\n"+
"    </book>\n"+
"  </books>\n"+
"</xd:def>";
			xml =
"<books>\n"+
"  <book isbn='123456789'>\n"+
"     <title>Tutorial</title>\n"+
"     <edited>2016</edited>\n"+
"     <author>John B. Brown</author>\n"+
"     <author>Peter Smith</author>\n"+
"  </book>\n"+
"  <book isbn='123456789'>\n"+
"     <title>The Bible</title>\n"+
"  </book>\n"+
"</books>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			StringWriter strw = new StringWriter();
			XDXmlOutStream xmlWriter =
				XDFactory.createXDXmlOutStream(strw, null, true);
			xd.setStreamWriter(xmlWriter);
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml,
				KXmlUtils.parseXml(strw.toString()).getDocumentElement());
			assertEq("<books/>", el);

			xdef = // Test XmlOutStream methods
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String tempDir;\n"+
"    XmlOutStream x = new XmlOutStream(tempDir + 'x.xml','UTF-8',true);\n"+
"  </xd:declaration>\n"+
"  <a x=';' xd:script='onStartElement x.writeElementStart();\n"+
"                      finally {x.writeElementEnd(); x.close();}'>\n"+
"    <b x=';' xd:script='*; onStartElement x.writeElementStart();\n"+
"                           finally x.writeElementEnd(); forget'>\n"+
"      <c xd:script='*; finally x.writeElement(); forget'>\n"+
"        string();\n"+
"      </c>\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a x='_'>" +
"<b x='a'><c>x</c><c>y</c></b>" +
"<b x='b'/>" +
"<b x='c'><c>z</c></b>" +
"</a>";
			xd = xp.createXDDocument();
			xd.setVariable("tempDir", tempDir);
			assertEq("<a x='_'/>", parse(xd, xml, reporter));
			assertNoErrors(reporter);
			el = KXmlUtils.parseXml(tempDir + "x.xml").getDocumentElement();
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
		new File(tempDir + "x.xml").delete();

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}
