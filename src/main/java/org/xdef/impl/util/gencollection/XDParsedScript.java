package org.xdef.impl.util.gencollection;

import org.xdef.sys.SBuffer;
import org.xdef.impl.code.DefString;
import org.xdef.impl.XOccurrence;
import org.xdef.impl.compile.XScriptParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.XConstants;

/** Parse script of X-definition.
 * @author Trojan
 */
public class XDParsedScript {
	/** Parsed "$$$script" or empty string.*/
	public String _script = "";
	/** Parsed template or empty string.*/
	public String _template = "";
	/** Parsed var section or empty string.*/
	public String _var = "";
	/** Parsed options (source string).*/
	public String _options = "";
	/** Parsed _onTrue action (source string).*/
	public String _onTrue = "";
	/** Parsed _onFalse action (source string).*/
	public String _onFalse = "";
	/** Parsed onAbsence action (source string).*/
	public String _onAbsence = "";
	/** Parsed onExcess action (source string).*/
	public String _onExcess = "";
	/** Parsed onIllegalAttr action (source string).*/
	public String _onIllegalAttr = "";
	/** Parsed create action (source string).*/
	public String _create = "";
	/** Parsed init action (source string).*/
	public String _init = "";
	/** Parsed default action (source string).*/
	public String _default = "";
	/** Parsed finally action (source string).*/
	public String _finally = "";
	/** Parsed onStartElement action (source string).*/
	public String _onStartElement = "";
	/** Parsed match action (source string).*/
	public String _match = "";
	/** Parsed forget (source string).*/
	public String _forget = "";
	/** Parsed type validation action (source string).*/
	public String _type = "";
	/** Parsed reference (source string).*/
	public String _reference = "";
	/** Parsed occurrence (source string).*/
	public final XOccurrence _xOccurrence = new XOccurrence();
	/** If true then parsed string corresponds to an attribute or to a text. */
	public final boolean _isValue;

	/** Create instance of parsed script.
	 * @param sp script parser.
	 * @param isValue if true the script corresponds to a value of an attribute
	 * or of a text node.
	 */
	public XDParsedScript(final XScriptParser sp, final boolean isValue) {
		_isValue = isValue;
		if (sp.getIndex() == 0 && sp._sym == XScriptParser.NOCHAR) {
			if (sp.isToken("$$$script:")) {
				_script = "$$$script: ";
			}
			sp.nextSymbol();
		}
		while (sp._sym != XScriptParser.NOCHAR) {
			if (!isSectionName(sp)) {
				if (sp.isOccurrence(_xOccurrence)) {
					if (_isValue) {
						parseTypeSection(sp);
					}
					continue;
				} else if (_isValue) {
					if (sp._sym == XScriptParser.IDENTIFIER_SYM
						|| sp._sym == XScriptParser.LPAR_SYM) {
						parseTypeSection(sp);
						continue;
					} else if (sp._sym == XScriptParser.CONSTANT_SYM &&
						sp._parsedValue.getItemId()==XScriptParser.XD_STRING) {
						_xOccurrence.setFixed();
						_default = '\'' + sp._parsedValue.toString() + '\'';
						sp.nextSymbol();
						continue;
					}
				}
			}
			char sym = sp._sym;
			sp.nextSymbol();
			switch (sym) {
				case XScriptParser.SEMICOLON_SYM:
					continue;
				case XScriptParser.OPTIONS_SYM:
					while (sp._sym == XScriptParser.IDENTIFIER_SYM ||
						sp._sym == XScriptParser.FORGET_SYM) {
						if (_options.length() > 0 ) {
							_options += ",";
						}
						_options += sp._sym == XScriptParser.IDENTIFIER_SYM ?
							sp._idName : "forget";
						sp.nextSymbol();
						if (sp._sym != XScriptParser.COMMA_SYM) {
							break;
						}
					}
					continue;
				case XScriptParser.OCCURS_SYM:
					sp.isOccurrenceInterval(_xOccurrence);
					continue;
				case XScriptParser.FIXED_SYM: {
					_xOccurrence.setFixed();
					_default = parseScriptSection(sp);
					continue;
				}
				case XScriptParser.ON_TRUE_SYM:
					_onTrue = parseScriptSection(sp);
					continue;
				case XScriptParser.ON_FALSE_SYM:
					_onFalse = parseScriptSection(sp);
					continue;
				case XScriptParser.ON_ABSENCE_SYM:
					_onAbsence = parseScriptSection(sp);
					continue;
				case XScriptParser.ON_ILLEGAL_ATTR_SYM:
					_onIllegalAttr = parseScriptSection(sp);
					continue;
				case XScriptParser.CREATE_SYM:
					_create = parseScriptSection(sp);
					continue;
				case XScriptParser.INIT_SYM:
					_init = parseScriptSection(sp);
					continue;
				case XScriptParser.DEFAULT_SYM:
					_xOccurrence.setOptional();
					_default = parseScriptSection(sp);
					continue;
				case XScriptParser.FINALLY_SYM:
					_finally = parseScriptSection(sp);
					continue;
				case XScriptParser.ON_START_ELEMENT_SYM:
					_onStartElement = parseScriptSection(sp);
					continue;
				case XScriptParser.MATCH_SYM:
					_match = parseScriptSection(sp);
					continue;
				case XScriptParser.ON_EXCESS_SYM: //only for xd:attrs or text
					_onExcess = parseScriptSection(sp);
					continue;
				case XScriptParser.REFERENCE_SYM:
					_reference = sp._idName;
					if (_reference.indexOf('#') < 0) {
						_reference = sp._actDefName + '#' + _reference;
					}
					sp.nextSymbol();
					continue;
				case XScriptParser.VAR_SYM:
					_var = parseScriptSection(sp);
					continue;
				case XScriptParser.FORGET_SYM:
					_forget = "forget";
			}
		}
	}

