package mytest.xdef;

import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Stack;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.impl.XDefinition;
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.parsers.XSAbstractParser;
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.json.JNull;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.model.XMVariable;
import org.xdef.model.XMVariableTable;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;
import org.xdef.util.XdefToXsd;

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {
	/** The package of X-components. */
	private static final String COMPONENT_PACKAGE = "mytest.component";

	private final Stack<String> _stack = new Stack<String>();
	private String _item;

	public MyTest_0() {super();}

////////////////////////////////////////////////////////////////////////////////
// User methods used in X-definitions tests
////////////////////////////////////////////////////////////////////////////////
	public static boolean next(XXElement x) {
		MyTest_0 y = (MyTest_0) x.getUserObject();
		return !y._stack.empty() && (y._item = y._stack.pop()) != null;
	}
	public static boolean methodA(XXElement x) {
		return "A".equals(((MyTest_0) x.getUserObject())._item);
	}
	public static boolean methodB(XXElement x) {
		return "B".equals(((MyTest_0) x.getUserObject())._item);
	}
	public static void exc() {throw new RuntimeException("bla...");}
	public static boolean cPar(XDContainer c) {
		return "A".equals(c.getXDNamedItemAsString("a"))
			&& "B".equals(c.getXDNamedItemAsString("b"));
	}
	public static XDParseResult kp(XXNode chkel, XDValue[] params) {
		XDContainer c = XDFactory.createXDContainer((XDContainer) params[2]);
		c.setXDNamedItem("minInclusive", params[0]);
		c.setXDNamedItem("maxInclusive", params[1]);
		try {
			XSAbstractParser d = new XSParseDecimal();
			d.setNamedParams(null, c);
			return d.check(null, chkel.getXMLNode().getNodeValue());
		} catch (Exception ex) {
			XDParseResult x = XDFactory.createParseResult("");
			x.error("", ex.toString());
			return x;
		}
	}

	private static void displayData(XMData x) {
		System.out.println(x.getName());
		System.out.println(x.getValueTypeName());
		String s = x.getRefTypeName();
		if (s != null) {
			XMVariableTable xv = x.getXDPool().getVariableTable();
			XMVariable v = xv.getVariable(s);
			if (v == null) {
				String xdName = x.getXMDefinition().getName();
				v = xv.getVariable(xdName + "#" + s);
				s = (v != null ? xdName + "#" : "UNDEFINED: ") + s;
			}
		}
		System.out.println(s);
		XDValue v = x.getParseMethod();
		if (v instanceof XDParser) {
			XDParser p = ((XDParser) v);
			System.out.println("\t" + p.parserName());
			XDContainer c = p.getNamedParams();
			for (XDNamedValue n : c.getXDNamedItems()) {
				String name = n.getName();
				System.out.print("\t\t" + name + "=");
				XDValue val = n.getValue();
				if (val.getItemId() == XDValue.XD_CONTAINER) {
					boolean first = true;
					for (XDValue y : ((XDContainer) val).getXDItems()) {
						if (first) {
							first = false;
						} else {
							System.out.print(",");
						}
						System.out.print(y.toString());
					}
					System.out.println();
				} else {
					System.out.println("\t" + n.toString());
				}
			}
		} else {
			// null -> string()!
			System.out.println("\t" + (v==null?"string":v.toString()));
		}
	}

	private static void printXMData(final XMData x) {
		System.out.println(x.getXDPosition()
			+ ", Parse: " + x.getParseMethod()
			+ ", Type: " + x.getValueTypeName()
			+ ", Ref: " + x.getRefTypeName()
			+ ", Default: " + x.getDefaultValue()
			+ ", Fixed: " + x.getFixedValue()
			+ ", occ: " + x.getOccurence());
	}

	private static void printXMData(final XMElement xe) {
		for (XMData x: xe.getAttrs()) {
			printXMData(x);
		}
		for (XMNode x: xe.getChildNodeModels()) {
			if (x.getKind() == XMNode.XMTEXT) {
				printXMData((XMData) x);
			} else if (x.getKind() == XMNode.XMELEMENT) {
				printXMData((XMElement) x);
			}
		}
	}

	/** Create X-components from XDPool to the directory from argument "srcDir"
	 * and compile it. Clear tempDir before generation.
	 * @param xp XDPool from which the X-components created.
	 * @param tempDir the directory where to generate Java sources.
	 * @return reporter with reported errors and warnings.
	 * @throws RuntimeException if an error occurs.
	 */
	public static final ArrayReporter genXComponent(final XDPool xp,
		final String tempDir) {
		File fdir = new File(tempDir);
		if (fdir.exists()) { // ensure the src directory exists.
			if (fdir.isFile()) {
				throw new RuntimeException(tempDir + " is not directory");
			}
			try {
				SUtils.deleteAll(tempDir, true); // delete this directory
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		fdir.mkdirs();
		return genXComponent(xp, fdir);
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
//		T = true; // if true, only first test is invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String tempDir = getTempDir();
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!tempDir.endsWith("/")) {
			tempDir += '/';
		}
		if (!f.isDirectory()) {
			fail('\"' + tempDir + "\" is not directory");
			return;
		}
		XDPool xp;
		String xdef;
		String xml;
		String s;
		String json;
		Object j;
		Object o;
		XDDocument xd;
		Element el;
		XDOutput xout;
		StringWriter strw;
		Report rep;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////		
//		Throwable throwable = new Throwable("");
//		System.out.println(throwable.getClass().getName() +
//			(throwable.getMessage()!=null ? ": "+throwable.getMessage() : ""));
//		for (StackTraceElement x: throwable.getStackTrace()) {
//			System.out.println("\tat "+ x.getClassName()
//				+ '.' + x.getMethodName()
//				+ '(' + x.getFileName() + ':' + x.getLineNumber() + ")");
//		}
//		System.out.println("===");
//		throwable.printStackTrace(System.out);
//if(true)return;
////////////////////////////////////////////////////////////////////////////////		
//		try {
//			xdef =
//"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.0'>\n"+
//"<xd:def name='A'>\n"+
//"  <xd:declaration> Parser p; uniqueSet u {x: p} </xd:declaration>\n"+
//"</xd:def>\n"+
//"<xd:def name='B' root='A'>\n"+
//"  <A id=\"? xdType(); onTrue p = getParsedValue();\" />\n"+
//"</xd:def>\n"+
//"</xd:collection>";
//			xp = XDFactory.compileXD(null, xdef);
//		} catch (Exception ex) {fail(ex);}
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"{\n" +
"  \"first name\": \"string; /**finally outln(getXDPosition());/**/\",\n" +
"  \"last name\" : \"string; /**finally outln(getXDPosition());/**/\",\n" +
"  \"age\"      : \"int(1, 100); /**finally outln(getXDPosition());/**/\",\n" +
"  \"address\"  : {\n" +
"    \"street address\": \"string; /**finally outln(getXDPosition());/**/\",\n"+
"    \"city\"         : \"string; /**finally outln(getXDPosition());/**/\",\n" +
"    \"postal code\"   : \"string; /**finally outln(getXDPosition());/**/\"\n" +
"  },\n" +
"  \"phone numbers\": [\n" +
"    {\n" +
"      $script: \"occurs +\",\n" +
"      \"type\"  : \"an; /**finally outln(getXDPosition());/**/\",\n" +
"      \"number\": \"num; /**finally outln(getXDPosition());/**/\"\n" +
"    }\n" +
"  ]\n" +
"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.A0 %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			json =
"{\n" +
"  \"first name\": \"John\",\n" +
"  \"last name\" : \"doe\",\n" +
"  \"age\"       : 101,\n" +
"  \"address\"   : {\n" +
"    \"street address\": \"naist street\",\n" +
"    \"city\"          : \"Nara\",\n" +
"    \"postal code\"   : \"630-0192\"\n" +
"  },\n" +
"  \"phone numbers\": [\n" +
"    {\n" +
"      \"type\"  : \"iPhone\",\n" +
"      \"number\": \"0123-4567-8888\"\n" +
"    },\n" +
"    {\n" +
"      \"type\"  : \"home\",\n" +
"      \"number\": \"0123-4567-8910\"\n" +
"    }\n" +
"  ]\n" +
"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
		} catch (Exception ex) {fail(ex);}
if(true)return;
		try {//test binding of XPath variables with XDefinition variables
/*xx*
			xdef = //integer variable x without leading "$"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>int x=123;</xd:declaration>\n"+
"  <a a='string()' xd:script=\"finally outln(xpath('/a/@a=$x'));\"/>"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xml = "<a a='123'/>";
			assertEq(xml, el = parse(xp, "", xml, reporter));
			assertNoErrors(reporter);	
/*xx*/
 //XML505 XPath error:
			xdef = //variable x with leading "$"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int x = 123; </xd:declaration>\n"+
"  <a xd:script = \"create from('b[@a=$x]')\">string</a>\n"+
//"  <a xd:script = \"create xpath('b[@a=$x]', getCreateContextElement())\">string</a>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
//			xp.displayCode();
			xml = "<w><b a='x'/><b a='123'>zxy</b><b>xx</b></w>";
			assertEq("<a>zxy</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
/*xx*/
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"  <xd:declaration scope='global'> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2..3; create true'>\n"+  //creates max
"      <b xd:script=\"create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create $typ EQ 2;\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a><b/><c/><c/></a>", create(xdef,"","a",reporter,null));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='Y'>\n"+
"<xd:json name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" + 
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TY_X %link #Y;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			Class<?> clazz = MyTest_0.class;
			String className = clazz.getName().replace('.', '/') + ".class";
			URL u = clazz.getClassLoader().getResource(className);
			String classDir =
				new File(u.getFile()).getAbsolutePath().replace('\\', '/');
			classDir = classDir.substring(0, classDir.indexOf(className));
			System.out.println(classDir + "mytest/xdef/xp.xp");
			XDFactory.writeXDPool(classDir + "mytest/xdef/xp.xp", xp);
			xp = XDFactory.readXDPool("classpath://mytest.xdef.xp.xp");			
			genXComponent(xp, tempDir);
			json = "[{\"a\":true},\"xxx\",125, true]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("xxx", getValueFromGetter(xc, "jgetitem"));
			assertEq(125, getValueFromGetter(xc, "jgetitem_1"));
			assertEq(true, getValueFromGetter(xc, "jgetitem_2"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n" +
"<xd:declaration>\n" +
"  type genre enum(\"Classic\", \"Country\", \"Folk\", \"Jazz\", \"Pop\", \"Pop_punk\",\n" +
"             \"Punk\", \"Punk_rock\", \"Rap\", \"Rock\", \"R&amp;B\", \"Other\");\n" +
"  boolean yy = genre().parse('Country').matches();\n"+
"  Parser p = enum(\"Classic\", \"Country\", \"Folk\", \"Jazz\", \"Pop\", \"Pop_punk\",\n" +
"             \"Punk\", \"Punk_rock\", \"Rap\", \"Rock\", \"R&amp;B\", \"Other\");\n" +
"  boolean zz = p.parse('Country').matches();\n"+
"</xd:declaration>\n" +
"<xd:json name='a'>\n" +
"{\n" +
"  \"Genre\": [$oneOf,\n" +
"     \"genre\",\n" +
"     [\"occurs 1..*; genre\"]\n" +
"  ]\n" +
"}\n" +
"</xd:json>"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"Genre\": \"Classic\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
			json = "{\"Genre\": [\"Classic\"]}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
			json = "{\"Genre\": [\"Punk\", \"Other\"]}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XAA %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq("aaa", getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", 123);
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", null);
			assertNull(getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", " a b \t");
			assertEq(" a b \t", getValueFromGetter(xc, "jgeta"));

			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(getValueFromGetter(
				xc, "getjs$item"), "getvalue"));
			
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", 123);
			assertEq(123, getValueFromGetter(xc, "jgeta"));

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgeta"));
			
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", 123);
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", null);
			assertNull(getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", " a b \t");
			assertEq(" a b \t", getValueFromGetter(xc, "jgeta"));

			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", JNull.JNULL);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgeta"));

			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgeta"));
			
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "jgeta"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A | B'>\n"+
"<xd:json name='A'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"[\"? jvalue()\"]\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XA %link #A;\n"+
"  %class mytest.component.XB %link #B;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq("aaa", getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", 123);
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", null);
			assertNull(getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", "xyz");
			assertEq("xyz", getValueFromGetter(xc, "jgeta"));
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(false, getValueFromGetter(xc, "jgeta"));
			setValueToSetter(xc, "jseta", 123);
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			
			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgeta"));
			
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "jgeta"));
			
			json = "[null]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("mytest.component.XB"), reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgetitem"));
			json = "[123]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("mytest.component.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(123, getValueFromGetter(xc, "jgetitem"));
			json = "[true]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("mytest.component.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(true, getValueFromGetter(xc, "jgetitem"));
			json = "[]";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				Class.forName("mytest.component.XB"), reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNull(getValueFromGetter(xc, "jgetitem"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"a\": \"? jvalue()\"}\n" +
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.XD %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "{\"a\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq("aaa", getValueFromGetter(xc, "jgeta"));
			json = "{\"a\":123}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertEq(123, getValueFromGetter(xc, "jgeta"));
			json = "{\"a\":false}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertTrue(!(Boolean) getValueFromGetter(xc, "jgeta"));
			json = "{\"a\":null}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgeta"));
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			reporter.checkAndThrowErrors();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			reporter.checkAndThrowErrors();
			assertNull(getValueFromGetter(xc, "jgeta"));

			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"[\n"+
