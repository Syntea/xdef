package bugreports;

import java.io.File;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import org.xdef.sys.ArrayReporter;

public class CharacterTest {

	static String xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"SuborP1A\" name=\"Example1\">\n" +
"  <SuborP1A xd:script=\"options moreAttributes,moreText\">\n" +
"	<xd:any xd:script=\"1..; options moreElements,moreAttributes,moreText; forget\" />\n" +
"  </SuborP1A>\n" +
"</xd:def>";

	static String xdef2 =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"SuborP1A\" name=\"Example1\">\n" +
"  <xd:declaration scope='local'>\n" +
"	   external method {void bugreports.CharacterTest.setErr(XXData);}\n" +
"  </xd:declaration>\n" +
"  <SuborP1A xd:script=\"options moreElements, moreAttributes\">\n" +
"	<NovaZml xd:script=\"options moreElements, moreAttributes\">\n" +
"		<Poistnik xd:script=\"options moreElements, moreAttributes\">\n" +
"			<Osoba xd:script=\"options moreElements, moreAttributes\"\n" +
"				Meno='string(); onFalse setErr()'\n" +
"				Priezvisko='string(); onFalse setErr()'/>\n" +
"		</Poistnik>\n" +
"	</NovaZml>\n" +
"  </SuborP1A>\n" +
"</xd:def>";

	public static void setErr(XXData chkElem) {
		System.out.println("setErr(): " + chkElem.getParseResult().getReporter().getReport());
	}

	private static void test(String xdef) {
		Properties props = System.getProperties();
		props.setProperty(XDConstants.XDPROPERTY_STRING_CODES, "Windows-1250");
		XDPool xdpool = XDFactory.compileXD(props, xdef);
		XDDocument xdoc = xdpool.createXDDocument("Example1");
		ArrayReporter reporter = new ArrayReporter();
		System.out.println(new File("").getAbsolutePath());
		File f = new File(new File("").getAbsolutePath() + "/src/test/java/bugreports/CharacterTestData.xml");
		xdoc.xparse(f, reporter);
		if (reporter.errors()) {
			System.out.println("Chyba:\n" + reporter.get(0));
		}
		System.out.println("*************************************");
	}

	public static void main(String[] args) {
		test(xdef);
		test(xdef2);
	}

}