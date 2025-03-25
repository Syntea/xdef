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
"  <xd:declaration> external EmailAddr email; </xd:declaration>\n"+
"  <A> emailAddr(); finally email = getParsedValue(); </A>\n" +
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
		XDEmailAddr xe = (XDEmailAddr) xd.getVariable("email");
		if (xe == null) {
			return false;
		}
		if (user != null && !user.equals(xe.getUserName())) {
			System.err.println("" + xe.getUserName());
			return false;
		}
		return !(email != null && !(xe.getLocalPart()+"@"+xe.getDomain()).equals(email));
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		//valid
		assertTrue(parseEmail("1@2.t", "", "1@2.t"));
		assertTrue(parseEmail("a.b@a.b.t", "", "a.b@a.b.t"));
		assertTrue(parseEmail("a_b@a.b.t", "", "a_b@a.b.t"));
		assertTrue(parseEmail("a.b@a.b-c1.c-z", "", "a.b@a.b-c1.c-z"));
		assertTrue(parseEmail("a-b-d@z.t", "", "a-b-d@z.t"));
		assertTrue(parseEmail("E.F@z--c-x.cz", "", "E.F@z--c-x.cz")); // '--' in domain is OK!
		assertTrue(parseEmail("a.b-c1.č-z@a.b-c1.č-z.t", "", "a.b-c1.č-z@a.b-c1.č-z.t"));
		assertTrue(parseEmail("a@b.t(Jo Do)", "Jo Do", "a@b.t"));
		assertTrue(parseEmail("ěščřžýáůú.ĚŠČŘŽÝÁÚŹĹ@a.b-c1.c.t", "", "ěščřžýáůú.ĚŠČŘŽÝÁÚŹĹ@a.b-c1.c.t"));
		assertTrue(parseEmail("s-e_.z.cz@a.s-e.z.cz.t", "", "s-e_.z.cz@a.s-e.z.cz.t"));
		assertTrue(parseEmail("<1E.a-J@s-e.z.cz>", "", "1E.a-J@s-e.z.cz"));
		assertTrue(parseEmail("jíř.Ký@abc", "", "jíř.Ký@abc"));
		assertTrue(parseEmail("jíř+Ký@abc.t", "", "jíř+Ký@abc.t"));
		assertTrue(parseEmail("!jíř^^+??Ký=@abc.t", "", "!jíř^^+??Ký=@abc.t"));
		assertTrue(parseEmail("(áb) (cd) a@b.t", "ábcd", "a@b.t"));
		assertTrue(parseEmail("a@b.t(ab) ()", "ab", "a@b.t"));
		assertTrue(parseEmail("(ab) a@b.t (cd)", "abcd", "a@b.t"));
		assertTrue(parseEmail("a b <a@b.t>", "a b", "a@b.t"));
		assertTrue(parseEmail("(a (c d) b)<a@b.t> (ef) (gh)", "c da (c d) befgh", "a@b.t"));
		assertTrue(parseEmail("El-, Ji. <EJ@sez.cz.t>", "El-, Ji.", "EJ@sez.cz.t"));
		assertTrue(parseEmail("=?UTF-8?B?xb5lbG92w6E=?= <e@e.t>", "želová", "e@e.t"));
		assertTrue(parseEmail("=?UTF-8?Q?P. B=C3=BDk?= <p@s.t>", "P. Býk", "p@s.t"));
		assertTrue(parseEmail("(V. T. )<tr@vo.xz.t>(u)", "V. T. u", "tr@vo.xz.t"));
		assertTrue(parseEmail("(a b) \"V. T.\" (c d) <tr@vo.x.t> (u v)", "a b\"V. T.\"c du v", "tr@vo.x.t"));
		assertTrue(parseEmail("skybík@x.Xz.t", "", "skybík@x.Xz.t"));
		assertTrue(parseEmail("rkhbvs+rixo@xg.t", "", "rkhbvs+rixo@xg.t"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.t", "", "#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.t"));
		assertTrue(parseEmail("Joe.\\\\Blow@example.com.t", "", "Joe.\\\\Blow@example.com.t"));

/*#if RFC5321*#/
		assertTrue(parseEmail("\" \"@strange.ex.com.t", "", "\"\"@strange.ex.com.t"));
		assertTrue(parseEmail("js@[192.168.2.1]", "", "js@[192.168.2.1]"));
		assertTrue(parseEmail("u@[IPv6:2001:db8::1]", "", "u@[IPv6:2001:db8::1]"));
		assertTrue(parseEmail("\"a ? b\"@gz.com.t", "", "\"a?b\"@gz.com.t"));
		assertTrue(parseEmail("\"a \\\" b\"@gz.com.t", "", "\"a\\\"b\"@gz.com.t"));
		assertTrue(parseEmail("\"much.more unusual\"@example.c.t", "", "\"much.moreunusual\"@example.c.t"));
		assertTrue(parseEmail("\"very.unusual.@.unusual.com\"@e.t", "","\"very.unusual.@.unusual.com\"@e.t"));
		assertTrue(parseEmail("\"very.(),:;<>[]\\\".VERY.\\\"very@\\ \\\"very\\\".unusual\"@s.com.t",
			"", "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\\"very\\\".unusual\"@s.com.t"));
		assertTrue(parseEmail("#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.org.t", "", "#!$%&'*+-/=?^_`{}|~.ÁŽúů@ex.org.t"));
		assertTrue(parseEmail("Joe.\\@Blow@example.com.t", "", "Joe.\\@Blow@example.com.t"));
		assertTrue(parseEmail("Joe.\\ Blow@example.com.t", "", "Joe.\\Blow@example.com.t"));
		assertFalse(parseEmail("!\\\\@@example.com.t", null, null));
		assertTrue(parseEmail("!\\@+@example.com.t", "", "!\\@+@example.com.t"));
/*#end*/

		//invalid
		assertFalse(parseEmail("1.2", null, null)); //missing '@'
		assertFalse(parseEmail("(ab)ab", null, null)); //missing '@'
		assertFalse(parseEmail("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)", null, null)); //missing '@'
		assertFalse(parseEmail("(a b (c d) <tr@vo.xz>", null, null)); //missing ')'
		assertFalse(parseEmail("a b) (c d) <tr@vo.xz>", null, null)); //missing '{'
		assertFalse(parseEmail("E\\@x@z>", null, null)); // illegal '\@'
		assertFalse(parseEmail("E@z>", null, null)); // illegal '>'
		assertFalse(parseEmail(">E@z", null, null)); // illegal '>'
		assertFalse(parseEmail("E@z<", null, null)); // illegal '<'
		assertFalse(parseEmail("<E@z.cz", null, null)); // '<' not closed with '>'
		assertFalse(parseEmail(".Joe.Blow@example.com", null, null)); // local part starts with '.'
		assertFalse(parseEmail("Joe.Blow.@example.com", null, null)); // local part ends with '.'
		assertFalse(parseEmail("-Joe.Blow@example.com", null, null)); // local part starts with '-'
		assertFalse(parseEmail("Joe.Blow-@example.com", null, null)); // local part ends with '-'
		assertFalse(parseEmail("Joe..Blow@example.com", null, null)); // '..' in local part
		assertFalse(parseEmail("Joe.-Blow@example.com", null, null)); // '.-' in local part
		assertFalse(parseEmail("E.F@z..cz", null, null)); // '..' in domain
		assertFalse(parseEmail("E.F@-z.cz", null, null)); // domain starts with '-'
		assertFalse(parseEmail("E.F@z.cz-", null, null)); // domain ends with '-'
		assertFalse(parseEmail("E.F@.z.cz", null, null)); // domain starts with '.'
		assertFalse(parseEmail("E.F@z.cz.", null, null)); // domain ends with '.'
		assertFalse(parseEmail("E.F@z.-cz", null, null)); // '.-' in domain
		assertFalse(parseEmail("E.F@z-.cz", null, null)); // '-.' in domain
		assertFalse(parseEmail("E.F@z_cz", null, null)); // '_' in domain
		assertFalse(parseEmail("E.F@z!cz", null, null)); // '!' in domain

/*#if !RFC5321*/
		// RFC 2822?
		assertFalse(parseEmail("js@[192.168.2.1]", null, null)); // IP address not allowed
		assertFalse(parseEmail("u@[IPv6:2001:db8::1]", null, null)); // IP address not allowed
		assertFalse(parseEmail("\" \"@strange.ex.com.t", null, null)); // Quoted_string is illegal
		assertFalse(parseEmail("Joe.\\@Blow@example.com.t", null, null)); // Escape character not allowed
		assertFalse(parseEmail("Joe--Blow@example.com", null, null)); // '--' in local part
		assertFalse(parseEmail("a--b-d@z.t", null, null)); // '--' in local part
		assertFalse(parseEmail("!\\@+@example.com.t", null, null)); // more then one '@'
		assertFalse(parseEmail("!\\\\@@example.com.t", null, null)); // more then one '@'
/*#end*/
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
