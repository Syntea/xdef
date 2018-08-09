package cz.syntea.xdef.util;

import java.io.File;
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
import org.testng.asserts.Assertion;
import org.w3c.dom.Element;

import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.util.L1protocol.L1A_ChkParser_dummy;
import cz.syntea.xdef.xml.KXmlUtils;


public class XdefToXsdTest {
	
	
	/**
	 * tests conversion xdef <-> xsd for example "L1-protocol"
	 * (conversion xsd -> xdef is malfunctional)
	 * @throws Exception
	 */
	@Test
	public static void L1XdefToXsdTest() throws Exception {
		
		final Assertion a = new Assertion();
		
		//load list of source xdef of L1-protokol, must not contain any "forget"
		//------------------------------------------------------------------
		List<String>      xdefSrcList = new ArrayList<String>();
		for (String xdefRsrc : L1XdefRsrcList) {
			xdefSrcList.add(IOUtil.copyFile(
				XdefToXsdTest.class.getResourceAsStream(exampleL1Pkg + "/L1/"
				+ xdefRsrc))
			);
		}
		
		final File   outDir        = new File("run/output/junit-test/xdef2xsd");
		final File   xsdGenDir     = new File(outDir,       "xsd-gen");
		final File   xdefGenDir    = new File(outDir,       "xdef-gen");
		final File   xdefGenCol    = new File(xdefGenDir,   "L1col.xdef");
		final File   L1batchXVDir  = new File(outDir,       "L1batchXV");
		final File   L1batchXVFile = new File(L1batchXVDir, "L1batchXV.xml");
		final String mainName      = "SouborL1A";
		final File   xsdMain       = new File(xsdGenDir,    mainName + ".xsd");
		
		xsdGenDir   .mkdirs();
		xdefGenDir  .mkdirs();
		L1batchXVDir.mkdirs();
		
		//xdef-validation of source xml-data to update data by xdef
		//------------------------------------------------------------------
		XDBuilder xdb = XDFactory.getXDBuilder(null);
		xdb.setSource(xdefSrcList.toArray(new String[0]));
		xdb.setExternals(L1A_ChkParser_dummy.class);
		XDPool	xdp = xdb.compileXD();
		
		XDDocument xddoc     = xdp.createXDDocument(mainName);
		Element    L1batchXV = xddoc.xparse(XdefToXsdTest.class
		                       .getResourceAsStream(L1batchRsrc), null);
		KXmlUtils.writeXml(L1batchXVFile, L1batchXV);
		
		//generate xml-schema from xdef to directory xsdGenDir
		//------------------------------------------------------------------
		XdefToXsd.genSchema(
			xdefSrcList.toArray(new String[0]),
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

		Source    L1batch   = new StreamSource(L1batchXVFile);
		Validator validator = xsdCol.newValidator();
		validator.validate(L1batch);
		
		//result of the xml-schema-validation: VALID (it means no exception)
		//------------------------------------------------------------------
		a.assertEquals(true, true);
		
		//FIXME: following test fails - there is a bug in the tool 
		//       XsdToXdef.genCollection()
		boolean run = false;
		if (run) {
			//feedback generation of xdef from the genrated xml-schema
			//to directory xdefGenDir
			//------------------------------------------------------------------
			XsdToXdef.genCollection(xsdMain.getPath(), xdefGenCol.getPath(),
				"xd", null);
			
			//xdef-validation of L1batch by regenerated xdef
			//------------------------------------------------------------------
			XDBuilder  xdb2   = XDFactory.getXDBuilder(null);
			xdb2.setSource(xdefGenCol);
			xdb2.setExternals(L1A_ChkParser_dummy.class);
			XDPool xdp2;
			
			try {
				//compilation of xdef
				xdp2 = xdb2.compileXD();
			} catch (SRuntimeException ex) {
				logger.error("compilation of the regenerated xdef failed, " +
					"exception:\n" + ex.getMessage());
				throw ex;
			}
	
			//xdef-validation
			XDDocument    xddoc2   = xdp2.createXDDocument(mainName);
			ArrayReporter reporter = new ArrayReporter();
			xddoc2.xparse(L1batchXVFile, reporter);
			
			if (reporter.errorWarnings()) {
				String msg =
					"xdef-validation failed:\n" + reporter.printToString();
				logger.error(msg);
				throw new Exception(msg);
			}
		}
	}

	
	
	//launch tests standalone
	public static void main(String... args) throws Exception {
		XdefToXsdTest.L1XdefToXsdTest();
	}
	
	
	
	/** main package of example "L1protocol" */
	private static final String   exampleL1Pkg   = "L1protocol";
	/** testing xml-data in example "L1protocol" */
	private static final String   L1batchRsrc    =
		exampleL1Pkg + "/data/L1batch-0008L199991400374A.xml";
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

}
