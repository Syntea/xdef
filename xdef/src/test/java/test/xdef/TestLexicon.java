package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.component.XComponentUtil;

/** Test of Lexicon.
 * @author Vaclav Trojan
 */
public final class TestLexicon extends XDTester {

	public TestLexicon() {super();}

	/** Run test and print error information. */
	@Override
	public void test() {
		String xdef;
		String xml;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp;
		XDDocument xd;
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Contract' name='contract'>\n"+
"\n"+
"<Contract Number=\"num()\">\n"+
"  <Client xd:script=\"+\"\n"+
"     Typ=\"int()\"\n"+
"     Name=\"? string\"\n"+
"     ID=\"? num()\"\n"+
"     GivenName=\"? string\"\n"+
"     LastName=\"? string\"\n"+
"     PersonalID=\"? string\" />\n"+
"</Contract>\n"+
"\n"+
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
"  %class "+_package+".component.L_Contract %link contract#Contract;\n"+
"</xd:component>\n"+
"</xd:def>";
			String lexicon1 =
"<xd:lexicon xmlns:xd='"+_xdNS+"' language='eng'>\n"+
"contract#Contract =                         Contract\n"+
"contract#Contract/@Number =                 Number\n"+
"contract#Contract/Client =                  Client\n"+
"contract#Contract/Client/@Typ =             Typ\n"+
"contract#Contract/Client/@Name =            Name\n"+
"contract#Contract/Client/@ID =              ID\n"+
"contract#Contract/Client/@GivenName =       GivenName\n"+
"contract#Contract/Client/@LastName =        LastName\n"+
"contract#Contract/Client/@PersonalID =      PersonalID\n"+
"contract#Agreement =                        Agreement\n"+
"contract#Agreement/@Date =                  Date\n"+
"contract#Agreement/@Number =                Number\n"+
"contract#Agreement/Owner =                  Owner\n"+
"contract#Agreement/Owner/@ID =              ID\n"+
"contract#Agreement/Owner/@Name =            Name\n"+
"contract#Agreement/Holder =                 Holder\n"+
"contract#Agreement/Holder/@PID =            PID\n"+
"contract#Agreement/Holder/@GivenName =      GivenName\n"+
"contract#Agreement/Holder/@LastName =       LastName\n"+
"contract#Agreement/Mediator =               Mediator\n"+
"contract#Agreement/Mediator/@ID =           ID\n"+
"contract#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>";
			String lexicon2 =
"<xd:lexicon xmlns:xd='"+_xdNS+"' language='ces'>\n"+
"contract#Contract =                         Smlouva\n"+
"contract#Contract/@Number =                 Číslo\n"+
"contract#Contract/Client =                  Klient\n"+
"contract#Contract/Client/@Typ =             Role\n"+
"contract#Contract/Client/@Name =            Název\n"+
"contract#Contract/Client/@ID =              IČO\n"+
"contract#Contract/Client/@GivenName =       Jméno\n"+
"contract#Contract/Client/@LastName =        Příjmení\n"+
"contract#Contract/Client/@PersonalID =      RodnéČíslo\n"+
"contract#Agreement =                        Dohoda\n"+
"contract#Agreement/@Date =                  Datum\n"+
"contract#Agreement/@Number =                Číslo\n"+
"contract#Agreement/Owner =                  Vlastník\n"+
"contract#Agreement/Owner/@ID =              IČO\n"+
"contract#Agreement/Owner/@Name =            Název\n"+
"contract#Agreement/Holder =                 Držitel\n"+
"contract#Agreement/Holder/@PID =            RČ\n"+
"contract#Agreement/Holder/@GivenName =      Jméno\n"+
"contract#Agreement/Holder/@LastName =       Příjmení\n"+
"contract#Agreement/Mediator =               Prostředník\n"+
"contract#Agreement/Mediator/@ID =           IČO\n"+
"contract#Agreement/Mediator/@Name =         Název\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("contract");
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument("contract");
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("contract");
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			lexicon1 =
"<xd:lexicon xmlns:xd='"+_xdNS+"' language='eng' default='yes'>\n"+
"/* this is just a comment */\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("contract");
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			lexicon1 =
"<xd:lexicon xmlns:xd='"+_xdNS+"' language='eng' default='yes'>\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("contract");
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
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
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			// try X-component
			genXComponent(xp); // create and compile X-components
			Class<?> clazz = Class.forName("test.xdef.component.L_Contract");
			XComponent xc = parseXC(xd, xml, clazz, reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertNoErrorwarnings(reporter);
			el = xc.toXml();
			assertEq(xml, el);
			assertEq("0123456789", XComponentUtil.get(xc,"Number"));
			List l = (List) XComponentUtil.getx(xc, "listOfClient");
			assertEq(3, l.size());
		} catch (ClassNotFoundException | RuntimeException ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Town' name='town'>\n"+
"  <Town Name=\"string\">\n" +
"     <Street xd:script=\"*;\" Name=\"string\">\n" +
"        <House xd:script=\"*; ref _house\"/>\n" +
"     </Street>\n" +
"  </Town>\n" +
"  <_house Num=\"int\" Address=\"optional string;\">\n" +
"     <Person xd:script=\"*; ref _person\" />\n" +
"  </_house>\n" +
"  <_person FirstName=\"string\" LastName=\"string();\"/>\n" +
"  <xd:lexicon language=\"eng\" default=\"yes\"/>\n" +
"  <xd:lexicon language=\"deu\">\n" +
"    town#Town =                 Stadt\n" +
"    town#Town/@Name =           Name\n" +
"    town#Town/Street =          Straße\n" +
"    town#Town/Street/@Name =    Name\n" +
"    town#Town/Street/House =    Haus\n" +
"    town#_house/@Num =          Nummer\n" +
"    town#_house/@Address =      Adresse\n" +
"    town#_house/Person =        Person\n" +
"    town#_person/@FirstName =   Vorname\n" +
"    town#_person/@LastName =    Nachname\n" +
"  </xd:lexicon>\n" +
"\n" +
"  <xd:lexicon language=\"ces\">\n" +
"    town#Town =                 Město\n" +
"    town#Town/@Name =           Jméno\n" +
"    town#Town/Street =          Ulice\n" +
"    town#Town/Street/@Name =    Jméno\n" +
"    town#Town/Street/House =    Dům\n" +
"    town#_house/@Num =          Číslo\n" +
"    town#_house/@Address =      Adresa\n" +
"    town#_house/Person =        Osoba\n" +
"    town#_person/@FirstName =   Jméno\n" +
"    town#_person/@LastName =    Příjmení\n" +
"  </xd:lexicon>\n" +
"  <xd:component>\n"+
"    %class "+_package+".component.Town %link town#Town;\n"+
"  </xd:component>\n"+
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument("town");
			String xml_eng =
"<Town Name='Nonehill'>\n" +
"  <Street Name='Long'>\n" +
"    <House Num='1'>\n" +
"      <Person FirstName='John' LastName='Smith'></Person>\n" +
"      <Person FirstName='Jane' LastName='Smith'></Person>\n" +
"    </House>\n" +
"    <House Num='2'/>\n" +
"    <House Num='3'>\n" +
"      <Person FirstName='James' LastName='Smith'></Person>\n" +
"    </House>\n" +
"  </Street>\n" +
"  <Street Name='Short'>\n" +
"    <House Num='1'>\n" +
"      <Person FirstName='Jeremy' LastName='Smith'></Person>\n" +
"    </House>\n" +
"  </Street>\n" +
"</Town>";
			xd.setLexiconLanguage("eng");
			el = parse(xd, xml_eng, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml_eng, el);
			xd = xp.createXDDocument("town");
			xd.setLexiconLanguage("deu");
			String xml_deu =
"<Stadt Name='Nonehill'>\n" +
"  <Straße Name='Long'>\n" +
"    <Haus Nummer='1'>\n" +
"      <Person Vorname='John' Nachname='Smith'></Person>\n" +
"      <Person Vorname='Jane' Nachname='Smith'></Person>\n" +
"    </Haus>\n" +
"    <Haus Nummer='2'/>\n" +
"    <Haus Nummer='3'>\n" +
"      <Person Vorname='James' Nachname='Smith'></Person>\n" +
"    </Haus>\n" +
"  </Straße>\n" +
"  <Straße Name='Short'>\n" +
"    <Haus Nummer='1'>\n" +
"      <Person Vorname='Jeremy' Nachname='Smith'></Person>\n" +
"    </Haus>\n" +
"  </Straße>\n" +
"</Stadt>";
			el = parse(xd, xml_deu, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml_deu, el);
			reporter.clear();
			assertEq(xml_eng, xd.xtranslate(xml_deu, "deu", "eng", reporter));
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("town");
			xd.setLexiconLanguage("deu");
			XComponent xc = parseXC(xd, xml_deu, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("", chkCompoinentSerializable(xc));
			assertEq(xml_deu, xc.toXml());
			assertEq("Nonehill", XComponentUtil.get(xc,"Name"));
		} catch (RuntimeException ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}