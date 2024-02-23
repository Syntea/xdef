package test.xdutils;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.ReportWriter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.File;
import java.util.Properties;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.impl.util.gencollection.XDGenCollection;
import org.xdef.sys.SException;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.GenCollection;
import org.xdef.util.XdefToXsd;
import org.xml.sax.SAXException;
import test.XDTester;

/** Test conversion of X-definition to XML schema.
 * @author Vaclav Trojan
 */
public class TestXd2XsdConv extends XDTester {

	private static final String MAIN_DEF_NAME = "main";
	private static final String MAIN_SCHEMA_FILE_NAME = "main.xsd";
	private ReportWriter _repWriter;
	private File _dataDir;
	private XDPool _xp;
	private File _tempDir;
	private File _xdefFile;
	private SchemaFactory _xsdFactory;
	private Validator _validator;
	private XDDocument _chkDoc;
	private boolean _prepared = false;
	private ErrMessage _errMessage;

	private void init() {
		_xsdFactory =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
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
		File tempDir = clearTempDir();
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
			} catch (SException ex) {
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
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
				 XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
			props.put("xdef.warnings", "false");
			_xp = XDFactory.compileXD(props, _xdefFile);
			if (!_xp.exists(MAIN_DEF_NAME)) {
				setMessage(new ErrMessage(
					"Could not find main definition in XDefinition file!",
					_xdefFile,
					null));
				return false;
			}
			_chkDoc = _xp.createXDDocument(MAIN_DEF_NAME);
		} catch (RuntimeException ex) {
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
			XdefToXsd.genSchema(
				new File[] {_xdefFile}, schemaDir, null, null, "main", true);
		} catch (Exception ex) {
			displayFiles(_xdefFile);
			try {
				Element el = GenCollection.genCollection(
					new String[]{_xdefFile.getAbsolutePath()}, true,true,true);
				System.err.println(KXmlUtils.nodeToString(el, true));
			} catch (Exception exx) {}
			setMessage(new ErrMessage(
				"Could not convert given XDefinition file!", _xdefFile, ex));
			return false;
		}
		//get main schema
		File mainSchema = new File(schemaDir, MAIN_SCHEMA_FILE_NAME);
		if (!mainSchema.exists() || !mainSchema.isFile()) {
			setMessage(new ErrMessage(
				"Main schema file doesn't exist",mainSchema,null));
			return false;
		}
		//prepare schema validator
		try {
			Schema schema = _xsdFactory.newSchema(mainSchema);
			_validator = schema.newValidator();
		} catch (SAXException ex) {
			displayFiles(_xdefFile);
			System.err.println("============");
			displayFiles(_tempDir);
			setMessage(new ErrMessage("Can't prepare schema validator!\n"
				+ ex.getMessage(),mainSchema, ex));
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
				"XML file doesn't exist or isn't file!", xmlFile, null));
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
			setMessage(new ErrMessage("Can't read from XML file stream!",
				xmlFile, ioex));
			return false;
		} catch (SAXException sex) {
			displayFiles(_xdefFile);
			try {
				Element el = XDGenCollection.genCollection(
					new String[]{_xdefFile.getAbsolutePath()}, true,true,true);
				System.err.println(KXmlUtils.nodeToString(el, true));
			} catch (Exception exx) {}
			System.err.println("============");
			displayFiles(_tempDir);
			setMessage(new ErrMessage(
				"Error when validating XML file by schema!", xmlFile, sex));
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
				"XML file doesn't exist or is not file!", xmlFile, null));
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
			setMessage(new ErrMessage("Couldn't read from XML file!",
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
		try {
			init();
			assertTrue(prepare("basicTest"), popMessage());
			assertTrue(parse("basicTest_valid_1"), popMessage());
			assertTrue(parse("basicTest_valid_2"), popMessage());
			assertTrue(parse("basicTest_valid_3"), popMessage());
			assertTrue(parseFail("basicTest_invalid_1"), popMessage());
			assertTrue(parseFail("basicTest_invalid_2"), popMessage());
			assertTrue(parseFail("basicTest_invalid_3"), popMessage());
			assertTrue(parseFail("basicTest_invalid_4"), popMessage());

			assertTrue(prepare("multiXdefTest"), popMessage());
			assertTrue(parse("multiXdefTest_valid_1"), popMessage());

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

			assertTrue(prepare("collectionTest"), popMessage());
			assertTrue(parse("collectionTest_valid1"), popMessage());
			assertTrue(parse("collectionTest_valid2"), popMessage());
			assertTrue(parse("collectionTest_valid3"), popMessage());
			assertTrue(parse("collectionTest_valid4"), popMessage());

			assertTrue(prepare("dateTimeTest"), popMessage());
			assertTrue(parse("dateTimeTest_valid_1"), popMessage());

			assertTrue(prepare("declarationTest"), popMessage());
			assertTrue(parse("declarationTest_valid_1"), popMessage());
			assertTrue(parseFail("declarationTest_invalid_1"), popMessage());
			assertTrue(parseFail("declarationTest_invalid_2"), popMessage());

			assertTrue(prepare("namespaceTest1"), popMessage());
			assertTrue(parse("namespaceTest1_valid_1"), popMessage());

			assertTrue(prepare("schemaTypeTest"), popMessage());
			assertTrue(parse("schemaTypeTest_valid_1"), popMessage());

			assertTrue(prepare("typeFixedTest"), popMessage());
			assertTrue(parse("typeFixedTest_valid"), popMessage());

			assertTrue(prepare("B1_common"), popMessage());
			assertTrue(parse("B1_Common_valid_1"), popMessage());
			assertTrue(parse("B1_Common_valid_2"), popMessage());
		} catch (Exception ex) {fail(ex);}
		clearTempDir();
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}
