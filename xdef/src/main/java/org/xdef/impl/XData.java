package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeTable;
import static org.xdef.impl.code.CodeTable.CALL_OP;
import static org.xdef.impl.code.CodeTable.INIT_NOPARAMS_OP;
import static org.xdef.impl.code.CodeTable.JMPEQ;
import static org.xdef.impl.code.CodeTable.JMPF_OP;
import static org.xdef.impl.code.CodeTable.JMPGE;
import static org.xdef.impl.code.CodeTable.JMPGT;
import static org.xdef.impl.code.CodeTable.JMPLE;
import static org.xdef.impl.code.CodeTable.JMPLT;
import static org.xdef.impl.code.CodeTable.JMPNE;
import static org.xdef.impl.code.CodeTable.JMPT_OP;
import static org.xdef.impl.code.CodeTable.JMP_OP;
import static org.xdef.impl.code.CodeTable.LD_CODE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.LD_GLOBAL;
import static org.xdef.impl.code.CodeTable.LD_XMODEL;
import static org.xdef.impl.code.CodeTable.NEW_PARSER;
import static org.xdef.impl.code.CodeTable.PARSE_OP;
import static org.xdef.impl.code.CodeTable.RETV_OP;
import static org.xdef.impl.code.CodeTable.SET_TEXT;
import static org.xdef.impl.code.CodeTable.STOP_OP;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKIDS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREFS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SETKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_SET;
import org.xdef.impl.code.DefContainer;
import static org.xdef.impl.compile.CompileBase.getTypeName;
import org.xdef.impl.parsers.XDParseCDATA;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;

/** Implementation of the model of attributes or text nodes.
 * @author Vaclav Trojan
 */
