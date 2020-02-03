package org.xdef.impl.util.conv.type;

import org.xdef.sys.SBuffer;
import org.xdef.sys.StringParser;
import org.xdef.impl.compile.XScriptParser;
import org.xdef.impl.util.conv.type.domain.Other;
import org.xdef.impl.util.conv.type.domain.ValueType;
import org.xdef.impl.util.conv.type.domain.XdefBase;
import org.xdef.impl.util.conv.type.domain.XdefType;
import org.xdef.impl.util.conv.type.domain.XsdBase;
import org.xdef.impl.util.conv.type.domain.XsdFacet;
import org.xdef.impl.util.conv.type.domain.XsdList;
import org.xdef.impl.util.conv.type.domain.XsdRestricted;
import org.xdef.impl.util.conv.type.domain.XsdUnion;
import org.xdef.impl.util.conv.type.domain.restr.DigitCountRestricted;
import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestricted;
import org.xdef.impl.util.conv.type.domain.restr.LengthRestricted;
import org.xdef.impl.util.conv.type.domain.restr.PatternRestricted;
import org.xdef.impl.util.conv.type.domain.restr.ValueRestricted;
import org.xdef.impl.util.conv.type.domain.restr.WhiteSpaceRestricted;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.xdef.impl.XConstants;

/** X-definition value type parser.
 * @author Ilia Alexandrov
 */
public final class XdefValueTypeParser {

	/** Parses given X-definition type declaration and returns type
	 * representation object.
	 * @param type X-definition type declaration to parse.
	 * @return type representation object.
	 * @throws NullPointerException if given type is <tt>null</tt> or
	 * IllegalArgumentException if given type is empty or
	 * TypeParseException if exception occurs during parsing.
	 */
	public static ValueType parse(String type) {
		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException("Given type is empty");
		}
		XdefValueTypeParser p = new XdefValueTypeParser(type);
		return p.parseString();
	}

	/* Script parser object for parsing. */
	private final XScriptParser _p;
	/** Previous read symbol character. */
	private Symbol _prevSym;
	/** Actual read symbol. */
	private Symbol _actSym;
	/** Parsed symbols buffer. */
	private final StringBuffer _b;
	/** Symbol read lock. */
	private boolean _symLock = false;
	/** Union type switch. */
	private boolean _isXdefUnion = false;

	/** Creates instance of type parser.
	 * @param type type string to parse.
	 * @throws NullPointerException if given type string is <tt>null</tt>.
	 * @throws IllegalArgumentException if given type string is empty.
	 */
	private XdefValueTypeParser(String type) {
		if (type == null) {
			throw new NullPointerException("Given type string is null");
		}
		if (type.length() == 0) {
			throw new IllegalArgumentException("Given type string is empty");
		}
		_p = new XScriptParser(XConstants.XML10);
		_p.setSource(new SBuffer(type), null, XConstants.XD20);
		_b = new StringBuffer();
	}

/*VT3*/
	private static boolean checkXDIntType(ValueType t) {
		if (t instanceof XsdRestricted
			&& ((XsdRestricted) t).getBase() == null
			&& ((XsdRestricted) t).getXdefBase() != null
			&& "int".equals(((XsdRestricted) t).getXdefBase().getName())) {
			XsdRestricted tt = (XsdRestricted) t;
			Set<String> enums = tt.getEnumerations();
			String min = tt.getMinInclusive();
			String max = tt.getMaxInclusive();
			if (!enums.isEmpty() || min != null || max != null) {
				boolean toLong = false;
				long l;
				for (String o : enums) {
					try {
						l = Long.parseLong(o);
						if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
							toLong = true; // -> xs.long
							break;
						}
					} catch (Exception ex) {}
				}
				if (!toLong) {
					if (min != null && Long.parseLong(min) < Integer.MIN_VALUE){
						toLong = true; // -> xs.long
					}
					if (max != null && Long.parseLong(max) > Integer.MAX_VALUE){
						toLong = true; // -> xs.long
					}
				}
				if (toLong) {
					tt.setXdefBase(XsdBase.LONG);
					return true;
				}
			}
		}
		return false;
	}
