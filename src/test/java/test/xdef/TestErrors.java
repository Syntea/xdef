/*
 * File: TestErrors.java
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

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportPrinter;
import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.util.Properties;
import cz.syntea.xdef.sys.ReportReader;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SUtils;
import test.utils.XDTester;

/** Test of reporting of script errors.
 * @author Vaclav Trojan
 */
public final class TestErrors extends XDTester {

	public TestErrors() {super(); setChkSyntax(false);}

	private void printReport(final Report report, final String data) {
		System.out.flush();
		System.err.flush();
		fail(new Throwable(
			report.toString() + "; '" + report.getModification() + "'"));
		System.err.flush();
		System.out.flush();
		ArrayReporter reporter = new ArrayReporter();
		reporter.putReport(report);
		printReports(reporter, data);
	}

	private static String chkReport(final ReportReader reporter,
		final String id,
		final String line,
		final String column,
		final String source) {
		return chkReport(reporter.getReport(), id, line, column, source);
	}

	private static String chkReport(final Report report,
		final String id,
		final String line,
		final String column,
		final String source) {
		if (report == null) {
			return "report missing";
		} else {
			boolean err = id == null ?  false : !id.equals(report.getMsgID());
			if (report.getModification() != null) {
				err |= line != null &&
					report.getModification().indexOf("&{line}" + line) < 0;
				err |= column != null &&
					report.getModification().indexOf("&{column}" + column) < 0;
				err |= source != null &&
					report.getModification().indexOf(source) < 0;
			}
			if (err) {
				return report.toString() + "; '" + report.getModification()+"'";
			}
		}
		return "";
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
			XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);
		try {
			test1();
		} catch (Error ex) {
			fail(ex);
		}
		resetTester();
	}

	/** Compile the source.
	 * @param props properties connected to compilation.
	 * @param xdef source of XDefinition.
	 * @return reporter connected to compilation.
	 */
	private ArrayReporter test(final Properties props, final String xdef) {
		return test(props, new String[] {xdef});
	}

	/** Compile the source.
	 * @param props properties connected to compilation.
	 * @param xdef array of sources wit XDefinitions.
	 * @param cls array with external classes.
	 * @return reporter connected to compilation.
	 */
	private ArrayReporter test(final Properties props,
		final String[] xdef,
		final Class<?>... cls) {
			ArrayReporter rw = new ArrayReporter();
			XDBuilder xb = XDFactory.getXDBuilder(props);
			xb.setExternals(cls);
			xb.setReporter(rw);
			xb.setSource(xdef);
			xb.compileXD();
			return rw;
	}

	private void test1() {
		XDBuilder xb;
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		props.setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
			XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);
		Report.setLanguage("en"); //localize

		final String dataDir = getDataDir();
		int count, pos;
		XDPool xp;
		String xdef;
		String xml;
		String s;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		Report report;
		ReportWriter rw;
		StringWriter strw;
		CharArrayWriter caw;
		try {// check of error reporting - script in value is not valid
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd = '" + XDEFNS + "'\n"+					//01
"        name  = 'SouborY1A' root = 'SouborY1A'>\n"+		//02
" <SouborY1A\n"+											//03
"    Davka=\"This is not script!\"\n"+						//04 <=
"    ZeDne=\"required\" > \n"+								//05
//"\n"+														//06
" This is not script!\n"+									//06 <=
" </SouborY1A>\n"+											//07
"</xd:def>\n";												//08
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "4", "17", null));
			assertEq("", chkReport(reporter, "XDEF410", "4", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "6", "7", null));
			assertEq("", chkReport(reporter, "XDEF410", "6", "7", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {// check of error reporting - low number of parameters of method
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd ='" + XDEFNS + "'\n"+					//01
" xd:name ='a' xd:root = 'a' >\n"+						//02
"<a a = 'optional xdatetime()' >\n"+					//03 <=
"</a>\n"+												//04
"</xd:def>\n";											//05
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, null,"3", "2"/*"27"*/, null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {// check of error reporting - high number of parameters of method
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd ='" + XDEFNS + "'\n"+					//01
" xd:name ='a' xd:root = 'a' >\n"+						//02
"<a a = 'optional string(1,2,3)' >\n"+					//03 <=
"</a>\n"+												//04
"</xd:def>\n";											//05
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF461", "3", "30", null));
			assertEq("", chkReport(reporter, "XDEF465", "3", "30", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		//test of error reporting - replacement in attribute value (all new
		// lines in parsed data are replaced by the space character!).
		try {
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<!DOCTYPE xd:collection [<!ENTITY ent \"abcd\">]>\n"+				//01
"<xd:collection xmlns:xd='" + XDEFNS + "'>\n"+						//02
"<xd:def xd:name  ='Q_SCN'\n"+										//03
"          xd:root  ='*'\n"+										//04
"          xd:script='\n"+											//05
"\n"+																//06
"options ppppp,    &#x0065;&#x0066;, &ent;, xxxxx,\n"+				//07  <=
"y' />\n"+															//08  <=
"</xd:collection>\n";												//09
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF433", "7", "9", null));
			assertEq("", chkReport(reporter, "XDEF433", "7", "19", null));
			assertEq("", chkReport(reporter, "XDEF433", "7", "37", null));
			assertEq("", chkReport(reporter, "XDEF433", "7", "44", null));
			assertEq("", chkReport(reporter, "XDEF433", "8", "1", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:collection xmlns:xd='" + XDEFNS + "'>\n"+							//01
"<xd:def xd:name  ='Q_SCN'\n"+											//02
"          xd:root  ='*'\n"+											//03
"          xd:script='options &#x0009; ignoreEmptyAttributex' />\n"+	//04 <=
"</xd:collection>\n";													//05
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF433", "4", "39", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+							//01
"     xd:root='root'\n"+										//02
"     xd:name='test'>\n"+										//03
"   <xd:declaration>\n"+										//04
"<![CDATA[\n"+													//05
"  void test() {\n"+											//06
"    int i=1; String s='';\n"+									//07
"\n"+															//08
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//09 <==
"\n"+															//10
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//11 <==
"]]>\n"+														//12
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//13 <==
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//14 <==
"  }\n"+														//15
"   </xd:declaration>\n"+										//16
"   <root>\n"+													//17
"      <a xd:script='occurs 1..; finally test();' />\n"+		//18
"   </root>\n"+													//19
"</xd:def>\n";													//20
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "9", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "11", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "13", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "14", "18", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<!DOCTYPE doc [<!ENTITY e '&#10;entity_e&#10;'>]>\n"+		// 01
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/2.0'\n"+		// 02
"  root='A'>\n"+											// 03
" <A\n"+													// 04
"  a='fixed a+\"&lt;,\n"+									// 05 <==
"&#x3e;\"+b;'\n"+											// 06 <==
"  b='fixed c+\" \" + &e; + d'>\n"+							// 07 <==
"  finally out('&#60;'+e+'&#62;x<![CDATA[['+f+'\n"+			// 08 <==
"]]<>'+g+']]>\n"+											// 09 <==
"  x&lt;'+h+'&gt;'+i+'');\n"+								// 10 <==
"fixed 'abc'+j+'d'+ &e; + '.';<a>fixed 'abc'</a>\n"+			// 11 <==
" </A>\n"+													// 12
"</xd:def>";												// 13
			s = "\\n";
			for (int i = 0; i < 2; i++) {
				reporter = test(props, xdef);
				assertEq("", chkReport(reporter, "XDEF424", "5", "13", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "6", "10", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "7", "13", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "7", "24", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "7", "26", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "8", "24", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "8", "45", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "9", "8", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "10", "8", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "10", "20",null),s);
				assertEq("", chkReport(reporter, "XDEF424", "11", "14",null),s);
				assertEq("", chkReport(reporter, "XDEF424", "11", "24",null),s);
				assertNull(reporter.getReport(), reporter.printToString());
				reporter.clear();
				s = "\\r\\n";
				xdef = SUtils.modifyString(xdef, "\n", "\r\n");
			}
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+							//01
"     xd:root='root'\n"+										//02
"     xd:name='test'>\n"+										//03
"   <xd:declaration>\n"+										//04
"  void test() {\n"+											//05
"    int i=1; String s='';\n"+									//06
"\n"+															//07
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//08 <=
"<![CDATA[t='1';\n"+											//09
"\n"+															//10
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//11 <=
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//12 <=
"]]>\n"+														//13
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//14 <=
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//15 <=
"  }\n"+														//19
"   </xd:declaration>\n"+										//15
"   <root>\n"+													//18
"      <a xd:script='occurs 1..; finally test();' />\n"+		//19
"   </root>\n"+													//20
"</xd:def>\n";													//21
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "8", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "9", "11", null));
			assertEq("", chkReport(reporter, "XDEF424", "11", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "14", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "15", "18", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+								//01
"     xd:root='root'\n"+											//02
"     xd:name='test'>\n"+											//03
"   <xd:macro name = 'mm'>if('1'==(t='1'))s='2';else t='3';</xd:macro>\n"+
"   <xd:macro name = 'n'>if('1' == (</xd:macro>\n"+					//05
"   <xd:declaration>void test() {int i=1; String s='';\n"+			//06
"    if('1' == (t='1')) s+=2; else t+=0;\n"+						//07 <==
"    ${n}t='1')) t+=2; else s+=0;\n"+								//08 <==
"    ${mm}if('1' == (t='1')) s+=2; else s+=0;\n"+					//09 <==
"    if('1' == (t='1')) s+=2; else s+=0;\n"+						//10 <==
"  }\n"+															//11
"   </xd:declaration>\n"+											//12
"   <root>\n"+														//13
"      <a xd:script=\"occurs 1..; finally test();\" />\n"+			//14
"   </root>\n"+														//15
"</xd:def>\n";														//16
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "7", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "7", "36", null));
			assertEq("", chkReport(reporter, "XDEF424", "8", "10", null));
			assertEq("", chkReport(reporter, "XDEF424", "8", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "9", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "9", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "9", "22", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "17", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //check ambiguous XDefinition error
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+					//01
"        xd:name = 'ambiguous_xdef' xd:root = 'a'>\n"+	//02
"<a>\n"+												//03
"  <b xd:script = 'occurs 0..1'/>\n"+					//04
"  <b xd:script = 'occurs 1..2'/>\n"+					//05 <==
"</a>\n"+												//06
"</xd:def>";											//07
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF235", "5", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+					//01
"        xd:name = 'ambiguous_xdef' xd:root = 'a'>\n"+	//02
"<a>\n"+												//03
"  <b xd:script = 'occurs 0..*'/>\n"+					//04
"  <b xd:script = 'occurs 0..1'/>\n"+					//05 <==
"</a>\n"+												//06
"</xd:def>";											//07
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter.getReport(), "XDEF238", "5", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='"+XDEFNS+"' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+															//02
"  <xd:text>required string()</xd:text>\n"+							//03
"  <xd:text>optional string()</xd:text>\n"+							//04 <==
"</a>\n"+															//05
"</xd:def>";														//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF239", "4", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='"+XDEFNS+"' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+															//02
"  required string();\n"+											//03
"  optional string();\n"+											//04 <==
"</a>\n"+															//05
"</xd:def>";														//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF422", "4", "3", null));
			assertEq("", chkReport(reporter, "XDEF422", "4", "12", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:collection>\n"+									//01 <==
"<xdef_de xd:name    = 'Q_SCN'\n"+						//02 <==
"          xd:root   = '*'\n"+							//03 <==
"          xd:script = '\n"+							//04 <==
"${mac#m} &#x0009;ignoreEmptyAttributes' />\n"+			//05 <==
"<xd:de xd:name  ='mac'>\n"+							//06 <==
"  <xd:macro name = 'm'>\n\noptions</xd:macro>\n"+		//07 <==
"</xd:def>\n"+											//08
"</xd:collection>\n"+									//09 <==
"";														//10 <==
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XML047", "1", null, null));
			assertEq("", chkReport(reporter, "XDEF256", "1", "2", null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XDEF255", "2", "2", null));
			assertEq("", chkReport(reporter, "XDEF259", "2", "2", null));
			assertEq("", chkReport(reporter, "XML047", "6", "2", null));
			assertEq("", chkReport(reporter, "XML047", "6", null, null));
			assertEq("", chkReport(reporter, "XDEF255", "6", "2", null));
			assertEq("", chkReport(reporter, "XDEF259", "6", "2", null));
			assertEq("", chkReport(reporter, "XDEF212", "6", "2", null));
			assertEq("", chkReport(reporter, "XML047", "7", null, null));
			assertEq("", chkReport(reporter, "XML075", "10", "8", null));
			assertEq("", chkReport(reporter, "XDEF483", "5", "8", null));
			assertEq("", chkReport(reporter, "XDEF254", "2", "24", null));
			assertEq("", chkReport(reporter, "XDEF254", "3", "24", null));
			assertEq("", chkReport(reporter, "XDEF254", "4", "24", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {//check declaration errors
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd = '" + XDEFNS + "' root = 'a'>\n"+				//01
"  <xd:declaration> int j = k; </xd:declaration>\n"+				//02
"  <xd:declaration>\n"+												//03
"    int k = j;\n"+													//04 <==
"  </xd:declaration>\n"+											//05
"  <a xd:script = \"finally if (k != 1) outln('error');\"/>\n"+		//06
"  <xd:declaration> k = 1; </xd:declaration>\n"+					//07 <==
"</xd:def>";														//08
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF487", "7", "22", null));
			assertEq("", chkReport(reporter, "XDEF424", "4", "14", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {//check declaration sequence independence and type error assignment
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd = '" + XDEFNS + "' root = 'a'>\n"+				//01
"  <xd:declaration> int j = k; </xd:declaration>\n"+				//02
"  <xd:declaration>\n"+												//03
"    int k = 'a'; int y = x, i = 0;\n"+								//04 <==
"    void h() {\n"+													//05
"      if (i != 0 || k != 'a' || x != 1 ||  y != 1) outln('error');\n"+//06 <==
"    }\n"+															//07
"  </xd:declaration>\n"+											//08
"  <a xd:script = \"finally {h(); if (k != 1) outln('error');}\"/>\n"+	//09
"  <xd:declaration> int x = true; </xd:declaration>\n"+				//10 <==
"</xd:def>";														//11
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF457", "10", "32", null));
			assertEq("", chkReport(reporter, "XDEF457", "4", "16", null));
			assertEq("", chkReport(reporter, "XDEF444", "6", "30", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test file names etc
			String fName1 = dataDir + "test/TestErrors1.xdef";
			String fName2 = dataDir + "test/TestErrors2.xdef";
			reporter = test(props, new String[]{fName1, fName2}, getClass());
			assertEq("", chkReport(reporter, "XDEF425", "10", "26", fName2));
			rep = reporter.getReport();
			if (!"".equals(chkReport(rep, "XDEF307", "4", "20", fName1))) {
				assertEq("", chkReport(rep, "XDEF307", "4", "20", fName2));
			}
			rep = reporter.getReport();
			if (!"".equals(chkReport(rep, "XDEF307", "4", "20", fName1))) {
				assertEq("", chkReport(rep, "XDEF307", "4", "20", fName2));
			}
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xdef:collection xmlns:xdef = '" + XDEFNS + "' >\n"+
"\n"+
"<xdef:def xdef:name = 'Example'\n"+
"          xdef:root = 'List' >\n"+
"\n"+
"<List>\n"+
"  <Employee Name = 'required string(1,30)'\n"+
"    FamilyName             = 'required string(1,30)'\n"+
"    Ingoing                = 'required xdatetime(\"yyyy-M-d\")'\n"+
"    Salary                 = 'required int(1000,100000)'\n"+
"    Qualification          = 'required string()'\n"+
"    SecondaryQualification = 'optional string()'\n"+
"    xdef:script            = 'occurs 0..' />\n"+
"\n"+
"</List>\n"+
"\n"+
"</xdef:def>\n"+
"</xdef:collection>";
			xp = XDFactory.compileXD(props, xdef);
			xml =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<List>\n"+														//01
"  <Employee Name                   = 'John'\n"+				//02
"            FamilyName             = 'Braun'\n"+				//03
"            Ingoing                = '2004-10-1'\n"+			//04
"            Salary                 = '2000' \n"+				//05
"            Qualification          = 'worker' />\n"+			//06
"\n"+															//07
"  <Employee Name                   = 'Mary'\n"+				//08
"            FamilyName             = 'White'\n"+				//09
"            Ingoing                = '1998-19-43'\n"+			//10 <==
"            Salary                 = 'abcd' \n"+				//11 <==
"            Qualification          = '' />\n"+					//12 <==
"\n"+															//13
"  <Employee Name                   = 'Peter'\n"+				//14
"            FamilyName             = 'Black'\n"+				//15
"            Ingoing                = '1998-2-13'\n"+			//16
"            Salary                 = '1600' \n"+				//17
"            SecondaryQualification = 'electrician' />\n"+		//18 <==
"\n"+															//19
"</List>";														//20
			parse(xp, "Example", xml, reporter);
			caw = new java.io.CharArrayWriter();
			ReportPrinter.printListing(
				caw,
				xml,
				reporter.getReportReader(),
				true);
			s = caw.toString();
			count = pos = 0;
			while ((pos = s.indexOf("\n ***** ", pos)) > 0) {
				int newPos = s.indexOf('|', pos += 8);
				if (newPos < 0) {
					fail(s);
					return;
				}
				String s1 = s.substring(pos, newPos);
				if (s1.length() == 0 || s1.trim().length() != 0) {
					fail(s);
					return;
				}
				pos = s.indexOf("E XDEF", newPos);
				if (pos < 0) {
					fail(s);
					return;
				}
				count++;
			}
			if (count < 3 || count > 4) {
				fail(s);
			}
			reporter.reset();
			assertEq("",chkReport(reporter, "XDEF809", "10", "39", null));
			assertEq("",chkReport(reporter, "XDEF809", "11", "39", null));
			assertEq("",chkReport(reporter, "XDEF814", "12", "39", null));
			assertEq("",chkReport(reporter, "XDEF526", "18", "54", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef = //check if error is only one
	"<xd:def xmlns:xd='" + XDEFNS + "' root='a' name ='a'>\n"+
	"<a>\n"+
	"  <xd:mixed>\n"+
	"    <x/>\n"+
	"    <y/>\n"+
	"  </xd:mixed>\n"+
	"</a>\n"+
	"</xd:def>";
			xp = compile(xdef);
			xml = "<a>0<x/><y/></a>";
			parse(xp, "a", xml, reporter);
			assertEq(1, reporter.getErrorCount());
			parse(xp, "a", xml, reporter);
			assertEq(1, reporter.getErrorCount());
			assertEq("", chkReport(reporter, "XDEF534", "1", "4", null));
			xml = "<a><x/>0<y/></a>";
			parse(xp, "a", xml, reporter);
			assertEq(1, reporter.getErrorCount());
			assertEq("", chkReport(reporter, "XDEF534", "1", "8", null));
			xml = "<a><x/><y/>0</a>";
			parse(xp, "a", xml, reporter);
			assertEq(1, reporter.getErrorCount());
			assertEq("", chkReport(reporter, "XDEF534", "1", "12", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:collection xmlns:xd = '" + XDEFNS + "' >\n"+
"\n"+
"<xd:def name='test' root='test'>\n"+
"\n"+
"<test p1 = 'required string()'\n"+
"      p2 = 'required string(2)'\n"+
"      p3 = 'required string(0,30)'\n"+
"      p4 = 'required string(0,30)'\n"+
"      p5 = 'required string(1,30)'\n"+
"      p6 = 'required int()'\n"+
"      p7 = 'required int(2)'\n"+
"      p8 = 'required int(2,3)'\n"+
"      p9 = 'required int(2,3)'/>\n"+
"\n"+
"</xd:def>\n"+
"</xd:collection>";
			xb = XDFactory.getXDBuilder(props);
			xb.setSource(xdef);
			xp = xb.compileXD();
			xml =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<test p1 = ''\n"+												//01 <==
"      p2 = 'xxx'\n"+											//02 <==
"      p3 = ''\n"+										        //03
"      p4 = 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzxx'\n"+	//04 <==
"      p5 = ''\n"+												//05 <==
"      p6 = 'z1234567890'\n"+									//06 <==
"      p7 = '0'\n"+												//07 <==
"      p8 = '1'\n"+												//08 <==
"      p9 = '4'/>\n";											//09 <==
			parse(xp, "test", xml, reporter);
			assertEq("", chkReport(reporter, "XDEF814", "1", "13", null));
			assertEq("", chkReport(reporter, "XDEF815", "2", "13", null));
			assertEq("", chkReport(reporter, "XDEF815", "4", "13", null));
			assertEq("", chkReport(reporter, "XDEF814", "5", "13", null));
			assertEq("", chkReport(reporter, "XDEF809", "6", "13", null));
			assertEq("", chkReport(reporter, "XDEF813", "7", "13", null));
			assertEq("", chkReport(reporter, "XDEF813", "8", "13", null));
			assertEq("", chkReport(reporter, "XDEF813", "9", "13", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"external method\n"+ //*
" void test.xdef.TestExtenalMethods.x(XXElement,, XDContainer, XDContainer);\n"+
"  Container p = null, q = null, r = null, s = null, t = null;\n"+
"</xd:declaration>\n"+
"  <a a = \"string; finally {x(p,q);}\">\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(null);
			xb.setReporter(reporter);
			xb.setSource(xml);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF412", "4", "48", null));
				assertEq("", chkReport(reporter, "XDEF443", "7", "34", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' name='a' root='root'>\n"+
"<root>\n"+
"  <A xd:script='2..3'/>\n"+
"  <A/>\n"+
"</root>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(null);
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF235", "4", "4", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {//test incorrect key parameter (see %0)
			xml =
"<xd:def xmlns:xd='" + XDEFNS + "' root = 'a'>\n"+
"<a a='decimal(%0,1)'/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(null);
			xb.setReporter(reporter);
			xb.setSource(xml);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF106", "2", "15", null));
				assertEq("", chkReport(reporter, "XDEF106", "2", "18", null));
				assertEq("", chkReport(reporter, "XDEF410", "2", "18", null));
				assertEq("", chkReport(reporter, "XDEF410", "2", "18", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {
			xml = // check illegal text
"<x:def xmlns:x ='" + XDEFNS + "' root ='a'>\n"+
"<a a = \"required eq('a')\">\n"+
"here is an illegal text\n"+
"</a>\n"+
"</x:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(null);
			xb.setReporter(reporter);
			xb.setSource(xml);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF424", "3", "6", null));
				assertEq("", chkReport(reporter, "XDEF410", "3", "6", null));
				assertEq("", chkReport(reporter, "XDEF422", "3", "12", null));
				assertEq("", chkReport(reporter, "XDEF422", "3", "20", null));
				assertEq("", chkReport(reporter, "XDEF424", "3", "20", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+							//01
"     xd:root='root'\n"+										//02
"     xd:name='test'>\n"+										//03
" <xd:macro name='test'>\n"+									//04
"    int i=1; String t='';\n"+									//05
"    if('1' == (s='1')) s+=2; else s+=0;\n"+					//06
"    if ('12' == s) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//07
"</xd:macro>\n"+												//08
"   <xd:declaration>\n"+										//09
"  void test() {\n"+											//10
"    ;\n"+														//11
"    ${test}" +													//12 <=
"  }\n"+														//13
"   </xd:declaration>\n"+										//14
"   <root>\n"+													//15
"      <a xd:script='occurs 1..; finally test();' />\n"+		//16
"   </root>\n"+													//17
"</xd:def>\n";													//18
			reporter = test(props, new String[] {xdef}, getClass());
			strw = new StringWriter();
			ReportPrinter.printListing(
				strw, new java.io.StringReader(xdef), reporter, true);
			pos = 0;
			s = strw.toString();
			if ((pos = s.indexOf("\n ***** ", pos)) > 0) {
				if (!s.startsWith("\n *****      |", pos)) {
					fail(s);
				}
			} else {
				fail(s);
			}
			reporter.reset();
			assertEq("", chkReport(reporter, "XDEF424", "12", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "5", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef = //macro starts now at new line
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+							//01
"     xd:root='root'\n"+										//02
"     xd:name='test'>\n"+										//03
" <xd:macro name='test'>\n"+									//04
"    int i=1; String t='';\n"+									//05
"    if('1' == (s='1')) s+=2; else s+=0;\n"+					//06
"    if ('12' == s) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//07
"</xd:macro>\n"+												//08
"   <xd:declaration>\n"+										//09
"  void test() {\n"+											//10
"    ;\n"+														//11
"${test}" +														//12 <=
"  }\n"+														//13
"   </xd:declaration>\n"+										//14
"   <root>\n"+													//15
"      <a xd:script='occurs 1..; finally test();' />\n"+		//16
"   </root>\n"+													//17
"</xd:def>\n";													//18
			reporter = test(props, new String[] {xdef}, getClass());
			strw = new StringWriter();
			ReportPrinter.printListing(
				strw, new java.io.StringReader(xdef), reporter, true);
			pos = 0;
			s = strw.toString();
			if ((pos = s.indexOf("\n ***** ", pos)) > 0) {
				if (!s.startsWith("\n *****  |", pos)) {
					fail(s);
				}
			} else {
				fail(s);
			}
			reporter.reset();
			assertEq("", chkReport(reporter, "XDEF424", "12", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "1", null));
			assertNull(reporter.getReport(), reporter.printToString());
			//test error reporting
			reporter.clear();
			xb = XDFactory.getXDBuilder(props);
			xb.setExternals(getClass());
			xb.setReporter(reporter);
			xb.setSource(dataDir + "test/TestErrors3.xdef");
			xp = xb.compileXD();
			if (reporter.errorWarnings()) {
				ReportReader rr = reporter.getReportReader();
				while ((report = rr.getReport()) != null) {
					printReport(report,	dataDir+"test/TestErrors3.xdef");
				}
			} else {
				parse(xp,"DefSystem", dataDir+"test/TestErrors3.xml", reporter);
				assertEq("XDEF501", reporter.getReport().getMsgID());
				assertNull(reporter.getReport(), reporter.printToString());
			}
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + XDEFNS + "'\n"+							//01
"     xd:root='root'\n"+										//02
"     xd:name='test'>\n"+										//03
"   <root>\n"+													//04
"	  <a xd:script='occurs 0..1; match matchFault();' />\n"+	//05 <==
"   </root>\n"+													//06
"</xd:def>\n";													//07
			reporter = test(props, new String[] {xdef}, getClass());
			if (reporter.getErrorCount() > 0) {
				assertEq("", chkReport(reporter, "XDEF423", "5", "49", null));
				if (reporter.getErrorCount() > 1) {
					fail(reporter.toString());
				}
				assertNull(reporter.getReport(), reporter.printToString());
			} else {
				fail("Error not reported");
			}
			xdef =
"<xd:collection xmlns:xd='" + XDEFNS + "'>\n"+
"<xd:def root='a'>\n"+
"  /* comment */\n"+	//ok
"  <a/>\n"+
"  blabla\n"+			//error
"</xd:def>\n"+
"</xd:collection>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF260", "4", "7", null));
			xdef = // variablecompilation error
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{external final String j=\"\"; out(i);}'/>\n"+
"\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF411", "5", "25", null));
			xdef = // variable ompilation error
"<xd:def xmlns:xd='" + XDEFNS + "' root='A'>\n"+
"  <A xd:script='var external int i;finally{i = 1; out(i);}'/>\n"+
"\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF411", "2", "21", null));
			xdef = // variable ompilation error
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String i;\n"+
"    external String j = i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{out(i+(i==null)+j+(j==null));}'/>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF120", "4", "23", null));
			xdef = //test empty sequence
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:sequence> </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef =
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:mixed> </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef = //test empty mixed
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:mixed> <b/> <b/> </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF234", "3", "21", null));
			xdef = //test empty choice
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:choice> </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef = //test repeated items in choice
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:choice> <b/> <b/> </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF234", "3", "22", null));
			xdef = // test incompatible casting.
"<xd:def xmlns:xd='" + XDEFNS + "' root=\"a\">\n"+
"  <a a='string' xd:script='finally outln(toString((boolean)(String) @a))'/>\n"+
"</xd:def>\n";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF457", "2", "71", null));
			xdef = //Incorrect fixed value
"<xd:def xmlns:xd='" + XDEFNS + "' root='a'>\n"+
"<a v=\"int; fixed '2.0'\"/>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF481", "2", "18", null));
			xdef = // test errors
"<xd:def name='a' root='macTest' xmlns:xd='" + XDEFNS + "'>\n"+
"<macTest  xd:script = \"finally ${text}; options trim;\"/>\n"+
"<xd:macro name = \"text\">\n"+
"outln('Macro call is:\n${text}');</xd:macro>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF486", "2", "39", null));
			assertEq("", chkReport(reporter, "XDEF426", "2", "32", null));
			assertEq("", chkReport(reporter, "XDEF433", "2", "49", null));
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
			String[] xdefs = new String[] {
"<xd:def xmlns:xd=  '" + XDEFNS + "'\n"+						//01
"		xd:name      =  'Test1'\n"+								//02
"		xd:root      =  'test' >\n"+							//03 <==
"<test1/>\n"+													//04
"</xd:def>",													//05
"<xd:def xmlns:xd=  '" + XDEFNS + "'\n"+						//06
"		xd:name      =  'Test2'\n"+								//07
"		xd:root      =  'test2' >\n"+							//08
"<test2/>\n"+													//09
"</xd:def>"};													//10
			String[] names = new String[] {"xd1", "xd2"};//<==(XDEf903 xd1, xd2)
			xb = XDFactory.getXDBuilder(props);
			reporter.clear();
			xb.setReporter(reporter);
			xb.setSource(xdefs);
			xb.setSource(names);
			xb.compileXD();
			if (reporter.getErrorCount() == 0) {
				fail("Error not reported");
			} else {
				s = reporter.getReport().toString();
				assertTrue(s.contains("E XDEF903") || !s.contains("xd1"), s);
				s = reporter.getReport().toString();
				assertTrue(s.contains("E XDEF903") || !s.contains("xd2"), s);
				assertEq("", chkReport(reporter, "XDEF307", "3", "20", null));
				assertNull(reporter.getReport(), reporter.printToString());
			}
		} catch (Exception ex) { fail(ex); }
		try {
			compile(dataDir + "bla/blabla.xdef");
			fail("Error not recognized");
		} catch (Exception ex) {
			if (ex.toString().indexOf("XDEF903") < 0) {
				fail(ex);
			}
		}

		resetTester();
	}

	final public static void matchFault() {}

	final public static void output(String s) {} //this needs TestErrors3.xdef

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}