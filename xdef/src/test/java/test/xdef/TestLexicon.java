package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.FUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;

/** Test of Lexicon.
 * @author Vaclav Trojan
 */
public final class TestLexicon extends XDTester {

	public TestLexicon() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String tempDir = getTempDir();
		try {
			if (new File(tempDir).exists()) {
				FUtils.deleteAll(tempDir, true);
			} else {
				fail("Temporary direcitory is not available");
				return;
			}
		} catch (Exception ex) {
			fail(ex);
			return;
		}
		String xdef;
		String xml;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp;
		XDDocument xd;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Contract' name='kontrakt'>\n"+
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
"              create toString(from('@GivenName'))+' '+from('@LastName');\"/>\n"+
"</Agreement>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.L_Contract %link kontrakt#Contract;\n"+
"</xd:component>\n"+
"</xd:def>";
			String lexicon1 =
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='eng'>\n"+
"kontrakt#Contract =                         Contract\n"+
"kontrakt#Contract/@Number =                 Number\n"+
"kontrakt#Contract/Client =                  Client\n"+
"kontrakt#Contract/Client/@Typ =             Typ\n"+
"kontrakt#Contract/Client/@Name =            Name\n"+
"kontrakt#Contract/Client/@ID =              ID\n"+
"kontrakt#Contract/Client/@GivenName =       GivenName\n"+
"kontrakt#Contract/Client/@LastName =        LastName\n"+
"kontrakt#Contract/Client/@PersonalID =      PersonalID\n"+
"kontrakt#Agreement =                        Agreement\n"+
"kontrakt#Agreement/@Date =                  Date\n"+
"kontrakt#Agreement/@Number =                Number\n"+
"kontrakt#Agreement/Owner =                  Owner\n"+
"kontrakt#Agreement/Owner/@ID =              ID\n"+
"kontrakt#Agreement/Owner/@Name =            Name\n"+
"kontrakt#Agreement/Holder =                 Holder\n"+
"kontrakt#Agreement/Holder/@PID =            PID\n"+
"kontrakt#Agreement/Holder/@GivenName =      GivenName\n"+
"kontrakt#Agreement/Holder/@LastName =       LastName\n"+
"kontrakt#Agreement/Mediator =               Mediator\n"+
"kontrakt#Agreement/Mediator/@ID =           ID\n"+
"kontrakt#Agreement/Mediator/@Name =         Name\n"+
"</xd:lexicon>";
			String lexicon2 =
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='ces'>\n"+
"kontrakt#Contract =                         Smlouva\n"+
"kontrakt#Contract/@Number =                 Číslo\n"+
"kontrakt#Contract/Client =                  Klient\n"+
"kontrakt#Contract/Client/@Typ =             Role\n"+
"kontrakt#Contract/Client/@Name =            Název\n"+
"kontrakt#Contract/Client/@ID =              IČO\n"+
"kontrakt#Contract/Client/@GivenName =       Jméno\n"+
"kontrakt#Contract/Client/@LastName =        Příjmení\n"+
"kontrakt#Contract/Client/@PersonalID =      RodnéČíslo\n"+
"kontrakt#Agreement =                        Dohoda\n"+
"kontrakt#Agreement/@Date =                  Datum\n"+
"kontrakt#Agreement/@Number =                Číslo\n"+
"kontrakt#Agreement/Owner =                  Vlastník\n"+
"kontrakt#Agreement/Owner/@ID =              IČO\n"+
"kontrakt#Agreement/Owner/@Name =            Název\n"+
"kontrakt#Agreement/Holder =                 Držitel\n"+
"kontrakt#Agreement/Holder/@PID =            RČ\n"+
"kontrakt#Agreement/Holder/@GivenName =      Jméno\n"+
"kontrakt#Agreement/Holder/@LastName =       Příjmení\n"+
"kontrakt#Agreement/Mediator =               Prostředník\n"+
"kontrakt#Agreement/Mediator/@ID =           IČO\n"+
"kontrakt#Agreement/Mediator/@Name =         Název\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("kontrakt");
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
			xd = xp.createXDDocument("kontrakt");
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
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("kontrakt");
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

			lexicon1 =
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='eng' default='yes'>\n"+
"/* this is just a comment */\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("kontrakt");
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

			lexicon1 =
"<xd:lexicon xmlns:xd='" + _xdNS + "' language='eng' default='yes'>\n"+
"</xd:lexicon>";
			xp = compile(new String[]{xdef, lexicon1, lexicon2});
			xd = xp.createXDDocument("kontrakt");
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
			// try X-component
			String xdir = tempDir + "components";
			File fdir = new File(xdir);
			fdir.mkdirs();
			if (fdir.exists() && !fdir.isDirectory()) {
				//Directory doesn't exist or isn't accessible: &{0}
				throw new SRuntimeException(SYS.SYS025, fdir.getAbsolutePath());
			}
			if (fdir.exists()) { // ensure the src directory exists.
				SUtils.deleteAll(fdir, true); // clear this directory
			}
			fdir.mkdirs();
			genXComponent(xp, fdir);
			Class<?> clazz = Class.forName("test.xdef.component.L_Contract");
			XComponent xc = parseXC(xd, xml, clazz, reporter);
			assertNoErrors(reporter);
			el = xc.toXml();
			assertEq(xml, el);
			Method m = clazz.getDeclaredMethod("getNumber");
			assertEq("0123456789", m.invoke(xc));
			m = clazz.getDeclaredMethod("listOfClient");
			List<?> list = (List<?>) m.invoke(xc);
			assertEq(3, list.size());
		} catch (Exception ex) {fail(ex);}
		try {
			if (new File(tempDir).exists()) {
				FUtils.deleteAll(tempDir, true);
			}
		} catch (Exception ex) {
			fail(ex);
		}

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