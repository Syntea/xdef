package org.xdef.impl;

import org.xdef.impl.code.DefContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.parsers.XDParseEnum;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeTable;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import java.io.IOException;
import java.util.ArrayList;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;

/** Implementation of the model of attributes or text nodes.
 * @author Vaclav Trojan
 */
public class XData extends XCodeDescriptor implements XMData, XDValueID {
	/** Type ID of value of data. */
	private short _baseType;
	/** Type name of value of data. */
	private String _valueTypeName;

	/** Creates a new instance of XData.
	 * @param name The name of attribute ("$text" if the type is text node).
	 * @param nsUri NameSpace URI.
	 * @param xp Refers to the XDefPool object.
	 * @param kind <tt>ATTRIBUTE</tt> or <tt>TEXT</tt>.
	 */
	public XData(final String name,
		String nsUri,
		final XDPool xp,
		final short kind) {
		super(name, nsUri, xp, kind);
		setOccurrence(1,1); //???
	}

	public XData(final XData x) {
		super(x);
		setOccurrence(x.minOccurs(), x.maxOccurs());
		_baseType = x._baseType;
		_valueTypeName = x._valueTypeName;
	}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefintion node.
	 */
	public final XMDefinition getXMDefinition() {return null;} //TODO!

	@Override
	// can't be final, can be overwritten!
	public void writeXNode(final XDWriter xw,
		final ArrayList<XNode> list) throws IOException {
		writeXCodeDescriptor(xw);
		xw.writeShort(_baseType);
		xw.writeString(_valueTypeName);
	}

	// can't be final, can be overwritten!
	static XData readXData(final XDReader xr,
		final short kind,
		final XDefinition xd)
		throws IOException {
		String name = xr.readString();
		String uri = xr.readString();
		XData x = new XData(name, uri, xd.getDefPool(), kind);
		x.readXCodeDescriptor(xr);
		x._baseType = xr.readShort();
		x._valueTypeName = xr.readString();
		return x;
	}

	@Override
	public final XDParseResult validate(final String value) {
		return null;
	}

	@Override
	/** Get value specified as default.
	 * @return string with value specified as default or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public final String getDefaultValue() {
		if (_deflt < 0) {
			return null;
		}
		XDValue[] code = ((XPool) getXDPool()).getCode();
		XDValue x = code[_deflt];
		if (x.getCode() != CodeTable.LD_CONST
			|| _deflt + 1 >= code.length
			|| code[_deflt + 1].getCode() != CodeTable.STOP_OP) {
			return null;
		}
		return x.toString();
	}

	@Override
	/** Get value specified as fixed.
	 * @return string with value specified as fixed or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public final String getFixedValue() {
		if (_onAbsence < 0) {
			return null;
		}
		// find code of value parser
		XDValue[] code = ((XPool) getXDPool()).getCode();
		XDValue x = code[_onAbsence];
		if (x.getCode() != CodeTable.CALL_OP
			|| _onAbsence + 2 >= code.length) {
			return null;
		}
		int j = x.getParam();
		if (j < 0 || j + 2 >= code.length
			|| code[j].getCode() != CodeTable.INIT_NOPARAMS_OP
			|| code[j + 1].getCode() != CodeTable.LD_CONST
			|| code[j + 2].getCode() != CodeTable.RETV_OP) {
			return null;
		}
		if (code[_onAbsence + 1].getCode() != CodeTable.SET_TEXT
			|| code[_onAbsence + 2].getCode() != CodeTable.STOP_OP) {
			return null;
		}
		return code[j + 1].toString();
	}

	@Override
	/** Get values of enumeration type (list, tokens, eq, string).
	 * @return string array with values specified as enumeration or return
	 * <tt>null</tt> if specified type is not enumeration of string values.
	 */
	public final String[] getEnumerationValues() {
		if (_check < 0) {
			return null;
		}
//		XDValue[] code = getXDPool().getCode();
//		if (code[_check].getCode() == CodeTable.LD_CONST
//			&& code[_check].getItemId() == XD_PARSER) {
//			XDParser p = (XDParser) code[_check];
		XDValue code = getParseMethod();
		if (code.getCode() == CodeTable.LD_CONST
			&& code.getItemId() == XD_PARSER) {
			XDParser p = (XDParser) code;
			XDContainer pars = p.getNamedParams();
			if (pars == null) {
				return null;
			}
			XDNamedValue n;
			String name = p.parserName();
			n = "list".equals(name) || "tokens".equals(name) ?
				pars.getXDNamedItem("argument") :
				"string".equals(name) ?
				pars.getXDNamedItem("enumeration") :
				null;
			if (n == null) {
				return null;
			}
			XDValue v;
			if ((v = n.getValue()) != null && !v.isNull()) {
				if (v.getItemId() == XD_CONTAINER) {
					XDContainer c = (XDContainer) v;
					XDValue[] vv = c.getXDItems();
					int num = vv == null ? 0 : vv.length;
					if (num == 0) {
						return null;
					}
					String[] result = new String[num];
					for (int i = 0; i < num; i++) {
						result[i] = "" + vv[i];
					}
					return result;
				} else {
					String s;
					if ((s = v.toString()) == null || s.length() == 0) {
						return null;
					}
					if ("tokens".equals(name)) {
						// Convert string to array of strings
						XDContainer context = XDParseEnum.tokensToContext(s);
						int num = context.getXDItemsNumber();
						if (num == 0) {
							return null;
						}
						String[] result = new String[num];
						for(int i=0; i < num; i++) {
							result[i] = context.getXDItem(i).toString();
						}
						return result;
					} else {
						return new String[] {s};
					}
				}
			}
		}
		return null;
	}

