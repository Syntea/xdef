package test.common.sys;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import java.io.PrintWriter;
import java.io.StringReader;
import org.xdef.sys.ReportReader;
import org.xdef.sys.STester;
import test.XDTester;

/** TestReport
 * @author  Vaclav Trojan
 */
public class TestErrorReporting extends STester {

	public TestErrorReporting() {super();}

	public void printReport(Report report, String data) {
		System.out.flush();
		System.err.flush();
		fail(new Throwable(
			report.toString() + "; '" + report.getModification() + "'"));
		System.err.flush();
		System.out.flush();
		if (XDTester.getFulltestMode()) {
			ArrayReporter reporter = new ArrayReporter();
			reporter.putReport(report);
			printReports(reporter, data);
		}
	}

	public void printReports(ReportReader reporter, String data) {
		if (XDTester.getFulltestMode()) {
			System.out.flush();
			System.err.flush();
			ReportPrinter.printListing(new PrintWriter(System.out),
				new StringReader(data),
				reporter, null, 80, true, null); //lineNumbers
			System.err.flush();
			System.out.flush();
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
			test1();
		} catch (Error ex) {
			ex.printStackTrace(System.err);
			fail(ex);
		}
	}

	private void test1() {
		String source =
			"\n" +      //01
			"\n" +      //02
			"a a\n" +   //03
			" b\n" +    //04
			"\n";       //05
		StringParser p;
		SPosition savedPos;

		try {
			p = new StringParser(source);
			test2(p, source);
		} catch (Exception ex) {
			fail(ex);
		}

		try {
			p = new StringParser(source);
			savedPos = p.getPosition(); //position with line info
			p.setLineInfoFlag(true);
			p.setSourceBuffer(source);
			test2(p, source);
		} catch (Exception ex) {
			fail(ex);
		}

	}

	private void test2(final StringParser p, final String source) {
		Report report;
		ArrayReporter reporter;
		try {
			p.error("1","1/1");
			p.skipSpaces();
			p.error("2","3/1");
			p.isChar('a');
			p.error("3","3/3");
			p.isChar(' ');
			p.error("4","3/4");
			p.isChar('a');
			p.error("5","3/5");
			p.skipSpaces();
			p.error("6","4/2");
			p.isChar('b');
			p.error("7","3/5");
			p.skipSpaces();
			p.error("8","4/2");
			p.error("9","4/2");
			reporter = (ArrayReporter) p.getReportWriter();
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"1".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}0&{line}1&{column}1")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"2".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}2&{line}3&{column}1")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"3".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}3&{line}3&{column}2")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"4".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}4&{line}3&{column}3")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"5".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}5&{line}3&{column}4")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"6".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}7&{line}4&{column}2")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"7".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}8&{line}4&{column}3")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"8".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}10&{line}6&{column}1")) {
				printReport(report, source);
			}
			if ((report = reporter.getReport()) == null) {
				fail("report missing");
			} else if (!"9".equals(report.getMsgID()) ||
				!report.getModification().equals("&{pos}10&{line}6&{column}1")) {
				printReport(report, source);
			}
			while ((report = reporter.getReport()) != null) {
				printReport(report, source);
			}
		} catch (Exception ex) {
			fail(ex);
		}

	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}