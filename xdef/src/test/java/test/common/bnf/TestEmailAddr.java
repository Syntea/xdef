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
			ex.printStackTrace();
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			String s;
			s = "1@2";
			DefEmailAddr d;
			assertNotNull(d = parse(s));
			s = "<1E.-J@s-e_.z.cz>";
			assertNotNull(d = parse(s));
			s = "(ab)a@b";
			assertNotNull(d = parse(s));
			s = "(ab) (cd) a@b";
			assertNotNull(d = parse(s));
			s = "a@b(ab) (cd)";
			assertNotNull(d = parse(s));
			s = "a b <a@b>";
			assertNotNull(d = parse(s));
			s = "(a (c d) b)a@b (ef) (gh)";
			assertNotNull(d = parse(s));
			s = "El-,Ji. <EJ@sez.cz>";
			assertNotNull(d = parse(s));
			s = "=?UTF-8?B?xb5lbG92w6E=?= <ep@e.c>";
			assertNotNull(d = parse(s));
			s = "=?UTF-8?Q?P. B=C3=BDk?= <p@s>";
			assertNotNull(d = parse(s));
			s = "(V. T.)<tr@vo.xz>(u)";
			assertNotNull(d = parse(s));
			s = "(a b) \"V. T.\" (c d) <tr@vo.xz> (u v)";
			assertNotNull(d = parse(s));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}