"    \"? jnull\",\n"+
"    \"? int()\",\n"+
"    \"? string()\"\n"+
"]\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class mytest.component.TJ1 %link #json;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			json = "[null, 12, \" a b \"]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgetitem"));
			assertEq(12, getValueFromGetter(xc, "jgetitem_1"));
			assertEq(" a b ", getValueFromGetter(xc, "jgetitem_2"));
			json = "[null]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertEq(JNull.JNULL, getValueFromGetter(xc, "jgetitem"));
			assertNull(getValueFromGetter(xc, "jgetitem_1"));
			assertNull(getValueFromGetter(xc, "jgetitem_2"));
			json = "[12]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertNull(getValueFromGetter(xc, "jgetitem"));
			assertEq(12, getValueFromGetter(xc, "jgetitem_1"));
			assertNull(getValueFromGetter(xc, "jgetitem_2"));
			json = "[\"\"]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			assertNull(getValueFromGetter(xc, "jgetitem"));
			assertNull(getValueFromGetter(xc, "jgetitem_1"));
			assertEq("", getValueFromGetter(xc, "jgetitem_2"));
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.0'>\n"+
"<xd:def name='X' root='X'>\n"+
"<xd:json xd:name='X'>\n"+
"[\"int()\"]\n"+
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='Y'>\n"+
"<xd:json name='Y'>\n"+
"[{\"a\":\"jboolean\"},\"jstring()\",\"jnumber()\",\"? jboolean()\"]\n" + 
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:def name='Z' root='Z | json'>\n"+
"<xd:json name='Z'>\n"+
"{\"a\":\"string()\"}\n" + 
"</xd:json>\n"+
"<xd:json xd:name='json'>\n"+
"[\"date()\"]\n"+
"</xd:json>\n"+
"</xd:def>\n"+
"<xd:component>\n"+
"  %class mytest.component.TX %link X#X;\n"+
"  %class mytest.component.TY %link Y#Y;\n"+
"  %class mytest.component.TZ %link Z#Z;\n"+
"  %class mytest.component.TJson %link Z#json;\n"+
"</xd:component>\n"+
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			Class<?> TX = Class.forName("mytest.component.TX");
			Class<?> TY = Class.forName("mytest.component.TY");
			Class<?> TZ = Class.forName("mytest.component.TZ");
			Class<?> TJson = Class.forName("mytest.component.TJson");
			json = "[\"2020-01-01\"]";
			j = xp.createXDDocument("Z").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument("Z").jparseXComponent(json,TJson,reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				xc.toJson());
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(xc.toXml()));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(JsonUtil.jsonToXml(xc.toJson())));
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.xmlToJson(JsonUtil.jsonToXmlXdef(xc.toJson())));
			json = "[123]";
			j = xp.createXDDocument("X").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			assertNoErrors(reporter);
			reporter.clear();
			xc = xp.createXDDocument("X").jparseXComponent(json, TX, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq(123, getValueFromGetter(xc, "jgetitem"));
			json = "[{\"a\":true},\"xxx\",125, true]";
			j = xp.createXDDocument("Y").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument("Y").jparseXComponent(json, TY, reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), xc.toJson()),
				JsonUtil.toJsonString(xc.toJson(), true));
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("xxx", getValueFromGetter(xc, "jgetitem"));
			assertEq(125, getValueFromGetter(xc, "jgetitem_1"));
			assertEq(true, getValueFromGetter(xc, "jgetitem_2"));
			json = "{\"a\":\"2020-01-01\"}";
			j = xp.createXDDocument("Z").jparse(json, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument("Z").jparseXComponent(json, TZ, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertEq("2020-01-01", getValueFromGetter(xc, "jgeta"));
			
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A|B|json'>\n"+
"<xd:json name='json'>\n"+
"[\n"+
"  {\"a\":\"boolean\"},\n"+
"  \"string()\",\n"+
"  \"int()\"\n"+
"]\n" + 
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"{\"a\":\"int\"}\n"+
"</xd:json>\n"+
"  <A/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			json = "[{\"a\":true},\"x\",-1]";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='A'>\n"+
"<xd:declaration> uniqueSet u{x: int();}; </xd:declaration>\n"+
"<A>\n"+
"  <a xd:script='?' a='u.x()' b='? u.x'/>\n"+
"  <b xd:script='+' a='u.x.ID' b='? u.x.ID()'/>\n"+
"  <c xd:script='?' a='u.x.IDREF' b='? u.x.IDREF()'/>\n"+
"  <d xd:script='?' a='u.x.CHKID' b='? u.x.CHKID()'/>\n"+
"  <e xd:script='?' a='u.x.SET' b='? u.x.SET()'/>\n"+
"</A>\n"+
"</xd:def>\n";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(xdef);
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A><a a='2'/><b a='1'/><c a='1'/><d a='1'/><e a='3'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n"+
"    uniqueSet r {a: int();};\n"+
"     type s string(1,*);\n" +
"     type T4_str enum(%argument=['a','b']);\n"+
"	  type T4_int int(1, 10);\n"+				
"  </xd:declaration>\n"+
"  <A a = ''>\n"+
"  <X xd:script='var uniqueSet r {a: string();};'>\n"+
"    ? union(%item=[T4_str, T4_int]);\n"+
"    <B xd:script='*' a='? r.a.ID' b='? r.a.ID()' c='? r.a' d='? r.a()'/>\n"+
"    ? list(%item=T4_int);\n"+
"    <C xd:script='*;' a='? r.a.CHKIDS' b='? r.a.CHKIDS()'/>\n"+
"    <D xd:script='*;' a='? T4_str' b='? T4_str()'  c='? s' d='? s()'/>\n"+
"    ? r.a.CHKIDS\n"+
"  </X>\n"+
"  </A>\n"+
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(xdef);
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A a='x'><X><B a='x'/><C a='x'/></X></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='? t()'/>\n"+
"  <xd:declaration>\n"+
"     type t enum(%argument=['a','b']);\n"+
//"     boolean t() {return enum(%argument=['a','b']).parse().matches();};\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			XMDefinition xmdef = xp.getXMDefinition("");
			XMElement xmel = xmdef.getModel(null, "a");
			XMData xmd = xmel.getAttr("a");
System.out.println("ParserType: "+CompileBase.getTypeName(xmd.getParserType()));
System.out.println("ValueTypeName: " + xmd.getValueTypeName());
System.out.println("RefTypeName: " + xmd.getRefTypeName());
System.out.println("ParserName: " + xmd.getParserName());
			assertEq(xmd.getParserType(),  XDValue.XD_STRING);
			assertEq(xmd.getRefTypeName(), "t");
			xml = "<a a='b' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/><b/>2</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/>2/>3</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/>2<b/>3<b/>4</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef = 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef = 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/></a>";
			assertEq("<a>1<b/>2</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/><b/></a>";
			assertEq("<a>1<b/>2<b/>3</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root = 'a'>\n"+
"  <a xd:textcontent=\"string();\n" +
"       onTrue out('T:'+getText()); finally out('f:'+getText());\">\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>t1<b/>t2<c/>t3</a>";
			StringWriter bos = new StringWriter();
			xout = XDFactory.createXDOutput(bos, false);
			xd = xp.createXDDocument();
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			xout.close();
			assertNoErrors(reporter);
			assertEq("T:t1t2t3f:t1t2t3", bos.toString());
			assertEq("<a>t1<b/>t2<c/>t3</a>",
				KXmlUtils.nodeToString(xd.getElement()));

			bos = new StringWriter();
			xout = XDFactory.createXDOutput(bos, false);
			xd = xp.createXDDocument();
			xd.setStdOut(xout);
			xd.setXDContext(KXmlUtils.parseXml(xml).getDocumentElement());
			el = xd.xcreate("a", null);
			xout.close();
			assertEq("<a><b/><c/>t1t2t3</a>", KXmlUtils.nodeToString(el));
			assertEq("T:t1t2t3f:t1t2t3", bos.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n"+
"    uniqueSet r {a: int();};\n"+
"     type s string(1,*);\n" +
"     type T4_str enum(%argument=['a','b']);\n"+
"	  type T4_int int(1, 10);\n"+				
"  </xd:declaration>\n"+
"  <A a = ''>\n"+
//"  <X>\n"+
"  <X xd:script='var uniqueSet r {a: string();};'>\n"+
"    ? union(%item=[T4_str, T4_int]);\n"+
"    <B xd:script='*' a='? r.a.ID' b='? r.a.ID()' c='? r.a' d='? r.a()'/>\n"+
"    ? list(%item=T4_int);\n"+
"    <C xd:script='*;' a='? r.a.CHKIDS' b='? r.a.CHKIDS()'/>\n"+
"    <D xd:script='*;' a='? T4_str' b='? T4_str()'  c='? s' d='? s()'/>\n"+
"    ? r.a.CHKIDS\n"+
"  </X>\n"+
"  </A>\n"+
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(xdef);
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A a='x'><X><B a='x'/><C a='x'/></X></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='T' >\n" +
"  <xd:declaration>\n" +
"    uniqueSet r {a: string(1,2); b: string(1,2)};\n" +
"  </xd:declaration>\n" +
"  <T>\n" +
"    <R xd:script='*; finally r.ID()' A='r.a' B='r.b'/>\n" +
"  </T>\n" +
"<xd:component>\n"+
"  %class mytest.component.T %link #T;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();
			xml =
"<T>\n" +
"  <R A='xx' B='aaa'/>\n" +
"  <R A='xxx' B='aa'/>\n" +
"  <R A='xxx' B='aaa'/>\n" +
"  <R A='xx' B='aa'/>\n" +
"</T>";
			assertEq(xml, parse(xd, xml, reporter));
			s = reporter.printToString();
			assertTrue(s.contains(" \"a\")") && s.contains(" \"b\")")
				&& s.contains(" \"a\", \"b\"")&&reporter.getErrorCount()==7,s);
//			Class.forName("mytest.component.T");
			xml =
"<T>\n" +
"  <R A='xx' B='aa'/>\n" +
"  <R A='yy' B='aa'/>\n" +
"  <R A='xx' B='yy'/>\n" +
"  <R A='zz' B='zz'/>\n" +
"</T>";
			el = parseXC(xp, "", xml, null, reporter).toXml();
			assertNoErrors(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='T'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet r {a: string(); b: int()};\n" +
"  </xd:declaration>\n" +
"  <T>\n" +
"    <R xd:script='*' A='r.a' B='r.b.ID()'/>\n" +
"  </T>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<T>\n" +
"  <R A='xx' B='a'/>\n" +
"  <R A='yy' B='a'/>\n" +
"</T>";
			parse(xd, xml, reporter);
			System.out.println(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='a'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet u {var Parser x; var int y; a: string(); var String z}\n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <DefParams>\n" +
"       <Param xd:script='*;'\n" +
"          Name='u.a.ID();'\n" +
"          Type='xdType(); onTrue u.x=getParsedValue();\n" +
"                          onFalse u.y=99;\n" +
"                          finally out(u.y)'/>\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script='*;'\n" +
"              Name='u.a.CHKID();'\n" +
"              Value='u.x; onTrue out(u.x); '/>\n" +
"    </Params>\n" +
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"xdatetime('dd.MM.yyyy')\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"14.8a\" Name=\"Vyska\"/>\n" +
"  </Params>\n" +
"</a>";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("stringdecxdatetime", strw.toString());
			assertTrue((rep = reporter.getReport()) != null
				&& "XDEF804".equals(rep.getMsgID()), rep);
			assertTrue((rep = reporter.getReport()) != null
				&& "XDEF524".equals(rep.getMsgID()), rep);
			assertNull(rep = reporter.getReport(), rep);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='A'>\n"+
"  <xd:declaration scope='global'>\n" +
"    type name string(1, 128);\n" +
"  </xd:declaration>\n" +
"  <A>name()</A>" + 
"</xd:def>\n";
			f = new File(tempDir + "x.xdef");
			SUtils.writeString(f, xdef);
			xp = compile(f);
			xml = "<A>1?xyz</A>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrors(reporter);
			XdefToXsd.main(
				"-i", f.getAbsolutePath(), "-o", tempDir, "-m", "A");
			File[] ff = SUtils.getFileGroup(tempDir + "*xsd", false);
			for (File x: ff) {
				System.out.println(SUtils.readString(x));
			}
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			s = "D:/cvs/DEV/java/xdef31/resources/"
				+ "cz/syntea/xdef/impl/compile/XdefOfXdef*.xdef";
			// filepath
			xp = XDFactory.compileXD(null, s);//with wildcards
			xp = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='" + s + "'/>");
			xp = XDFactory.compileXD(null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='" + s + "'/>");
			// URL (file:/filepath)
			xp = XDFactory.compileXD(null, "file:/" + s);
			xp = XDFactory.compileXD((Properties) null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='file:/" + s + "'/>");
			xp = XDFactory.compileXD((Properties) null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='file:/" + s + "'/>");
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			reporter.clear();
			xp = XDFactory.compileXD(reporter, (Properties) null, 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='global'>\n"+
"    int i;\n"+
"    int x(){return 0;}\n"+
"    type t int();\n"+
"  </xd:declaration>\n"+
"  <a/>\n"+
"</xd:def>",
"<xd:declaration xmlns:xd='"+_xdNS+"'>\n"+
"  int i; int x(){return 0;} type t int();\n"+
"</xd:declaration>");
			if (reporter.errorWarnings()) {
				s = reporter.printToString();
				assertTrue(s.contains("XDEF450"), s);
				assertTrue(s.contains("XDEF462"), s);
				assertTrue(s.contains("XDEF470"), s);
			} else {
				fail("Error not reported");
			}
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"A\">\n" +
"<xd:declaration scope = 'local'>\n" +
"type t string(1,10,%pattern=[\"[a-z]+\", \"d+\"], %whiteSpace=\"replace\");\n"+				
"</xd:declaration>\n" +
"  <A a='?'\n" +
"     b='? t'\n" +
"     c='? decimal(1,2,%fractionDigits=5, %totalDigits=11)'\n" +
"     d='? boolean()'\n" +
"     e='? date(%enumeration=[\"2010-01-01\",\"2010-01-31\"])' >\n" +
"   ? t;\n"+
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			XDefinition xmd = (XDefinition) xp.getXMDefinitions()[0];
			XMElement xme = xmd.getModel(null, "A");
			for (XMNode x : xme.getChildNodeModels()) {
				if (x instanceof XMData) {
					displayData((XMData) x);
				}
			}
			xml = "<A a='a' b='bb' c='1' d='true'></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xd:name=\"Test\" xd:root=\"json\">\n" +
"  <xd:json name='json'>{\"A\":\"int();\"}</xd:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef, 
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#json\"/>"
			);
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"abc\"}";
			xd.jparse(s, reporter);
			assertErrors(reporter);
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.syntea.cz/xdef/3.1\" xd:root=\"A\">\n" +
"    <A xd:script=\"create from('/*/*[last()]');\"\n" +
"       a=\"required string()\" >\n" +
"        <B xd:script=\"occurs 1..\"\n" +
"           b=\"required string()\">\n" +
"            <xd:choice script='occurs +'>\n" +
"               <X xd:script=\"ref X\" />\n" +
"               <Y xd:script=\"ref Y\" />\n" +
"               <Z xd:script=\"ref Z\" />\n" +
"            </xd:choice>\n" +
"        </B>\n" +
"    </A>\n" +
"    <X x=\"required string()\"/>\n" +
"    <Y x=\"required string()\"/>\n" +
"    <Z x=\"required string()\"/>\n" +
"\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = 
"<A_ IdFlow=\"181131058\">\n" +
"    <XXX IdDefPartner=\"163\"/>\n" +
"    <YYY DruhSouboru=\"W1A\"/>\n" +
"    <A a=\"Pojistitel_SLP\">\n" +
"        <B b=\"b\">\n" +
"            <Z x=\"3\" />\n" +
"            <Z x=\"4\" />\n" +
"        </B>\n" +
"   </A>\n" +
"</A_>";
			xd.setXDContext(xml);
			el = create(xd, "A", reporter);
			System.out.println(KXmlUtils.nodeToString(el, true));
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var {type x int(2,3); uniqueSet u {p: x; q: t};}'\n"+
"    a='? t()'\n"+
"    b='? x()'\n"+
"    c='? dateTime()'\n"+
"    d='? u.p.ID()'\n"+
"    e='? u.q.ID()'\n"+
"    f='? integer()'\n"+
"    g='? string(%enumeration=[\"a\",\"b\"])'\n"+
"    h='? long(%enumeration=[2,3])'\n"+
"    i='? s' />\n"+
"  <xd:declaration>\n"+
"     type s string(1,*);\n" +
"     type t enum(%argument=['a','b']);\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			XMDefinition xmdef = xp.getXMDefinition("");
			XMElement xmel = xmdef.getModel(null, "a");
			XMData xmd = xmel.getAttr("a");
			assertEq(xmd.getParserType(),  XDValue.XD_STRING);
System.out.println("RefTypeName: " + xmd.getRefTypeName());
			assertEq(xmd.getRefTypeName(), "t");
			assertEq(xmd.getParserName(), "enum");
			o = xmd.getParseParams();
			System.out.println(o);
			System.out.println(xmel.getAttr("a"));
			System.out.println(xmel.getAttr("b"));
			System.out.println(xmel.getAttr("c"));
			System.out.println(xmel.getAttr("d"));
			System.out.println(xmel.getAttr("e"));
			System.out.println(xmel.getAttr("f"));
			System.out.println(xmel.getAttr("g"));
			System.out.println(xmel.getAttr("h"));
			System.out.println(xmel.getAttr("i"));
			xml = "<a/>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
	try {
			xdef = //???
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A>\n"+
"    <B xd:script='occurs 2' a=\"xdatetime('dd.MM.yy|dd.MM.yyyy')\"/>\n"+
"    <C xd:script='occurs 2' a=\"xdatetime('dd.MM.yyyy|dd.MM.yy')\"/>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
"  <C a='11.06.87'/><C a='11.06.1987'/>\n"+
"</A>";
			parse(xp, "", xml, reporter);
			assertTrue((s = reporter.printToString()).contains("XDEF804")
				&& s.contains("/A/B[2]/@a") && reporter.getErrorCount() == 1);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
" <xd:declaration scope='global'>\n"+
"   <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
" </xd:declaration>\n"+
" <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
" <xd:declaration scope='local'>\n"+
"  <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
" </xd:declaration>\n"+
" <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
"  <xd:macro name=\"moreAll\">options moreAttributes,moreElements;\n"+
"    </xd:macro>\n"+
"</xd:declaration>\n" +
"<xd:def root='A'>\n"+
"  <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrors(reporter);
			xp = compile(new String[] {
"<xd:declaration xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>\n"+
" <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
"</xd:declaration>",
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A xd:script='${moreAll}'/>\n"+
"</xd:def>"
			});
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:lexicon language='eng' >\n"+
"#a      =    a\n"+
"#a/c    =    b\n"+
"#a/c/@f =    e\n"+
"</xd:lexicon>\n"+
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='slk' >\n"+
"#a      =    a\n"+
"#a/c    =    d\n"+
"#a/c/@f =    g\n"+
"</xd:lexicon>\n"+
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='ces' default='yes' />\n"+
"<xd:component>\n"+
"  %class mytest.component.A %link #a;\n"+
"</xd:component>\n"+
"<a><c f='string'/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();

			xd.setLexiconLanguage("eng");
			xml = "<a><b e='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("slk");
			xml = "<a><d g='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("ces");
			xml = "<a><c f='a'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("ces");
			xml = "<a><c f='a'/></a>";
			XComponent xcA =
				parseXC(xd,
					xml, Class.forName("mytest.component.A"), reporter);
			assertNoErrors(reporter);
			assertEq(xml, xcA.toXml());
			xd = xp.createXDDocument();
			xd.setLexiconLanguage("eng");
			xml = "<a><b e='a'/></a>";
			xc = parseXC(xd,
				xml, Class.forName("mytest.component.A"), reporter);
//			xd.setDestLexiconLanguage("eng");
			assertNoErrors(reporter);
			assertEq("<a><b e='a'/></a>", xc.toXml());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Contract'>\n"+
"<xd:component>\n"+
"  %class mytest.component.Contract %link #Contract;\n"+
"</xd:component>\n"+
"<Contract Number=\"num()\">\n"+
"  <Client xd:script=\"+\"\n"+
"     Type=\"int()\"\n"+
"     Name=\"? string\"\n"+
"     ID=\"? num()\"\n"+
"     GivenName=\"? string\"\n"+
"     LastName=\"? string\"\n"+
"     PersonalID=\"? string\" />\n"+
"</Contract>\n"+
"<Agreement Date=\"required; create toString(now(),'yyyy-MM-dd HH:mm');\"\n"+
"           Number=\"required num(10); create from('@Number');\" >\n"+
"  <Owner xd:script= \"occurs 1;\n"+
"                         create from('Client[@Typ=\\'1\\']');\" \n"+
"           ID=\"required num(8); create from('@ID');\"\n"+
"           Name=\"required string(1,30); create from('@Name');\" />\n"+
"  <Holder xd:script=\"occurs 1; create from('Client[@Typ=\\'2\\']');\" \n"+
"          PID=\"required string(10,11); create from('@PID');\"\n"+
"          GivenName=\"required string(1,30); create from('@GivenName');\"\n"+
"          LastName=\"required string(1,30); create from('@LastName');\" />\n"+
"  <Mediator xd:script=\"occurs 1; create from('Client[@Typ=\\'3\\']');\"\n"+
"            ID=\"required num(8); create from('@IČO');\"\n"+
"            Name=\"required string(1,30);\n"+
"              create toString(from('@GivenName'))+' '+from('@LastName');\"/>\n"+
"</Agreement>\n"+
"</xd:def>";
			String[] params = new String[]{xdef,
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='eng'>\n"+
"#Contract =                         Contract\n"+
"#Contract/@Number =                 Number\n"+
"#Contract/Client =                  Client\n"+
"#Contract/Client/@Type =            Type\n"+
"#Contract/Client/@Name =            Name\n"+
"#Contract/Client/@ID =              ID\n"+
"#Contract/Client/@GivenName =       GivenName\n"+
"#Contract/Client/@LastName =        LastName\n"+
"#Contract/Client/@PersonalID =      PersonalID\n"+
"#Agreement =                        Agreement\n"+
"#Agreement/@Date =                  Date\n"+
"#Agreement/@Number =                Number\n"+
"#Agreement/Owner =                  Owner\n"+
"#Agreement/Owner/@ID =              ID\n"+
"#Agreement/Owner/@Name =            Name\n"+
"#Agreement/Holder =                 Holder\n"+
"#Agreement/Holder/@PID =            PID\n"+
"#Agreement/Holder/@GivenName =      GivenName\n"+
"#Agreement/Holder/@LastName =       LastName\n"+
"#Agreement/Mediator =               Mediator\n"+
"#Agreement/Mediator/@ID =           ID\n"+
"#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>",
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='ces'>\n"+
"#Contract =                         Smlouva\n"+
"#Contract/@Number =                 Číslo\n"+
"#Contract/Client =                  Klient\n"+
"#Contract/Client/@Type =            Role\n"+
"#Contract/Client/@Name =            Název\n"+
"#Contract/Client/@ID =              IČO\n"+
"#Contract/Client/@GivenName =       Jméno\n"+
"#Contract/Client/@LastName =        Příjmení\n"+
"#Contract/Client/@PersonalID =      RodnéČíslo\n"+
"#Agreement =                        Dohoda\n"+
"#Agreement/@Date =                  Datum\n"+
"#Agreement/@Number =                Číslo\n"+
"#Agreement/Owner =                  Vlastník\n"+
"#Agreement/Owner/@ID =              IČO\n"+
"#Agreement/Owner/@Name =            Název\n"+
"#Agreement/Holder =                 Držitel\n"+
"#Agreement/Holder/@PID =            RČ\n"+
"#Agreement/Holder/@GivenName =      Jméno\n"+
"#Agreement/Holder/@LastName =       Příjmení\n"+
"#Agreement/Mediator =               Prostředník\n"+
"#Agreement/Mediator/@ID =           IČO\n"+
"#Agreement/Mediator/@Name =         Název\n"+
"</xd:lexicon>",
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='deu'>\n"+
"#Contract =                         Vertrag\n"+
"#Contract/@Number =                 Nummer\n"+
"#Contract/Client =                  Klient\n"+
"#Contract/Client/@Type =            Art\n"+
"#Contract/Client/@Name =            Name\n"+
"#Contract/Client/@ID =              Organisations-ID\n"+
"#Contract/Client/@GivenName =       Vorname\n"+
"#Contract/Client/@LastName =        Nachname\n"+
"#Contract/Client/@PersonalID =      Personalausweis\n"+
"#Agreement =                        Zustimmung\n"+
"#Agreement/@Date =                  Datum\n"+
"#Agreement/@Number =                Nummer\n"+
"#Agreement/Owner =                  Inhaber\n"+
"#Agreement/Owner/@ID =              Organisations-ID\n"+
"#Agreement/Owner/@Name =            Name\n"+
"#Agreement/Holder =                 Halter\n"+
"#Agreement/Holder/@PID =            Geburtsnummer\n"+
"#Agreement/Holder/@GivenName =      Vorname\n"+
"#Agreement/Holder/@LastName =       Nachname\n"+
"#Agreement/Mediator =               Vermittler\n"+
"#Agreement/Mediator/@ID =           Organisations-ID\n"+
"#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>",
			};
			xp = compile(params);
			genXComponent(xp, tempDir);
			xd = xp.createXDDocument();
			xml =
"<Smlouva Číslo = \"0123456789\">\n"+
"  <Klient Role       = \"1\"\n"+
"          Název      = \"Nějaká Firma s.r.o.\"\n"+
"          IČO        = \"12345678\" />\n"+
"  <Klient Role       = \"2\"\n"+
"          Jméno      = \"Jan\"\n"+
"          Příjmení   = \"Kovář\"\n"+
"          RodnéČíslo = \"311270/1234\" />\n"+
"  <Klient Role       = \"3\"\n"+
"          Jméno      = \"František\"\n"+
"          Příjmení   = \"Bílý\"\n"+
"          RodnéČíslo = \"311270/1234\"\n"+
"          IČO        = \"87654321\" />\n"+
"</Smlouva>";
			xd.setLexiconLanguage("ces");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Type  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd.setLexiconLanguage("deu");
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Type  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			System.out.println(KXmlUtils.nodeToString(el, true));
			assertNoErrors(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='B' root='A'>\n"+
"  <xd:declaration> Parser p; uniqueSet u {x: p} </xd:declaration>\n"+
"  <A id=\"? xdType(); onTrue p = getParsedValue();\" />\n"+
"</xd:def>\n"+
"</xd:collection>";
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(xdef);
			oos.writeObject(compile(xdef));
			oos.writeObject("<A id=\"string()\"/>");
			oos.close();
			ByteArrayInputStream bais =
				new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			ois.readObject();
			xp = (XDPool) ois.readObject();
			xml = (String) ois.readObject();
			ois.close();
			xd = xp.createXDDocument("B");
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			assertEq(xml, xd.xparse(el, null));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>required {\n"+
"              switch(getText()) {\n"+
"                case '1': return true;\n"+
"                default: return false;\n"+
"              }\n"+
"            }\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a>1</a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
" type t {\n" +
"          switch(getText()) {\n"+
"            case '1': return true;\n"+
"            default: return false;\n"+
"          }\n"+
"        }\n"+
"</xd:declaration>\n"+
"<a>required t; </a>\n"+
"</xd:def>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= [0-9]+\n"+
"      S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      intList ::= integer (S? \",\" S? integer)* S?\n"+
"    ');\n"+
"    type intList rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"<a>required intList()</a>\n"+
"</xd:def>";
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally out(parseInt(\"12x3\"))'>\n" +
"<xd:mixed>\n"+
"    match getOccurrence() == 0; ? string(); \n" +
"    <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"    match getOccurrence() == 0; string(); \n" +
"</xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("", strw.toString());
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally out(parseInt(\"12x3\"))'>\n" +
"    <xd:mixed>\n"+
"       match getOccurrence() == 0; ? string(); \n" +
"       <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"       match getOccurrence() == 0; string(); \n" +
"    </xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("", strw.toString());
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:textcontent='int(); create 123; option cdata;'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>123</a>";
			el = parse(xdef, "", xml, reporter);
			assertEq(xml, el);
			strw = new StringWriter();
			KXmlUtils.writeXml(strw,
				null, //encoding
				el,   //element to write
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[123]]></a>",strw.toString());
			assertNoErrors(reporter);
			el = create(xd, "a", reporter);
			assertNoErrors(reporter);
			strw = new StringWriter();
			KXmlUtils.writeXml(strw,
				null, //encoding
				el,   //element to write
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[123]]></a>",strw.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <A>\n"+
"    <xd:sequence xd:script='occurs *; create next()'>\n"+
"      <a xd:script=\"occurs 0..1; create methodA();\n"+
"                     finally outln(getElement());\n"+
"                     forget\"/>\n"+
"      <b xd:script=\"occurs 0..1; create methodB();\n"+
"                     finally outln(getElement());\n"+
"                     forget\"/>\n"+
"    </xd:sequence>\n"+
"  </A>\n"+
"</xd:def>";
			xd = compile(xdef, getClass()).createXDDocument();
			_stack.push("A");
			_stack.push("A");
			_stack.push("B");
			_stack.push("A");
			_stack.push("B");
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setUserObject(this);
			assertEq("<A><b/><a/><b/><a/><a/></A>", create(xd, "A", null));
			assertEq("<b/>\n<a/>\n<b/>\n<a/>\n<a/>\n", strw.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"   ParseResult licheCislo() {\n"+
"      ParseResult p = int();\n"+
"      if (p.intValue() % 2 == 0) {\n"+
"         p.setError(\"Cislo musí byt liche!\");\n"+
"      }\n"+
"      return p;\n"+
"   }\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo(); onTrue outln(getParsedInt()==1);\"/>"+
"</xd:def>\n";
			strw = new StringWriter();
			parse(xdef, "", "<a a='3'></a>", reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq("false\n", strw.toString());
			strw = new StringWriter();
			parse(xdef, "", "<a a='2'></a>", reporter, strw, null, null);
			assertErrors(reporter);
			assertEq("", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  ParseResult lc() {\n"+
"    ParseResult r = int();\n"+
"    int i = r.intValue();\n"+
"    if (i % 2 == 0) {\n"+
"      r.error(\"Cislo musí byt liche!\");\n"+
"    }\n"+
"    return r;\n"+
"  }\n"+
"   type licheCislo lc;\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo()\"/>"+
"</xd:def>\n";
			parse(xdef, "", "<a a='1'></a>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a a='2'></a>", reporter);
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"   boolean licheCislo() {\n"+
"     return parseInt(getText()) % 2 != 0\n"+
"       ? true : error(\"Cislo musí byt liche!\");\n"+
"   }\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo()\"/>"+
"</xd:def>\n";
			parse(xdef, "", "<a a='1'></a>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a a='2'></a>", reporter);
			assertErrors(reporter);
			xdef = // var in model with reference
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var {int j, i=123;}'>\n"+
"    <xd:choice xd:script=\"?\">\n"+
"      <O xd:script=\"occurs 0..2;\n"+
"           finally if (i != 123) throw new Exception('error');\"/>\n"+
"      <A xd:script=\"occurs 1;\n"+
"            finally if (i != 123) throw new Exception('error');\">\n"+
"        <X xd:script=\"optional; ref Y;\"/>\n"+
"      </A>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"  <Y xd:script=\"var {String t='T';}\"/>\n"+
"</xd:def>";
			parse(xdef, "", "<a><O/></a>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a><O/><O/></a>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a><A><X/></A></a>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a/>", reporter);
			assertNoErrors(reporter);
			parse(xdef, "", "<a><O/><O/><O/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xdef, "", "<a><O/><A/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
		} catch (Exception ex) {fail(ex);}
if(T){return;}
boolean chkSynteax = getChkSyntax();
		try {
			setChkSyntax(false);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
" external method {\n"+
"   void mytest.xdef.MyTest_0.p3(Long,) as p;\n"+
"   boolean mytest.xdef.MyTest_0.p3(Long) as;\n"+
"   long mytest.xdef.MyTest_0.p3(Long) ppp;\n"+
"   double mytest.xdef.MyTest_0.p3 Long);\n"+
"   String mytest.xdef.MyTest_0.p2(String, Long as p2;\n"+
"   mytest.xdef.MyTest_0.p2(String, Long)\n"+
"   mytest.xdef.MyTest_0.p3(XDValue[) as pp\n"+
" }\n"+
"</xd:declaration>\n"+
"<a id = \"int; onTrue pp('a', 5)\" xd:script = 'finally p(3)'/>\n"+
"</xd:def>\n";
			compile(xdef);
			fail("Error not recognized");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || s.indexOf("SYS012") < 0
				|| s.indexOf("XDEF410") < 0 || s.indexOf("XDEF362") < 0
				|| s.indexOf("XDEF220") < 0 || s.indexOf("XDEF443") < 0
				|| s.indexOf("XDEF412") < 0 || s.indexOf("XDEF225") < 0
				|| s.indexOf("XDEF228") < 0) {
				fail(ex);
			}
		}
if(T){return;}
setChkSyntax(chkSynteax);
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <b>required string()</b>\n"+
"  <xd:sequence occurs = '*'>\n"+
"    required string();\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"  <b>required string()</b>\n"+
"  <xd:sequence occurs = '*'>\n"+
"    required string();\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</a>\n"+
"</xd:def>\n";
			xml = "<a><b>0</b>1<c/>2<c/><b>3</b>4<c/>5<c/></a>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>init out('i#{p}');finally out('f#{p}')</xd:macro>\n"+
"  <a xd:script=\"${m(p='a')}\">\n"+
"    <xd:choice>\n"+
"      <xd:sequence>\n"+
"        <c xd:script=\"1; ${m(p='c1')}; create from('/a/c')\"/>\n"+
"        <d xd:script=\"?; ${m(p='d1')}; create from('//a/d')\"/>\n"+
"        <e xd:script=\"?; ${m(p='e1')}\"/>\n"+
"      </xd:sequence>\n"+
"      <xd:sequence>\n"+
"        <d/>\n"+
"        <c xd:script=\"occurs ?; ${m(p='c2')}\"/>\n"+
"        <e xd:script=\"occurs *; ${m(p='e2')}\"/>\n"+
"      </xd:sequence>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a/>";
			xp = compile(xdef);
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			if (reporter.getErrorCount() == 0) {
				fail("error not reported");
			} else {
				assertTrue(reporter.getErrorCount() == 1 &&
					reporter.getReport().getModification().indexOf("/a") > 0,
					reporter.printToString());
			}
			assertEq("iafa", strw.toString());
			assertEq(el, xml);
			xml = "<a><c/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrors(reporter);
			assertEq("iaic1fc1fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrors(reporter);
			assertEq("iafa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/><c/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrors(reporter);
			assertEq("iaic2fc2fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrors(reporter);
			assertEq("iaie2fe2fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><c/><d/><e/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount()==1&&s.indexOf("XDEF501")>=0,s);
			assertEq("iaic1fc1id1fd1ie1fe1fa", strw.toString());
			assertEq(el, "<a><c/><d/><e/></a>");
			xml = "<a><c/><d/><d/><e/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrors(reporter);
			assertEq("iaic1fc1id1fd1ie2fe2ie2fe2fa", strw.toString());
			assertEq(el, xml);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"<xd:macro name='m' p='?'>init out('i#{p}');finally out('f#{p}')</xd:macro>\n"+
" <A xd:script=\"1;${m(p='A')}\">\n"+
"   <xd:sequence xd:script=\"1;${m(p='SQ')}\">\n"+
"      <B xd:script=\"1;${m(p='B')}; create from('/A/B')\"/>\n"+
"      <xd:any xd:script=\"1; ${m(p='X')}; create from('/A/C')\"/>\n"+
"   </xd:sequence>\n"+
"   <D xd:script=\"1;${m(p='D')}; create from('/A/D')\"/>\n"+
" </A>\n"+
"</xd:def>";
			xml = "<A><B/><C/><D/></A>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setXDContext(xml);
			el = create(xd, "A", reporter);
			xout.close();
			assertNoErrors(reporter);
			assertEq(xml, el);
			assertEq("iAiSQiBfBiXfXfSQiDfDfA", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			el = parse(xd, xml, reporter);
			strw.close();
			assertNoErrors(reporter);
			assertEq(xml, el);
			assertEq("iAiSQiBfBiXfXfSQiDfDfA", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script='init out(\"ia\");finally out(\"fa\")'>\n"+
"   <xd:sequence xd:script='1;init out(\"im\");finally out(\"fm\")'>\n"+
"      <b xd:script='init out(\"ib\");finally out(\"fb\")'/>\n"+
"      <xd:any xd:script='1; init out(\"ix\");finally out(\"fx\")'/>\n"+
"   </xd:sequence>\n"+
"   <c xd:script='init out(\"ic\");finally out(\"fc\")'/>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/></a>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setXDContext(xml);
			el = xd.xcreate("a", reporter);
			xout.close();
			assertNoErrors(reporter);
			assertEq("<a><b/><b/><c/></a>", el);
			assertEq("iaimibfbixfxfmicfcfa", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			el = parse(xd, xml, reporter);
			strw.close();
			assertNoErrors(reporter);
			assertEq(xml, el);
			assertEq("iaimibfbixfxfmicfcfa", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"    <a>\n"+
"        <xd:mixed xd:script='?;'>\n"+
"            <d xd:script='1; create from(\"/a/d\")'/>\n"+
"        </xd:mixed>\n"+
"    </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a></a>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			el = xd.xcreate("a", reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"    <z/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/><p/><q/><z/></a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:mixed><p/><q/></xd:mixed>\n"+
"      <b/><c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><p/><q/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><q/><p/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><p/><p/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b/><q/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><b/><p/><p/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b/><c/><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2;create 2'>\n"+
"      <b xd:script=\"*;create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create ++$typ EQ 3\"/>\n"+
"      <d xd:script=\"create $typ EQ 3\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef); //vytvořeni ze zdroju
			create(xp, "", "a", reporter);
			assertFalse(xd.errorWarnings(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' xd:root = \"err\" >\n"+
"<err xd:script = \"finally exc();\">\n"+
"</err>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			parse(xp, "", "<err/>", reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			assertTrue(s.indexOf("bla...") >= 0, s);
		}
if(T){return;}
		try {//union with base
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  NamedValue n = %y = 'Y';\n"+
"  Container m1 = [n, %a='A',%b='B', 1];\n"+
"  Container m2 = new Container();\n"+
"  Container c = [1, 'x'];\n"+
"</xd:declaration>\n"+
" <a>\n"+
"   string;\n"+
"   onTrue {\n"+
"     m2.setNamedItem(new NamedValue('x', 1));\n"+
"     m2.setNamedItem(n); m2.setNamedItem('z', 'Z');\n"+
"     m2.setNamedItem(n = %q = 'Q');\n"+
"     setText(m1.getNamedItem('a') + ',' + m1.getNamedItem('b') +\n"+
"       ',' + m2.getNamedItem('x') +','+ m2.getNamedItem('y') + ',' + \n"+
"       m2.getNamedItem('z') + ',' +  c.getItemType(0) + ',' +\n"+
"       c.getItemType(1) +','+ n +','+ m2.getNamedItem('q') + ',' +\n"+
"       m1.getNamedItem('y') + ',' + m1.item(0) + ',' + cPar(m1));\n"+
"   }\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			el = parse(xp, "", "<a>x</a>", reporter);
			assertEq("<a>A,B,1,Y,Z,1,12,%q=Q,Q,Y,1,true</a>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name = 'test' root = 'a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"    %enumeration=['true 1', 'false 2']); \n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='true 1' />", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='false 2' />", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a='false 1' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='Example' root='a'> <a>required myType()</a> </xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration scope='global'>\n"+
"    BNFGrammar $rrr = new BNFGrammar('\n"+
"      S        ::= $whitespace+ /*skipped white spaces*/\n"+
"      intList  ::= $integer (S? \",\" S? $integer)*\n"+
"      name     ::= $uppercaseLetter $lowercaseLetter+\n"+
"      fullName ::= name S ($uppercaseLetter \".\" S)? name\n"+
"      nameList ::= fullName (S? \",\" S? fullName)*\n"+
"      list     ::= intList | nameList\n"+
"    ');\n"+
"    type myType $rrr.check('list');\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xd = xp.createXDDocument("Example");
			parse(xd, "<a>123</a>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a>123, 456, 789</a>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a>Arthur C. Clark, Jack London</a>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a>Arthur C Clark, Jack London</a>", reporter);
			assertTrue(reporter.errorWarnings(),"error not recognized");
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='Example' root='a'> <a> required myType() </a> </xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration scope='global'>\n"+
"     type myType $rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"  <xd:BNFGrammar name = \"$base\" scope='global'>\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar name = \"$rrr\" extends = \"$base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\" S)? name\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xd = xp.createXDDocument("Example");
			parse(xd, "<a>123, 456, 789</a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= S? [0-9]+\n"+
"      S       ::= [#9#10#13 ]+  /*skip redundant white spaces*/\n"+
"      intList ::= integer (S? \",\" integer)* S?  /*list of integers*/\n"+
"    ');\n"+
"    type intList rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"\n"+
"<a>required intList()</a>\n"+
"\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a> 123, 456, 789 </a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal(%whiteSpace = \"collapse\");\n"+
"       options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal(%whiteSpace = \"collapse\");\n"+
"       options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='   1   ' />", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal; options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = list(%item=decimal, %enumeration=['1', '2', '3 4']); \n"+
"  Parser t = union(%item=[boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a=' true' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 2 ' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='3 4' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 7 ' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' true 1 ' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"                      %enumeration=['true 1', 'false 2']); \n"+
"  Parser t = union(%item=[decimal,boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a=' true 1 ' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' true ' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"                      %enumeration=['true 1', 'false 2']);\n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true 1' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='false 2' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='false 1' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = string(%enumeration=['abc', 'xyz']); \n"+
"  Parser t = union(%item=[decimal, boolean, s ]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  type t  int;\n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='2' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(0,1)'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"optional; finally \n"+
"    {if(!gYear(%minInclusive='1999').check()) error('false');}\"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='0999'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='1999'/>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='list(%item=decimal(%minInclusive=1,%maxInclusive=$i," +
"      %totalDigits=1,%fractionDigits=0,%enumeration=[1,3]," +
"      %pattern=[\"\\\\d\"]))'/>"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1 4'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='1 3'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2000'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='list(%item=decimal(%minInclusive= -100))'/>"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='1 3'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2000'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='1 4'/>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=[1,3],%pattern=[\"\\\\d\"])'/>\n"+
"</xd:def>";
			xd = compile(xdef, getClass()).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1.23'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='+1.23'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='-99999999999999999.0000999999'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='--99999999999999999.0000999999'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(%base=decimal(%minInclusive=0),%minInclusive=1," +
"      %maxInclusive=5,%totalDigits=1,%fractionDigits=0," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			xd.xparse("<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal(%minInclusive=1, %maxInclusive=$i, %totalDigits=1," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			xd.xparse("<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(0,5, %totalDigits=1,\n"+
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal( 0, $i, %totalDigits=1," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal(%base=decimal(%minInclusive=0),%minInclusive=1," +
"      %maxInclusive=5,%totalDigits=1,%fractionDigits=0,%enumeration=[1,3]," +
"      %pattern=[\"\\\\d\"])' />\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='union(%item=[decimal(%maxInclusive=5), boolean()])'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
		} catch (Exception ex) {fail(ex);}

		try {
			if (new File(tempDir).exists()) {
				SUtils.deleteAll(tempDir, true);
			}
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}