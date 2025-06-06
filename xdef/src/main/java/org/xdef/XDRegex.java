package org.xdef;

import java.util.regex.Pattern;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueType.REGEX;
import org.xdef.msg.XDEF;
import static org.xdef.msg.XDEF.XDEF651;
import static org.xdef.msg.XDEF.XDEF653;
import static org.xdef.msg.XDEF.XDEF654;
import static org.xdef.msg.XDEF.XDEF655;
import static org.xdef.msg.XDEF.XDEF658;
import static org.xdef.msg.XDEF.XDEF659;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;

/** Implementation of X-script value of regular expression.
 * @author Vaclav Trojan
 */
public final class XDRegex extends XDValueAbstract {

	/** The source of regular expression. */
	private final String _source;
	/** Compiled pattern of regular expression. */
	private final Pattern _value;
	/** Compiled pattern of regular expression. */
	private final boolean _mode;

	/** Creates null instance of XDRegex. */
	public XDRegex() {_source = null; _value = null; _mode = false;}

	/** Creates new instance of XDRegex.
	 * @param s The string with regular expression source.
	 * @param mode if true, then it is a regular expression in XML Schema (XSD) format, otherwise it is
	 * a regular expression in Java format.
	 * @throws SRuntimeException if an error occurs.
	 */
	public XDRegex(final String s, final boolean mode) {
		_source = s;
		_mode = mode;
		try {
			_value = Pattern.compile(mode ? new Translator(s).translate() : s);
		} catch (SRuntimeException ex) {
			String t = ex.getMessage();
			if (t == null) {
				t = ex.toString();
			}
			throw new SRuntimeException(XDEF.XDEF650, s +"; ("+ t +")");//Incorrect regular expression: &{0}
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implemented of methods of XDRegex
////////////////////////////////////////////////////////////////////////////////

	/** Check if given data matches the regular expression.
	 * @param x The data to be checked.
	 * @return true if and only if the data matches regular expression.
	 */
	public final boolean matches(final String x) {return _value.matcher(x).matches();}

	/** Return regex result.
	 * @param x string to be processed with this regular expression.
	 * @return XDRegexResult object.
	 */
	public final XDRegexResult getRegexResult(final String x) {return new XDRegexResult(_value.matcher(x));}

	/** Get value of item as String representation of value.
	 * @return The string representation of value of the object.
	 */
	public final String sourceValue() {return _source;}

	/** Get mode of regular expression source format.
	 * @return true if it is XML schema (XSD) format.
	 */
	public final boolean isXMLSchema() {return _mode;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public final short getItemId() {return XD_REGEX;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public final XDValueType getItemType() {return REGEX;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public final String toString() {return _source;}

	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	@Override
	public final String stringValue() {return _source;}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @return true if and only if the argument is equal to this one.
	 */
	@Override
	public final boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_REGEX) {
			return false;
		}
		return _source==null ? arg.isNull() : _source.equals(arg.stringValue());
	}

	/** Check if the object is null.
	 * @return true if the object is null otherwise returns false.
	 */
	@Override
	public boolean isNull() {return _source == null;}

////////////////////////////////////////////////////////////////////////////////

	/** Translates a source of XML Schema (XSD) regular expression into Java format. (This code ia
	 * the modified version of the source code originally written by James Clark and modified by Michael Kay.)
	 * @see java.util.regex.Pattern
	 * @see <a href="http://www.w3.org/TR/xmlschema-2/#regexs">  XML Schema Part 2</a>
	 * ***********************************************
	 * *   Syntax of XML schema regular expression   *
	 * ***********************************************
	 * regExp ::= branch ( '|' branch )*
	 * branch ::= piece*
	 * piece ::= atom quantifier?
	 * atom ::= normalChar | charClass | ( '(' regExp ')' )
	 * quantifier ::= [?*+] | ( '{' quantity '}' )
	 * quantity ::= quantRange | quantMin | quantExact
	 * quantRange ::= quantExact ',' quantExact
	 * quantMin ::=  quantExact ','
	 * quantExact ::= [0-9]+
	 * normalChar ::= [^.\?*+{}()|#x5B#x5D]
	 * charClass ::= singleCharEsc | charClassEsc | charClassExpr | wildcardEsc
	 * singleCharEsc ::= '\' [nrt\|.?*+(){}#x2D#x5B#x5D#x5E]
	 * charClassEsc ::= ( multiCharEsc | catEsc | complEsc )
	 * catEsc ::= '\p{' charProp '}'
	 * complEsc ::= '\P{' charProp '}'
	 * multiCharEsc ::=  '\' [sSiIcCdDwW]
	 * singleCharNoEsc ::= [^\#x5B#x5D]
	 * charClassExpr ::= '[' charGroup ']'
	 * charGroup ::= ( posCharGroup | negCharGroup ) ( '-' charClassExpr )?
	 * posCharGroup ::= ( charGroupPart )+
	 * negCharGroup ::= '^' posCharGroup
	 * charGroupPart ::= singleChar | charRange | charClassEsc
	 * singleChar ::= singleCharEsc | singleCharNoEsc
	 * charRange ::= singleChar '-' singleChar
	 * charProp ::= isCategory | isBlock
	 * isCategory ::= letters | marks | numbers | punctuation | separators
	 *                | symbols | others
	 * letters ::= 'L' [ultmo]?
	 * marks ::= 'M' [nce]?
	 * numbers ::= 'N' [dlo]?
	 * punctuation ::= 'P' [cdseifo]?
	 * separators ::= 'Z' [slp]?
	 * symbols ::= 'S' [mcko]?
	 * others ::= 'C' [cfon]?
	 * isBlock  ::= 'Is' [a-zA-Z0-9#x2D]+
	 * wildcardEsc ::=  '.'
	*/
	private static final class Translator extends StringParser {

		/** Charset block names which differs in XML schema (XSD) and Java. */
		static private final String[] BLOCKNAMESDIFFERENT = {
			"OldItalic", "[\ud800\udf00-\ud800\udf2f]", "^",
			"Gothic", "[\ud800\udf30-\ud800\udf4f]", "^",
			"Deseret", "[\ud801\udc00-\ud801\udc4f]", "^",
			"ByzantineMusicalSymbols", "[\ud834\udc00-\ud834\udcff]", "^",
			"MusicalSymbols", "[\ud834\udd00-\ud834\uddff]", "^",
			"MathematicalAlphanumericSymbols","[\ud835\udc00-\ud835\udfff]","^",
			"CJKUnifiedIdeographsExtensionB", "[\ud840\udc00-\ud869\uded6]","^",
			"CJKCompatibilityIdeographsSupplement","[\ud87e\udc00-\ud87e\ude1f]","^",
			"Tags", "[\udb40\udc00-\udb40\udc7f]","^",
			"PrivateUse", "[\ue000-\uf8ff\udb80\udc00-\udbbf\udffd\udbc0\udc00-\udbff\udffd]","^",
		};

		/** Charset block names which differs in XML schema (XSD) and Java. Each block name is represented
		 * by three items:
		 * <p>1) block name
		 * <p>2) string with charGroups for this block name or null
		 * <p>3) string with charGroups for the complement of this this block name.
		 * If it is the string with one character "^" the it will be generated the complement of the string
		 * from 2)
		 */
		static private final String[] BLOCKNAMESNOTDIFFERENT = {
			"Arabic", "Armenian", "Bengali", "Cherokee", "Cyrillic",
			"Devanagari", "Ethiopic", "Georgian", "Greek", "GreekExtended",
			"Gujarati", "Gurmukhi", "HangulJamo", "Hebrew", "Kannada",
			"Khmer", "Lao", "Malayalam", "Mongolian", "Myanmar", "Ogham",
			"Oriya", "Runic", "Sinhala", "Syriac", "Tamil", "Telugu",
			"Thaana", "Thai", "Tibetan", "UnifiedCanadianAboriginalSyllabics",
			"ArabicPresentationForms-A", "Arrows", "BasicLatin",
			"BlockElements", "BoxDrawing", "BraillePatterns",
			"CJKCompatibilityIdeographs", "CJKRadicalsSupplement",
			"CJKSymbolsandPunctuation", "CJKUnifiedIdeographs",
			"CJKUnifiedIdeographsExtensionA", "CombiningDiacriticalMarks",
			"CombiningMarksforSymbols", "ControlPictures",
			"CurrencySymbols", "Dingbats", "EnclosedAlphanumerics",
			"GeometricShapes", "GeneralPunctuation",
			"IdeographicDescriptionCharacters", "IPAExtensions",
			"Latin-1Supplement", "LatinExtended-A", "LatinExtended-B",
			"LatinExtendedAdditional", "SpacingModifierLetters",
			"SuperscriptsandSubscripts", "LetterlikeSymbols",
			"NumberForms", "MathematicalOperators",
			"MiscellaneousTechnical", "OpticalCharacterRecognition",
			"MiscellaneousSymbols", "KangxiRadicals", "Hiragana",
			"Katakana", "Bopomofo", "HangulCompatibilityJamo", "Kanbun",
			"BopomofoExtended", "EnclosedCJKLettersandMonths",
			"CJKCompatibility", "YiSyllables", "YiRadicals",
			"HangulSyllables", "AlphabeticPresentationForms",
			"CombiningHalfMarks", "CJKCompatibilityForms", "SmallFormVariants",
			"ArabicPresentationForms-B", "HalfwidthandFullwidthForms",
			"Specials",
		};

		/** Charset categories which not differs in XML schema (XSD) and Java. */
		static private final String[] CATEGORIESNOTDIFFERENT = {
			"Lm", "Lo", "Lt",
			"M", "Mc", "Me", "Mn",
			"N", "Nd",
			"Pc", "Pd", "Pe", "Po", "Ps",
			"Z", "Zl", "Zp", "Zs",
			"S", "Sc", "Sk", "Sm", "So",
			"Cc", "Cf", "Co",
		};

		/** Charset categories which differs in XML schema (XSD) and Java. Each block name is represented
		 * by three items:
		 * <p>1) block name</p>
		 * <p>2) string with charGroups for this block name or null</p>
		 * <p>3) string with charGroups for the complement of this this block name.
		 * If it is the string with one character "^" the it will be generated* the complement of the string
		 * from 2)</p>
		 */
		static private final String[] CATEGORIESDIFFERENT = {
			"Lu", "[\\p{Lu}\u03f4]", "^",
			"Ll", "[\\p{Ll}\u03f5]", "^",
			"L", "[\\p{L}\u03f5\u03f4]", "^",
			"No", "[[\\p{No}]&&[^\u16ee-\u16f0]]",
			  "[[^\\p{No}][\u16ee-\u16f0]]", "Nl", "[\\p{Nl}\u16ee-\u16f0]", "^",
			"P", "[\\p{P}[\u00ab\u2018\u201b\u201c\u201f\u2039\u00bb\u2019\u201d\u203a]]",
			  "[^\\p{P}&&[^\u00ab\u2018\u201b\u201c\u201f\u2039\u00bb\u2019\u201d\u203a]]",
			  "Pf", "[\u00bb\u2019\u201d\u203a]", "^",
			"Pi", "[\u00ab\u2018\u201b\u201c\u201f\u2039]", "^",
			"C", "[\\p{C}[\\p{Cn}&&[^\u03f4\u03f5]]]", "[^\\p{C}&&[\\P{Cn}[\u03f4\u03f5]]]",
			  "Cn", "[\\p{Cn}&&[^\u03f4\u03f5]]", "[\\P{Cn}[\u03f4\u03f5]]",
		};

		/** Index is the sequence number of escape character in "sSiIcCdDwW" */
		static private final String[] MULTICHARESC = {
		/*s*/"[ \\n\\r\\t]",
		/*S*/"[^ \\n\\r\\t]",
		/*i*/"[[\\p{Ll}\\p{Lu}\\p{Lo}\\p{Lt}\\p{Nl}:_\u02bb-\u02c1\u0559\u06e5\u06e6\u212e]&&[^\u00aa-\u00ba\u0132\u0133\u013f\u0140\u0149\u017f\u01c4-\u01cc\u01f1-\u01f3\u01f6-\u01f9\u0218-\u0236\u02a9-\u02af\u03d7-\u03d9\u03db\u03dd\u03df\u03e1\u03f4-\u0400\u040d\u0450\u045d\u048a-\u048f\u04c5\u04c6\u04c9\u04ca\u04cd\u04ce\u04ec\u04ed\u0500-\u050f\u0587\u066e\u066f\u06b8\u06b9\u06bf\u06cf\u06ee-\u0904\u0950\u09bd\u0a8c\u0ad0\u0ae1\u0b35\u0b71-\u0b83\u0cbd\u0d85-\u0dc6\u0e2f\u0eaf\u0edc-\u0f00\u0f6a-\u1055\u10f7\u10f8\u1101\u1104\u1108\u110a\u110d\u1113-\u113b\u113d\u113f\u1141-\u114b\u114d\u114f\u1151-\u1153\u1156-\u1158\u1162\u1164\u1166\u1168\u116a-\u116c\u116f-\u1171\u1174\u1176-\u119d\u119f-\u11a2\u11a9\u11aa\u11ac\u11ad\u11b0-\u11b6\u11b9\u11bb\u11c3-\u11ea\u11ec-\u11ef\u11f1-\u11f8\u1200-\u1d6b\u2071-\u2124\u2128\u212c\u212d\u212f-\u217f\u2183-\u3006\u3038-\u303c\u3095-\u309f\u30ff\u3131-\u4db5\ua000-\ua48c\uf900-\uffdc\ud800\udc00-\udbff\udfff]]",
		/*I*/"[[^\\p{Ll}\\p{Lu}\\p{Lo}\\p{Lt}\\p{Nl}:_\u02bb-\u02c1\u0559\u06e5\u06e6\u212e][\u00aa-\u00ba\u0132\u0133\u013f\u0140\u0149\u017f\u01c4-\u01cc\u01f1-\u01f3\u01f6-\u01f9\u0218-\u0236\u02a9-\u02af\u03d7-\u03d9\u03db\u03dd\u03df\u03e1\u03f4-\u0400\u040d\u0450\u045d\u048a-\u048f\u04c5\u04c6\u04c9\u04ca\u04cd\u04ce\u04ec\u04ed\u0500-\u050f\u0587\u066e\u066f\u06b8\u06b9\u06bf\u06cf\u06ee-\u0904\u0950\u09bd\u0a8c\u0ad0\u0ae1\u0b35\u0b71-\u0b83\u0cbd\u0d85-\u0dc6\u0e2f\u0eaf\u0edc-\u0f00\u0f6a-\u1055\u10f7\u10f8\u1101\u1104\u1108\u110a\u110d\u1113-\u113b\u113d\u113f\u1141-\u114b\u114d\u114f\u1151-\u1153\u1156-\u1158\u1162\u1164\u1166\u1168\u116a-\u116c\u116f-\u1171\u1174\u1176-\u119d\u119f-\u11a2\u11a9\u11aa\u11ac\u11ad\u11b0-\u11b6\u11b9\u11bb\u11c3-\u11ea\u11ec-\u11ef\u11f1-\u11f8\u1200-\u1d6b\u2071-\u2124\u2128\u212c\u212d\u212f-\u217f\u2183-\u3006\u3038-\u303c\u3095-\u309f\u30ff\u3131-\u4db5\ua000-\ua48c\uf900-\uffdc\ud800\udc00-\udbff\udfff]]",
		/*c*/"[[\\p{Ll}\\p{Lu}\\p{Lo}\\p{Lt}\\p{Nl}\\p{Mc}\\p{Me}\\p{Mn}\\p{Lm}\\p{Nd}\\-\\.:_\u00b7\u0387\u06dd\u212e]&&[^\u00aa-\u00b5\u00ba\u0132\u0133\u013f\u0140\u0149\u017f\u01c4-\u01cc\u01f1-\u01f3\u01f6-\u01f9\u0218-\u0236\u02a9-\u02ba\u02c6-\u02cf\u02e0-\u02ee\u0346-\u035f\u0362-\u037a\u03d7-\u03d9\u03db\u03dd\u03df\u03e1\u03f4-\u0400\u040d\u0450\u045d\u0488-\u048f\u04c5\u04c6\u04c9\u04ca\u04cd\u04ce\u04ec\u04ed\u0500-\u050f\u0587\u0610-\u0615\u0653-\u0658\u066e\u066f\u06b8\u06b9\u06bf\u06cf\u06ee\u06ef\u06fa-\u07b1\u0904\u0950\u09bd\u0a01\u0a03\u0a8c\u0ad0\u0ae1-\u0ae3\u0b35\u0b71\u0cbc\u0cbd\u0d82-\u0df3\u0e2f\u0eaf\u0edc-\u0f00\u0f6a\u0f96\u0fae-\u0fb0\u0fb8\u0fba-\u1059\u10f7\u10f8\u1101\u1104\u1108\u110a\u110d\u1113-\u113b\u113d\u113f\u1141-\u114b\u114d\u114f\u1151-\u1153\u1156-\u1158\u1162\u1164\u1166\u1168\u116a-\u116c\u116f-\u1171\u1174\u1176-\u119d\u119f-\u11a2\u11a9\u11aa\u11ac\u11ad\u11b0-\u11b6\u11b9\u11bb\u11c3-\u11ea\u11ec-\u11ef\u11f1-\u11f8\u1200-\u1d6b\u2071-\u207f\u20dd-\u20e0\u20e2-\u2124\u2128\u212c\u212d\u212f-\u217f\u2183\u3006\u3038-\u303c\u3095\u3096\u309f\u30ff\u3131-\u4db5\ua000-\ua48c\uf900-\uffdc\ud800\udc00-\udbff\udfff]]",
		/*C*/"[[^\\p{Ll}\\p{Lu}\\p{Lo}\\p{Lt}\\p{Nl}\\p{Mc}\\p{Me}\\p{Mn}\\p{Lm}\\p{Nd}\\-\\.:_\u00b7\u0387\u06dd\u212e][\u00aa-\u00b5\u00ba\u0132\u0133\u013f\u0140\u0149\u017f\u01c4-\u01cc\u01f1-\u01f3\u01f6-\u01f9\u0218-\u0236\u02a9-\u02ba\u02c6-\u02cf\u02e0-\u02ee\u0346-\u035f\u0362-\u037a\u03d7-\u03d9\u03db\u03dd\u03df\u03e1\u03f4-\u0400\u040d\u0450\u045d\u0488-\u048f\u04c5\u04c6\u04c9\u04ca\u04cd\u04ce\u04ec\u04ed\u0500-\u050f\u0587\u0610-\u0615\u0653-\u0658\u066e\u066f\u06b8\u06b9\u06bf\u06cf\u06ee\u06ef\u06fa-\u07b1\u0904\u0950\u09bd\u0a01\u0a03\u0a8c\u0ad0\u0ae1-\u0ae3\u0b35\u0b71\u0cbc\u0cbd\u0d82-\u0df3\u0e2f\u0eaf\u0edc-\u0f00\u0f6a\u0f96\u0fae-\u0fb0\u0fb8\u0fba-\u1059\u10f7\u10f8\u1101\u1104\u1108\u110a\u110d\u1113-\u113b\u113d\u113f\u1141-\u114b\u114d\u114f\u1151-\u1153\u1156-\u1158\u1162\u1164\u1166\u1168\u116a-\u116c\u116f-\u1171\u1174\u1176-\u119d\u119f-\u11a2\u11a9\u11aa\u11ac\u11ad\u11b0-\u11b6\u11b9\u11bb\u11c3-\u11ea\u11ec-\u11ef\u11f1-\u11f8\u1200-\u1d6b\u2071-\u207f\u20dd-\u20e0\u20e2-\u2124\u2128\u212c\u212d\u212f-\u217f\u2183\u3006\u3038-\u303c\u3095\u3096\u309f\u30ff\u3131-\u4db5\ua000-\ua48c\uf900-\uffdc\ud800\udc00-\udbff\udfff]]",
		/*s*/"\\p{Nd}",
		/*D*/"\\P{Nd}",
		/*w*/"[^\\p{P}\\p{Z}\\p{C}]",
		/*W*/"[\\p{P}\\p{Z}\\p{C}]",
		};

		/** StringBuilder with the result of translation. */
		private final StringBuilder _result;

		/** Create instance of Translator.
		 * @param source regular expression (XML schema - XSD format).
		 */
		private Translator(String source) {
			super(source);
			_result = new StringBuilder();
		}

		/** Translate XML Schema (XSD) regular expression to Java format.
		 * @return string with Java regular expression.
		 * @throws SRuntimeException if an error occurs.
		 */
		private String translate() throws SRuntimeException {
			regExp();
			if (eos()) {
				return _result.toString();
			}
			throw new SRuntimeException(XDEF659, getIndex()); //Regex: error in expression near position &{0}
		}

////////////////////////////////////////////////////////////////////////////////
// methods providing syntax rules
////////////////////////////////////////////////////////////////////////////////
		/** regExp::= branch ( '|' branch )*
		 * @return true if regExp parsed.
		 */
		private void regExp() {
			branch();
			while (isChar('|')) {
				_result.append('|');
				branch();
			}
		}

		/** branch::= piece*
		 * @return true if branch parsed.
		 */
		private void branch() {
			while (piece()) {}
		}

		/** piece::= atom quantifier?
		 * @return true if piece parsed.
		 */
		private boolean piece() {
			if (atom()) {
				quantifier();
				return true;
			}
			return false;
		}

		/** atom::= normalChar | charClass | ( '(' regExp ')' )
		 * @return true if atom parsed.
		 */
		private boolean atom() {
			if (normalChar()) {
				return true;
			}
			if (charClass()) {
				return true;
			}
			if (isChar('(')) {
				_result.append('(');
				regExp();
				if (isChar(')')) {
					_result.append(')');
					return true;
				}
				//regex: expected "&{0}" near position &{1}
				throw new SRuntimeException(XDEF654, ")", getIndex());
			}
			return false;
		}

		/** quantifier::= [?*+] | ( '{' quantity '}' )
		 *  quantity::= quantRange | quantMin | quantExact
		 *  quantRange::= quantExact ',' quantExact
		 *  quantMin::= quantExact ','
		 *  quantExact::= [0-9]+
		 * @return true if quantifier parsed.
		 */
		private boolean quantifier() {
			char ch;
			if ((ch = isOneOfChars("?*+")) != NOCHAR) {
				_result.append(ch);
				return true;
			} else if (isChar('{')) {
				int pos = getIndex() - 1;
				if (isInteger()) {
					int i = getParsedInt();
					if (isChar(',')) {
						if (isInteger()) {
							int j = getParsedInt();
							if (i > j) {
								//Regex: lower bound of quantifier is greater than upper bound near
								// position &{0}
								throw new SRuntimeException(XDEF658, pos);
							}
						}
					}
					if (!isChar('}')) {
						//regex: expected "&{0}" near position &{1}
						throw new SRuntimeException(XDEF654, "}", getIndex());
					}
					_result.append(getBufferPart(pos, getIndex()));
					return true;
				} else {
					//Regex: expected a digit near position &{0}
					throw new SRuntimeException(XDEF655, getIndex());
				}
			}
			return false;
		}

		/** normalChar::= [^.\?*+{}()|#x5B#x5D] /* N.B.:  #x5B = '[', #x5D = ']'
		 * @return true if normalchar parsed.
		 */
		private boolean normalChar() {
			char ch;
			switch(ch = notOneOfChars(".\\?*+{}()|[]")) {
				case NOCHAR: return false;
				case '^' : _result.append("\\^"); return true;
				case '$' : _result.append("\\$"); return true;
			}
			_result.append(ch);
			return true;
		}

		/** singleCharEsc::= '\' [nrt\|.?*+(){}#x2D#x5B#x5D#x5E]
		 * @return  true if a singleCharEsc parsed.
		 */
		private boolean singleCharEsc() {
			int pos = getIndex();
			if (isChar('\\')) {
				char ch;
				if ((ch = isOneOfChars("nrt\\|.?*+(){}-[]^")) != NOCHAR) {
					_result.append('\\').append(ch);
					return true;
				}
			}
			setIndex(pos);
			return false;
		}

		/** singleCharNoEsc::= [^\#x5B#x5D]  /*  N.B.:  #x5B = '[', #x5D = ']'
		 * @return true if a singleCharNoEsc parsed.
		 */
		private boolean singleCharNoEsc() {
			char ch;
			if ((ch = notOneOfChars("\\[]")) > NOCHAR) {
				_result.append(ch);
				return true;
			}
			return false;
		}

		/** singleChar::= singleCharEsc | singleCharNoEsc
		 * @return true if a singleChar parsed.
		 */
		private boolean singleChar() {return singleCharEsc() || singleCharNoEsc();}

		/** charClass::= singleCharEsc|charClassEsc|charClassExpr|wildcardEsc
.		 * @return true if a charclass parsed.
		 */
		private boolean charClass() {
			return singleCharEsc() || charClassEsc() || charClassExpr() || wildcardEsc();
		}

		/** charClassEsc::= ( multiCharEsc | catEsc | complEsc )
		 * 	multiCharEsc::= '\' [sSiIcCdDwW]
		 *  catEsc::= '\p{' charProp '}'
		 *  complEsc::= '\P{' charProp '}'
		 *  charProp:: isCategory | isBlock
		 *  isCategory::= letters | marks | numbers | punctuation | separators | symbols | others
		 *  letters::= 'L' [ultmo]?
		 *  marks::= 'M' [nce]?
		 *  numbers::= 'N' [dlo]?
		 *  punctuation::= 'P' [cdseifo]?
		 *  separators::= 'Z' [slp]?
		 *  symbols::= 'S' [mcko]?
		 *  others::= 'C' [cfon]?
		 *  isBlock::= 'Is' [a-zA-Z0-9#x2D]+
		 * @return true if a charClassEsc parsed.
		 */
		private boolean charClassEsc() {
			if (!isChar('\\')) {
				return false;
			}
			char escChar = isLetter();
			int index = "sSiIcCdDwW".indexOf(escChar);
			if (index >= 0) {
				_result.append(MULTICHARESC[index]);
				return true;
			}
			if (escChar == 'p' || escChar == 'P') {
				if (!isChar('{')) {
					//regex: expected "&{0}" near position &{1}
					throw new SRuntimeException(XDEF654, '{', getIndex());
				}
				char c = isLetter();
				if (c == NOCHAR) {
					//regex: expected "&{0}" near position &{1}
					throw new SRuntimeException(XDEF654, "letter", getIndex());
				}
				String name = String.valueOf(c);
				c = isLetter();
				if (c != NOCHAR) {
					name += c;
					c = getCurrentChar();
					while ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c=='-') {
						name += c;
						c = nextChar();
					}
				}
				if (!isChar('}')) {
					//regex: expected "&{0}" near position &{1}
					throw new SRuntimeException(XDEF654, '}', getIndex()-1);
				}
				if (name.length() > 2 && name.startsWith("Is")) {
					name = name.substring(2);
					// isBlock
					for (String blockNameParent : BLOCKNAMESNOTDIFFERENT) {
						if (name.equals(blockNameParent)) {
							_result.append("\\").append(escChar).append('{').append("In").append(name)
								.append('}');
							return true;
						}
					}
					for (int i=0; i < BLOCKNAMESDIFFERENT.length; i+=3) {
						if (name.equals(BLOCKNAMESDIFFERENT[i])) {
							String p = BLOCKNAMESDIFFERENT[i + 1];
							String s;
							if (escChar == 'p') {
								s = p == null ? "\\"+escChar+"(In"+name+')' : p;
							} else {
								String q = BLOCKNAMESDIFFERENT[i + 2];
								s = q == null ? "\\"+escChar+"{In"+name+'}'
									: "^".equals(q) ? "[^"+p.substring(1) : q;
							}
							_result.append(s);
							return true;
						}
					}
					//regex: unrecognized Unicode block name: "&{0}" near position &{1}
					throw new SRuntimeException(XDEF651, name, getIndex());
				} else {
					for (String categoriesNotDifferent: CATEGORIESNOTDIFFERENT){
						if (name.equals(categoriesNotDifferent)) {
							_result.append("\\").append(escChar).append('{').append(name).append('}');
							return true;
						}
					}
					for (int i=0; i < CATEGORIESDIFFERENT.length; i+=3) {
						if (name.equals(CATEGORIESDIFFERENT[i])) {
							String s;
							String p = CATEGORIESDIFFERENT[i + 1];
							if (escChar == 'p') {
								s = p == null ? "\\"+escChar+"{"+name+"}" : p;
							} else {
								String q = CATEGORIESDIFFERENT[i + 2];
								s = q == null ? "\\"+escChar+"("+name+")"
									: "^".equals(q) ? "[^"+p.substring(1) : q;
							}
							_result.append(s);
						}
					}
					//regex: unrecognized Unicode block name: "&{0}" near position &{1}
					throw new SRuntimeException(XDEF651, name, getIndex());
				}
			}
			//Regex: illegal escape char &{0}{x} near position &{1}
			throw new SRuntimeException(XDEF653, escChar, getIndex());
		}

		/** charClassExpr::= '[' charGroup ']'
		 * @return true if a charClassExpr parsed
		 */
		private boolean charClassExpr() {
			int pos = getIndex();
			if (isChar('[')) {
				_result.append('[');
				if (charGroup()) {
					if (!isChar(']')) {
						//regex: expected "&{0}" near position &{1}
						throw new SRuntimeException(XDEF654, "]", getIndex());
					}
					_result.append(']');
					return true;
				}
			}
			setIndex(pos);
			return false;
		}

		/** wildcardEsc::= '.'
		 * @return true if a wildcardEsc parsed.
		 */
		private boolean wildcardEsc() {
			if (isChar('.')) {
				_result.append("[^\\n\\r]"); // ???
				return true;
			}
			return false;
		}

		/** charGroup::= (negCharGroup | posCharGroup ) ('-' charClassExpr)?
		 * @return true if a charGroup parsed.
		 */
		private boolean charGroup() {
			if (negCharGroup() || posCharGroup()) {
				if (isChar('-')) {
					_result.append('-');
					if (!charClassExpr()) {
						//regex: expected "&{0}" near position &{1}
						throw new SRuntimeException(XDEF654, "charClassExpr", getIndex());
					}
				}
				return true;
			}
			return false;
		}

		/** posCharGroup::= ( charGroupPart )+
		 * @return true if a posCharGroup parsed.
		 */
		private boolean posCharGroup() {
			if (charGroupPart()) {
				while(charGroupPart()){}
				return true;
			}
			return false;
		}

		/** negCharGroup::= '^' posCharGroup
		 * @return true if a negCharGroup parsed.
		 */
		private boolean negCharGroup() {
			if (isChar('^')) {
				_result.append('^');
				if (posCharGroup()) {
					return true;
				}
				//regex: expected "&{0}" near position &{1}
				throw new SRuntimeException(XDEF654, "CharGroup", getIndex());
			}
			return false;
		}

		/** charGroupPart::= singleChar | charRange | charClassEsc
		 * @return true if a charGroupPart parsed.
		 */
		private boolean charGroupPart() {return singleChar() || charRange() || charClassEsc();}

		/** charRange::= singleChar '-' singleChar
		 * @return true if a charRange parsed.
		 */
		private boolean charRange() {
			if (singleChar()) {
				if (isChar('-')) {
					_result.append('-');
					if (singleChar()) {
						return true;
					}
					//regex: expected "&{0}" near position &{1}
					throw new SRuntimeException(XDEF654, "singleChar", getIndex());
				}
			}
			return false;
		}
	}
}
