package mytests;

import org.xdef.xon.XonUtils;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;
import org.xdef.xon.XonTools;
import org.yaml.snakeyaml.Yaml;

/** YAML example.
 * @author Vaclav Trojan
 */
public class YamlExample {

	/** Remove white spaces at given position from the StringBuilder.
	 * @param sb StringBuilder object.
	 * @param pos position in the StringBuilder where removfe spaces.
	 */
	private static void removeSpaces(final StringBuilder sb, int pos) {
		char c;
		while ((c = sb.charAt(pos)) == ' ' || c == 9 || c == 10 || c == 13) {
			sb.deleteCharAt(pos);
		}
	}

	/** Create XON model from YAML model.
	 * @param yaml YAML model.
	 * @return string with XON model.
	 * @throws Exception
	 */
	private static String toXonModel(final String yaml) throws Exception {
		Object o = new Yaml().load(yaml); // parse YAML to object
		StringBuilder sb = new StringBuilder(XonUtils.toJsonString(o, true));
//		StringBuilder sb = new StringBuilder(XonUtils.toJsonString(o));
		char c;
		int i = 0;
		while ((i = sb.indexOf("\"%script\"", i)) >= 0) {
			sb.replace(i, i+9, "%script");
			removeSpaces(sb, i += 7); //remove white spaces from position i
			if (sb.charAt(i) == ':') {
				sb.replace(i, ++i, "=");
			}
		}
		i = 0;
		while ((i = sb.indexOf("\"%script", i)) >= 0) {
			sb.replace(i, i+8, "%script");
			removeSpaces(sb, i += 7); //remove white spaces from position i
			if ((c = sb.charAt(i)) == '=') {
				removeSpaces(sb, ++i); //remove white spaces from position i
				sb.insert(i++, " \"");
			} else if (c == '\"') { // error! this should not happen!
				sb.insert(i++, " \"");
			}
		}
		i = 0;
		while ((i = sb.indexOf("\"%anyName\"", i)) >= 0) {
			sb.replace(i, i+10, "%anyName");
			removeSpaces(sb, i += 8); //remove white spaces from position i
			if (sb.charAt(i) == ':') {
				sb.replace(i, ++i, ":");
			}
		}
		i = 0;
		while ((i = sb.indexOf("\"%anyObj", i)) >= 0) {
			sb.replace(i, i= i+8, "%anyObj");
			removeSpaces(sb, i += 8); //remove white spaces from position i
			if ((c=sb.charAt(i++)) == '=') {
				removeSpaces(sb, i); //remove white spaces from position i
				sb.insert(i++, "\"");
			} else if (c == '\"') { // only %anyObj
				sb.deleteCharAt(i);
			}
		}
		i = 0;
		while ((i = sb.indexOf("\"%oneOf", i)) >= 0) {
			sb.replace(i, i+7, "%oneOf");
			removeSpaces(sb, i += 6); //remove white spaces from position i
			if ((c=sb.charAt(i)) == '=') {
				removeSpaces(sb, ++i); //remove white spaces from position i
				sb.insert(i, "\"");
			} else if (c == '\"') {
				sb.deleteCharAt(i);
			}
		}
/* check if result is correct model *
		String xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" xd:root=\"A\">\n" +
"  <xd:json xd:name = \"A\" >\n" + sb.toString() +
"\n  </xd:json>\n" +
"</xd:def>";
//		System.out.println(xdef);
		XDFactory.compileXD(null, xdef);
/**/
		return sb.toString();
	}
////////////////////////////////////////////////////////////////////////////////
// YAML parser
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////



	private static class YamlCommand {
		int _indent;
		char _type;
		Object _value;

		public final String valueToJSON() {
			if (_value == null) {
				return "null";
			} else if (_value instanceof String) {
				return XonTools.jstringToSource((String) _value);
			} else {
				return _value.toString();
			}
		}

