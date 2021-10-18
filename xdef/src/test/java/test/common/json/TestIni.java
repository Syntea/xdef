package test.common.json;

import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import org.xdef.xon.IniReader;
import org.xdef.xon.XonUtil;
import org.xdef.xon.XonReader;
import org.xdef.sys.STester;

/** Test .properties and .ini files. 
 * @author Vaclav Trojan
 */
public class TestIni extends STester {

	public TestIni() {super();}
	
	private static String toPropertyString(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch(ch) {
				case '\\' : sb.append("\\\\"); continue;
				case '\t' : sb.append("\\t"); continue;
				case '\n' : sb.append("\\n"); continue;
				default :
					if (ch >= ' ' && ch <= 127) {
						sb.append(ch);
					} else {
						sb.append("\\u");
						for (int x = 12; x >= 0; x -=4) {
							sb.append("0123456789ABCDEF".charAt((ch >> x)&0xf));
						}
					}
			}
		}
		return sb.toString();
	}
	
	private static String toPropertyLine(String name, String value) {
		return toPropertyString(name) + "=" + toPropertyString(value) + "\n";
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseIni(String ini) {
		IniReader rdr = new IniReader(
			new StringReader(ini), new XonReader.ObjParser());
		rdr.parse();
		return (Map<String, Object>) rdr.getValue();
	}
	
	@SuppressWarnings("unchecked")
	private static String iniToString(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> x: map.entrySet()) {
			Object val = ((Entry)x).getValue();
			if (val instanceof String) {
				sb.append(toPropertyLine(x.getKey(), (String) val));
			}
		}
		for (Entry<String, Object> x: map.entrySet()) {
			Object val = x.getValue();
			if (val instanceof Map) {
				sb.append('[').append(x.getKey()).append("]\n");
				for (Entry<String, Object> y 
					: ((Map<String, Object>) val).entrySet()) {
					sb.append(toPropertyLine(y.getKey(), (String) y.getValue()));
				}
			}
		}
		return sb.toString();
	}
	
	private static String test(String ini) {
		Map<String, Object> map = parseIni(ini);
		String ini1 = iniToString(map);
		Map<String, Object> map1 = parseIni(ini1);
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
