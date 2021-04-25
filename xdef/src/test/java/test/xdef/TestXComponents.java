package test.xdef;

import test.XDTester;
import org.xdef.XDPool;
import org.xdef.XDDocument;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.json.JsonUtil;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.sys.Price;
import org.xdef.sys.GPSPosition;

/** Test XComponents.
 * @author Vaclav Trojan
 */
public final class TestXComponents extends XDTester {
	String _tempDir;

	public TestXComponents() {super();}

	private static void genXPosList(final XComponent xc, StringBuilder sb) {
		sb.append(xc.xGetXPos()).append('\n');
		java.util.List<XComponent> childNodes = xc.xGetNodeList();
		if (childNodes != null && !childNodes.isEmpty()) {
			for (int i = 0; i < childNodes.size(); i++) {
				genXPosList(childNodes.get(i), sb);
			}
		}
	}

	private static String checkXPos(final XComponent xc) {
		StringBuilder sb = new StringBuilder();
		genXPosList(xc,sb);
		String before = sb.toString();
		sb.setLength(0);
		XComponentUtil.updateXPos(xc);
		genXPosList(xc, sb);
		String after = sb.toString();
		return before.equals(after)? "" : ("Before:\n"+before+"After:\n"+after);
	}

	/** Generate and compile X-components from X-definition sources.
	 * @param xdsources array with path names of sources of X-definitions.
	 */
	private XDPool genComponents(final String... xdsources) {
		// ensure that following classes are compiled!
		TestXComponents_C.class.getClass();
		TestXComponents_G.class.getClass();
		TestXComponents_Y04.class.getClass();
		TestXComponents_Y06Container.class.getClass();
		TestXComponents_Y06Domain.class.getClass();
		TestXComponents_Y06DomainContainer.class.getClass();
		TestXComponents_Y06XCDomain.class.getClass();
		TestXComponents_Y07Operation.class.getClass();
		TestXComponents_Y08.class.getClass();
		TestXComponents_Y21enum.class.getClass();
		// generate XCDPool from sources
		XDPool xp = compile(xdsources);
		// generate and compile XComponents from xp
		genXComponent(xp, new File(_tempDir));
		return xp;
	}

	@Override
	@SuppressWarnings("unchecked")
	/** Run test and print error information. */
	public void test() {
		String xml;
		Element el;
		XDDocument xd;
		String s;
		XComponent xc;
		Object obj, json;
		List list, list1;
		SDatetime sd;
		ArrayReporter reporter = new ArrayReporter();
		final String dataDir = getDataDir() + "test/";
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		_tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!_tempDir.endsWith("/")) {
			_tempDir += '/';
		}
		if (!f.isDirectory()) {
			throw new RuntimeException('\"' + _tempDir + "\" is not directory");
		}
		try { // GPSPosition, Price
			String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n" +
"<xd:declaration\n>\n"+
"  Price a;\n"+
"  GPSPosition p = new GPSPosition(50.08, 14.42, 399, 'Prague'), q;\n"+
"  int d; /* distance in km */\n"+
"</xd:declaration>\n"+
"<A xd:script='finally d = round(p.distanceTo(q)/1000); /* km */'\n"+
"   a='? price(); onTrue a= getParsedValue();'\n"+
"   q='gps(); onTrue q=getParsedValue();'/>\n"+
"<xd:component>\n"+
"  %class test.xdef.TY_GPS %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			XDPool xp = compile(xdef);
			genXComponent(xp, new File(_tempDir));
			xml = "<A a='1.25 CZK' q='48.2, 16.37, 151, Vienna'/>"; //
			xd = xp.createXDDocument();
			xc = xd.parseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq("1.25 CZK", xd.getVariable("a").stringValue());
			assertEq(252, xd.getVariable("d").intValue());
			assertEq(new GPSPosition(48.2, 16.37, 151, null),
				SUtils.getValueFromGetter(xc, "getq"));
			SUtils.setValueToSetter(xc, "seta", new Price(456.001, "USD"));
			assertEq("456.001 USD", xc.toXml().getAttribute("a"));
			xml = "<A q='51.52,-0.09,0,\"London\"'/>"; //,
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			assertEq(1030, xd.getVariable("d").intValue());
			assertEq("51.52, -0.09, 0.0, London",
				xd.getVariable("q").toString());
		} catch (Exception ex) {fail(ex);}

