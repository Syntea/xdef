package test.xdef;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDXmlOutStream;
import org.w3c.dom.Element;
import org.xdef.XDTools;

/** TestDebug provides testing of XML writer.
 * @author Trojan
 */
public final class TestXmlWriter extends XDTester {

	public TestXmlWriter() {super();}

	@Override
	public void test() {
		ByteArrayOutputStream bos;
		ByteArrayInputStream bis;
		XDPool xp;
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		String tempDir;
		try {
			tempDir = clearTempDir().getCanonicalPath().replace('\\', '/');
			if (!tempDir.endsWith("/")) {
				tempDir += "/";
			}
		} catch (Exception ex) {
			fail(ex);
			return;
		}
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
			bos = new ByteArrayOutputStream();
			XDXmlOutStream xmlWriter =
				XDFactory.createXDXmlOutStream(bos, null, true);
			xd.setStreamWriter(xmlWriter);
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			bis =
				new ByteArrayInputStream(bos.toByteArray());
			assertEq(xml, KXmlUtils.parseXml(bis).getDocumentElement());
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
			xdef = // Test XmlOutStream methods
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> external XmlOutStream x; </xd:declaration>\n"+
"  <a x=';' xd:script='onStartElement x.writeElementStart();\n"+
"                      finally {x.writeElementEnd(); x.close();}'>\n"+
"    <b x='' xd:script='*; finally x.writeElement(); forget'>\n"+
"      <c xd:script='*;'>string();</c>\n"+
"    </b>\n"+
"    <d y='?' xd:script='*; finally x.writeElement(); forget'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			System.out.println(xdef);
			xd = xp.createXDDocument();
			bos = new ByteArrayOutputStream();
			xd.setVariable("x", XDTools.createXDXmlOutStream(bos,"UTF-8",true));
			xml =
"<a x='_'>" +
"<b x='a'><c>x</c><c>y</c></b>" +
"<b x='b'/>" +
"<b x='c'><c>z</c></b>" +
"<d y='1'/>" +
"<d y='2'/>" +
"</a>";
			assertEq("<a x='_'/>", parse(xd, xml, reporter));
			bos.close();
			assertNoErrors(reporter);
			bis = new ByteArrayInputStream(bos.toByteArray());
			el = KXmlUtils.parseXml(bis).getDocumentElement();
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete created temporary files
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