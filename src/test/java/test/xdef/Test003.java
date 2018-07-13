/*
 * File: Test002.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test.xdef;

import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXData;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.FileReportReader;
import cz.syntea.xdef.sys.FileReportWriter;
import cz.syntea.xdef.sys.ReportPrinter;
import cz.syntea.xdef.sys.ReportReader;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileInputStream;
import org.w3c.dom.Element;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import org.w3c.dom.Document;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test003 extends Tester {
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

	public Test003() {
		super();
/*#if DEBUG*/
		setChkSyntax(true);
		setGenObjFile(true);
/*#end*/
	}

	@Override
	/** Run tests. */
	final public void test() {
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
		String tempDir = getTempDir();
		String durationInfo = "";
		Report.setLanguage("en"); //localize
/*#if DEBUG*/
		try {
			xml = getDataDir() + "long.xml.big";
			FileOutputStream longfile = new FileOutputStream(xml);
			longfile.write(
				"<?xml version = \"1.0\" encoding = \"windows-1250\"?>\r\n".
				getBytes("windows-1250"));
			longfile.write("<koně>\r\n".getBytes("windows-1250"));
			byte[] child = (
"  <kůň jaký = \"úplně šílený nóbl žluťoučký kůň\"\r\n" +
"    kde = \"louže\"\r\n" +
"    barva = \"žluťoučký\"\r\n" +
"    co = \"kůň\"\r\n" +
"    nějaký = \"nóbl\">\r\n" +
"     <kam>daleko</kam>\n " +
"     skákal přes louže\n " +
"     <proč>jen tak</proč>\n " +
" </kůň>\r\n").getBytes("windows-1250");
			long num = 50000; // 10 Mbytes
			for (int i = 0; i < num; i++) {
				longfile.write(child);
			}
			longfile.write("</koně>\r\n".getBytes("windows-1250"));
			longfile.close();
			xdef =
"<xd:def xmlns:xd='" + test.xdef.Tester.XDEFNS + "' xd:root=\"koně\">\n"+
"\n"+
"  <koně>\n"+
"    <kůň xd:script = \"occurs *; forget\"\n" +
"      jaký = \"eq('úplně šílený nóbl žluťoučký kůň')\"\n" +
"      kde = \"string\"\n" +
"      barva = \"an\"\n" +
"      co = \"string(3)\"\n" +
"      nějaký = \"string(4)\">\n" +
"      <kam>pic('AAAAAA')</kam>\n" +
"       string(10,999); fixed 'skákal přes louže'\n" +
"      <proč>string(7,%pattern=['j.*'])</proč>\n" +
"    </kůň>\n"+
"  </koně>\n"+
"\n"+
"</xd:def>";
			xp = compile(xdef);
			//parse created file and get time of processing
			xd = xp.createXDDocument();
			long t = System.currentTimeMillis();
			xd.xparse(xml, null);
			float duration=((float)((System.currentTimeMillis() - t) / 1000.0));
			DecimalFormat df = new DecimalFormat("0.00");
			long datalen = new File(xml).length();
			durationInfo = "Big XML: "
				+ df.format(((float) datalen / 1000.0))
				+ "KB/" + df.format(duration)
				+ "s (" + df.format((datalen / 1000.0)/duration) + "KB/s);";
			setResultInfo(durationInfo);
		} catch (Exception ex) {fail(ex);}
/*#end*/
		try {
			reporter.clear();
			xp = compile(dataDir + "TestChkParser1_1.xdef");
			xd = xp.createXDDocument("CKP");
			xd.xparse(new File(dataDir + "TestChkParser1_1.xml"), reporter);
			assertNoErrors(reporter);
		} catch(Exception ex) {fail(ex);}
		try {
			// X-definition referred from XML
			reporter.clear();
			XDFactory.xparse(dataDir + "TestChkParser1.xml", reporter);
			assertNoErrors(reporter);
		} catch(Exception ex) {fail(ex);}
		try {
			_count = 0;
			long t;
			float duration;
			int count;
			DecimalFormat df = new DecimalFormat("0.00");
			df.setDecimalSeparatorAlwaysShown(true);
			ReportReader rr;
			Report report1,report2;
			Document doc;
			OutputStreamWriter lst;
			FileReader fr;
			t = System.currentTimeMillis();
//			setProperty("xdef.externalmode", "both");
			xp = compile(dataDir + "SouborD1A.xdef");
//			DisplayCode.displayXArchive(xPool);
			duration= (float) ((System.currentTimeMillis() - t) / 1000.0);
			durationInfo += "D1A: compile "
				+ df.format(duration) + "s";
			fw = new FileOutputStream(tempDir + "SouborD1A.err");
			rw = new FileReportWriter(fw);
			xd = xp.createXDDocument("SouborD1A");
			if (!"29.5.2003".equals(xd.getImplProperty("date"))) {
				fail(xd.getImplProperty("date"));
			}
			if (!"1.0.0".equals(xd.getImplProperty("version"))) {
				fail(xd.getImplProperty("version"));
			}
			xd.setUserObject(this);
			t = System.currentTimeMillis();
			xd.xparse(dataDir + "SouborD1A.xml", rw);
			duration = (float)((System.currentTimeMillis() - t) / 1000.0);
			if (!"windows-1250".equalsIgnoreCase(
				xd.getDocument().getXmlEncoding())) {
				fail("encoding: " + xd.getDocument().getXmlEncoding());
			}
			long datalen = new File(dataDir + "SouborD1A.xml").length();
			durationInfo += "; data "+ df.format(((float) datalen / 1000.0)) +
				"KB/" + df.format(duration) +
				"s (" + df.format((datalen / 1000.0)/duration) + "KB/s);";
			isr = new InputStreamReader(new FileInputStream(
				dataDir + "SouborD1A.xml"));
			fw.close();
			fr = new FileReader(tempDir + "SouborD1A.err");
			rr = new FileReportReader(fr, true);
			lst = new OutputStreamWriter(
				new FileOutputStream(tempDir + "SouborD1A.lst"));
			ReportPrinter.printListing(lst, isr, rr, null, 80, false, null);
			fr.close();
			isr.close();
			lst.close();
			assertTrue(rw.getErrorCount() == 361 && _count == 221,
				"expected errors/count: 361/221, found " +
					"detected: " + rw.getErrorCount() + "/" + _count);
			assertTrue(_myErrFlg, "myErr not invoked");
			assertEq(_xx, 673, "nonstatic result 673/" + _xx);
			// Same data, this time called as validation
			_xx = 0;
			_myErrFlg = false;
			_count = 0;
			doc = KXmlUtils.parseXml(dataDir + "SouborD1A.xml", false);
			String encoding = doc.getXmlEncoding();
			if (!"windows-1250".equalsIgnoreCase(encoding)) {
				fail("encoding: " + encoding);
			}
			reporter = new ArrayReporter();
			xd = xp.createXDDocument("SouborD1A");
			xd.setUserObject(this);
			xd.xparse(doc, reporter);
			fr = new FileReader(tempDir + "SouborD1A.err");
			rr = new FileReportReader(fr, true);
			count = 0;
			while ((report1 = reporter.getReport()) != null) {
				report2 = rr.getReport();
				if (report2 == null ||
					(report1.getMsgID() != report2.getMsgID() &&
						(report1.getMsgID() == null ||
					!report1.getMsgID().equals(report2.getMsgID())))) {
					fail("Report " + count + "\nparser:     " +
						(report2 == null ? "null" : report2.toString())+ "\n" +
						"validation: " + report1.toString());
					break;
				}
				count++;
			}
			if (!_myErrFlg) {
				fail("myErr not invoked");
			}
			if ( _xx != 673) {
				fail("nonstatic result 673/" + _xx);
			}
			fr.close();
		} catch (Exception ex) {fail(ex);}
		try {
			String defFile = dataDir + "SouborD1A_.xdef";
			setProperty("xdef.externalmode", "both");
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			xp = compile(defFile);
			fw = new FileOutputStream(tempDir + "SouborD1A.err");
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
			fr = new FileReader(tempDir + "SouborD1A.err");
			rr = new FileReportReader(fr, true);
			lst = new OutputStreamWriter(
				new FileOutputStream(tempDir + "SouborD1A.lst"));
			ReportPrinter.printListing(lst, isr, rr, null, 80, false, null);
			fr.close();
			isr.close();
			lst.close();
//			doc = KXmlUtils.parseXml(dataDir + "SouborD1A.xml", false);
//			String encoding = doc.getXmlEncoding();
//			if (!"windows-1250".equalsIgnoreCase(encoding)) {
//				fail("encoding: " + encoding);
//			}
//			reporter = new ArrayReporter();
//			xd = xp.createXDDocument("SouborD1A");
//			xd.xparse(doc, reporter);
//			fr = new FileReader(tempDir + "SouborD1A.err");
//			rr = new FileReportReader(fr, true);
//			count = 0;
//			while ((report1 = reporter.getReport()) != null) {
//				report2 = rr.getReport();
//				if (report2 == null ||
//					(report1.getMsgID() != report2.getMsgID() &&
//					(report1.getMsgID() == null ||
//					!report1.getMsgID().equals(report2.getMsgID())))) {
//					fail("Report " + count + "\nparser: " +
//						(report2 == null ? "null" : report2.toString())+ "\n" +
//						"validation: " + report1.toString());
//					break;
//				}
//				count++;
//			}
//			fr.close();
		} catch (Exception ex) {fail(ex);}
		setResultInfo(durationInfo);
		if (getFailCount() == 0) {
			try {
				FUtils.deleteAll(tempDir, true);
			} catch (Exception ex) {fail(ex);}
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
