package task4;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlOutStream;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import java.io.IOException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** External methods called from Order2.xdef */
public class Orders2ext {

	Element _items, _customers;
	int _errors, _errorsOld, _count;
	KXmlOutStream _output;
	XDPool _xpool;

	private final XPath _xp = XPathFactory.newInstance().newXPath();

	public Orders2ext(XDPool xpool,
		String items,
		String customers,
		String output) throws IOException {
		_items = KXmlUtils.parseXml(items).getDocumentElement();
		_customers = KXmlUtils.parseXml(customers).getDocumentElement();
		_output = new KXmlOutStream(output, "UTF-8", true);
		_xpool = xpool;
		_errorsOld = _errors = _count = 0;
	}

	public static boolean customer(XXData xdata) {
		/* Get the instance of this class. */
		Orders2ext u = (Orders2ext) xdata.getUserObject();
		String s = xdata.getTextValue(); /* get attribute value. */
		try {
			/* Find the customer. */
			XPathExpression xExpr = u._xp.compile("Customer[@CustomerCode='" + s + "']");
			NodeList nl =
				(NodeList) xExpr.evaluate(u._customers, XPathConstants.NODESET);
			if (nl == null || nl.getLength() == 0) {
				/* customer not found, increase erro counter and report an error. */
				u._errors++;
				xdata.error("Incorrect customer code: " + s, null);
				return false; /* returns false -> incorrect. */
			}
			return true; /* Customer found, OK */
		} catch (Exception ex) {
			u._errors++;
			xdata.error("Unexpected exception: " + ex, null);
			return false;
		}
	}

	public static boolean item(XXData xdata) {
		/* Get the instance of this class. */
		Orders2ext u = (Orders2ext) xdata.getUserObject();
		String s = xdata.getTextValue(); /* get attribute value. */
		try {
			/* Find description of the Item according to code. */
			XPathExpression xExpr = u._xp.compile("Product[@Code='" + s + "']");
			NodeList nl = (NodeList) xExpr.evaluate(
				u._items, XPathConstants.NODESET);
			if (nl == null || nl.getLength() == 0) {
				/* Item not found, increase the error counter and report an error. */
				u._errors++;
				xdata.error("Incorrect item number: " + s, null);
				return false;
			}
			return true; /* Item was found, OK */
		} catch (Exception ex) {
			u._errors++;
			xdata.error("Unexpected exception: " + ex, null);
			return false;
		}
	}

	public static void writeObj(XXNode xnode) {
		/* Get the instance of this class. */
		Orders2ext u = (Orders2ext) xnode.getUserObject();
		if (u._errors != u._errorsOld) {
			/* an new error occured, do not write recore*/
			u._errorsOld = u._errors; /* save error counter to "errorsOld". */
		} else {
			/* Check if this the first record. */
			if (u._count++ == 0) {
				/* first time, so write the root element. */
				u._output.setIndenting(true); /* nastaveni indentace na vystupu. */
				u._output.writeElementStart(
				xnode.getElement().getOwnerDocument().getDocumentElement());
			}
			/* Prepare XDDocument for the construction according model "Order". */
			XDDocument xdoc = u._xpool.createXDDocument("Orders");
			xdoc.setUserObject(u);
			Element el = xnode.getElement();
			xdoc.setXDContext(el);
			try {
				String s = el.getAttribute("CustomerCode");
				XPathExpression xExpr = u._xp.compile(
					"Customer[@CustomerCode='" + s + "']/Address");
				NodeList nl = (NodeList) xExpr.evaluate(
					u._customers, XPathConstants.NODESET);
				Element adresa = (Element) nl.item(0);
				xdoc.setVariable("address", adresa);
				/* Create the object and write it. */
				ArrayReporter reporter = new ArrayReporter();
				u._output.writeNode(xdoc.xcreate("Order", reporter));
			} catch (Exception ex) {
				// do nothing, an error was already reported when parsed
			}
		}
	}

	public static void closeAll(XXNode xnode) {
		/* Get the instance of this class. */
		Orders2ext u = (Orders2ext) xnode.getUserObject();
		/* Check if a record was written. */
		if (u._count != 0) {
			/* Yes, close the stream. */
			u._output.writeElementEnd();
			u._output.closeStream();
		}
	}

	public static String price(XXNode xnode) {
		try {
			/* Get the instance of this class. */
			Orders2ext u = (Orders2ext) xnode.getUserObject();
			Element el = xnode.getXDContext().getElement(); /* Actual context. */
			String s = el.getAttribute("ProductCode"); /* get commodity code. */
			/* Find the item description. */
			XPathExpression xExpr = u._xp.compile("Product[@Code='" + s + "']");
			/* We already know that it exists. */
			NodeList nl = (NodeList) xExpr.evaluate(u._items, XPathConstants.NODESET);
			Element el1 = (Element) nl.item(0);
			/* get qouantity as a number */
			int pocet = Integer.parseInt(el.getAttribute("Quantity"));
			/* compute price */
			float cena = Float.parseFloat(el1.getAttribute("Price")) * pocet;
			/* Return it as a string */
			return String.valueOf(cena);
		} catch (Exception ex) {return "-1"; /* this never should happen! */}
	}
}