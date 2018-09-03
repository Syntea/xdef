/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: TestPrettyXdef.java.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package test.xdutils;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.xml.KDOMBuilder;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.util.PrettyXdef;
import java.io.File;
import cz.syntea.xdef.sys.ReportWriter;
import test.utils.XDTester;

/** Test pretty indentation of XDefinition.
 * @author Trojan
 */
public class TestPrettyXdef extends XDTester {

	public TestPrettyXdef() {super();}

	@Override
	public void test() {
		String dataDir = getDataDir();
		String tempDir = dataDir + "temp/";
		File f = new File(tempDir);
		if (!f.exists()) {
			f.mkdirs();
		}

		try {
			PrettyXdef.main(new String[] {
				"-o", tempDir + "Igor02_xd.txt",
				"-i", "8",
				"-e", "UTF-8",
				"-p", "xd",
				dataDir + "test/Igor02_xd.xml"});
			KDOMBuilder kd = new KDOMBuilder();
			kd.setNamespaceAware(true);
			kd.parse(tempDir + "Igor02_xd.txt"); //just check XML
			ReportWriter rw =
				KXmlUtils.compareXML(tempDir + "Igor02_xd.txt",
				dataDir + "test/Igor02_xd.xml");
			if (rw.errorWarnings()) {
				fail();
			}
		} catch (Error ex) {
			fail(ex);
		}
		try {
			PrettyXdef.main(new String[] {
				"-o", tempDir + "Matej2_L1_common.txt",
				"-i", "8",
				"-e", "UTF-8",
				"-p", "xd",
				dataDir + "test/Matej2_L1_common.def"});
			KDOMBuilder kd = new KDOMBuilder();
			kd.setNamespaceAware(true);
			kd.parse(tempDir + "Matej2_L1_common.txt"); //just check XML
			ReportWriter rw =
				KXmlUtils.compareXML(tempDir + "Matej2_L1_common.txt",
				dataDir + "test/Matej2_L1_common.def");
			if (rw.errorWarnings()) {
				Report rep;
				while ((rep = ((ArrayReporter) rw).getReport()) != null) {
					fail(rep);
				}
				fail();
			}
		} catch (Error ex) {
			fail(ex);
		}
		try {
			FUtils.deleteAll(tempDir, true);
		} catch (SException ex) {
			throw new RuntimeException("Could not delete temporary files!", ex);
		}
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}

}