		XDPool xp = genComponents(getDataDir() + "test/TestXComponents.xdef",
			dataDir + "TestXComponent_Z.xdef");
		try {
			xml = "<A a='a' dec='123.45'><W w='wwwwwwww'/></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			xml = "<A a='a' dec='123.45'><W w='w'>wwwwwwww</W></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			xml =
"<A a='a' dec='123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>1</i><Y>3</Y>"+
"<d>2013-09-14</d><t>10:20:30</t><s>Franta</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>";
			xc = parseXC(xp, "A", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			el = xc.toXml();
			assertEq("/A/@a", SUtils.getValueFromGetter(xc, "xposOfa"));
			assertEq("/A/d[1]/$text",
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
					xc, "getd"), "xposOf$value").toString());
			list = (List) SUtils.getValueFromGetter(xc,"get$Y");
			assertEq("1", list.get(0));
			assertEq("2", list.get(1));
			assertEq("3", SUtils.getValueFromGetter(xc,"get$Y_1"));
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"),
				"get$value").toString(), "2013-09-14");
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"), "get$value"),
				SUtils.getValueFromGetter(
					SUtils.getValueFromGetter(xc, "getd"), "dateOf$value"));
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"), "get$value"),
				SUtils.getValueFromGetter(
					SUtils.getValueFromGetter(xc, "getd"),"timestampOf$value"));
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"), "get$value"),
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(xc, "getd"),
				"calendarOf$value"));
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "geti"),
				"get$value").toString(), "1");
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gett"),
				"get$value").toString(), "10:20:30");
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(2));
			assertEq("Franta", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"gets"), "get$value"));
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "gets"),
				"set$value", "Pepik");
			assertTrue(BigInteger.valueOf(2).equals(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "geti"), "get$value")));
			assertEq("Pepik", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gets"), "get$value"));
			list = (List)SUtils.getValueFromGetter(xc, "listOfd2");
			assertEq("/A/d2[1]",
				SUtils.getValueFromGetter(list.get(0),"xGetXPos"));
			assertEq("/A/d2[2]",
				SUtils.getValueFromGetter(list.get(1),"xGetXPos"));
			assertEq(xc.toXml(),
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>2</i><Y>3</Y>"+
"<d>2013-09-14</d><t>10:20:30</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(3));
			SDatetime date =  new SDatetime("2013-09-01");
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "getd"),
				"set$value", date);
			SDatetime time =  new SDatetime("11:21:31");
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "gett"),
				"set$value", time);
			assertTrue(BigInteger.valueOf(3).equals(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "geti"), "get$value")));
			assertEq(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"), "get$value"), date);
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			assertEq("", checkXPos(xc));
			xc = parseXC(xp, "A", el, null, null);
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			SUtils.setValueToSetter(xc, "setdec", new BigDecimal("456.01"));
			assertEq(xc.toXml(),
"<A a='a' dec='456.01'>"+
"<W w='w'/><W w='w1'>blabla</W><Y>1</Y><Y>2</Y><i>3</i><Y>3</Y>"+
"<d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			SUtils.setValueToSetter(xc, "setdec", new BigDecimal("123.45"));
			list = (List) SUtils.getValueFromGetter(xc, "listOfW");
			assertNull(SUtils.getValueFromGetter(list.get(0),"get$value"));
			assertEq("blabla",
				SUtils.getValueFromGetter(list.get(1),"get$value"));
			list.clear();
			((List) SUtils.getValueFromGetter(xc, "listOfY")).clear();
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(99));
			assertEq(xc.toXml(), //clone
"<A a='a' dec='123.45'>"+
"<i>99</i><Y>3</Y><d>2013-09-01</d><t>11:21:31</t><s>Pepik</s><Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d-e.f d-e.f='xx'>yy</d-e.f><g-h.i>zz</g-h.i>"+
"</A>");
			el = XComponentUtil.toXml(xc, xp.createXDDocument("B"), "A");
			xml = "<A id='99' date='2013-09-01' time='11:21:31' name='Pepik'/>";
			assertEq(xml, el);
			try {
				SUtils.setValueToSetter(xc, "setdec",new BigDecimal("456.001"));
				el = xc.toXml();
				xp.createXDDocument("A").xparse(el, null);
				fail("Error not reported" + el);
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("path=/A/@dec") < 0) {
					fail(ex);
				}
			}
			assertEq("", checkXPos(xc));

