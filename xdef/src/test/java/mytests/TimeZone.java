package mytests;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import java.util.Properties;

public class TimeZone {

	public static void main(String[] args) {
		process("2024-11-04T10:00:00+03:00", "Europe/Prague");
		process("2024-08-04T10:00:00+03:00", "Europe/Prague");
	}

	// Prepare path to Xdefinition
	static String process(String date, String defaultZone) {
		System.out.printf("Processing %s\n", date);
		String xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"root\" root=\"root\">\n" +
"  <xd:declaration>\n" +
"    type datumCas ydatetime('yyyy-MM-ddTHH:mm:ss[Z]', 'yyyy-MM-ddTHH:mm:ssZ');\n" +
"    external String dateTime;\n" +
"  </xd:declaration>\n" +
"  <root datum=\"datumCas(); onTrue dateTime = getText()\" />\n" +
"</xd:def>";
		String xmlData = String.format("<root datum=\"%s\" />", date);
		// 1. Create XDPool.
		Properties props = System.getProperties();
		props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, defaultZone);
		XDPool xp = XDFactory.compileXD(props, xdef);
		System.out.println("Default time zone: " + xp.getDefaultZone().getDisplayName()
			+ " (" + xp.getDefaultZone().getID() + ")");

		// 2. Create XDDocument.
		XDDocument xdoc = xp.createXDDocument("root");

		// 3. Validate and process XML data.
		ArrayReporter reporter = new ArrayReporter();
		String result = "";
		xdoc.xparse(xmlData, reporter); //validate and process data
		if (!reporter.errors()) {
			result = xdoc.getVariable("dateTime").stringValue();
			System.out.println("Result: " + result);
		} else {
			System.out.println("Errors detected in input data.");
			System.out.println(reporter.printToString());
		}
		return result;
	}
}