	/** Get canonized form of script.
	 * @param removeActions if true all actions except validation are removed.
	 * @return string with canonized script.
	 */
	final String getCanonizedScript(final boolean removeActions) {
		String result = getxOccurrence().toString(_isValue);
		if (_xOccurrence.isFixed()) {
			if (_type != null && !_type.isEmpty()) {
				result =  _type + "; " + result + " " + _default;
			} else {
				result += " " + _default;
			}
		} else {
			if (_isValue) {
				if (_type != null && !_type.isEmpty()) {
					result +=  " " +  _type;
				}
			}
			if (_default.length() > 0) { // default value
				if (result.equals("optional")) {
					result = "default " + _default;
				} else {
					result += "; default " + _default;
				}
			}
		}
		if (!_script.isEmpty()) {
			result = _script + result;
		}
		result = addSection(result, "ref ", _reference);
		result = addSection(result, "match ", _match);
		result = addSection(result, "init ", _init);
		result = addSection(result, "options ", _options);
		if (removeActions) { //only validation
			return result;
		}
		if (!_var.isEmpty()) {
			String s = !_var.endsWith(";") && !_var.endsWith("}") ? ";" : "";
			result = "var " + _var + s + result;
		}
		result = addSection(result, "onTrue ", _onTrue);
		result = addSection(result, "onFalse ", _onFalse);
		result = addSection(result, "onStartElement ", _onStartElement);
		result = addSection(result, "onAbsence ", _onAbsence);
		result = addSection(result, "onExcess ", _onExcess);
		result = addSection(result, "onIllegalAttr ", _onIllegalAttr);
		result = addSection(result, "create ", _create);
		result = addSection(result, "finally ", _finally);
		result = addSection(result, "forget ", _forget);
		return result;
	}

	private static String addSection(final String result,
		final String name,
		final String content) {
		if (!content.isEmpty()) {
			return result
				+ (!result.endsWith(";") && !result.endsWith("}") ? ";" : "")
				+ name + content;
		}
		return result;
	}

