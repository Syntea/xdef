package test.util;

import java.util.Map;

import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

/**
 * A simple soft assertion mechanism that also captures the stacktrace to help
 * pin point the source of failure.
 * Source-code is based on {@link org.testng.asserts.SoftAssert}
 */
public class SoftAssert extends Assertion {
	private final Map<AssertionError, IAssert<?>> m_errors = Maps
		.newLinkedHashMap();
	
	@Override
	protected void doAssert(IAssert<?> a) {
		onBeforeAssert(a);
		try {
			a.doAssert();
			onAssertSuccess(a);
		} catch (AssertionError ex) {
			onAssertFailure(a, ex);
			m_errors.put(ex, a);
		} finally {
			onAfterAssert(a);
		}
	}
	
	public void assertAll() {
		if (!m_errors.isEmpty()) {
			StringBuilder sb = new StringBuilder(
				"The following asserts failed:");
			
			for (Map.Entry<AssertionError, IAssert<?>> ae : m_errors.entrySet()) {
				sb.append("\n--------------------\n");
				sb.append("Message:\n");
				sb.append(ae.getKey().getMessage() + "\n");
				sb.append("Stacktrace:\n");
				sb.append(TestUtil.exceptionStackTrace(ae.getKey()));
			}
			
			sb.append("\n---- end of assertion failed list ----");
			
			throw new AssertionError(sb.toString());
		}
	}

}
