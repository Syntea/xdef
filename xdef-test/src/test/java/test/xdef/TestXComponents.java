package test.xdef;

import buildtools.XDTester;
import static buildtools.XDTester.setValueToSetter;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.json.JsonUtil;

/** Test XComponents.
 * @author Vaclav Trojan
 */
public final class TestXComponents extends XDTester {

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

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		String componentDir = getSourceDir();
		try {
			ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(componentDir + "component/Pool.xp"));
			xp = (XDPool) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			xp = null;
		}
		if (xp == null) {
			try {
				componentDir = "test/test/xdef/";
				ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(componentDir + "component/Pool.xp"));
				xp = (XDPool) ois.readObject();
				ois.close();
			} catch (Exception ex) {
				fail("XDPool is not available");
				return;
			}
		}
		String xml;
		Element el;
		XDDocument xd;
		String s;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
		final String dataDir = getDataDir() + "test/";
		try {
			xml = "<A a='a' dec='123.45'><W w='wwwwwwww'/></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			xml = "<A a='a' dec='123.45'><W w='w'>wwwwwwww</W></A>";
			parseXC(xp, "A", xml, null, reporter);
			assertTrue(reporter.errors());
			xml =
"<A a='a' dec='123.45'>"+
"<W w='w'/>"+
"<W w='w1'>blabla</W>"+
"<Y>1</Y>"+
"<Y>2</Y>"+
"<i>1</i>"+
"<d>2013-09-14</d>"+
"<t>10:20:30</t>"+
"<s>Franta</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>";
			xc = parseXC(xp, "A", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			el = xc.toXml();
			assertEq("/A/@a", getValueFromGetter(xc, "xposOfa"));
			assertEq("/A/d[1]/$text", getValueFromGetter( 
				getValueFromGetter(xc, "getd"), "xposOf$value").toString());
			assertEq(getValueFromGetter(getValueFromGetter(xc, "getd"),
				"get$value").toString(), "2013-09-14");
			assertEq(getValueFromGetter(getValueFromGetter(xc, "getd"),
				"get$value"), getValueFromGetter(getValueFromGetter(xc, "getd"),
				"dateOf$value")); 
			assertEq(getValueFromGetter(getValueFromGetter(xc, "getd"),
				"get$value"), getValueFromGetter(getValueFromGetter(xc, "getd"),
				"timestampOf$value")); 
			assertEq(getValueFromGetter(getValueFromGetter(xc, "getd"),
				"get$value"), getValueFromGetter(getValueFromGetter(xc, "getd"),
				"calendarOf$value")); 
			assertEq(getValueFromGetter(getValueFromGetter(xc, "geti"),
				"get$value").toString(), "1"); 
			assertEq(getValueFromGetter(getValueFromGetter(xc, "gett"),
				"get$value").toString(), "10:20:30");
			setValueToSetter(getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(2));
			assertEq("Franta", getValueFromGetter(getValueFromGetter(xc,"gets"),
				"get$value"));
			setValueToSetter(getValueFromGetter(xc, "gets"),
				"set$value", "Pepik");
			assertTrue(BigInteger.valueOf(2).equals(getValueFromGetter(
				getValueFromGetter(xc, "geti"), "get$value")));
			assertEq("Pepik", getValueFromGetter(getValueFromGetter(xc, "gets"),
				"get$value"));
			assertEq("/A/d2[1]", getValueFromGetter(
				((List) getValueFromGetter(xc, "listOfd2")).get(0),"xGetXPos"));
			assertEq("/A/d2[2]", getValueFromGetter(
				((List) getValueFromGetter(xc, "listOfd2")).get(1),"xGetXPos"));
			assertEq(xc.toXml(),
"<A a='a' dec = '123.45'>"+
"<W w='w'/>"+
"<W w='w1'>blabla</W>"+
"<Y>1</Y>"+
"<Y>2</Y>"+
"<i>2</i>"+
"<d>2013-09-14</d>"+
"<t>10:20:30</t>"+
"<s>Pepik</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>");
			setValueToSetter(getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(3));
			SDatetime date =  new SDatetime("2013-9-1");
			setValueToSetter(getValueFromGetter(xc, "getd"),
				"set$value", date);
			SDatetime time =  new SDatetime("11:21:31");
			setValueToSetter(getValueFromGetter(xc, "gett"),
				"set$value", time);
			assertTrue(BigInteger.valueOf(3).equals(getValueFromGetter(
				getValueFromGetter(xc, "geti"), "get$value")));
			assertEq(getValueFromGetter(getValueFromGetter(xc, "getd"),
				"get$value"), date);
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/>"+
"<W w='w1'>blabla</W>"+
"<Y>1</Y>"+
"<Y>2</Y>"+
"<i>3</i>"+
"<d>2013-09-01</d>"+
"<t>11:21:31</t>"+
"<s>Pepik</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>");
			assertEq("", checkXPos(xc));
			xc = parseXC(xp, "A", el, null, null);
			el = xc.toXml();
			assertEq(el,
"<A a='a' dec = '123.45'>"+
"<W w='w'/>"+
"<W w='w1'>blabla</W>"+
"<Y>1</Y>"+
"<Y>2</Y>"+
"<i>3</i>"+
"<d>2013-09-01</d>"+
"<t>11:21:31</t>"+
"<s>Pepik</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>");
			setValueToSetter(xc, "setdec", new BigDecimal("456.01"));
			assertEq(xc.toXml(),
"<A a='a' dec='456.01'>"+
"<W w='w'/>"+
"<W w='w1'>blabla</W>"+
"<Y>1</Y>"+
"<Y>2</Y>"+
"<i>3</i>"+
"<d>2013-09-01</d>"+
"<t>11:21:31</t>"+
"<s>Pepik</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>");
			setValueToSetter(xc, "setdec", new BigDecimal("123.45"));
			assertNull(getValueFromGetter(
				((List) getValueFromGetter(xc, "listOfW")).get(0),"get$value"));
			assertEq("blabla", getValueFromGetter(
				((List) getValueFromGetter(xc, "listOfW")).get(1),"get$value"));
			((List) getValueFromGetter(xc, "listOfY")).clear();
			((List) getValueFromGetter(xc, "listOfW")).clear();
			setValueToSetter(getValueFromGetter(xc, "geti"),
				"set$value", BigInteger.valueOf(99));
			assertEq(xc.toXml(), //clone
"<A a='a' dec='123.45'>"+
"<i>99</i><d>2013-09-01</d><t>11:21:31</t><s>Pepik</s>"+
"<Z z='z'/>"+
"<d1 d='20130903113600'>20130903113601</d1>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>Thu, 15 Oct 2009 01:02:04 +0200</d2>"+
"<d2 d='Thu, 15 Oct 2009 01:02:03 +0200'>"+
"Thu, 15 Oct 2009 01:02:04 +0200"+
"</d2>"+
"</A>");
			el = XComponentUtil.toXml(xc, xp.createXDDocument("B"), "A");
			xml = "<A id='99' date='2013-09-01' time='11:21:31' name='Pepik'/>";
			assertEq(xml, el);
			try {
				setValueToSetter(xc, "setdec", new BigDecimal("456.001"));
				el = xc.toXml();
				xp.createXDDocument("A").xparse(el, null);
				fail("Error not reported" + el);
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("xpath=/A/@dec") < 0) {
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
				assertEq("z", getValueFromGetter(xc,"getz"));
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
			TestXComponents_C.class.getClass(); //force compilation
			xc = parseXC(xp, "C", xml, null, null);
			assertEq(3, ((TestXComponents_C) xc).getTest());
			assertEq(xml, xc.toXml());
			assertEq("Praha", getValueFromGetter(xc, "getName"));
			Object o = ((List) getValueFromGetter(xc, "listOfStreet")).get(0);
			assertEq("Dlouha", getValueFromGetter(o, "getName"));
			o = ((List) getValueFromGetter(o, "listOfHouse")).get(0);
			assertEq(1, getValueFromGetter(o, "getNum"));
			o = ((List) getValueFromGetter(o, "listOfPerson")).get(0);
			assertEq("Jan", getValueFromGetter(o, "getFirstName"));
			assertEq("Novak", getValueFromGetter(o, "getLastName"));
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
			assertEq(3, getValueFromGetter(xc, "getNum"));
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
			assertEq("1", getValueFromGetter(getValueFromGetter(xc, "getB"),
				"get$value"));
			assertEq("/a:A/B[1]/$text", getValueFromGetter(
				getValueFromGetter(xc, "getB"), "xposOf$value"));
			assertEq("2", getValueFromGetter(getValueFromGetter(xc, "getB"),
				"get$value1"));
			assertEq("/a:A/B[1]/$text", getValueFromGetter(
				getValueFromGetter(xc, "getB"), "xposOf$value1"));
			byte[] bytes;
			bytes = SUtils.decodeBase64("ahgkjfd01Q==");
			assertEq(bytes, getValueFromGetter(getValueFromGetter(xc, "getG"),
				"getA"));
			assertEq(2,((List) getValueFromGetter(getValueFromGetter(xc,"getG"),
				"listOfX")).size());
			bytes = SUtils.decodeBase64("bhgkjfd01Q==");
			assertEq(bytes, getValueFromGetter(
				((List) getValueFromGetter(getValueFromGetter(xc,"getG"),
				"listOfX")).get(0), "get$value"));
			bytes = SUtils.decodeBase64("dhgkjfd01Q==");
			assertEq(bytes, getValueFromGetter(
				getValueFromGetter(getValueFromGetter(xc,"getG"),
				"getY"), "get$value"));
			bytes = SUtils.decodeHex("0123456789ABCDEF");
			assertEq(bytes, getValueFromGetter(getValueFromGetter(xc,"getH"),
				"getA"));
			List list = (List) getValueFromGetter(getValueFromGetter(xc,"getH"),
				"listOfX");
			assertEq(2, list.size());
			bytes = SUtils.decodeHex("ABCDEF03456789");
			assertEq(bytes, getValueFromGetter(list.get(0), "get$value"));
			bytes = SUtils.decodeHex("89ABCDE34567");
			assertEq(bytes, getValueFromGetter(list.get(1), "get$value"));
			bytes = SUtils.decodeHex("6789");
			assertEq(bytes, getValueFromGetter(
				getValueFromGetter(getValueFromGetter(xc, "getH"),
				"getY"), "get$value"));
			assertEq("t", getValueFromGetter(getValueFromGetter(xc, "geta$T"),
				"geta$t"));
			assertTrue(getValueFromGetter(getValueFromGetter(xc, "geta$T"),
				"geta$I") != null);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<if><class try='t'/></if>";
			xc = parseXC(xp, "E", xml, null, null);
			assertEq(xc.toXml(), xml);
			assertEq("t", getValueFromGetter(getValueFromGetter(xc, "getClazz"),
				"gettry"));
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<X><A/></X>";
			xc = parseXC(xp, "F", xml, null, null);
			assertEq(xc.toXml(), xml);
			xml = "<X><B/></X>";
			Class cls = Class.forName("test.xdef.component.F");
			Constructor cns = cls.getConstructor();
			xc = (XComponent) cns.newInstance();
			setValueToSetter(xc, "setB", null);
			cls = Class.forName("test.xdef.component.F$A");
			cns = cls.getConstructor();
			setValueToSetter(xc, "setA", cns.newInstance());
			assertEq(xc.toXml(), "<X><A/></X>");			
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Y><A/><B/></Y>";
			test.xdef.component.F1 p = (test.xdef.component.F1)
				parseXC(xp, "F", xml, null, null);
			assertEq(p.toXml(), xml);
			xml = "<Y><B/><A/></Y>";
			p = (test.xdef.component.F1)
				parseXC(xp, "F", xml, null, null);
			assertEq(p.toXml(), "<Y><B/><A/></Y>");
			test.xdef.component.F1 p1 = new test.xdef.component.F1();
			p1.setA(p.getA());
			XComponentUtil.setVariable(p1, "B", p.getB());
			assertEq(p1.toXml(), "<Y><B/><A/></Y>");
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<G g='g'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>";
			xd = xp.createXDDocument("G");
			TestXComponents_G x = new TestXComponents_G();
			xd.setUserObject(x);
			test.xdef.component.G p = (test.xdef.component.G)
				parseXC(xd, xml, null, null);
			assertEq("<G g='g_'><XXX x='x'/><YYY y='y'/><YYY y='z'/></G>",
				p.toXml());
			assertEq("x", ((test.xdef.component.G.XXX) x._X).getx());
			assertEq("z", XComponentUtil.getVariable(x._Y,"y"));
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
			xc = (test.xdef.component.H) parseXC(xp, "H", xml, null, null);
			assertEq(xc.toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Ping/>";
			xc = (test.xdef.component.I) parseXC(xp, "I", xml, null, null);
			assertEq(xc.toXml(), xml);
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			test.xdef.component.J.class.getClass();
			test.xdef.component.J1.class.getClass();
			test.xdef.component.J2.class.getClass();
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
			assertEq(xml, xc.toXml()); // ? <C>a<D/>c<D/><D/>b</C> =>poradi textu
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<A>" +
"<c Kod='1' Cislo='2' Rok='14'/>" +
"<c Kod='1' Cislo='1' Rok='15'/>" +
"<c Kod='1' Cislo='3' Rok='16'/>" +
"123456" +
"<d a='23.6.2015'/>" +
"</A>";
			test.xdef.component.K p = (test.xdef.component.K)
				parseXC(xp, "K", xml, null, null);
			assertEq(3, p.listOfc().size());
			assertEq("2", p.listOfc().get(0).getCislo());
			assertEq("1", p.listOfc().get(1).getCislo());
			assertEq("3", p.listOfc().get(2).getCislo());
			assertEq("123456", "" + p.get$value());
			assertEq("2015-06-23", "" + p.getd().geta());
			assertEq(xml, p.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			test.xdef.component.L.class.getClass();
			test.xdef.component.L1.class.getClass();
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
			xc = (test.xdef.component.M)
				parseXC(xp, "M", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><Operation One='9' Two='a'/></A>";
			test.xdef.component.N p = (test.xdef.component.N)
				parseXC(xp, "N", xml, null, null);
			assertEq(xml, p.toXml());
			//force compilations
			test.xdef.component.N_Operation.class.getClass();
			test.xdef.component.N_Part.class.getClass();
			test.xdef.component.N_i r = p.getOperation();
			assertEq(9, r.getOne());
			assertEq("a", r.getTwo());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			test.xdef.component.O p = (test.xdef.component.O)
				parseXC(xp, "O", xml, null, null);
			el = p.toXml();
			assertEq(xml, el);
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
			test.xdef.component.P p = (test.xdef.component.P)
				parseXC(xp, "P", xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(p.toXml(), parse(xp.createXDDocument("P"), xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>" +
"  <a a='3.1' b='3.3.1999'/>\n"+
"  <b a='4.1' b='4.3.1999'/>\n"+
"  <b a='5.1' b='5.3.1999'/>\n"+
"  <c a='3.1' b='4.3.1999'/>\n"+
"  <d a='4.1' b='4.3.1999'/>\n"+
"</A>";
			p = (test.xdef.component.P)
				parseXC(xp, "P", xml, null, reporter);
			assertErrors(reporter);
			assertEq(p.toXml(), parse(xp.createXDDocument("P"), xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<X><A><B><E>1</E></B><C/></A><A><B/><B><E>2</E></B><C/><C/></A><A/></X>";
			test.xdef.component.X p = (test.xdef.component.X)
				parseXC(xp, "X", xml, null, null);
			assertEq(xml, p.toXml());
			assertEq(3, p.listOfA().size());
			assertEq(1, p.listOfA().get(0).listOfC().size());
			assertEq(1, p.listOfA().get(0).listOfB().size());
			p = (test.xdef.component.X)
				parseXC(xp, "X", p.toXml(), null, null);
			assertEq(3, p.listOfA().size());
			assertEq(1, p.listOfA().get(0).listOfC().size());
			assertEq(1, p.listOfA().get(0).listOfB().size());
			assertEq(xml, p.toXml());
			xml = "<Y><A V='2'/>xx<B/></Y>";
			xc = parseXC(xp, "X", xml, null, null);
			assertEq(xml, xc.toXml());
			el = xc.toXml();
			test.xdef.component.X1 p1 = (test.xdef.component.X1)
				parseXC(xp, "X", el, null, null);
			assertEq(el, p1.toXml());
			assertEq("abc", p1.getXX());
		} catch (Exception ex) {fail(ex);}
		try {
			test.xdef.component.Y02.class.getClass();
			xml = "<Test><Operation One='prvni' Two='druhy' x='X'/></Test>";
			test.xdef.component.Y01 p = (test.xdef.component.Y01)
				parseXC(xp, "Y01", xml, null, null);
			assertEq(xml, p.toXml());
			test.xdef.component.s.Y01Part x = p.getOperation();
			assertEq("prvni", x.getOne());
			x.setOne("first");
			assertEq("first", x.getOne());
			assertEq("X", (p.getOperation()).getx());
			p.getOperation().setx("Y");
			assertEq("Y", (p.getOperation()).getx());
			xml = "<Test One='prvni' Two= 'druhy'/>";
			test.xdef.component.s.Y01Part r =(test.xdef.component.s.Y01Part)
				parseXC(xp, "Y02", xml, null, null);
			assertEq(xml, r.toXml());
			assertEq("prvni", r.getOne());
		} catch (Exception ex) {fail(ex);}
		try {
			test.xdef.component.Y03.class.getClass();
			test.xdef.component.Y03PartOne.class.getClass();
			xml = "<Part><PartOne One='one'/><PartTwo One='1'/></Part>";
			test.xdef.component.Y03i p = (test.xdef.component.Y03i)
				parseXC(xp, "Y03", xml, null, null);
			assertEq("one", p.getPartOne().getOne());
			assertEq(1, p.getPartTwo().getOne());
			assertEq(xml, p.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<Part One='1' Two='Two'/>";
			test.xdef.component.Y04 p = (test.xdef.component.Y04)
				parseXC(xp, "Y04", xml, null, null);
			assertEq("One", p.getJedna());
			assertEq("Two", p.getTwo());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A One='Jedna' Two='Dva'/>";
			test.xdef.component.Y05 p = (test.xdef.component.Y05)
				parseXC(xp, "Y05a", xml, null, null);
			assertEq("Jedna", p.getOne());
			assertEq("Dva", p.getTwo());
			assertEq(xml, p.toXml());
			xml = "<B One='Jedna' Two='Dva'/>";
			p = (test.xdef.component.Y05)
				parseXC(xp, "Y05", xml, null, null);
			assertEq("Jedna", p.getOne());
			assertEq("Dva", p.getTwo());
			assertEq(xml, p.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B One='Jedna' Two='Dve'/></A>";
			TestXComponents_Y06Container<?> p = (test.xdef.component.Y06)
				parseXC(xp, "Y06", xml, null, null);
			assertEq("<B One='Jedna' Two='Dve'/>",
				((XComponent) p.getDomain()).toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A Id='123'><Domain One='Jedna' Two='Dve'/></A>";
			test.xdef.component.Y08 p = (test.xdef.component.Y08)
				parseXC(xp, "Y08", xml, null, null);
			assertEq(124, p.getIdFlow());
			p.setIdFlow(456);
			xml = "<A Id='457'><Domain One='Jedna' Two='Dve'/></A>";
			assertEq(xml, p.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A x='X' y='Y'><a b='x'><b/></a></A>";
			xc = parseXC(xp, "Y09", xml, null, null);
			assertEq(xml, xc.toXml());
			test.xdef.component.Y09 p = (test.xdef.component.Y09) xc;
			assertEq("<a b='x'><b/></a>", p.get$any().toXml());
			assertEq("/A", p.xGetXPos());
			assertEq("/A/a[1]", p.get$any().xGetXPos());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A a='A' b='B' c='XX'><a x='x'/><b x='xx'/></A>";
			xc = parseXC(xp, "Y10", xml, null, null);
			assertEq(xml, xc.toXml());
			test.xdef.component.Y10 p = (test.xdef.component.Y10) xc;
			assertEq("x", p.getp().getx());
			assertEq("xx", p.getq().getx());
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
			el = xc.toXml();
			assertEq(xml, el);
			test.xdef.component.Y11 p = (test.xdef.component.Y11) xc;
			assertEq("D", p.listOfB().get(0).listOfOp().get(0).getN());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><B><P O='oo'/><S V='s'/><Q O='q'/><P O='o'/></B></A>";
			test.xdef.component.Y12.class.getClass();
			test.xdef.component.Y12_a.class.getClass();
			test.xdef.component.Y12_B.class.getClass();
			test.xdef.component.Y12_S.class.getClass();
			xc =	parseXC(xp, "Y12", xml, null, null);
			el = xc.toXml();
			assertEq(xml, el);
			xml = "<a><b/><c/>1<b/><c/>2</a>";
			xc =	parseXC(xp, "Y12", xml, null, null);
			el = xc.toXml();
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
		try {
			String source = FUtils.readString(
				new File(componentDir, "component/Y13.java"));
			if (source.indexOf("public static class B ") > 0) {
				fail("Error Y13: "
					+ "class test.xdef.component.Y13.A.B was generated.");
			}
			xml = "<A><B a='1'/></A>";
			test.xdef.component.Y13.class.getClass();
			xc = parseXC(xp, "Y13", xml,null,null);
			assertEq(xml, xc.toXml());
			test.xdef.component.Y13 p = (test.xdef.component.Y13) xc;
			assertEq("1", ((test.xdef.component.Y13C) p.getB()).geta());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<C><X a='1'/></C>";
			xc = parseXC(xp, "Y14", xml, null, null);
			assertEq(xml, xc.toXml());
			test.xdef.component.Y14C p = (test.xdef.component.Y14C) xc;
			assertEq("1", p.getX().geta());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<a>1<b/>2</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			test.xdef.component.Y15 p;
			assertEq(xml, xc.toXml());
			p = (test.xdef.component.Y15) xc;
			assertEq("1", p.get$value());
			assertEq("2", p.get$value1());
			assertEq("/a/$text", p.xposOf$value());
			assertEq("/a/$text", p.xposOf$value1());
			assertNoErrorwarnings(reporter);
			xml = "<a>1</a>";
			xc = parseXC(xp, "Y15", xml, null, reporter);
			assertEq(xml, xc.toXml());
			p = (test.xdef.component.Y15) xc;
			assertEq("1", p.get$value());
			assertNull(p.get$value1());
			assertEq("/a/$text", p.xposOf$value());
			assertEq("/a/$text", p.xposOf$value1());
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
			test.xdef.component.Y19 p = (test.xdef.component.Y19) xc;
			assertEq(1, p.getB().getB_1().getC().getB_2().getb());
			s = p.getB().getB_1().getC().getB_2().getClass().getName();
			assertTrue(s.endsWith("B_2"), s);
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><x:b xmlns:x='x.int' y='1'/></a>";
			test.xdef.component.Y16 p = new test.xdef.component.Y16();
			test.xdef.component.Y16a r = new test.xdef.component.Y16a();
			r.sety(1);
			p.setx$b(r);
			Object json = JsonUtil.xmlToJson(KXmlUtils.parseXml(xml)
				.getDocumentElement());
			el = p.toXml();
			assertEq(xml, el);
			if (!JsonUtil.jsonEqual(json, JsonUtil.xmlToJson(el))) {
				fail();
			}
			xml = "<c><d xmlns='y.int' y='1'/></c>";
			test.xdef.component.Y16c t = new test.xdef.component.Y16c();
			test.xdef.component.Y16d u = new test.xdef.component.Y16d();
			u.sety(1);
			t.addd(u);
			assertEq(xml, t.toXml());
			xml = "<e><f y='1'/></e>";
			test.xdef.component.Y16e v = new test.xdef.component.Y16e();
			test.xdef.component.Y16f w = new test.xdef.component.Y16f();
			w.sety(1);
			v.setf(w);
			assertEq(xml, v.toXml());
		} catch (Exception ex) {fail(ex);}
		try { // construction of XComponent
			xml = "<a><b a='1'/><c/><b a='x'/></a>";
			test.xdef.component.Y17 p = (test.xdef.component.Y17)
				parseXC(xp, "Y17", xml, null, reporter);
			Object json = JsonUtil.xmlToJson(KXmlUtils.parseXml(xml)
				.getDocumentElement());
			el = p.toXml();
			assertEq(xml, el);
			assertEq(1, p.getb_1().geta());
			assertEq("x", p.getb_2().geta());
			if (!JsonUtil.jsonEqual(json, JsonUtil.xmlToJson(el))) {
				fail();
			}
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A a='a' b='b'><C c='c' d='d' e='e'>f</C></A>";
			test.xdef.component.Y18 p = (test.xdef.component.Y18)
				parseXC(xp, "Y18", xml, null, reporter);
			assertEq("e", p.getC().gete());
			assertEq("f", p.getC().getx());
			assertEq(xml, p.toXml());
			p.getC().sete("x");
			p.getC().setx(null);
			assertEq("<A a='a' b='b'><C c='c' d='d' e='x'/></A>", p.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></A>";
			test.xdef.component.Y20_A p = (test.xdef.component.Y20_A)
				parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			assertEq("1", p.getX().getb());
			java.util.List<test.xdef.component.Y20_A.X.X_1> lb =
				p.getX().listOfX_1();
			assertEq(2, lb.size());
			assertEq("2", lb.get(0).getb());
			assertEq(1, lb.get(0).listOfX_2().size());
			assertEq("3", lb.get(0).listOfX_2().get(0).getb());
			assertEq(0, lb.get(1).listOfX_2().size());
			assertEq("4", lb.get(1).getb());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<B><X b='1'><X b='2'><X b='3'/></X><X b='4'/></X></B>";
			test.xdef.component.Y20_B p = (test.xdef.component.Y20_B)
				parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			assertEq("1", p.getX().getb());
			java.util.List<test.xdef.component.Y20_B.X> lb =
				p.getX().listOfX_1();
			assertEq(2, lb.size());
			assertEq("2", lb.get(0).getb());
			assertEq(1, lb.get(0).listOfX_1().size());
			assertEq("3", lb.get(0).listOfX_1().get(0).getb());
			assertEq(0, lb.get(1).listOfX_1().size());
			assertEq("4", lb.get(1).getb());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<C><B b='1'><Y b='2'><Y b='3'/></Y><Y b='4'/></B></C>";
			assertNoErrors(reporter);
			test.xdef.component.Y20_C p = (test.xdef.component.Y20_C)
				parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			assertEq("1", p.getB().getb());
			java.util.List<test.xdef.component.Y20_C.B.Y> lb =
				p.getB().listOfY();
			assertEq(2, lb.size());
			assertEq("2", lb.get(0).getb());
			assertEq(1, lb.get(0).listOfY_1().size());
			assertEq("3", lb.get(0).listOfY_1().get(0).getb());
			assertEq(0, lb.get(1).listOfY_1().size());
			assertEq("4", lb.get(1).getb());
		} catch (Exception ex) {fail(ex);}
		try {
			xml =
"<D><Z b='1'><C><Z b='2'><C><Z b='3'><C/></Z></C></Z></C></Z></D>";
			test.xdef.component.Y20_D p = (test.xdef.component.Y20_D)
				parseXC(xp,"Y20", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			assertEq("1", p.getZ().getb());
			java.util.List<test.xdef.component.Y20_D.Z.C.Z_1> lb =
				p.getZ().getC().listOfZ_1();
			assertEq(1, lb.size());
			assertEq("2", lb.get(0).getb());
			assertEq(1, lb.get(0).getC_1().listOfZ_1().size());
			assertEq("3", lb.get(0).getC_1().listOfZ_1().get(0).getb());
		} catch (Exception ex) {fail(ex);}
		try {
			xml = "<A b='x'>z<B c='a'>x</B><B c='c'>y</B>x</A>";
			test.xdef.component.Y21 x = (test.xdef.component.Y21)
				parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, x.toXml());
			x.set$value(null);
			x.setb(test.xdef.component.Y21_enum.y);
			x.listOfB().get(1).setc(TestXComponents_Y21enum.b);
			assertEq("<A b='y'><B c='a'>x</B><B c='b'>y</B>x</A>", x.toXml());
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
			test.xdef.component.Y22 p = (test.xdef.component.Y22)
				parseXC(xp, "Y22", xml, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, p.toXml());
		} catch (Exception ex) {fail(ex);}
		try { // test vlaue setters/getters
			xml = "<a><s k='p'>t1</s><s k='q'>t2</s></a>";
			test.xdef.component.XCa a = (test.xdef.component.XCa)
				parseXC(xp,"Y23",xml,test.xdef.component.XCa.class, reporter);
			assertEq("p", a.gets().getk());
			assertEq("t1", a.gets().get$value());
			assertEq("q", a.gets_1().getk());
			assertEq("t2", a.gets_1().get$value());
			assertEq(xml, a.toXml());
			xml = "<b><c>xx</c></b>";
			test.xdef.component.XCb b = (test.xdef.component.XCb)
				parseXC(xp,"Y23",xml,test.xdef.component.XCb.class,reporter);
			assertEq(xml, b.toXml());
			b.getc().set$value("yy");
			assertEq("<b><c>yy</c></b>", b.toXml());
			xml = "<d><e>2019-04-01+02:00</e></d>";
			test.xdef.component.XCd d = (test.xdef.component.XCd)
				parseXC(xp,"Y23",xml,test.xdef.component.XCd.class,reporter);
			assertEq(xml, d.toXml());
			SDatetime sd = new SDatetime("2019-04-01+02:00");
			assertEq(sd, d.gete().get$value());
			sd = new SDatetime("2019-04-02+02:00");
			d.gete().set$value(sd);
			assertTrue(new SDatetime(d.gete().dateOf$value()).equals(sd));
			assertTrue(new SDatetime(d.gete().timestampOf$value()).equals(sd));
			assertTrue(new SDatetime(d.gete().calendarOf$value()).equals(sd));
			sd = new SDatetime("2019-04-02+02:00");
			assertEq("<d><e>2019-04-02+02:00</e></d>", d.toXml());
			sd = new SDatetime("2019-04-03+02:00");
			d.gete().set$value(sd);
			assertEq(sd, d.gete().get$value());
			assertEq("<d><e>2019-04-03+02:00</e></d>", d.toXml());
			d.gete().set$value(sd.getCalendar());
			assertEq(sd, d.gete().get$value());
			assertEq("<d><e>2019-04-03+02:00</e></d>", d.toXml());

			xml = "<e>2019-04-01+02:00</e>";
			test.xdef.component.XCe e = (test.xdef.component.XCe)
				parseXC(xp,"Y23",xml,test.xdef.component.XCe.class,reporter);
			sd = new SDatetime("2019-04-01+02:00");
			assertEq(sd, e.get$value());
			assertEq(xml, e.toXml());
			sd = new SDatetime("2019-04-02+02:00");
			e.set$value(sd);
			assertEq(sd, e.get$value());
			assertEq("<e>2019-04-02+02:00</e>", e.toXml());

			xml = "<f><g>2019-04-02+02:00</g></f>";
			test.xdef.component.XCf f = (test.xdef.component.XCf)
				parseXC(xp,"Y23",xml,test.xdef.component.XCf.class,reporter);
			sd = new SDatetime("2019-04-03+02:00");
			List<test.xdef.component.XCf.g> lst = f.listOfg();
			test.xdef.component.XCf.g g = new test.xdef.component.XCf.g();
			g.set$value(sd);
			f.addg(g);
			assertEq(2, lst.size());
			assertEq(sd, lst.get(1).get$value());
			assertEq("<f><g>2019-04-02+02:00</g><g>2019-04-03+02:00</g></f>",
				f.toXml());
			lst.clear();
			assertEq("<f/>", f.toXml());
			g = new test.xdef.component.XCf.g();
			g.set$value(sd);
			lst.add(g);
			assertEq("<f><g>2019-04-03+02:00</g></f>", f.toXml());
			f.listOfg().get(0).set$value(new SDatetime("2019-04-01+02:00"));
			assertEq("<f><g>2019-04-01+02:00</g></f>", f.toXml());
			f.listOfg().clear();
			assertEq("<f/>", f.toXml());

			xml = "<a><d/></a>";
			test.xdef.component.Y24a y24a = (test.xdef.component.Y24a)
				parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, y24a.toXml());
			assertTrue(y24a.getd()!=null && "test.xdef.component.Y24$d".equals(
				y24a.getd().getClass().getName()));
			xml = "<c><d/></c>";
			test.xdef.component.Y24 y24 = (test.xdef.component.Y24)
				parseXC(xp,"Y24", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, y24.toXml());
			assertTrue(y24.getd()!=null && "test.xdef.component.Y24$d".equals(
				y24.getd().getClass().getName()));
			xml = "<Y24d Y24d='Y24d'><Y24d/></Y24d>";
			assertEq(xml, parseXC(
				xp,"Y24",xml,test.xdef.component.Y24d.class,reporter).toXml());
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
			test.xdef.component.Y25 y25 = (test.xdef.component.Y25)
				parseXC(xp,"Y25", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, y25.toXml());
			assertEq("dec()",y25.getDefParams().listOfParam().get(1).getType());
			assertEq("14.8",
				y25.listOfParams().get(0).listOfParam().get(1).getValue());
		} catch (Exception ex) {fail(ex);}

		try { // test theaurus from generated XDPool
			xd = xp.createXDDocument("Lexicon");
			xml = "<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>";
			el = xd.xtranslate(xml, "eng", "eng", reporter);
			assertNoErrors(reporter);

			xd = xp.createXDDocument("Lexicon");
			xml = "<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>";
			el = xd.xtranslate(xml, "eng", "ces", reporter);
			assertNoErrors(reporter);

			xd = xp.createXDDocument("Lexicon");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			el = xd.xtranslate(xml, "ces", "eng", reporter);
			assertNoErrors(reporter);

			xd = xp.createXDDocument("Lexicon");
			el = xd.xtranslate(xml, "ces", "deu", reporter);
			assertNoErrors(reporter);

			xd = xp.createXDDocument("Lexicon");
			xd.setLexiconLanguage("eng");
			xml = "<X x=\"x\"><Y y=\"1\"/><Y y=\"2\"/><Y y=\"3\"/></X>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);

			xd = xp.createXDDocument("Lexicon");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			xd.setLexiconLanguage("ces");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);

			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			xd = xp.createXDDocument("Lexicon");
			xd.setLexiconLanguage("ces");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);

			// test theaurus with X-component
			xd = xp.createXDDocument("Lexicon");
			xml = "<P p=\"x\"><Q q=\"1\"/><Q q=\"2\"/><Q q=\"3\"/></P>";
			xd.setLexiconLanguage("ces");
			Class<?> clazz = test.xdef.component.Lexicon.class;
			test.xdef.component.Lexicon lx = (test.xdef.component.Lexicon)
				parseXC(xd, xml, clazz, reporter);
			assertNoErrors(reporter);
			assertEq(lx.getx(), "x");
			List<test.xdef.component.Lexicon.Y> x = lx.listOfY();
			assertEq(x.size(), 3);
			assertEq(x.get(0).gety(), 1);
			assertEq(x.get(2).gety(), 3);
			assertEq(xml, lx.toXml());
		} catch (Exception ex) {fail(ex);}
		try {
			//just force compilation
			test.xdef.component.Z1.class.getClass();
			test.xdef.component.Z2.class.getClass();
			test.xdef.component.Z3.class.getClass();
			test.xdef.component.Z4.class.getClass();
			test.xdef.component.Z5.class.getClass();
			test.xdef.component.Z6.class.getClass();
			test.xdef.component.Z7.class.getClass();
			test.xdef.component.Z8.class.getClass();
			test.xdef.component.Z9.class.getClass();
			xc = parseXC(xp, "SouborD1A",
				dataDir + "TestXComponent_Z.xml", null, null);
			assertEq(xc.getClass(), test.xdef.component.Z.class);
			test.xdef.component.Z p = (test.xdef.component.Z) xc;
			assertEq(2, p.listOfZaznamPDN().get(1).listOfVozidlo().size());
			el = p.toXml();
			p = (test.xdef.component.Z)	parseXC(xp, "SouborD1A", el, null,null);
			assertEq(2, p.listOfZaznamPDN().get(1).listOfVozidlo().size());
			assertEq(p.toXml(), el);
			assertEq("", checkXPos(p));
		} catch (Exception ex) {fail(ex);}

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