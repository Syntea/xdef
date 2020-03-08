package task2;

import org.xdef.sys.SPosition;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import org.xdef.XDFactory;
import org.xdef.XDXmlOutStream;
import org.xdef.proc.XXNode;
import java.io.IOException;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Orders3ext {

	private final String _outputFile;  // Output file
	private final String _errorFile;  // Error file
	private XDXmlOutStream _outputWriter;  // writer for output
	private XDXmlOutStream _errorWriter;  // writer for errors
	private Document _errorDoc;  // XML document with errors
	private Document _objDoc;  // XML document with orders
	private final KXpathExpr _xpath;  // prepared xpath expression
	private int _errCount;  // error counter
	private int _errCountOld;  // previous value of the error counter
	private int _count;  // counter of correct orders

	// Create instance of this class.
	public Orders3ext(String outputFile, String errorFile) throws IOException {
		_outputFile = outputFile; // output file name.
		_errorFile = errorFile; // error file name.
		// writers will be created when an item to bew written occurs
		_errorWriter = _outputWriter = null;
		// Prepare XPath expression to get customer code from an order
		// Because of the command "forget" it will be in the processed document
		// only one (the actually processed) order.
		// This XPath expression will be executed when an error item is generated.
		_xpath = new KXpathExpr("/Orders/Order[1]/@KodZakaznika");
		// Clear counters
		_errCount = _errCountOld = _count = 0;
	}

	// Write the order (only if no error was reported
	public static void writeOrder(XXNode xnode) {
		// Get "User object" (i.e. the instance of this class).
		Orders3ext x = (Orders3ext) xnode.getUserObject();
		if (x._errCount != x._errCountOld) { // an error was reported?
			// set old error counter (i.e. no errors reported for next item)
			x._errCountOld = x._errCount;
		} else {
			// No error reported, so writ the order the result.
			Element el = xnode.getElement();
			if (x._count == 0) { // check if nothig was written yeat
				// Create writer a write the XML header and the root element
				try {
					x._outputWriter =
						XDFactory.createXDXmlOutStream(x._outputFile,
						"windows-1250", true);
				} catch (Exception ex) {
					throw new RuntimeException(ex.getMessage());
				}
				x._objDoc = el.getOwnerDocument();
				x._outputWriter.writeElementStart(x._objDoc.getDocumentElement());
			}
			x._count++; // increase the counter of correct orders.
			// write the processed order
			x._outputWriter.writeNode(el);
		}
	}

	// Create the writeOrder and set the variable "_error".
	public static void err(XXNode xnode, long code) {
		// Get "User object" (i.e. the instance of this class).
		Orders3ext x = (Orders3ext) xnode.getUserObject();
		// Create the XML writer for errors (if it was not created yeat)
		if (x._errCount == 0) {
			try {
				x._errorWriter = XDFactory.createXDXmlOutStream(x._errorFile,
					"windows-1250",true);
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}
			x._errorDoc = KXmlUtils.newDocument(null,"Errors",null);
			// write XML header and the root element.
			x._errorWriter.writeElementStart(x._errorDoc.getDocumentElement());
		}
		x._errCount++; // increase error counter
		// Create the element to be written.
		Element el = x._errorDoc.createElement("Error");
		el.setAttribute("ErrorCode", String.valueOf(code));
		String customer =
			(String) x._xpath.evaluate(xnode.getElement(), XPathConstants.STRING);
		el.setAttribute("Customer", customer);
		SPosition pos = xnode.getSPosition();
		el.setAttribute("Line", String.valueOf(pos.getLineNumber()));
		el.setAttribute("Column", String.valueOf(pos.getColumnNumber()));
		x._errorWriter.writeNode(el);
	}

	// Get number of errors
	public int errNum() {return _errCount;}

	// Close created files
	public void closeAll() {
		// close result output stream (if something was written)
		if (_outputWriter != null) {
			_outputWriter.closeStream(); // write root end tag and close the stream
		}
		// close error output stream (if something was written)
		if (_errorWriter != null) {
			_errorWriter.closeStream(); // write root end tag and close the stream
		}
	}
}