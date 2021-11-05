package test.xdef;

import test.XDTester;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.StringParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.w3c.dom.Element;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test003 extends XDTester {

	public Test003() {super();}

	static int _count = 0;
	boolean _myErrFlg;
	int _xx;
	public static boolean skodaTisice(final XXData xdata,final XDValue[]params){
		Test003 test2 = (Test003)xdata.getUserObject();
		if (test2 != null) {
			test2._xx++;
		}
		StringParser p = new StringParser(xdata.getTextValue());
		if (p.isInteger()) {
			if (p.eos()) {
				return true;
			}
			if (p.isChar(',')) {
				if (p.isInteger()) {
					if (p.eos()) {
						return true;
					}
				}
			}
		}
		xdata.error("XDEF532",
			"Error detected by external check method: &{msg}",
			"&{msg}skodaTisice: " + xdata.getTextValue());
		return false;
	}

	public static boolean rodneCislo(final XXData xdata,
		final XDValue[] params) {
		try {
			String value = xdata.getTextValue();
			int rok = Integer.parseInt(value.substring(0,2));
			int mesic = Integer.parseInt(value.substring(2,4));
			if ((mesic < 1) || (mesic > 12 && mesic < 51) || (mesic > 62)) {
				xdata.error("XDEF532",
					"Error detected by external check method: &{msg}",
					"&{msg}RC - mesic:" + mesic);
				return false;
			}
			int den = Integer.parseInt(value.substring(4,6));
			if ((den < 1) || (den > 31)) {
				xdata.error("XDEF532",
					"Error detected by external check method: &{msg}",
					"&{msg}RC - mesic:" + mesic);
				return false;
			}
			int i = 6;
			if (value.charAt(i) == '/') {
				i++;
				if (Character.toUpperCase(value.charAt(i)) == 'C') {
					return true; //cizinec
				}
			}
			int n = Integer.parseInt(value.substring(i));
			if (value.length() > i + 3) {
				int mod =  n % 10;
				int num = rok*10000000+mesic*100000+den*1000+(n/10);
				if (num % 11 != mod) {
					xdata.error("XDEF532",
						"Error detected by external check method: &{msg}",
						"&{msg}RC - CRC");
					return false;
				}
			}
			return true;
		} catch (Exception ex) {
			xdata.error("XDEF532",
				"Error detected by external check method: &{msg}",
				"&{msg}RC - format:" + xdata.getTextValue());
			return false;
		}
	}

	public static boolean skodaText(final XXData xdata, final XDValue[] params){
		if (xdata.getTextValue() != null) {
			return true;
		}
		String s = xdata.getElement().getAttribute("TisKc");
		if (s.length() > 0) {
			xdata.setTextValue("");
			return true;
		}
		return true;
	}

	public static boolean PlatnostPDN(final XDValue[] params) {return true;}

	public static  boolean blobref(final XDValue[] params)	{return true;}

	public static void myErr(XXNode chkElem, XDValue[] params) {
		Test003 test2 = (Test003) chkElem.getUserObject();
		if (test2 != null) {
			test2._myErrFlg = true;
		}
		if ("/SouborD1A/ZaznamPDN[54]/VyliceniDN[1]".equals(chkElem.getXPos())){
			chkElem.error(null,"OK Error myErr: " + chkElem.getXPos());
		} else  {
			chkElem.error(null, "Incorrect Error myErr: " + chkElem.getXPos());
			chkElem.error(null,"Error myErr again");
		}
	}

	public static void trString(final XDValue[] params) {}

	public static void konecZaznamu(final XDValue[] params)	{_count++;}

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
			assertNoErrors(reporter);
		} catch(Exception ex) {fail(ex);}
		try {// X-definition referred from XML
			reporter.clear();
			XDFactory.xparse(dataDir + "TestChkParser1.xml", reporter);
			assertNoErrors(reporter);
		} catch(Exception ex) {fail(ex);}
		try {
			String defFile = dataDir + "SouborD1A_.xdef";
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
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
			if (xd.getDocument().getXmlEncoding() != null
				&& !"UTF-8".equalsIgnoreCase(
				el.getOwnerDocument().getXmlEncoding())) {
				fail("encoding: " + xd.getDocument().getXmlEncoding());
			}
			isr = new InputStreamReader(
				new FileInputStream(dataDir + "SouborD1B.xml"));
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
		} catch (Exception ex) {fail(ex);}
		if (getFulltestMode()) {
			try { // test big XML
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
				// create big XML file
				//parse created file and get time of processing
				xd = xp.createXDDocument();
				File tempfile = File.createTempFile("bigxml", "xml");
				tempfile.deleteOnExit();
				xml = tempfile.getAbsolutePath();
				FileOutputStream longfile = new FileOutputStream(xml);
				longfile.write("<koně>\r\n".getBytes("UTF-8"));
				long num = 60000; // 15 Mbytes
				for (int i = 0; i < num; i++) {
					longfile.write(child);
				}
				longfile.write("</koně>\r\n".getBytes("UTF-8"));
				longfile.close();
				long datalen = tempfile.length();
				long t = System.currentTimeMillis();
				xd.xparse(xml, null);
				float duration =
					((float)((System.currentTimeMillis() - t) / 1000.0));
				DecimalFormat df = new DecimalFormat("0.00");
				setResultInfo("Big XML: "
					+ df.format(((float) datalen / 1000.0))
					+ "KB/" + df.format(duration)
					+ "s (" + df.format((datalen / 1000.0)/duration)+"KB/s);");
			} catch (Exception ex) {fail(ex);}
		}
		try {// check compiling if source items have assignment of sourceId
			Object[] p1 = new Object[] { // sources
"<xd:def  xmlns:xd='" + _xdNS + "' root='A' name='A' ><A/></xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' root='B' name='B' ><B/></xd:def>",
			new ByteArrayInputStream((
"<xd:def xmlns:xd='" + _xdNS + "' root='C' name='C' ><C/></xd:def>").getBytes(
				"UTF-8"))};
			String[] p2 = new String[] {"AA", "AB", "AC"}; // source names
			xp = XDFactory.compileXD(null, p1, p2);
			assertEq(xml = "<A/>", parse(xp, "A", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml = "<B/>", parse(xp, "B", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml = "<C/>", parse(xp, "C", xml, reporter));
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