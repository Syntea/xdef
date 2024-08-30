package test.xdef;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDXmlOutStream;
import org.w3c.dom.Element;
import org.xdef.XDTools;
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

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
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='books'>\n"+
"  <books>\n"+
"    <book xd:script='+; forget' ISBN='int'>\n"+
"      <title>string</title>\n"+
"      <edited xd:script='?'>gYear</edited>\n"+
"      <author xd:script='*'>string</author>\n"+
"    </book>\n"+
"  </books>\n"+
"</xd:def>";
			xml =
"<books>\n"+
"  <book ISBN='123456789'>\n"+
"     <title>Tutorial</title>\n"+
"     <edited>2016</edited>\n"+
"     <author>John B. Brown</author>\n"+
"     <author>Peter Smith</author>\n"+
"  </book>\n"+
"  <book ISBN='123456789'>\n"+
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
			xmlWriter.closeStream();
			assertNoErrorwarnings(reporter);
			bis = new ByteArrayInputStream(bos.toByteArray());
			assertEq(xml, KXmlUtils.parseXml(bis).getDocumentElement());
			assertEq("<books/>", el);

			xdef = // Test XmlOutStream methods
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String xmlFile;\n"+
"    XmlOutStream x = new XmlOutStream(xmlFile,'UTF-8',true);\n"+
"  </xd:declaration>\n"+
"  <a x=';' xd:script='onStartElement x.writeElementStart();\n"+
"             finally {x.writeElementEnd(); x.close();}'>\n"+
"    <b x=';' xd:script='*; onStartElement x.writeElementStart();\n"+
"               finally x.writeElementEnd(); forget'>\n"+
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
			File xmlFile = new File(clearTempDir(), "x.xml");
			xd.setVariable("xmlFile", xmlFile.getCanonicalPath());
			assertEq("<a x='_'/>", parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			el = KXmlUtils.parseXml(xmlFile).getDocumentElement();
			assertEq(xml, el);
			xdef = // Test XmlOutStream methods
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> external XmlOutStream x; </xd:declaration>\n"+
"  <a x='' xd:script='onStartElement x.writeElementStart();\n"+
"            finally {x.writeElementEnd(); x.close();}'>\n"+
"    <b x='' xd:script='*; finally x.writeElement(); forget'>\n"+
"      <c xd:script='*;'>string();</c>\n"+
"    </b>\n"+
"    <d y='?' xd:script='*; finally x.writeElement(); forget'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
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
			assertNoErrorwarnings(reporter);
			bis = new ByteArrayInputStream(bos.toByteArray());
			el = KXmlUtils.parseXml(bis).getDocumentElement();
			assertEq(xml, el);
		} catch (IOException ex) {fail(ex);}

		resetTester();
		clearTempDir(); // delete created temporary files
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}