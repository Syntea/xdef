package test.common.bnf;

import org.xdef.impl.code.DefEmail;
import org.xdef.sys.STester;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends STester {

	public TestEmailAddr() {super();}

	private String parse(String s) {
		try {
			new DefEmail(s);
			return s;
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			String s;
			s = "1@2";
			assertEq(s, parse(s));
			s = "<1E.-J@s-e_.z.cz>";
			assertEq(s, parse(s));
			s = "(ab)a@b";
			assertEq(s, parse(s));
			s = "a@b(ab)";
			assertEq(s, parse(s));
			s = "s b <a@b>";
			assertEq(s, parse(s));
			s = "(a (b c) b)a@b (de) (fg)";
			assertEq(s, parse(s));
			s = "(a (b c) b)a@b (de) (fg)";
			assertEq(s, parse(s));
			s = "El-,Ji. <EJ@sez.cz>";
			assertEq(s, parse(s));
			s = "=?UTF-8?B?RZZhIEt1xb5lbG92w6E=?= <ep@e.c>";
			assertEq(s, parse(s));
			s = "=?UTF-8?Q?P. B=C3=BDk?= <p@s>";
			assertEq(s, parse(s));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}