		@Override
		public final String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < _indent; i++) sb.append(' ');
			switch (_type) {
				case '-':
					sb.append("- ");
					sb.append(valueToJSON());
					break;
				case ':':
					sb.append(XonTools.jstringToSource((String) _value));
					sb.append(':');
					break;
				default:
					valueToJSON();
					break;
			}
			sb.append('\n');
			return sb.toString();
		}
	}

	private static YamlCommand readCommand(final StringParser p) {
		YamlCommand command = new YamlCommand();
		if (p.eos()) {
			command._indent = -1;
			return command;
		}
		int pos = p.getIndex();
		while (p.isChar(' ')) {
			command._indent++;
		}
		String result = p.findChar('\n') ? p.getParsedBufferPartFrom(pos) : p.getBufferPartFrom(pos);
		p.nextChar();
		pos = result.length() - 1;
		for (; pos >= command._indent; pos--) {
			char ch = result.charAt(pos);
			if (ch == '#') {
				result = result.substring(0, pos);
				pos = result.length() - 1;
			}
		}
		pos = result.length() - 1;
		for (; pos >= command._indent; pos--) {
			if (result.charAt(pos) <= ' ') {
				result = result.substring(0, pos);
			} else {
				break;
			}
		}

		if (!result.isEmpty()) {
			StringParser pp = new StringParser(result.trim());
			if (pp.isChar('-')) {
				command._type = '-';
				pp.isSpaces();
			}
			char c;
			String s = "";
			if ((c = pp.isOneOfChars("'\"")) != SParser.NOCHAR) {//quoted string
				for(;;) {
					if (pp.isChar(c)) {
						if (pp.isChar(c)) {
							s += c;
						} else {
							break;
						}
					} else if (pp.eos()) {
						throw new RuntimeException("missing end of string");
					} else {
						s += pp.getCurrentChar();
						pp.nextChar();
					}
				}
				command._value = s;
				if (pp.isChar(':')) {
					command._type = ':';
				}
			} else {
				for (;;) {
					if (pp.isChar(':')) {
						command._value = s;
						command._type = ':';
						break;
					} else if (pp.eos()) {
						break;
					} else {
						s += pp.getCurrentChar();
						pp.nextChar();
					}
				}
			}
		}
		return command;
	}

