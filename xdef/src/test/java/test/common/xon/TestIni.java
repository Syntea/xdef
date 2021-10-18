package test.common.xon;

import java.util.Map;
import org.xdef.xon.XonUtil;
import org.xdef.sys.STester;

/** Test .properties and .ini files. 
 * @author Vaclav Trojan
 */
public class TestIni extends STester {

	public TestIni() {super();}

	private static String test(String ini) {
		Map<String, Object> map = XonUtil.parseINI(ini);
		String ini1 = XonUtil.toIniString(map);
		Map<String, Object> map1 = XonUtil.parseINI(ini1);
		if (!XonUtil.xonEqual(map, map1)) {
			return "Differs:\n" + ini + "\n===\n" + ini1;
		}
		return "";
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String s =
"#this is INI file comment\n" +
"address=dhcp\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +	
"servertool.serverup=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
		assertEq("", test(s));
		s =
"proxy type=0\n" +
"hostaddr=\n" +
"port=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.550";
		assertEq("", test(s));
	}

	public static void main (String[] args) throws Exception {
		if (runTest(args) > 0) {System.exit(1);}
  }
}
