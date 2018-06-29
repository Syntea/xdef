/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XdParsedScript.java, created 2009-07-15.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */

package cz.syntea.xdef.util.gencollection;

import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.impl.code.DefString;
import cz.syntea.xdef.impl.XOccurrence;
import cz.syntea.xdef.impl.compile.XScriptParser;
import java.util.ArrayList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Parse script of X-definition.
 * @author Trojan
 */
public class XDParsedScript {
	/** Parsed occurrence (source string).*/
	public XOccurrence _xOccurrence;
	/** Parsed "options" (source string).*/
	public String _options;
	/** Parsed "" action (source string).*/
	public String _onTrue;
	/** Parsed "" action (source string).*/
	public String _onFalse;
	/** Parsed "" action (source string).*/
	public String _onAbsence;
	/** Parsed "" action (source string).*/
	public String _onExcess;
	/** Parsed "onIllegalAttr" action (source string).*/
	public String _onIllegalAttr;
	/** Parsed "create" action (source string).*/
	public String _create;
	/** Parsed "init" action (source string).*/
	public String _init;
	/** Parsed "default" action (source string).*/
	public String _default;
	/** Parsed "finally" action (source string).*/
	public String _finally;
	/** Parsed "match" action (source string).*/
	public String _match;
	/** Parsed type validation action (source string).*/
	public String _type;
	/** List of types as union ("OR" operators). Use ONLY for XML schema!*/
	public String[] _typeList;
	/** Parsed reference (source string).*/
	public String _reference;
	/** If true then parsed string corresponds to an attribute or to a text. */
	public boolean _isValue;

