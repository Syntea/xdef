package org.xdef.impl.saxon;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDContainer;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ATTR;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_TEXT;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.XQueryImpl;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXqueryExpr;

/** Saxon implementation of XQuery.
 * @author Vaclav Trojan
 */
public class XQuerySaxonImpl implements XQueryImpl {

	/** Create empty instance of this class. */
	public XQuerySaxonImpl() {
		if (!KXqueryExpr.isXQueryImplementation()) {
			throw new SRuntimeException();
		}
	}

	private static boolean findVariable(QName[] qnames, QName qname) {
		for (int i = 0; i < qnames.length; i++) {
			if (qname.equals(qnames[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	/** Execute XPath expression and return result.
	 * @param node node or <i>null</i>.
	 * @param xNode node model or <i>null</i>.
	 * @return The string representation of value of the object.
	 */
	public final XDContainer exec(final KXqueryExpr x,
		final Node node,
		final XXNode xNode) {
		try {
			QName[] qnames = x.getAllExternalVariables();
			if (xNode != null && qnames != null && qnames.length > 0) {
				String[] names = xNode.getVariableNames();
				for (int i = 0; i < names.length; i++) {
					String s = names[i];
					XDValue var = xNode.getVariable(s);
					if (var == null) {
						continue;
					}
					if (s.charAt(0) == '$') {
						s = s.substring(1);
					}
					QName qname = new QName(s);
					if (!findVariable(qnames, qname)) {
						continue;
					}
					switch (var.getItemId()) {
						case XD_ATTR:
						case XD_ELEMENT:
						case XD_TEXT:
//						case XMLNODE_VALUE:
							Node n = (Node) var.getXMLNode();
							if (n != null) {
								x.bindValue(qname, n);
							}
							break;
						case XD_DATETIME:
							x.bindValue(qname, var.datetimeValue());
							break;
						case XD_LONG:
							x.bindValue(qname, var.longValue());
							break;
						case XD_DOUBLE:
							x.bindValue(qname, var.doubleValue());
							break;
						default:
							x.bindValue(qname, var.toString());
					}
				}
			}
			DefContainer result = new DefContainer();
			int count = 0;
			XQResultSequence seq = (XQResultSequence) (node == null
				? x.evaluate() : x.evaluate(node));
			if (seq == null) {
				return result;
			}
			while(seq.next()) {
				XQItem item = seq.getItem();
				if (item == null) {
					break;
				}
				count++;
				switch (item.getItemType().getItemKind()) {
					case XQItemType.XQITEMKIND_ATOMIC: {
						switch (item.getItemType().getBaseType()) {
							case XQItemType.XQBASETYPE_BOOLEAN:
								result.addXDItem(
									new DefBoolean(item.getBoolean()));
								continue;
							case XQItemType.XQBASETYPE_BYTE:
							case XQItemType.XQBASETYPE_INT:
							case XQItemType.XQBASETYPE_INTEGER:
							case XQItemType.XQBASETYPE_LONG:
							case XQItemType.XQBASETYPE_POSITIVE_INTEGER:
							case XQItemType.XQBASETYPE_NEGATIVE_INTEGER:
							case XQItemType.XQBASETYPE_UNSIGNED_BYTE:
							case XQItemType.XQBASETYPE_UNSIGNED_INT:
							case XQItemType.XQBASETYPE_UNSIGNED_SHORT:
							case XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER:
							case XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER:
								result.addXDItem(new DefLong(item.getLong()));
								continue;
							case XQItemType.XQBASETYPE_FLOAT:
							case XQItemType.XQBASETYPE_DOUBLE:
								result.addXDItem(
									new DefDouble(item.getDouble()));
								continue;
							case XQItemType.XQBASETYPE_DECIMAL:
								result.addXDItem(new DefDecimal(
									item.getAtomicValue()));
								continue;
							case XQItemType.XQBASETYPE_UNSIGNED_LONG:
								result.addXDItem(new DefBigInteger(
									item.getAtomicValue()));
								continue;
							case XQItemType.XQBASETYPE_DURATION:
							case XQItemType.XQBASETYPE_DAYTIMEDURATION:
							case XQItemType.XQBASETYPE_YEARMONTHDURATION:
								result.addXDItem(
									new DefDuration(item.getAtomicValue()));
								continue;
							case XQItemType.XQBASETYPE_DATE:
							case XQItemType.XQBASETYPE_DATETIME:
							case XQItemType.XQBASETYPE_GDAY:
							case XQItemType.XQBASETYPE_GMONTH:
							case XQItemType.XQBASETYPE_GMONTHDAY:
							case XQItemType.XQBASETYPE_GYEAR:
							case XQItemType.XQBASETYPE_GYEARMONTH:
								result.addXDItem(
									new DefDate(item.getAtomicValue()));
								continue;
							case XQItemType.XQBASETYPE_BASE64BINARY:
								result.addXDItem(DefBytes.parseBase64(
									item.getAtomicValue()));
								continue;
							case XQItemType.XQBASETYPE_HEXBINARY:
								result.addXDItem(DefBytes.parseHex(
									item.getAtomicValue()));
								continue;
//							case XQItemType.XQBASETYPE_ENTITY:
//							case XQItemType.XQBASETYPE_ENTITIES: //????
							default:
								result.addXDItem(
									new DefString(item.getAtomicValue()));
						}
						continue;
					}
					case XQItemType.XQITEMKIND_ATTRIBUTE:
					case XQItemType.XQITEMKIND_COMMENT:
					case XQItemType.XQITEMKIND_PI:
					case XQItemType.XQITEMKIND_TEXT:
						result.addXDItem(new DefString(
							item.getNode().getNodeValue()));
						continue;
					case XQItemType.XQITEMKIND_DOCUMENT:
					case XQItemType.XQITEMKIND_DOCUMENT_ELEMENT:
						result.addXDItem(new DefElement(
							((Document) item.getNode()).getDocumentElement()));
						continue;
					case XQItemType.XQITEMKIND_ELEMENT:
						result.addXDItem(
							new DefElement((Element) item.getNode()));
						continue;
					case XQItemType.XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT:
					case XQItemType.XQITEMKIND_SCHEMA_ATTRIBUTE:
					case XQItemType.XQITEMKIND_SCHEMA_ELEMENT:
						continue;
					case XQItemType.XQITEMKIND_ITEM: {
//						Node n = item.getNode();
						//result.addXDItem(new DefXmlNode(item.getNode()));
						result.addXDItem(
							new DefString(item.getItemAsString(null)));
						continue;
					}
					default:
						throw new SRuntimeException(
							"UNKNOWN RESULT TYPE OF ITEM [" + count + "]: "
								+ item.getItemType().getClass());
				}
			}
			return result;
		} catch (Exception ex) {
			throw new SRuntimeException(ex.toString());
		}
	}
}