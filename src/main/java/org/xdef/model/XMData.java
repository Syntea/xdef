package org.xdef.model;

import org.xdef.XDValue;
import org.xdef.XDContainer;

/** Interface of model of XML text data (attributes, text nodes).
 * @author Vaclav Trojan
 */
public interface XMData extends XMNode {

	/** Get value specified as default.
	 * @return string with value specified as default or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public String getDefaultValue();

	/** Get value specified as fixed.
	 * @return string with value specified as fixed or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public String getFixedValue();

	/** Get type name of value.
	 * @return type name of data value.
	 */
	public String getValueTypeName();

	/** Get reference name to declared type.
	 * @return reference name to declared type or null if not reference.
	 */
	public String getRefTypeName();

	/** Check if the value type is declared as local within the X-definition.
	 * @return true if the value type is declared as local within
	 * the X-definition.
	 */
	public boolean isLocalType();

	/** Get parser used for parsing of value.
	 * @return XDParser or null if parser is not available.
	 */
	public XDValue getParseMethod();

	/** Get parameters of parsing method.
	 * @return XDParser or null if parser is not available.
	 */
	public XDContainer getParseParams();

	/** Get type of parsed value.
	 * @return value from org.xdef.XDValueTypes.
	 */
	public short getParserType();

	/** Get datetime mask from the model parser.
	 * @return mask of datetime type or <tt>null</tt>.
	 */
	public String getDateMask();

	/** Get name parser (i.e. string, "base64Binary", "hexBinary", float etc.).
	 * @return name of parser or empty string.
	 */
	public String getParserName();

}