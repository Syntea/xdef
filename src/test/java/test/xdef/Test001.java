package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import org.w3c.dom.Element;

/** Class for testing (miscellaneous).
 * @author Vaclav Trojan
 */
public final class Test001  extends XDTester {

	public Test001() {super();}

	@Override
	/** Run tests and print error information. */
	public void test() {
		String xdef, xml;
		Element el;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"  <a>\n"+
"     <b attr = \"required string()\"\n"+
"        xd:script = \"occurs 0..1; finally {outln(@attr); \n"+
"                      outln('1.' + @attr); \n"+
"                      outln('2.' + (@attr).toString()); \n"+
"                      outln('3.' + toString(@attr));\n"+
"                      outln(!(@attr).exists());\n"+
"                      outln(!(@attr));\n"+
"                      outln((@attr));\n"+
"                      outln(@attr);} \" />\n"+
"  optional string(); finally {outln('text: '+getText()); setText('text1');}\n"+
"   <c xd:script=\"occurs 1;onAbsence setElement(newElement('c'))\" />\n"+
"    optional string();finally {outln('text: '+getText());setText('text2');}\n"+
"  </a>\n"+
"</xd:def>\n";
			xml =
"<a>\n"+
"  <b attr='xxxx' />\n"+
"orig1\n"+
"</a>\n";
			assertFalse(test(xdef, xml, "",'P',
				"<a><b attr='xxxx'/>text1<c/>text2</a>",
				"xxxx\n1.xxxx\n2.xxxx\n3.xxxx\nfalse\n"+
				"false\nxxxx\nxxxx\ntext: orig1\ntext: \n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Book'>\n"+
"<Book isbn = \"int(0, 999999999)\"\n"+
"      published  = \"? gYear()\"\n " +
"      xd:script = \"finally outln('isbn: ' + @isbn + '; ' + getElementText());\"> \n"+
"  string();\n"+
"  <Author xd:script=\"*\"> string() </Author>\n"+
"</Book>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Book isbn='123456789' published='2011'>\n"+
" The Crash\n"+
" <Author>John Brown</Author>\n"+
" <Author>Peter Smith</Author>\n"+
"</Book>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("isbn: 123456789; The Crash\n", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"<a>\n"+
"  <xd:choice>\n"+
"     <from xd:script=\"match(@partnerLink OR @endpointReference)\"\n"+
"           partnerLink = \"required NCName ()\"\n"+
"           endpointReference = \"required enum('myRole','partnerRole')\"/>\n"+
"     <from xd:script = \"match(@variable AND @property)\"\n"+
"           variable = \"required string()\"\n"+
"           property = \"optional NCName ()\"/>\n"+
"     <from xd:script=\"match(@variable)\"\n"+
"           variable = \"required string()\"\n"+
"           part  = \"optional NCName ()\">\n"+
"             <query xd:script = \"occurs 0..1; ref query\" />\n"+
"     </from>\n"+
"     <from xd:script=\"match(@expressionLanguage)\"\n"+
"     		 expressionLanguage = \"optional uri()\">\n"+
"             required string()\n"+
"     </from>\n"+
"     <from>\n"+
"       <xd:choice>\n"+
"         <literal>required string()</literal>\n"+
"         required string()\n"+
"       </xd:choice>\n"+
"     </from>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"<query queryLanguage=\"optional uri()\">required string()</query>\n"+
"</xd:def>\n";
			xml =
"<a><from variable='a'><query>this is query</query></from></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xml = "<a><from variable = 'a' property = 'a' ></from></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='List'>\n"+
"<xd:declaration> int $n = 0; </xd:declaration>\n"+
"<List xd:script = \"init outln('List of employes:\\n');" +
"                  finally outln('\\nNumber of employes: ' + $n)\" >\n"+
"  <Employee Name                   = \"required string(1,30)\"\n"+
"            FamilyName             = \"required string(1,30)\"\n"+
"            Ingoing                = \"required xdatetime('yyyy-M-d')\"\n"+
"            Salary                 = \"required int(1000,100000)\"\n"+
"            Qualification          = \"required string()\"\n"+
"            SecondaryQualification = \"optional string()\"\n"+
"            xd:script            = \"occurs 0..;" +
"                 finally outln(++$n + '. ' + @Name + ' ' + @FamilyName " +
"                  + ' ' + @Qualification + (@SecondaryQualification ?" +
"                  ' (' + @SecondaryQualification + ')' : ' '))\" />\n"+
"</List>\n"+
"</xd:def>\n";
			xml =
"<List>\n"+
"  <Employee Name                   = \"John\"\n"+
"            FamilyName             = \"Braun\"\n"+
"            Ingoing                = \"2004-10-1\"\n"+
"            Salary                 = \"2000\"\n"+
"            Qualification          = \"worker\" />\n"+
"  <Employee Name                   = \"Mary\"\n"+
"            FamilyName             = \"White\"\n"+
"            Ingoing                = \"1998-9-3\"\n"+
"            Salary                 = \"1234\"\n"+
"            Qualification          = \"carpenter\" />\n"+
"  <Employee Name                   = \"Peter\"\n"+
"            FamilyName             = \"Black\"\n"+
"            Ingoing                = \"1998-2-13\"\n"+
"            Salary                 = \"1600\"\n"+
"            Qualification          = \"carpenter\"\n"+
"            SecondaryQualification = \"electrician\" />\n"+
"</List>\n";
			assertFalse(test(xdef, xml, "",'P',
"<List>" +
	"<Employee Name='John' Qualification='worker'" +
	" Ingoing='2004-10-1' Salary='2000' FamilyName='Braun'/>" +
	"<Employee Name='Mary' Qualification='carpenter'" +
	" Ingoing='1998-9-3' Salary='1234' FamilyName='White'/>" +
	"<Employee Name='Peter' Qualification='carpenter'" +
	" Ingoing='1998-2-13' Salary='1600'" +
	" SecondaryQualification='electrician' FamilyName='Black'/>" +
	"</List>",
"List of employes:\n\n"+
	"1. John Braun worker \n"+
	"2. Mary White carpenter \n"+
	"3. Peter Black carpenter (electrician)\n\n"+
	"Number of employes: 3\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Contract'>\n"+
"<xd:declaration>\n"+
"   int ii = 3*5, jj = 2*ii, kk = 2*jj; \n"+
"   boolean $checkId(){\n"+
"      String s = getText();\n"+
"      if (!string(10,11))\n"+
"         return error('Incorrect length of PID');\n"+
"      if (s.substring(6,7) != '/')\n"+
"         return error('Missing slash character');\n"+
"      if (!isNumeric(s.cut(5)))\n"+
"         return error('Second part is not numeric');\n"+
"      if (!isNumeric(s.substring(7)))\n"+
"         return error('First part is not numeric');\n"+
"      return true;\n"+
"   }\n"+
"</xd:declaration>\n"+
"<dummy>\n"+
"<Contract cId=\"required num(10)\" xd:script=\"create from('/Contract')\" >\n"+
"  <Owner Title     = \"required string(1,30); create from('@title')\"\n"+
"         IC        = \"required num(8); create from('@ic')\"\n"+
"         xd:script = \"occurs 1; create from('Client[@role=\\'1\\']')\"/>\n"+
"  <Holder Name = \"required string(1,30); create from('@name')\"\n"+
"          FamilyName =\"required string(1,30); create from('@familyname')\"\n"+
"          PersonalId = \"required $checkId(); create from('@pid')\"\n"+
"          xd:script = \"occurs 1; create from('Client[@role=\\'2\\']')\"/>\n"+
"  <Policyholder Title = \"required string(1,30);\n"+
"                  create toString(from('@name'))+' '+from('@familyname')\"\n"+
"     IC    = \"required num(8); create from('@ic')\"\n"+
"     xd:script=\"occurs 1; create from('Client[@role=\\'3\\']')\"/>\n"+
"</Contract>\n"+
"</dummy>\n"+
"<Contract cId = \"required num(10)\"\n"+
"          xd:script = \"finally {if (errors() == 0) {" +
"            setElement(xpath('/Contract/dummy/Contract', xcreate('dummy')));" +
"            outln('OK');} else outln('Found ' + errors() + ' errors');" +
"            } \" >\n"+
"  <Client xd:script = \"occurs 1..\"\n"+
"          role = \"required int()\"\n"+
"          typ  = \"required string()\"\n"+
"          typid  = \"optional int()\"\n"+
"          title = \"optional string()\"\n"+
"          name = \"optional string()\"\n"+
"          familyname = \"optional string()\"\n"+
"          pid = \"optional $checkId();\"\n"+
"          ic = \"optional num(8);\" />\n"+
"</Contract>\n"+
"</xd:def>";
			xml =
"<Contract cId = \"0123456789\">\n"+
"  <Client role = \"1\"\n"+
"          typ = \"P\"\n"+
"          title = \"Firma XYZ Ltd\"\n"+
"          ic = \"12345678\" />\n"+
"  <Client role = \"2\"\n"+
"          typ = \"O\"\n"+
"          typid = \"1\"\n"+
"          name = \"Jan\"\n"+
"          familyname = \"Novak\"\n"+
"          pid = \"311270/1234\" />\n"+
"  <Client role = \"3\"\n"+
"          typ = \"O\"\n"+
"          typid = \"2\"\n"+
"          name = \"Jan\"\n"+
"          familyname = \"Novak\"\n"+
"          pid = \"311270/1234\"\n"+
"          ic = \"87654321\" />\n"+
"</Contract>";
			assertFalse(test(xdef, xml, "",'P',
				"<Contract cId='0123456789'>" +
					"<Owner IC='12345678' Title='Firma XYZ Ltd'/>" +
					"<Holder Name='Jan' PersonalId='311270/1234'" +
					" FamilyName='Novak'/>" +
					"<Policyholder IC='87654321' Title='Jan Novak'/>" +
					"</Contract>",
				"OK\n"));

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='GenContract'>\n"+
"<Contract cId = \"required num(10)\" >\n"+
"  <Policyholder\n"+
"     xd:script=\"create from('/Contract/Client[@role=\\'3\\']')\"\n"+
"     IC=\"required num(8); create from('@ic')\"\n"+
"         Title=\"required string(1,30);/*create section is string*/\n"+
"                create from('@name') + ' ' + from('@familyname');\" />\n"+
"  <Owner Title = \"required string(1,30); create from('@title')\"\n"+
"    IC=\"required num(8); create from('@ic')\"\n"+
"    xd:script=\"create from('Client[@role=\\'1\\']')\"/>\n"+
"  <Holder Name=\"required string(1,30); create from('@name')\"\n"+
"    FamilyName= \"required string(1,30); create from('@familyname')\"\n"+
"    PersonalId= \"required string(10,11); create from('@pid')\"\n"+
"    xd:script = \"create from('Client[@role=\\'2\\']')\"/>\n"+
"</Contract>\n"+
"</xd:def>";
			xml =
"<Contract cId   = \"0123456789\">\n"+
"  <Client role  = \"1\"\n"+
"          typ   = \"P\"\n"+
"          title = \"Company X Ltd\"\n"+
"          ic    = \"12345678\" />\n"+
"  <Client role  = \"2\"\n"+
"          typ   = \"O\"\n"+
"          typid = \"1\"\n"+
"          name  = \"John\"\n"+
"          familyname = \"Brown\"\n"+
"          pid   = \"120456/432\" />\n"+
"  <Client role  = \"3\"\n"+
"          typ   = \"O\"\n"+
"          typid = \"2\"\n"+
"          name  = \"Michael\"\n"+
"          familyname = \"Grey\"\n"+
"          pid   = \"311270/1234\"\n"+
"          ic    = \"87654321\" />\n"+
"</Contract>";
			assertFalse(test(xdef, xml, "GenContract", 'C',
				"<Contract cId='0123456789'>" +
				"<Policyholder IC='87654321' Title='Michael Grey'/>" +
				"<Owner IC='12345678' Title='Company X Ltd'/>" +
				"<Holder Name='John' FamilyName='Brown'" +
				" PersonalId='120456/432'/>" +
				"</Contract>",
				""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='GenContract'>\n"+
"<Contract cId='required num(10)'>\n"+
"<Policyholder xd:script=\"create from('/Contract/Client[@role=\\'3\\']')\">\n"+
"  <IC xd:script = \"create newElement()\">create from('@ic')</IC>\n"+
"</Policyholder>\n"+
"</Contract>\n"+
"</xd:def>";
			xml =
"<Contract cId   = \"0123456789\">\n"+
"  <Client role  = \"1\"\n"+
"          typ   = \"P\"\n"+
"          title = \"Company X Ltd\"\n"+
"          ic    = \"12345678\" />\n"+
"  <Client role  = \"2\"\n"+
"          typ   = \"O\"\n"+
"          typid = \"1\"\n"+
"          name  = \"John\"\n"+
"          familyname = \"Brown\"\n"+
"          pid   = \"120456/432\" />\n"+
"  <Client role  = \"3\"\n"+
"          typ   = \"O\"\n"+
"          typid = \"2\"\n"+
"          name  = \"Michael\"\n"+
"          familyname = \"Grey\"\n"+
"          pid   = \"311270/1234\"\n"+
"          ic    = \"87654321\" />\n"+
"</Contract>\n";
			assertFalse(test(xdef, xml, "GenContract", 'C',
				"<Contract cId='0123456789'>" +
					"<Policyholder><IC>87654321</IC></Policyholder>" +
					"</Contract>",
				""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Contract'>\n"+
"<Contract cId = \"required num(10); create '0123456789'\">\n"+
"  <Holder IC    = \"required num(8); create '87654321'\"\n"+
"          Title = \"required string(1,30); create 'Michael Grey';\" >\n"+
"  required string(); create 'holder OK'\n"+
"  </Holder>\n"+
"</Contract>\n"+
"</xd:def>\n";
			xml = null;
			assertFalse(test(xdef, xml, "Contract", 'C',
				"<Contract cId='0123456789'>" +
					"<Holder IC='87654321' Title='Michael Grey'>" +
					"holder OK</Holder>" +
					"</Contract>",
				""));

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test'\n"+
"      xd:root=\"DavkaA|ZaznamB\" >\n"+
"<DavkaA>\n"+
"  <ZaznamA xd:script=\"occurs 1..;\n"+
"           finally setElement(xcreate('ZaznamB')); forget\"\n"+
"           attrA=\"required\" >\n"+
"    <ChildA1 xd:script=\"occurs 1..\"\n"+
"             ChildA1Attr=\"required\" >\n"+
"    </ChildA1>\n"+
"    <ChildA2 xd:script=\"occurs 0..1\"\n"+
"             ChildA2Attr=\"required\" >\n"+
"      required\n"+
"    </ChildA2>\n"+
"  </ZaznamA>\n"+
"</DavkaA>\n"+
"\n"+
"  <ZaznamB xd:script=\"create from('/DavkaA/ZaznamA');\n"+
"                      finally out(getElement());\"\n"+
"           attrB=\"required; create from('@attrA');\">\n"+
"    <ChildB1 xd:script=\"occurs 1..; create from('ChildA1');\" \n"+
"             ChildB1Attr=\"required; create from('@ChildA1Attr');\" >\n"+
"    </ChildB1>\n"+
"    <ChildB2 xd:script=\"occurs 0..1; create from('ChildA2');\"\n"+
"             ChildB2Attr=\"required; create from('@ChildA2Attr')\" >\n"+
"      required\n"+
"    </ChildB2>\n"+
"  </ZaznamB>\n"+
"</xd:def>";
			xml =
"<DavkaA >\n"+
"  <ZaznamA attrA=\"aaa1\">\n"+
"    <ChildA1 ChildA1Attr=\"1 ChildA1 1\">\n"+
"    </ChildA1>\n"+
"    <ChildA1 ChildA1Attr=\"1 ChildA1 2\">\n"+
"    </ChildA1>\n"+
"    <ChildA2 ChildA2Attr=\"1 ChildA2 1\" >\n"+
"      text 1\n"+
"    </ChildA2>\n"+
"  </ZaznamA>\n"+
"  <ZaznamA attrA=\"aaa2\">\n"+
"    <ChildA1 ChildA1Attr=\"2 1\">\n"+
"    </ChildA1>\n"+
"  </ZaznamA>\n"+
"</DavkaA>\n";
			assertFalse(test(xdef, xml, "test",'P', "<DavkaA/>",
"<ZaznamB attrB=\"aaa1\"><ChildB1 ChildB1Attr=\"1 ChildA1 1\"/>" +
	"<ChildB1 ChildB1Attr=\"1 ChildA1 2\"/>" +
	"<ChildB2 ChildB2Attr=\"1 ChildA2 1\">text 1</ChildB2>" +
	"</ZaznamB><ZaznamB attrB=\"aaa2\">" +
	"<ChildB1 ChildB1Attr=\"2 1\"/>" +
"</ZaznamB>"));

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='aaa'>\n"+
"<aaa>\n"+
"  <bbb attr = \"ignore\"\n"+
"    xd:script = \"occurs 0..3\" />\n"+
"  required string(1,9999); onTrue setText('OK1');\n"+
"                          onAbsence setText('missing1');\n"+
"     <ccc xd:script = \"occurs 0..1\" />\n"+
"      optional string(); onTrue setText('wasstring')\n"+
"     <ddd xd:script = \"occurs 1; onAbsence setElement(xcreate('ddd'))\" />\n"+
"</aaa>\n"+
"<ddd xx=\"required; create 'xxx'\" xd:script = \"create newElement()\" >\n"+
"<x xd:script = \"create newElement()\" />\n"+
"</ddd>\n"+
"</xd:def>\n";
			xml =
"<aaa>\n"+
"  <bbb attr=\"12345\" />\n"+
"  <bbb attr=\"?\" />\n"+
"  <bbb />\n"+
"tex1\n"+
"  <ccc/>\n"+
"</aaa>\n";
			assertFalse(test(xdef, xml, "", 'P',
			"<aaa><bbb/><bbb/><bbb/>OK1<ccc/><ddd xx='xxx'><x/></ddd></aaa>",
			""));

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Weather'>\n"+
"<xd:declaration> float $sum = 0; int $num = 0; </xd:declaration>" +
"<html>\n"+
"  <body>\n"+
"    <h1>\n"+
"      required string(); create 'Date: ' + from('/Weather/@date')\n"+
"    </h1>\n"+
"    <li xd:script=\"occurs 1..; create from('/Weather/Measurement')\">\n"+
"    required string(); create {$num++;\n"+
"             $sum += parseFloat(toString(from('@temperature')));" +
"outln('sum=' + $sum + ', num=' + $num);\n"+
"return 'Time: ' + toString(from('@time'))\n"+
"       + ', wind: ' + toString(from('@wind'))\n"+
"       + ', temperature: ' + toString(from('@temperature'));}\n"+
"     </li>\n"+
"<p>" +
"   required; create 'Average temprerature: ' + toString(((float)$sum)/$num)\n"+
"</p>" +
"  </body>\n"+
"</html>\n"+
"\n"+
"<Weather xd:script=\"finally setElement(xcreate('html'))\"\n"+
"     date=\"optional string()\">\n"+
"     <Measurement wind=\"required string()\"\n"+
"                  temperature=\"required string()\"\n"+
"                  time=\"required string()\"\n"+
"                  xd:script=\"occurs 1..\" />\n"+
"</Weather>\n"+
"</xd:def>";
			xml =
"<Weather date=\"2005-05-11\" >\n"+
"<Measurement wind=\"5\" temperature=\"13.2\" time=\"05:00\" />\n"+
"<Measurement wind=\"7\" temperature=\"15.0\" time=\"11:00\" />\n"+
"<Measurement wind=\"8\" temperature=\"17.8\" time=\"15:00\" />\n"+
"<Measurement wind=\"3\" temperature=\"16.0\" time=\"20:00\" />\n"+
"</Weather>\n";
			assertFalse(test(xdef, xml, "", 'P',
"<html><body>" +
	"<h1>Date: 2005-05-11</h1>" +
	"<li>Time: 05:00, wind: 5, temperature: 13.2</li>" +
	"<li>Time: 11:00, wind: 7, temperature: 15.0</li>" +
	"<li>Time: 15:00, wind: 8, temperature: 17.8</li>" +
	"<li>Time: 20:00, wind: 3, temperature: 16.0</li>" +
	"<p>Average temprerature: 15.5</p>" +
"</body></html>",
"sum=13.2, num=1\nsum=28.2, num=2\nsum=46.0, num=3\nsum=62.0, num=4\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Weather'>\n"+
"<xd:declaration> float $sum = 0; int $num = 0; </xd:declaration>" +
"\n"+
"<html>\n"+
"  <body>\n"+
"     <h1>\n"+
"        required string(); create 'Date: ' + from('/Weather/@date')\n"+
"     </h1>\n"+
"     optional string(); create { \n"+
"       Container c = from('/Weather/Measurement');\n"+
"       for (int i = 0; i LT c.getLength(); i++) {\n"+
"         Element e = c.getElement(i);\n"+
"			out(' ' + xpath('@time', e));\n"+
"       }\n"+
"		  outln();\n"+
"       return '';\n"+
"     }\n"+
"     <li xd:script=\"occurs 1..; create from('/Weather/Measurement')\">\n"+
"        required string();\n"+
"        create {$num++; $sum += parseFloat(toString(@temperature));" +
"          outln('sum=' + $sum + ', num=' + $num);\n"+
"          return 'Time: ' + toString(from('@time'))\n"+
"            + ', wind: ' + toString(from('@wind'))\n"+
"            + ', temperature: ' + toString(from('@temperature'));}\n"+
"     </li>\n"+
"<p>" +
"   required; create 'Average temprerature: ' + toString(((float)$sum)/$num)\n"+
"</p>" +
"  </body>\n"+
"</html>\n"+
"\n"+
"<Weather xd:script=\"finally setElement(xcreate('html'))\"\n"+
"     date=\"optional xdatetime('y-M-d')\">\n"+
"     <Measurement xd:script='occurs 1..' wind=\"required float()\"\n"+
"       temperature=\"required float(-30.0, +50.0)\"\n"+
"       time=\"required xdatetime('HH:mm[:ss]')\"/>\n"+
"</Weather>\n"+
"\n"+
"</xd:def>";
			xml =
"<Weather date=\"2005-05-11\" >\n"+
"<Measurement wind=\"5\" temperature=\"13.2\" time=\"05:00\" />\n"+
"<Measurement wind=\"7\" temperature=\"15.0\" time=\"11:00\" />\n"+
"<Measurement wind=\"8\" temperature=\"17.8\" time=\"15:00\" />\n"+
"<Measurement wind=\"3\" temperature=\"16.0\" time=\"20:00\" />\n"+
"</Weather>\n";
			assertFalse(test(xdef, xml, "", 'P',
"<html><body><h1>Date: 2005-05-11</h1>" +
	"<li>Time: 05:00, wind: 5, temperature: 13.2</li>" +
	"<li>Time: 11:00, wind: 7, temperature: 15.0</li>" +
	"<li>Time: 15:00, wind: 8, temperature: 17.8</li>" +
	"<li>Time: 20:00, wind: 3, temperature: 16.0</li>" +
	"<p>Average temprerature: 15.5</p></body></html>",
" 05:00 11:00 15:00 20:00\n"+
"sum=13.2, num=1\nsum=28.2, num=2\nsum=46.0, num=3\nsum=62.0, num=4\n"));

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='aaa'>\n"+
"  <aaa>\n"+
"     <bbb xd:script='occurs 0..3' attr='ignore'/>\n"+
"       required string(1,9999); onTrue setText('OK1');\n"+
"            onAbsence setText('missing1');\n"+
"     <ccc xd:script='occurs 0..1'/>\n"+
"       optional string(); onTrue setText('wasstring')\n"+
"     <ddd xd:script='occurs 1; ref ddd'/>\n"+
"</aaa>\n"+
"<ddd xx=\"fixed 'xxx';\"\n"+
"     xd:script=\"onAbsence setElement(newElement('ddd'))\">\n"+
"  <x xd:script='onAbsence setElement(newElement()); create newElement()'/>\n"+
"</ddd>\n"+
"</xd:def>\n";
			xml =
"<aaa>\n"+
"  <bbb attr=\"12345\" />\n"+
"  <bbb attr=\"?\" />\n"+
"  <bbb />\n"+
"tex1\n"+
"  <ccc/>\n"+
"tex2\n"+
"</aaa>\n";
			assertFalse(test(xdef, xml, "test", 'P',
				"<aaa><bbb/><bbb/><bbb/>OK1<ccc/>wasstring<ddd xx='xxx'>" +
				"<x/></ddd></aaa>", ""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test'>\n"+
"<xd:declaration>int $n = 0;</xd:declaration>\n"+
"<test>\n"+
"  <a n='required int(); create ++$n' xd:script='*; create newElements(3)'>\n"+
"    required string(); create $n\n"+
"  </a>\n"+
"  <b n='required int(); create ++$n' xd:script='*;create newElements(0)'>\n"+
"    required string(); create $n\n"+
"  </b>\n"+
"  <c xd:script='occurs 2; create newElements(5)'/>\n"+
"</test>\n"+
"</xd:def>\n";
			xml = null;
			assertFalse(test(xdef, xml, "test", 'C',
			"<test><a n='1'>1</a><a n='2'>2</a><a n='3'>3</a><c/><c/></test>",
			""));
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def root='aaa'>\n"+
"<aaa>\n"+
"  <bbb attr = \"required string()/*xx*/\"\n"+
"    xd:script=\"occurs 0..1; finally outln(@attr)\" />\n"+
"  optional string();\n"+
"    finally {outln('text: ' + getText()); setText('text1');}\n"+
"  <ccc xd:script=\"occurs 1; onAbsence setElement(newElement('ccc'))\"/>\n"+
"  optional string();\n"+
"    finally {outln('text: '+getText()); setText('text2');\n"+
"    hanoi(3); if ((test != 1) | (k != 1)) outln('error');}\n"+
"</aaa>\n"+
"</xd:def>\n"+
"<xd:def name='y'>\n"+
"<xd:declaration>\n"+
"  int test = 1;\n"+
"  int i = 0, q = i;\n"+
"  int j = i;\n"+
"  void hanoi(int v) {\n"+
"    outln('v=' + v + ':');\n"+
"    move(v, 1, 2, 3);\n"+
"    if ((test != 1) | (j != 0)) outln('error');\n"+
"  }\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'x'>\n"+
"<xd:declaration>\n"+
"  int x;\n"+
"  int y;\n"+
"  int z;\n"+
"  void move(int v, int o, int k, int h) {\n"+
"    if (v GT 0) {\n"+
"      move(v -1, o, h, k);\n"+
"      outln(o + '->' + k);\n"+
"      move(v -1, h, k, o);\n"+
"    }\n"+
"    if ((test != 1) | (j != 0)) outln('error');\n"+
"    for(int i=1, j=1; i LT j; i++);\n"+
"  }\n"+
"  int r;\n"+
"  int s = 1, f = r = 1;\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def xd:name  = 'z'>\n"+
"<xd:declaration>\n"+
"  int k = 1;\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xml =
"<aaa>\n"+
"  <bbb attr='xxxx' />\n"+
"orig1\n"+
"</aaa>\n";
			assertFalse(test(xdef, xml, "",'P',
				"<aaa><bbb attr='xxxx'/>text1<ccc/>text2</aaa>",
				"xxxx\ntext: orig1\ntext: \nv=3:\n"+
				"1->2\n1->3\n2->3\n1->2\n3->1\n3->2\n1->2\n"));

			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'\n"+
"   script='options ignoreEmptyAttributes'>\n"+
"<a x:script = \"ref b\">\n"+
"  <p/>\n"+
"  <q/>\n"+
"  optional int(); default 456\n"+
"</a>\n"+
"<b attr=\"optional an(); default 'a123x'\">\n"+
"  <c/>\n"+
"</b>\n"+
"</x:def>\n";
			xml = "<a><c/><p/><q/></a>";
			assertFalse(test(xdef, xml, "",'P',
				"<a attr='a123x'><c/><p/><q/>456</a>", ""));
			xml = "<a attr='b123c'><c/><p/><q/>123</a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'\n"+
"       script='options ignoreEmptyAttributes'>\n"+
"<a x:script = \"ref b\">\n"+
"  <p/>\n"+
"  <q/>\n"+
"  optional int(); default 456\n"+
"  <r/>\n"+
"</a>\n"+
"<b attr=\"optional an(); default 'a123x'\">\n"+
"  <c/>\n"+
"</b>\n"+
"</x:def>\n";
			xml = "<a><c/><p/><q/><r/></a>";
			assertFalse(test(xdef, xml, "",'P',
				"<a attr='a123x'><c/><p/><q/>456<r/></a>", ""));
			xml = "<a attr='b123c'><c/><p/><q/>123<r/></a>";
			assertFalse(test(xdef, xml, "",'P',
				"<a attr='b123c'><c/><p/><q/>123<r/></a>", ""));
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a:aaa'\n"+
"  xmlns:a='nsa' xmlns:b='nsb' xmlns:c='nsc'>\n"+
"<a:aaa>\n"+
"  <b:bbb c:attr='required string()' xmlns:b='nsb' xmlns:c='nsc'\n"+
"    xd:script = \"occurs 0..1; finally outln(@c:attr)\" />\n"+
"  optional string(); finally {outln('text: '+getText()); setText('text1');}\n"+
"  <ccc xd:script = \"occurs 1; onAbsence setElement(newElement('ccc'))\" />\n"+
"  optional string(); finally {outln('text: '+getText()); setText('text2');}\n"+
"</a:aaa>\n"+
"</xd:def>";
			xml =
"<c:aaa xmlns:c='nsa'>\n"+
"  <d:bbb xmlns:d='nsb' xmlns:e='nsc' e:attr='xxxx'/>\n"+
"  orig1\n"+
"</c:aaa>";
			assertFalse(test(xdef, xml, "", 'P',
				"<c:aaa xmlns:c='nsa'>" +
					"<d:bbb xmlns:d = 'nsb' xmlns:e='nsc' e:attr='xxxx'/>" +
					"text1<ccc/>text2</c:aaa>",
				"xxxx\ntext: orig1\ntext: \n"));
			xml =
"<c:aaa xmlns:c='nsa' >\n"+
"  <d:bbb xmlns:d='nsb' xmlns:e='nsc' e:attr='xxxx' />\n"+
"orig1\n"+
"  <ccc/>\n"+
"orig2\n"+
"</c:aaa>";
			assertFalse(test(xdef, xml, "", 'P',
				"<c:aaa xmlns:c='nsa'>" +
				"<d:bbb xmlns:d = 'nsb' xmlns:e='nsc' e:attr='xxxx'/>" +
				"text1<ccc/>text2</c:aaa>",
				"xxxx\ntext: orig1\ntext: orig2\n"));

			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"	<xd:def xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"		impl-version='0.0.1' impl-date='18.9.2006'\n"+
"		name='XDefSOAP' root='s:Envelope'>\n"+
"		<s:Envelope xd:script=\"occurs 1\" encodingStyle=\"required\">\n"+
"			<s:Header xd:script=\"occurs 0..1\">\n"+
"				<xd:any xd:script=\"occurs 0..;\n"+
"                  options moreAttributes, moreElements, moreText\" />\n"+
"			</s:Header>\n"+
"			<s:Body xd:script=\"occurs 1\">\n"+
"				<xd:any xd:script=\"occurs 0..;\n"+
"                   options moreAttributes, moreElements, moreText\" />\n"+
"			</s:Body>\n"+
"		</s:Envelope>\n"+
"	</xd:def>\n"+
"</xd:collection>\n";
		xml =
"<s:Envelope xmlns=\"http://ws.ckp.cz/\"\n"+
"		xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"		xmlns:p=\"http://www.p\"\n"+
"		encodingStyle=\"http://pis.ckp.cz/soap/encoding\">\n"+
"	<s:Header>\n"+
"		<asd/>\n"+
"	</s:Header>\n"+
"	<s:Body>\n"+
"		<p:NajdiPojistitele xmlns:p=\"http://pis.ckp.cz/soap/ps\">\n"+
"			<p:SPZ>1A22334</p:SPZ>\n"+
"			<p:Datum>2.6.2006</p:Datum>\n"+
"		</p:NajdiPojistitele>\n"+
"	</s:Body>\n"+
"</s:Envelope>\n";
			el = parse(xdef, "XDefSOAP", xml, reporter);
			el = removeNs(el);
			checkResult(el,
"<Envelope encodingStyle=\"http://pis.ckp.cz/soap/encoding\">" +
  "<Header><asd/></Header>" +
  "<Body>" +
	"<NajdiPojistitele>" +
	  "<SPZ>1A22334</SPZ>" +
	  "<Datum>2.6.2006</Datum>" +
	"</NajdiPojistitele>" +
  "</Body>" +
"</Envelope>");
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"	<xd:def xmlns:s='http://www.w3c.org/2003/05/soap-envelope'\n"+
"		impl-version='0.0.1' impl-date='18.9.2006'\n"+
"		name='XDefSOAP' root='s:Envelope'>\n"+
"		<s:Envelope xd:script=\"occurs 1\" encodingStyle=\"required\">\n"+
"			<s:Header xd:script=\"occurs 0..1\">\n"+
"				<xd:any xd:script=\"occurs 0..;\n"+
"					options moreAttributes, moreElements, moreText\" />\n"+
"			</s:Header>\n"+
"			<s:Body xd:script=\"occurs 1\">\n"+
"				<xd:any xd:script=\"occurs 0..;\n"+
"                   options moreAttributes, moreElements, moreText\" />\n"+
"			</s:Body>\n"+
"		</s:Envelope>\n"+
"	</xd:def>\n"+
"</xd:collection>\n";
			xml =
"<s:Envelope xmlns=\"http://ws.ckp.cz/\"\n"+
"    xmlns:s=\"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"    encodingStyle=\"http://pis.ckp.cz/soap/encoding\">\n"+
"  <s:Header>\n"+
"    <asd p:at = \"at\"/>\n"+
"  </s:Header>\n"+
"  <s:Body>\n"+
"    <p:NajdiPojistitele xmlns:p=\"http://pis.ckp.cz/soap/ps\">\n"+
"      <p:SPZ>1A22334</p:SPZ>\n"+
"        <p:Datum>2.6.2006</p:Datum>\n"+
"    </p:NajdiPojistitele>\n"+
"  </s:Body>\n"+
"</s:Envelope>\n";
			Report r;
			try {
				parse(xdef, "XDefSOAP", xml, reporter);
				if (reporter.errors()) {
					//Unknown NameSpace for qualified name '&{0}'
					assertTrue("XML047".equals((r=reporter.getReport()).getMsgID())
						&& r.getModification().startsWith("&{0}p:at"),r.toString());
					while ((r = reporter.getReport()) != null) {
						if (!"XML080".equals(r.getMsgID())) {
							fail("Unexpected: " + r + "; "+r.getModification());
						}
					}
				} else {
					fail("No error reported");
				}
			} catch (Exception ex) {
				assertTrue("XML047".equals((r=reporter.getReport()).getMsgID())
					&& r.getModification().startsWith("&{0}p:at"),r.toString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'root'>\n"+
"<r>\n"+
"  <xd:choice occurs = '?'>\n"+
"    <A/>\n"+
"    <B/>\n"+
"  </xd:choice>\n"+
"</r>\n"+
"<root xd:script='ref r' />\n"+
"</xd:def>\n";
			xml = "<root></root>";
			assertFalse(test(xdef, xml, "",'P'));
			xml = "<root><A/></root>";
			assertFalse(test(xdef, xml, "",'P'));
//test recusive references in groups
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'Expr'>\n"+
"<V>required int(); finally outln(getText());</V>\n"+
"<S>\n"+
"  <xd:choice>\n"+
"    <V xd:script = 'ref V'/>\n"+
"    <Expr xd:script = 'ref E' />\n"+
"  </xd:choice>\n"+
"  <xd:choice occurs = '+' >\n"+
"    <ADD xd:script = 'occurs ?; ref SOP; finally outln(\"+\")'/>\n"+
"    <SUB xd:script = 'occurs ?; ref SOP; finally outln(\"-\")'/>\n"+
"  </xd:choice>\n"+
"</S>\n"+
"<SOP>\n"+
"  <xd:choice>\n"+
"    <S xd:script = 'ref S'/>\n"+
"    <V xd:script = 'ref V' />\n"+
"    <Expr xd:script = 'ref E' />\n"+
"  </xd:choice>\n"+
"</SOP>\n"+
"<M>\n"+
"  <xd:choice>\n"+
"    <S xd:script = 'ref S'/>\n"+
"    <V xd:script = 'ref V'/>\n"+
"    <Expr xd:script = 'ref E' />\n"+
"  </xd:choice>\n"+
"  <xd:choice occurs = '+' >\n"+
"    <MUL xd:script = 'ref MOP; finally outln(\"*\")'/>\n"+
"    <DIV xd:script = 'ref MOP; finally outln(\"/\")'/>\n"+
"  </xd:choice>\n"+
"</M>\n"+
"<MOP>\n"+
 " <xd:choice>\n"+
"    <S xd:script = 'ref S'/>\n"+
"    <V xd:script = 'ref V'/>\n"+
"    <M xd:script = 'ref M'/>\n"+
"    <Expr xd:script = 'ref E' />\n"+
"  </xd:choice>\n"+
"</MOP>\n"+
"<E>\n"+
"  <xd:choice>\n"+
"    <V xd:script = 'ref V' />\n"+
"    <S xd:script = 'ref S' />\n"+
"    <M xd:script = 'ref M' />\n"+
"    <Expr xd:script = 'ref E' />\n"+
"  </xd:choice>\n"+
"</E>\n"+
"<Expr xd:script = 'ref E; finally outln(\"=\")'/>\n"+
"</xd:def>\n";
			xml = "<Expr><S><V>1</V><ADD><V>2</V></ADD></S></Expr>";
			assertFalse(test(xdef, xml, "",'P', xml, "1\n2\n+\n=\n"));
			xml = "<Expr><V>1</V></Expr>";
			assertFalse(test(xdef, xml, "",'P', xml, "1\n=\n"));
			xml = "<Expr><Expr><V>1</V></Expr></Expr>";
			assertFalse(test(xdef, xml, "",'P', xml, "1\n=\n"));
			xml = "<Expr><S><V>3</V><SUB><V>2</V></SUB>" +
					"<ADD><V>1</V></ADD></S></Expr>";
			assertFalse(test(xdef, xml, "",'P', xml, "3\n2\n-\n1\n+\n=\n"));
			xml = "<Expr><M><S><V>2</V><ADD><V>3</V></ADD></S><MUL>" +
				"<V>4</V></MUL></M></Expr>";
			assertFalse(test(xdef, xml, "",'P', xml, "2\n3\n+\n4\n*\n=\n"));
			xml =
				"<Expr><M><Expr><S><V>2</V><ADD><V>3</V></ADD></S></Expr><MUL>"+
				"<V>4</V></MUL></M></Expr>";
			assertFalse(test(xdef, xml, "", 'P', xml, "2\n3\n+\n4\n*\n=\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        xd:script='options ignoreEmptyAttributes'>\n"+
"<a xd:script='ref b'>\n"+
"  <xd:mixed>\n"+
"    <xd:list xd:ref='dummy1'/>\n"+
"  </xd:mixed>\n"+
"  required\n"+
"  <xd:list ref='dummy2'/>\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"<xd:list name='dummy1'>" +
"  <xd:mixed>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:list>" +
"<xd:list name='dummy2'>" +
"  <xd:choice>\n"+
"    <r/>\n"+
"    <s/>\n"+
"  </xd:choice>\n"+
"</xd:list>" +
"</xd:def>";
			xml = "<a attr='attr'><c/><p/><q/>text<s/></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        xd:script='options ignoreEmptyAttributes'>\n"+
"<a xd:script='ref b'>\n"+
"  <xd:list xd:ref='dummy1' />\n"+
"  required\n"+
"  <xd:list ref='dummy2' />\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"<xd:list name='dummy1'>" +
"  <xd:mixed>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:list>" +
"<xd:list name='dummy2'>" +
"  <xd:choice>\n"+
"    <r/>\n"+
"    <s/>\n"+
"  </xd:choice>\n"+
"</xd:list>" +
"</xd:def>";
			xml = "<a attr='attr'><c/><p/><q/>text<s/></a>";
			assertFalse(test(xdef, xml, "",'P'));
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a' script='options ignoreEmptyAttributes' >\n"+
"<a>\n"+
"  <xd:mixed>\n"+
"    <p xd:script='occurs 1..'/>\n"+
"    <xd:sequence>\n"+
"      <q xd:script='occurs 1'/>\n"+
"      required int()\n"+
"      <q xd:script='occurs 1'/>\n"+
"    </xd:sequence>\n"+
"    <r xd:script='occurs 0..'/>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			assertFalse(test(xdef, "<a><p/><q/>123<q/><p/></a>", "",'P'));
			assertFalse(test(xdef, "<a><p/><q/>123<q/><p/><r/></a>", "",'P'));
			assertFalse(test(xdef, "<a><q/>123<q/><p/><r/></a>", "",'P'));
			assertFalse(test(xdef, "<a><q/>123<q/><r/><p/></a>", "",'P'));
			assertFalse(test(xdef, "<a><r/><q/>123<q/><p/></a>", "",'P'));
			assertFalse(test(xdef,"<a><r/><p/><q/>123<q/><p/><r/></a>","",'P'));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:a='a' root='a:aaa'>\n"+
"<a:aaa>\n"+
"  <a:bbb attr='required string()'\n"+
"    xd:script='occurs 0..1; finally outln(@attr)'/>\n"+
"  optional string();finally {outln('text: '+getText());setText('text1');}\n"+
"</a:aaa>\n"+
"</xd:def>";
			xml = "<a:aaa xmlns:a='a'><a:bbb attr='xxxx' />orig1</a:aaa>\n";
			assertFalse(test(xdef, xml, "",'P',
				"<a:aaa xmlns:a='a'><a:bbb attr='xxxx'/>text1</a:aaa>",
				"xxxx\ntext: orig1\n"));
			xml = "<b:aaa xmlns:b='a'><b:bbb attr='xxxx' />orig1</b:aaa>\n";
			assertFalse(test(xdef, xml, "",'P',
				"<b:aaa xmlns:b='a'><b:bbb attr='xxxx'/>text1</b:aaa>",
				"xxxx\ntext: orig1\n"));
			xml = "<aaa xmlns='a'><bbb attr='xxxx'/>orig1</aaa>\n";
			assertFalse(test(xdef, xml, "",'P',
				"<aaa xmlns='a'><bbb attr='xxxx'/>text1</aaa>",
				"xxxx\ntext: orig1\n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:a='a' root='a:aaa'>\n"+
"<a:aaa>\n"+
"  <a:bbb attr='required string()'\n"+
"    xd:script='occurs 0..1; finally outln(@attr)'/>\n"+
"  optional string(); finally {outln('text: '+getText()); setText('text1');}\n"+
"  <a:ccc xd:script=\"occurs 1;onAbsence setElement(newElement('a:ccc'))\"/>\n"+
"  optional string(); finally{outln('text: '+getText()); setText('text2');}\n"+
"</a:aaa>\n"+
"</xd:def>";
			xml = "<a:aaa xmlns:a='a'><a:bbb attr='xxxx' />orig1</a:aaa>\n";
			assertFalse(test(xdef, xml, "",'P',
			"<a:aaa xmlns:a='a'><a:bbb attr='xxxx'/>text1<a:ccc/>text2</a:aaa>",
				"xxxx\ntext: orig1\ntext: \n"));
			xml = "<b:aaa xmlns:b='a'><b:bbb attr='xxxx' />orig1</b:aaa>\n";
			test(xdef, xml, "",'P',
				"<b:aaa xmlns:b='a'><b:bbb attr='xxxx'/>text1" +
				"<a:ccc xmlns:a='a'/>" +
				"text2</b:aaa>",
				"xxxx\ntext: orig1\ntext: \n");
			xml = "<aaa xmlns='a'><bbb attr='xxxx' />orig1</aaa>\n";
			test(xdef, xml, "",'P',
				"<aaa xmlns='a'><bbb attr='xxxx'/>text1" +
				"<a:ccc xmlns:a='a'/>" +
				"text2</aaa>",
				"xxxx\ntext: orig1\ntext: \n");
			xdef = //test setValidateAttrNames in Simpledom
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a attr='optional'>\n"+
"  <b attr='ignore' xd:script='occurs 0..3'/>\n"+
"  required string(1,9);onTrue setText('OK1');onAbsence setText('missing1');\n"+
"  <c xd:script = \"occurs 0..1\" />\n"+
"  ignore\n"+
"</a>\n"+
"</xd:def>\n";
			xml =
"<a><b attr='12345'/><b attr='?'/><b/>tex1<c/>tex2</a>";
			if (test(xdef, xml, "", 'P', "<a><b/><b/><b/>OK1<c/></a>", "")) {
				fail();
			} else {
				assertEq("<a><b/><b/><b/>OK1<c/></a>",
					test(xdef, xml, "", null, reporter, 'P', null));
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Misto|Udaj|Base'>\n"+
"<xd:declaration>\n"+
"external Element base; /*V tomto elementu je nase databaze. */\n"+
"\n"+
"/*Pridame do databaze novy element “Misto”pokud jeste neexistuje*/\n"+
"void addMisto(String misto) {\n"+
"  if (xpath('Misto[@name=\"' + misto + '\"]', base).getLength() == 0) {\n"+
"    Element e = new Element('Misto');\n"+
"    e.setAttribute('name', misto);\n"+
"    base.addElement(e);\n"+ //10
"  } else {\n"+
"     outln(misto + ' already defined!');\n"+
"  }\n"+
"}\n"+
"\n"+
"/*Pridame do databaze do prislusneho elementu Misto novy element “Udaj”.\n"+
"   Pokud misto neexistuje nebo udaj jiz existuje , ohlasime chybu.*/\n"+
"void addUdaj(String misto, String od) {\n"+
"   Element e = getElement();\n"+
"   Element e1 =\n"+//20
"     xpath('Misto[@name=\"' + misto + '\"]', base).getElement(0);\n"+
"   if (e1 == null) {\n"+
"     outln(misto + ' not defined!'); return;\n"+
"   }\n"+
"   if (xpath('Udaj[@od=\"' + od + '\"]', e1).getLength() != 0) {\n"+
"     outln(misto + '/' + od + ' already exists!'); return;\n"+
"   }\n"+
"   Element e2 = new Element('Udaj');\n"+
"   e2.setAttribute('hodnota', e.getAttribute('hodnota'));\n"+
"   e2.setAttribute('od', e.getAttribute('od'));\n"+//30
"   e2.setAttribute('do', e.getAttribute('do'));\n"+
"   e1.addElement(e2);\n"+
"}\n"+
"</xd:declaration>\n"+
"\n"+
"<Misto xd:script='finally addMisto((String) @name)'\n"+
"   name='string; /*update Base/misto*/'/>\n"+
"<Udaj xd:script='finally addUdaj((String) @misto, (String) @od)'\n"+
"   misto='string'\n"+
"   hodnota='float'\n"+//40
"   od='dateTime'\n"+
"   do='dateTime'/>\n"+
"\n"+
"<Base>\n"+
"  <Misto xd:script='*;'\n"+
"    name='ID'>\n"+
"    <Udaj xd:script='*'\n"+
"      hodnota='float'\n"+
"      od='dateTime'\n"+
"      do='dateTime'/>\n"+//50
"  </Misto>\n"+
"</Base>\n"+
"</xd:def>";
			xp = compile(xdef);
			Element base =
				KXmlUtils.newDocument(null, "Base", null).getDocumentElement();
			xml = "<Misto name='A'/>";
			xd = xp.createXDDocument();
			xd.setVariable("base", base);
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xml = "<Misto name='B'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml =
"<Udaj misto='A' hodnota='1.5'\n"+
"  od='2012-10-01T10:00:00'\n"+
"  do='2012-10-01T11:00:00'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml =
"<Udaj misto='A'\n"+
"  hodnota='0.45'\n"+
"  od='2012-10-02T09:30:00'\n"+
"  do='2012-10-02T10:45:00'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml =
"<Udaj misto='B'\n"+
"  hodnota='9'\n"+
"  od='2012-10-01T10:30:00'\n"+
"  do='2012-10-01T10:35:00'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml = "<Misto name='A'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml =
"<Udaj misto='A'\n"+
"  hodnota='0.45'\n"+
"  od='2012-10-02T09:30:00'\n"+
"  do='2012-10-02T10:45:00'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xml = "<Misto name='C'/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			reporter.clear();
			assertEq(xd.xparse(base, reporter),
"<Base>"+
"<Misto name='A'>"+
	"<Udaj hodnota='1.5' od='2012-10-01T10:00:00' do='2012-10-01T11:00:00'/>"+
	"<Udaj hodnota='0.45' od='2012-10-02T09:30:00' do='2012-10-02T10:45:00'/>"+
"</Misto>"+
"<Misto name='B'>"+
	"<Udaj hodnota='9' od='2012-10-01T10:30:00' do='2012-10-01T10:35:00'/>"+
"</Misto>"+
"<Misto name='C'/>"+
"</Base>");
			assertNoErrors(reporter);
			assertEq("A already defined!\n"+
				"A/2012-10-02T09:30:00 already exists!\n", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			// check compiling if source items have assignment of sourceId
			Object[] p1 = new Object[] {
"<xd:def xmlns:xd='" + _xdNS + "' root='A' name='A'><A/></xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' root='B' name='B'><B/></xd:def>",
			new ByteArrayInputStream((
"<xd:def xmlns:xd='" + _xdNS + "' root='C' name='C'><C/></xd:def>")
				.getBytes("UTF-8"))
			};
			String[] p2 = new String[] {"AA", "AB", "AC"};
			xp = XDFactory.compileXD(null, p1, p2);
			xml = "<A/>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrors(reporter);
			xml = "<B/>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrors(reporter);
			xml = "<C/>";
			assertEq(xml, parse(xp, "C", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			// check compiling if source items have assignment of sourceId
			Object[] p1 = new Object[] {
"<xd:def xmlns:xd='" + _xdNS + "' root='A' name='A'><A a='x'/></xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' root='B' name='B'><B a='x'/></xd:def>",
			new ByteArrayInputStream((
"<xd:def xmlns:xd='" + _xdNS + "' root='C' name='C'><C a='x'/></xd:def>")
				.getBytes("UTF-8"))
			};
			String[] p2 = new String[] {"AA", "AB", "AC"};
			XDFactory.compileXD(null, p1, p2);
		} catch (Exception ex) {
			String s = ex.getMessage();
			if (!s.contains("AA") || !s.contains("AB") || !s.contains("AC")) {
				fail(ex); // not present "AA" or "AB" or "AC"
			}
		}

		resetTester();
	}

	private static Element removeNs(final Element e) {
		return KXmlUtils.setAllNSToNull(e);
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}