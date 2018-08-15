package cz.syntea.xdef.util;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.Assertion;

import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ReportWriter;
import test.xdef.Tester;



public class TesterSt {
	
	
	public static void assertNoErrors(final ReportWriter reporter) {
		final Assertion a = new Assertion();
		
		if (reporter.errorWarnings()) {
			String msg = "XDef-reporter:\n" +
				reporter.getReportReader().printToString() + "\n";
			
			if (reporter.errors()) {
				logger.error(msg);
				a.assertTrue(false, msg);
			} else {
				logger.warn(msg);
			}
		}
	}
	
	
	
	public static XDPool compile(final URL[] urls, final Class<?>... obj) {
		TesterLoc t = new TesterLoc();
		return t.compile(urls, obj);
	}
	
	public static XDPool compile(final File file, final Class<?>... obj) {
		TesterLoc t = new TesterLoc();
		return t.compile(file, obj);
	}

	
	
	private static class TesterLoc extends Tester {

		@Override
		public void test() {
			//empty, not to run
		}
		
	}

	
	
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(
		TesterSt.class
	);

}
