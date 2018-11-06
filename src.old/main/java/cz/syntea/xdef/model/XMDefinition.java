package cz.syntea.xdef.model;

import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.XDDocument;
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
	 * @param nsURI NameSpace URI of element or <tt>null</tt>.
	 * @param name name of element (may be qualified).
	 * @return Element model with given NameSpace and name or return
	 * <tt>null</tt> if such model not exists.
	 */
	public XMElement getModel(String nsURI, String name);

	/** Get the Element model with given NameSpace and name.
	 * @param nsURI NameSpace URI of element or <tt>null</tt>.
	 * @param name name of element (may be qualified).
	 * @return Element model with given NameSpace and name or return
	 * <tt>null</tt> if such model not exists.
	 */
	public XMElement getRootModel(String nsURI, String name);

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
	 * @return version of X-definition:
	 * or 20 for version 2.0 AND 31 for version 3.1.
	 * (see {@link cz.syntea.xdef.XDConstants#XD20_ID}
	 * or {@link cz.syntea.xdef.XDConstants#XD31_ID}).
	 */
	public byte getXDVersion();

	/** Get XML version of X-definition source.
	 * @return XML version of X-definition source (10 .. 1.0, 11 .. 1.1).
	 */
	public byte getXmlVersion();

}