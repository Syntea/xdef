import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.xml.KXmlUtils;

/** Construct XML element from XML source element. */
public class ExampleLexicon {

	public static void main(String[] args) {
		// Prepare source path to XDefinition and XML data.
		String xdef = "./src/ExampleLexicon.xdef";
		String data = "./src/ExampleLexicon_deu.xml";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);
		
		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument("contract");

		// 3. set language of input data
		xdoc.setLexiconLanguage("deu");
			
		// 4. Parse data
		Element el = xdoc.xparse(data, null); // 
		
		// 5. Print parsed result  
		System.out.println("Parsed result (German): ");
		System.out.println(KXmlUtils.nodeToString(el, true));
		
		// 6. translate german data to English  		
		el = xdoc.xtranslate(data, "deu", "eng", null);
		System.out.println("Translated result to English: ");
		System.out.println(KXmlUtils.nodeToString(el, true));
    }
}