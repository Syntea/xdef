/*
 * File: TestXDefUtils.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */

package test.xdutils;

import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.util.GenCollection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Test of xdef utilities.
 *
 * @author  trojan
 */
public class TestXDefUtils extends STester {

	public TestXDefUtils() {super();}

	@Override
	public void test() {
		String tempDir = getTempDir();
		if (tempDir == null) {
			fail("Temporary directory is missing, test canceled");
			return;
		}
		Report.setLanguage("en"); //localize
		try {
			String outFile = tempDir + "collection.xml";
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
			outFile = tempDir + "collection.xml";
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
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			FUtils.deleteAll(tempDir, true);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		runTest();
	}

}