	/** Create instance of parsed script.
	 * @param sp script parser.
	 * @param isValue if true the script corresponds to a value of an attribute
	 * or of a text node.
	 */
	public XDParsedScript(final XScriptParser sp, final boolean isValue) {
		_options = "";
		_onTrue = "";
		_onFalse = "";
		_onAbsence = "";
		_onExcess = "";
		_onIllegalAttr = "";
		_create = "";
		_init = "";
		_default = "";
		_finally = "";
		_match = "";
		_type = "";
		_typeList = new String[0];
		_reference = "";
		_isValue = isValue;
		_xOccurrence = new XOccurrence();

		if (sp.getIndex() == 0 && sp._sym == XScriptParser.NOCHAR) {
			sp.nextSymbol();
		}
		while (sp._sym != XScriptParser.NOCHAR) {
			if (sp.isOccurrence(_xOccurrence)) {
				if (_isValue) {
					parseTypeSection(sp);
				}
				continue;
			} else if (_isValue) {
				if (sp._sym == XScriptParser.IDENTIFIER_SYM) {
					parseTypeSection(sp);
					continue;
				} else if (sp._sym == XScriptParser.CONSTANT_SYM &&
					sp._parsedValue.getItemId()==XScriptParser.XD_STRING) {
					_xOccurrence.setFixed();
					_default = '\'' + sp._parsedValue.toString() + '\'';
					_typeList = new String[]{_type};
					sp.nextSymbol();
					continue;
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
				case XScriptParser.FORGET_SYM:
					if (_options.length() > 0) {
						_options+= ',';
					}
					_options+= "forget";
//					continue;
//				case XScriptParser.REF_SYM:
//				default:
			}
		}
	}

	/** Get canonized form of script.
	 * @param actions if true script will return the actions, otherwise
	 * only occurrence, validation, option and reference.
	 * @return string with canonized script.
	 */
	public String getCanonizedScript(boolean actions) {
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
		if (_reference.length() > 0) {
			result += ";ref " + _reference;
		}
		if (_match.length() > 0) {
			result += ";match " + _match;
		}
		if (getOptions().length() > 0) {
			result += ";options " + getOptions();
		}
		if (!actions) { //only validation
			return result;
		}
		if (_init.length() > 0) {
			result = _init + ";init " + result;
		}
		if (getOnTrue().length() > 0) {
			result += ";onTrue " + getOnTrue();
		}
		if (getOnFalse().length() > 0) {
			result += ";onFalse " + getOnFalse();
		}
		if (getOnAbsence().length() > 0) {
			result += ";onAbsence " + getOnAbsence();
		}
		if (getOnExcess().length() > 0) {
			result += ";onExcess " + getOnExcess();
		}
		if (getOnIllegalAttr().length() > 0) {
			result += ";onIllegalAttr " + getOnIllegalAttr();
		}
		if (_create.length() > 0) {
			result += ";create " + _create;
		}
		if (_finally.length() > 0) {
			result += ";finaly " + _finally;
		}
		return result;
	}

	private static String symToString(final XScriptParser sp) {
		switch (sp._sym) {
			case XScriptParser.AND_SYM:
				return " AND ";
			case XScriptParser.AAND_SYM:
				return " AAND ";
			case XScriptParser.OR_SYM:
				return " OR ";
			case XScriptParser.OOR_SYM:
				return " OOR ";
			case XScriptParser.GT_SYM:
				return " GT ";
			case XScriptParser.GE_SYM:
				return " GE ";
			case XScriptParser.LT_SYM:
				return " LT ";
			case XScriptParser.LE_SYM:
				return " LE ";
			case XScriptParser.EQ_SYM:
				return " EQ ";
			case XScriptParser.NE_SYM:
				return " NE ";
			case XScriptParser.ASSGN_SYM:
				return " = ";
			case XScriptParser.MINUS_SYM:
				return " - ";
			case XScriptParser.PLUS_SYM:
				return " + ";
			case XScriptParser.MUL_SYM:
				return " * ";
			case XScriptParser.DIV_SYM:
				return " / ";
			case XScriptParser.COLON_SYM:
				return " : ";
			default:
				return XScriptParser.symToName(sp._sym);
		}
	}

	/** Parse parameters of typoe method.
	 * @param sp script parser.
	 * @return string with canonized method parameters.
	 */
	private String parseTypeMethodParams(final XScriptParser sp) {
		if (sp._sym == XScriptParser.NOCHAR ||
			sp._sym != XScriptParser.LPAR_SYM) {
			return "";
		}
		StringBuilder sb = new StringBuilder("(");
		boolean parClosed = false;
		sp.nextSymbol();
		while (sp._sym != XScriptParser.NOCHAR &&
			sp._sym != XScriptParser.RPAR_SYM) {
			switch (sp._sym) {
				case XScriptParser.MOD_SYM:
					if (sp.isXMLName(sp._xmlVersion1)) {
						sb.append('%').append(sp.getParsedString());
					}
					break;
				case XScriptParser.IDENTIFIER_SYM:
					sb.append(sp._idName);
					break;
				case XScriptParser.CONSTANT_SYM:
					if (sp._parsedValue.getItemId() ==
						XScriptParser.XD_STRING) {
						sb.append(((DefString) sp._parsedValue).sourceValue());
					} else {
						sb.append(sp._parsedValue.toString());
					}
					break;
				case XScriptParser.LPAR_SYM:
					sb.append(parseTypeMethodParams(sp));
					break;
				case XScriptParser.RPAR_SYM:
					parClosed = true;
				default:
					sb.append(symToString(sp));
			}
			sp.nextSymbol();
		}
		if (!parClosed) {
			//oprava - cteni symbolu navic - v nekterych pripadech chybi carka
			//sp.nextSymbol();
			sb.append(')');
		}
		return sb.toString();
	}

	/** Parse type section.
	 * @param sp script parser.
	 * @return string with canonized script.
	 */
	private void parseTypeSection(final XScriptParser sp) {
		if (sp._sym == XScriptParser.BEG_SYM) {
			sp.nextSymbol();
			_type = "{";
			while (sp._sym != XScriptParser.END_SYM &&
				sp._sym != XScriptParser.NOCHAR) {
				_type += parseScriptSection(sp);
				_type += ';';
			}
			if (sp._sym == XScriptParser.END_SYM) {
				sp.nextSymbol();
			}
			_type += "}";
			_typeList = new String[]{_type};
		} else {
			_type = "";
			ArrayList<String> typeList = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			while (sp._sym != XScriptParser.NOCHAR &&
				sp._sym != XScriptParser.SEMICOLON_SYM) {
				switch (sp._sym) {
					case XScriptParser.IDENTIFIER_SYM:
						sb.append(sp._idName);
						sp.nextSymbol();
						if (sp._sym == XScriptParser.LPAR_SYM) {
							sb.append(parseTypeMethodParams(sp));
						} else if (sp._sym == XScriptParser.IDENTIFIER_SYM) {
							sb.append(' ');
						}
						continue;
					case XScriptParser.CONSTANT_SYM:
						if (sp._parsedValue.getItemId() ==
							XScriptParser.XD_STRING) {
								sb.append(((DefString) sp._parsedValue)
									.sourceValue());
						} else {
							sb.append(sp._parsedValue.toString());
						}
						break;
					case XScriptParser.OR_SYM:
						typeList.add(sb.toString());
						_type += sb.append(" OR ").toString();
						sb.setLength(0);
						break;
					case XScriptParser.OOR_SYM:
						typeList.add(sb.toString());
						_type += sb.append(" OOR ").toString();
						sb.setLength(0);
						break;
					default:
						if (sp._sym != XScriptParser.RPAR_SYM) {
							sb.append(symToString(sp));
						}
				}
				sp.nextSymbol();
			}
			if (sp._sym == XScriptParser.SEMICOLON_SYM) {
				sp.nextSymbol();
			}
			_type += sb.toString();
			_typeList = new String[typeList.size() + 1];
			int i = 0;
			for (; i < typeList.size(); i++) {
				_typeList[i] = typeList.get(i);
			}
			_typeList[i] = sb.toString();
		}
	}

	/** Parse script section.
	 * @param sp script parser.
	 * @return string with canonized script.
	 */
	private static String parseScriptSection(final XScriptParser sp) {
		StringBuilder sb = new StringBuilder();
		if (sp._sym == XScriptParser.BEG_SYM) {
			sp.nextSymbol();
			sb.append("{");
			while (sp._sym != XScriptParser.END_SYM &&
				sp._sym != XScriptParser.NOCHAR) {
				sb.append(parseScriptSection(sp));
				sb.append(';');
			}
			if (sp._sym == XScriptParser.END_SYM) {
				sp.nextSymbol();
			}
			return sb.append("}").toString();
		} else {
			while (sp._sym != XScriptParser.NOCHAR &&
				sp._sym != XScriptParser.SEMICOLON_SYM) {
				switch (sp._sym) {
					case XScriptParser.IDENTIFIER_SYM:
						sb.append(sp._idName);
						break;
					case XScriptParser.CONSTANT_SYM:
						if (sp._parsedValue.getItemId() ==
							XScriptParser.XD_STRING) {
							sb.append(
								((DefString) sp._parsedValue).sourceValue());
						} else {
							sb.append(sp._parsedValue.toString());
						}
						break;
					case XScriptParser.ATCHAR_SYM:
						sb.append('@').append(sp._idName);
						break;
					default:
						sb.append(symToString(sp));
				}
				sp.nextSymbol();
			}
			if (sp._sym == XScriptParser.SEMICOLON_SYM) {
				sp.nextSymbol();
			}
			return sb.toString();
		}
	}

	/** Get array of union types.  Use ONLY for XML schema conversion!
	 * @return array list of types.
	 */
	public String[] getTypeList() {return _typeList;}

	/** Check if script contains an option specified by the argument.
	 * @param option string with a name of option.
	 * @return true if and only if the script contains the option.
	 */
	public boolean hasOption(final String option) {
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
			i+= 1;
		}
		return false;
	}

