package test.common.bnf;

import org.xdef.impl.code.DefEmailAddr;
import org.xdef.sys.STester;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends STester {

	public TestEmailAddr() {super();}

	private DefEmailAddr parse(String s) {
		try {
			DefEmailAddr x = new DefEmailAddr(s);
//			System.out.println("'" + x.getUserName() + "'; "+x.getEmailAddr());
			return x;
		} catch (Exception ex) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			assertNotNull(parse("1@2"));
			assertNotNull(parse("<1E.-J@s-e_.z.cz>"));
			assertNotNull(parse("(ab)a@b"));
			assertNotNull(parse("(ab) (cd) a@b"));
			assertNotNull(parse("a@b(ab) (cd)"));
			assertNotNull(parse("a b <a@b>"));
			assertNotNull(parse("(a (c d) b)a@b (ef) (gh)"));
			assertNotNull(parse("El-,Ji. <EJ@sez.cz>"));
			assertNotNull(parse("=?UTF-8?B?xb5lbG92w6E=?= <ep@e.c>"));
			assertNotNull(parse("=?UTF-8?Q?P. B=C3=BDk?= <p@s>"));
			assertNotNull(parse("(V. T.)<tr@vo.xz>(u)"));
			assertNotNull(parse("(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)"));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}