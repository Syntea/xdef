package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.ext.XExtUtils;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/** Parser of Schema "ENTITY" type.
 * @author Vaclav Trojan
 */
public class XSParseENTITY extends XSParseQName {
	private static final String ROOTBASENAME = "ENTITY";

	public XSParseENTITY() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		String id = result.getSourceBuffer();
		if (!chkEntity(id, xnode.getElement())) {
			//Incorrect value of '&{0}'
			result.error(XDEF.XDEF809, parserName() + ": " + id);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}

	/** Check if exists an entity with given name in given element.
	 * @param id name of entity
	 * @param el element
	 * @return true if entity exists.
	 */
	static boolean chkEntity(final String id, final Element el) {
		DocumentType dt = el.getOwnerDocument().getDoctype();
		if (dt == null) {
			return false;
		}
		NamedNodeMap nm = dt.getEntities();
		if (nm == null || nm.getLength() == 0) {
			return false;
		}
		String nsURI = XExtUtils.getQnameNSUri(id, el);
		if (nsURI.length() > 0) {
			int ndx = id.indexOf(':');
			String localName = ndx < 0 ? id : id.substring(ndx + 1);
			return nm.getNamedItemNS(nsURI, localName) != null;
		} else {
			return nm.getNamedItem(id) != null;
		}
	}
}