	@Override
	/** Get type of value.
	 * @return type ID of data value.
	 */
	public final short getBaseType() {return _baseType;}

	@Override
	/** Get type name of value.
	 * @return type name of data value.
	 */
	public final String getValueTypeName() {return _valueTypeName;}

	@Override
	/** Get parser used for parsing of value.
	 * @return XDParser or null if parser is not available.
	 */
	public final XDValue getParseMethod() {
		XDValue[] xv = ((XPool) getXDPool()).getCode();
		return getParseMethod(xv);
	}

	public final XDValue getParseMethod(final XDValue[] xv) {
		int xs = _check; //start of code
		if (xs < 0) {
			return null;
		}
		XDValue y = xv[xs];
		if (y.getCode() == CodeTable.JMP_OP
			|| (xs + 1 < xv.length
				&& y.getCode() == CodeTable.CALL_OP
				&& xv[xs+1].getCode() == CodeTable.STOP_OP)) {
			y = xv[xs = y.getParam()];
		} else if (xs + 2 < xv.length
			&& (y.getCode() == CodeTable.LD_GLOBAL
				|| y.getCode() == CodeTable.LD_XMODEL)
			&& (xv[xs+1].getCode() == CodeTable.UNIQUESET_KEY_SETKEY
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_KEY_ID
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_KEY_SET
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_KEY_IDREF
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_KEY_CHKID
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_ID
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_SET
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_IDREF
				|| xv[xs+1].getCode() == CodeTable.UNIQUESET_CHKID)
			&& xv[xs+2].getCode() == CodeTable.STOP_OP) {
			y = xv[xs = xv[xs+1].intValue()]; // this should be parser
		}
		for (;;) {
			if (y.getCode() == CodeTable.JMP_OP) {
				y = xv[xs = xv[xs].getParam()];
			} else if (y.getCode() == CodeTable.CALL_OP) {
				if (y.getParam() >= 0 && xs + 3 < xv.length
					&& xv[xs+1].getCode() == CodeTable.NEW_PARSER
					&& "eq".equals(xv[xs+1].stringValue())
					&& xv[xs+2].getCode() == CodeTable.PARSEANDCHECK
					&& xv[xs+3].getCode() == CodeTable.STOP_OP) {
					return ((CodeParser) xv[xs+1]).getParser(); // fixed
				} else {
					y = xv[xs = y.getParam()];
					if (y.getCode() == CodeTable.LD_CONST
						&& y.getItemId() == XDValueID.XD_PARSER
						&& xv[xs+1].getCode() == CodeTable.PARSE_OP
						&& xv[xs+2].getCode() == CodeTable.STOP_OP) {
						return y;
					}
				}
			} else if (xs + 2 < xv.length
				&& y.getCode() == CodeTable.LD_CONST
				&& y.getItemId() == XDValueID.XD_PARSER
				&& xv[xs+1].getCode() == CodeTable.PARSE_OP
				&& xv[xs+2].getCode() == CodeTable.STOP_OP) {
				return y; // we found parser and parse operation
			} else {
				return null;
			}
		}
	}

