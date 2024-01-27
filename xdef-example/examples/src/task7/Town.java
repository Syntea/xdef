package task7;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;

public class Town {

	public static void main(String... args) throws Exception {
		// 1. Compile X-definitions
		XDPool xPool = XDFactory.compileXD(null, "src/task7/town.xdef");

		XDDocument xd;
		Element el;
		ArrayReporter reporter = new ArrayReporter();

		// 3. Create XDDOcument, set language, and process the localized version
		// for the English language
		xd = xPool.createXDDocument("town");
		xd.setLexiconLanguage("eng");
		el = xd.xparse("task7/input/town_eng.xml", reporter);
		if (reporter.errors()) {
			System.err.println("Error on English version");
		}

		// 3. Create XDDOcument, set language, and process the localized version
		// for the English language
		xd = xPool.createXDDocument("town");
		xd.setLexiconLanguage("eng");
		reporter.clear();
		el = xd.xparse("task7/input/town_eng.xml", reporter);
		if (reporter.errors()) {
			System.err.println("Error on English version");
		} else {
			System.out.println("eng OK");
		}

		// 4. Create XDDOcument, set language, and process the localized version
		// for the German language
		xd = xPool.createXDDocument("town");
		xd.setLexiconLanguage("deu");
		reporter.clear();
		el = xd.xparse("task7/input/town_deu.xml", reporter);
		if (reporter.errors()) {
			System.err.println("Error on German version");
		} else {
			System.out.println("deu OK");
		}

		// 5. Create XDDOcument and translate the localized version to Czech.
		// result write to the file "task7/output/town_ces.xml"
		xd = xPool.createXDDocument("town");
		reporter.clear();
		el = xd.xtranslate("task7/input/town_deu.xml", "deu", "ces", reporter);
		new File("task7/output").mkdirs();
		if (reporter.errors()) {
			System.err.println("Error on translation from 'deu' to 'ces'");
		} else {
			KXmlUtils.writeXml("task7/output/town_ces.xml", el, true, false);
			System.out.println(
				"town_deu transated to ces (see task7/output/town_ces.xml)");
		}

		// 6. Check translated Czech version
		xd = xPool.createXDDocument("town");
		xd.setLexiconLanguage("ces");
		reporter.clear();
		xd.xparse("task7/output/town_ces.xml", reporter);
		if (reporter.errors()) {
			System.err.println("errors in translation");
		}
	}
}