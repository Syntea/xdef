package task1;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SPosition;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Order3 {

	public static void main(String[] args) throws Exception {
		// Create instance of XDDocument object (from XDPool)
		// (external method "err" called from the Script see below)
		XDPool xpool = XDFactory.compileXD(null, "src/task1/Order3.xdef");

		// Create instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Create reporter
		ArrayReporter reporter = new ArrayReporter();

		// Prepare XML element for recording of errors
		Element errors =
			KXmlUtils.newDocument(null, "Errors", null).getDocumentElement();
		xdoc.setUserObject(errors);

		// Run validation mode (you can also try task1/input/Order_err.xml)
		xdoc.xparse("task1/input/Order.xml", reporter);

		// Check errors
		if (errors.getChildNodes().getLength() > 0) {
			// Write error information to the file
			KXmlUtils.writeXml("task1/errors/Order_err.xml", errors);
			System.err.println("Incorrect input data");
		} else {
			// No errors, write the processed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", xdoc.getElement());
			System.out.println("OK");
		}
	}

	// External method called from the Script of X-definition
	public static void err(XXNode xnode, long code) {
		Document doc = ((Element) xnode.getUserObject()).getOwnerDocument();
		Element newElem = doc.createElement("Error");
		newElem.setAttribute("ErrorCode", String.valueOf(code));
		Element root = xnode.getElement().getOwnerDocument().getDocumentElement();
		newElem.setAttribute("Customer", root.getAttribute("CustomerCode"));
		SPosition pos = xnode.getSPosition();
		newElem.setAttribute("Line", String.valueOf(pos.getLineNumber()));
		newElem.setAttribute("Column", String.valueOf(pos.getColumnNumber()));
		doc.getDocumentElement().appendChild(newElem);
	}
}