	private static String symToString(final XScriptParser sp) {
		switch (sp._sym) {
			case XScriptParser.NOT_SYM:
				return "!";
			case XScriptParser.AND_SYM:
				return " AND ";
			case XScriptParser.AAND_SYM:
				return " AAND ";
			case XScriptParser.OR_SYM:
				return "|";
			case XScriptParser.OOR_SYM:
				return "||";
			case XScriptParser.GT_SYM:
				return " GT ";
			case XScriptParser.GE_SYM:
				return " GE ";
			case XScriptParser.LT_SYM:
				return " LT ";
			case XScriptParser.LE_SYM:
				return " LE ";
			case XScriptParser.EQ_SYM:
				return "==";
			case XScriptParser.NE_SYM:
				return "!=";
			case XScriptParser.ASSGN_SYM:
				return "=";
			case XScriptParser.MUL_SYM:
				return "*";
			case XScriptParser.DIV_SYM:
				return "/";
			case XScriptParser.COLON_SYM:
				return ":";
			case XScriptParser.MOD_SYM:
				return "%";
			case XScriptParser.PLUS_SYM:
				return "+";
			case XScriptParser.MINUS_SYM:
				return "-";
			case XScriptParser.INC_SYM:
				return "++";
			case XScriptParser.DEC_SYM:
				return "--";
			case XScriptParser.TYPE_SYM:
			case XScriptParser.UNIQUE_SET_SYM:
			case XScriptParser.ON_ABSENCE_SYM:
			case XScriptParser.ON_TRUE_SYM:
			case XScriptParser.ON_EXCESS_SYM:
			case XScriptParser.ON_FALSE_SYM:
				return XScriptParser.symToName(sp._sym) + ' ';
			default:
				return XScriptParser.symToName(sp._sym);
		}
	}

	private static boolean isSectionName(final XScriptParser sp) {
		switch (sp._sym) {
			case XScriptParser.DEFAULT_SYM:
				return true;
			case XScriptParser.CREATE_SYM:
			case XScriptParser.FINALLY_SYM:
			case XScriptParser.FIXED_SYM:
			case XScriptParser.FORGET_SYM:
			case XScriptParser.INIT_SYM:
			case XScriptParser.MATCH_SYM:
			case XScriptParser.ON_ABSENCE_SYM:
			case XScriptParser.ON_EXCESS_SYM:
			case XScriptParser.ON_FALSE_SYM:
			case XScriptParser.ON_ILLEGAL_ATTR_SYM:
			case XScriptParser.ON_ILLEGAL_ELEMENT_SYM:
			case XScriptParser.ON_ILLEGAL_ROOT_SYM:
			case XScriptParser.ON_ILLEGAL_TEXT_SYM:
			case XScriptParser.ON_START_ELEMENT_SYM:
			case XScriptParser.ON_TRUE_SYM:
			case XScriptParser.ON_XML_ERROR_SYM:
			case XScriptParser.VAR_SYM:
				return true;
		}
		return false;
	}

	/** Parse type section.
	 * @param sp script parser.
	 * @return string with canonized script.
	 */
	private void parseTypeSection(final XScriptParser sp) {
		if (sp._sym == XScriptParser.BEG_SYM) {
			sp.nextSymbol();
			_type = "{";
			while (sp._sym != XScriptParser.END_SYM
				&& sp._sym != XScriptParser.NOCHAR
				&& !isSectionName(sp)) {
				_type += parseScriptSection(sp);
				if (sp._sym == XScriptParser.SEMICOLON_SYM) {
					sp.nextSymbol();
					_type += ';';
				}
			}
			if (sp._sym == XScriptParser.END_SYM) {
				sp.nextSymbol();
				_type += "}";
			}
		} else {
			_type = "";
			StringBuilder sb = new StringBuilder();
			boolean spaceNeeded = false;
			while (sp._sym != XScriptParser.NOCHAR
				&& sp._sym != XScriptParser.SEMICOLON_SYM
				&& !isSectionName(sp)) {
				switch (sp._sym) {
					case XScriptParser.IDENTIFIER_SYM:
						if (spaceNeeded) {
							sb.append(' '); //must be separated by space
						}
						sb.append(sp._idName);
						spaceNeeded = true;
						break;
					case XScriptParser.CONSTANT_SYM:
						if (sp._parsedValue.getItemId() ==
							XScriptParser.XD_STRING) {
								sb.append(((DefString) sp._parsedValue)
									.sourceValue());
						} else {
							if (spaceNeeded) {
								sb.append(' '); //must be separated by space
							}
							sb.append(sp._parsedValue.toString());
							spaceNeeded = true;
						}
						break;
					default:
						spaceNeeded = false;
						sb.append(symToString(sp));
				}
				sp.nextSymbol();
			}
			_type += sb.toString();
		}
	}

