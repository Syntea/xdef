package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.xdef.sys.ReportReader;

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
			XDBuilder xb = XDFactory.getXDBuilder(rw, props);
			xb.setExternals(cls);
			xb.setSource(xdef);
			xb.compileXD();
			return rw;
	}

	private void test1() {
		XDBuilder xb;
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
			XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);

		final String dataDir = getDataDir();
		int count, pos;
		XDPool xp;
		String xdef;
		String xml;
		String s;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		Report report;
		StringWriter strw;
		CharArrayWriter caw;
		try {// check of error reporting - script in value is not valid
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+	//01
" <a b=\"This is not script!\" c=\"required\" > \n"+		//02<=
" This is not script!\n"+									//03<=
" </a>\n"+													//04
"</xd:def>";												//05
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "2", "13", null));
			assertEq("", chkReport(reporter, "XDEF410", "2", "13", null));
			assertEq("", chkReport(reporter, "XDEF424", "3", "7", null));
			assertEq("", chkReport(reporter, "XDEF410", "3", "7", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {// check of error reporting - xpath
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"create from(' ');\"/>\n"+
"</xd:def>";
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XML505", "2", "33", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {// check of error reporting - low number of parameters of method
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' name='a' xd:root = 'a' >\n"+	//01
"<a a='optional xdatetime()' >\n"+								//02<=
"</a>\n"+														//03
"</xd:def>";													//04
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, null,"2", "2"/*"27"*/, null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try {// check of error reporting - high number of parameters of method
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd ='" + _xdNS + "' name='a' root='a'>\n"+	//01
"  <a a='optional string(1,2,3)'/>\n"+						//02<=
"</xd:def>\n";												//03
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF461", "2", "30", null));
			assertEq("", chkReport(reporter, "XDEF465", "2", "30", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		//test of error reporting - replacement in attribute value (all new
		// lines in parsed data are replaced by the space character!).
		try {
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<!DOCTYPE xd:collection [<!ENTITY ent \"abcd\">]>\n"+				//01
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+						//02
"<xd:def name='Q_SCN' root='*' script='\n"+							//05
"options ppppp,    &#x0065;&#x0066;, &ent;, xxxxx,\n"+				//04<=
"y' />\n"+															//05<=
"</xd:collection>\n";												//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF433", "4", "9", null));
			assertEq("", chkReport(reporter, "XDEF433", "4", "19", null));
			assertEq("", chkReport(reporter, "XDEF433", "4", "37", null));
			assertEq("", chkReport(reporter, "XDEF433", "4", "44", null));
			assertEq("", chkReport(reporter, "XDEF433", "5", "1", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+							//01
"<xd:def name='Q_SCN' root='*'\n"+										//02
"        xd:script='options &#x0009; ignoreEmptyAttributex' />\n"+		//03<=
"</xd:collection>\n";													//04
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF433", "3", "37", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='root' name='a'>\n"+		//01
"   <xd:declaration>\n"+										//02
"<![CDATA[\n"+													//03
"  void test() {\n"+											//04
"    int i=1; String s='';\n"+									//05
"\n"+															//06
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//07<=
"\n"+															//08
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//09<=
"]]>\n"+														//10
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//11<=
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//12<=
"  }\n"+														//13
"   </xd:declaration>\n"+										//14
"   <root>\n"+													//15
"      <a xd:script='occurs 1..; finally test();' />\n"+		//16
"   </root>\n"+													//17
"</xd:def>\n";													//18
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "7", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "9", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "11", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "12", "18", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<!DOCTYPE doc [<!ENTITY e '&#10;entity_e&#10;'>]>\n"+			// 01
"<xd:def xmlns:xd='"+XDConstants.XDEF20_NS_URI+"' root='a'>\n"+	// 02
" <a\n"+														// 03
"  a='fixed a+\"&lt;,\n"+										// 04<=
"&#x3e;\"+b;'\n"+												// 05<=
"  b='fixed c+\" \" + &e; + d'>\n"+								// 06<=
"  finally out('&#60;'+e+'&#62;x<![CDATA[['+f+'\n"+				// 07<=
"]]<>'+g+']]>\n"+												// 08<=
"  x&lt;'+h+'&gt;'+i+'');\n"+									// 09<=
"fixed 'abc'+j+'d'+ &e; + '.';<a>fixed 'abc'</a>\n"+			// 10<=
" </a>\n"+														// 11
"</xd:def>";													// 12
			s = "\\n";
			for (int i = 0; i < 2; i++) {
				reporter = test(props, xdef);
				assertEq("", chkReport(reporter, "XDEF424", "4", "13", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "5", "10", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "6", "13", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "6", "24", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "6", "26", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "7", "24", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "7", "45", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "8", "8", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "9", "8", null),s);
				assertEq("", chkReport(reporter, "XDEF424", "9", "20",null),s);
				assertEq("", chkReport(reporter, "XDEF424", "10", "14",null),s);
				assertEq("", chkReport(reporter, "XDEF424", "10", "24",null),s);
				assertNull(reporter.getReport(), reporter.printToString());
				reporter.clear();
			}
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name='a'>\n"+		//01
"   <xd:declaration>\n"+										//02
"  void test() {\n"+											//03
"    int i=1; String s='';\n"+									//04
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//05<=
"<![CDATA[t='1';\n"+											//06
"    if('1' == (t='1')) s+=2; else s+=0;\n"+					//07<=
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//08<=
"]]>\n"+														//09
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//10<=
"    if ('12' == t) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//11<=
"  }\n"+														//12
"   </xd:declaration>\n"+										//13
"   <a>\n"+														//14
"      <b xd:script='occurs 1..; finally test();' />\n"+		//15
"   </a>\n"+													//16
"</xd:def>\n";													//17
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "5", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "6", "11", null));
			assertEq("", chkReport(reporter, "XDEF424", "7", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "8", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "11", "18", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test error reporting
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name='a'>\n"+			//01
" <xd:macro name='mm'>if('1'==(t='1'))s='2';else t='3';</xd:macro>\n"+//02
" <xd:macro name='n'>if('1' == (</xd:macro>\n"+						//03
" <xd:declaration>void test() {int i=1; String s='';\n"+			//04
"    if('1' == (t='1')) s+=2; else t+=0;\n"+						//05<=
"    ${n}t='1')) t+=2; else s+=0;\n"+								//06<=
"    ${mm}if('1' == (t='1')) s+=2; else s+=0;\n"+					//07<=
"    if('1' == (t='1')) s+=2; else s+=0;\n"+						//08<=
"  }\n"+															//09
" </xd:declaration>\n"+												//10
" <a>\n"+															//11
"   <b xd:script=\"occurs 1..; finally test();\" />\n"+				//12
" </a>\n"+															//13
"</xd:def>\n";														//14
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF424", "5", "17", null));
			assertEq("", chkReport(reporter, "XDEF424", "5", "36", null));
			assertEq("", chkReport(reporter, "XDEF424", "6", "10", null));
			assertEq("", chkReport(reporter, "XDEF424", "6", "18", null));
			assertEq("", chkReport(reporter, "XDEF424", "7", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "7", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "7", "22", null));
			assertEq("", chkReport(reporter, "XDEF424", "8", "17", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //check ambiguous XDefinition error
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+																//02
"  <b xd:script = 'occurs 0..1'/>\n"+									//03
"  <b xd:script = 'occurs 1..2'/>\n"+									//04<=
"</a>\n"+																//05
"</xd:def>";															//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF235", "4", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+																//02
"  <b xd:script = 'occurs 0..*'/>\n"+									//03
"  <b xd:script = 'occurs 0..1'/>\n"+									//04<=
"</a>\n"+																//05
"</xd:def>";															//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF238", "4", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='"+_xdNS+"' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+															//02
"  <xd:text>required string()</xd:text>\n"+							//03
"  <xd:text>optional string()</xd:text>\n"+							//04<=
"</a>\n"+															//05
"</xd:def>";														//06
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF239", "4", "4", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='"+_xdNS+"' name='ambiguous_xdef' root='a'>\n"+	//01
"<a>\n"+															//02
"  required string();\n"+											//03
"  optional string();\n"+											//04<=
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
"<xd:collection>\n"+									//01<=
"<xdef_de xd:name    = 'Q_SCN'\n"+						//02<=
"          xd:root   = '*'\n"+							//03<=
"          xd:script = '\n"+							//04<=
"${mac#m} &#x0009;ignoreEmptyAttributes' />\n"+			//05<=
"<xd:de xd:name  ='mac'>\n"+							//06<=
"  <xd:macro name = 'm'>\n\noptions</xd:macro>\n"+		//07<=
"</xd:def>\n"+											//08
"</xd:collection>\n";									//09<=
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XML047", "1", null, null));
			assertEq("", chkReport(reporter, "XDEF256", "1", "2", null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XML047", null, null, null));
			assertEq("", chkReport(reporter, "XDEF255", "2", "2", null));
			assertEq("", chkReport(reporter, "XML047", "6", "2", null));
			assertEq("", chkReport(reporter, "XML047", "6", null, null));
			assertEq("", chkReport(reporter, "XDEF255", "6", "2", null));
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
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+					//01
"  <xd:declaration> int j = k; </xd:declaration>\n"+				//02
"  <xd:declaration>\n"+												//03
"    int k = j;\n"+													//04<=
"  </xd:declaration>\n"+											//05
"  <a xd:script = \"finally if (k != 1) outln('error');\"/>\n"+		//06
"  <xd:declaration> k = 1; </xd:declaration>\n"+					//07<=
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
"<xd:def xmlns:xd = '" + _xdNS + "' root = 'a'>\n"+					//01
"  <xd:declaration> int j = k; </xd:declaration>\n"+				//02
"  <xd:declaration>\n"+												//03
"    int k = 'a'; int y = x, i = 0;\n"+								//04<=
"    void h() {\n"+													//05
"      if (i != 0 || k != 'a' || x != 1 ||  y != 1) outln('error');\n"+//06<=
"    }\n"+															//07
"  </xd:declaration>\n"+											//08
"  <a xd:script = \"finally {h(); if (k != 1) outln('error');}\"/>\n"+	//09
"  <xd:declaration> int x = true; </xd:declaration>\n"+				//10<=
"</xd:def>";														//11
			reporter = test(props, xdef);
			assertEq("", chkReport(reporter, "XDEF457", "10", "32", null));
			assertEq("", chkReport(reporter, "XDEF457", "4", "16", null));
			assertEq("", chkReport(reporter, "XDEF444", "6", "30", null));
			assertNull(reporter.getReport(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
		try { //test file names etc
			String fName1 = "TestErrors1.xdef";
			String fName2 = "TestErrors2.xdef";
			String[] sources = new String[] {
				dataDir + "test/" + fName1,
				dataDir + "test/" + fName2};
			reporter = test(props, sources, getClass());
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
		try {// variable, mothod, type redefinition
			reporter.clear();
			xp = XDFactory.compileXD(reporter, (Properties) null, 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    int i;\n"+
"    int x(){return 0;}\n"+
"    type t int();\n"+
"  </xd:declaration>\n"+
"  <a/>\n"+
"</xd:def>",
"<xd:declaration xmlns:xd='"+_xdNS+"'>\n"+
"  int i; int x(){return 0;} type t int();\n"+
"</xd:declaration>");
			if (reporter.errorWarnings()) {
				rep = reporter.getReport();
				assertEq("", chkReport(rep, "XDEF450", "2", "8", "String_2"));
				assertTrue(rep.toString().contains(
					": line=3; column=9; source=\"String_1\")"));
				rep = reporter.getReport();
				if (rep == null) {
					fail("Error not reported");
				} else {
					assertEq("", chkReport(rep,"XDEF462","2","17","String_2"));
					assertTrue(rep.toString().contains(
						": line=4; column=9; source=\"String_1\")"));
				}
				rep = reporter.getReport();
				if (rep == null) {
					fail("Error not reported");
				} else {
					assertEq("", chkReport(rep,"XDEF470","2","34","String_2"));
					assertTrue(rep.toString().contains(
						": line=5; column=10; source=\"String_1\")"));
				}
			} else {
				fail("Error not reported");
			}
			assertNull(reporter.getReport(), reporter.printToString());
			reporter.clear();
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xdef:def xmlns:xdef='" + _xdNS + "' name='Example' root='List'>\n"+
"<List>\n"+
"  <Employee Name       ='required string(1,30)'\n"+
"    FamilyName         ='required string(1,30)'\n"+
"    Ingoing            =\"required xdatetime('yyyy-M-d')\"\n"+
"    Salary             ='required int(1000,100000)'\n"+
"    Qualification      ='required string()'\n"+
"    SecondQualification='optional string()'\n"+
"    xdef:script        ='occurs 0..' />\n"+
"</List>\n"+
"</xdef:def>";
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
"            Ingoing                = '1998-19-43'\n"+			//10<=
"            Salary                 = 'abcd' \n"+				//11<=
"            Qualification          = '' />\n"+					//12<=
"\n"+															//13
"  <Employee Name                   = 'Peter'\n"+				//14
"            FamilyName             = 'Black'\n"+				//15
"            Ingoing                = '1998-2-13'\n"+			//16
"            Salary                 = '1600' \n"+				//17
"            SecondQualification    = 'electrician' />\n"+		//18<=
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
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name ='a'>\n"+
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
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='test'>\n"+
"<test p1 = 'required string()'\n"+
"      p2 = 'required string(2)'\n"+
"      p3 = 'required string(0,30)'\n"+
"      p4 = 'required string(0,30)'\n"+
"      p5 = 'required string(1,30)'\n"+
"      p6 = 'required int()'\n"+
"      p7 = 'required int(2)'\n"+
"      p8 = 'required int(2,3)'\n"+
"      p9 = 'required int(2,3)'/>\n"+
"</xd:def>";
			xb = XDFactory.getXDBuilder(props);
			xb.setSource(xdef);
			xp = xb.compileXD();
			xml =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<test p1 = ''\n"+												//01<=
"      p2 = 'xxx'\n"+											//02<=
"      p3 = ''\n"+										        //03
"      p4 = 'zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzxx'\n"+	//04<=
"      p5 = ''\n"+												//05<=
"      p6 = 'z1234567890'\n"+									//06<=
"      p7 = '0'\n"+												//07<=
"      p8 = '1'\n"+												//08<=
"      p9 = '4'/>\n";											//09<=
			parse(xp, "", xml, reporter);
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
			//force compilation of test.xdef.Test002;
			test.xdef.TestExtenalMethods.class.getClass();
			xml =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"external method\n"+ //*
" void test.xdef.TestExtenalMethods.x(XXElement,, XDContainer, XDContainer);\n"+
"  Container p = null, q = null, r = null, s = null, t = null;\n"+
"</xd:declaration>\n"+
"  <a a=\"string; finally {x(p,q);}\"/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, null);
			xb.setSource(xml);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF412", "4", "48", null));
				assertEq("", chkReport(reporter, "XDEF443", "7", "32", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"<a>\n"+
"  <A xd:script='2..3'/>\n"+
"  <A/>\n"+
"</a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, null);
			xb.setSource(xdef);
			xb.compileXD();
			if (reporter.errorWarnings()) {
				assertEq("", chkReport(reporter, "XDEF235", "4", "4", null));
			}
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
		try {//test incorrect key parameter (see %0)
			xml =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<a a='decimal(%0,1)'/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, null);
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
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<x:def xmlns:x ='" + _xdNS + "' root ='a'>\n"+		//01
"<a a=\"required eq('a')\">\n"+						//02
"this is an illegal text\n"+						//03<=
"</a>\n"+											//04
"</x:def>";											//05
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, null);
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
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name='a'>\n"+		//01
" <xd:macro name='test'>\n"+									//02
"    int i=1; String t='';\n"+									//03
"    if('1' == (s='1')) s+=2; else s+=0;\n"+					//04
"    if ('12' == s) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//05
"</xd:macro>\n"+												//06
"   <xd:declaration>\n"+										//07
"  void test() {\n"+											//08
"    ;\n"+														//09
"    ${test}" +													//10<=
"  }\n"+														//11
"   </xd:declaration>\n"+										//12
"   <a>\n"+														//13
"      <b xd:script='occurs 1..; finally test();' />\n"+		//14
"   </a>\n"+													//15
"</xd:def>\n";													//16
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
			assertEq("", chkReport(reporter, "XDEF424", "10", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "5", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "5", null));
			assertNull(reporter.getReport(), reporter.printToString());
			xdef = //macro starts now at new line
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name='a'>\n"+		//01
" <xd:macro name='test'>\n"+									//02
"    int i=1; String t='';\n"+									//03
"    if('1' == (s='1')) s+=2; else s+=0;\n"+					//04
"    if ('12' == s) out('OK 1 '); else out('ERR 1: ' + s);\n"+	//05
"</xd:macro>\n"+												//06
"   <xd:declaration>\n"+										//07
"  void test() {\n"+											//08
"    ;\n"+														//09
"${test}" +														//10<=
"  }\n"+														//11
"   </xd:declaration>\n"+										//12
"   <a>\n"+														//13
"      <a xd:script='occurs 1..; finally test();' />\n"+		//14
"   </a>\n"+													//15
"</xd:def>\n";													//16
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
			assertEq("", chkReport(reporter, "XDEF424", "10", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "1", null));
			assertEq("", chkReport(reporter, "XDEF424", "10", "1", null));
			assertNull(reporter.getReport(), reporter.printToString());
			//test error reporting
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter,props);
			xb.setExternals(getClass());
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
"<xd:def xmlns:xd='" + _xdNS + "' root='a' name='a'>\n"+		//01
"   <a>\n"+														//02
"	  <b xd:script='occurs 0..1; match matchFault();' />\n"+	//03<=
"   </a>\n"+													//04
"</xd:def>\n";													//05
			reporter = test(props, new String[] {xdef}, getClass());
			if (reporter.getErrorCount() > 0) {
				assertEq("", chkReport(reporter, "XDEF423", "3", "49", null));
				if (reporter.getErrorCount() > 1) {
					fail(reporter.toString());
				}
				assertNull(reporter.getReport(), reporter.printToString());
			} else {
				fail("Error not reported");
			}
			xdef =
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+	//01
"<xd:def root='a'>\n"+							//02
"  /* comment */\n"+//ok						//03
"  <a/>\n"+										//04<=
"  blabla\n"+									//05
"</xd:def>\n"+									//06
"</xd:collection>";								//07
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF260", "4", "7", null));
			xdef = // variablecompilation error
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+							//01
"  <xd:declaration>\n"+													//02
"    external String i;\n"+												//04
"  </xd:declaration>\n"+												//05
"  <a xd:script='finally{external final String j=\"\"; out(i);}'/>\n"+	//05<=
"</xd:def>";															//06
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF411", "5", "25", null));
			xdef = // variable ompilation error
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var external int i; finally{i = 1; out(i);}'/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF411", "2", "21", null));
			xdef = // variable ompilation error
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external String i;\n"+
"    external String j = i;\n"+
"  </xd:declaration>\n"+
"  <a xd:script='finally{out(i + (i == null) + j + (j == null));}'/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF120", "4", "23", null));
			xdef = //test empty sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:sequence> </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:mixed> </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef = //test empty mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:mixed> <b/> <b/> </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF234", "3", "21", null));
			xdef = //test empty choice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:choice> </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF325", "3", "5", null));
			xdef = //test repeated items in choice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"   <xd:choice> <b/> <b/> </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF234", "3", "22", null));
			xdef = // test incompatible casting.
"<xd:def xmlns:xd='" + _xdNS + "' root=\"a\">\n"+
"  <a a='string' xd:script='finally outln(toString((boolean)(String) @a))'/>\n"+
"</xd:def>\n";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter,props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF457", "2", "71", null));
			xdef = //Incorrect fixed value
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a v=\"int; fixed '2.0'\"/>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF481", "2", "20", null));
			xdef = // test errors
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest  xd:script=\"finally ${text}; options trim;\"/>\n"+
"<xd:macro name=\"text\">\n"+
"outln('Macro call is:\n${text}');</xd:macro>\n"+
"</xd:def>";
			reporter.clear();
			xb = XDFactory.getXDBuilder(reporter, props);
			xb.setSource(xdef);
			xb.compileXD();
			assertEq("", chkReport(reporter, "XDEF486", "2", "37", null));
			assertEq("", chkReport(reporter, "XDEF426", "2", "30", null));
			assertEq("", chkReport(reporter, "XDEF433", "2", "47", null));
			if (XDConstants.XDEF31_NS_URI.equals(XDTester._xdNS)) {
//        1         2         3         4        5          6         7
//234567890123456789012345678901234567890123456789012345678901234567890123456789
				String[] xdefs = new String[] {
"<xd:def xmlns:xd='" + _xdNS + "' name  ='Test1' root  ='test' >\n"+	//01<=
"<test1/>\n"+															//02
"</xd:def>",															//03
"<xd:def xmlns:xd='" + _xdNS + "' name  ='Test2' root  ='test2' >\n"+	//04
"<test2/>\n"+															//05
"</xd:def>"};															//06
				String[] names = new String[] {"xd1", "xd2"};//<=(XDEf903 xd1, xd2)
				reporter.clear();
				xb = XDFactory.getXDBuilder(reporter, props);
				xb.setSource(xdefs);
				xb.setSource(names);
				xb.compileXD();
				if (reporter.getErrorCount() == 0) {
					fail("Error not reported");
				} else {
					s = reporter.getReport().toString();
					assertTrue(s.contains("E XDEF903")||!s.contains("xd1"), s);
					s = reporter.getReport().toString();
					assertTrue(s.contains("E XDEF903")||!s.contains("xd2"), s);
					assertEq("", chkReport(reporter, "XDEF307","1","73",null));
					assertNull(reporter.getReport(), reporter.printToString());
				}
			}
		} catch (Exception ex) { fail(ex); }
		try {
			compile(dataDir + "bla/blabla.xdef");
			fail("Error not reported");
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
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}