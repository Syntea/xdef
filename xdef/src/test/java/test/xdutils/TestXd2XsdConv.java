package test.xdutils;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.proc.XXElement;
import org.xdef.util.GenCollection;
import java.io.File;
import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xdef.sys.ReportWriter;
import org.xdef.util.XdefToXsd;
import test.XDTester;

/** Test XDefinition to schema conversion.
 * @author Ilia Alexandrov
 */
public class TestXd2XsdConv extends XDTester {

	public TestXd2XsdConv() {super();}

	private static final String MAIN_DEF_NAME = "main";
	private static final String MAIN_SCHEMA_FILE_NAME = "main.xsd";
//    private XdefToXsd _conv;
	private ReportWriter _repWriter;
	private File _dataDir;
	private File _tempDir;
	private File _xdefFile;
	private SchemaFactory _xsdFactory;
	private Validator _validator;
	private XDDocument _chkDoc;
	private boolean _prepared = false;
	private ErrMessage _errMessage;

	private void init() {
//        _conv = new XdefToXsd();
		_xsdFactory =
			SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		_repWriter = new ArrayReporter();
		File dataDir = new File(getDataDir());
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new RuntimeException(
				"Data directory does not exists or is not a directory!");
		}
		_dataDir = new File(dataDir.getAbsolutePath(), "xd2xsd");
		if (!_dataDir.exists() || !_dataDir.isDirectory()) {
			throw new RuntimeException(
				"Xsd2xd directory does not exists or is not a directory!");
		}
		File tempDir = new File(getTempDir());
		if (!tempDir.exists()) {
			tempDir.mkdir();
		} else {
			if (!tempDir.isDirectory()) {
				throw new RuntimeException(
					"Temporary directory is not a directory!");
			}
		}
		_tempDir = new File(tempDir, "xd2xsd");
		if (!_tempDir.exists()) {
			_tempDir.mkdir();
		} else {
			if (!_tempDir.isDirectory()) {
				throw new RuntimeException(
					"Temporary 'xd2xsd' directory is not a directory!");
			}
		}
	}

	private static void displayFiles(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (File f: list) {
				displayFiles(f);
			}
		} else {
			try {
				System.err.println("=== File: " + file.getAbsolutePath());
				System.err.println(FUtils.readString(file));
				System.err.println("===");
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}

	private boolean prepare(String testName) {
		_prepared = false;
		//prepare xdefinition
		_xdefFile = new File(_dataDir.getAbsolutePath(), testName+".xdef");
		if (!_xdefFile.exists() || !_xdefFile.isFile()) {
			setMessage(new ErrMessage("Test XDefinition file does not exists!",
				_xdefFile, null));
			return false;
		}
		try {
			XDPool xdPool = compile(_xdefFile);
			if (!xdPool.exists(MAIN_DEF_NAME)) {
				setMessage(new ErrMessage(
					"Could not find main definition in XDefinition file!",
					_xdefFile,
					null));
				return false;
			}
			_chkDoc = xdPool.createXDDocument(MAIN_DEF_NAME);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			setMessage(new ErrMessage("Could not prepare XDefinition!",
				_xdefFile, ex));
			return false;
		}
		//prepare schema
		File schemaDir = new File(_tempDir, testName);
		if (!schemaDir.exists()) {
			schemaDir.mkdir();
		} else {
			if (!schemaDir.isDirectory()) {
				setMessage(new ErrMessage(
					"Temporary directory for schema is not a directory!",
					schemaDir,
					null));
				return false;
			}
		}
		//generate schema
		try {
			XdefToXsd.genSchema(_xdefFile.getAbsolutePath(),
				schemaDir.getAbsolutePath(), null, null, null);
		} catch (Exception ex) {
			displayFiles(_xdefFile);
			try {
				Element el = GenCollection.genCollection(
					new String[]{_xdefFile.getAbsolutePath()}, true,true,true);
				System.err.println(KXmlUtils.nodeToString(el, true));
			} catch (Exception exx) {}
			ex.printStackTrace(System.err);
			setMessage(new ErrMessage(
				"Could not convert given XDefinition file!", _xdefFile, ex));
			return false;
		}
		//get main schema
		File mainSchema = new File(schemaDir, MAIN_SCHEMA_FILE_NAME);
		if (!mainSchema.exists() || !mainSchema.isFile()) {
			setMessage(
				new ErrMessage("Main schema file does not exists or not a file",
					mainSchema,	null));
			return false;
		}
		//prepare schema validator
		try {
			Schema schema = _xsdFactory.newSchema(mainSchema);
			_validator = schema.newValidator();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			displayFiles(_xdefFile);
			System.err.println("============");
			displayFiles(_tempDir);
			setMessage(new ErrMessage(
				"Could not prepare schema validator!\n" + ex.getMessage(),
				mainSchema, ex));
			return false;
		}
		_prepared = true;
		return true;
	}

	private boolean parse(String xmlName) {
		if (!_prepared) {
			return true;
		}
		File xmlFile = new File(_dataDir, xmlName + ".xml");
		if (!xmlFile.exists() || !xmlFile.isFile()) {
			setMessage(new ErrMessage(
				"XML file does not exists or is not a file!", xmlFile, null));
			return false;
		}
		//check by xdef
		_repWriter.clear();
		_chkDoc.xparse(xmlFile, _repWriter);
		if (_repWriter.errors()) {
			setMessage(new ErrMessage(
				_repWriter.getReportReader().printToString(), xmlFile, null));
			return false;
		}
		//check by schema
		Source source = new StreamSource(xmlFile);
		try {
			_validator.validate(source);
		} catch (IOException ioex) {
			ioex.printStackTrace(System.err);
			setMessage(new ErrMessage("Could not read from XML file stream!",
				xmlFile, ioex));
			return false;
		} catch (SAXException sex) {
			displayFiles(_xdefFile);
			try {
				Element el = GenCollection.genCollection(
					new String[]{_xdefFile.getAbsolutePath()}, true,true,true);
				System.err.println(KXmlUtils.nodeToString(el, true));
			} catch (Exception exx) {}
			System.err.println("============");
			displayFiles(_tempDir);
			sex.printStackTrace(System.err);
			setMessage(new ErrMessage(
				"Error during validating XML file by schema!", xmlFile, sex));
			return false;
		}
		return true;
	}

	private boolean parseFail(String xmlName) {
		if (!_prepared) {
			return true;
		}
		File xmlFile = new File(_dataDir.getAbsolutePath() + File.separator
				+ xmlName + ".xml");
		if (!xmlFile.exists() || !xmlFile.isFile()) {
			setMessage(new ErrMessage(
				"XML file does not exists or is not a file!", xmlFile, null));
			return false;
		}
		//check by xdef
		_repWriter.clear();
		_chkDoc.xparse(xmlFile, _repWriter);
		if (!_repWriter.errors()) {
			setMessage(new ErrMessage("XML file is valid by XDefinition!",
				xmlFile, null));
			return false;
		}
		//check by schema
		Source source = new StreamSource(xmlFile);
		try {
			_validator.validate(source);
			setMessage(new ErrMessage("XML file is valid against schema!",
				xmlFile, null));
			return false;
		} catch (IOException ioex) {
			setMessage(new ErrMessage("Could not read from XML file stream!",
				xmlFile, ioex));
			return false;
		} catch (SAXException saxex) {
			return  true;
		}
	}

	private ErrMessage popMessage() {
		if (_errMessage == null) {
			return ErrMessage.NO_MESSAGE;
		}
		ErrMessage message = _errMessage;
		_errMessage = null;
		return message;
	}

	private void setMessage(ErrMessage message) {_errMessage = message;}

	private static class ErrMessage {

		public static final ErrMessage NO_MESSAGE =
				new ErrMessage("No message!", null, null);
		private final String _message;
		private final File _file;
		private final Exception _ex;

		public ErrMessage(String message, File file, Exception ex) {
			_message = message;
			_file = file;
			_ex = ex;
		}

		@Override
		public String toString() {
			String ret = "\nError: " + _message;
			if (_file != null) {
				ret += "\n\tat file: " + _file.getAbsolutePath();
			}
			if (_ex != null) {
				ret += "\n\tcause: " + _ex.getMessage();
			}
			return ret;
		}
	}

	@Override
	public void test() {
		init();

		assertTrue(prepare("basicTest"), popMessage());
		assertTrue(parse("basicTest_valid_1"), popMessage());
		assertTrue(parse("basicTest_valid_2"), popMessage());
		assertTrue(parse("basicTest_valid_3"), popMessage());
		assertTrue(parseFail("basicTest_invalid_1"), popMessage());
		assertTrue(parseFail("basicTest_invalid_2"), popMessage());
		assertTrue(parseFail("basicTest_invalid_3"), popMessage());
		assertTrue(parseFail("basicTest_invalid_4"), popMessage());

		assertTrue(prepare("simpleRefTest"), popMessage());
		assertTrue(parse("simpleRefTest_valid_1"), popMessage());

		assertTrue(prepare("simpleModelTest"), popMessage());
		assertTrue(parse("simpleModelTest_valid_1"), popMessage());
		assertTrue(parse("simpleModelTest_valid_2"), popMessage());
		assertTrue(parse("simpleModelTest_valid_3"), popMessage());
		assertTrue(parse("simpleModelTest_valid_4"), popMessage());
		assertTrue(parse("simpleModelTest_valid_5"), popMessage());
		assertTrue(parse("simpleModelTest_valid_6"), popMessage());
		assertTrue(parse("simpleModelTest_valid_7"), popMessage());
		assertTrue(parse("simpleModelTest_valid_8"), popMessage());

		assertTrue(prepare("typeTest"), popMessage());
		assertTrue(parse("typeTest_valid_1"), popMessage());
		assertTrue(parseFail("typeTest_invalid_1"), popMessage());
		assertTrue(parseFail("typeTest_invalid_2"), popMessage());
		assertTrue(parseFail("typeTest_invalid_3"), popMessage());
		assertTrue(parseFail("typeTest_invalid_4"), popMessage());
		assertTrue(parseFail("typeTest_invalid_5"), popMessage());
		assertTrue(parseFail("typeTest_invalid_6"), popMessage());
		assertTrue(parseFail("typeTest_invalid_7"), popMessage());
		assertTrue(parseFail("typeTest_invalid_8"), popMessage());
		assertTrue(parseFail("typeTest_invalid_9"), popMessage());
		assertTrue(parseFail("typeTest_invalid_10"), popMessage());
		assertTrue(parseFail("typeTest_invalid_11"), popMessage());

		assertTrue(prepare("ATTR_to_ATTR"), popMessage());
		assertTrue(parse("ATTR_to_ATTR_valid_1"), popMessage());
		assertTrue(parse("ATTR_to_ATTR_valid_2"), popMessage());
		assertTrue(parseFail("ATTR_to_ATTR_invalid_1"), popMessage());
		assertTrue(parseFail("ATTR_to_ATTR_invalid_2"), popMessage());

		assertTrue(prepare("ATTR_to_CHLD"), popMessage());
		assertTrue(parse("ATTR_to_CHLD_valid_1"), popMessage());

		assertTrue(prepare("ATTR_to_ATTR_CHLD"), popMessage());
		assertTrue(parse("ATTR_to_ATTR_CHLD_valid_1"), popMessage());

		assertTrue(prepare("CHLD_to_ATTR"), popMessage());
		assertTrue(parse("CHLD_to_ATTR_valid_1"), popMessage());

		assertTrue(prepare("CHLD_to_CHLD"), popMessage());
		assertTrue(parse("CHLD_to_CHLD_valid_1"), popMessage());

		assertTrue(prepare("CHLD_to_ATTR_CHLD"), popMessage());
		assertTrue(parse("CHLD_to_ATTR_CHLD_valid_1"), popMessage());

		assertTrue(prepare("ATTR_CHLD_to_ATTR"), popMessage());
		assertTrue(parse("ATTR_CHLD_to_ATTR_valid_1"), popMessage());

		assertTrue(prepare("ATTR_CHLD_to_CHLD"), popMessage());
		assertTrue(parse("ATTR_CHLD_to_CHLD_valid_1"), popMessage());

		assertTrue(prepare("ATTR_CHLD_to_ATTR_CHLD"), popMessage());
		assertTrue(parse("ATTR_CHLD_to_ATTR_CHLD_valid_1"), popMessage());

		assertTrue(prepare("dateTimeTest"), popMessage());
		assertTrue(parse("dateTimeTest_valid_1"), popMessage());

		/*VT*/
		setProperty("xdef.warnings", "false");//do not check validity of 3.1
		assertTrue(prepare("declarationTest"), popMessage());
		assertTrue(parse("declarationTest_valid_1"), popMessage());
		assertTrue(parse("declarationTest_valid_2"), popMessage());
		assertTrue(parse("declarationTest_valid_3"), popMessage());
		assertTrue(parseFail("declarationTest_invalid_1"), popMessage());
		assertTrue(parseFail("declarationTest_invalid_2"), popMessage());
		assertTrue(parseFail("declarationTest_invalid_3"), popMessage());
		assertTrue(parseFail("declarationTest_invalid_4"), popMessage());

		assertTrue(prepare("globalAndLocalTest"), popMessage());
		assertTrue(parse("globalAndLocalTest_X"), popMessage());
		assertTrue(parseFail("globalAndLocalTest_X_invalid"), popMessage());
		assertTrue(parse("globalAndLocalTest_Y"), popMessage());
		assertTrue(parseFail("globalAndLocalTest_Y_invalid"), popMessage());
		assertTrue(parse("globalAndLocalTest_Z"), popMessage());
		assertTrue(parseFail("globalAndLocalTest_Z_invalid"), popMessage());
		resetProperties();
		/*VT*/

		assertTrue(prepare("schemaTypeTest"), popMessage());
		assertTrue(parse("schemaTypeTest_valid_1"), popMessage());

		assertTrue(prepare("namespaceTest"), popMessage());
		assertTrue(parse("namespaceTest_valid"), popMessage());

		assertTrue(prepare("namespaceTest1"), popMessage());
		assertTrue(parse("namespaceTest1_valid_1"), popMessage());

		assertTrue(prepare("B1_common"), popMessage());
		assertTrue(parse("B1_Common_valid_1"), popMessage());
		assertTrue(parse("B1_Common_valid_2"), popMessage());

		/*VT*/
		setProperty("xdef.warnings", "false"); // do not check deprecated
		assertTrue(prepare("Sisma"), popMessage());
		assertTrue(parse("Sisma"), popMessage());

		assertTrue(prepare("Sisma3_1"), popMessage());
		assertTrue(parse("Sisma"), popMessage());
		resetProperties();
		/*VT*/

		try {
			FUtils.deleteAll(_tempDir, true);
		} catch (Exception ex) {
			throw new RuntimeException("Could not delete temporary files!", ex);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// External methods for the test Sisma
////////////////////////////////////////////////////////////////////////////////
	public static void initParams(XXElement chkElem) {}
	public static void setErr(XXElement chkElem, XDValue[] params) {}
	public static boolean tab(XXElement chkEl, XDValue[] params) {return true;}
	public static void chkOpt_RC_ifEQ(XXElement chkElem, XDValue[] params) {}
	public static void dateDavka(XXElement chkElem, XDValue[] params) {}
	public static void chk_dec_nonNegative(XXElement chkEl, XDValue[] params) {}
	public static void chk_RC_DatNar_ifEQ(XXElement chkEl, XDValue[] params) {}
	public static void setDefault_ifEx(XXElement chkElem, XDValue[] params) {}
	public static void emptySubjHasAddr(XXElement chkElem, XDValue[] params) {}
	public static String getIdOsoba(XXElement chkElem) { return "1"; }
	public static void protocol(XXElement chkElem, String role, long idXxx) {}
	public static void protocol(XXElement chkElem, String role, String ident) {}
	public static void outputIVR(XXElement chkElem, XDValue[] params) {}
	public static String getKodPartnera() { return "1"; }
	public static void chkEQ_PojistitelFuze(XXElement chkEl, XDValue[] params){}
	public static void chk_Poj_NeexElement(XXElement chkEl, XDValue[] params) {}
	public static void chkOpt_IC_ifEQ(XXElement chkElem, XDValue[] params) {}
	public static void hasElement_if(XXElement chkElem, XDValue[] params) {}
	public static void subjekt_OsobaOrFirma(XXElement chkEl, XDValue[] params){}
	public static String getIdSubjekt(XXElement chkElem) { return "1"; }
	public static void notEmptyMisto(XXElement chkElem, XDValue[] params) {}
	public static void equal(XXElement chkElem, XDValue[] params) {}
	public static void chkOpt_CisloTP_ifEQ(XXElement chkEl, XDValue[] params) {}
	public static String getIdVozidlo(XXElement chkElem) { return "1"; }
	public static boolean kvadrant(XXElement chkElem) { return true; }
	public static void chk_TypMinusPlneni_Platba(XXElement chkEl,
		XDValue[] params) {}
	public static boolean fil0(XXElement chkEl, XDValue[] params) {return true;}
////////////////////////////////////////////////////////////////////////////////

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}