	/** Parse script section.
	 * @param sp script parser.
	 * @return string with canonized script.
	 */
	private static String parseScriptSection(final XScriptParser sp) {
		StringBuilder sb = new StringBuilder();
		if (sp._sym == XScriptParser.TRY_SYM) {
			sb.append("try ");
			sp.nextSymbol();
			sb.append(parseScriptSection(sp));
			if (sp._sym == XScriptParser.END_SYM) {
				sb.append("}");
				sp.nextSymbol();
			}
			if (sp._sym == XScriptParser.CATCH_SYM) {
				sb.append("catch");
				sp.nextSymbol();
				sb.append(parseScriptSection(sp));
				if (sp._sym == XScriptParser.END_SYM) {
					sb.append("}");
					sp.nextSymbol();
				}
				if (sp._sym == XScriptParser.BEG_SYM) {
					sb.append(parseScriptSection(sp));
				}
			}
			return sb.toString();
		} else if (sp._sym == XScriptParser.SWITCH_SYM) {
			sp.nextSymbol();
			sb.append("switch").append(parseScriptSection(sp));
			if (sp._sym == XScriptParser.BEG_SYM) {
				sb.append("{");
				sp.nextSymbol();
				for (;;) {
					if (sp._sym == XScriptParser.CASE_SYM) {
						sp.nextSymbol();
						sb.append("case ").append(parseScriptSection(sp));
						sb.append(parseScriptSection(sp));
					} else if (sp._sym == XScriptParser.DEFAULT_SYM) {
						sp.nextSymbol();
						if (sp._sym == XScriptParser.COLON_SYM) {
							sp.nextSymbol();
						}
						sb.append("default: ").append(parseScriptSection(sp));
					} else {
						break;
					}
				}
				if (sp._sym == XScriptParser.END_SYM) {
					sp.nextSymbol();
					sb.append("}");
				}
			}
			return sb.toString();
		} else if (sp._sym == XScriptParser.IF_SYM) {
			sb.append("if");
			sp.nextSymbol();
			sb.append(parseScriptSection(sp));
			if (sp._sym == XScriptParser.END_SYM) {
				sb.append("}");
				sp.nextSymbol();
			}
		}
		if (sp._sym == XScriptParser.BEG_SYM) {
			sp.nextSymbol();
			sb.append("{");
			while (sp._sym != XScriptParser.END_SYM &&
				sp._sym != XScriptParser.NOCHAR) {
				if (isSectionName(sp)) {
					return sb.toString();
				}
				if (sp._sym == XScriptParser.TRY_SYM) {
					sb.append(parseScriptSection(sp));
				} else if (sp._sym == XScriptParser.IF_SYM) {
					sb.append(parseScriptSection(sp));
				} else if (sp._sym == XScriptParser.SWITCH_SYM) {
					sb.append(parseScriptSection(sp));
				}
				if (sp._sym == XScriptParser.BEG_SYM) {
					sb.append("{");
					sp.nextSymbol();
					sb.append(parseScriptSection(sp));
					if (sp._sym == XScriptParser.END_SYM) {
						sb.append("}");
						sp.nextSymbol();
					}
				} else {
					sb.append(parseScriptSection(sp));
				}
			}
			if (sp._sym == XScriptParser.END_SYM) {
				sb.append("}");
				sp.nextSymbol();
			}
			if (isSectionName(sp)) {
				return sb.toString();
			}
			if (sp._sym == XScriptParser.RETURN_SYM) {
				sb.append(parseScriptSection(sp));
				if (sp._sym == XScriptParser.END_SYM) {
					sb.append("}");
					sp.nextSymbol();
				}
			}
			return sb.toString();
		} else {
			boolean spaceNeeded = false;
			while (sp._sym != XScriptParser.NOCHAR
				&& sp._sym != XScriptParser.END_SYM
				&& sp._sym != XScriptParser.SEMICOLON_SYM
				&& !isSectionName(sp)) {
				switch (sp._sym) {
					case XScriptParser.BEG_SYM:
						return sb.toString();
					case XScriptParser.IF_SYM:
					case XScriptParser.TRY_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(symToString(sp));
						return sb.toString();
					case XScriptParser.SWITCH_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(symToString(sp));
						return sb.toString();
					case XScriptParser.CASE_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(symToString(sp));
						return sb.toString();
					case XScriptParser.IDENTIFIER_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(sp._idName);
						spaceNeeded = true;
						break;
					case XScriptParser.CONSTANT_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						if (sp._parsedValue.getItemId() ==
							XScriptParser.XD_STRING) {
							sb.append(
								((DefString) sp._parsedValue).sourceValue());
						} else {
							sb.append(sp._parsedValue.toString());
						}
						spaceNeeded = true;
						break;
					case XScriptParser.THROW_SYM:
					case XScriptParser.NEW_SYM:
					case XScriptParser.NULL_SYM:
					case XScriptParser.RETURN_SYM:
					case XScriptParser.ELSE_SYM:
					case XScriptParser.COLON_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(symToString(sp));
						spaceNeeded = true;
						break;
					case XScriptParser.ATCHAR_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append('@').append(sp._idName);
						spaceNeeded = true;
						break;
					case XScriptParser.CATCH_SYM:
						if (spaceNeeded) {
							sb.append(' ');
						}
						sb.append(symToString(sp));
						break;
					default:
						spaceNeeded = false;
						sb.append(symToString(sp));
				}
				sp.nextSymbol();
			}
			if (sp._sym == XScriptParser.SEMICOLON_SYM
				|| sp._sym == XScriptParser.END_SYM) {
				sb.append(symToString(sp));
				sp.nextSymbol();
			}
			return sb.toString();
		}
	}

