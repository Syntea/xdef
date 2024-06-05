package test.xdutils;

import org.xdef.sys.NullReportWriter;
import org.xdef.sys.ReportWriter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.util.XsdToXdef;
import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xdef.XDBuilder;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xml.sax.SAXException;
import test.XDTester;

/** Test conversion of XML schema to X-definition. */
public class TestXsd2XdConv extends XDTester {

	private XDDocument _chkDoc;
	private ReportWriter _repWriter;
	private File _dataDir;
	private File _tempDir;
	private ErrMessage _errMessage;
	private boolean _prepared;
	private SchemaFactory _xsdFactory;
	private Validator _validator;

	private void init() {
		_xsdFactory =
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		_repWriter = new NullReportWriter(false);
		File dataDir = new File(getDataDir());
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new RuntimeException(
				"Data directory does not exist or is not a directory");
		}
		_dataDir = new File(dataDir.getAbsolutePath(),"xsd2xd");
		if (!_dataDir.exists() || !_dataDir.isDirectory()) {
			throw new RuntimeException(
				"Xsd2xd directory does not exist or is not a directory");
		}
		File tempDir = clearTempDir();
		if (!tempDir.exists()) {
			tempDir.mkdir();
		} else {
			if (!tempDir.isDirectory()) {
				throw new RuntimeException(
					"Temporary directory is not a directory");
			}
		}
		_tempDir = new File(tempDir.getAbsolutePath(),"xsd2xd");
		if (!_tempDir.exists()) {
			_tempDir.mkdir();
		} else {
			if (!_tempDir.isDirectory()) {
				throw new RuntimeException(
					"Temporary 'xsd2xd' directory is not a directory");
			}
		}
		_prepared = false;
	}

	private boolean prepare(String testName) {
		_prepared = false;
		//prepare schema
		File schemaFile = new File(_dataDir.getAbsolutePath(), testName+".xsd");
		if (!schemaFile.exists() || !schemaFile.isFile()) {
			setMessage(new ErrMessage(
				"Schema file does not exist or is not a file",schemaFile,null));
			return false;
		}
		try {
			Schema schema = _xsdFactory.newSchema(schemaFile);
			_validator = schema.newValidator();
		} catch (SAXException ex) {
			setMessage(new ErrMessage(
				"Could not prepare schema validator", null, ex));
			return false;
		}
		//generating XDefinition collection
		String xdefFileName =
			new File(_tempDir, testName + ".xdef").getAbsolutePath();
		try {
			XsdToXdef.genCollection(schemaFile.getAbsolutePath(),
				xdefFileName, null, null);
		} catch (IOException | RuntimeException ex) {
			setMessage(new ErrMessage(
				"Could not generate XDefinition from given schema",
				schemaFile, ex));
			return false;
		}
		File xdefFile = new File(xdefFileName);
		if (!xdefFile.exists() || !xdefFile.isFile()) {
			setMessage(new ErrMessage(
				"Generated XDefinition file does not exist or is not a file",
				xdefFile, null));
			return false;
		}
		try {
			ArrayReporter reporter = new ArrayReporter();
			XDBuilder xdBuild = XDFactory.getXDBuilder(reporter, null);
			xdBuild.setSource(xdefFile);
			XDPool xdPool = xdBuild.compileXD();
			if (!xdPool.exists(testName)) {
				setMessage(new ErrMessage(
					"Main XDefinition is missing", xdefFile, null));
				return false;
			}
			reporter.checkAndThrowErrors();
			if (reporter.errorWarnings()) {
				System.out.println("[WARNING]\n" + reporter.printToString());
			}
			_chkDoc = xdPool.createXDDocument(testName);
		} catch (RuntimeException ex) {
			setMessage(new ErrMessage(
				"Could not prepare XDefinition", xdefFile, ex));
			return false;
		}
		_prepared = true;
		return true;
	}

	private boolean parse(String xmlName) {
		if (!_prepared) {
			return true;
		}
		File xmlFile = new File(_dataDir.getAbsolutePath(), xmlName + ".xml");
		if (!xmlFile.exists() || !xmlFile.isFile()) {
			setMessage(new ErrMessage(
				"XML file does not exist or is not a file", xmlFile, null));
			return false;
		}
		//validate by schema
		Source source = new StreamSource(xmlFile);
		try {
			_validator.validate(source);
		} catch (IOException | SAXException ex) {
			setMessage(new ErrMessage(
				"XML file IS NOT VALID against schema", xmlFile, ex));
			return false;
		}
		//validate by XDefinition
		_repWriter.clear();
		_chkDoc.xparse(xmlFile, _repWriter);
		if (_repWriter.errors()) {
			setMessage(new ErrMessage(
				"XML file IS NOT VALID against XDefinition", xmlFile, null));
			return false;
		}
		return true;
	}

	private boolean parseFail(String xmlName) {
		if (!_prepared) {
			return true;
		}
		File xmlFile = new File(_dataDir.getAbsolutePath(), xmlName + ".xml");
		if (!xmlFile.exists() || !xmlFile.isFile()) {
			setMessage(new ErrMessage(
				"XML file does not exist or is not a file", xmlFile, null));
			return false;
		}
		//validate by schema
		Source source = new StreamSource(xmlFile);
		try {
			_validator.validate(source);
			setMessage(new ErrMessage(
				"XML file IS VALID against schema", xmlFile, null));
			return false;
		} catch (IOException | SAXException ex) {
			//validate by XDefinition
			_repWriter.clear();
			_chkDoc.xparse(xmlFile, _repWriter);
			if (!_repWriter.errors()) {
				setMessage(new ErrMessage(
					"XML file IS VALID against XDefinition",xmlFile,null));
				return false;
			}
			return true;
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
			new ErrMessage("No message", null, null);
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

		assertTrue(prepare("D1A"), popMessage());
		assertTrue(parse("D1A"), popMessage());

		assertTrue(prepare("D2A"), popMessage());
		assertTrue(parse("D2A"), popMessage());

		assertTrue(prepare("D3A"), popMessage());
		assertTrue(parse("D3A"), popMessage());

		assertTrue(prepare("D5"), popMessage());
		assertTrue(parse("D5"), popMessage());

		assertTrue(prepare("L1A"), popMessage());
		assertTrue(parse("L1A"), popMessage());

		assertTrue(prepare("M1RC"), popMessage());
		assertTrue(parse("M1RC"), popMessage());

		assertTrue(prepare("M1RN"), popMessage());
		assertTrue(parse("M1RN"), popMessage());

		assertTrue(prepare("M1RS"), popMessage());
		assertTrue(parse("M1RS"), popMessage());

		assertTrue(prepare("M1RT"), popMessage());
		assertTrue(parse("M1RT"), popMessage());

		assertTrue(prepare("M1RV"), popMessage());
		assertTrue(parse("M1RV"), popMessage());

		assertTrue(prepare("P1A"), popMessage());
		assertTrue(parse("P1A"), popMessage());

		assertTrue(prepare("t000"), popMessage());
		assertTrue(parse("t000"), popMessage());
		assertTrue(parseFail("t000_1e"), popMessage());
		assertTrue(parseFail("t000_2e"), popMessage());
		assertTrue(parseFail("t000_3e"), popMessage());

		assertTrue(prepare("t001"), popMessage());
		assertTrue(parse("t001"), popMessage());
		assertTrue(parseFail("t001_1e"), popMessage());
		assertTrue(parseFail("t001_2e"), popMessage());
		assertTrue(parseFail("t001_3e"), popMessage());
		assertTrue(parseFail("t001_4e"), popMessage());

		assertTrue(prepare("t002"), popMessage());
		assertTrue(parse("t002"), popMessage());
		assertTrue(parseFail("t002_1e"), popMessage());
		assertTrue(parseFail("t002_2e"), popMessage());

		assertTrue(prepare("t003"), popMessage());
		assertTrue(parse("t003"), popMessage());
		assertTrue(parseFail("t003_1e"), popMessage());

		assertTrue(prepare("t004"), popMessage());
		assertTrue(parse("t004"), popMessage());
		assertTrue(parseFail("t004_1e"), popMessage());

		assertTrue(prepare("t005"), popMessage());
		assertTrue(parse("t005"), popMessage());

		assertTrue(prepare("t006"), popMessage());
		assertTrue(parse("t006"), popMessage());
		assertTrue(parse("t006_1"), popMessage());
		assertTrue(parseFail("t006_2e"), popMessage());
		assertTrue(parseFail("t006_3e"), popMessage());

		assertTrue(prepare("t007"), popMessage());
		assertTrue(parse("t007"), popMessage());
		assertTrue(parseFail("t007_1e"), popMessage());

		assertTrue(prepare("t008"), popMessage());
		assertTrue(parse("t008"), popMessage());

		assertTrue(prepare("t009"), popMessage());
		assertTrue(parse("t009"), popMessage());

		assertTrue(prepare("t010"), popMessage());
		assertTrue(parse("t010"), popMessage());

		assertTrue(prepare("t011"), popMessage());
		assertTrue(parse("t011"), popMessage());

		assertTrue(prepare("t012"), popMessage());
		assertTrue(parse("t012"), popMessage());
		assertTrue(parse("t012_1"), popMessage());
		assertTrue(parse("t012_2"), popMessage());

		assertTrue(prepare("t013"), popMessage());
		assertTrue(parse("t013"), popMessage());

		assertTrue(prepare("t014"), popMessage());
		assertTrue(parse("t014"), popMessage());
		assertTrue(parse("t014_1"), popMessage());

		assertTrue(prepare("t015"), popMessage());
		assertTrue(parse("t015"), popMessage());
		assertTrue(parse("t015_1"), popMessage());

		assertTrue(prepare("t016"), popMessage());
		assertTrue(parse("t016"), popMessage());
		assertTrue(parseFail("t016e"), popMessage());
		assertTrue(prepare("t018"), popMessage());
		assertTrue(parse("t018"), popMessage());
		assertTrue(prepare("t019"), popMessage());
		assertTrue(parse("t019"), popMessage());

		assertTrue(prepare("t020"), popMessage());
		assertTrue(parse("t020"), popMessage());

		assertTrue(prepare("t990"), popMessage());
		assertTrue(parse("t990"), popMessage());
		assertTrue(parse("t990_1"), popMessage());
		assertTrue(parseFail("t990_1e"), popMessage());
		assertTrue(parseFail("t990_2e"), popMessage());
		assertTrue(parseFail("t990_3e"), popMessage());
		assertTrue(parseFail("t990_4e"), popMessage());
		assertTrue(parseFail("t990_5e"), popMessage());
		assertTrue(prepare("test_00015"), popMessage());
		assertTrue(parse("test_00015_data"), popMessage());
		assertTrue(prepare("MARC21"), popMessage());
		assertTrue(parse("MARC21"), popMessage());
		//my tests
		assertTrue(prepare("basicTestSchema"), popMessage());
		assertTrue(parse("basicTest_valid_1"), popMessage());
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