//			test.xdef.component.B.class.getClass(); // force compilation!
			el = XComponentUtil.toXml(xc, xp.createXDDocument("B"), "A");
			xc =	parseXC(xp, "B", el, null, null);
			assertEq(xc.toXml(), xml);
			xd = xp.createXDDocument("B");
			assertEq(xd.xparse(el, null), xml);
			xc = parseXC(xd, el, null, null);
			assertEq(xc.toXml(), xml);
			try {
				XComponentUtil.getVariable(xc, "nazdar");
				fail("Error not reported");
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("nazdar") < 0) {
					fail(ex);
				}
			}
			xml = "<Z z='z'/>";
			XMElement xe = xp.getXMDefinition("A").getModel(null, "A");
			xd = null;
			for (XMNode xm: xe.getChildNodeModels()) {
				if ("Z".equals(xm.getName())) {
					xd = ((XMElement) xm).createXDDocument();
					break;
				}
			}
			if (xd != null) {
				xc = parseXC(xd, xml, null, null);
				assertEq("z", XComponentUtil.getVariable(xc, "z"));
				assertEq("z", SUtils.getValueFromGetter(xc,"getz"));
				assertEq(xc.toXml(), xml);
				XComponentUtil.setVariable(xc, "z", "Z");
				assertEq("<Z z='Z'/>", xc.toXml());
				XComponentUtil.setVariable(xc, "z", null);
				assertEq("<Z/>", xc.toXml());
			} else {
				fail("Model not found");
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<Town Name='Praha'>" +
	"<Street Name='Dlouha'>" +
		"<House Num='1'>"+
			"<Person FirstName='Jan' LastName='Novak'></Person>"+
			"<Person FirstName='Jana' LastName='Novakova'></Person>"+
		"</House>"+
		"<House Num='2'/>"+
		"<House Num='3'>"+
			"<Person FirstName='Josef' LastName='Novak'></Person>"+
		"</House>"+
	"</Street>"+
	"<Street Name='Kratka'>"+
		"<House Num='1'>"+
			"<Person FirstName='Pavel' LastName='Novak'></Person>"+
		"</House>"+
	"</Street>"+
"</Town>";
			xc = parseXC(xp, "C", xml, null, null);
			assertEq(3, SUtils.getValueFromGetter(xc, "getTest"));
			assertEq(xml, xc.toXml());
			assertEq("Praha", SUtils.getValueFromGetter(xc, "getName"));
			obj = ((List) SUtils.getValueFromGetter(xc, "listOfStreet")).get(0);
			assertEq("Dlouha", SUtils.getValueFromGetter(obj, "getName"));
			obj = ((List) SUtils.getValueFromGetter(obj, "listOfHouse")).get(0);
			assertEq(1, SUtils.getValueFromGetter(obj, "getNum"));
			obj = ((List) SUtils.getValueFromGetter(obj,"listOfPerson")).get(0);
			assertEq("Jan", SUtils.getValueFromGetter(obj, "getFirstName"));
			assertEq("Novak", SUtils.getValueFromGetter(obj, "getLastName"));
			XMElement xe = xp.getXMDefinition("C").getModel(null, "Town");
			for (XMNode xn: xe.getChildNodeModels()) {
				if ("Street".equals(xn.getName())) {
					xe = (XMElement) xn;
					for (XMNode xn1: xe.getChildNodeModels()) {
						if ("House".equals(xn1.getName())) {
							xe = (XMElement) xn1;
							break;
						}
					}
				}
			}
			el = XComponentUtil.toXml(xc, xp, "C#Persons");
			xml =
"<Persons>"+
	"<Person>Jan Novak; Dlouha 1, Praha</Person>"+
	"<Person>Jana Novakova; Dlouha 1, Praha</Person>"+
	"<Person>Josef Novak; Dlouha 3, Praha</Person>"+
	"<Person>Pavel Novak; Kratka 1, Praha</Person>"+
"</Persons>";
			assertEq(xml, el);
			xml =
"<House Num='3'>"+
	"<Person FirstName='Jan' LastName='Novak'/>"+
	"<Person FirstName='Jana' LastName='Novakova'/>"+
"</House>";
			xc = parseXC(xe.createXDDocument(), xml, null, null);
			assertEq(3, SUtils.getValueFromGetter(xc, "getNum"));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<a:A xmlns:a='a.b'>"+
"<_/>"+
	"<B A='true'>1<X>true</X><X>false</X>2<Y>true</Y></B>"+
	"<I A='1'><X>2</X><X>3</X><Y>4</Y></I>"+
	"<F A='3.14'><X>-3.14</X><X>-3.15</X><Y>NaN</Y></F>"+
	"<G A='ahgkjfd01Q=='>"+
		"<X>bhgkjfd01Q==</X>"+
		"<X>chgkjfd01Q==</X>"+
		"<Y>dhgkjfd01Q==</Y>"+
	"</G>"+
	"<H A='0123456789ABCDEF'>"+
		"<X>ABCDEF03456789</X>"+
		"<X>89ABCDE34567</X>"+
		"<Y>6789</Y>"+
	"</H>"+
	"<P A='1.15'><X>0.1</X><X>123.0</X><Y>-12.0</Y></P>"+
	"<Q A='2013-09-25'><X>2013-09-26</X><X>2013-09-27</X><Y>2013-09-28</Y></Q>"+
	"<R A='P2Y1M3DT11H'><X>P2Y1M3DT12H</X><X>P2Y1M3DT13H</X><Y>P2Y1M3DT14H</Y>"+
	"</R>"+
	"<S A='abc'><X>abc</X><X>def</X><Y>ghi</Y></S>"+
	"<E/>"+
	"<T xmlns='x.y' t='s'><I/></T>"+
	"<a:T xmlns:a='a.b' a:t='t'><a:I/></a:T>"+
"</a:A>";
			xc = parseXC(xp, "D", xml, null, null);
			el = xc.toXml();
			assertEq(el, xml);
			assertEq("1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "get$value"));
			assertEq("/a:A/B[1]/$text", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "xposOf$value"));
			assertEq("2", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "get$value1"));
			assertEq("/a:A/B[1]/$text", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "xposOf$value1"));
			byte[] bytes;
			bytes = SUtils.decodeBase64("ahgkjfd01Q==");
			assertEq(bytes, SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getG"), "getA"));
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"getG"), "listOfX");
			assertEq(2,list.size());
			bytes = SUtils.decodeBase64("bhgkjfd01Q==");
			assertEq(bytes, SUtils.getValueFromGetter(list.get(0),"get$value"));
			bytes = SUtils.decodeBase64("dhgkjfd01Q==");
			assertEq(bytes, SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"getG"), "getY"), "get$value"));
			bytes = SUtils.decodeHex("0123456789ABCDEF");
			assertEq(bytes, SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"getH"), "getA"));
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"getH"), "listOfX");
			assertEq(2, list.size());
			bytes = SUtils.decodeHex("ABCDEF03456789");
			assertEq(bytes, SUtils.getValueFromGetter(list.get(0),"get$value"));
			bytes = SUtils.decodeHex("89ABCDE34567");
			assertEq(bytes, SUtils.getValueFromGetter(list.get(1),"get$value"));
			bytes = SUtils.decodeHex("6789");
			assertEq(bytes, SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getH"), "getY"), "get$value"));
			assertEq("t", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "geta$T"), "geta$t"));
			assertTrue(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "geta$T"), "geta$I") != null);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<if><class try='t'/></if>";
			xc = parseXC(xp, "E", xml, null, null);
			assertEq(xc.toXml(), xml);
			assertEq("t", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getClazz"), "gettry"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<X><A/></X>";
			xc = parseXC(xp, "F", xml, null, null);
			assertEq(xc.toXml(), xml);
			xml = "<X><B/></X>";
			xc = (XComponent) SUtils.getNewInstance("test.xdef.component.F");
			SUtils.setValueToSetter(xc, "setB", null);
			SUtils.setValueToSetter(xc, "setA",
				SUtils.getNewInstance("test.xdef.component.F$A"));
			assertEq(xc.toXml(), "<X><A/></X>");
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Y><B/><A/></Y>";
			xc = parseXC(xp, "F", xml, null, null);
			assertEq(xml, xc.toXml());
			obj = (XComponent) SUtils.getNewInstance("test.xdef.component.F1");
			SUtils.setValueToSetter(obj, "setA",
				SUtils.getValueFromGetter(xc, "getA"));
			XComponentUtil.setVariable((XComponent) obj,
				"B", SUtils.getValueFromGetter(xc, "getB"));
			assertEq(((XComponent) obj).toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<G g='g'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>";
			xd = xp.createXDDocument("G");
			obj = SUtils.getNewInstance("test.xdef.component.G");
			xd.setUserObject(obj);
			xc = parseXC(xd, xml, null, null);
			assertEq("<G g='g_'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>",
				xc.toXml());
			assertEq("x", SUtils.getValueFromGetter(
				SUtils.getObjectField(obj, "_X"), "getx"));
			assertEq("z", XComponentUtil.getVariable(
				(XComponent) SUtils.getObjectField(obj, "_Y"),"y"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<s:H xmlns:s='soap' s:encodingStyle='encoding'>"+
	"<s:Header>"+
		"<b:User xmlns:b='request' s:understand='true' IdentUser='Novak'/>"+
		"<b:Request xmlns:b='request' IdentZpravy='xx' s:understand='true'/>"+
	"</s:Header>"+
	"<s:Body><b:PingFlow xmlns:b='request' Flow='B1B'/></s:Body>"+
"</s:H>\n";
			xc = parseXC(xp, "H", xml, null, null);
			assertEq(xc.toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Ping/>";
			xc = parseXC(xp, "I", xml, null, null);
			assertEq(xc.toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><B/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><B/><C/><C/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml =
				"<B><X>a</X><C/><X x='x'/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<B><X>a</X><C/><C/><X x='x'/><C c='c'/><X xx='xx'/></B>";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<C>a<D/>b<D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml()); // ? <C>a<D/>c<D/>b</C> => poradi textu
			xml = "<C>a<D/>b<D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml()); // ? <C>a<D/>c<D/>b</C> => poradi textu
			xml = "<C>a<D/>b<D/><D/>c</C>\n";
			xc = parseXC(xp, "J", xml, null, null);
			assertEq(xml, xc.toXml()); //  <C>a<D/>c<D/><D/>b</C> =>poradi textu
		} catch (Exception ex) {fail(ex);}
		try {
			int century = new GregorianCalendar().get(Calendar.YEAR);
			int y = century % 100; // actual year in century;
			century /= 100; // actual century
			DecimalFormat df = new DecimalFormat("00");
			xml =
"<A>" +
"<c Kod='1' Cislo='1' Rok='00'/>" +
"<c Kod='1' Cislo='2' Rok='01'/>" +
"<c Kod='1' Cislo='3' Rok='10'/>" +
"<c Kod='1' Cislo='4' Rok='" + df.format(y-1)+ "'/>" +
"<c Kod='1' Cislo='5' Rok='" + df.format(y)+ "'/>" +
"<c Kod='1' Cislo='6' Rok='" + df.format(y+1)+ "'/>" +
"<c Kod='1' Cislo='7' Rok='99'/>" +
"123456" +
"<d a='23.6.2015'/>" +
"</A>";
			xc = parseXC(xp, "K", xml, null, null);
			assertEq(xml, xc.toXml());
			list = (List) SUtils.getValueFromGetter(xc, "listOfc");
			assertEq(7, list.size());

			int x;
			assertEq("1", SUtils.getValueFromGetter(list.get(0), "getCislo"));
			assertEq(String.valueOf((x=00)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(0), "getRok").toString());
			assertEq("2", SUtils.getValueFromGetter(list.get(1), "getCislo"));
			assertEq(String.valueOf((x=01)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(1), "getRok").toString());
			assertEq("3", SUtils.getValueFromGetter(list.get(2), "getCislo"));
			assertEq(String.valueOf((x=10)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(2), "getRok").toString());
			assertEq("4", SUtils.getValueFromGetter(list.get(3), "getCislo"));
			assertEq(String.valueOf((x=y-1)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(3), "getRok").toString());
			assertEq("5", SUtils.getValueFromGetter(list.get(4), "getCislo"));
			assertEq(String.valueOf((x=y)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(4), "getRok").toString());
			assertEq("6", SUtils.getValueFromGetter(list.get(5), "getCislo"));
			assertEq(String.valueOf((x=y+1)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(5), "getRok").toString());
			assertEq("7", SUtils.getValueFromGetter(list.get(6), "getCislo"));
			assertEq(String.valueOf((x=99)+(y<x ? century-1 : century)*100),
				SUtils.getValueFromGetter(list.get(6), "getRok").toString());
			assertEq("123456", "" + SUtils.getValueFromGetter(xc, "get$value"));
			assertEq("2015-06-23", "" + SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getd"), "geta"));
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			xml = "<L><D></D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<L><D a='a'>a</D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<L><D><E/></D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<L><D a='a'>a<E><F>b</F>c</E>d</D></L>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<xx><D a='a'>a<E><F>b</F>c</E>d</D></xx>\n";
			xc = parseXC(xp, "L", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/><C/><B b='b'/><C c='c'/></A>";
			xc = parseXC(xp, "M", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><Operation One='9' Two='a'/></A>";
			xc = parseXC(xp, "N", xml, null, null);
			assertEq(xml, xc.toXml());
			obj = SUtils.getValueFromGetter(xc, "getOperation");
			assertEq(9, SUtils.getValueFromGetter(obj, "getOne"));
			assertEq("a", SUtils.getValueFromGetter(obj, "getTwo"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A>" +
"  <a a='3.1' b='3.3.1999'/>\n"+
"  <b a='4.1' b='4.3.1999'/>\n"+
"  <b a='5.1' b='5.3.1999'/>\n"+
"  <c a='4.1' b='4.3.1999'/>\n"+
"  <d a='4.1' b='4.3.1999'/>\n"+
"</A>";
			xc = parseXC(xp, "P", xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xc.toXml(), parse(xp.createXDDocument("P"), xml,reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<X><A><B><E>1</E></B><C/></A><A><B/><B><E>2</E></B><C/><C/></A><A/></X>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq(xml, xc.toXml());
			list = (List) SUtils.getValueFromGetter(xc, "listOfA");
			assertEq(3, list.size());
			assertEq(1, ((List)SUtils.getValueFromGetter(
				list.get(0), "listOfC")).size());
			assertEq(1, ((List)SUtils.getValueFromGetter(
				list.get(0), "listOfB")).size());
			xml = "<Y><A V='2'/>xx<B/></Y>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq(xml, xc.toXml());
			el = xc.toXml();
			xc = parseXC(xp, "X", el, null, null);
			assertEq(el, xc.toXml());

			xml = "<Y><A V='2'/>xx<B/></Y>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq(xml, xc.toXml());
			el = xc.toXml();
			xc = parseXC(xp, "X", el, null, null);
			assertEq(el, xc.toXml());
			assertEq("abc", SUtils.getValueFromGetter(xc, "getXX"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Test><Operation One='prvni' Two='druhy' x='X'/></Test>";
			xc = parseXC(xp, "Y01", xml, null, null);
			assertEq(xml, xc.toXml());
			obj = SUtils.getValueFromGetter(xc, "getOperation");
			assertEq("prvni", SUtils.getValueFromGetter(obj, "getOne"));
			SUtils.setValueToSetter(obj, "setOne", "first");
			assertEq("first", SUtils.getValueFromGetter(obj, "getOne"));
			assertEq("X", SUtils.getValueFromGetter(obj, "getx"));
			SUtils.setValueToSetter(obj, "setx", "Y");
			assertEq("Y", SUtils.getValueFromGetter(obj, "getx"));
			xml = "<Test One='prvni' Two= 'druhy'/>";
			xc = parseXC(xp, "Y02", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("prvni", SUtils.getValueFromGetter(xc, "getOne"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Part><PartOne One='one'/><PartTwo One='1'/></Part>";
			xc = parseXC(xp, "Y03", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("one", SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getPartOne"), "getOne"));
			assertEq(1, SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getPartTwo"), "getOne"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Part One='1' Two='Two'/>";
			xc = parseXC(xp, "Y04", xml, null, null);
			assertEq("One", SUtils.getValueFromGetter(xc, "getJedna"));
			assertEq("Two", SUtils.getValueFromGetter(xc, "getTwo"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A One='Jedna' Two='Dva'/>";
			xc = parseXC(xp, "Y05a", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("Jedna", SUtils.getValueFromGetter(xc, "getOne"));
			assertEq("Dva", SUtils.getValueFromGetter(xc, "getTwo"));
			xml = "<B One='Jedna' Two='Dva'/>";
			xc = parseXC(xp, "Y05", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("Jedna", SUtils.getValueFromGetter(xc, "getOne"));
			assertEq("Dva", SUtils.getValueFromGetter(xc, "getTwo"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B One='Jedna' Two='Dve'/></A>";
			xc = parseXC(xp, "Y06", xml, null, null);
			assertEq("<B One='Jedna' Two='Dve'/>",
				((XComponent) SUtils.getValueFromGetter(
					xc, "getDomain")).toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A Id='123'><Domain One='Jedna' Two='Dve'/></A>";
			xc = parseXC(xp, "Y08", xml, null, null);
			assertEq(124, SUtils.getValueFromGetter(xc, "getIdFlow"));
			SUtils.setValueToSetter(xc, "setIdFlow", 456);
			xml = "<A Id='457'><Domain One='Jedna' Two='Dve'/></A>";
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A x='X' y='Y'><a b='x'><b/></a></A>";
			xc = parseXC(xp, "Y09", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("<a b='x'><b/></a>",
				((XComponent) SUtils.getValueFromGetter(
					xc, "get$any")).toXml());
			assertEq("/A", SUtils.getValueFromGetter(xc, "xGetXPos"));
			assertEq("/A/a[1]", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "get$any"), "xGetXPos"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A a='A' b='B' c='XX'><a x='x'/><b x='xx'/></A>";
			xc = parseXC(xp, "Y10", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("x",SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getp"), "getx"));
			assertEq("xx",SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getq"), "getx"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A><B N='p' I='P'>" +
"<Ev N='J' M='O_'><Co C='M' I='No'><X/></Co><Y Y='X'/></Ev>" +
"<Ev N='N' M='B_'><Co C='B' I='No'><X/></Co><Y Y='Y'/></Ev>" +
"<Op N='D' M='D_'><Co C='M' I='Yes'><X/></Co>"+
"<Co C='B' I='Yes'><X/></Co><Y Y='Z'/></Op>" +
"</B></A>";
			xc = parseXC(xp, "Y11", xml, null, null);
			assertEq(xml, xc.toXml());
			list = (List)  SUtils.getValueFromGetter(xc, "listOfB");
			list = (List) SUtils.getValueFromGetter(list.get(0), "listOfOp");
			assertEq("D", SUtils.getValueFromGetter(list.get(0), "getN"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B><P O='oo'/><S V='s'/><Q O='q'/><P O='o'/></B></A>";
			xc = parseXC(xp, "Y12", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<a><b/><c/>1<b/><c/>2</a>";
			xc = parseXC(xp, "Y12", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			try {
				Class.forName("test.xdef.component.Y13$A$B");
				fail("Error Y13: "
					+ "class test.xdef.component.Y13.A.B was generated.");
			} catch (Exception ex) {}
			xml = "<A><B a='1'/></A>";
			xc = parseXC(xp, "Y13", xml,null,null);
			assertEq(xml, xc.toXml());
			assertEq("1",SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getB"), "geta"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<C><X a='1'/></C>";
			xc = parseXC(xp, "Y14", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq("1",SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc, "getX"), "geta"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<a>1<b/>2</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(xc, "get$value"));
			assertEq("2", SUtils.getValueFromGetter(xc, "get$value1"));
			assertEq("/a/$text", SUtils.getValueFromGetter(xc,"xposOf$value1"));
			assertNoErrorwarnings(reporter);
			xml = "<a>1</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(xc, "get$value"));
			assertNull(SUtils.getValueFromGetter(xc, "get$value1"));
			assertEq("/a/$text", SUtils.getValueFromGetter(xc, "xposOf$value"));
			assertEq("/a/$text", SUtils.getValueFromGetter(xc,"xposOf$value1"));
			assertNoErrorwarnings(reporter);
			xml = "<b>1</b>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			if (xc != null) {
				fail("XComponent shlould be null");
			} else {
				assertTrue(reporter.errors());
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B><B_1><C><B b='1'/></C></B_1></B></A>";
			xc = parseXC(xp, "Y19", xml, null, null);
			assertEq(xml, xc.toXml());
			assertEq(1, SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(xc, "getB"),
					"getB_1"), "getC"), "getB_2"), "getb"));
			s = (String) SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(xc, "getB"),
					"getB_1"), "getC"), "getB_2"), "getClass"), "getName");
			assertTrue(s.endsWith("B_2"), s);
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><x:b xmlns:x='x.int' y='1'/></a>";
			Object x = SUtils.getNewInstance("test.xdef.component.Y16");
			Object y = SUtils.getNewInstance("test.xdef.component.Y16a");
			SUtils.setValueToSetter(y, "sety", 1);
			SUtils.setValueToSetter(x, "setx$b", y);
			json = JsonUtil.xmlToJson(KXmlUtils.parseXml(xml)
				.getDocumentElement());
			el = ((XComponent)x).toXml();
			assertEq(xml, el);
			if (!JsonUtil.jsonEqual(json, JsonUtil.xmlToJson(el))) {
				fail();
			}
			x = SUtils.getNewInstance("test.xdef.component.Y16c");
			y = SUtils.getNewInstance("test.xdef.component.Y16d");
			SUtils.setValueToSetter(y, "sety", 1);
			SUtils.setValueToSetter(x, "addd", y);
			xml = "<c><d xmlns='y.int' y='1'/></c>";
			assertEq(xml, ((XComponent) x).toXml());
			x = SUtils.getNewInstance("test.xdef.component.Y16e");
			y = SUtils.getNewInstance("test.xdef.component.Y16f");
			SUtils.setValueToSetter(y, "sety", 1);
			SUtils.setValueToSetter(x, "setf", y);
			xml = "<e><f y='1'/></e>";
			assertEq(xml, ((XComponent) x).toXml());
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><b a='1'/><c/><b a='x'/></a>";
			xc = parseXC(xp, "Y17", xml, null, reporter);
			json = JsonUtil.xmlToJson(KXmlUtils.parseXml(xml)
				.getDocumentElement());
			el = xc.toXml();
			assertEq(xml, el);
			assertEq(1, SUtils.getValueFromGetter(SUtils.getValueFromGetter(xc,
				"getb_1"), "geta"));
			assertEq("x", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getb_2"), "geta"));
			if (!JsonUtil.jsonEqual(json, JsonUtil.xmlToJson(el))) {
				fail();
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A a='a' b='b'><C c='c' d='d' e='e'>f</C></A>";
			xc = parseXC(xp, "Y18", xml, null, reporter);
			assertEq("e", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getC"), "gete"));
			assertEq("f", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getC"), "getx"));
			assertEq(xml, xc.toXml());
			SUtils.setValueToSetter(
				SUtils.getValueFromGetter(xc, "getC"), "sete", "x");
			SUtils.setValueToSetter(
				SUtils.getValueFromGetter(xc, "getC"), "setx", null);
			assertEq("<A a='a' b='b'><C c='c' d='d' e='x'/></A>", xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getX"), "getb"));
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getX"), "listOfX_1");
			assertEq(2, list.size());
			assertEq("2", SUtils.getValueFromGetter(list.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(0), "listOfX_2");
			assertEq(1, list1.size());
			assertEq("3", SUtils.getValueFromGetter(list1.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(1), "listOfX_2");
			assertEq(0, list1.size());
			assertEq("4", SUtils.getValueFromGetter(list.get(1), "getb"));
			xml = "<B><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></B>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getX"), "getb"));
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getX"), "listOfX_1");
			assertEq(2, list.size());
			assertEq("2", SUtils.getValueFromGetter(list.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(0), "listOfX_1");
			assertEq(1, list1.size());
			assertEq("3", SUtils.getValueFromGetter(list1.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(1), "listOfX_1");
			assertEq(0, list1.size());
			assertEq("4", SUtils.getValueFromGetter(list.get(1), "getb"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<C><B b='1'><Y b='2'><Y b='3'/></Y><Y b='4'/></B></C>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "getb"));
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getB"), "listOfY");
			assertEq(2, list.size());
			assertEq("2", SUtils.getValueFromGetter(list.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(0), "listOfY_1");
			assertEq(1, list1.size());
			assertEq("3", SUtils.getValueFromGetter(list1.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(list.get(1), "listOfY_1");
			assertEq(0, list1.size());
			assertEq("4", SUtils.getValueFromGetter(list.get(1), "getb"));
			xml =
"<D><Z b='1'><C><Z b='2'><C><Z b='3'><C/></Z></C></Z></C></Z></D>";
			xc = parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			assertEq("1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getZ"), "getb"));
			list = (List) SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getZ"), "getC"), "listOfZ_1");
			assertEq(1, list.size());
			assertEq("2", SUtils.getValueFromGetter(list.get(0), "getb"));
			list1 = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(list.get(0), "getC_1"), "listOfZ_1");
			assertEq(1, list1.size());
			assertEq("3", SUtils.getValueFromGetter(list1.get(0), "getb"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A b='x'>z<B c='a'>x</B><B c='c'>y</B>x</A>";
			xc = parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
			SUtils.setValueToSetter(xc, "set$value", null);
			obj = SUtils.getObjectField("test.xdef.component.Y21_enum", "y");
			SUtils.setValueToSetter(xc, "setb", obj);
			list = (List) SUtils.getValueFromGetter(xc, "listOfB");
			obj = SUtils.getObjectField("test.xdef.TestXComponents_Y21enum", "b");
			SUtils.setValueToSetter(list.get(1), "setc", obj);
			assertEq("<A b='y'><B c='a'>x</B><B c='b'>y</B>x</A>", xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A Creator='DK'\n" +
"   NumBlocks='Not in X-definition'>\n" +
"  Not in X-definition: <a x='y'>x<y/></a>xxx<b/>\n" +
"  <Transfer Date='Not in X-definition'\n" +
"    Sender='0012'\n" +
"    Recipient='Not in X-definition'>\n" +
"    Not in X-definition: <a x='y'>x<y/></a>xxx<b/>\n" +
"    <DataFiles>\n" +
"      <Directory Path='q:/Ckp-2.6/VstupTest_SK/KOOP_P1_163/'>\n" +
"        <File Name='7P19998163.ctl'/>" +
"        <File Name='7P19998163A.xml'/>\n" +
"      </Directory>\n" +
"    </DataFiles>\n" +
"    Not in X-definition: <a x='y'>x<y/></a>xxx<b/>\n" +
"  </Transfer>\n" +
" Not in X-definition: <a x='y'>x<y/></a>xxx<b/>\n" +
"</A>";
			el = parse(xp, "Y22", xml , reporter);
			assertNoErrors(reporter);
			xc = parseXC(xp, "Y22", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try { // test vlaue setters/getters
			xml = "<a><s k='p'>t1</s><s k='q'>t2</s></a>";
			xc = parseXC(xp,"Y23",xml, null, reporter);
			assertEq(xml, xc.toXml());
			assertEq("p", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gets"), "getk"));
			assertEq("t1", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gets"), "get$value"));
			assertEq("q", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gets_1"), "getk"));
			assertEq("t2", SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gets_1"), "get$value"));
			xml = "<b><c>xx</c></b>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertEq(xml, xc.toXml());
			SUtils.setValueToSetter(
				SUtils.getValueFromGetter(xc, "getc"), "set$value", "yy");
			assertEq("<b><c>yy</c></b>", xc.toXml());
			xml = "<d><e>2019-04-01+02:00</e></d>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			assertEq(xml, xc.toXml());
			assertEq(new SDatetime("2019-04-01+02:00"),
				SUtils.getValueFromGetter(
					SUtils.getValueFromGetter(xc,"gete"), "get$value"));
			sd = new SDatetime("2019-04-02+02:00");
			SUtils.setValueToSetter(
				SUtils.getValueFromGetter(xc, "gete"), "set$value", sd);
			assertTrue(SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc,"gete"), "get$value").equals(sd));
			assertTrue(new SDatetime((Timestamp)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gete"), "timestampOf$value"))
				.equals(sd));
			assertTrue(new SDatetime((Calendar)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "gete"), "calendarOf$value"))
				.equals(sd));
			assertEq("<d><e>2019-04-02+02:00</e></d>", xc.toXml());
			sd = new SDatetime("2019-04-03+02:00");
			SUtils.setValueToSetter(
				SUtils.getValueFromGetter(xc, "gete"), "set$value", sd);
			assertEq(sd,
				SUtils.getValueFromGetter(
					SUtils.getValueFromGetter(xc,"gete"), "get$value"));
			assertEq("<d><e>2019-04-03+02:00</e></d>", xc.toXml());
			SUtils.setValueToSetter(SUtils.getValueFromGetter(xc, "gete"),
				"set$value", sd.getCalendar());
			assertEq(sd, SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc,"gete"), "get$value"));
			assertEq("<d><e>2019-04-03+02:00</e></d>", xc.toXml());
			xml = "<e>2019-04-01+02:00</e>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			sd = new SDatetime("2019-04-01+02:00");
			assertEq(sd, SUtils.getValueFromGetter(xc,"get$value"));
			assertEq(xml, xc.toXml());
			sd = new SDatetime("2019-04-02+02:00");
			SUtils.setValueToSetter(xc, "set$value", sd);
			assertEq(sd, SUtils.getValueFromGetter(xc,"get$value"));
			assertEq("<e>2019-04-02+02:00</e>", xc.toXml());

			xml = "<f><g>2019-04-02+02:00</g></f>";
			xc = parseXC(xp, "Y23", xml, null, reporter);
			sd = new SDatetime("2019-04-03+02:00");
			list = (List) SUtils.getValueFromGetter(xc, "listOfg");
			obj = SUtils.getNewInstance("test.xdef.component.XCf$g");
			SUtils.setValueToSetter(obj, "set$value", sd);
			SUtils.setValueToSetter(xc, "addg", obj);
			assertEq(2, list.size());
			assertEq(sd, SUtils.getValueFromGetter(list.get(1), "get$value"));
			assertEq("<f><g>2019-04-02+02:00</g><g>2019-04-03+02:00</g></f>",
				xc.toXml());
			list.clear();
			assertEq("<f/>", xc.toXml());
			obj = SUtils.getNewInstance("test.xdef.component.XCf$g");
			SUtils.setValueToSetter(obj, "set$value", sd);
			list.add(obj);
			assertEq("<f><g>2019-04-03+02:00</g></f>", xc.toXml());
			list = (List) SUtils.getValueFromGetter(xc, "listOfg");
			SUtils.setValueToSetter(list.get(0), "set$value",
				new SDatetime("2019-04-01+02:00"));
			assertEq("<f><g>2019-04-01+02:00</g></f>", xc.toXml());
			list.clear();
			assertEq("<f/>", xc.toXml());
			xml = "<a><d/></a>";
			xc = parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			obj = SUtils.getValueFromGetter(xc, "getd");
			assertTrue(obj !=null && "test.xdef.component.Y24$d".equals(
				obj.getClass().getName()));
			xml = "<c><d/></c>";
			xc = parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			obj = SUtils.getValueFromGetter(xc, "getd");
			assertTrue(obj !=null && "test.xdef.component.Y24$d".equals(
				obj.getClass().getName()));
			xml = "<Y24d Y24d='Y24d'><Y24d/></Y24d>";
			assertEq(xml, parseXC(xp,"Y24",xml,null,reporter).toXml());
			assertNoErrorwarnings(reporter);

			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"date()\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"1987-02-01\"/>\n" +
"  </Params>\n" +
"</a>";
			xc = parseXC(xp,"Y25", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			list = (List) SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(xc, "getDefParams"), "listOfParam");
			assertEq("dec()",SUtils.getValueFromGetter(list.get(1), "getType"));
			list = (List) SUtils.getValueFromGetter(xc, "listOfParams");
			list = (List) SUtils.getValueFromGetter(list.get(0), "listOfParam");
			assertEq("14.8",SUtils.getValueFromGetter(list.get(1), "getValue"));
		} catch (Exception ex) {fail(ex);}
		try { // test lexicon
			xd = xp.createXDDocument("LEX");
			xml = "<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>";
			el = xd.xtranslate(xml, "eng", "eng", reporter);
			assertNoErrors(reporter);

			xd = xp.createXDDocument("LEX");
			assertEq("<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>",
				 xd.xtranslate(xml, "eng", "ces", reporter));
			assertNoErrors(reporter);

			xd = xp.createXDDocument("LEX");
			xd.setLexiconLanguage("eng");
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);

			xd = xp.createXDDocument("LEX");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			xd.setLexiconLanguage("ces");
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);

			xd = xp.createXDDocument("LEX");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			assertEq("<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>",
				xd.xtranslate(xml, "ces", "eng", reporter));
			assertNoErrors(reporter);

			xd = xp.createXDDocument("LEX");
			assertEq("<S s=\"x\"><T t=\"1\"/><T t=\"2\"/><T t=\"3\"/></S>",
				xd.xtranslate(xml, "ces", "deu", reporter));
			assertNoErrors(reporter);

			// test lexicon with X-component
			xd = xp.createXDDocument("LEX");
			xd.setLexiconLanguage("ces");
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrors(reporter);
			assertEq("x", SUtils.getValueFromGetter(xc, "getx"));
			list = (List) SUtils.getValueFromGetter(xc, "listOfY");
			assertEq(list.size(), 3);
			assertEq(1, SUtils.getValueFromGetter(list.get(0), "gety"));
			assertEq(3, SUtils.getValueFromGetter(list.get(2), "gety"));
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			xc = parseXC(xp, "SouborD1A",
				dataDir + "TestXComponent_Z.xml", null, null);
			list = (List) SUtils.getValueFromGetter(xc, "listOfZaznamPDN");
			list1 = (List) SUtils.getValueFromGetter(
				list.get(1), "listOfVozidlo");
			assertEq(2, list1.size());
			el = xc.toXml();
			xc = parseXC(xp, "SouborD1A", el, null,null);
			list = (List) SUtils.getValueFromGetter(xc, "listOfZaznamPDN");
			list1 = (List) SUtils.getValueFromGetter(
				list.get(1), "listOfVozidlo");
			assertEq(2, list1.size());
			assertEq(xc.toXml(), el);
			assertEq("", checkXPos(xc));
		} catch (Exception ex) {fail(ex);}

		// delete temporary files.
		if (new File(getTempDir()).exists()) {
			try {
				SUtils.deleteAll(getTempDir(), true);//delete all generated data
			} catch (Exception ex) {}
		}

		resetTester();
	}

	/** Run test.
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}