////////////////////////////////////////////////////////////////////////////////

	private static String yamlToJson(final StringParser p) {
		StringBuilder sb = new StringBuilder();
		System.out.println(p.getSourceBuffer());
		for (;;) {
			YamlCommand yc = readCommand(p);
			sb.append(yc.toString());
			System.out.print(sb);
			if (yc._indent == -1) {
				break;
			}
		}
		return sb.toString();
	}

	private static void yamlAsJson(final String s) {
//		System.out.println(XonUtils.toJsonString(
//			new Yaml().load(new StringReader(s)), true));
		System.out.println(yamlToJson(new StringParser(s)));
	}

	/**
	 * @param args the command line arguments
	 * @throws java.lang.Exception
	 */
	public static void main(String[] args) throws Exception {
//		StringReader sourceYAML;
//		Object json, o;
//		StringWriter swr;
//		StringBuilder sb;
//		String xdef, jsonData, resultYaml;
//		XDPool xpool;
//		XDDocument xdoc;
//		ArrayReporter reporter;
/**
		readlines("-");
		readlines("- \"int();\" # xxx");
		readlines("#-\n- \"int();\"\n  - \"boolean();\"\n # ");
		readlines(
"'cities':\n" +
"- '2020-02-22'\n" +
"- Brussels:\n" +
"  - to: London\n" +
"    distance: 322\n" +
"  - to: Paris\n" +
"    distance: 265\n" +
"- London \"City\":\n" +
"  - to: Brussels\n" +
"    distance: 322\n" +
"  - to:\n" +
"     Paris\n" +
"    distance:\n" +
"     344\n" +
"     #");
if (true) return;
/**/
//		yamlAsJson("\n- A\n\n- B\n-\n");
//		yamlAsJson("- A\n\n- B\n-");
//		yamlAsJson("A: 1\nB:\n-\n");
//		yamlAsJson("A: 1\nB:\n-");
//		yamlAsJson("");
//		yamlAsJson("[]");
//		yamlAsJson(" # \"int();\"");
//		yamlAsJson("[\"int();\"]");

//		yamlAsJson("- # null");
//		yamlAsJson("-\n-\n");
//		yamlAsJson("-          \"int();\"");
		yamlAsJson("- \n       \"int();\"");
		yamlAsJson("- \"int();\"\n- \"boolean();\"\n");
		yamlAsJson("-\n-  \"int();\"\n-\n  \"boolean();\"\n");
		yamlAsJson("- - \"int();\"\n");
		yamlAsJson("- - \"int();\"\n  - \"boolean();\"");
		yamlAsJson("{}");
		yamlAsJson("{\"a\":\"int();\"}\n");
		yamlAsJson("a : \"int();\"\nb: \"boolean();\"\n");
		yamlAsJson("a:\n  \"int();\"\n");
		yamlAsJson("a:\nb:  \"int();\"\n");
		yamlAsJson("- []\n");
		yamlAsJson("- []\n-\n  - 2\n");
		yamlAsJson("- []\n-  - 2\n");
		yamlAsJson("- []\n-  a: 2\n");
		yamlAsJson(
"#  xxxxxxxx\n" +
"\n" +
"'cities':\n" +
"- '2020-02-22'\n" +
"- Brussels:\n" +
"  - to: London\n" +
"    distance: 322\n" +
"  - to: Paris\n" +
"    distance: 265\n" +
"- London \"City\":\n" +
"  - to: Brussels\n" +
"    distance: 322\n" +
"  - to:\n" +
"     Paris\n" +
"\n" +
"    distance:\n" +
"     344\n" +
"     # xxx\n" +
"    xxx:\n" +
"     xxx\n" +
"     #");
		yamlAsJson(
">\n" +
" a\n" +
" b\n" +
" cd \n");
		yamlAsJson(
"|\n" +
"  a|\n" +
"  b|\n" +
"  c||d");
		yamlAsJson("national:\n- Chicago Cubs\n- Atlanta Braves");
		yamlAsJson(
"hr:  65    # Home runs\n" +
"avg: 0.278 # Batting average\n" +
"rbi: 147   # Runs Batted In");
		yamlAsJson(
"hr:\n" +
"- Mark McGwire\n" +
"# Following node labeled SS\n" +
"- &SS Sammy Sosa\n" +
"- &A x\n" +
"rbi:\n" +
"- *SS # Subsequent occurrence\n" +
"- *A # Subsequent occurrence\n" +
"- Ken Griffey");
//		yamlAsJson(
//"? - Detroit Tigers\n" +
//"  - Chicago cubs\n" +
//": - 2001-07-23\n" +
//"\n" +
//"? [ New York Yankees,\n" +
//"    Atlanta Braves ]\n" +
//": [ 2001-07-02, 2001-08-12,\n" +
//"    2001-08-14 ]");
		yamlAsJson(
"- item    : Super Hoop\n" +
"  quantity: 1\n" +
"- item    : Basketball\n" +
"  quantity: 4\n" +
"- item    : Big Shoes\n" +
"  quantity: 1");
		yamlAsJson(
"# ASCII Art\n" +
"--- |\n" +
"  \\//||\\/||\n" +
"  // ||  ||__");
		yamlAsJson(
"--- >\n" +
"  Mark McGwire's\n" +
"  year was crippled\n" +
"  by a knee injury.");
		yamlAsJson(
"string: '2002-04-28'\n");
		yamlAsJson(
"string: !!str 2002-04-28\n");
//"\n" +
//"picture: !!binary |\n" +
//" R0lGODlhDAAMAIQAAP//9/X\n" +
//" 17unp5WZmZgAAAOfn515eXv\n" +
//" Pz7Y6OjuDg4J+fn5OTk6enp\n" +
//" 56enmleECcgggoBADs=\n" +
//"\n" +
//"application specific tag: !something |\n" +
//" The semantics of the tag\n" +
//" above may be different for\n" +
//" different documents.");
//if(true)return;
/**/
		System.out.println(toXonModel(
"- '%oneOf'\n" +
"-\n" +
"  - \"int()\"\n" +
"- {a: int()}"));
		System.out.println(toXonModel(
"- '%oneOf=*;'\n" +
"-\n" +
"  - \"int()\"\n" +
"- {a: int()}"));
		System.out.println(toXonModel(
"- '%oneOf=   *;'\n" +
"- - \"int()\"\n" +
"- {\"a\": \"int()\"}"));
		System.out.println(toXonModel(
"- '%oneOf  =*;'\n" +
"- - \"int()\"\n" +
"- {\"a\": \"int()\"}"));
		System.out.println(toXonModel(
"- '%oneOf  =   *;'\n" +
"- - \"int()\"\n" +
"- {\"a\": \"int()\"}"));
		System.out.println(toXonModel(
"cities:\n" +
"- \"date(); finally printf('Measurements taken on: %s%n', getText());\"\n" +
"- \"%script\": '+;'\n" +
"  '%anyName':\n" +
"     - \"%script= +; init printf('Distance from %s to%n', getXonKey());\"\n"+
"     -  '%script':  \"+; finally outln();\"\n" +
"        to: \"jstring();finally printf('  %s:', getText());\"\n"+
"        distance: 'int(); finally printf(''%s (km)'', getText());'\n" +
	""));
	}
}
