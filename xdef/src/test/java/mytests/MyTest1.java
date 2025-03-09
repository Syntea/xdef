package mytests;

import java.util.List;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTest1 extends XDTester {
	public MyTest1() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
//		System.setProperty(XConstants.DEBUG_SWITCHES,
//			XConstants.DEBUG_SHOW_XON_MODEL);
////////////////////////////////////////////////////////////////////////////////
		String s;
		String xdef;
		String xml;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		Object x, o;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////
//		System.out.println(org.xdef.sys.SDatetime.formatDate(
//			new org.xdef.sys.SDatetime(new java.util.Date())
//			.getCalendar(),"yyyy-MM-ddTHH.mm.ssZ"));
//System.out.println(org.xdef.XDConstants.BUILD_DATETIME);
//System.out.println(org.xdef.XDConstants.BUILD_VERSION);
//if(T)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class bugreports.data.JCreateX2 %link X</xd:component>\n"+
"<xd:json name = 'X'>\n"+
"[\"2 boolean()\", \"boolean()\"]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			s = "[true, false, true]";
			x = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x, xd.jparse(s, reporter)));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(XonUtils.xonToJson(x));
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(x,
				SUtils.getValueFromGetter(xc,"toXon")));
if(T)return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class test.xdef.JCreateX3 %link X</xd:component>\n"+
"<xd:json name = 'X'>\n"+
"[\"2 boolean()\", \"boolean()\"]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			s = "[true, false, true]";
			x = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x, xd.jparse(s, reporter)));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(XonUtils.xonToJson(x));
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(x,
				SUtils.getValueFromGetter(xc,"toXon")));
if(T)return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:component>%class test.xdef.JCreateX4 %link X</xd:component>\n"+
"<xd:json name = 'X'>\n"+
"[\"2 boolean()\", \"boolean()\"]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			s = "[true, false, true]";
			x = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x, xd.jparse(s, reporter)));
			assertNoErrors(reporter);
			xd = xp.createXDDocument();
			xd.setXONContext(XonUtils.xonToJson(x));
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(x,
				SUtils.getValueFromGetter(xc,"toXon")));
if(T)return;
			Object y;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:json name='A'>\n" +
