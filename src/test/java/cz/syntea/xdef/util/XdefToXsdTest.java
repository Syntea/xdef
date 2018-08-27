package cz.syntea.xdef.util;

import static test.util.Assert.assertEquals;
import static test.util.Assert.assertNoErrors;
import static test.util.TestUtil.compile;
import static test.util.TestUtil.getResrc;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.util.L1protocol.L1A_ChkParser_dummy;
import cz.syntea.xdef.xml.KXmlUtils;



@Test(groups = "other")
public class XdefToXsdTest {
	
	
	/**
	 * tests conversion xdef <-> xsd for example "L1-protocol"
	 * (conversion xsd -> xdef is malfunctional)
	 * @throws Exception
	 */
	@Test
	public static void L1XdefToXsdTest() throws Exception {
		
		final File   outDir        = new File("target/test-output/data-tmp/xdef2xsd");
		final File   xsdGenDir     = new File(outDir,       "xsd-gen");
		final File   xdefGenDir    = new File(outDir,       "xdef-gen");
		final File   xdefGenCol    = new File(xdefGenDir,   "L1col.xdef");
		final File   l1batchXVDir  = new File(outDir,       "L1batchXV");
		final File   l1batchXVFile = new File(l1batchXVDir, "L1batchXV.xml");
		final String mainName      = "SouborL1A";
		final File   xsdMain       = new File(xsdGenDir,    mainName + ".xsd");
		
		xsdGenDir   .mkdirs();
		xdefGenDir  .mkdirs();
		l1batchXVDir.mkdirs();
		
		ArrayReporter reporter = new ArrayReporter();
		
		//load list of source xdef of L1-protokol, must not contain any "forget"
		//------------------------------------------------------------------
		List<URL>    xdefSrcList  = new ArrayList<URL>();
		List<String> xdefSrcSList = new ArrayList<String>();
		for (String xdefRsrc : L1XdefRsrcList) {
			URL xdef = getResrc(XdefToXsdTest.class,
				exampleL1Pkg + "/L1/" + xdefRsrc);
			xdefSrcList.add(xdef);
			xdefSrcSList.add(FUtils.readString(xdef.openStream(), encoding));
		}
		
		//xdef-validation of source xml-data to update data by xdef
		//------------------------------------------------------------------
		XDPool xdp = compile(xdefSrcList.toArray(new URL[0]), L1A_ChkParser_dummy.class);
		
		XDDocument xddoc     = xdp.createXDDocument(mainName);
		URL        L1batch   = getResrc(XdefToXsdTest.class, L1batchRsrc);
		reporter.clear();
		Element    l1batchXV = xddoc.xparse(L1batch, reporter);
		KXmlUtils.writeXml(l1batchXVFile, l1batchXV);
		assertNoErrors(reporter);
		
		//test of l1batch size after xdef-validation
		//it should holds at least: #l1batch/2 < #l1batchXV
		//if it fails then l1batchXV was cut accidentally
		//------------------------------------------------------------
		boolean l1sizeTest = 
			FUtils.readString(
				XdefToXsdTest.class.getResourceAsStream(L1batchRsrc)
			).length() / 2
			< FUtils.readString(l1batchXVFile).length()
		;
		assertEquals(l1sizeTest, true);
		
		//generate xml-schema from xdef to directory xsdGenDir
		//------------------------------------------------------------------
		XdefToXsd.genSchema(
			xdefSrcSList.toArray(new String[0]),
			xsdGenDir.getPath(),
			mainName, mainName,
			"xs", "xsd", null
		);

		//xml-schema-validation of source L1-batch data
		//------------------------------------------------------------------
		Source[]      xsdSrcAr = new Source[]{new StreamSource(xsdMain)};
		SchemaFactory schFact  = SchemaFactory.newInstance(
			XMLConstants.W3C_XML_SCHEMA_NS_URI
		);
		Schema        xsdCol   = schFact.newSchema(xsdSrcAr);

		Source    L1batchXV   = new StreamSource(l1batchXVFile);
		Validator validator = xsdCol.newValidator();
		validator.validate(L1batchXV);
		
		//result of the xml-schema-validation: VALID (it means no exception)
		//------------------------------------------------------------------
		assertEquals(true, true);
		
		//feedback generation of xdef from the genrated xml-schema
		//to directory xdefGenDir
		//------------------------------------------------------------------
		XsdToXdef.genCollection(xsdMain.getPath(), xdefGenCol.getPath(),
			"xd", null);
		
		//xdef-validation of L1batch by regenerated xdef
		//------------------------------------------------------------------
		XDPool xdp2 = compile(xdefGenCol, L1A_ChkParser_dummy.class);
		
		//xdef-validation
		XDDocument xddoc2 = xdp2.createXDDocument(mainName);
		reporter.clear();
		xddoc2.xparse(l1batchXVFile, reporter);
		assertNoErrors(reporter);
		
		logger.info("OK - L1XdefToXsdTest");
	}

	
	
	@Test(enabled = false)
	public static void ignoredTest() {
		logger.info("OK - ignored test");
	}
	
	
	
	@SuppressWarnings("unused")
	private static void notTest() {
		logger.info("OK - it isn't test");
	}
	
	
	
	@Test
	public void emptyTest() {
		logger.info("OK - empty test, in class: " + getClass().getName());
	}
	
	
	
	@Test(enabled = false)
	public static void faillingTest() throws Exception {
		logger.info("start - failling test");
		boolean run = true;
		if (run) {
			throw new Exception("fake failure");
		}
		logger.info("OK - failling test");
	}
	
	
	
	/** main package of example "L1protocol" */
	private static final String   exampleL1Pkg   = "L1protocol";
	/** testing xml-data in example "L1protocol" */
	private static final String   L1batchRsrc    =
		exampleL1Pkg + "/data/L1batch-0008L199991400374A.xml";
	/** default file-encoding */
	private static final String   encoding    = "UTF-8";
	/** seznam xdefinic definujicich L1-protokol */
	private static final String[] L1XdefRsrcList = {
		"!L1Macros.xdef",
		"DotazSU.xdef",
		"L1_common.xdef",
		"Naroky.xdef",
		"NovaIdentPU.xdef",
		"NovaIdentSU.xdef",
		"ReaktivacePU.xdef",
		"ReaktivaceSU.xdef",
		"RegistracePU.xdef",
		"RegistraceSU.xdef",
		"Regres.xdef",
		"SouborL1A.xdef",
		"UzavreniPU.xdef",
		"UzavreniSU.xdef",
		"ZmenaPU.xdef",
		"ZmenaSU.xdef",
		"ZruseniNaroku.xdef",
		"ZruseniPU.xdef",
		"ZruseniSU.xdef"
	};
	
	
	
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(
		XdefToXsdTest.class
	);

	
	
	/** launch tests standalone */
	public static void main(String... args) throws Exception {
		XdefToXsdTest.L1XdefToXsdTest();
	}

}
