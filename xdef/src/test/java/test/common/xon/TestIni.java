package test.common.xon;

import java.util.Map;
import org.xdef.xon.XonUtils;
import org.xdef.sys.STester;

/** Test .properties and .ini files.
 * @author Vaclav Trojan
 */
public class TestIni extends STester {

	public TestIni() {super();}

	private static String MapToIni(final Map<String, Object> ini) {
		String s = "";
		for (String section:  ini.keySet()) {
			Object o = ini.get(section);
			if (o instanceof Map) {
				s += "[" + section + "]\n";
				Map item = (Map) ini.get(section);
				for (Object x:  item.keySet()) {
					o = item.get(x);
					if (o == null) {
						s += x + "=\n";
					} else {
						s += x + "=" + o + '\n';
					}
				}
			} else {
				if (o == null) {
					s += section + "=\n";
				} else {
					s += section + "=" + o + '\n';
				}
			}
		}
		return s;
	}

	private static String test(String ini) {
		Map<String, Object> map = XonUtils.parseINI(ini);
		String ini1 = XonUtils.toIniString(map);
		Map<String, Object> map1 = XonUtils.parseINI(ini1);
		if (!XonUtils.xonEqual(map, map1)) {
			return "Differs:\n" + ini + "\n===\n" + ini1;
		}
		return "";
	}

	/** Run test and print error information. */
	@Override
	public void test() {
		String s =
"address=dhcp\n" +
"dns = 192.168.1.1\n" +
"name = John E\\\n" +
" . \\\n" +
" Smith\n" +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.serverup=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
		assertEq("", test(s));
		s =
"#############################\n" +
"proxy type=0\n" +
"hostaddr=\n" +
"port=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.550";
		assertEq("", test(s));
		s =
" \n" +
" \t ###### comment 1 ########## \n" +
" \n" +
"# comment 2\n" +
" \n" +
" [X]\n" +
"#\n" +
"name = Novak \t\n\n" +
"email =\t\n" +
"  #;;;;;comment 3 ;;;;;;;; \t \n" +
" [X.Y] \t \n" +
" IP = 255.0.0.0 \n \n" +
"######\n";
		s = MapToIni(XonUtils.parseINI(s));
		assertEq("[X]\nname=Novak\nemail=\n[X.Y]\nIP=255.0.0.0\n", s);
		s = "#\n B = 1 \n C=2121-10-19\n D=2.121\n [E] \n[F]\n#";
		s = MapToIni(XonUtils.parseINI(s));
		assertEq("B=1\nC=2121-10-19\nD=2.121\n[E]\n[F]\n", s);
	}

	public static void main (String[] args) throws Exception {
		if (runTest(args) > 0) {System.exit(1);}
  }
}