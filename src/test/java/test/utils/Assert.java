package test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.xml.KXmlUtils;

public class Assert extends org.testng.Assert {
	
	
	/**
	 * Asserts that two elements are equal. If they are not, an AssertionError,
	 * with the given message, is thrown.
	 * 
	 * @param actual   the actual value
	 * @param expected the expected value
	 * @param message  the assertion error message
	 * @param trim     whether trim text elements
	 */
	public static void assertEquals(
		final Element actual,
		final Element expected,
		final String  message,
		final boolean trim
	) {
		if ((expected == null) && (actual == null)) {
			return;
		}
		if (expected == null ^ actual == null) {
			failNotEquals(actual, expected, message);
		}
		
		ArrayReporter reporter = new ArrayReporter();
		KXmlUtils.compareElements(actual, expected, trim, reporter);

		if (!reporter.errorWarnings()) {
			return;
		}
		
		failNotEquals(actual, expected, message +
			", report:\n" + reporter.getReportReader().printToString());
	}
	
	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final Element actual, final Element expected,
		final String  message) {
		assertEquals(actual, expected, message, true);
	}

	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final Element actual, final Element expected) {
		assertEquals(actual, expected, null);
	}

	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final Element actual, final String expected,
		final String  message) {
		assertEquals(actual, KXmlUtils.parseXml(expected).getDocumentElement(), message);
	}

	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final Element actual, final String expected) {
		assertEquals(actual, expected, null);
	}
	
	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final String actual, final Element expected,
		final String  message) {
		assertEquals(KXmlUtils.parseXml(actual).getDocumentElement(), expected, message);
	}

	/** see {@link #assertEquals(Element, Element, String, boolean)}*/
	public static void assertEquals(final String actual, final Element expected) {
		assertEquals(actual, expected, null);
	}

	
	
	/**
	 * Asserts that reporter does contain no errors. If it doesn't, an AssertionError,
	 * with the given message, is thrown.
	 * If reporter contains warnings then logs them in level "warn".
	 * 
	 * @param reporter given reporter
	 * @param message user message to be added to report-message
	 * @return report-message. If reporter doesn't contain errors then null
	 */
	public static void assertNoErrors(
		final ReportWriter reporter,
		final String       message
	) {
		if (reporter.errorWarnings()) {
			String msg2 =
				(message != null ? message.toString().trim() + "\n" : "") +
				"XDef-reporter:\n" +
				reporter.getReportReader().printToString() + "\n";
			
			if (reporter.errors()) {
				fail(msg2);
			} else {
				logger.warn(msg2);
			}
		}
	}
	
	public static void assertNoErrors(final ReportWriter reporter) {
		assertNoErrors(reporter, null);
	}
	
	
	
	private static void failNotEquals(
		Object actual,
		Object expected,
		String message
	) {
		fail(format(actual, expected, message));
	}
	
	
	
	private static String format(
		Object actual,
		Object expected,
		String message
	) {
		return
			(message != null ? message + " " : "") +
			ASSERT_LEFT + expected + ASSERT_MIDDLE + actual	+ ASSERT_RIGHT
		;
	}
	


	private static final Character OPENING_CHAR  = '[';
	private static final Character CLOSING_CHAR  = ']';
	private static final String    ASSERT_LEFT   = "expected " + OPENING_CHAR;
	private static final String    ASSERT_MIDDLE = CLOSING_CHAR + " but found " + OPENING_CHAR;
	private static final String    ASSERT_RIGHT  = Character.toString(CLOSING_CHAR);

	/** logger */
	private static final Logger    logger        = LoggerFactory.getLogger(
		Assert.class
	);

}