"[\n" +
"  {\n" +
"    a : \"? short()\",\n" +
"    b : \"? jstring()\",\n" +
"    c : \"? double()\",\n" +
"    f : \"? boolean()\",\n" +
"    g : \"? duration()\",\n" +
"    h : \"? jnull()\",\n" +	/* null */
"    i : [],\n" +
"    Towns : [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j : \"? char()\",\n" +
"    k : \"? char()\",\n" +
"    l : \"? char()\",\n" +
"    m : \"? char()\",\n" +		/*char '\u0007' */
"    n : \"? char()\",\n" +
"    o : \"? char()\",\n" +
"    p : \"? char()\",\n" +
"    q : \"? char()\",\n" +
"    t : \"? gYear()\",\n" +
"    u : \"? gYear()\",\n" +
"    v : \"? gYear()\",\n" +
"    w : \"? gYear()\",\n" +
"    \" name with space \": \"? jstring()\"\n" +
"  },\n" +
"  \"float()\",\n" +
"  \"decimal()\",\n" +
"  \"byte()\",\n" +
"  \"integer()\",\n" +
"  \"integer()\",\n" +
"  \"date()\",\n" +
"  \"gMonth()\",\n" +
"  \"gMonth()\",\n" +
"  \"gMonthDay()\",\n" +
"  \"gMonthDay()\",\n" +
"  \"time()\",\n" +
"  \"time()\",\n" +
"  \"time()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth()\",\n" +
"  \"gYearMonth\",\n" +
"  \"dateTime()\",\n" +
"  \"gps()\",\n" +
"  \"base64Binary()\",\n" +
"  \"price()\",\n" +
"  \"price()\",\n" +
"  \"currency()\",\n" +
"  \"ipAddr()\",\n" +
"  \"ipAddr()\"\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>\n"+
"  %class mytests.Xontest %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			s =
"# Start of XON example\n" +
"[ #***** Array *****/\n" +
"  { #***** Map *****/\n" +
"    a : 1s,                          # Short\n" +
"    b : \"ab cd\",                     # String\n" +
"    c : -123.4e2d,                   # Double\n" +
"    f : true,                        # Boolean\n" +
"    g : P1Y1M1DT1H1M1.12S,           # Duration\n" +
"    h : null,                        # null\n" +
"    i : [],                          # empty array\n" +
"    Towns : [                        # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      null,\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j : c\"a\",                      # Character\n" +
"    k : c\"'\",                      # Character\n" +
"    l : c\"\\\"\",                   # Character\n" +
"    m : c\"\\u0007\",                # Character\n" +
"    n : c\"\\\\\",                   # Character\n" +
"    o : c\"\n\",                     # Character\n" +
"    p : c\"\\n\",                    # Character\n" +
"    q : c\" \",                      # Character\n" +
"    t : d0001,                       # year (without zone)\n" +
"    u : d-0001,                      # year (without zone)\n" +
"    v : d123456789Z,                 # year zone\n" +
"    w : d-0001-01:00,                # year zone\n" +
"    \" name with space \": \"x\\ty\" # name with space is quoted!\n" +
"  },  #**** end of map ****\n" +
"  -3f,                               # Float\n" +
"  -3.1d,                             # BigDecimal\n" +
"  -2b,                               # Byte\n" +
"  1N,                                # BigInteger\n" +
"  999999999999999999999999999999999, # big integer (authomatic)\n" +
"  d2021-01-11,                       # date\n" +
"  d--11,                             # month\n" +
"  d--02Z,                            # month zone\n" +
"  d--11-22,                          # month day\n" +
"  d--03-04-01:01,                    # month day zone\n" +
"  d19:23:01,                         # hours, minutes seconds\n" +
"  d19:23:01.012,                     # hours minutes seconds millis\n" +
"  d00:00:00.00001+00:00,             # time nanos zone\n" +
"  d2000-11Z,                         # year month zone\n" +
"  d2000-10-01:00,                    # year month zone\n" +
"  d2000-10,                          # year month; no zone\n" +
"  d2021-01-12T01:10:11.54012-00:01,  # date and time (nanos, zone)\n" +
"  g(-0, +1),                         # GPS\n" +
"  b(HbRBHbRBHQw=),                   # byte array (base64)\n" +
"  p(123.45 CZK),                     # price\n" +
"  p(12 USD),                         # price\n" +
"  C(USD),                            # currency\n" +
"  /129.144.52.38,                    # inetAddr (IPv4)\n" +
"  /1080:0:0:0:8:800:200C:417A       # inetAddr (IPv6)\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
			x = XonUtils.parseXON(s);
			s = XonUtils.toXonString(x, true);
			y = XonUtils.parseXON(s);
			assertTrue(XonUtils.xonEqual(x,y));
			jparse(xp, "", s, reporter);
			assertNoErrors(reporter);
			xc = xp.createXDDocument().jparseXComponent(s, null, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			o = y = xc.toXon();
			assertTrue(XonUtils.xonEqual(x,y));
			x = XonUtils.xmlToXon(xc.toXml());
			y = XonUtils.xonToJson(y);
			assertTrue(XonUtils.xonEqual(x,y));
			xd = xp.createXDDocument();
			xd.setXONContext(s);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrors(reporter);
			y = SUtils.getValueFromGetter(xc,"toXon");
			assertTrue(XonUtils.xonEqual(o,y));
		} catch (RuntimeException ex) {fail(ex);}
		reporter.clear();
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:json name='A'> [\"? int()\"] </xd:json>\n"+
"  <xd:component> %class mytests.GJint %link #A; </xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			s = "[]";
			o = XonUtils.parseXON(s);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			xd.setXONContext(s);
			xc = xd.jcreateXComponent("A", null, reporter);
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				fail(s + "\n" +  XonUtils.toXonString(x));
			}
