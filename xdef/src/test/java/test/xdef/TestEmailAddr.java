package test.xdef;

import org.xdef.XDDocument;
import org.xdef.XDEmailAddr;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends XDTester {

	public TestEmailAddr() {super();}

	private final XDPool _xp = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration> external EmailAddr e;</xd:declaration>\n"+
"  <A> emailAddr(); finally e = getParsedValue(); </A>\n" +
"</xd:def>");

	/** Parse and check email address in XML data.
	 * @param s source format of email address.
	 * @param user expected name or null (then it is not checked).
	 * @param email expected mailbox or null (then it is not checked).
	 * @return true if no error was found.
	 */
	private boolean parseEmail(final String s, final String user, final String email) {
		String xml = "<A>"+SUtils.modifyString(SUtils.modifyString(s, "&", "&amp;"), "<", "&lt;")+"</A>";
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd = _xp.createXDDocument();
		parse(xd, xml, reporter);
		if (reporter.errors()) {
			return false;
		}
		XDEmailAddr xe = (XDEmailAddr) xd.getVariable("e");
		if (xe == null) {
			return false;
		}
		if (user != null && !user.equals(xe.getUserName())) {
			System.err.println("" + xe.getUserName());
			return false;
		}
		return !(email != null && !(xe.getLocalPart()+"@"+xe.getDomain()).equals(email));
//		if ((mail != null && !(xe.getLocalPart()+"@"+xe.getDomain()).equals(mail))) {
//			System.err.println(xe.getLocalPart()+"@"+xe.getDomain());
//			return false;
//		}
//		return true;
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		//valid
		XDEmailAddr xe;
		assertTrue(parseEmail("1@2", "", "1@2"));
		assertTrue(parseEmail("a.b@a.b-c1.cz", "", "a.b@a.b-c1.cz"));
		assertTrue(parseEmail("a@b(John Doe)", "John Doe", "a@b"));
		assertTrue(parseEmail("\\\"\\\\!#$%&'*+/=?^`{|}~@[IPv6:2001:db8::1]", "",
			"\\\"\\\\!#$%&'*+/=?^`{|}~@[IPv6:2001:db8::1]"));
		assertTrue(parseEmail("ěščřžýáůú.ĚŠČŘŽÝÁÚŹĹ@a.b-c1.cz", "", "ěščřžýáůú.ĚŠČŘŽÝÁÚŹĹ@a.b-c1.cz"));
		assertTrue(parseEmail("\"a b\"@[1.255.0.99]", "", "\"ab\"@[1.255.0.99]"));
		assertTrue(parseEmail("s-e_.z.cz@s-e_.z.cz", "", "s-e_.z.cz@s-e_.z.cz"));
		assertTrue(parseEmail("\\\"\\\\!#$%&'*+/=?^`{|}~@a.b-c1.cz",
			"", "\\\"\\\\!#$%&'*+/=?^`{|}~@a.b-c1.cz"));
		assertTrue(parseEmail("<1E.a-J@s-e_.z.cz>", "", "1E.a-J@s-e_.z.cz"));
		assertTrue(parseEmail("jiří.Kamenický@abcd", "", "jiří.Kamenický@abcd"));
		assertTrue(parseEmail("jiří+Kamenický@abcd", "", "jiří+Kamenický@abcd"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~@e", "", "#!$%&'*+-/=?^_`{}|~@e"));
		assertTrue(parseEmail("1@[0.00.000.9]", "", "1@[0.00.000.9]"));
		assertTrue(parseEmail("1@[IPv6:2001:db8::1]", "", "1@[IPv6:2001:db8::1]"));
		assertTrue(parseEmail("(ab) (cd) a@b", "abcd", "a@b"));
		assertTrue(parseEmail("a@b(ab) (cd)", "abcd", "a@b"));
		assertTrue(parseEmail("(ab) a@b (cd)", "abcd", "a@b"));
		assertTrue(parseEmail("a b <a@b>", "a b", "a@b"));
		assertTrue(parseEmail("(a (c d) b)a@b (ef) (gh)", "c da (c d) befgh", "a@b"));
		assertTrue(parseEmail("(a (c d) b)a@b (ef) (gh)", "c da (c d) befgh", "a@b"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~@e", "", "#!$%&'*+-/=?^_`{}|~@e"));
		assertTrue(parseEmail("El-,Ji. <EJ@sez.cz>", "El-,Ji.", "EJ@sez.cz"));
		assertTrue(parseEmail("=?UTF-8?B?xb5lbG92w6E=?= <e@e.c>", "želová", "e@e.c"));
		assertTrue(parseEmail("=?UTF-8?Q?P. B=C3=BDk?= <p@s>", "P. Býk", "p@s"));
		assertTrue(parseEmail("(V. T.)<tr@vo.xz>(u)", "V. T.u", "tr@vo.xz"));
		assertTrue(parseEmail("(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)", "a b\"V. T.\"c du v", "tr@vo.xz"));
		assertTrue(parseEmail("xample-indeed@strange-example.com", "", "xample-indeed@strange-example.com"));
		assertTrue(parseEmail("jsmith@[192.168.2.1]", "", "jsmith@[192.168.2.1]"));
		assertTrue(parseEmail("user@[IPv6:2001:db8::1]", "", "user@[IPv6:2001:db8::1]"));
		assertTrue(parseEmail("skybík@xesto.cz", "", "skybík@xesto.cz"));
		assertTrue(parseEmail("rkhbvs+rixo@xgmail.com", "", "rkhbvs+rixo@xgmail.com"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~@example", "", "#!$%&'*+-/=?^_`{}|~@example"));
		assertTrue(parseEmail("\" \"@strange.ex.com", "", "\"\"@strange.ex.com"));
		assertTrue(parseEmail("\"a ? b\"@gmail.com", "", "\"a?b\"@gmail.com"));
		assertTrue(parseEmail("\"a \\\" b\"@gmail.com", "", "\"a\\\"b\"@gmail.com"));
		assertTrue(parseEmail("\"much.more unusual\"@example.com", "", "\"much.moreunusual\"@example.com"));
		assertTrue(parseEmail("\"very.unusual.@.unusual.com\"@e", "", "\"very.unusual.@.unusual.com\"@e"));
		assertTrue(parseEmail("\"very.(),:;<>[]\\\".VERY.\\\"very@\\ \\\"very\\\".unusual\"@strange.com",
			"", "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\\"very\\\".unusual\"@strange.com"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.org", "", "#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.org"));
		assertTrue(parseEmail("Joe.\\\\Blow@example.com", "", "Joe.\\\\Blow@example.com"));
		assertTrue(parseEmail("Joe.\\@Blow@example.com", "", "Joe.\\@Blow@example.com"));
		assertTrue(parseEmail("Joe.\\ Blow@example.com", "", "Joe.\\Blow@example.com"));

		//invalid
		assertFalse(parseEmail("1.2", null, null)); //missing '@'
		assertFalse(parseEmail("(ab)ab", null, null)); //missing '@'
		assertFalse(parseEmail("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)", null, null)); //missing '@'
		assertFalse(parseEmail("(a b (c d) <tr@vo.xz>", null, null)); //missing ')'
		assertFalse(parseEmail("a b) (c d) <tr@vo.xz>", null, null)); //missing '{'
		assertFalse(parseEmail("E.F@z.cz>", null, null)); // '>' without '<'
		assertFalse(parseEmail("<E.F@z.cz", null, null)); // '>' without '>'
		assertFalse(parseEmail(".Joe.Blow@example.com", null, null)); // local part starts witn '.'
		assertFalse(parseEmail("Joe.Blow.@example.com", null, null)); // local part ends witn '.'
		assertFalse(parseEmail("-Joe.Blow@example.com", null, null)); // local part starts witn '-'
		assertFalse(parseEmail("Joe.Blow-@example.com", null, null)); // local part ends witn '-'
		assertFalse(parseEmail("Joe..Blow@example.com", null, null)); // '..' in local part
		assertFalse(parseEmail("Joe--Blow@example.com", null, null)); // '--' in local part
		assertFalse(parseEmail("Joe.-Blow@example.com", null, null)); // '.-' in local part
		assertFalse(parseEmail("E.F@z--cz", null, null)); // '--' in domain
		assertFalse(parseEmail("E.F@-z.cz", null, null)); // domain starts witn '-'
		assertFalse(parseEmail("E.F@z.cz-", null, null)); // domain ends witn '-'
		assertFalse(parseEmail("E.F@.z.cz", null, null)); // domain starts witn '.'
		assertFalse(parseEmail("E.F@z.cz.", null, null)); // domain ends witn '.'
		assertFalse(parseEmail("E.F@z..cz", null, null)); // '..' in domain
		assertFalse(parseEmail("E.F@z.-cz", null, null)); // '.-' in domain
		assertFalse(parseEmail("E.F@z-.cz", null, null)); // '-.' in domain
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