/*VT3*/

	/** Parses string.
	 * @return parsed type representation.
	 */
	private ValueType parseString() {
		ValueType t = null;
		StringBuffer b = new StringBuffer();
		readSym();
		boolean typeRead = false;
		while (true) {
			if (isIdent()) {
				if (typeRead) {
					throwEx("Expecting operator after type declaration",
						Symbol.OR);
				}
				String name = getIdent();
				ValueType parsed = parseType(name);
				if (!_isXdefUnion) {
					t = parsed;
				} else {
					((XsdUnion) t).addMemberType(parsed);
				}
				buffer(b, parsed.getTypeString());
				typeRead = true;
			} else if (isOR() || isAND()) {
				if (!typeRead) {
					throwEx("Expecting type declaration after operator");
				}
				buffer(b);
				if (!_isXdefUnion) {
					XsdUnion u = new XsdUnion();
					u.addMemberType(t);
					t = u;
					_isXdefUnion = true;
				}
				typeRead = false;
			} else {
				buffer(b);
				t = parseType("eq");
				typeRead = true;
			}
			readSym();
			if (isEos()) {
				break;
			}
		}
		t.setTypeString(b.toString());
		checkXDIntType(t);
		return t;
	}

	/** Parses type and returns parsed value type object.
	 * @return parsed value type object.
	 */
	private ValueType parseType() {
		readSym();
		if (!isIdent()) {
			throwEx("Expecting type name", "Type name");
		}
		String name = getIdent();
		return parseType(name);
	}

	/** Parses type with given name and returns type object.
	 * @param typeName type name.
	 * @return type object.
	 */
	private ValueType parseType(String typeName) {
		ValueType t;
		StringBuffer b = new StringBuffer(typeName);
		XsdBase xsdBase = XsdBase.getByXdefName(typeName);
		if (xsdBase != null) {
			t = parseXsdRestricted(xsdBase, b);
			t.setTypeString(b.toString());
			return t;
		}
		if (!typeName.startsWith("xs:")) {
//			if ("dec".equals(typeName)) {
/*VT1*/
			if ("int".equals(typeName)) {
				// this is nasty, in X-definition int is long.
				xsdBase = XsdBase.getByXdefName("xs:int");
			} else if ("dec".equals(typeName)) {
/*VT1*/
				xsdBase = null;
			} else {
				xsdBase = XsdBase.getByXdefName("xs:" +typeName);
			}
			if (xsdBase != null) {
				t = parseXsdRestricted(xsdBase, b);
				t.setTypeString(b.toString());
				return t;
			}
		}
		XdefBase xdBase = XdefBase.get(typeName);
		if (xdBase != null) {
			t = parseXdefType(xdBase, b);
			t.setTypeString(b.toString());
			return t;
		}
		if (XsdList.isXsdList(typeName)) {
			t = parseXsdList(b);
			t.setTypeString(b.toString());
			return t;
		}
		if (XsdUnion.isXsdUnion(typeName)) {
			t = parseXsdUnion(b);
			t.setTypeString(b.toString());
			return t;
		}
		t = parseOther(typeName, b);
		t.setTypeString(b.toString());
		return t;
	}

	/** Parses schema restricted type and returns object representation.
	 * @param xsdBase schema restricted X-definition base.
	 * @param b output buffer.
	 * @return schema restricted type object.
	 */
	private XsdRestricted parseXsdRestricted(XsdBase xsdBase, StringBuffer b) {
		XsdRestricted t = new XsdRestricted();
		t.setXdefBase(xsdBase);
		readSym();
		if (isLPar()) {
			buffer(b);
			parseXsdParams(t, b);
		} else {
			_symLock = true;
		}
		return t;
	}

/*VT*/
	/** Set sequential parameter as facet.
	 * @param t schema restricted type.
	 * @param paramName Name of facet in Schema
	 * @param parsedFacets Set of parsed facets
	 * @param value value of parameter.
	 */
	private void setFacet(XsdRestricted t,
		String paramName,
		Set<XsdFacet> parsedFacets,
		String value) {
		XsdFacet facet = XsdFacet.getByXdefName(paramName);
		if (facet == null) {
			throwEx("Unknown parameter name", paramName);
		}
		switch (facet.getId()) {
			case XsdFacet.Id.MAX_INCLUSIVE:
				t.setMaxInclusive(value);
				break;
			case XsdFacet.Id.MIN_EXCLUSIVE:
				t.setMinExclusive(value);
				break;
			case XsdFacet.Id.MIN_INCLUSIVE:
				t.setMinInclusive(value);
				break;
			case XsdFacet.Id.LENGTH:
				t.setLength(Integer.parseInt(value));
				break;
			case XsdFacet.Id.MIN_LENGTH:
				t.setMinLength(Integer.parseInt(value));
				break;
			case XsdFacet.Id.MAX_LENGTH:
				t.setMaxLength(Integer.parseInt(value));
				break;
			case XsdFacet.Id.TOTAL_DIGITS:
				t.setTotalDigits(Integer.parseInt(value));
				break;
			case XsdFacet.Id.FRACTION_DIGITS:
				t.setFractionDigits(Integer.parseInt(value));
				break;
			default:
				throwEx("Unknown sequential parameter name", paramName);
		}
		parsedFacets.add(facet);
	}

	/** Add sequential parameters.
	 * @param t schema restricted type.
	 * @param parsedFacets Set of parsed facets
	 * @param sqParams array with sequential parameters.
	 */
	private void processSeqParams(XsdRestricted t,
		Set<XsdFacet> parsedFacets,
		ArrayList<String> sqParams) {
		if (sqParams == null || sqParams.isEmpty()) {
			return;
		}
		int size = sqParams.size();
		String[][] sqParamIds = t.getXdefBase().getSqParams();
		if (sqParamIds == null || size > sqParamIds.length) {
			throwEx("Illegal sequential parameters",
			sqParams.get(0)+(sqParams.size() > 0 ? ", "+sqParams.get(1) : ""));
		}
		String[] parNames = sqParamIds[size - 1];
		if (size == 1) {
			String value = sqParams.get(0);
			for (int j = 0; j < parNames.length; j++) {
				setFacet(t, parNames[j], parsedFacets, value);
			}
		} else {
			for (int i = 0; i < size; i++) {
				setFacet(t, parNames[i], parsedFacets, sqParams.get(i));
			}
		}
	}