if(T)return;
			xdef = // jcreate with create section
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:json name = 'X'>\n"+
" [ \"boolean(); create 'true'\", \"int(); create '2'\" ]\n"+
"</xd:json>\n"+
"<xd:component> %class test.xdef.JCreateX5 %link X; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			x = XonUtils.parseXON("[true, 2]");
			assertTrue(XonUtils.xonEqual(x, xd.jcreate("X", reporter)));
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument();
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
if(T)return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:json name = 'X'>\n"+
"{ a:\"int(); create '1'\",\n"+
"  b:[\n"+
"    \"boolean(); create 'true'\",\n"+
"    \"int(); create '2'\"\n"+
"  ]\n"+
"}\n"+
"</xd:json>\n"+
"<xd:component> %class test.xdef.JCreateX6 %link X; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			x = XonUtils.parseXON("{a:1, b:[true, 2]}");
			assertTrue(XonUtils.xonEqual(x, xd.jcreate("X", reporter)));
			assertNoErrorwarnings(reporter);
			xc = xd.jcreateXComponent("X", null, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='X'>\n"+
"<xd:declaration>type gam xdatetime('yyyyMMddHHmmssSSS');</xd:declaration>\n"+
"  <X a='gam()'>int()<Y xd:script='*' a='int()'/>? date()</X>\n"+
"<xd:component>%class test.xdef.CreateX6 %link X</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml =
"<X a='20201211010101333'>3<Y a='1'/><Y a='2'/>2021-12-30</X>";
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, xc.toXml());
			xd = xp.createXDDocument();
			el = xc.toXml();
			xd.setXDContext(el);
			assertEq(xml, xd.xcreate("X", reporter));
			xd = xp.createXDDocument();
			xd.setXDContext(el);
			xc = xd.xcreateXComponent(null, "X", null, null);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			o = XonUtils.parseXON(
"{ \"date\" : [[\"2020-02-22\",1,2,[],{}],\n" +
"            [\"2020-02-23\",1,2,[],{}],\n" +
"            [\"2020-02-24\",1,2,[],{}],\n" +
"            [d2021-02-25,1,2,[],{}]\n" +
"            ],\n" +
"  \"from\": [\"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+
				"xxxxxxxxxxxxxxxxxxxxx\"],\n" +
"  \"cities\" : [\n" +
"    { \"from\": [\"Brussels\",\n" +
"        {\"to\": \"London\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"from\": [\"London\",\n" +
"        {\"to\": \"Brussels\", \"distance\": 322, \"x\" : 1.1},\n" +
"        {\"to\": \"Paris\", \"distance\": 344, \"x\" : [1,2,3,4,5,[],{}]}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}");
			s = XonUtils.toXonString(o, true);
			assertTrue(XonUtils.xonEqual(o, XonUtils.parseXON(s)));
