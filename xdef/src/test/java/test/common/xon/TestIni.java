package test.common.xon;

import java.util.Map;
import org.xdef.xon.XonUtils;
import org.xdef.sys.STester;

/** Test .properties and .ini files.
 * @author Vaclav Trojan
 */
public class TestIni extends STester {

	public TestIni() {super();}

	private static String test(String ini) {
		Map<String, Object> map = XonUtils.parseINI(ini);
		String ini1 = XonUtils.toIniString(map);
		Map<String, Object> map1 = XonUtils.parseINI(ini1);
		if (!XonUtils.xonEqual(map, map1)) {
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
		String data =
" \t ###### comment 1 ########## \n" +
"; comment 2\n" +
" [X]\n" +
"name = Novak \t\n\n" +
"email =\t\n" +
"  ;;;;;;comment 3 ;;;;;;;; \n" +
"[X.Y]\n" +
" IP = 255.0.0.0\n";
		Map<String, Object> ini = XonUtils.parseINI(data);
		data = "";
		for (String section:  ini.keySet()) {
			data += "[" + section + "]\n";
			Map item = (Map) ini.get(section);
			for (Object x:  item.keySet()) {
				s = (String) item.get(x);
				if (s == null) {
					s = "";
				}
				data += x + "=" + s + '\n';
			}
		}
		assertEq("[X]\nname=Novak\nemail=\n[X.Y]\nIP=255.0.0.0\n", data);
	}

	public static void main (String[] args) throws Exception {
		if (runTest(args) > 0) {System.exit(1);}
  }
}