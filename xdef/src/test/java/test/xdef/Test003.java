package test.xdef;

import test.XDTester;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.w3c.dom.Element;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import static test.XDTester._xdNS;
import static test.XDTester.getFulltestMode;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test003 extends XDTester {
	public Test003() {super();}

	@Override
	/** Run tests. */
	public void test() {
		String xdef;
		String xml;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		FileOutputStream fw;
		ReportWriter rw;
		InputStreamReader isr;
		Element el;
		final String dataDir = getDataDir() + "test/";
		try {
			reporter.clear();
			xp = compile(dataDir + "TestChkParser1_1.xdef");
			xd = xp.createXDDocument("CKP");
			xd.xparse(new File(dataDir + "TestChkParser1_1.xml"), reporter);
			assertNoErrorwarnings(reporter);
		} catch(RuntimeException ex) {fail(ex);}
		try {// Xdefinition referred from XML
			reporter.clear();
			XDFactory.xparse(dataDir + "TestChkParser1.xml", reporter);
			assertNoErrorwarnings(reporter);
		} catch(RuntimeException ex) {fail(ex);}
		try {
			String defFile = dataDir + "SouborD1A_.xdef";
			setProperty(XDConstants.XDPROPERTY_XINCLUDE, XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			xp = compile(defFile);
			File tmp1 = File.createTempFile("SouborD1A", "err");
			tmp1.deleteOnExit();
			fw = new FileOutputStream(tmp1);
			rw = new FileReportWriter(fw);
			xd = xp.createXDDocument("SouborD1A");
			if (!"29.5.2003".equals(xd.getImplProperty("date"))) {
				fail(xd.getImplProperty("date"));
			}
			if (!"1.0.0".equals(xd.getImplProperty("version"))) {
				fail(xd.getImplProperty("version"));
			}
			xd.setUserObject(this);
			el = xd.xparse(dataDir + "SouborD1D.xml", rw);
			if (null != xd.getDocument().getXmlEncoding() && !"UTF-8".equalsIgnoreCase(
				el.getOwnerDocument().getXmlEncoding())) {
				fail("encoding: " + xd.getDocument().getXmlEncoding());
			}
			isr = new InputStreamReader(new FileInputStream(dataDir + "SouborD1B.xml"));
			fw.close();
			OutputStreamWriter lst;
			FileReader fr;
			ReportReader rr;
			File tmp2 = File.createTempFile("SouborD1A", "err");
			tmp2.deleteOnExit();
			fr = new FileReader(tmp2);
			rr = new FileReportReader(fr, true);
			File tmp3 = File.createTempFile("SouborD1A", "lst");
			tmp3.deleteOnExit();
			lst = new OutputStreamWriter(new FileOutputStream(tmp3));
			ReportPrinter.printListing(lst, isr, rr, null, 80, false, null);
			fr.close();
			isr.close();
			lst.close();
		} catch (IOException | RuntimeException ex) {fail(ex);}
		try {// check compiling if source items have assignment of sourceId
			Object[] p1 = new Object[] { // sources
"<xd:def  xmlns:xd='" + _xdNS + "' root='A' name='A' ><A/></xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' root='B' name='B' ><B/></xd:def>",
			new ByteArrayInputStream((
"<xd:def xmlns:xd='" + _xdNS + "' root='C' name='C' ><C/></xd:def>").getBytes("UTF-8"))};
			String[] p2 = new String[] {"AA", "AB", "AC"}; // source names
			xp = XDFactory.compileXD(null, p1, p2);
			assertEq(xml = "<A/>", parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml = "<B/>", parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml = "<C/>", parse(xp, "C", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (UnsupportedEncodingException | RuntimeException ex) {fail(ex);}
		try {// test URL with encoded chracters.
			File f = new File(clearTempDir(), "/aa bb");
			f.mkdirs();
			f = new File(f, "čé řž.xdef");
			Writer wr = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			wr.write(
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"<test a='int()'/>\n" +
"</xd:def>");
			wr.close();
			URL u = f.toURI().toURL();
			XDFactory.compileXD(null, u);
			xdef = "<xd:collection xmlns:xd='" + _xdNS + "' include='"+u.toExternalForm()+"' />";
			xp = compile(xdef);

			xml = "<test a='123'/>";
			f = new File(clearTempDir(), "/aa bb");
			f.mkdirs();
			f = new File(f, "čé řž.xml");
			wr = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			wr.write(xml);
			wr.close();
			xp.createXDDocument("A");
			assertEq(xml, parse(xp,"A",xml, reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument("A");
			assertEq(xml, xd.xparse(f.toURI().toURL(), reporter));
			assertNoErrorsAndClear(reporter);
		} catch (IOException | RuntimeException ex) {fail(ex);}
		try {// join elements "B" which can be joined.
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"  /** Test if the element f can be joined with the element e. */\n" +
"  boolean join(Element e, Element f, String a, String b, String mask) {\n"+
"    Datetime t1 = new Datetime(e.getAttribute(b));\n"+
"    if (mask.endsWith(\"d\")) {\n" +
"      t1 = t1.addDay(1);\n" +
"    } else if (mask.endsWith(\"H\")) {\n" +
"      t1 = t1.addHour(1);\n" +
"    } else if (mask.endsWith(\"s\")) {\n" +
"      t1 = t1.addSecond(1);\n" +
"    } else {\n" +
"       return false; /*can't join*/\n" +
"    }\n" +
"    Datetime t2 = new Datetime(f.getAttribute(a));\n"+
"    Container eatrs = e.getAttributes();\n" +
"    Container fatrs = f.getAttributes();\n" +
"    if (!t1.equals(t2) || eatrs.getLength() != fatrs.getLength()\n" +
"      || !e.hasAttribute(a) || !f.hasAttribute(a)\n" +
"      || !e.hasAttribute(b) || !f.hasAttribute(b)) {\n" +
"      return false; /*can't join*/\n"+
"    }\n"+
"    for (int i = 0; i LT eatrs.getLength(); i++) {\n" +
"      String name = ((NamedValue) eatrs.item(i)).getName();\n" +
"      if (!a.equals(name) AAND !b.equals(name) AAND\n" +
"		 !e.getAttribute(name).equals(f.getAttribute(name)))\n" +
"		 return false; /*can't join*/\n" +
"    }\n" +
"    return true;\n" +
"  }\n"+
"  /** Create list of joined elements. */\n" +
"  Container x(Container x, String a, String b, String mask) {\n"+
"    Container y = new Container();\n" +
"    if (!x.isEmpty()) {\n" +
"      Element e = x.getElement(0);\n" +
"      y.addItem(e);\n" +
"      for (int i = 1; i LT x.getLength(); i++) {\n" +
"        Element f = x.getElement(i);\n" +
"        if (join(e, f, a, b, mask)) {\n" +
"          e.setAttribute(b, f.getAttribute(b));\n" +
"        } else {\n" +
"          y.addItem(e = f);\n" +
"        }\n" +
"      }\n" +
"    }\n" +
"    return y;\n" +
"  }\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <B xd:script=\"occurs *; create x(from('//B'), 'x', 'y', 'yyyy-MM-dd');\"\n"+
"    a='string' b='string'\n"+
"    x=\"xdatetime('yyyy-MM-dd')\" y=\"xdatetime('yyyy-MM-dd')\"/>\n"+
"</A>\n"+
"</xd:def>";
		xml =
"<A>\n"+
"  <B a='a' b='b' x='2023-12-08' y='2023-12-31'/>\n"+
"  <B a='a' b='b' x='2024-01-01' y='2024-09-11'/>\n"+
"  <B a='a' b='b' x='2024-09-12' y='2024-09-13'/>\n"+
"  <B a='a' b='b' x='2024-09-20' y='2024-09-30'/>\n"+
"  <B a='a' b='b' x='2024-10-01' y='2024-10-02'/>\n"+
"</A>";
			xd = compile(xdef).createXDDocument();
			xd.setXDContext(xml);
			el = xd.xcreate("A", null);
			KXmlUtils.compareElements(el,
"<A>" +
"<B a=\"a\" b=\"b\" x=\"2023-12-08\" y=\"2024-09-13\"/>" +
"<B a=\"a\" b=\"b\" x=\"2024-09-20\" y=\"2024-10-02\"/>" +
"</A>").checkAndThrowErrorWarnings();
			assertEq(el,
"<A>\n" +
"  <B a=\"a\" b=\"b\" x=\"2023-12-08\" y=\"2024-09-13\"/>\n" +
"  <B a=\"a\" b=\"b\" x=\"2024-09-20\" y=\"2024-10-02\"/>\n" +
"</A>");
		} catch (RuntimeException ex) {fail(ex);}
		 // test big XML
		if (getFulltestMode()) {
			try {
				xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"koně\">\n"+
"  <koně>\n"+
"    <kůň xd:script = '*; forget'\n" +
"      jaký = \"eq('úplně šílený nóbl žluťoučký kůň')\"\n" +
"      kde = 'string'\n" +
"      barva = 'an'\n" +
"      co = 'string(3)'\n" +
"      nějaký = 'string(4)'>\n" +
"      <kam>string(%pattern='\\\\D{6}')</kam>\n" +
"       string(10,999); fixed 'skákal přes louže'\n" +
"      <proč>string(7,%pattern=['j.*'])</proč>\n" +
"    </kůň>\n"+
"  </koně>\n"+
"</xd:def>";
				xp = compile(xdef);
				byte[] child = (
"  <kůň jaký = \"úplně šílený nóbl žluťoučký kůň\"\r\n" +
"    kde = \"louže\"\r\n" +
"    barva = \"žluťoučký\"\r\n" +
"    co = \"kůň\"\r\n" +
"    nějaký = \"nóbl\">\r\n" +
"     <kam>daleko</kam>\n " +
"     skákal přes louže\n " +
"     <proč>jen tak</proč>\n " +
" </kůň>\r\n").getBytes("UTF-8");
				// Create big XML file. Parse created file and get time of processing
				xd = xp.createXDDocument();
				File tempfile = File.createTempFile("bigxml", "xml");
				tempfile.deleteOnExit();
				xml = tempfile.getAbsolutePath();
				try (FileOutputStream longfile = new FileOutputStream(xml)) {
					longfile.write("<koně>\r\n".getBytes("UTF-8"));
					long num = 60000; // 15 Mbytes
					for (int i = 0; i < num; i++) {
						longfile.write(child);
					}
					longfile.write("</koně>\r\n".getBytes("UTF-8"));
				}
				long datalen = tempfile.length();
				long t = System.currentTimeMillis();
				xd.xparse(xml, null);
				float duration = ((float)((System.currentTimeMillis() - t) / 1000.0));
				DecimalFormat df = new DecimalFormat("0.00");
				setResultInfo("Big XML: " + df.format(((float) datalen / 1000.0)) + "KB/" +df.format(duration)
					+ "s (" + df.format((datalen / 1000.0)/duration)+"KB/s);");
			} catch (IOException | RuntimeException ex) {fail(ex);}
		}

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