	/** Create XdParsedScript object from the script of the node.
	 * @param n Node.
	 * @return XdParsedScript object created from script of the node.
	 */
	public static XDParsedScript getXdScript(final Node n) {
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
		String s;
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			s = XDGenCollection.getXdefAttr(
				(Element) n, xdUri, "script", false);
		} else {
			s = n.getNodeValue();
		}
		if (s == null || (s = s.trim()).length() == 0) {
			return null;
		}
		return getXdScript(s, defName, isValue);
	}

	/** Create XdParsedScript object from the script.
	 * @param script string with script.
	 * @param defName name of X-definition.
	 * @param isValue if <tt>true</tt> then script describes text value.
	 * @return XdParsedScript object created from the script.
	 */
	public static XDParsedScript getXdScript(final String script,
		final String defName,
		final boolean isValue) {
		XScriptParser sp = new XScriptParser(false, null); //no macros!
		sp.setSource(new SBuffer(script), defName, XDConstants.XD20_ID);
		return new XDParsedScript(sp, isValue);
	}

	/** Get required part of the script of a node in the canonized form.
	 * @param n Node.
	 * @param actions if true the script will return actions, otherwise
	 * only occurrence, validation, option and reference.
	 * @return string with canonized form of the script from the node.
	 */
	public static String getScript(final Node n, boolean actions) {
		return getXdScript(n).getCanonizedScript(actions);
	}

	/** @return the _xOccurrence */
	public XOccurrence getxOccurrence() {return _xOccurrence;}

	/** @param xOccurrence the _xOccurrence to set */
	public void setxOccurrence(XOccurrence xOccurrence){
		_xOccurrence=xOccurrence;
	}

	public String getOptions() {return _options;}

	public String getOnTrue() {return _onTrue;}

	public String getOnFalse() {return _onFalse;}

	public String getOnAbsence() {return _onAbsence;}

	public String getOnExcess() {return _onExcess;}

	public String getOnIllegalAttr() {return _onIllegalAttr;}

}