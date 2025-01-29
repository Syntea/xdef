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
		//RFC 5322
		assertTrue(parse("skybík@esto.cz"));
		assertTrue(parse(" jiří . Kamenický@ abc . čž "));
		assertTrue(parse("rkhbvs+rixo@gmail.com"));
		//invalid
		assertTrue(!parse("1.2"));
		assertTrue(!parse("<1E.-J-s-e_.z.cz>"));
		assertTrue(!parse("(ab)ab"));
		assertTrue(!parse("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)"));
		assertTrue(!parse("(a b) \"V. T.\" (c d) <tr.vo.xz> (u v)"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}