/*VT*/

	/** Parses given schema restricted type parameters.
	 * @param t schema restricted type.
	 * @param b output buffer.
	 */
	private void parseXsdParams(XsdRestricted t, StringBuffer b) {
		boolean paramRead = false;
		Set<XsdFacet> parsedFacets = new HashSet<XsdFacet>();
		boolean baseParsed = false;
		readSym();
		ArrayList<String> sqParams = new ArrayList<String>();
		while (true) {
			if (isMod()) { // named parameter
/*VT*/
				processSeqParams(t, parsedFacets, sqParams);
				sqParams = null;
/*VT*/
				if (paramRead) {
					throwEx("Expecting comma after parameter", Symbol.COMMA);
				}
				buffer(b);
				readSym(); // base | enumeration | pattern | ...
				if (!isIdent()) {
					throwEx("Expecting parameter name");
				}
				String paramName = getIdent();
				buffer(b);
				if (XsdRestricted.isXdefBaseFacet(paramName)) {
					if (baseParsed) {
						throwEx("Base parameter has been already parsed");
					}
					readSym(); // =
					if (!isAssign()) {
						throwEx("Expecting assign after parameter name",
							Symbol.ASSGN);
					}
					buffer(b);
					ValueType base = parseType();
					t.setBase(base);
					buffer(b, base.getTypeString());
					baseParsed = true;
				} else {
					XsdFacet facet = XsdFacet.getByXdefName(paramName);
					if (facet == null) {
						throwEx("Unknown parameter name", paramName);
					}
					if (parsedFacets.contains(facet)) {
						throwEx("Parameter was already parsed");
					}
					parseXsdFacet(t, facet, b);
					parsedFacets.add(facet);
				}
				paramRead = true;
			} else if (isComma()) {
				if (!paramRead) {
					throwEx("Expecting parameter");
				}
				buffer(b);
				paramRead = false;
			} else if (isRPar()) {
				if (isCommaPrev()) {
					throwEx("Expecting parameter");
				}
				buffer(b);
				break;
/*VI*/
			} else if (sqParams != null) { // can parse sequential params
				if (!isConst()) {
					throwEx("Expecting sequential parameter value");
				}
				buffer(b);
				sqParams.add(getConst()); //we save sequential parameter
				paramRead = true;
/*VI*/
			} else {
				throwEx("Illegal symbol");
			}
			readSym();
			if (isEos()) {
				//TODO: chyba - necekany konec
				//throw new TypeParserException("Unexpected end of string",_b);
			}
		}
/*VT*/
		processSeqParams(t, parsedFacets, sqParams);
/*VT*/
	}

	/** Processes schema facet and adds restriction to given value type.
	 * @param t value type to add facet to.
	 * @param f facet to process.
	 * @param b output buffer.
	 */
	private void parseXsdFacet(ValueType t, XsdFacet f, StringBuffer b) {
		readSym(); // =
		if (!isAssign()) {
			throwEx("Expecting assign after parameter name", Symbol.ASSGN);
		}
		buffer(b);
		if (XsdFacet.isMultipleValue(f)) {
			parseMultipleValueFacet(t, f, b);
		} else if (XsdFacet.isSingleNumericRestriction(f)) {
			parseSingleNumericFacet(t, f, b);
		} else if (XsdFacet.isSingleStringRestriction(f)) {
			parseSingleStringFacet(t, f, b);
		} else {
			throwEx("Illegal facet", "Facet");
		}
	}

	/** Parses given numeric value facet and adds restriction to given
	 * value type.
	 * @param t value type to add restriction to.
	 * @param f facet to parse.
	 * @param b output buffer.
	 */
	private void parseSingleNumericFacet(ValueType t,XsdFacet f,StringBuffer b){
		if (!isConst()) { // if it was not processed yet
			readSym(); // value
			if (!isConst()) {
				throwEx("Expecting parameter value", "Parameter value");
			}
		}
		String value = getConst();
		int intValue = 0;
		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throwEx("Expecting numeric value");
		}
		if (XsdFacet.isDigitCountRestriction(f)) {
			DigitCountRestricted d = null;
			try {
				d = (DigitCountRestricted) t;
			} catch (ClassCastException ex) {
				throwEx("Expecting digit count restricted type",
					"Digit restricted type", t);
			}
			switch (f.getId()) {
				case XsdFacet.Id.FRACTION_DIGITS:
					d.setFractionDigits(intValue);
					break;
				case XsdFacet.Id.TOTAL_DIGITS:
					d.setTotalDigits(intValue);
					break;
				default:
					throwEx("Expecting digit count restriction facet",
						"Digit count restricted facet", f);
			}
		} else if (XsdFacet.isLengthRestriction(f)) {
			LengthRestricted l = null;
			try {
				l = (LengthRestricted) t;
			} catch (ClassCastException ex) {
				throwEx("Expecting length restricted type",
					"Length restricted type", t);
			}
			switch (f.getId()) {
				case XsdFacet.Id.LENGTH:
					l.setLength(intValue);
					break;
				case XsdFacet.Id.MAX_LENGTH:
					l.setMaxLength(intValue);
					break;
				case XsdFacet.Id.MIN_LENGTH:
					l.setMinLength(intValue);
					break;
				default:
					throwEx("Expecting length restriction facet",
						"Length restriction facet", f);
			}
		} else {
			throwEx("Expecting numeric value facet", "Numeric value facet", f);
		}
		buffer(b);
	}

	/** Parses given single string value facet and adds restriction to given
	 * value type.
	 * @param t value type to add restriction to.
	 * @param f single string value facet.
	 * @param b output buffer.
	 */
	private void parseSingleStringFacet(ValueType t, XsdFacet f, StringBuffer b) {
		readSym(); // value
		if (!isConst()) {
			throwEx("Expecting parameter value", "Paramter value");
		}
		String value = getConst();
		if (XsdFacet.isValueRestricted(f)) {
			ValueRestricted v = null;
			try {
				v = (ValueRestricted) t;
			} catch (ClassCastException ex) {
				throwEx("Expecting value restricted type",
					"Value restricted type", t);
			}
			switch (f.getId()) {
				case XsdFacet.Id.MAX_EXCLUSIVE:
					v.setMaxExclusive(value);
					break;
				case XsdFacet.Id.MAX_INCLUSIVE:
					v.setMaxInclusive(value);
					break;
				case XsdFacet.Id.MIN_EXCLUSIVE:
					v.setMinExclusive(value);
					break;
				case XsdFacet.Id.MIN_INCLUSIVE:
					v.setMinInclusive(value);
					break;
				default:
					throwEx("Expecting value restriction facet",
						"Value restriction facet", f);
			}
		} else if (XsdFacet.Id.WHITE_SPACE == f.getId()) {
			WhiteSpaceRestricted w = null;
			try {
				w = (WhiteSpaceRestricted) t;
			} catch (ClassCastException ex) {
				throwEx("Expecting white space restricted type",
					"White space restricted type", t);
			}
			w.setWhiteSpace(value);
		} else {
			throwEx("Expecting string value restriction",
				"String value restriction", f);
		}
		buffer(b);
	}

	/** Parses given multiple value facet and adds restrictions to given
	 * value type.
	 * @param t value type to add restrictions to.
	 * @param f multiple value facet.
	 * @param b output buffer.
	 */
	private void parseMultipleValueFacet(ValueType t,XsdFacet f,StringBuffer b){
		readSym(); // [
		if (!isLsq()) {
			throwEx("Expecting left square bracket", Symbol.LSQ);
		}
		buffer(b);
		readSym(); // value
		boolean paramRead = false;
		while (true) {
			if (isConst()) {
				if (paramRead) {
					throwEx("Expecting comma after parameter", Symbol.COMMA);
				}
				String value = getConst();
				switch (f.getId()) {
					case XsdFacet.Id.ENUMERATION: {
						EnumerationRestricted enumRestr = null;
						try {
							enumRestr = (EnumerationRestricted) t;
						} catch (ClassCastException ex) {
							throwEx("Expecting enumeration restricted type",
								"Enumeration restricted type", t);
						}
						enumRestr.addEnumeration(value);
					}
					break;
					case XsdFacet.Id.PATTERN: {
						PatternRestricted patternRestr = null;
						try {
							patternRestr = (PatternRestricted) t;
						} catch (ClassCastException ex) {
							throwEx("Expecting pattern restricted type",
								"Pattern restricted type", t);
						}
						patternRestr.addPattern(value);
					}
					break;
					default:
						throwEx("Expecting multiple value facet",
							"Multiple value facet", f);
				}
				buffer(b, "'");
				buffer(b);
				buffer(b, "'");
				paramRead = true;
			} else if (isComma()) {
				if (!paramRead) {
					throwEx("Expecting paramter", "Parameter");
				}
				buffer(b);
				paramRead = false;
			} else if (isRsq()) {
				if (isCommaPrev()) {
					throwEx("Expecting paramter", "Parameter");
				}
				buffer(b);
				break;
			} else {
				throwEx("Illegal symbol");
			}
			readSym();
			if (isEos()) {
				throwEx("Unexpected end of string");
			}
		}
	}

	/** Parses X-definition type and returns type object.
	 * @param xdefBase X-definition base type.
	 * @param b output buffer
	 * @return X-definition type object.
	 */
	private XdefType parseXdefType(XdefBase xdefBase, StringBuffer b) {
		XdefType t = new XdefType(xdefBase);
		readSym();
		if (isLPar()) {
			buffer(b);
			parseXdefParams(t, b);
		} else {
			_symLock = true;
		}
		return t;
	}

	/** Parses X-definition type parameters.
	 * @param t X-definition type to add parameters.
	 * @param b output buffer.
	 */
	private void parseXdefParams(XdefType t, StringBuffer b) {
		boolean paramRead = false;
		readSym();
		while (true) {
			if (isConst()) {
				if (paramRead) {
					throwEx("Expecting comma after parameter", Symbol.COMMA);
				}
				buffer(b);
				String param = getConst();
				t.addParam(param);
				paramRead = true;
			} else if (isComma()) {
				if (!paramRead) {
					throwEx("Expecting parameter value", "Paramter value");
				}
				buffer(b);
				paramRead = false;
			} else if (isRPar()) {
				if (isCommaPrev()) {
					throwEx("Expecting parameter value", "Paramter value");
				}
				buffer(b);
				break;
			} else {
				buffer(b);
			}
			readSym();
			if (isEos()) {
				throwEx("Unexpected end of string");
			}
		}
	}

	/** Parses schema union type and returns type object.
	 * @param b output buffer.
	 * @return schema union type object.
	 */
	private XsdUnion parseXsdUnion(StringBuffer b) {
		XsdUnion u = new XsdUnion();
		boolean paramRead = false;
		boolean itemParsed = false;
		Set<XsdFacet> parsedFacets = new HashSet<XsdFacet>();
		readSym(); // (
		if (!isLPar()) {
			throwEx("Excepting left parenthesis after union type delcaration",
				Symbol.LPAR);
		}
		buffer(b);
		readSym();
		while (true) {
			if (isMod()) {
				if (paramRead) {
					throwEx("Expecting comma after parameter", Symbol.COMMA);
				}
				buffer(b);
				readSym(); // base | enumeration | pattern | ...
				if (!isIdent()) {
					throwEx("Expecting parameter name", "Parameter name");
				}
				String paramName = getIdent();
				buffer(b);
				readSym(); // =
				if (!isAssign()) {
					throwEx("Expecting assign after parameter name",
						Symbol.ASSGN);
				}
				buffer(b);
				if (XsdUnion.isItemParam(paramName)) {
					if (itemParsed) {
						throwEx("Paramter item has been already parsed");
					}
					parseMemberTypes(u, b);
					itemParsed = true;
				} else {
					XsdFacet facet = XsdFacet.getByXdefName(paramName);
					if (facet == null) {
						throwEx("Illegal facet", "Facet", paramName);
					}
					if (!XsdUnion.isAviableFacet(facet)) {
						throwEx("Illegal facet",
							"Legal union type facet", facet);
					}
					if (parsedFacets.contains(facet)) {
						throwEx("Facet has been already parsed",
							"Union type facet", facet);
					}
					parseXsdFacet(u, facet, b);
					parsedFacets.add(facet);
				}
				paramRead = true;
			} else if (isComma()) {
				if (!paramRead) {
					throwEx("Expecting type parameter", "Type parameter");
				}
				buffer(b);
				paramRead = false;
			} else if (isRPar()) {
				if (isCommaPrev()) {
					throwEx("Expecting type parameter", "Type parameter");
				}
				buffer(b);
				break;
			} else {
				throwEx("Illegal symbol");
			}
			readSym();
			if (isEos()) {
				throwEx("Unexpected end of string");
			}
		}
		return u;
	}

	/** Parses schema union member types and add them to given schema union type.
	 * @param u schema union type to add member types to.
	 * @param b output buffer.
	 */
	private void parseMemberTypes(XsdUnion u, StringBuffer b) {
		readSym();
		if (!isLsq()) {
			throwEx("Expecting square bracket after item parameter",
				Symbol.LSQ);
		}
		buffer(b);
		readSym();
		boolean added = false;
		while (true) {
			if (isIdent()) {
				ValueType t = parseType(getIdent());
				buffer(b, t.getTypeString());
				u.addMemberType(t);
				added = true;
			} else if (isComma()) {
				if (!added) {
					throwEx("Expecting type delcaration", "Type declaration");
				}
				buffer(b);
			} else if (isRsq()) {
				if (!added) {
					throwEx("Expecting type delcaration", "Type declaration");
				}
				buffer(b);
				break;
			}
			readSym();
			if (isEos()) {
				throwEx("Unexpected end of string");
			}
		}
	}

	private XsdList parseXsdList(StringBuffer b) {
		XsdList t = new XsdList();
		readSym();
		if (!isLPar()) {
			throwEx("Expecting left parenthesis after list type name",
				Symbol.LPAR);
		}
		buffer(b);
		readSym();
		boolean paramRead = false;
		boolean itemTypeParsed = false;
		Set<XsdFacet> parsedFacets = new HashSet<XsdFacet>();
		while (true) {
			if (isMod()) {
				if (paramRead) {
					throwEx("Expecting comma after parameter declaration",
						Symbol.COMMA);
				}
				buffer(b);
				readSym();
				if (!isIdent()) {
					throwEx("Expecting parameter name", "Parameter name");
				}
				String paramName = getIdent();
				if (XsdList.isItemParam(paramName)) {
					if (itemTypeParsed) {
						throwEx("Parameter item has been already parsed");
					}
					buffer(b);
					readSym();
					if (!isAssign()) {
						throwEx("Expecting assign after parameter",
							Symbol.ASSGN);
					}
					buffer(b);
					ValueType itemType;
					itemType = parseType();
					t.setItemType(itemType);
					buffer(b, itemType.getTypeString());
					itemTypeParsed = true;
				} else {
					XsdFacet facet = XsdFacet.getByXdefName(paramName);
					if (facet == null) {
						throwEx("Illegal parameter name",
							"Parameter name", paramName);
					}
					if (!XsdList.isAviableFacet(facet)) {
						throwEx("Illegal facet", "Legal facet", facet);
					}
					if (parsedFacets.contains(facet)) {
						throwEx("Already parsed facet",
							"Unparsed legal facet", facet);
					}
					parseXsdFacet(t, facet, b);
					parsedFacets.add(facet);
				}
				paramRead = true;
			} else if (isComma()) {
				if (!paramRead) {
					throwEx("Expectiong parameter", "Parameter");
				}
				buffer(b);
				paramRead = false;
			} else if (isRPar()) {
				if (isCommaPrev()) {
					throwEx("Expectiong parameter after comma", "Parameter");
				}
				if (!itemTypeParsed) {
					throwEx("Item type parameter has not been parsed");
				}
				buffer(b);
				break;
			} else {
				throwEx("Unexpected symbol");
			}
			readSym();
			if (isEos()) {
				throwEx("Unexpected end of string");
			}
		}
		return t;
	}

	/** Parses other type with given name and returns other type object.
	 * @param name other type name.
	 * @param b
	 * @return other type object.
	 */
	private Other parseOther(String name, StringBuffer b) {
		Other t = new Other(name);
		readSym();
		if (isLPar()) {
			buffer(b);
			readSym();
			int level = 1;
			while (true) {
				if (isRPar()) {
					buffer(b);
					level--;
					if (level == 0) {
						break;
					}
				} else if (isLPar()) {
					buffer(b);
					t.setSimple(false);
					level++;
				} else {
					buffer(b);
					t.setSimple(false);
				}
				readSym();
				if (isEos()) {
					throwEx("Unexpected end of string");
				}
			}
		} else {
			_symLock = true;
		}
		return t;
	}

	/** Reads next symbol if symbol lock is not active and adds symbol
	 * to buffer.
	 */
	private void readSym() {
		if (_symLock) {
			_symLock = false;
		} else {
			_prevSym = _actSym;
			_actSym = getSymbol(_p.nextSymbol());
/*VT*/
			if ((_prevSym == Symbol.LPAR || _prevSym ==  Symbol.COMMA)
				&& (_actSym == Symbol.MINUS  || _actSym == Symbol.PLUS)) {
				// read signed numbers
				Symbol sym = getSymbol(_p.nextSymbol());
				boolean isNumber = false;
				if (sym._type == Symbol.CONST) {
					StringParser p = new StringParser(sym._value);
					isNumber = p.isFloat() && p.eos();
					if (!isNumber) {
						p.setBufIndex(0);
						isNumber = p.isInteger() && p.eos();
					}
				}
				if (isNumber) {
					if (Symbol.MINUS == _actSym) {
						sym._value = "-" + sym._value;
					}
				} else { // never should happen
					// This is a nasty code! It probably should be an error!
					buffer(_b);
					sym._value = _actSym._value + sym._value;
				}
				_actSym = sym;
			}
/*VT*/
			buffer(_b);
		}
	}

	private void throwEx(String message) {
		throw new ParserException(message, null, _actSym, _b.toString());
	}

	private void throwEx(String message, Object expected) {
		throw new ParserException(message, expected, _actSym, _b.toString());
	}

	private void throwEx(String message, Object expected, Object found) {
		throw new ParserException(message, expected, found, _b.toString());
	}

	private Symbol getSymbol(char symbol) {
		switch (symbol) {
			case XScriptParser.MUL_SYM: // can be only as maxLength seq. param
				return Symbol.getConst(String.valueOf(Integer.MAX_VALUE));
			case XScriptParser.ASSGN_SYM:
				return Symbol.ASSGN;
			case XScriptParser.COMMA_SYM:
				return Symbol.COMMA;
			case XScriptParser.CONSTANT_SYM:
				return Symbol.getConst(_p._parsedValue.stringValue());
			case XScriptParser.NOCHAR:
				return Symbol.EOS;
			case XScriptParser.IDENTIFIER_SYM:
				return Symbol.getIdent(_p._idName);
			case XScriptParser.LPAR_SYM:
				return Symbol.LPAR;
			case XScriptParser.LSQ_SYM:
				return Symbol.LSQ;
			case XScriptParser.MOD_SYM:
				return Symbol.MOD;
			case XScriptParser.AND_SYM:
			case XScriptParser.AAND_SYM:
				return Symbol.AND;
			case XScriptParser.OR_SYM:
			case XScriptParser.OOR_SYM:
				return Symbol.OR;
			case XScriptParser.RPAR_SYM:
				return Symbol.RPAR;
			case XScriptParser.RSQ_SYM:
				return Symbol.RSQ;
/*VT*/
			case XScriptParser.PLUS_SYM:
				return Symbol.PLUS;
			case XScriptParser.MINUS_SYM:
				return Symbol.MINUS;
/*VT*/
		}
		throw new RuntimeException("Unknown symbol");
	}

	/** Returns <code>true</code> if actual symbol is identifier. */
	private boolean isIdent() {return _actSym.isIdent();}

	/** Returns name of last read identifier symbol. */
	private String getIdent() {return _actSym._value;}

	/** Returns <code>true</code> if actual symbol is constant. */
	private boolean isConst() {return _actSym.isConst();}

	/** Returns last read constant value. */
	private String getConst() {return _actSym._value;}

	/** Returns true if actual symbol is left parenthesis <code>(</code>. */
	private boolean isLPar() {return Symbol.LPAR.equals(_actSym);}

	/** Returns true if actual symbol is right parenthesis <code>)</code>. */
	private boolean isRPar() {return Symbol.RPAR.equals(_actSym);}

	/** Returns true if actual symbol is left square <code>[</code>. */
	private boolean isLsq() {return Symbol.LSQ.equals(_actSym);}

	/** Returns true if actual symbol is right square <code>[</code>. */
	private boolean isRsq() {return Symbol.RSQ.equals(_actSym);}

	/** Returns <code>true</code> if actual symbol is comma <code>,</code>. */
	private boolean isComma() {return Symbol.COMMA.equals(_actSym);}

	/** Returns <code>true</code> if previous symbol is comma <code>,</code>.*/
	private boolean isCommaPrev() {return Symbol.COMMA.equals(_prevSym);}

	/** Returns <code>true</code> if actual symbol is assign <code>=</code>. */
	private boolean isAssign() {return Symbol.ASSGN.equals(_actSym);}

	/** Returns <code>true</code> if actual symbol is modulo <code>%</code>. */
	private boolean isMod() {return Symbol.MOD.equals(_actSym);}

	/** Returns <code>true</code> if actual symbol is end of string symbol. */
	private boolean isEos() {return Symbol.EOS.equals(_actSym);}

	/** Returns <code>true</code> if actual symbol is OR operator symbol. */
	private boolean isOR() {return Symbol.OR.equals(_actSym);}

	/** Returns <code>true</code> if actual symbol is AND operator symbol. */
	private boolean isAND() {return Symbol.AND.equals(_actSym);}

	/** Adds actual symbol value to given buffer.
	 * @param builder buffer.
	 */
	private void buffer(StringBuffer builder) {
		if (isConst()) {
			builder.append('\'').append(getConst()).append('\'');
		} else if (isIdent()) {
			builder.append(getIdent());
		} else {
			builder.append(_actSym._value);
		}
	}

	/** Adds given string to given buffer.
	 * @param builder buffer.
	 * @param string string to add.
	 */
	private void buffer(StringBuffer builder, String string) {
		builder.append(string);
	}

	/** Exception thrown during parsing. */
	private static class ParserException extends RuntimeException {

		private static final long serialVersionUID = -2582190224833320816L;

		/** Exception message. */
		private final String _message;
		/** Parsed string. */
		private final String _parsed;
		/** Expected. */
		private final Object _expected;
		/** Found. */
		private final Object _found;

		private ParserException(String message,
			Object expected,
			Object found,
			String parsed, Throwable cause) {
			super(message, cause);
			_message = message;
			_expected = expected;
			_found = found;
			_parsed = parsed;
		}

		private ParserException(String message,
			Object expected,
			Object found,
			String parsed) {
			super(message);
			_message = message;
			_expected = expected;
			_found = found;
			_parsed = parsed;
		}

		@Override
		public String getMessage() {
			StringBuilder sb = new StringBuilder(_message);
			String newLine = "\n\t";
			if (_expected != null) {
				sb.append(newLine).append("Expected: ")
					.append(_expected.toString());
			}
			if (_found != null) {
				sb.append(newLine).append("Found: ").append(_found.toString());
			}
			if (_parsed != null) {
				sb.append(newLine).append("Parsed: ").append(_parsed);
			}
			return sb.toString();
		}
	}

	/** Script parser symbol. */
	private static class Symbol {

		private static final int SYMBOL = 1;
		private static final int CONST = SYMBOL + 1;
		private static final int IDENT = CONST + 1;

		private final int _type;
		private String _value;

		private static final Symbol ASSGN = new Symbol(SYMBOL, "=");
		private static final Symbol COMMA = new Symbol(SYMBOL, ",");
		private static final Symbol EOS = new Symbol(SYMBOL, "EOS");
		private static final Symbol LPAR = new Symbol(SYMBOL, "(");
		private static final Symbol LSQ = new Symbol(SYMBOL, "[");
		private static final Symbol MOD = new Symbol(SYMBOL, "%");
		private static final Symbol OR = new Symbol(SYMBOL, "OR");
		private static final Symbol AND = new Symbol(SYMBOL, "AND");
		private static final Symbol RPAR = new Symbol(SYMBOL, ")");
		private static final Symbol RSQ = new Symbol(SYMBOL, "]");
/*VT*/
		private static final Symbol PLUS = new Symbol(SYMBOL, "+");
		private static final Symbol MINUS = new Symbol(SYMBOL, "-");
/*VT*/

		private static Symbol getConst(String constant) {
			return new Symbol(CONST, constant);
		}

		private static Symbol getIdent(String ident) {
			return new Symbol(IDENT, ident);
		}

		private Symbol(int type, String value) {
			_type = type;
			_value = value;
		}

		/** Returns <code>true</code> if symbol is constant. */
		private boolean isConst() {return CONST == _type;}

		/** Returns <code>true</code> if symbol is identifier. */
		private boolean isIdent() {return IDENT == _type;}

		@Override
		public String toString() {
			switch (_type) {
				case SYMBOL:
					return "Symbol['" + _value + "']";
				case IDENT:
					return "Identifier['" + _value + "']";
				case CONST:
					return "Constant['" + _value + "']";
			}
			return "Unknown symbol";
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Symbol)) {
				return false;
			}
			Symbol s = (Symbol) obj;
			if (_type != s._type) {
				return false;
			}
			return !(_value==null ? s._value!=null : !_value.equals(s._value));
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 59 * hash + this._type;
			hash = 59 * hash + (this._value != null ? this._value.hashCode():0);
			return hash;
		}
	}
}