//			System.out.println(s);
			s = XonUtils.toXonString(o);
			assertTrue(XonUtils.xonEqual(o, XonUtils.parseXON(s)));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try { // test country type
			SUtils.getISO3Country("RUS");
			xdef = // country, countries
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A a=\"? country()\" b=\"? countries()\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A a='cz' b='CZE gb US CA SK RUS IT LY GE FRA SE usa IL' />";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A a=\"country()\" b=\"countries()\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A a='cz' b='cze GB us ca sk RUS lt it ge fra se usa'/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef = // test lexicon
"<xd:def xmlns:xd='" + _xdNS + "' root='Contract' name='A'>\n"+
"<Contract Number=\"num()\">\n"+
"  <Client xd:script=\"+\"\n"+
"     Typ=\"int()\"\n"+
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
"              create from('@GivenName') + ' ' + from('@LastName');\"/>\n"+
"</Agreement>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.L_Contract %link A#Contract;\n"+
"</xd:component>\n"+
"<xd:lexicon language='eng' default='yes'/>\n"+
"<xd:lexicon language='ces'>\n"+
"A#Contract =                         Smlouva\n"+
"A#Contract/@Number =                 Číslo\n"+
"A#Contract/Client =                  Klient\n"+
"A#Contract/Client/@Typ =             Role\n"+
"A#Contract/Client/@Name =            Název\n"+
"A#Contract/Client/@ID =              IČO\n"+
"A#Contract/Client/@GivenName =       Jméno\n"+
"A#Contract/Client/@LastName =        Příjmení\n"+
"A#Contract/Client/@PersonalID =      RodnéČíslo\n"+
"A#Agreement =                        Dohoda\n"+
"A#Agreement/@Date =                  Datum\n"+
"A#Agreement/@Number =                Číslo\n"+
"A#Agreement/Owner =                  Vlastník\n"+
"A#Agreement/Owner/@ID =              IČO\n"+
"A#Agreement/Owner/@Name =            Název\n"+
"A#Agreement/Holder =                 Držitel\n"+
"A#Agreement/Holder/@PID =            RČ\n"+
"A#Agreement/Holder/@GivenName =      Jméno\n"+
"A#Agreement/Holder/@LastName =       Příjmení\n"+
"A#Agreement/Mediator =               Prostředník\n"+
"A#Agreement/Mediator/@ID =           IČO\n"+
"A#Agreement/Mediator/@Name =         Název\n"+
"</xd:lexicon>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
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
			xd = xp.createXDDocument("A");
			xd.setLexiconLanguage("ces");
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Typ  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Typ       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Typ        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument("A");
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
			xd = xp.createXDDocument("A");
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Typ  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Typ       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Typ        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			xml =
"<Contract Number = \"0123456789\">\n"+
"  <Client Typ  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Typ       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Typ        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
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
//	System.out.println("=*=\n"+XonUtils.toXonString(xd.getXon(), true)+"\n=*=");
			genXComponent(xp, clearTempDir());
			xc = parseXC(xd, xml, null, reporter);
//	System.out.println("===\n" + XonUtils.toXonString(
//		org.xdef.component.xc.toXon(), true)+ "\n===");
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
			assertEq("0123456789", SUtils.getValueFromGetter(xc, "getNumber"));
			assertEq(3,
				((List<?>)SUtils.getValueFromGetter(xc,"listOfClient")).size());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"root\">\n" +
"  <xd:BNFGrammar name = \"base\">\n" +
"    S ::= [#9#10#13 ]+ /* white spaces */\n" +
"    sign ::= (\"+\"|\"-\") /* sign or unary operator */\n" +
"    number ::= [0-9]+ (\".\" [0-9]+)? ((\"E\"|\"e\") (sign)? [0-9]+)?\n" +
"    identifier ::= [a-zA-Z] [a-zA-Z0-9]*\n" +
"  </xd:BNFGrammar>\n" +
"\n" +
"  <xd:BNFGrammar name = \"expr\" extends = \"base\">\n" +
"    value ::= (sign S?)? (number|identifier | \"(\" S? expression S? \")\")\n"+
"    term ::= value (S? (\"*\" | \"/\" | \"%\") S? term)?\n" +
"    expression ::= term (S? (\"+\" | \"-\") S? expression)?\n" +
"  </xd:BNFGrammar>\n" +
"\n" +
"  <xd:declaration>\n" +
"     type expression expr.rule(\"expression\");\n" +
"  </xd:declaration>\n" +
"\n" +
"  <root name = \"base.rule('identifier')\">\n" +
"    expression;\n" +
"  </root>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xd = xp.createXDDocument();
			xml =
"<root name = \"test1\">\n" +
"  - abc + 3.14159 * - ( 2.3 - 3e-2 ) / 2 * 10e5\n" +
"</root>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
//	System.out.println(XonUtils.toXonString(xd.getXon(), true));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}