public class XData extends XCodeDescriptor
	implements XMData, XDValueID, CodeTable {
	/** Default parser. */
	private static final XDParseCDATA DEFAULT_PARSER = new XDParseCDATA();
	/** Type ID of value of data. */
	short _valueType;
	/** Type name of value of data. */
	private String _valueTypeName;
	/** Referenced type (null if not reference). */
	private String _refTypeName;

	/** Creates a new instance of XData.
	 * @param name The name of attribute ("$text" if the type is text node).
	 * @param nsUri NameSpace URI.
	 * @param xp Refers to the XDefPool object.
	 * @param kind <code>ATTRIBUTE</code> or <code>TEXT</code>.
	 */
	public XData(final String name,
		String nsUri,
		final XDPool xp,
		final short kind) {
		super(name, nsUri, xp, kind);
		setOccurrence(1,1); // required ???
		_valueType = XD_STRING;
	}

	public XData(final XData x) {
		super(x);
		setOccurrence(x.minOccurs(), x.maxOccurs());
		_valueType = x._valueType;
		_valueTypeName = x._valueTypeName;
		_refTypeName = x._refTypeName;
	}

////////////////////////////////////////////////////////////////////////////////
// impemetation of method from interfaces.
////////////////////////////////////////////////////////////////////////////////

	@Override
	// can't be final, can be overwritten!
	public void writeXNode(final XDWriter xw,
		final List<XNode> list) throws IOException {
		writeXCodeDescriptor(xw);
		xw.writeShort(_valueType);
		xw.writeString(_valueTypeName);
		xw.writeString(_refTypeName);
	}

	@Override
	/** Get value specified as default.
	 * @return value specified as default or return <i>null</i>
	 * if there was not specified a default value.
	 */
	public final XDValue getDefaultValue() {
		if (_deflt < 0) {
			return null;
		}
		XDValue[] code = ((XPool) getXDPool()).getCode();
		XDValue x = code[_deflt];
		if (x.getCode() != LD_CONST || _deflt + 1 >= code.length
			|| code[_deflt + 1].getCode() != STOP_OP) {
			return null;
		}
		return x;
	}

	@Override
	/** Get value specified as fixed.
	 * @return value specified as fixed or return <i>null</i>
	 * if there was not specified a default value.
	 */
	public final XDValue getFixedValue() {
		if (_onAbsence < 0) {
			return null;
		}
		// find code of value parser
		XDValue[] code = ((XPool) getXDPool()).getCode();
		XDValue x = code[_onAbsence];
		if (x.getCode() != CALL_OP || _onAbsence + 2 >= code.length) {
			return null;
		}
		int j = x.getParam();
		if (j < 0 || j + 2 >= code.length
			|| code[j].getCode() != INIT_NOPARAMS_OP
			|| code[j + 1].getCode() != LD_CONST
			|| code[j + 2].getCode() != RETV_OP) {
			return null;
		}
		if (code[_onAbsence + 1].getCode() != SET_TEXT
			|| code[_onAbsence + 2].getCode() != STOP_OP) {
			return null;
		}
		return code[j + 1];
	}

	@Override
	/** Get type name of value.
	 * @return type name of data value.
	 */
	public final String getValueTypeName() {
		int xs = _check; //start of code of parse method.
		if (xs >= 0) {
			if (_valueTypeName.indexOf('.') < 0 || _valueTypeName.endsWith("ID")
				|| _valueTypeName.endsWith("IDREF")
				|| _valueTypeName.endsWith("IDREFS")
				|| _valueTypeName.endsWith("CHKID")
				|| _valueTypeName.endsWith("CHKIDS")
				|| _valueTypeName.endsWith("SET")) {
				return _valueTypeName;
			}
			final XDValue[] xv = ((XPool) getXDPool()).getCode();
			XDValue y = xv[xs];
			if (y.getCode() == JMP_OP
				|| (xs + 1 < xv.length && y.getCode() == CALL_OP
					&& xv[xs+1].getCode() == STOP_OP)) {
			} else if (xs + 2 < xv.length
				&& (y.getCode() == LD_GLOBAL || y.getCode() == LD_XMODEL)) {
				String uniquesetName = "";
				switch (xv[xs+1].getCode()) {
					case UNIQUESET_KEY_SET:
					case UNIQUESET_SET: uniquesetName = ".SET"; break;
					case UNIQUESET_KEY_ID:
					case UNIQUESET_ID: uniquesetName = ".ID"; break;
					case UNIQUESET_KEY_IDREF:
					case UNIQUESET_IDREF: uniquesetName = ".IDREF"; break;
					case UNIQUESET_IDREFS: uniquesetName = ".IDREFS"; break;
					case UNIQUESET_KEY_CHKID:
					case UNIQUESET_CHKID: uniquesetName = ".CHKID"; break;
					case UNIQUESET_CHKIDS: uniquesetName = ".CHKIDS"; break;
				}
				return _valueTypeName + uniquesetName;
			}
		}
		return _valueTypeName;
	}

	@Override
	/** Get reference name to declared type.
	 * @return reference name to declared type or null if not reference.
	 */
	public final String getRefTypeName() {return _refTypeName;}

	@Override
	/** Check if the value type is declared as local within the X-definition.
	 * @return true if the value type is declared as local within
	 * the X-definition.
	 */
	public final boolean isLocalType() {
		return getXMDefinition().isLocalName(_refTypeName);
	}

	@Override
	/** Get parser used for parsing of value.
	 * @return XDParser or XDValue of executed code.
	 */
	public final XDValue getParseMethod() {
		if (_check < 0) {
			return DEFAULT_PARSER;
		}
		int xi = _check; //start of code of parse method.
		final XDValue[] xv = ((XPool) getXDPool()).getCode();
		XDValue y = xv[xi];
		if (y.getCode() == JMP_OP || (xi+1 < xv.length && y.getCode() == CALL_OP
				&& xv[xi+1].getCode() == STOP_OP)) {
			y = xv[xi = y.getParam()];
		} else if (xi + 2 < xv.length
			&& (y.getCode() == LD_GLOBAL || y.getCode() == LD_XMODEL)
			&& (xv[xi+1].getCode() == UNIQUESET_KEY_SETKEY
				|| xv[xi+1].getCode() == UNIQUESET_KEY_ID
				|| xv[xi+1].getCode() == UNIQUESET_KEY_SET
				|| xv[xi+1].getCode() == UNIQUESET_KEY_IDREF
				|| xv[xi+1].getCode() == UNIQUESET_KEY_CHKID
				|| xv[xi+1].getCode() == UNIQUESET_ID
				|| xv[xi+1].getCode() == UNIQUESET_SET
				|| xv[xi+1].getCode() == UNIQUESET_IDREF
				|| xv[xi+1].getCode() == UNIQUESET_IDREFS
				|| xv[xi+1].getCode() == UNIQUESET_CHKID
				|| xv[xi+1].getCode() == UNIQUESET_CHKIDS)
			&& xv[xi+2].getCode() == STOP_OP) {
			y = xv[xi = xv[xi+1].intValue()]; // this should be parser
		}
		for (;;) {
			switch (y.getCode()) {
				case JMP_OP:
					y = xv[xi = xv[xi].getParam()];
					break;
				case LD_CODE:
					y = xv[y.getParam()];
					break;
				case CALL_OP:
					if (y.getParam() >= 0 && xi + 3 < xv.length
						&& xv[xi+1].getCode() == NEW_PARSER
						&& "eq".equals(xv[xi+1].stringValue())
						&& xv[xi+2].getCode() == PARSE_OP
						&& xv[xi+3].getCode() == STOP_OP) {
						return ((CodeParser) xv[xi+1]).getParser(); // fixed
					} else {
						y = xv[xi = y.getParam()];
						if (y.getCode()==LD_CONST && y.getItemId()==XD_PARSER
							&& xv[xi+1].getCode() == PARSE_OP
							&& xv[xi+2].getCode() == STOP_OP) {
							return y;
						}
					}	break;
				default:
					if (xi + 2 < xv.length
						&& y.getCode()==LD_CONST && y.getItemId()==XD_PARSER) {
						if (xv[xi+1].getCode() == PARSE_OP
							&& xv[xi+2].getCode() == STOP_OP) {
							return y;
						} else {// ??? try to parse an espression.
							// if all parsers are same return parser
							//  without parameters
							// ???
						}
					}
					return DEFAULT_PARSER;
			}
		}
	}

	@Override
	/** Get parameters of parsing method.
	 * @return XDParser or null if parser is not available.
	 */
	public final XDContainer getParseParams() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XD_PARSER && p instanceof XDParser
			? ((XDParser) p).getNamedParams() : new DefContainer();
	}

	@Override
	/** Get type of parsed value.
	 * @return value from org.xdef.XDValueTypes.
	 */
	public final short getParserType() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XD_PARSER
			&& p instanceof XDParser ? ((XDParser) p).parsedType() : XD_STRING;
	}

	@Override
	/** Get type of parsed value or items of parsed list or union.
	 * @return value from org.xdef.XDValueTypes.
	 */
	public short getAlltemsType() {
		XDValue p = getParseMethod();
		return p != null && p.getItemId() == XD_PARSER
			&& p instanceof XDParser
			? ((XDParser) p).getAlltemsType(): XD_STRING;
	}

	@Override
	/** Get datetime mask from the model parser.
	 * @return mask of datetime type or <i>null</i>.
	 */
	public final String getDateMask() {
		XDValue p = getParseMethod();
		if (p != null && p.getItemId()==XD_PARSER && p instanceof XDParser) {
			XDParser y = (XDParser) p;
			switch (y.parserName()) {
				case "xdatetime": {
					XDContainer c = y.getNamedParams();
					for (XDNamedValue item: c.getXDNamedItems()) {
						if ("format".equals(item.getName())) {
							return '"' + item.getValue().toString() + '"';
						}
					}
					return null;
				}
				case "dateTime": return null;
				case "date": return "\"yyyy-MM-dd[Z]\"";
				case "gDay": return "\"---dd[Z]\"";
				case "gMonth": return "\"--MM[Z]\"";
				case "gMonthDay": return "\"--MM-dd[Z]\"";
				case "gYear": return "\"yyyy[Z]\"";
				case "ISOyear": return "\"yyyy[Z]\"";
				case "gYearMonth": return "\"yyyy-MM[Z]\"";
				case "ISOyearMonth": return "\"yyyy-MM[Z]\"";
				case "dateYMDhms": return "\"yyyyMMddHHmmss\"";
				case "ISOdate": return "\"yyyy-MM-dd[Z]\"";
				case "time": return "\"HH:mm:ss[.S][Z]\"";
				case "emailDate":
					return "\"EEE, d MMM yyyy HH:mm:ss[ ZZZZZ][ (z)]\"";
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
		return p != null && p.getItemId() == XD_PARSER && p instanceof XDParser
			? ((XDParser) p).parserName() : "string";
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

	@Override
	public String toString() {
		String result = getName() + ": type=" + getTypeName(getParserType());
		String s = getRefTypeName();
		if (s != null) {
			result += ", refType=" + s;
		}
		result += ", method=" + getParseMethod();
		XDContainer params = getParseParams();
		if (params != null && params.getXDNamedItemsNumber() > 0) {
			s = "";
			for (XDNamedValue x : params.getXDNamedItems()) {
				if (!s.isEmpty()) {
					s += ",";
				}
				if (x.getValue()!=null && x.getValue() instanceof XDContainer) {
					s += '%' + x.getName() + '=';
					String t = "";
					for (XDValue y: ((XDContainer) x.getValue()).getXDItems()) {
						if (!t.isEmpty()) {
							t += ',';
						}
						t += y;
					}
					s += '[' + t + ']';

				} else {
					s += x;
				}
			}
			result += '[' + s + ']';
		}
		s = getDateMask();
		if (s != null) {
			result += ", date mask=" + s;
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Set type of value.
	 * @param valType ID of data value type.
	 * @param valName Name of data value type.
	 */
	public final void setValueType(final short valType, final String valName) {
		_valueType = valType;
		_valueTypeName = valName;
	}

	/** Set reference name to declared type.
	 * @param x reference name to declared type or null if not reference.
	 */
	public final void setRefTypeName(final String x) {_refTypeName = x;}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return XMDefintion node.
	 */
	public final XMDefinition getXMDefinition() {
		String s = getXDPosition();
		int ndx = s == null ? -1 : s.indexOf("#");
		return getXDPool().getXMDefinition(ndx >= 0 ? s.substring(0, ndx) : s);
	}

	// can't be final, can be overwritten!
	static XData readXData(final XDReader xr,
		final short kind,
		final XDefinition xd)
		throws IOException {
		String name = xr.readString();
		String uri = xr.readString();
		XData x = new XData(name, uri, xd.getXDPool(), kind);
		x.readXCodeDescriptor(xr);
		x._valueType = xr.readShort();
		x._valueTypeName = xr.readString();
		x._refTypeName = xr.readString();
		return x;
	}

	/** Check compatibility of this object and XDData.
	 * @param y XDData object to be compared.
	 * @param rep Reporter where to put errors.
	 * @param full of all must be tested (implements/uses).
	 * @return true if the XDData object from argument is compatible.
	 */
	protected final boolean compareData(final XData y,
		final ArrayReporter rep,
		final boolean full) {
		boolean result = compareNameAndNS(y, rep) &&
			compareOccurrence(y, rep);
		if ("$text".equals(getName())) {
			if (_textValuesCase != y._textValuesCase ||
				_textWhiteSpaces != y._textWhiteSpaces ||
				_textValuesCase != y._textValuesCase ||
				_trimText != y._trimText) {
				//Options differs: &{0} and &{1}
				rep.error(XDEF.XDEF290, getXDPosition(), y.getXDPosition());
				result = false;
			}
		} else {
			if (_attrValuesCase != y._attrValuesCase ||
				_acceptQualifiedAttr != y._acceptQualifiedAttr ||
				_ignoreEmptyAttributes != y._ignoreEmptyAttributes ||
				_attrValuesCase != y._attrValuesCase ||
				_attrWhiteSpaces != y._attrWhiteSpaces ||
				_trimAttr != y._trimAttr) {
				//Options differs: &{0} and &{1}
				rep.error(XDEF.XDEF290, getXDPosition(), y.getXDPosition());
				result = false;
			}
		}
		XDValue[] cx = ((XPool) getXDPool()).getCode();
		XDValue[] cy = ((XPool) y.getXDPool()).getCode();
		if (isFixed()) {
			if (!y.isFixed()) {
				if (y._check>=0||y._onAbsence>=0||y._onFalse>=0) {
					//Default or fixed values differs: &{0} and &{1}
					rep.error(XDEF.XDEF286, getXDPosition(), y.getXDPosition());
					return false;
				}
				if (full || cx != cy) {
					return false;
				} else {
					y._check = _check;
					y._onAbsence = _onAbsence;
					y._onFalse = _onFalse;
					return result;
				}
			}
			if (cx == cy && _check == y._check && _onAbsence == y._onAbsence
				&& _onFalse == y._onFalse) {
				return result;
			} else {
				if (compareCode(cx, cy, _check, y._check, false)
					&& compareCode(cx, cy, _onAbsence, y._onAbsence, false)
					&& compareCode(cx, cy, _onFalse, y._onFalse, false)) {
					return result;
				}
			}
			//Default or fixed values differs: &{0} and &{1}
			rep.error(XDEF.XDEF286, getXDPosition(), y.getXDPosition());
			return false;
		} else if (y.isFixed()) {
			//Default or fixed values differs: &{0} and &{1}
			rep.error(XDEF.XDEF286, getXDPosition(), y.getXDPosition());
			return false;
		}
		XDValue vx, vy;
		if ((vx = getDefaultValue()) != null) {
			if ((vy = y.getDefaultValue()) == null) {
				if (full || cx != cy) {
					result = false;
				} else {
					y._deflt = _deflt;
				}
			} else if (cx != cy || y._deflt != _deflt) {
				if (vx == null || vy == null || !vx.equals(vy)) {
					//Default or fixed values differs: &{0} and &{1}
					rep.error(XDEF.XDEF286, getXDPosition(), y.getXDPosition());
					result = false;
				}
			}
		}
		if ((vx = getFixedValue()) != null) {
			if ((vy = y.getFixedValue()) == null) {
				if (full || cx != cy) {
					result = false;
				} else {
					y._onAbsence = _onAbsence;
				}
			} else if (cx != cy || y._onAbsence != _onAbsence) {
				if (vx == null || vy == null || !vx.equals(vy)) {
					//Default or fixed values differs: &{0} and &{1}
					rep.error(XDEF.XDEF286, getXDPosition(), y.getXDPosition());
					result = false;
				}
			}
		}
		int ix = _check;
		int iy = y._check;
		if (ix == iy) {
			return result;
		}
		if (ix < 0) {
			return iy < 0 && result;
		} else {
			if (iy < 0) {
				if (full || cx != cy) {
					return false;
				}
				y._check = _check;
				return result;
			} else {
				if (compareCode(cx, cy, ix,	iy, full)) {
					return result;
				}
			}
			//Type of value differs: &{0} and &{1}
			rep.error(XDEF.XDEF285, getXDPosition(), y.getXDPosition());
			return false;
		}
	}

	/** Compare code of actions in two nodes.
	 * @param cx code of XDPool x.
	 * @param cy code of XDPool y.
	 * @param x address to code of XDPool x.
	 * @param y address to code of XDPool y.
	 * @param full of all must be tested (implements/uses).
	 * @return true if cods in both objects are compatible. Otherwise
	 * return false.
	 */
	private static boolean compareCode(final XDValue[] cx,
		final XDValue[] cy,
		final int x,
		final int y,
		final boolean full) {
		if (x == y && (x == -1 || cx == cy)) {
			return true;
		}
		int p;
		int ix = x, iy = y;
		XDValue xx,xy;
		while (ix < cx.length && iy < cx.length &&
			(p = (xx = cx[ix]).getCode()) == (xy = cy[iy]).getCode()) {
			switch (p) {
				case STOP_OP:
					return true;
				case CALL_OP: {
					if (cx == cy && xx.getParam() == xy.getParam() ||
						!full && compareCode(cx,
							cy, xx.getParam(), xy.getParam(), full)) {
						ix++;
						iy++;
						continue;
					} else {
						return false;
					}
				}
				case JMPEQ:
				case JMPNE:
				case JMPLE:
				case JMPGE:
				case JMPLT:
				case JMPGT:
				case JMP_OP:
				case JMPF_OP:
				case JMPT_OP: {
					int m = xx.getParam();
					int n = xy.getParam();
					if (m - ix == n - iy) {
						ix++;
						iy++;
					} else {
						ix = Integer.MAX_VALUE;
					}
					continue;
				}
				default:
					if (!xx.equals(xy)) {
						return false;
					}
					ix++;
					iy++;
			}
		}
		return false;
	}
}