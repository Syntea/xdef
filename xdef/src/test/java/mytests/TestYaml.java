package mytests;

import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Test YAML.
 * @author Vaclav Trojan
 */
public class TestYaml extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String xdef;
		String s;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Object x;

////////////////////////////////////////////////////////////////////////////////
		try {
//https://docs.ansible.com/ansible/latest/reference_appendices/YAMLSyntax.html

//["Apple", "Orange", "Strawberry", "Mango"]
System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"---\n" +
"# A list of tasty fruits\n" +
"- Apple\n" +
"- Orange\n" +
"- Strawberry\n" +
"- Mango\n" +
"..."),true));
//{ martin : {name : "Martin D'vloper", job : "Developer", skill : "Elite"}}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"# An employee record\n" +
"martin:\n" +
"  name: Martin D'vloper\n" +
"  job: Developer\n" +
"  skill: Elite"),true));
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"create_key: yes\n" + // true
"needs_agent: no\n" + // false
"knows_oop: True\n" + // true
"likes_emacs: TRUE\n" + // true
"uses_cvs: false"),true));
//{include_newlines : "exactly as you see\nappear these three\nlines of poetry"}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"include_newlines: |\n" +
"            exactly as you see\n" +
"            appear these three\n" +
"            lines of poetry"),true));
//{ fold_newlines : "this is really a single line of text despite appearances"}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"fold_newlines: >\n" +
"            this is really a\n" +
"            single line of text\n" +
"            despite appearances"),true));
			//there are two ways to enforce a newline to be kept:
//{fold_some_newlines : "a b\nc d\n  e\nf\n"}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"fold_some_newlines: >\n" +
"    a\n" +
"    b\n" +
"\n" +
"    c\n" +
"    d\n" +
"      e\n" +
"    f\n"),true));
			//Alternatively, it can be enforced by including newline \n characters:
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"fold_same_newlines: \"a b\\nc d\\n  e\\nf\\n\""),true));
//{ name : "Martin D'vloper",
//  job : "Developer",
//  skill : "Elite",
//  employed : true,
//  foods : ["Apple", "Orange", "Strawberry", "Mango"],
//  languages : {perl : "Elite", python : "Elite", pascal : "Lame"},
//  education : "4 GCSEs\n3 A-Levels\nBSc in the Internet of Things"
//}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"---\n" +
"# An employee record\n" +
"name: Martin D'vloper\n" +
"job: Developer\n" +
"skill: Elite\n" +
"employed: True\n" +
"foods:\n" +
"  - Apple\n" +
"  - Orange\n" +
"  - Strawberry\n" +
"  - Mango\n" +
"languages:\n" +
"  perl: Elite\n" +
"  python: Elite\n" +
"  pascal: Lame\n" +
"education: |\n" +
"  4 GCSEs\n" +
"  3 A-Levels\n" +
"  BSc in the Internet of Things\n"+
"..."),true));
//[
//  { martin : { name : "Martin D'vloper",
//      job : "Developer",
//      skills : ["python", "perl", "pascal"]
//    }
//  },
//  { tabitha : { name : "Tabitha Bitumen",
//      job : "Developer",
//      skills : ["lisp", "fortran", "erlang"]
//    }
//  }
//]
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML(
"# Employee records\n" +
"- martin:\n" +
"    name: Martin D'vloper\n" +
"    job: Developer\n" +
"    skills:\n" +
"      - python\n" +
"      - perl\n" +
"      - pascal\n" +
"- tabitha:\n" +
"    name: Tabitha Bitumen\n" +
"    job: Developer\n" +
"    skills:\n" +
"      - lisp\n" +
"      - fortran\n" +
"      - erlang\n"),true));
//{ martin : {name : "Martin D'vloper", job : "Developer", skill : "Elite"},
//  fruits : ["Apple", "Orange", "Strawberry", "Mango"]
//}
			System.out.println(XonUtils.toXonString(XonUtils.parseYAML("---\n" +
"martin: {name: Martin D'vloper, job: Developer, skill: Elite}\n" +
"fruits: ['Apple', 'Orange', 'Strawberry', 'Mango']"),true));
// this is error:
//foo: somebody said I should put a colon here: so I did
//windows_drive: c:
//A colon followed by a space (or newline) ": " is an indicator for a mapping.
// This is OK:
//windows_path: c:\windows
// or text with colon iis in quotes or apostrophs
//foo: 'somebody said I should put a colon here: so I did'
//windows_drive: "c:"
//The difference between single quotes and double quotes is that in double quotes you can use escapes:
//foo: "a \t TAB and a \n NEWLINE"
//special charaters: [] {} > | * & ! % # ` @ ,.
		} catch (RuntimeException ex) {fail(ex);}
		try {
			System.out.println("Xdefinition version: " + XDFactory.getXDVersion());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:json name=\"test\">\n" +
"    { \"date\" : \"date(); finally outln('Measured on: ' + getText() + '\\n');\",\n" +
"      \"cities\"  : [\n" +
"        {%script = \"occurs 1..*; finally outln();\",\n" +
"          \"from\": [\n" +
"            \"string(); finally outln('From ' + getText());\",\n" +
"            {%script = \"occurs 1..*; finally outln();\",\n" +
"              \"to\": \"jstring(); finally out(' to ' + getText() + ' is distance: ');\",\n" +
"              \"distance\": \"int(); finally out(getText() + ' (km)');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			s =
"date: '2020-02-22'\n" +
"cities:\n" +
"- from:\n" +
"  - Brussels\n" +
"  - {to: London, distance: 322}\n" +
"  - {to: Paris, distance: 265}\n" +
"- from:\n" +
"  - London\n" +
"  - {to: Brussels, distance: 322}\n" +
"  - {to: Paris, distance: 344}\n";
			x = xd.yparse(s, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(x, xd.yparse(XonUtils.toYamlString(x), reporter)));
			assertNoErrors(reporter);
			reporter.clear();
		} catch (RuntimeException ex) {fail(ex);}
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:ini xd:name = \"test\">\n" +
"    name = string();\n" +
"    date = date();\n" +
"    email = ? emailAddr();\n" +
"    [Server]\n" +
"    IPAddr = ? ipAddr();\n" +
"  </xd:ini>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			String ini =
"date = 2021-02-03\n"+
"name = Jan Novak\n"+
"email = jan.novak@novak.org\n" +
"[Server]\n" +
"  IPAddr = 123.45.6.7";
			Map<String, Object> xini = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(
				XonUtils.xonEqual(XonUtils.parseINI(ini), XonUtils.parseINI(XonUtils.toIniString(xini))));
		} catch (RuntimeException ex) {fail(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}