	@Override
	/** Get parameters of parsing method.
	 * @return XDParser or null if parser is not available.
	 */
	public final XDContainer getParseParams() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XDValueID.XD_PARSER
			? ((XDParser) p).getNamedParams() : new DefContainer();
	}

	@Override
	/** Get type of parsed value.
	 * @return value from org.xdef.XDValueTypes.
	 */
	public final short getParserType() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XDValueID.XD_PARSER
			? ((XDParser) p).parsedType() : getBaseType();
	}

	@Override
	/** Get datetime mask from the model parser.
	 * @return mask of datetime type or <tt>null</tt>.
	 */
	public final String getDateMask() {
		XDValue p = getParseMethod();
		if (p != null && p.getItemId()==XDValueID.XD_PARSER) {
			XDParser y = (XDParser) p;
			if ("xdatetime".equals(y.parserName())) {
				XDContainer c = y.getNamedParams();
				for (XDNamedValue item: c.getXDNamedItems()) {
					if ("format".equals(item.getName())) {
						return '"' + item.getValue().toString() + '"';
					}
				}
				return "\"yyyy-MM-ddTHH:mm:ss[.S][Z]\"";
			} else if ("dateTime".equals(y.parserName())) {
				return "\"y-MM-ddTHH:mm:ss[.S][Z]\"";
			} else if ("date".equals(y.parserName())) {
				return "\"y-MM-dd[Z]\"";
			} else if ("gDay".equals(y.parserName())) {
				return "\"---dd[Z]\"";
			} else if ("gMonth".equals(y.parserName())) {
				return "\"--MM[Z]\"";
			} else if ("gMonthDay".equals(y.parserName())) {
				return "\"--MM-dd[Z]\"";
			} else if ("\"gYear".equals(y.parserName())) {
				return "\"y[Z]\"";
			} else if ("ISOyear".equals(y.parserName())) {
				return "\"y[Z]\"";
			} else if ("gYearMonth".equals(y.parserName())) {
				return "\"y-MM[Z]\"";
			} else if ("ISOyearMonth".equals(y.parserName())) {
				return "\"y-MM[Z]\"";
			} else if ("dateYMDhms".equals(y.parserName())) {
				return "\"yyyyMMddHHmmss\"";
			} else if ("ISOdate".equals(y.parserName())) {
				return "\"y-MM-dd[Z]\"";
			} else if ("time".equals(y.parserName())) {
				return "\"HH:mm:ss[.S][Z]\"";
			} else if ("emailDate".equals(y.parserName())) {
				return "\"EEE, d MMM y HH:mm:ss[ ZZZZZ][ (z)]\"";
			}
		}
		return null;
	}

	@Override
	/** Get name parser (i.e. "base64Binary" or "hexBinary").
	 * @return name of parser or empty string.
	 */
	public final String getParserName() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XDValueID.XD_PARSER
			? ((XDParser) p).parserName() : "";
	}

	/** Set type of value.
	 * @param valueType ID of data value type.
	 * @param valueTypeName Name of data value type.
	 */
	public final void setValueType(final short valueType,
		final String valueTypeName) {
		_baseType = valueType;
		_valueTypeName = valueTypeName;
	}

	@Override
	/** Add node as child.
	 * @param xnode The node to be added.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final void addNode(final XNode xnode) {
		throw new SRuntimeException(SYS.SYS066, //Internal error: &{0}
			"Attempt to add node to ScriptCodeDescriptor");
	}
}