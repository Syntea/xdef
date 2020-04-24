package test.common.sys;

import org.xdef.XDConstants;
import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.STester;
import org.xdef.sys.Report;
import org.xdef.sys.SManager;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

/** Test reporter.
 * @author  Vaclav Trojan
 */
public class TestReport extends STester {

	public TestReport() {super();}

	private static Properties readProperties(Reader in) throws Exception {
		try {
			Properties result = new Properties();
			result.load(in);
			return result;
		} finally {
			in.close();
		}
	}

	@Override
	/** Run test and print error information. */
	public final void test() {
		String s;
		Report r;
		ArrayReporter reporter = new ArrayReporter();
		// test modification
		assertEq("axzyb", Report.text("","a&{0}{&u&x}{y}b","&{0}z").toString());
		try {
			reporter.clear();
			//XML075=XML chyba&{0}{: }&{#SYS000}
			reporter.add(new Report(
				KXmlUtils.parseXml("<E id='XML075'/>").getDocumentElement()));
			assertEq(1, reporter.getErrorCount());
			assertEq(reporter.get(0).getType(), Report.ERROR);
			s = reporter.printToString("slk");
			assertEq(s, "E XML075: XML chyba");
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
		String dataDir = getSourceDir();
		if (!new File(dataDir + "ABC_ces.properties").exists()) {
			if (!new File(dataDir + "ABC_ces.properties").exists()) {
				throw new RuntimeException("Report files are not available: "
					+ dataDir);
			}
		}
		try {
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "ces");
			s = "\n\na a\n b\n\n";
			assertEq(s, Report.string(null, s).toString());
			s = "\n\na a\n b\n\n";
			assertEq(s, Report.text("XYZ01", s).toString());
			s = "\n\na a\n b\n\n";
			assertEq(s, Report.text("XYZ01", s).toString());
			s = "Mod: &{p}";
			assertEq("Mod: P", Report.text("XYZ01", s, "&{p}P").toString());
			s = "Mod: &{p}";
			assertEq("Mod: P", Report.text("XYZ01", s, "&{p}P").toString());
			assertEq("?", Report.text("ABC001", "?").toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE,
				"ara"); //arabic is not available
			s = Report.text("SYS012","?","").toString();
			assertEq("Errors detected: ", s);
			//SManager.setLanguage("cs");
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "cs");
			s = Report.text("SYS012", "?", "").toString();
			assertEq("Při zpracování se vyskytly chyby: ", s);
			s = Report.text("XML_LANGUAGE", null).toString();
			assertEq("česky", s);
			s = Report.getReportText("XML_LANGUAGE", "eng");
			assertEq("English", s);
			reporter.clear();
			SManager.setProperty(XDConstants.XDPROPERTY_MESSAGES + "ABC",
				dataDir + "ABC_*.properties");
			s = Report.getReportText("ABC_LANGUAGE", "deu");
			assertEq("deutsch", s);
			s = Report.getReportText("ABC_DESCRIPTION", "deu");
			assertEq("Test Nachrichten", s);
			s = Report.getReportText("ABC014", "ces");
			assertTrue(s.startsWith("Tento report"), s);
			s = Report.getReportText("ABC014", "eng");
			assertNull(s); //not defined in eng
			s = Report.getReportText("ABC014", "deu");
			assertNull(s); //not defined in deu
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "cs");
			assertEq("Testovací zpráva", Report.text("ABC001", "?").toString());
			assertEq("TAB: \t, NL: \n, CR: \r.",
				Report.text("ABC013","?").toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "en");
			assertEq("Testing report", Report.text("ABC001","?").toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "de");
			assertEq("Test Nachricht", Report.text("ABC001", "?").toString());
			assertEq("Etwas: nichts!",
				Report.text("ABC003","?","&{n}nichts").toString());
			assertEq("Test Nachricht: Etwas", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString());
			SManager.removeReportTables("ABC");
			SManager.setProperty(XDConstants.XDPROPERTY_MESSAGES +
				"ABC_ces", dataDir + "ABC_ces.properties");
			SManager.setProperty(XDConstants.XDPROPERTY_MESSAGES +
				"ABC_eng", dataDir + "ABC_eng.properties");
			SManager.setProperty(XDConstants.XDPROPERTY_MESSAGES +
				"ABC_deu", dataDir + "ABC_deu.properties");
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "cs");
			assertEq("Testovací zpráva: x",
				Report.text("ABC001","?","&{msg}x").toString());
			assertEq("Testovací zpráva: Cosi", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString());
			assertEq("Testovací zpráva: x; Cosi: y!", Report.text(
				"ABC001","?","&{msg}x; &{#ABC003{???}{&{n}y}}").toString());
			assertEq("Testovací zpráva: x; Cosi: y!", Report.text(
				"ABC001","?","&{msg}x; &{#ABC003{}{&{n}y}}").toString());
			assertEq("Testovací zpráva: Cosi", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString("ces"));
			assertEq("Testing report: Something", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString("eng"));
			assertEq("Test Nachricht: Etwas", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString("deu"));
			assertEq("Testing report: Something", Report.text(
				"ABC001","?","&{msg}&{#ABC003}").toString("rus"));
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "de");
			assertEq("Test Nachricht", Report.text("ABC001","?","").toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "eng");
			s = Report.text("SYS012","?","   ").toString();
			assertEq("Errors detected:    ", s);
			s = Report.text("ABC001", "?").toString();
			assertEq("Testing report", s);
			s = Report.text("ABC004","?",
				"&{line}1&{col}5&{source}c:/temp.txt").toString();
			assertEq("Error 1 5 c:/temp.txt", s);
			assertEq("Error   ", Report.text("ABC004", "?").toString());
			s = Report.text("ABC005","?",
				"&{line}1&{col}5&{source}c:/temp.txt").toString();
			assertEq("Error (line: 1) column: 5 source: c:/temp.txt", s);
			s = Report.text("ABC005","?").toString();
			assertEq("Error unknown source", s);
			s = Report.text("ABC006","Message&{position}{ (}{)}",
				"&{position}This is position").toString();
			assertEq("Message (This is position)", s);
			s = Report.text(null, "Message&{position}",
				"&{This} tag is not prezent in source").toString();
			assertEq("Message", s);
			s = Report.text("ABC008", null,
				"&{line}1&{col}2&{source}d:\\temp\\a.a").toString();
			assertEq("Message line = 1 column = 2 source = 'd:\\temp\\a.a'", s);
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "ces");
			s = Report.text("SYS012","?","").toString();
			assertEq("Při zpracování se vyskytly chyby: ", s);
			s = Report.text("ABC001","?").toString();
			assertEq("Testovací zpráva", s);
			s = Report.text("ABC004","?",
				"&{line}1&{col}5&{source}c:/temp.txt").toString();
			assertEq("Chyba 1 5 c:/temp.txt", s);
			assertEq("Chyba   ", Report.text("ABC004","?").toString());
			s = Report.text("ABC005",
				"?","&{line}1&{col}5&{source}c:/temp.txt").toString();
			assertEq("Chyba (radka: 1) sloupec: 5 zdroj: c:/temp.txt", s);
			s = Report.text("ABC005","?").toString();
			assertEq("Chyba neznamy zdroj", s);
			s = Report.text("ABC006","Message&{position}{ (}{)}",
				"&{position}This is position").toString();
			assertEq("Zpráva (This is position)", s);
			s = Report.text(null,"Message&{position}{; (}{)}",
				"&{position}This is position").toString();
			assertEq("Message; (This is position)", s);
			s = Report.text("ABC008","Message&{position}{ (}{)}",
				"&{line}1&{col}2&{source}d:\\temp\\src.txt&{position}"
				+ "&{&ABC007 &{line}{ line=}"
				+ "&{col}{ column=}&{source}{ source='}{'}&}").toString();
			assertTrue(s.indexOf("sloupec=2 zdroj='d:\\temp\\src.txt'") > 0, s);
			s = Report.text("ABC010",null).toString();
			assertEq("Error on undefined position", s);
			s = Report.text("ABC010",null,"&{p}123").toString();
			assertEq("Error on position 123", s);
			s = Report.text("ABC010",null,"&{p}123").toString("ces");
			assertEq("Error on position 123", s);
			s = Report.text("ABC010",null,"&{p}123").toString("deu");
			assertEq("Error on position 123", s);
			s = Report.text("ABC011",null).toString("deu");
			assertEq("Error on undefined position", s);
			s = Report.text("ABC011",null, "&{p}123").toString("deu");
			assertEq("Error 123", s);
			s = Report.text("ABC012",null).toString("deu");
			assertEq("Error", s);
			s = Report.text("ABC012",null, "&{p}123").toString("deu");
			assertEq("Error pos=123.", s);
			s = Report.text("ABC015",null).toString("eng"); // no params
			assertEq("P1: P2:", s);
			//three params - one skipped
			s = Report.text("ABC015",null,"X",null,"Y").toString("eng");
			assertEq("P1:X P2:Y", s);
			s = Report.text("ABC015",null,null,"","Z").toString("eng");
			assertEq("P1: P2:Z", s);
			s = Report.text("ABC015",null,"X","","").toString("eng");//three
			assertEq("P1:X P2:", s);
			String[] params = Report.getReportParamNames("ABC002", null);
			if (params.length != 2 || !"anglicky".equals(params[0]) ||
				params[1].indexOf("esky") <= 0) {
				fail("'" + s + '\'');
			}
			assertEq("Chyba", Report.text("ABC009", null).toString("ces"));
			assertEq("Error", Report.text("ABC009", null).toString("eng"));
		} catch (Exception ex) {fail(ex);}
		try {
			//declare file with messages
//			SManager.setLanguage("ces");
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "ces");
			s = Report.getReportText("ABC_LANGUAGE", "ces") +
				" ABC009: '" +
				Report.text("ABC009", null).toString("ces") + '\'';
			assertTrue(s.indexOf("esky ABC009: 'Chyba'") > 0, s);
			s = Report.getReportText("ABC_LANGUAGE", "ces") +
				" ABC009: '" +
				Report.text("ABC009",null).toString("ces") + '\'';
			assertTrue(s.indexOf("esky ABC009: 'Chyba'") > 0, s);
			s = Report.getReportText("ABC_LANGUAGE", "eng") +
				" ABC009: '" +
				Report.text("ABC009", null).toString("eng") + '\'';
			assertEq("english ABC009: 'Error'", s);
			//set language (if it differs from the default one)
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "ces");
			s = " ABC009: '" + Report.text("ABC009", null) + '\'';
			assertEq(" ABC009: 'Chyba'", s);
			s = Report.text("ABC_LANGUAGE", null).toString("eng") +
				" ABC009: '" + Report.text("ABC009", null) + '\'';
			assertEq("english ABC009: 'Chyba'", s);
		} catch (Exception ex) {fail(ex);}
		try {
			File f = File.createTempFile("report", "tmp");
			f.deleteOnExit();
			FileReportWriter fw = new FileReportWriter(f, "UTF-8", true);
			fw.writeString("");
			fw.writeString("ABC");
			fw.writeString("\na\nb\n");
			fw.writeString("<x>");
			fw.writeString("&lt;x&gt;");
			fw.putReport(Report.warning("", "warning"));
			fw.putReport(Report.fatal("", "fatal"));
			fw.putReport(Report.error(null, "error1"));
			fw.putReport(Report.error("e1", "error2"));
			fw.putReport(Report.error("e2", "error3=&{p}", "&{p}mod"));
			fw.putReport(Report.text("t1", "txt1=&{p}", "&{p}mod"));
			fw.writeString("end");
			fw.close();
			FileReportReader fr = new FileReportReader(f,null, true);
			if ((r = fr.getReport()) != null) {
				assertEq("", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("ABC", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("\na\nb\n", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("<x>", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("&lt;x&gt;", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("W: warning", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("F: fatal", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("E: error1", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("E e1: error2", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("E e2: error3=mod", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("txt1=mod", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				assertEq("end", r.toString());
			} else {
				fail("unexpected end");
			}
			if ((r = fr.getReport()) != null) {
				fail(r.toString());
			}
			fr.close();
			f.delete();
		} catch (Exception ex) {fail(ex);}
		try {//check registered tables
			r = Report.error("123", "par &{p} par &{q}",
				"&{p}&{#456{a&{b}{:}{,}}{&{b}bc}}&{q}q");
			assertEq("E 123: par a:bc, par q", r.toString());
			r = Report.error("123", "par &{p} par &{q}",
				"&{p}&{#456{a&{b}{:}{,}}{&{b}bc{{{}}}}&{d}d}&{q}q");
			assertEq("E 123: par a:bc{{{}}}}, par q", r.toString());
			r = Report.error("123", "par &{p} par &{q}",
				"&{p}&{#456}&{q}q");
			assertEq("E 123: par  par q", r.toString());
			r = Report.error("123", "par &{p} par &{q}",
				"&{p}&{#456{a&{b}{:}{,}}}&{q}q");
			assertEq("E 123: par a par q", r.toString());
			r = Report.error("123", "par &{p} par &{q}",
				"&{p}&{#456{a&{b}{:}{,}}{}}&{q}q");
			assertEq("E 123: par a par q", r.toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "eng");
			r = Report.error("123", "par &{p}, par &{q}",
				"&{p}&{#ABC001}&{q}q");
			assertEq("E 123: par Testing report, par q", r.toString());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "ces");
			r = Report.error("123", "par &{p}, par &{q}",
				"&{p}&{#ABC001}&{q}q");
			assertEq("E 123: par Testovací zpráva, par q", r.toString());
			assertEq("&{#ABC001}", r.getParameter("p"));
			assertEq("q", r.getParameter("q"));
			r = Report.error("123", "par &{p}, par &{q}",
				"&{q}q&{p}&{#ABC001}");
			assertEq("E 123: par Testovací zpráva, par q", r.toString());
			assertEq("&{#ABC001}", r.getParameter("p"));
			assertEq("q", r.getParameter("q"));
			r = Report.error("123", "par &{p}, par &{q}",
				"&{q}q&{p}&{#ABC001}&{#ABC002}");
			assertEq("&{#ABC001}&{#ABC002}", r.getParameter("p"));
			assertEq("q", r.getParameter("q"));
			r = Report.error("123", "par &{p}, par &{q}",
				"&{p}&{#ABC001}&{#ABC002}&{q}q");
			assertEq("&{#ABC001}&{#ABC002}", r.getParameter("p"));
			assertEq("q", r.getParameter("q"));
			r = Report.error("123", "par &{p}, par &{q}", "&{p}&{q}q");
			assertEq("", r.getParameter("p"));
			assertEq("q", r.getParameter("q"));
			assertNull(r.getParameter("r"));
			r = Report.error("123", "par &{p}, par &{q}", "&{p}x");
			assertEq("x", r.getParameter("p"));
			r = Report.error("123", "par &{p}, par &{q}");
			assertNull(r.getParameter("p"));
			r = Report.error("abc", "x &{p} y &{q} z &{r}");
			assertNull(r.getParameter("p"));
			r.setParameter("p", "1");
			assertEq("1", r.getParameter("p"));
			r.setParameter("q", "2");
			assertEq("1", r.getParameter("p"));
			assertEq("2", r.getParameter("q"));
			r.setParameter("p", "");
			assertEq("", r.getParameter("p"));
			assertEq("2", r.getParameter("q"));
			r.setParameter("p", null);
			assertNull(r.getParameter("p"));
			r.setParameter("q", null);
			assertNull(r.getParameter("p"));
			assertNull(r.getParameter("q"));
			r.setParameter("r", null);
			assertNull(r.getParameter("r"));
			r.setParameter("r", "3");
			assertEq("3", r.getParameter("r"));
			r.setParameter("q", "2");
			r.setParameter("p", "1");
			assertEq("1", r.getParameter("p"));
			assertEq("2", r.getParameter("q"));
			assertEq("3", r.getParameter("r"));
		} catch (Exception ex) {fail(ex);}
		try {//check registered tables
			assertEq("eng", SManager.getDefaultLanguage());
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "en");
			s = Report.text(SYS.SYS012).toString();
			assertEq("Errors detected", s);
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "cs");
			s = Report.text(SYS.SYS012, "").toString();
			assertEq("Při zpracování se vyskytly chyby: ", s);
			s = Report.text(SYS.SYS012, "").toString("eng");
			assertEq("Errors detected: ", s);
			s = Report.text(SYS.SYS012, "").toString("sk");
			assertTrue(s.indexOf("sa vyskytli chyby") >= 0, s);
			s = Report.text(XML.XML_LANGUAGE).toString();
			assertTrue(s.indexOf("esky") > 0, s);
			s = Report.getReportText(XML.XML_LANGUAGE, "eng");
			assertEq("English", s);
			s = Report.getReportText(XML.XML_LANGUAGE, null);
			assertTrue(s.indexOf("esky") > 0, s);
			SManager.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "en");
			s = Report.getReportText(XML.XML_LANGUAGE, null);
			assertEq("English", s);
			Properties props = readProperties(new StringReader(
				"_language=deu\n_prefix=SYS\nSYS013=Fehler"));
			SManager.addReports(props);
			s = Report.text(SYS.SYS012).toString("deu");
			assertEq("Errors detected", s);
			s = Report.text(SYS.SYS013).toString("deu");
			assertEq("Fehler", s);
			SManager.removeReportTable("SYS", "deu");
			s = Report.text(SYS.SYS013).toString("deu");
			assertEq("Too many errors", s);
		} catch (Exception ex) {fail(ex);}
		new File(getTempDir()).delete();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}