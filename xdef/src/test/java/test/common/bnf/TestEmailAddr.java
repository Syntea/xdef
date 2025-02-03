package test.common.bnf;

import org.xdef.XDEmailAddr;
import org.xdef.XDParseResult;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.parsers.XDParseEmailAddr;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.runTest;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends STester {

	public TestEmailAddr() {super();}

	private XDEmailAddr parse(final String s) {
		XDParseResult q = new DefParseResult(s);
		new XDParseEmailAddr().check(null, q);
		if (q.errors()) {
			return null;
		}
		return (XDEmailAddr) q.getParsedValue();
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		//valid
		assertNotNull(parse("1@2"));
		assertNotNull(parse("a.b@a.b-c1.cz"));
		assertNotNull(parse("\\\"\\\\!#$%&'*+/=?^`{|}~@[IPv6:2001:db8::1]"));
		assertNotNull(parse("ěščřžýáůú.ĚŠČŘ?ÝÁ?ŹĹ@a.b-c1.cz"));
		assertNotNull(parse("\"a b\"@[1.255.0.99]"));
		assertNotNull(parse("s-e_.z.cz@s-e_.z.cz"));
		assertNotNull(parse("\\\"\\\\!#$%&'*+/=?^`{|}~@a.b-c1.cz"));
		assertNotNull(parse("<1E.a-J@s-e_.z.cz>"));
		assertNotNull(parse("jiří.Kamenický@abcd"));
		assertNotNull(parse("jiří+Kamenický@abcd"));
		assertNotNull(parse("#!$%&'*+-/=?^_`{}|~@example.org"));
		assertNotNull(parse("1@[0.00.000.9]"));
		assertNotNull(parse("1@[IPv6:2001:db8::1]"));
		assertNotNull(parse("(ab) (cd) a@b"));
		assertNotNull(parse("a@b(ab) (cd)"));
		assertNotNull(parse("a b <a@b>"));
		assertNotNull(parse("(a (c d) b)a@b (ef) (gh)"));
		assertNotNull(parse("#!$%&'*+-/=?^_`{}|~@example.org"));
		assertNotNull(parse("El-,Ji. <EJ@sez.cz>"));
		assertNotNull(parse("=?UTF-8?B?xb5lbG92w6E=?= <ep@e.c>"));
		assertNotNull(parse("=?UTF-8?Q?P. B=C3=BDk?= <p@s>"));
		assertNotNull(parse("(V. T.)<tr@vo.xz>(u)"));
		assertNotNull(parse("(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)"));
		assertNotNull(parse("xample-indeed@strange-example.com"));
		assertNotNull(parse("jsmith@[192.168.2.1]"));
		assertNotNull(parse("user@[IPv6:2001:db8::1]"));
		assertNotNull(parse("skybík@xesto.cz"));
		assertNotNull(parse("rkhbvs+rixo@xgmail.com"));
		assertNotNull(parse("#!$%&'*+-/=?^_`{}|~@example.org"));
		assertNotNull(parse("\" \"@strange.ex.com"));
		assertNotNull(parse("\"a ? b\"@gmail.com"));
		assertNotNull(parse("\"a \\\" b\"@gmail.com"));
		assertNotNull(parse("\"much.more unusual\"@example.com"));
		assertNotNull(parse("\"very.unusual.@.unusual.com\"@example.com"));
		assertNotNull(parse("\"very.(),:;<>[]\\\".VERY.\\\"very@\\ \\\"very\\\".unusual\"@strange.ex.com"));
		assertNotNull(parse("#!$%&'*+-/=?^_`{}|~.ÁŽúů@example.org"));
		assertNotNull(parse("Joe.\\\\Blow@example.com"));
		assertNotNull(parse("Joe.\\@Blow@example.com"));
		assertNotNull(parse("Joe.\\ Blow@example.com"));

		//invalid
		assertNull(parse("1.2"));
		assertNull(parse(".john.doe@example.com/>"));
		assertNull(parse("(ab)ab"));
		assertNull(parse("<1E.-J-s-e_.z.cz>"));
		assertNull(parse("E.F@z.cz>"));
		assertNull(parse("<E.F@z.cz"));
		assertNull(parse("E..F@z.cz"));
		assertNull(parse("-E.F@z.cz"));
		assertNull(parse("E.F-@z.cz"));
		assertNull(parse("E.F@-z.cz"));
		assertNull(parse("E.F@z.cz-"));
		assertNull(parse("E.F@z..cz"));
		assertNull(parse("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)"));
		assertNull(parse("john.@example.com"));
		assertNull(parse(".john@example.com"));
		assertNull(parse("jo..hn@example.com"));
		assertNull(parse("john@example.com-"));
		assertNull(parse("john@-example.com"));
		assertNull(parse("john@example..com"));
		assertNull(parse("john@example.com."));
		assertNull(parse("john@.example.com"));

		// test parsed result
		XDEmailAddr p;
		p = parse("a@b(John Doe)");
		assertEq("John Doe", p.getUserName());
		assertEq("a@b", p.getLocalPart() + "@" + p.getDomain());
		p = parse("(John Doe)a@b");
		assertEq("John Doe", p.getUserName());
		assertEq("a@b", p.getLocalPart() + "@" + p.getDomain());
		p = parse("John Doe<a@b>");
		assertEq("John Doe", p.getUserName());
		assertEq("a@b", p.getLocalPart() + "@" + p.getDomain());
		p = parse("(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)");
		assertEq("a b\"V. T.\"c du v", p.getUserName());
		assertEq("tr@vo.xz", p.getLocalPart() + "@" + p.getDomain());
		p = parse("(ab) (cd) a@b");
		assertEq("abcd", p.getUserName());
		assertEq("a@b", p.getLocalPart() + "@" + p.getDomain());
		p = parse("a@b(ab) (cd)");
		assertEq("abcd", p.getUserName());
		assertEq("a@b", p.getLocalPart() + "@" + p.getDomain());
		p = parse("=?UTF-8?B?xb5lbG92w6E=?= <ep@e.c>");
		assertEq("želová", p.getUserName());
		assertEq("ep@e.c", p.getLocalPart() + "@" + p.getDomain());
		p = parse("=?UTF-8?B?SmnFmcOtLks=?= <a@example.com>");
		assertEq("Jiří.K", p.getUserName());
		assertEq("a@example.com", p.getLocalPart() + "@" + p.getDomain());
		p = parse("=?UTF-8?Q?P. B=C3=BDk?= <p@s>");
		assertEq("P. Býk", p.getUserName());
		assertEq("p@s", p.getLocalPart() + "@" + p.getDomain());
		p = parse("\"a \\\" b\"@gmail.com");
		assertEq("", p.getUserName());
		assertEq("\"a\\\"b\"@gmail.com", p.getLocalPart() + "@" + p.getDomain());
		p = parse("#!$%&'*+-/=?^_`{}|~@example.org");
		assertEq("", p.getUserName());
		assertEq("#!$%&'*+-/=?^_`{}|~@example.org", p.getLocalPart() + "@" + p.getDomain());
		p = parse("\"v.(),:;<>[]\\\".V.\\\"v@\\ \\\"v\\\".u\"@strange");
		assertEq("", p.getUserName());
		assertEq("\"v.(),:;<>[]\\\".V.\\\"v@\\\\\"v\\\".u\"@strange", p.getLocalPart() + "@" + p.getDomain());
		p = parse("Joe.\\@Blow@example.com");
		assertEq("", p.getUserName());
		assertEq("Joe.\\@Blow@example.com", p.getLocalPart() + "@" + p.getDomain());
		p = parse("user@[200.1.99.0]");
		assertEq("", p.getUserName());
		assertEq("user@[200.1.99.0]", p.getLocalPart() + "@" + p.getDomain());
		p = parse("\\\"\\\\!#$%&'*+/=?^`{|}~@[IPv6:2001:db8::1]");
		assertEq("", p.getUserName());
		assertEq("\\\"\\\\!#$%&'*+/=?^`{|}~@[IPv6:2001:db8::1]", p.getLocalPart() + "@" + p.getDomain());
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}