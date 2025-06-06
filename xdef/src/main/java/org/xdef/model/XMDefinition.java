package org.xdef.model;

import org.xdef.sys.SPosition;
import org.xdef.XDDocument;
import java.util.Properties;

/** Interface of X-definition.
 * @author Vaclav Trojan
 */
public interface XMDefinition extends XMNode {

	/** Get all Element models from this X-definition.
	 * @return The array of element models.
	 */
	public XMElement[] getModels();

	/** Get source position of this X-definition.
	 * @return source ID of this X-definition..
	 */
	public SPosition getSourcePosition();

	/** Get all Element models defined as root from this X-definition.
	 * @return The array of root element models.
	 */
	public XMElement[] getRootModels();

	/** Get the Element model with given NameSpace and name.
	 * @param nsURI NameSpace URI of element or null.
	 * @param name name of element (may be qualified).
	 * @return Element model with given NameSpace and name or return null if such model not exists.
	 */
	public XMElement getModel(String nsURI, String name);

	/** Create XDDocument.
	 * @return XDDocument created from this XMDefinition.
	 */
	public XDDocument createXDDocument();

	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	public  Properties getImplProperties();

	/** Get implementation property of X-definition.
	 * @param name The name of property.
	 * @return the value implementation property of X-definition.
	 */
	public String getImplProperty(final String name);

	/** Get version of X-definition.
	 * @return version of X-definition: version 2.0, 3.1, 3.2 or 4.0. (see org.xdef.impl.XConstants#XDxx).
	 */
	public byte getXDVersion();

	/** Get XML version of X-definition source.
	 * @return XML version of X-definition source (1.0 or 1.1); see org.xdef.impl.XConstants#XML10 or .XML11).
	 */
	public byte getXmlVersion();

	/** Check if given name is declared as local in this X-definition.
	 * @param name the name to be checked.
	 * @return true if given name is declared as local in this X-definition.
	 */
	public boolean isLocalName(final String name);
}