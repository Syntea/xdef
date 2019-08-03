/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytest;

import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.compile.CompileBase.InternalMethod;
import org.xdef.impl.compile.CompileBase.KeyParam;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;
import test.utils.XDTester;
import static test.utils.XDTester._xdNS;

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {

	public MyTest_0() {super(); setChkSyntax(true);}

	private static String getMethodDescr(final String name) {
		return "<" + name + "></" +  name + ">";
	}

	private static String getMethodParams(final String name) {
		InternalMethod im =
			CompileBase.getTypeMethod(CompileBase.NOTYPE_VALUE_ID, name);
		String result = "<" + name + ">";
//		result += "<description> . </description>";
		short[] partypes = im.getParamTypes();
		int maxparams = im.getMaxParams();
		if (maxparams >= 1 && partypes != null) {
			result += "<params>";
			String params = "";
			for (int i = 0; i < partypes.length; i++) {
				if (!params.isEmpty()) {
					params += ", ";
				}
				params += CompileBase.getTypeName(partypes[i]);
				String[] sqpars = im.getSqParamNames();
				if (i < sqpars.length) {
					params += '(' + sqpars[i] + '}';
				}
			}
			result += params;
			if (maxparams > partypes.length) {
				result += "...";
			}
			result += "</params>";
		}
//		String[] sqpars = im.getSqParamNames();
//		if (sqpars != null && sqpars.length > 0) {
//			result += "<params>";
//			for (String x : sqpars) {
//				result += ' ' + x;
//			}
//			result += "</params>";
//		}
		String keys;
		KeyParam[] kpars = im.getKeyParams();
		keys = "";
		for (KeyParam x : kpars) {
			if (!keys.isEmpty()) {
				keys += " ";
			}
			XDValue deflt = x.getDefaultValue();
			XDValue[] legals = x.getLegalValues();
			String params = "";
			if (legals != null && legals.length > 0) {
				for (XDValue y : legals) {
					if (!params.isEmpty()) {
						params += "|";
					}
					params += (y.getItemId() == XDValueID.XD_STRING)
						? '"' + y.toString() + '"' : y.toString();
					if (y.equals(deflt)) {
						params += '*';
					}
				}
				if (!params.isEmpty()) {
					params = '[' + params + ']';
				}
			} else {
				params += '(' + CompileBase.getTypeName(x.getType()) + ')';
			}
			if (!params.isEmpty()) {
				keys += '%' + x.getName() + params;
			}
		}
		if (!keys.isEmpty()) {
			result += "<keyParams>" + keys + "</keyParams>";
		}
		result += "</" + name + ">";
		return result;
	}
	
	@Override
	/** Run test and print error information. */
	public void test() {
		String tempDir = getTempDir();
		XDPool xp;
		String xdef;
		String xml;
		String s;
		try {
			if (new File(tempDir).exists()) {
				FUtils.deleteAll(tempDir, true);
				new File(tempDir).mkdir();
			}
		} catch (Exception ex) {fail(ex);}
/*xx*
		try {
		String[] names = new String[] {
			"anyURI",
			"base64Binary",
			"boolean",
			"byte",
			"date",
			"dateTime",
			"decimal",
			"double",
			"duration",
			"ENTITIES",
			"ENTITY",
			"float",
			"gDay",
			"gMonth",
			"gMonthDay",
			"gYear",
			"gYearMonth",
			"hexBinary",
			"ID",
			"IDREF",
			"IDREFS",
			"int",
			"integer",
			"language",
			"list",
			"long",
			"Name",
			"NCName",
			"negativeInteger",
			"NMTOKEN",
			"NMTOKENS",
			"nonNegativeInteger",
			"nonPositiveInteger",
			"normalizedString",
			"NOTATION",
			"positiveInteger",
			"QName",
			"short",
			"string",
			"time",
			"token",
			"union",
			"unsignedByte",
			"unsignedInt",
			"unsignedLong",
			"unsignedShort",
			"ID",
			"IDREF",
			"IDREFS",
		};
		s = "<SchemaValidationMethod>\n";
		for (String x : names) {
			s += getMethodParams(x);
		}
		s += "</SchemaValidationMethod>";
		s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
		FUtils.writeString(new File("C:/temp/SchemaValidationMethod.xml"), s, "UTF-8");
		s = "<SchemaValidationMethod>\n";
		for (String x : names) {
			s += getMethodDescr(x);
		}
		s += "</SchemaValidationMethod>";
		s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
		FUtils.writeString(new File("C:/temp/SchemaValidationMethod.eng.xml"), s, "UTF-8");
		names = new String[] {
			"an",
//			"base64",
//			"bool",
			"BNF",
			"contains",
			"containsi",
			"dateYMDhms",
//			"datetime",
//			"dec",
			"emailDate",
			"empty",
			"ends",
			"endsi",
			"enum",
			"enumi",
			"eq",
			"eqi",
//			"hex",
//			"ISOdate",
//			"ISOdateTime",
//			"ISOday",
//			"ISOduration",
			"ISOlanguage",
			"ISOlanguages",
//			"ISOmonth",
//			"ISOmonthDay",
//			"ISOtime",
//			"ISOyear",
//			"ISOyearMonth",
//			"jboolean",
//			"jnull",
//			"jnumber",
//			"jstring",
//			"jvalue",
			"languages",
			"letters",
			"MD5",
			"NCNameList",
			"nmTokens",
//			"normString",
//			"normToken",
//			"normTokens",
			"num",
//			"parseSequence",
			"QNameList",
			"QNameURI",
			"QNameURIList",
			"pic",
//			"picture",
			"regex",
			"sequence",
			"starts",
			"startsi",
			"xdatetime",
			"CHKID",
			"CHKIDS",
			"SET"};
		s = "<XDValidationMethod>\n";
		for (String x : names) {
			s += getMethodParams(x);
		}
		s += "</XDValidationMethod>";
		s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
		FUtils.writeString(new File("C:/temp/XDValidationMethod.xml"), s, "UTF-8");
		s = "<XDValidationMethod>\n";
		for (String x : names) {
			s += getMethodDescr(x);
		}
		s += "</XDValidationMethod>";
		s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
		FUtils.writeString(new File("C:/temp/XDValidationMethod.eng.xml"), s, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		System.exit(0);
/*xx*/	

		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		XDOutput out;
		StringWriter strw;
		boolean chkSynteax = getChkSyntax();
//		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef.display
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef.debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef.warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
		final boolean T = true; //This flag is used to return from a test
//		final boolean T = false; //This flag is used to return from a test
//		try {
////			xp = compile(
//			xp = XDFactory.compileXD(null,
//"<xd:def xmlns:xd='" + _xdNS + "' xmlns:x='a' root='M'>\n" +
//"<M xd:script=\"var String s = '';\">\n" +
//"    <Measurement xd:script=\"occurs 1..*;\n" +
//"      var { int count = 0; float total = 0; }\n"+
//"      finally s += @x:date +';n=' +count+ ',average='+ (total/count)+' ';\"\n"+
//"      x:date='required dateTime'>\n" +
//"      <Value xd:script='occurs 1..*; finally count++;'>\n" +
//"        required double; onTrue total += (float) getParsedValue();\n" +
//"      </Value>\n" +
//"    </Measurement>\n" +
//"    string; onAbsence addText(s);\n" +
//"  </M>\n" +
//"</xd:def>");
//			xml =
//"<M>" +
//"<Measurement xmlns:x = \"a\" x:date=\"2017-08-10T11:31:05\">" +
//"<Value>10</Value>" +
//"<Value>11.8</Value>" +
//"<Value>9.4</Value>" +
//"</Measurement>" +
//"<Measurement xmlns:x = \"a\" x:date=\"2017-08-10T13:01:27\">" +
//"<Value>12.35</Value>" +
//"</Measurement>" +
//"</M>";
//			assertEq(
//"<M>" +
//"<Measurement xmlns:x = \"a\" x:date=\"2017-08-10T11:31:05\">" +
//"<Value>10</Value>" +
//"<Value>11.8</Value>" +
//"<Value>9.4</Value>" +
//"</Measurement>" +
//"<Measurement xmlns:x = \"a\" x:date=\"2017-08-10T13:01:27\">" +
//"<Value>12.35</Value>" +
//"</Measurement>" +
//"2017-08-10T11:31:05;n=3,average=10.4 2017-08-10T13:01:27;n=1,average=12.35 "+
//"</M>", parse(xp, "", xml, reporter));
//			assertNoErrors(reporter);
//		} catch (Exception ex) {fail(ex);}
//if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:name=\"Test\" xd:root=\"A\">\n" +
"  <A a=''>\n" +
"    <a:a xmlns:a='a.a' a=''></a:a>\n" +
"    <a:a xmlns:a='a.b' a=''></a:a>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A a='a'>\n" +
"    <a:a xmlns:a='a.a' a='b'></a:a>\n" +
"    <a:a xmlns:a='a.b' a='c'></a:a>\n" +
"</A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
//if(T){return;}
		try {
boolean xxx;
xxx = false;
xxx = true;
			java.io.ByteArrayOutputStream baos;
			java.io.ObjectOutput outx;
			java.io.ObjectInput in;
/*xx*/
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xmlns:js='" + XDConstants.JSON_NS_URI + "'\n" +
"   xd:name=\"Test\" xd:root=\"js:json\">\n" +
"  <js:json>{\"A\":\"int();\"}</js:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef, 
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xmlns:js='" + XDConstants.JSON_NS_URI + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#js:json\"/>");
if (xxx) {
baos = new java.io.ByteArrayOutputStream();
outx = new java.io.ObjectOutputStream(baos);
outx.writeObject(xp);
outx.close();
in = new java.io.ObjectInputStream(
	new java.io.ByteArrayInputStream(baos.toByteArray()));
xp = (XDPool) in.readObject();
}
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"1234\"}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertErrors(reporter);
			reporter.clear();
/*xx*/
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"  xmlns:js='" + XDConstants.JSON_NS_URI_W3C + "'\n" +
"  xd:name=\"Test\" xd:root=\"js:json\">\n" +
"  <js:json>{\"A\":\"int();\"}</js:json>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef,
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"  xmlns:js='" + XDConstants.JSON_NS_URI_W3C + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#js:json\"/>");
if (xxx) {
baos = new java.io.ByteArrayOutputStream();
outx = new java.io.ObjectOutputStream(baos);
outx.writeObject(xp);
outx.close();
in = new java.io.ObjectInputStream(
	new java.io.ByteArrayInputStream(baos.toByteArray()));
xp = (XDPool) in.readObject();
}
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertNoErrors(reporter);
			reporter.clear();
			s = "{\"A\":\"1234\"}";
			assertTrue(!JsonUtil.jsonEqual(xd.jparse(s, "js:json", reporter),
				JsonUtil.parse(s)));
			assertErrors(reporter);
			reporter.clear();
/*xx*/			
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def root='A'\n" +
"  xmlns:xd='"+XDConstants.XDEF32_NS_URI+"'>\n" +
"    <A a='optional jstring()'>optional jstring();</A>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
/*xx*
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
//"<A>a\\\"</A>";
//"<A a='a\\\"'></A>";
"<A a='a\\\"\\tb'>a\\\"\\tb</A>";
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			assertEq(el, xd.xparse(xml, reporter));
			assertNoErrors(reporter);
			Object o = XmlToJson.toJson(el);
			assertTrue(JsonUtil.jsonEqual(o,
				JsonUtil.parse(JsonUtil.toJSONString(o))));
			System.out.println(JsonUtil.toJSONString(o));
			el = JsonToXml.toXmlXD(o);
			System.out.println(KXmlUtils.nodeToString(el, true));
			assertTrue(JsonUtil.jsonEqual(o, XmlToJson.toJson(el)));
/*xx*/
			s = "{\"\":\"\\\\\\\"\\t\"}";
			el = JsonUtil.jsonToXmlW3C(JsonUtil.parse(s));
			xml = KXmlUtils.nodeToString(el, true);
			System.out.println(xml);
			System.out.println(s);
			System.out.println(JsonUtil.toJsonString(JsonUtil.xmlToJson(el)));
/*xx*/
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
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("0", strw.toString());
			assertNoErrors(reporter);
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
			xp = compile(xdef);
			xd = xp.createXDDocument();
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
			assertEq("nullnullnullCDATAdecxdatetime", strw.toString());
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524")
				&& s.contains("DatumNarozeni") && s.contains("Jmeno"),
				reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
	}
	
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
	
}
