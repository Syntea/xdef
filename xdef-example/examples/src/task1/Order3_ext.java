package task1;

import org.xdef.sys.SPosition;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Order3_ext {

	/** Add error item. */
	public static void err(XXNode xnode, XDValue[] params) {
		Document doc = ((Element) xnode.getUserObject()).getOwnerDocument();
		Element newElem = doc.createElement("Error");
		newElem.setAttribute("ErrorCode", params[0].toString());
		Element root = xnode.getElement().getOwnerDocument().getDocumentElement();
		newElem.setAttribute("Customer", root.getAttribute("CustomerCode"));
		SPosition pos = xnode.getSPosition();
		newElem.setAttribute("Line", String.valueOf(pos.getLineNumber()));
		newElem.setAttribute("Column", String.valueOf(pos.getColumnNumber()));
		doc.getDocumentElement().appendChild(newElem);
		xnode.clearTemporaryReporter(); // remove the error
	}
}