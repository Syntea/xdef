package test.xdutils;

import java.io.File;
import org.xdef.xml.KXmlUtils;
import org.xdef.util.GenCollection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import test.XDTester;

/** Test of xdef utilities.
 * @author  trojan
 */
public class TestXDefUtils extends XDTester {

	public TestXDefUtils() {super();}

	@Override
	public void test() {
		File tempDir = clearTempDir();
		try {
			String outFile =
				new File(tempDir, "collection.xml").getCanonicalPath();
			GenCollection.main(new String[] {
				"-m",
				"-o", outFile,
				"-e", "windows-1250",
				"-i",getDataDir() + "test/xdef_xdef.xml"});
			Element collection =
				KXmlUtils.parseXml(outFile).getDocumentElement();
			NodeList nl = KXmlUtils.getChildElements(collection);
			if (nl.getLength() != 1) {
				fail("Num of definitions: " + nl.getLength());
			} else {
				for (int i = 0; i < nl.getLength(); i++) {
					if (!"def".equals(nl.item(i).getLocalName())) {
						fail("item[" + i + "]: " + nl.item(i).getNodeName());
					}
				}
			}
			GenCollection.main(new String[] {
				"-m",
				"-o", outFile,
				"-e", "windows-1250",
				"-i", getDataDir() + "test/Matej3*.def"});
			collection = KXmlUtils.parseXml(outFile).getDocumentElement();
			nl = KXmlUtils.getChildElements(collection);
			if (nl.getLength() != 2) {
				fail("Num of definitions: " + nl.getLength());
			} else {
				for (int i = 0; i < nl.getLength(); i++) {
					if (!"def".equals(nl.item(i).getLocalName())) {
						fail("item[" + i + "]: " + nl.item(i).getNodeName());
					}
				}
			}
			new File(outFile).delete();
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}