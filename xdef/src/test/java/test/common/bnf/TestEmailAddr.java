package test.common.bnf;

import org.xdef.impl.parsers.XDParseEmailAddr;
import org.xdef.sys.STester;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends STester {

	public TestEmailAddr() {super();}

	private boolean parse(String s) {
		XDParseEmailAddr p = new XDParseEmailAddr();
		return !p.check(null, s).errors();
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		assertTrue(parse("jsmith@[192.168.2.1]"));
		//valid
		assertTrue(parse("1@2"));
		assertTrue(parse("<1E.-J@s-e_.z.cz>"));
		assertTrue(parse("(ab)a@b"));
		assertTrue(parse("(ab) (cd) a@b"));
		assertTrue(parse("a@b(ab) (cd)"));
		assertTrue(parse("a b <a@b>"));
		assertTrue(parse("(a (c d) b)a@b (ef) (gh)"));
		assertTrue(parse("El-,Ji. <EJ@sez.cz>"));
		assertTrue(parse("=?UTF-8?B?xb5lbG92w6E=?= <ep@e.c>"));
		assertTrue(parse("=?UTF-8?Q?P. B=C3=BDk?= <p@s>"));
		assertTrue(parse("(V. T.)<tr@vo.xz>(u)"));
		assertTrue(parse("(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)"));
		assertTrue(parse("xample-indeed@strange-example.com"));
		assertTrue(parse("jsmith@[192.168.2.1]"));
		assertTrue(parse("user@[IPv6:2001:db8::1]"));
		//RFC 5322
		assertTrue(parse("skybík@esto.cz"));
		assertTrue(parse(" jiří . Kamenický@abcd"));
		assertTrue(parse("rkhbvs+rixo@gmail.com"));
		assertTrue(parse("#!$%&'*+-/=?^_`{}|~@example.org"));
		assertTrue(parse("\" \"@strange.ex.com"));
		assertTrue(parse("\"a ? b\"@gmail.com"));
		assertTrue(parse("\"a \\\" b\"@gmail.com"));
		assertTrue(parse("\"much.more unusual\"@example.com"));
		assertTrue(parse("\"very.unusual.@.unusual.com\"@example.com"));
		assertTrue(parse("\"very.(),:;<>[]\\\".VERY.\\\"very@\\ \\\"very\\\".unusual\"@strange.ex.com"));
		assertTrue(parse("\"()<>[]:,;@\\\\\\\"!#$%&'-/=?^_`{}| ~.ÁŽúů\"@example.org"));
		assertTrue(parse("Joe.\\\\Blow@example.com"));
		assertTrue(parse("Joe.\\@Blow@example.com"));
		assertTrue(parse("Joe.\\ Blow@example.com"));
		assertTrue(parse("Áá!#$%&'-=?^_`{}|~\\ \\.\\\".-.\\[\\]\\<\\>\\(\\)\\,\\:\\/\\/Ü@example.com"));

		//invalid
		assertTrue(!parse("1.2"));
		assertTrue(!parse("<1E.-J-s-e_.z.cz>"));
		assertTrue(!parse("(ab)ab"));
		assertTrue(!parse("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)"));
		assertTrue(!parse("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)"));
		assertTrue(!parse(".john.doe@example.com/>"));
		assertTrue(!parse("john..doe@example.com"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}