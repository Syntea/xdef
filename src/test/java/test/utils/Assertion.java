package test.utils;

import org.testng.asserts.IAssert;
import org.w3c.dom.Element;

import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.xml.KXmlUtils;

public class Assertion extends org.testng.asserts.Assertion {
	
	public void assertEquals(final Element actual, final Element expected,
		final String message, final boolean trim) {
		doAssert(new SimpleAssert<Element>(actual, expected, message) {
			@Override public void doAssert() {
				Assert.assertEquals(actual, expected, message, trim);
			}
		});
	}
	
	public void assertEquals(final Element actual, final Element expected,
		final String message) {
		doAssert(new SimpleAssert<Element>(actual, expected, message) {
			@Override public void doAssert() {
				Assert.assertEquals(actual, expected, message);
			}
		});
	}

	public void assertEquals(final Element actual, final Element expected) {
		doAssert(new SimpleAssert<Element>(actual, expected) {
			@Override public void doAssert() {
				Assert.assertEquals(actual, expected);
			}
		});
	}

	public void assertEquals(final Element actual, final String expected,
		final String  message) {
		assertEquals(actual, KXmlUtils.parseXml(expected).getDocumentElement(), message);
	}

	public void assertEquals(final Element actual, final String expected) {
		assertEquals(actual, KXmlUtils.parseXml(expected).getDocumentElement());
	}
	
	public void assertEquals(final String actual, final Element expected,
		final String  message) {
		assertEquals(KXmlUtils.parseXml(actual).getDocumentElement(), expected, message);
	}

	public void assertEquals(final String actual, final Element expected) {
		assertEquals(KXmlUtils.parseXml(actual).getDocumentElement(), expected);
	}



	public void assertNoErrors(final ReportWriter reporter, final String message) {
		doAssert(new SimpleAssert<Boolean>(reporter.errors(), Boolean.FALSE, message) {
			@Override public void doAssert() {
				Assert.assertNoErrors(reporter, message);
			}
		});
	}
	
	public void assertNoErrors(final ReportWriter reporter) {
		doAssert(new SimpleAssert<Boolean>(reporter.errors(), Boolean.FALSE) {
			@Override public void doAssert() {
				Assert.assertNoErrors(reporter);
			}
		});
	}

	
	
	abstract private static class SimpleAssert<T> implements IAssert<T> {
		private final T      actual;
		private final T      expected;
		private final String message;
		
		public SimpleAssert(T actual, T expected) {
			this(actual, expected, null);
		}
		
		public SimpleAssert(T actual, T expected, String message) {
			this.actual   = actual;
			this.expected = expected;
			this.message  = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
		
		@Override
		public T getActual() {
			return actual;
		}
		
		@Override
		public T getExpected() {
			return expected;
		}
		
		@Override
		abstract public void doAssert();
	}
}
