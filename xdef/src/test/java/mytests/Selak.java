package mytests;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

/**
 * @author Vaclav Trojan
 */
public class Selak {
	public static void main(String[] args) {
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr = new StringWriter();
		try {
			String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='collection'>\n"+
"<xd:declaration> int n = 0; </xd:declaration>\n"+
"<xd:any xd:name='collection'\n"+
"        xd:script='init outln(getElement().toString());\n"+
"                   options moreAttributes; finally outln(\"Num: \" + n)'>\n"+
" <xd:any xd:script='*;options moreAttributes, moreElements, moreText;\n"+
"  finally if (n++ LT 10 || n GT 6212340) outln(getElement().toString());\n"+
"         /*else cancel();*/ forget'/>\n"+
"</xd:any>\n"+
"</xd:def>";
/*  */
			XDPool xp = XDFactory.compileXD(null, xdef);
			URL u = new URL("https://data.dnb.de/DNB/dnb_all_dnbmarc_20241013-1.mrc.xml.gz");
//			URL u = new URL("https://data.dnb.de/DNB/dnb_all_dnbmarc_20240213-2.mrc.xml.gz");
			InputStream in = new GZIPInputStream(u.openStream());
			XDDocument xd = xp.createXDDocument();
			xd.xparse(in, "dnb_all_dnbmarc_20240213-2.mrc.xml", reporter);
		} catch (IOException | RuntimeException ex) {
			if (ex.getMessage().contains("F XDEF906")) {
				System.out.println("Process canceled, first 10 items printed.");
				if (reporter.size() > 1) {
					System.out.println(reporter.printToString());
				} else {
					System.out.println(swr);
				}
			} else {
				ex.printStackTrace(System.out); // some other error
			}
		}
	}
}