	/** Check if script contains an option specified by the argument.
	 * @param option string with a name of option.
	 * @return true if and only if the script contains the option.
	 */
	public final boolean hasOption(final String option) {
		int optionsLen;
		if ((optionsLen = getOptions().length()) == 0) {
			return false;
		}
		int i = 0;
		int oLen = option.length();
		if ((i = getOptions().indexOf(option, i)) >= 0) {
			if ((i == 0 || getOptions().charAt(i-1) == ',') &&
				(i + oLen == optionsLen || getOptions().charAt(i+oLen) == ',')) {
				return true;
			}
		}
		return false;
	}

	/** Create XdParsedScript object from the script of the node.
	 * @param n Node.
	 * @return XdParsedScript object created from script of the node.
	 */
	public static final XDParsedScript getXdScript(final Node n) {
		String defName = "";
		String xdUri = null;
		Node n1 = n.getNodeType() == Node.ATTRIBUTE_NODE ?
			((Attr) n).getOwnerElement(): n.getParentNode();
		boolean isValue = !"script".equals(n.getLocalName());
		while (n1 != null && n1.getNodeType() == Node.ELEMENT_NODE) {
			if ("def".equals(n1.getLocalName())) {
				Node n2 = n1.getParentNode();
				if (n2 != null && "collection".equals(n2.getLocalName()) &&
					(n2.getParentNode() == null ||
					n2.getParentNode().getNodeType() != Node.ELEMENT_NODE)) {
					xdUri = n1.getNamespaceURI();
					if (xdUri != null) {
						defName = XDGenCollection.getXdefAttr(
							(Element) n1, xdUri, "name", false);
						isValue = !xdUri.equals(n.getNamespaceURI()) ||
							!"script".equals(n.getLocalName());
					}
					break;
				}
			}
			n1 = n1.getParentNode();
		}
		String s = (n.getNodeType() == Node.ELEMENT_NODE)
			? XDGenCollection.getXdefAttr(
				(Element) n, xdUri, "script", false)
			: n.getNodeValue();
		return s == null || (s = s.trim()).length() == 0
			? null : getXdScript(s, defName, isValue);
	}

	/** Create XdParsedScript object from the script.
	 * @param script string with script.
	 * @param defName name of X-definition.
	 * @param isValue if <tt>true</tt> then script describes text value.
	 * @return XdParsedScript object created from the script.
	 */
	public static final XDParsedScript getXdScript(final String script,
		final String defName,
		final boolean isValue) {
		XScriptParser sp = new XScriptParser(XConstants.XML10);
		sp.setSource(new SBuffer(script), defName, XConstants.XD20);
		return new XDParsedScript(sp, isValue);
	}

	/** @return the _xOccurrence */
	public final XOccurrence getxOccurrence() {return _xOccurrence;}

	public final String getOptions() {return _options;}

	public final String getOnTrue() {return _onTrue;}

	public final String getOnFalse() {return _onFalse;}

	public final String getOnAbsence() {return _onAbsence;}

	public final String getOnExcess() {return _onExcess;}

	public final String getOnIllegalAttr() {return _onIllegalAttr;}
}