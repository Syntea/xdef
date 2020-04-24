package org.xdef.impl.compile;

import org.xdef.XDContainer;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.CodeOp;
import org.xdef.impl.code.CodeTable;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXQueryExpr;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.LinkedHashMap;
import org.xdef.XDValueID;
import java.util.Locale;

/** Provides the implemented methods.
 * Declared literals and declared objects
 * see GenCode, GenCodeObj, CompileExpression etc.
 * @author  Vaclav Trojan
 */
public class CompileBase implements CodeTable, XDValueID {

	////////////////////////////////////////////////////////////////////////////
	// Non public Value types
	////////////////////////////////////////////////////////////////////////////
	/** Value type: reference to attribute; used by compiler. */
	public static final short ATTR_REF_VALUE = XD_UNDEF + 1;				//47
	/** Value of PARSEITEM. */
	public static final short PARSEITEM_VALUE = ATTR_REF_VALUE + 1;			//48
	/** Value of UNIQUESET. */
	public static final short UNIQUESET_M_VALUE = PARSEITEM_VALUE + 1;		//49
	/** Value type: reference to attribute; used by compiler. */
	public static final short UNIQUESET_KEY_VALUE = UNIQUESET_M_VALUE + 1;	//50
	/** Named value of UNIQUESET. */
	public static final short UNIQUESET_NAMED_VALUE = UNIQUESET_KEY_VALUE+1;//51
	/** attribute ref, undefined type and methods which are not above a type. */
	public static final short NOTYPE_VALUE_ID = UNIQUESET_NAMED_VALUE + 1;	//52

	/** Value of UNIQUESET. */
	public static final short UNIQUESET_VALUE = NOTYPE_VALUE_ID + 1;		//53

	////////////////////////////////////////////////////////////////////////////
	//Compilation modes (context where code can be executed)
	////////////////////////////////////////////////////////////////////////////
	/** No mode */
	static final byte NO_MODE = 0;
	/** Methods associated with value of attributes or text nodes. */
	static final byte TEXT_MODE = 1;
	/** Compilation of actions associated with elements. */
	static final byte ELEMENT_MODE = 2;
	/** Compilation of actions in declaration section. */
	static final byte GLOBAL_MODE = 4;
	/** Compilation of actions in create section. */
	static final byte CREATE_MODE = 8;
	/** All modes. */
	static final byte ANY_MODE = 127;

	////////////////////////////////////////////////////////////////////////////
	// Dummy codes, used by compiler
	////////////////////////////////////////////////////////////////////////////
	/** Undefined code - used by compiler. */
	static final short UNDEF_CODE = LAST_CODE + 1;
	/** This is used for undefined objects. */
	static final CodeOp UNDEF_OPERATION = new CodeOp(XD_UNDEF, UNDEF_CODE);

	////////////////////////////////////////////////////////////////////////////
	//Value types
	////////////////////////////////////////////////////////////////////////////
	/** Array of classes corresponding to implemented types. */
	private final static String[] TYPENAMES = new String[NOTYPE_VALUE_ID];
	/** Table of type names and type IDs.*/
	private static final String TYPEIDS;
	/** Array of classes corresponding to implemented types. */
	private final static Class<?>[] TYPECLASSES = new Class<?>[NOTYPE_VALUE_ID];
	/** Table of internal methods.*/
	@SuppressWarnings("unchecked")
	private static final Map<String, InternalMethod>[] METHODS =
		(Map<String, InternalMethod>[]) new Map[NOTYPE_VALUE_ID + 1];
	/** List of predefined parsers*/
	private static final Map<String, Constructor<?>> PARSERS =
		new LinkedHashMap<String, Constructor<?>>();
	/* Error id (to ensure to generate the unique identifier).*/
	private static int _errIdIndex = 1000;

	/** Gen error identifier.*/
	static final String genErrId() {return "#UNDEF#" + _errIdIndex++;}

////////////////////////////////////////////////////////////////////////////////
// Initialization.
////////////////////////////////////////////////////////////////////////////////
	static {
		// Set type tables.
		setType(XD_VOID, "void", Void.TYPE);
		setType(XD_INT, "int", Long.TYPE);
		setType(XD_DECIMAL, "Decimal", java.math.BigDecimal.class);
		setType(XD_BIGINTEGER, "BigInteger", java.math.BigInteger.class);
		setType(XD_BOOLEAN, "boolean", Boolean.TYPE);
		setType(XD_FLOAT, "float", Double.TYPE);
		setType(XD_STRING, "String", String.class);
		setType(XD_DATETIME, "Datetime", org.xdef.sys.SDatetime.class);
		setType(XD_DURATION, "Duration", org.xdef.sys.SDuration.class);
		setType(XD_CONTAINER, "Container", org.xdef.XDContainer.class);
		setType(XD_REGEX, "Regex", org.xdef.XDRegex.class);
		setType(XD_REGEXRESULT, "RegexResult", org.xdef.XDRegexResult.class);
		setType(XD_BNFGRAMMAR,"BNFGrammar", org.xdef.XDBNFGrammar.class);
		setType(XD_BNFRULE,"BNFRule", org.xdef.XDBNFRule.class);
		setType(XD_INPUT,"Input", org.xdef.XDInput.class);
		setType(XD_OUTPUT,"Output", org.xdef.XDOutput.class);
		setType(XX_ELEMENT, "", org.xdef.proc.XXElement.class);
		setType(XX_DATA, "", org.xdef.proc.XXData.class);
		setType(XD_BYTES, "Bytes", byte[].class);
		setType(XD_ELEMENT, "Element", org.w3c.dom.Element.class);
		setType(XD_EXCEPTION, "Exception", org.xdef.XDException.class);
		setType(XD_REPORT, "Report", org.xdef.sys.Report.class);
		setType(XD_XPATH, "XpathExpr", org.xdef.xml.KXpathExpr.class);
		setType(XD_XQUERY,"XqueryExpr", org.xdef.XDXQueryExpr.class);
		setType(XD_PARSER, "Parser", org.xdef.XDParser.class);
		setType(XD_PARSERESULT, "ParseResult", org.xdef.XDParseResult.class);
		setType(XD_SERVICE, "Service", org.xdef.XDService.class);
		setType(XD_STATEMENT, "Statement", org.xdef.XDStatement.class);
		setType(XD_RESULTSET, "ResultSet", org.xdef.XDResultSet.class);
		setType(XM_MODEL, "XModel", null);
		setType(XD_NAMEDVALUE, "NamedValue", org.xdef.XDNamedValue.class);
		setType(XD_XMLWRITER, "XmlOutStream", org.xdef.XDXmlOutStream.class);
		setType(XD_LOCALE, "Locale", Locale.class);
		setType(XD_ANY, "AnyValue", org.xdef.XDValue.class);
		setType(XD_OBJECT, "Object", java.lang.Object.class);
		setType(UNIQUESET_M_VALUE, "uniqueSet", null);
		// Table of type names and typeIds
		TYPEIDS = ((char) XD_VOID) + ";void;" +
			((char) XD_ANY) + ";XDValue;" +
			((char) XD_INT) + ";long;" +
			((char) XD_INT) + ";Long;" +
			((char) XD_INT) + ";int;" +
			((char) XD_INT) + ";Integer;" +
			((char) XD_INT) + ";short;" +
			((char) XD_INT) + ";Short;" +
			((char) XD_INT) + ";byte;" +
			((char) XD_INT) + ";Byte;" +
			((char) XD_DECIMAL) + ";BigDecimal;" +
			((char) XD_BIGINTEGER) + ";BigInteger;" +
			((char) XD_FLOAT) + ";double;" +
			((char) XD_FLOAT) + ";float;" +
			((char) XD_FLOAT) + ";Double;" +
			((char) XD_FLOAT) + ";float;" +
			((char) XD_FLOAT) + ";Float;" +
			((char) XD_BOOLEAN) + ";boolean;" +
			((char) XD_BOOLEAN) + ";Boolean;" +
			((char) XD_STRING) + ";String;" +
			((char) XD_DATETIME) + ";SDatetime;" +
			((char) XD_DURATION) + ";SDuration;" +
			((char) XD_REGEX) + ";Pattern;" +
			((char) XD_REGEXRESULT) + ";Matcher;" +
			((char) XD_DURATION) + ";SDuration;" +
			((char) XD_CONTAINER) + ";XDContainer;" +
			((char) XD_BNFGRAMMAR) + ";DefBNFGrammar;" +
			((char) XD_BYTES) + ";byte[];" +
			((char) XD_LOCALE) + ";Locale;" +

			((char) XX_ELEMENT) + ";XXNode;" + //???
			((char) XX_ELEMENT) + ";XXElement;" +
			((char) XX_ATTR) + ";XXAttr;" +
			((char) XX_DATA) + ";XXData;" +
			((char) XD_ELEMENT) + ";Element;" +
			((char) XD_INPUT) + ";XDInput;" +
			((char) XD_OUTPUT) + ";XDOutput;" +
			((char) XD_REPORT) + ";Report;" +
			((char) XD_XPATH) + ";KXpathExpr;" +
			(DefXQueryExpr.isXQueryImplementation()
				 ? ((char) XD_XQUERY) + ";KXqueryExpr;" : "") +
			((char) XD_PARSER) + ";XDParser;" +
			((char) XD_PARSERESULT) + ";XDParseResult;" +
			((char) XD_SERVICE) + ";XDService;" +
			((char) XD_STATEMENT) + ";XDStatement;" +
			((char) XD_RESULTSET) + ";XDResultSet;" +
			((char) XD_NAMEDVALUE) + ";XDNamedItem;" +
			((char) XD_XMLWRITER) + ";XDXmlOutStream;";

///////////////////////////////////////////////////////////////////////////////
//  parsers
///////////////////////////////////////////////////////////////////////////////
		InternalMethod im = genParserMetnod(0, 0, null,
			XD_BOOLEAN,
			keyParam("pattern", XD_STRING, false, -1, false),
			keyParam("whiteSpace", XD_STRING, false,
					-1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseBoolean.class,
			"boolean", "?xs:boolean", "?bool");
		parser(im, org.xdef.impl.parsers.XDParseJBoolean.class,"jboolean");
		parser(im, org.xdef.impl.parsers.XDParseJNull.class, "jnull");

		im = genParserMetnod(0, 2, new short[] {XD_DECIMAL, XD_DECIMAL},
			XD_DECIMAL,
			keyParam("base", XD_STRING, true,-1,false),
			keyParam("enumeration", XD_DECIMAL, true,-1,false),
			keyParam("fractionDigits", XD_INT,false,-1,false),
			keyParam("maxExclusive", XD_DECIMAL,false,-1,false),
			keyParam("maxInclusive", XD_DECIMAL,false,1,false),
			keyParam("minExclusive", XD_DECIMAL,false,-1,false),
			keyParam("minInclusive", XD_DECIMAL,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_INT,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDecimal.class,
			"decimal", "?xs:decimal");

		im = genParserMetnod(0, 2, new short[] {XD_FLOAT, XD_FLOAT},
			XD_FLOAT,
			keyParam("base", XD_STRING, true, -1, false),
			keyParam("enumeration", XD_FLOAT, false, -1, false),
			keyParam("fractionDigits", XD_INT,false, -1, false),
			keyParam("maxExclusive", XD_FLOAT, false,-1,false),
			keyParam("maxInclusive", XD_FLOAT, false,1,false),
			keyParam("minExclusive", XD_FLOAT, false,-1,false),
			keyParam("minInclusive", XD_FLOAT, false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_INT,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDouble.class,
			"double", "?xs:double");
		parser(im, org.xdef.impl.parsers.XSParseDouble.class,
			"float", "?xs:float");

		im = genParserMetnod(0, 2, new short[] {XD_DATETIME, XD_DATETIME},
			XD_DATETIME,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_DATETIME, true,-1,false),
			keyParam("maxExclusive",XD_DATETIME,false,-1,false),
			keyParam("maxInclusive",XD_DATETIME,false,1,false),
			keyParam("minExclusive",XD_DATETIME,false,-1,false),
			keyParam("minInclusive",XD_DATETIME,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDatetime.class,
			"dateTime", "?xs:dateTime");
		parser(im, org.xdef.impl.parsers.XSParseDate.class, "date", "?xs:date");
		parser(im, org.xdef.impl.parsers.XSParseGDay.class,
			"gDay", "?xs:gDay", "?ISOday");
		parser(im, org.xdef.impl.parsers.XSParseGMonth.class,
			"gMonth", "?xs:gMonth", "?ISOmonth");
		parser(im, org.xdef.impl.parsers.XSParseGMonthDay.class,
			"gMonthDay", "?xs:gMonthDay", "?ISOmonthDay");
		parser(im, org.xdef.impl.parsers.XSParseGYear.class,
			"gYear", "?xs:gYear");
		parser(im, org.xdef.impl.parsers.XSParseGYearMonth.class,
			"gYearMonth", "?xs:gYearMonth");
		parser(im, org.xdef.impl.parsers.XSParseTime.class,
			"time", "?xs:time", "?ISOtime");
		parser(im, org.xdef.impl.parsers.XDParseISOYearMonth.class,
			"?ISOyearMonth");
		parser(im, org.xdef.impl.parsers.XDParseDateYMDhms.class, "dateYMDhms");
		parser(im, org.xdef.impl.parsers.XDParseEmailDate.class, "emailDate");
		parser(im, org.xdef.impl.parsers.XDParseISOYear.class, "?ISOyear");
		parser(im, org.xdef.impl.parsers.XDParseISODate.class, "?ISOdate");
		parser(im, org.xdef.impl.parsers.XDParseISODateTime.class,
			"?ISOdateTime");

		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT},
			XD_INT,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_INT, true, -1,false),
			keyParam("maxExclusive", XD_INT,false, -1,false),
			keyParam("maxInclusive", XD_INT,false, 1,false),
			keyParam("minExclusive", XD_INT,false, -1,false),
			keyParam("minInclusive", XD_INT,false, 0,false),
			keyParam("pattern",XD_STRING,true, -1,false),
			keyParam("totalDigits", XD_INT,false, -1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseByte.class, "byte", "?xs:byte");
		parser(im, org.xdef.impl.parsers.XSParseInteger.class,
			"integer", "?xs:integer");
		parser(im, org.xdef.impl.parsers.XSParseInt.class, "int", "?xs:int");
		parser(im, org.xdef.impl.parsers.XSParseLong.class, "long", "?xs:long");
		parser(im, org.xdef.impl.parsers.XSParseNegativeInteger.class,
			"negativeInteger", "?xs:negativeInteger");
		parser(im, org.xdef.impl.parsers.XSParseNonNegativeInteger.class,
			"nonNegativeInteger", "?xs:nonNegativeInteger");
		parser(im, org.xdef.impl.parsers.XSParseNonPositiveInteger.class,
			"nonPositiveInteger", "?xs:nonPositiveInteger");
		parser(im, org.xdef.impl.parsers.XSParsePositiveInteger.class,
			"positiveInteger", "?xs:positiveInteger");
		parser(im, org.xdef.impl.parsers.XSParseShort.class,
			"short", "?xs:short");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedByte.class,
			"unsignedByte", "?xs:unsignedByte");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedInt.class,
			"unsignedInt", "?xs:unsignedInt");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedShort.class,
			"unsignedShort", "?xs:unsignedShort");

		im = genParserMetnod(0, 2, new short[] {XD_DECIMAL, XD_DECIMAL},
			XD_DECIMAL,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_DECIMAL, true, -1,false),
			keyParam("maxExclusive", XD_DECIMAL,false,-1,false),
			keyParam("maxInclusive", XD_DECIMAL,false,1,false),
			keyParam("minExclusive", XD_DECIMAL,false,-1,false),
			keyParam("minInclusive", XD_DECIMAL,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_INT,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		//unsigned long must be decimal!
		parser(im, org.xdef.impl.parsers.XSParseUnsignedLong.class,
			"unsignedLong", "?xs:unsignedLong");

		im = genParserMetnod(0, 2, new short[] {XD_INT,XD_INT},
			XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseAnyURI.class,
			"anyURI", "?xs:anyURI");
		parser(im, org.xdef.impl.parsers.XSParseName.class, "Name", "?xs:Name");

		im = genParserMetnod(0, 2, new short[] {XD_INT,XD_INT},
			XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, 1,false),
			keyParam("minLength", XD_INT, false, 0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, false,
				new DefString("preserve"), new DefString("collapse"),
				new DefString("replace")));
		parser(im, org.xdef.impl.parsers.XSParseString.class,
			"string", "?xs:string");

		im = genParserMetnod(0, 2, new short[] {XD_INT,XD_INT},
			XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false,  -1, false,
				new DefString("replace"), new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseNormalizedString.class,
			"normalizedString", "?xs:normalizedString", "?normString");

		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT}, XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, 1,false),
			keyParam("minLength", XD_INT, false, 0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false,  -1, false,
				new DefString("preserve"), new DefString("collapse"),
				new DefString("replace")));
		parser(im, org.xdef.impl.parsers.XDParseLetters.class, "letters");
		parser(im, org.xdef.impl.parsers.XDParseAn.class, "an");
		parser(im, org.xdef.impl.parsers.XDParseNum.class, "num");
		parser(im, org.xdef.impl.parsers.XDParseCDATA.class, "CDATA");
		parser(im, org.xdef.impl.parsers.XDParseJNumber.class, "jnumber");
		parser(im, org.xdef.impl.parsers.XDParseJString.class, "jstring");

		im = genParserMetnod(0, 0, null, XD_ANY,
			keyParam("enumeration", XD_ANY, true, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false));
		parser(im, org.xdef.impl.parsers.XDParseJValue.class, "jvalue");

		im = genParserMetnod(0, 0, null, XD_ANY,
			keyParam("enumeration", XD_ANY, true, -1,false),
			keyParam("item", XD_PARSER, true, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false));
		parser(im, org.xdef.impl.parsers.XSParseUnion.class,
			"union", "?xs:union");

		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT}, XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseID.class, "ID", "?xs:ID");
		parser(im, org.xdef.impl.parsers.XSParseIDREF.class,
			"IDREF", "?xs:IDREF");
		parser(im, org.xdef.impl.parsers.XSParseENTITY.class,
			"ENTITY", "?xs:ENTITY");
		parser(im, org.xdef.impl.parsers.XSParseNMTOKEN.class,
			"NMTOKEN", "?xs:NMTOKEN", "?normToken");
		parser(im, org.xdef.impl.parsers.XSParseToken.class,
			"token", "?xs:token");
		parser(im, org.xdef.impl.parsers.XDParseCHKID.class, "CHKID");
		parser(im, org.xdef.impl.parsers.XDParseSET.class, "SET");
		parser(im, org.xdef.impl.parsers.XDParseCHKIDS.class, "CHKIDS");
		parser(im, org.xdef.impl.parsers.XSParseNOTATION.class,
			"NOTATION", "?xs:NOTATION");
		parser(im, org.xdef.impl.parsers.XSParseNCName.class,
			"NCName", "?xs:NCName", "?NCname");
		parser(im, org.xdef.impl.parsers.XSParseQName.class,
			"QName", "?xs:QName", "?Qname");
		parser(im, org.xdef.impl.parsers.XSParseLanguage.class,
			"language", "?xs:language", "?ISOlanguage");

		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT}, XD_CONTAINER,
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseIDREFS.class,
			"IDREFS", "?xs:IDREFS");
		parser(im, org.xdef.impl.parsers.XSParseENTITIES.class,
			"ENTITIES", "?xs:ENTITIES");
		parser(im, org.xdef.impl.parsers.XSParseNMTOKENS.class,
			"NMTOKENS", "?xs:NMTOKENS", "?normTokens", "?nmTokens");
		parser(im, org.xdef.impl.parsers.XDParseCHKIDS.class, "CHKIDS");

		im = genParserMetnod(0, 2, new short[] {XD_DURATION,XD_DURATION},
			XD_DURATION,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_DURATION, true,-1,false),
			keyParam("maxExclusive",XD_DURATION,false,-1,false),
			keyParam("maxInclusive",XD_DURATION,false,1,false),
			keyParam("minExclusive",XD_DURATION,false,-1,false),
			keyParam("minInclusive",XD_DURATION,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDuration.class,
			"duration", "?xs:duration", "?ISOduration");

		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT}, XD_BYTES,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_BYTES, true,  -1, false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseBase64Binary.class,
			"base64Binary", "?xs:base64Binary", "base64");
		parser(im, org.xdef.impl.parsers.XSParseHexBinary.class,
			"hexBinary", "?xs:hexBinary");
		parser(im, org.xdef.impl.parsers.XDParseHex.class, "hex");

		im = genParserMetnod(0, 1, new short[] {XD_PARSER}, XD_CONTAINER,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("item", XD_PARSER, false, 0,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, -1,false),
			keyParam("minLength", XD_INT, false, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseList.class,
			"list", "?xs:list");
////////////////////////////////////////////////////////////////////////////////
// X-Script parsers
////////////////////////////////////////////////////////////////////////////////
		im = genParserMetnod(0, 2, new short[] {XD_INT, XD_INT}, XD_STRING,
			keyParam("enumeration", XD_ANY, true,  -1, false),
			keyParam("item", XD_PARSER, true,  -1,false),
			keyParam("length", XD_INT, false,  -1, false),
			keyParam("maxLength", XD_INT, false,  1, false),
			keyParam("minLength", XD_INT, false,  0, false),
			keyParam("pattern", XD_STRING, true,  -1, false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseSequence.class,
			"sequence", "?parseSequence");

		im = genParserMetnod(0, 2, new short[]{XD_INT, XD_INT}, XD_DECIMAL,
			keyParam("enumeration", XD_DECIMAL, true, -1,false),
			keyParam("fractionDigits", XD_INT,false,1,false),
			keyParam("maxExclusive", XD_DECIMAL,false,-1,false),
			keyParam("maxInclusive", XD_DECIMAL,false,-1,false),
			keyParam("minExclusive", XD_DECIMAL,false,-1,false),
			keyParam("minInclusive", XD_DECIMAL,false,-1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_INT,false,0,false));
		parser(im, org.xdef.impl.parsers.XDParseDec.class, "dec");

		im = genParserMetnod(0, 0, null, XD_STRING,
			keyParam("enumeration", XD_BYTES, true, -1,false),
			keyParam("length", XD_INT, false, -1, true,/*fixed*/
				new DefLong(16)));
		parser(im, org.xdef.impl.parsers.XDParseMD5.class, "MD5");

		im = genParserMetnod(0, 1, new short[]{XD_ELEMENT}, XD_CONTAINER,
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, -1,false),
			keyParam("minLength", XD_INT, false, -1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("separator", XD_STRING, true, 0,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseNCNameList.class,
			"NCNameList", "?NCnameList");
		parser(im, org.xdef.impl.parsers.XDParseQNameList.class,
			"QNameList", "?QnameList");
		parser(im, org.xdef.impl.parsers.XDParseISOLanguages.class,
			"languages", "?ISOlanguages");

		im = genParserMetnod(0, 1, null, XD_STRING,
			keyParam("argument", XD_ANY, true, 0,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, -1,false),
			keyParam("minLength", XD_INT, false, -1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseQNameURI.class,
			"QNameURI", "?QnameURI");

		im = genParserMetnod(0, 1, new short[] {XD_STRING}, XD_CONTAINER,
			keyParam("argument", XD_ANY, true, 1,false),
				keyParam("enumeration", XD_STRING, true, -1,false),
				keyParam("length", XD_INT, false, -1,false),
				keyParam("maxLength", XD_INT, false, -1,false),
				keyParam("minLength", XD_INT, false, -1,false),
				keyParam("pattern",XD_STRING,true,-1,false),
				keyParam("separator", XD_STRING, true,  0, false),
				keyParam("whiteSpace", XD_STRING, false, -1, true,
					new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseQNameURIList.class,
			"QNameURIList", "?QnameListURI", "?QnameURIList");

		im = genParserMetnod(1, 2, new short[] {XD_STRING, XD_STRING},
			XD_DATETIME,
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("format", XD_STRING, true, 0,false),
			keyParam("length", XD_INT, false, -1,false),
			keyParam("maxLength", XD_INT, false, -1,false),
			keyParam("minLength", XD_INT, false, -1,false),
			keyParam("outFormat", XD_STRING, true, 1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true,
				new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseXDatetime.class,
			"xdatetime", "?datetime");

		im = genParserMetnod(0, 0, null, XD_STRING);
		parser(im, org.xdef.impl.parsers.XDParseEmpty.class, "empty");

		im = genParserMetnod(1, 1, new short[] {XD_STRING}, XD_STRING,
			keyParam("argument", XD_STRING, false,  0, false));
		parser(im, org.xdef.impl.parsers.XDParseEq.class, "eq");
		parser(im, org.xdef.impl.parsers.XDParseEqi.class, "eqi");
		parser(im, org.xdef.impl.parsers.XDParseStarts.class, "starts");
		parser(im, org.xdef.impl.parsers.XDParseStartsi.class, "startsi");
		parser(im, org.xdef.impl.parsers.XDParseEnds.class, "ends");
		parser(im, org.xdef.impl.parsers.XDParseEndsi.class, "endsi");
		parser(im, org.xdef.impl.parsers.XDParseContains.class,"contains");
		parser(im, org.xdef.impl.parsers.XDParseContainsi.class, "containsi");
		parser(im, org.xdef.impl.parsers.XDParsePic.class, "pic", "?picture");
		parser(im, org.xdef.impl.parsers.XDParseRegex.class, "regex");

		im = genParserMetnod(1, Integer.MAX_VALUE, new short[] {XD_STRING},
			XD_STRING,
			keyParam("argument", XD_CONTAINER, true,  0, false));
		parser(im, org.xdef.impl.parsers.XDParseEnum.class, "enum");
		parser(im, org.xdef.impl.parsers.XDParseEnumi.class, "enumi", "?listi");

		im = genParserMetnod(1, 1, new short[] {XD_STRING}, XD_STRING,
			keyParam("argument", XD_CONTAINER, true,  0, false));
		// This type is deprecated, replace with "list(%item=typ)"
		parser(im, org.xdef.impl.parsers.XDParseTokens.class, "?tokens");
		parser(im, org.xdef.impl.parsers.XDParseTokensi.class, "?tokensi");

		im = genParserMetnod(1, 2, new short[] {XD_ANY, XD_STRING},
			XD_STRING,
			keyParam("a2", XD_ANY, true, 1, false),
			keyParam("a1", XD_ANY, true, 0, false));
		parser(im, org.xdef.impl.parsers.XDParseBNF.class, "BNF");

		im = genParserMetnod(0, 0, new short[] {XD_PARSER}, XD_STRING);
		parser(im,org.xdef.impl.parsers.XDParseEmail.class, "email");
		parser(im,org.xdef.impl.parsers.XDParseEmailList.class,
			"emailList");
		parser(im,org.xdef.impl.parsers.XDParseFile.class, "file");
		parser(im,org.xdef.impl.parsers.XDParseFileList.class,"fileList");
		parser(im,org.xdef.impl.parsers.XDParseXDType.class, "xdType");
		parser(im,org.xdef.impl.parsers.XDParseUri.class, "uri");
		parser(im,org.xdef.impl.parsers.XDParseUriList.class, "uriList");
		parser(im,org.xdef.impl.parsers.XDParseUrl.class, "url");
		parser(im,org.xdef.impl.parsers.XDParseUrlList.class, "urlList");

		// This type is deprecated, replace with "list(%item=typ)"
		im = genParserMetnod(1, 1, new short[] {XD_PARSER}, XD_STRING,
			keyParam("item", XD_PARSER, true, 0, false));
		parser(im, org.xdef.impl.parsers.XSParseList.class, "?ListOf");
////////////////////////////////////////////////////////////////////////////////
// implemented methods
////////////////////////////////////////////////////////////////////////////////
		short ti = NOTYPE_VALUE_ID; // no base methods
		method(ti, genInternalMethod(CLEAR_REPORTS, XD_VOID,
			ANY_MODE, 0, 0), "clearReports");
		method(ti, genInternalMethod(COMPILE_REGEX, XD_REGEX, ANY_MODE, 1,1,
			XD_STRING), " new Regex", "?compilePattern");
		method(ti, genInternalMethod(DEFAULT_ERROR, XD_BOOLEAN,
			ANY_MODE, 0, 0), "defaultError");
		method(ti, genInternalMethod(GET_EASTERMONDAY, XD_DATETIME,
			ANY_MODE, 1, 1, XD_INT), "easterMonday");
		method(ti, genInternalMethod(PUT_ERROR, XD_BOOLEAN, ANY_MODE, 1, 4,
			XD_ANY, XD_STRING, XD_STRING, XD_STRING), "error");
		method(ti, genInternalMethod(GET_NUMOFERRORS, XD_INT,
			ANY_MODE, 0, 0), "errors");
		method(ti, genInternalMethod(GET_NUMOFERRORWARNINGS, XD_INT,
			ANY_MODE, 0, 0), "errorWarnings");
		method(ti, genInternalMethod(FORMAT_STRING, XD_STRING,
			ANY_MODE, 1, Integer.MAX_VALUE, XD_ANY), "format");
		method(ti, genInternalMethod(GET_XPATH_FROM_SOURCE, XD_CONTAINER,
			(byte) (TEXT_MODE + ELEMENT_MODE),
			0, 2, XD_ANY, XD_STRING), "from");
		method(ti, genInternalMethod(FROM_ELEMENT, XD_CONTAINER,
			ELEMENT_MODE, 1, 1, XD_ELEMENT), "fromElement");
		method(ti, genInternalMethod(GET_ATTR, XD_STRING,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 2,
			XD_STRING, XD_STRING), "getAttr");
		method(ti, genInternalMethod(GET_ATTR_NAME, XD_STRING,
			TEXT_MODE, 0, 0), "getAttrName");
		method(ti, genInternalMethod(GET_ELEMENT, XD_ELEMENT, //get element
			ANY_MODE, 0, 0), "getElement");
		method(ti, genInternalMethod(GET_ELEMENT_NAME, XD_STRING,
			ANY_MODE, 0, 0), "getElementName");
		method(ti, genInternalMethod(GET_ELEMENT_LOCALNAME, XD_STRING,
			ANY_MODE, 0, 0), "getElementLocalName");
		method(ti, genInternalMethod(ELEMENT_GETTEXT, XD_STRING,
			ANY_MODE, 0, 0), "getElementText");
		method(ti, genInternalMethod(GET_IMPLROPERTY, XD_STRING,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "getImplProperty");
		method(ti, genInternalMethod(GET_ITEM, XD_STRING,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 1, XD_STRING),
			"getItem", "?fromAttr");
		method(ti, genInternalMethod(GET_LASTERROR, XD_REPORT,
			ANY_MODE, 0, 0), "getLastError");
		method(ti, genInternalMethod(GET_NS,XD_STRING, ANY_MODE, 0, 2,
			XD_ANY, XD_ELEMENT), "getNamespaceURI");
		method(ti, genInternalMethod(GET_COUNTER, XD_INT,
			ANY_MODE, 0, 0), "getOccurrence", "?getCounter");
		method(ti, genInternalMethod(GET_PARSED_BOOLEAN, XD_BOOLEAN,
			ANY_MODE, 0, 0), "getParsedBoolean");
		method(ti, genInternalMethod(GET_PARSED_BYTES, XD_BYTES,
			ANY_MODE, 0, 0), "getParsedBytes");
		method(ti, genInternalMethod(GET_PARSED_DATETIME, XD_DATETIME,
			ANY_MODE, 0, 0), "getParsedDatetime");
		method(ti, genInternalMethod(GET_PARSED_DECIMAL, XD_DECIMAL,
			ANY_MODE, 0, 0), "getParsedDecimal");
		method(ti, genInternalMethod(GET_PARSED_DURATION, XD_DATETIME,
			ANY_MODE, 0, 0), "getParsedDuration");
		method(ti, genInternalMethod(GET_PARSED_DOUBLE, XD_FLOAT,
			ANY_MODE, 0, 0), "getParsedFloat");
		method(ti, genInternalMethod(GET_PARSED_LONG, XD_INT,
			ANY_MODE, 0, 0), "getParsedInt");
		method(ti, genInternalMethod(GET_PARSED_VALUE, XD_ANY,
			TEXT_MODE, 0, 0), "getParsedValue");
		method(ti, genInternalMethod(GET_QNLOCALPART, XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "getQnameLocalpart");
		method(ti, genInternalMethod(GET_QNPREFIX, XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "getQnamePrefix");
		method(ti, genInternalMethod(GET_QNAMEURI, XD_STRING,
			ANY_MODE, 1, 2, XD_ANY,XD_ELEMENT), "getQnameURI");
		method(ti, genInternalMethod(GET_ROOTELEMENT, XD_ELEMENT,
			ELEMENT_MODE, 0, 1, XD_ELEMENT), "getRootElement");
		method(ti, genInternalMethod(CONTEXT_GETTEXT, XD_STRING,
			ANY_MODE, 0, 2, XD_CONTAINER, XD_INT), "getText");
		method(ti, genInternalMethod(GET_USEROBJECT, XD_OBJECT,
			ANY_MODE, 0, 0), "getUserObject");
		method(ti, genInternalMethod(GET_XPOS, XD_STRING,
			ANY_MODE, 0, 0), "getXpos");
		method(ti, genInternalMethod(HAS_ATTR, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 2,
			XD_STRING, XD_STRING), "hasAttr");
		method(ti, genInternalMethod(IS_CREATEMODE, XD_BOOLEAN,
			ANY_MODE, 0, 0), "isCreateMode");
		method(ti, genInternalMethod(IS_DATETIME, XD_BOOLEAN,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "isDatetime");
		method(ti, genInternalMethod(IS_LEAPYEAR, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_INT), "isLeapYear");
		method(ti, genInternalMethod(IS_NUM, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_STRING), "isNumeric");
		method(ti, genInternalMethod(CREATE_ELEMENT, XD_ELEMENT,
			ANY_MODE, 0, 2, XD_STRING, XD_STRING), "newElement");
		method(ti, genInternalMethod(CREATE_ELEMENTS, XD_CONTAINER,
			ANY_MODE, 1, 3, XD_INT, XD_STRING, XD_STRING), "newElements");
		method(ti, genInternalMethod(GET_NOW, XD_DATETIME,
			ANY_MODE, 0, 0), "now");
		method(ti, genInternalMethod(GET_OCCURRENCE, XD_INT,
			(byte) (TEXT_MODE + ELEMENT_MODE), 0, 0), "occurrence");
		method(ti, genInternalMethod(OUT_STREAM, XD_VOID,  //out string
			ANY_MODE, 1, 1, XD_STRING), "out");
		method(ti, genInternalMethod(OUTLN_STREAM, XD_VOID,//outln(..)
			ANY_MODE, 0, 1, XD_STRING), "outln");
		method(ti, genInternalMethod(PRINTF_STREAM, XD_VOID,//outln(..)
			ANY_MODE, 1, Integer.MAX_VALUE, XD_ANY), "printf");
		method(ti, genInternalMethod(PARSE_DATE, XD_DATETIME,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "parseDate","?parseISODate");
		method(ti, genInternalMethod(PARSE_FLOAT, XD_FLOAT,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "parseFloat");
		method(ti, genInternalMethod(PARSE_INT,XD_INT,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "parseInt");
		method(ti, genInternalMethod(DEBUG_PAUSE, XD_VOID, // debug pause
			ANY_MODE, 0, 2, XD_ANY), "pause");
		method(ti, genInternalMethod(DEL_ATTR, XD_VOID, //remove attribute
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 2, XD_STRING, XD_STRING),
			"removeAttr");
		method(ti, genInternalMethod(REMOVE_TEXT, XD_VOID, TEXT_MODE, 0, 0),
			"removeText");
		method(ti, genInternalMethod(WHITESPACES_S, XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "removeWhiteSpaces");
		method(ti, genInternalMethod(REPLACE_S, XD_STRING,
			ANY_MODE,3,3, XD_STRING, XD_STRING, XD_STRING),"replace");
		method(ti, genInternalMethod(REPLACEFIRST_S, XD_STRING,
			ANY_MODE, 3, 3, XD_STRING, XD_STRING, XD_STRING), "replaceFirst");
		method(ti, genInternalMethod(SET_ELEMENT, XD_VOID,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 1, XD_ANY), "returnElement");
		method(ti, genInternalMethod(SET_ATTR,XD_VOID,
			(byte) (TEXT_MODE + ELEMENT_MODE), 2, 3,
			XD_STRING, XD_STRING, XD_STRING), "setAttr");
		method(ti, genInternalMethod(SET_ELEMENT, XD_VOID,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 1, XD_ANY), "setElement");
		method(ti, genInternalMethod(SET_PARSED_VALUE, XD_PARSERESULT,
			ANY_MODE, 1, 1, XD_ANY), "setParsedValue");
		method(ti, genInternalMethod(SET_TEXT, XD_VOID,
			TEXT_MODE, 1, 1, XD_STRING), "setText");
		method(ti, genInternalMethod(SET_USEROBJECT, XD_VOID,
			ANY_MODE, 1, 1, XD_OBJECT), "SetUserObject");
		method(ti, genInternalMethod(GET_STRING_TAIL, XD_STRING,
			ANY_MODE, 2, 2, XD_STRING,XD_INT), "tail");
		method(ti, genInternalMethod(TO_STRING, XD_STRING,
			ANY_MODE, 1, 2, XD_ANY,XD_STRING), "toString");
		method(ti, genInternalMethod(DEBUG_TRACE, XD_VOID, //debug trace
			ANY_MODE, 0, 2, XD_ANY), "trace");
		method(ti, genInternalMethod(TRANSLATE_S, XD_STRING,
			ANY_MODE, 3 ,3, XD_STRING,XD_STRING,XD_STRING), "translate");
		method(ti, genInternalMethod(COMPOSE_OP, XD_ELEMENT,
			ELEMENT_MODE, 1, 2, XD_STRING, XD_ELEMENT), "xcreate");
		method(ti, genInternalMethod(PARSE_XML, XD_ELEMENT,
			ANY_MODE, 1, 2, XD_ANY, XD_STRING), "xparse");
		method(ti, genInternalMethod(GET_XPATH, XD_CONTAINER,
			ANY_MODE, 1, 2, XD_STRING,XD_ANY), "xpath");
		if (DefXQueryExpr.isXQueryImplementation()) {
			method(ti, genInternalMethod(GET_XQUERY, XD_CONTAINER,
				ANY_MODE, 1, 2, XD_STRING,XD_ANY), "xquery", "?fromXQ");
		}

////////////////////////////////////////////////////////////////////////////////
// auxiliary methods
////////////////////////////////////////////////////////////////////////////////
		//getElement(list, index)
		method(ti, genInternalMethod(CONTEXT_GETELEMENT_X, XD_ELEMENT,
			ANY_MODE, 2, 2, XD_CONTAINER,XD_INT), "#getElement");
		//getText()
		method(ti, genInternalMethod(GET_TEXTVALUE, XD_STRING,
			TEXT_MODE, 0, 0), "#getValue");
////////////////////////////////////////////////////////////////////////////////
// methods above all types
////////////////////////////////////////////////////////////////////////////////
		//get name of type
		method(ti, genInternalMethod(GET_TYPENAME, XD_STRING,
			ANY_MODE, 1, 1, XD_ANY), "typeName");
		method(ti, genInternalMethod(GET_TYPEID, XD_INT,
			ANY_MODE, 1, 1, XD_ANY), "valueType");

////////////////////////////////////////////////////////////////////////////////
// ATTR REFERENCE (reference to direct attribute of the processed element)
////////////////////////////////////////////////////////////////////////////////
//		ti = ATTR_REF_VALUE;

////////////////////////////////////////////////////////////////////////////////
// ANY VALUE (touple name, value)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_ANY;
		method(ti, genInternalMethod(GET_TYPEID, XD_INT,
			ANY_MODE, 1, 1, XD_ANY), "valueType");
		method(ti, genInternalMethod(GET_TYPENAME, XD_STRING,
			ANY_MODE, 1, 1, XD_ANY), "typeName");

////////////////////////////////////////////////////////////////////////////////
// BNF GRAMMAR
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BNFGRAMMAR;
		method(ti, genInternalMethod(NEW_BNFGRAMAR, XD_BNFGRAMMAR,
			ANY_MODE, 1, 2, XD_STRING,XD_BNFGRAMMAR), "#");
		method(ti, genInternalMethod(BNF_PARSE, XD_PARSERESULT,
			ANY_MODE, 2, 3, XD_BNFGRAMMAR, XD_STRING, XD_STRING),
			"parse", "?check");
		method(ti, genInternalMethod(GET_BNFRULE, XD_BNFRULE,
			ANY_MODE, 2, 2, XD_BNFGRAMMAR, XD_STRING), "rule");

////////////////////////////////////////////////////////////////////////////////
// BNF RULE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BNFRULE;
		method(ti, genInternalMethod(BNFRULE_PARSE, XD_PARSERESULT,
			ANY_MODE, 1, 2, XD_BNFRULE, XD_STRING), "parse", "?check");

////////////////////////////////////////////////////////////////////////////////
// BYTES (array)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BYTES;
		method(ti, genInternalMethod(NEW_BYTES, XD_BYTES,
			ANY_MODE, 0, 1, XD_INT), "#");
		method(ti, genInternalMethod(BYTES_ADDBYTE, XD_VOID,
			ANY_MODE, 2, 2, XD_BYTES, XD_INT), "add");
		method(ti, genInternalMethod(BYTES_CLEAR, XD_VOID,
			ANY_MODE, 1, 1, XD_BYTES), "clear");
		method(ti, genInternalMethod(BYTES_GETAT, XD_INT,
			ANY_MODE, 2, 2, XD_BYTES, XD_INT), "getAt");
		method(ti, genInternalMethod(BYTES_INSERT, XD_VOID,
			ANY_MODE, 3, 3, XD_BYTES,XD_INT,XD_INT), "insert");
		method(ti, genInternalMethod(BYTES_REMOVE, XD_BYTES,
			ANY_MODE, 2, 3, XD_BYTES, XD_INT, XD_INT), "remove");
		method(ti, genInternalMethod(BYTES_SIZE, XD_INT,
			ANY_MODE, 1, 1, XD_BYTES), "size");
		method(ti, genInternalMethod(BYTES_SETAT, XD_VOID,
			ANY_MODE, 3, 3, XD_BYTES, XD_INT, XD_INT), "setAt");
		method(ti, genInternalMethod(BYTES_TO_BASE64, XD_STRING,
			ANY_MODE, 1, 1, XD_BYTES), "toBase64");
		method(ti, genInternalMethod(BYTES_TO_HEX, XD_STRING,
			ANY_MODE, 1, 1, XD_BYTES), "toHex");

////////////////////////////////////////////////////////////////////////////////
// CONTAINER (general object envelope)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_CONTAINER;
		method(ti, genInternalMethod(NEW_CONTEXT, XD_CONTAINER,
			ANY_MODE, 0, Integer.MAX_VALUE), "#");
		method(ti, genInternalMethod(CONTEXT_ADDITEM, XD_VOID,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_ANY), "addItem");
		method(ti, genInternalMethod(CONTEXT_GETELEMENT_X, XD_ELEMENT,
			ANY_MODE, 1, 2, XD_CONTAINER, XD_INT), "getElement");
		method(ti, genInternalMethod(CONTEXT_GETELEMENTS, XD_CONTAINER,
			ANY_MODE, 1, 2, XD_CONTAINER, XD_STRING), "getElements");
		method(ti, genInternalMethod(CONTEXT_ITEMTYPE, XD_INT,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_INT), "getItemType", "?itemType");
		method(ti, genInternalMethod(CONTEXT_GETLENGTH, XD_INT,
			ANY_MODE, 1, 1, XD_CONTAINER), "getLength");
		method(ti, genInternalMethod(GET_NAMEDVALUE, XD_ANY,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "getNamedItem");
		method(ti, genInternalMethod(GET_NAMED_AS_STRING, XD_STRING,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING),
			"getNamedString", "?fromAttr");
		method(ti, genInternalMethod(CONTEXT_GETTEXT, XD_STRING,
			ANY_MODE, 1, 2, XD_CONTAINER, XD_INT), "getText");
		method(ti, genInternalMethod(HAS_NAMEDVALUE, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "hasNamedItem");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_CONTAINER), "isEmpty");
		method(ti, genInternalMethod(CONTEXT_ITEM, XD_ANY,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_INT), "item");
		method(ti, genInternalMethod(CONTEXT_REMOVEITEM, XD_ANY,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_INT), "removeItem");
		method(ti, genInternalMethod(REMOVE_NAMEDVALUE, XD_VOID,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "removeNamedItem");
		method(ti, genInternalMethod(CONTEXT_REPLACEITEM, XD_ANY,
			ANY_MODE, 3, 3, XD_CONTAINER, XD_INT, XD_ANY), "replaceItem");
		method(ti, genInternalMethod(SET_NAMEDVALUE, XD_VOID,
			ANY_MODE, 2, 3, XD_CONTAINER,XD_ANY,XD_ANY),"setNamedItem");
		method(ti, genInternalMethod(CONTEXT_SORT, XD_CONTAINER,
			ANY_MODE, 1, 3, XD_CONTAINER, XD_STRING, XD_BOOLEAN),"sort");
		method(ti, genInternalMethod(CONTEXT_TO_ELEMENT, XD_ELEMENT,
			ANY_MODE, 1, 3, XD_CONTAINER, XD_STRING, XD_STRING), "toElement");

////////////////////////////////////////////////////////////////////////////////
// DATETIME
////////////////////////////////////////////////////////////////////////////////
		ti = XD_DATETIME;
		method(ti, genInternalMethod(PARSE_DATE, XD_DATETIME,
			ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(ADD_DAY, XD_DATETIME, //add day
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addDay");
		method(ti, genInternalMethod(ADD_HOUR, XD_DATETIME, //add hour
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addHour");
		method(ti, genInternalMethod(ADD_MILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_INT), "addMillisecond");
		method(ti, genInternalMethod(ADD_MINUTE, XD_DATETIME,  //add minute
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addMinute");
		method(ti, genInternalMethod(ADD_MONTH, XD_DATETIME, //add month
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addMonth");
		method(ti, genInternalMethod(ADD_NANOS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_INT), "addNanosecond");
		method(ti, genInternalMethod(ADD_SECOND, XD_DATETIME, //add second
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addSecond");
		method(ti, genInternalMethod(ADD_YEAR, XD_DATETIME, //add year
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "addYear");
		method(ti, genInternalMethod(GET_EASTERMONDAY, XD_DATETIME,
			ANY_MODE, 1, 1, XD_DATETIME), "easterMonday");
		method(ti, genInternalMethod(GET_DAY, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getDay");
		method(ti, genInternalMethod(GET_FRACTIONSECOND, XD_FLOAT,
			ANY_MODE, 1, 1, XD_DATETIME), "getFractionalSecond");
		method(ti, genInternalMethod(GET_HOUR, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getHour");
		method(ti, genInternalMethod(GET_MILLIS, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getMillis", "?getMillisecond");
		method(ti, genInternalMethod(GET_MINUTE, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getMinute");
		method(ti, genInternalMethod(GET_MONTH, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getMonth");
		method(ti, genInternalMethod(GET_NANOS, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getNanos", "?getNanosecond");
		method(ti, genInternalMethod(GET_SECOND, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getSecond");
		method(ti, genInternalMethod(GET_WEEKDAY, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getWeekDay");
		method(ti, genInternalMethod(GET_YEAR, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getYear");
		method(ti, genInternalMethod(GET_DAYTIMEMILLIS, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getDaytimeMillis");
		method(ti, genInternalMethod(GET_ZONEOFFSET, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "getZoneOffset");
		method(ti, genInternalMethod(GET_ZONEID, XD_STRING,
			ANY_MODE, 1, 1, XD_DATETIME), "getZoneName");
		method(ti, genInternalMethod(IS_LEAPYEAR, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_DATETIME), "isLeapYear");
		method(ti, genInternalMethod(GET_LASTDAYOFMONTH, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "lastDayOfMonth");
		method(ti, genInternalMethod(SET_DAY, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setDay");
		method(ti, genInternalMethod(SET_DAYTIMEMILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setDaytimeMillis");
		method(ti, genInternalMethod(SET_FRACTIONSECOND, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_FLOAT),"setFractionalSecond");
		method(ti, genInternalMethod(SET_HOUR, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setHour");
		method(ti, genInternalMethod(SET_MINUTE, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setMinute");
		method(ti, genInternalMethod(SET_MILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_INT),
			"setMillis", "?setMillisecond");
		method(ti, genInternalMethod(SET_MONTH, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setMonth");
		method(ti, genInternalMethod(SET_NANOS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_INT), "setNanos", "?setNanosecond");
		method(ti, genInternalMethod(SET_SECOND, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_INT), "setSecond");
		method(ti, genInternalMethod(SET_YEAR, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setYear");
		method(ti, genInternalMethod(SET_ZONEOFFSET, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_INT), "setZoneOffset");
		method(ti, genInternalMethod(SET_ZONEID, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_STRING), "setZoneName");
		method(ti, genInternalMethod(TO_MILLIS, XD_INT,
			ANY_MODE, 1, 1, XD_DATETIME), "toMillis");
		method(ti, genInternalMethod(TO_STRING, XD_STRING,
			ANY_MODE, 1, 2, XD_DATETIME, XD_STRING), "toString");

////////////////////////////////////////////////////////////////////////////////
// DURATION
///////////////////////////////
		ti = XD_DURATION;
		method(ti, genInternalMethod(PARSE_DURATION, XD_DURATION,
			ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(DURATION_GETDAYS, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getDays");
		method(ti, genInternalMethod(DURATION_GETEND, XD_DATETIME,
			ANY_MODE, 1, 1, XD_DURATION), "getEnd");
		method(ti, genInternalMethod(DURATION_GETFRACTION, XD_FLOAT,
			ANY_MODE, 1, 1, XD_DURATION), "getFractionalSecond");
		method(ti, genInternalMethod(DURATION_GETHOURS, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getHours");
		method(ti, genInternalMethod(DURATION_GETMINUTES, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getMinutes");
		method(ti, genInternalMethod(DURATION_GETMONTHS, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getMonths");
		method(ti, genInternalMethod(DURATION_GETNEXTTIME, XD_DATETIME,
			ANY_MODE, 1, 1, XD_DURATION), "getNextDate");
		method(ti, genInternalMethod(DURATION_GETRECURRENCE, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getRecurrence");
		method(ti, genInternalMethod(DURATION_GETSECONDS, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getSeconds");
		method(ti, genInternalMethod(DURATION_GETSTART, XD_DATETIME,
			ANY_MODE, 1, 1, XD_DURATION), "getStart");
		method(ti, genInternalMethod(DURATION_GETYEARS, XD_INT,
			ANY_MODE, 1, 1, XD_DURATION), "getYears");

////////////////////////////////////////////////////////////////////////////////
// ELEMENT
////////////////////////////////////////////////////////////////////////////////
		ti = XD_ELEMENT;
		method(ti, genInternalMethod(NEW_ELEMENT, XD_ELEMENT,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(ELEMENT_ADDELEMENT, XD_VOID,
			ANY_MODE, 2, 2, XD_ELEMENT, XD_ELEMENT), "addElement");
		method(ti, genInternalMethod(ELEMENT_ADDTEXT, XD_VOID,
			ANY_MODE, 2, 2, XD_ELEMENT, XD_STRING), "addText");
		method(ti, genInternalMethod(ELEMENT_CHILDNODES, XD_CONTAINER,
			ANY_MODE, 1, 1, XD_ELEMENT), "getChidNodes");
		method(ti, genInternalMethod(ELEMENT_NSURI, XD_STRING,
			ANY_MODE, 1, 1, XD_ELEMENT), "getNamespaceURI");
		method(ti, genInternalMethod(ELEMENT_NAME, XD_STRING,
			ANY_MODE, 1, 1, XD_ELEMENT), "getTagName");
		method(ti, genInternalMethod(ELEMENT_GETTEXT, XD_STRING,
			ANY_MODE, 1, 1, XD_ELEMENT), "getText");
		method(ti, genInternalMethod(ELEMENT_GETATTR, XD_STRING,
			ANY_MODE, 2, 3, XD_ELEMENT, XD_STRING, XD_STRING),
			"getAttribute", "?getAttr");
		method(ti, genInternalMethod(ELEMENT_HASATTR, XD_BOOLEAN,
			ANY_MODE, 2, 3, XD_ELEMENT, XD_STRING, XD_STRING), "hasAttribute");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_ELEMENT), "isEmpty");
		method(ti, genInternalMethod(ELEMENT_SETATTR, XD_VOID,
			ANY_MODE, 3,4,XD_ELEMENT,XD_STRING,XD_STRING,XD_STRING),
			"setAttribute", "?setAttr"); //add text child
		method(ti, genInternalMethod(ELEMENT_TOCONTEXT, XD_CONTAINER,
			ANY_MODE, 1, 1, XD_ELEMENT), "toContainer", "?toContext");
		method(ti, genInternalMethod(ELEMENT_TOSTRING, XD_STRING,
			ANY_MODE, 1, 2,XD_ELEMENT, XD_BOOLEAN), "toString");

////////////////////////////////////////////////////////////////////////////////
// EXCEPTION (internal)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_EXCEPTION;
		method(ti, genInternalMethod(NEW_EXCEPTION, XD_EXCEPTION,
			ANY_MODE, 1, 3,XD_STRING, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(GET_REPORT, XD_REPORT,
			ANY_MODE, 1, 1, XD_EXCEPTION), "getReport");
		method(ti, genInternalMethod(GET_MESSAGE, XD_STRING,
			ANY_MODE, 1, 1, XD_EXCEPTION), "getMessage");

////////////////////////////////////////////////////////////////////////////////
// LOCALE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_LOCALE;
		method(ti, genInternalMethod(NEW_LOCALE, XD_LOCALE,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_STRING), "#");

////////////////////////////////////////////////////////////////////////////////
// INPUT STREAM
////////////////////////////////////////////////////////////////////////////////
		ti = XD_INPUT;
		method(ti, genInternalMethod(NEW_INSTREAM, XD_INPUT,
			GLOBAL_MODE, 1, 3, XD_STRING,XD_STRING,XD_BOOLEAN), "#");
		method(ti, genInternalMethod(STREAM_EOF, XD_BOOLEAN,
			ANY_MODE, 0, 1, XD_INPUT), "eof");
		method(ti, genInternalMethod(STREAM_READLN, XD_STRING,
			ANY_MODE, 0, 1, XD_INPUT), "readln");

////////////////////////////////////////////////////////////////////////////////
// NAMED VALUE (touple name, value)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_NAMEDVALUE;
		method(ti, genInternalMethod(NEW_NAMEDVALUE, XD_NAMEDVALUE,
			ANY_MODE, 2, 2, XD_STRING, XD_ANY), "#");
		method(ti, genInternalMethod(NAMEDVALUE_NAME, XD_STRING,
			ANY_MODE, 1, 1,XD_NAMEDVALUE), "getName");
		method(ti, genInternalMethod(NAMEDVALUE_GET, XD_ANY,
			ANY_MODE, 1, 1, XD_NAMEDVALUE), "getValue");
		method(ti, genInternalMethod(NAMEDVALUE_SET, XD_VOID,
			ANY_MODE, 2, 3, XD_NAMEDVALUE, XD_ANY), "setValue");

////////////////////////////////////////////////////////////////////////////////
// OBJECT (not used)
////////////////////////////////////////////////////////////////////////////////
//		ti = XD_OBJECT;

////////////////////////////////////////////////////////////////////////////////
// OUTPUT STREAM
////////////////////////////////////////////////////////////////////////////////
		ti = XD_OUTPUT;
		method(ti, genInternalMethod(NEW_OUTSTREAM, XD_OUTPUT,
			GLOBAL_MODE, 1, 3, XD_STRING,XD_STRING,XD_BOOLEAN), "#");
		method(ti, genInternalMethod(PUT_ERROR1, XD_BOOLEAN,
			ANY_MODE, 2, 4, XD_OUTPUT, XD_STRING, XD_STRING, XD_STRING),
			"error");
		method(ti, genInternalMethod(GET_REPORT, XD_REPORT,
			ANY_MODE, 1, 1, XD_OUTPUT), "getLastError");
		method(ti, genInternalMethod(OUT1_STREAM, XD_VOID,
			ANY_MODE, 2, 2, XD_OUTPUT, XD_STRING), "out");
		method(ti, genInternalMethod(OUTLN1_STREAM, XD_VOID,
			ANY_MODE, 1, 2, XD_OUTPUT, XD_STRING), "outln");
		method(ti, genInternalMethod(PRINTF_STREAM, XD_VOID,
			ANY_MODE, 2, Integer.MAX_VALUE, XD_OUTPUT, XD_ANY), "printf");
		method(ti, genInternalMethod(PUT_REPORT, XD_VOID,
			ANY_MODE, 2, 2, XD_OUTPUT, XD_REPORT), "putReport");
////////////////////////////////////////////////////////////////////////////////
// PARSER
////////////////////////////////////////////////////////////////////////////////
		ti = XD_PARSER;
//		method(ti, genInternalMethod(NEW_PARSER, XD_PARSER,
//			ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(PARSE_OP, XD_PARSERESULT,
			ANY_MODE, 1, 2, XD_PARSER, XD_STRING), "parse", "?check");

////////////////////////////////////////////////////////////////////////////////
// PARSERESULT (result of parsing by parsers)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_PARSERESULT;
		method(ti, genInternalMethod(NEW_PARSERESULT, XD_PARSERESULT,
			ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(GET_PARSED_BOOLEAN, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_PARSERESULT), "booleanValue");
		method(ti, genInternalMethod(GET_PARSED_BYTES, XD_BYTES,
			ANY_MODE, 1, 1, XD_PARSERESULT), "bytesValue");
		method(ti, genInternalMethod(PARSERESULT_MATCH, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_PARSERESULT), "matches", "?check");
		method(ti, genInternalMethod(GET_PARSED_DATETIME, XD_DATETIME,
			ANY_MODE, 1, 1, XD_PARSERESULT), "datetimeValue");
		method(ti, genInternalMethod(GET_PARSED_DURATION, XD_DURATION,
			ANY_MODE, 1, 1, XD_PARSERESULT), "durationValue");
		method(ti, genInternalMethod(GET_PARSED_DECIMAL, XD_DECIMAL,
			ANY_MODE, 1, 1, XD_PARSERESULT), "decimalValue");
		method(ti, genInternalMethod(GET_PARSED_LONG, XD_INT,
			ANY_MODE, 1, 1, XD_PARSERESULT), "intValue");
		method(ti, genInternalMethod(SET_PARSED_ERROR, XD_PARSERESULT,
			ANY_MODE, 2, 4, XD_PARSERESULT, XD_STRING, XD_STRING, XD_STRING),
			"error", "?setError");
		method(ti, genInternalMethod(GET_PARSED_DOUBLE, XD_FLOAT,
			ANY_MODE, 1, 1, XD_PARSERESULT), "floatValue");
		method(ti, genInternalMethod(GET_PARSED_ERROR, XD_REPORT,
			ANY_MODE, 1, 1, XD_PARSERESULT), "getError");
		method(ti, genInternalMethod(GET_PARSED_STRING, XD_STRING,
			ANY_MODE, 1, 1, XD_PARSERESULT), "getParsedString");
		method(ti, genInternalMethod(GET_PARSED_VALUE, XD_ANY,
			ANY_MODE, 1, 1, XD_PARSERESULT), "getValue");
		method(ti, genInternalMethod(SET_PARSED_STRING, XD_PARSERESULT,
			ANY_MODE, 2, 2, XD_PARSERESULT, XD_STRING),"setParsedString");
		method(ti, genInternalMethod(SET_PARSED_VALUE, XD_PARSERESULT,
			ANY_MODE, 2, 2, XD_PARSERESULT, XD_ANY), "setValue");

////////////////////////////////////////////////////////////////////////////////
// REGEX
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REGEX;
		method(ti, genInternalMethod(COMPILE_REGEX, XD_REGEX,
			ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(GET_REGEX_RESULT, XD_REGEXRESULT,
			ANY_MODE, 2, 2, XD_REGEX, XD_STRING), "getMatcher");
		method(ti, genInternalMethod(MATCHES_REGEX, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_REGEX, XD_STRING), "matches");

////////////////////////////////////////////////////////////////////////////////
// REGEX RESULT
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REGEXRESULT;
		//get group end index from regex result
		method(ti, genInternalMethod(GET_REGEX_GROUP_END, XD_INT,
			ANY_MODE, 2, 2, XD_REGEXRESULT, XD_INT), "end");
		method(ti, genInternalMethod(GET_REGEX_GROUP, XD_STRING,
			ANY_MODE, 2, 2, XD_REGEXRESULT, XD_INT), "group");
		method(ti, genInternalMethod(GET_REGEX_GROUP_NUM, XD_INT,
			ANY_MODE, 1, 1, XD_REGEXRESULT), "groupCount");
		method(ti, genInternalMethod(MATCHES_REGEX, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_REGEXRESULT), "matches");
		method(ti, genInternalMethod(GET_REGEX_GROUP_START, XD_INT,
			ANY_MODE, 2, 2, XD_REGEXRESULT, XD_INT), "start");

////////////////////////////////////////////////////////////////////////////////
// REPORT (see org.xdef.sys.Report)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REPORT;
		method(ti, genInternalMethod(NEW_REPORT, XD_REPORT,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(REPORT_GETPARAM, XD_STRING,
			ANY_MODE, 2, 2, XD_REPORT, XD_STRING), "getParameter");
		method(ti, genInternalMethod(REPORT_SETPARAM, XD_REPORT,
			ANY_MODE, 3, 3, XD_REPORT, XD_STRING, XD_STRING), "setParameter");
		method(ti, genInternalMethod(REPORT_SETTYPE, XD_REPORT,
			ANY_MODE, 2, 2, XD_REPORT, XD_STRING), "setType");
		method(ti, genInternalMethod(REPORT_TOSTRING, XD_STRING,
			ANY_MODE, 1, 1, XD_REPORT), "toString");

////////////////////////////////////////////////////////////////////////////////
// RESULT SET (result of statement of service)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_RESULTSET;
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID,
			ANY_MODE, 1, 1, XD_RESULTSET), "close");
		method(ti, genInternalMethod(DB_CLOSESTATEMENT, XD_VOID,
			ANY_MODE, 1, 1, XD_RESULTSET), "closeStatement");
		method(ti, genInternalMethod(GET_RESULTSET_COUNT, XD_INT,
			(byte) (TEXT_MODE + ELEMENT_MODE),1,1,XD_RESULTSET),"getCount");
		method(ti, genInternalMethod(GET_RESULTSET_ITEM, XD_STRING,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 2,
			XD_RESULTSET, XD_STRING), "getItem");
		method(ti, genInternalMethod(HAS_RESULTSET_ITEM, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEMENT_MODE), 2, 2,
			XD_RESULTSET, XD_STRING), "hasItem");
		method(ti, genInternalMethod(HAS_RESULTSET_NEXT, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 1,XD_RESULTSET),"hasNext");
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_RESULTSET), "isClosed");
		method(ti, genInternalMethod(RESULTSET_NEXT, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEMENT_MODE), 1, 1, XD_RESULTSET), "next");

////////////////////////////////////////////////////////////////////////////////
// SERVICE (e.g. database)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_SERVICE;
		method(ti, genInternalMethod(NEW_SERVICE, XD_SERVICE,
			ANY_MODE, 4, 4,
			XD_STRING,XD_STRING,XD_STRING,XD_STRING), "#");
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID, //DB close
			ANY_MODE, 1, 1, XD_SERVICE), "close");
		method(ti, genInternalMethod(DB_COMMIT, XD_VOID, //DB commit
			ANY_MODE, 1, 1, XD_SERVICE), "commit");
		method(ti, genInternalMethod(DB_EXEC, XD_BOOLEAN,
			ANY_MODE, 2, Integer.MAX_VALUE, //mode, min, max parameters
			XD_SERVICE, XD_STRING), "execute");
		method(ti, genInternalMethod(HAS_DBITEM, XD_BOOLEAN,
			ANY_MODE, 2, Integer.MAX_VALUE, //mode, min, max parameters
			XD_SERVICE, XD_STRING, XD_STRING), "hasItem");
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_SERVICE), "isClosed");
		method(ti, genInternalMethod(DB_PREPARESTATEMENT, XD_STATEMENT,
			ANY_MODE, 2, 2, XD_SERVICE, XD_STRING), "prepareStatement");
		method(ti, genInternalMethod(GET_DBQUERY, XD_RESULTSET,
			ANY_MODE, 2, Integer.MAX_VALUE,
			XD_SERVICE, XD_STRING, XD_STRING), "query");
		method(ti, genInternalMethod(GET_DBQUERY_ITEM, XD_RESULTSET,
			ANY_MODE, 3, Integer.MAX_VALUE, //mode, min, max parameters
			XD_SERVICE, XD_STRING, XD_STRING), "queryItem");
		method(ti, genInternalMethod(DB_ROLLBACK, XD_VOID,
			ANY_MODE, 1, 1, XD_SERVICE), "rollback");
		method(ti, genInternalMethod(DB_SETPROPERTY, XD_BOOLEAN,
			ANY_MODE, 3, 3,
			XD_SERVICE, XD_STRING, XD_STRING), "setProperty");

////////////////////////////////////////////////////////////////////////////////
// STATEMENT (on service, e.g. database)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_STATEMENT;
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID,
			ANY_MODE, 1, 1, XD_STATEMENT), "close");
		method(ti, genInternalMethod(DB_EXEC, XD_BOOLEAN, //execute
			ANY_MODE, 1, Integer.MAX_VALUE, //mode, min, max parameters
			XD_STATEMENT, XD_STRING), "execute");
		method(ti, genInternalMethod(HAS_DBITEM, XD_BOOLEAN,
			ANY_MODE, 1, Integer.MAX_VALUE, //mode, min, max parameters
			XD_STATEMENT, XD_STRING), "hasItem");
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_STATEMENT), "isClosed");
		method(ti, genInternalMethod(GET_DBQUERY, XD_RESULTSET,
			ANY_MODE, 1, Integer.MAX_VALUE,
			XD_STATEMENT, XD_STRING), "query");
		method(ti, genInternalMethod(GET_DBQUERY_ITEM, XD_RESULTSET,
			ANY_MODE, 2, Integer.MAX_VALUE, //mode, min, max parameters
			XD_STATEMENT, XD_STRING), "queryItem");

////////////////////////////////////////////////////////////////////////////////
// STRING
////////////////////////////////////////////////////////////////////////////////
		ti = XD_STRING;
		method(ti, genInternalMethod(CONTAINS, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING,XD_STRING), "contains");
		method(ti, genInternalMethod(CONTAINSI, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "containsi");
		method(ti, genInternalMethod(CUT_STRING, XD_STRING,
			ANY_MODE, 2, 2, XD_STRING, XD_INT), "cut");
		method(ti, genInternalMethod(ENDSWITH, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "endsWith");
		method(ti, genInternalMethod(ENDSWITHI, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "endsWithi");
		method(ti,genInternalMethod(CMPEQ, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "equals");
		method(ti,genInternalMethod(EQUALSI, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "equalsIgnoreCase");
		method(ti, genInternalMethod(GET_BYTES_FROM_STRING, XD_BYTES,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "getBytes");
		method(ti, genInternalMethod(GET_INDEXOFSTRING, XD_INT,
			ANY_MODE, 2, 3, XD_STRING, XD_STRING, XD_INT), "indexOf");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_STRING), "isEmpty");
		method(ti, genInternalMethod(GET_LASTINDEXOFSTRING, XD_INT,
			ANY_MODE, 2, 3, XD_STRING, XD_STRING, XD_INT), "lastIndexOf");
		method(ti, genInternalMethod(GET_STRING_LENGTH, XD_INT, //length
			ANY_MODE, 1, 1, XD_STRING), "length");
		method(ti, genInternalMethod(STARTSWITH, XD_BOOLEAN,//check prefix
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "startsWith");
		method(ti, genInternalMethod(STARTSWITHI, XD_BOOLEAN,//startWith
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "startsWithi");
		method(ti, genInternalMethod(GET_SUBSTRING, XD_STRING,
			ANY_MODE, 2, 3, XD_STRING, XD_INT, XD_INT), "substring");
		method(ti, genInternalMethod(LOWERCASE,	XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "toLower");
		method(ti, genInternalMethod(UPPERCASE, XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "toUpper");
		method(ti, genInternalMethod(TRIM_S, XD_STRING,
			ANY_MODE, 1, 1, XD_STRING), "trim");

////////////////////////////////////////////////////////////////////////////////
// UNIQUESET (key, keyref)
////////////////////////////////////////////////////////////////////////////////
//		ti = UNIQUESET_VALUE;
//		method(ti, genInternalMethod(UNIQUESET_ID, XD_PARSERESULT,//check ID
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE, XD_PARSERESULT), "ID");
//		method(ti, genInternalMethod(UNIQUESET_SET, XD_PARSERESULT,
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE,XD_PARSERESULT), "SET");
//		method(ti, genInternalMethod(UNIQUESET_IDREF, XD_PARSERESULT,
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE), "IDREF");
//		method(ti, genInternalMethod(UNIQUESET_IDREFS, XD_PARSERESULT,
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE,XD_PARSERESULT), "IDREFS");
//		method(ti, genInternalMethod(UNIQUESET_CHKID, XD_PARSERESULT,
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE,XD_PARSERESULT), "CHKID");
//		method(ti, genInternalMethod(UNIQUESET_CHKIDS, XD_PARSERESULT,
//			TEXT_MODE, 1, 2, UNIQUESET_VALUE,XD_PARSERESULT), "CHKIDS");
//		method(ti, genInternalMethod(UNIQUESET_CLOSE, XD_BOOLEAN,
//			ELEMENT_MODE, 1, 1, UNIQUESET_VALUE), "CLEAR");

////////////////////////////////////////////////////////////////////////////////
// UNIQUESET_KEY_VALUE (part of key list)
////////////////////////////////////////////////////////////////////////////////
		ti = UNIQUESET_KEY_VALUE;
		method(ti, genInternalMethod(UNIQUESET_KEY_ID, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE, XD_PARSERESULT), "ID");
		method(ti, genInternalMethod(UNIQUESET_KEY_SET, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE, XD_PARSERESULT), "SET");
		method(ti, genInternalMethod(UNIQUESET_KEY_IDREF, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE, XD_PARSERESULT), "IDREF");
		method(ti, genInternalMethod(UNIQUESET_KEY_CHKID, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE, XD_PARSERESULT), "CHKID");
		// following two methods
		method(ti, genInternalMethod(UNIQUESET_IDREFS, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE, XD_PARSERESULT), "IDREFS");
		method(ti, genInternalMethod(UNIQUESET_CHKIDS, XD_PARSERESULT,
			TEXT_MODE, 1, 2, UNIQUESET_KEY_VALUE,XD_PARSERESULT), "CHKIDS");

////////////////////////////////////////////////////////////////////////////////
// UNIQUESET_M_VALUE (Multiple key uniqueset)
////////////////////////////////////////////////////////////////////////////////
		ti = UNIQUESET_M_VALUE;
		method(ti, genInternalMethod(UNIQUESET_M_ID, XD_BOOLEAN,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 2, UNIQUESET_M_VALUE), "ID");
		method(ti, genInternalMethod(UNIQUESET_M_SET, XD_BOOLEAN,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 2, UNIQUESET_M_VALUE), "SET");
		method(ti, genInternalMethod(UNIQUESET_M_IDREF, XD_VOID,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 2,
			UNIQUESET_M_VALUE,XD_PARSERESULT), "IDREF");
		method(ti, genInternalMethod(UNIQUESET_M_CHKID, XD_VOID,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 2, UNIQUESET_M_VALUE,
			XD_PARSERESULT), "CHKID");
		method(ti, genInternalMethod(UNIQUESET_M_NEWKEY, XD_VOID,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 1, UNIQUESET_M_VALUE), "NEWKEY");
		method(ti, genInternalMethod(UNIQUESET_CLOSE, XD_VOID,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 1, UNIQUESET_M_VALUE), "CLEAR");
		method(ti, genInternalMethod(UNIQUESET_CHEKUNREF, XD_VOID,
			(byte)(TEXT_MODE+ELEMENT_MODE), 1, 1,
			UNIQUESET_M_VALUE), "checkUnref");
		method(ti, genInternalMethod(UNIQUESET_M_SIZE, XD_INT,
			ANY_MODE, 1, 1, UNIQUESET_M_VALUE), "size");
		method(ti, genInternalMethod(UNIQUESET_M_TOCONTAINER, XD_CONTAINER,
			ANY_MODE, 1, 1, UNIQUESET_M_VALUE), "toContainer");

////////////////////////////////////////////////////////////////////////////////
// XML Writer (output XML stream)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_XMLWRITER;
		method(ti, genInternalMethod(NEW_XMLWRITER, XD_XMLWRITER,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_BOOLEAN), "#");
		method(ti, genInternalMethod(SET_XMLWRITER_INDENTING, XD_VOID,
			ANY_MODE, 2, 2, XD_XMLWRITER, XD_BOOLEAN), "setIndenting");
		method(ti, genInternalMethod(WRITE_ELEMENT_START, XD_VOID,
			ANY_MODE, 1, 2,XD_XMLWRITER, XD_ELEMENT),"writeElementStart");
		method(ti, genInternalMethod(WRITE_ELEMENT_END, XD_VOID,
			ANY_MODE, 1, 1, XD_XMLWRITER), "writeElementEnd");
		method(ti, genInternalMethod(WRITE_ELEMENT, XD_VOID,
			ANY_MODE, 1, 2, XD_XMLWRITER, XD_ELEMENT), "writeElement");
		method(ti, genInternalMethod(WRITE_TEXTNODE, XD_VOID,
			ANY_MODE, 1, 2, XD_XMLWRITER, XD_STRING), "writeText");
		method(ti, genInternalMethod(CLOSE_XMLWRITER, XD_VOID,
			ANY_MODE, 1, 1, XD_XMLWRITER), "close");
	}

////////////////////////////////////////////////////////////////////////////////

	/** Set type parameters.
	 * @param type type ID (see org.xdef.XDValueID).
	 * @param name the name of type used in script.
	 * @param clazz the class rep[resenting the type.
	 * @param typeCodeAbbr the abbreviation used in code display.
	 */
	private static void setType(final short type,
		final String name,
		final Class<?> clazz) {
		TYPECLASSES[type] = clazz;
		TYPENAMES[type] = name;
	}

	/** Register parser.
	 * @param im Internal method object.
	 * @param clazz class of parser.
	 * @param names alias names of the parser. If a name starts with "?" then
	 * that item is deprecated (and the previous item is a recommended parser
	 * name - i.e. alias name can't be the first one).
	 */
	private static void parser(final InternalMethod im,
		final Class<?> clazz,
		final String... names) {
		try {
			Constructor<?> c = ((Class<?>) clazz).getConstructor();
			Map<String, InternalMethod> hm;
			if ((hm = METHODS[NOTYPE_VALUE_ID]) == null) {
				METHODS[NOTYPE_VALUE_ID] = hm =
					new LinkedHashMap<String, InternalMethod>();
			}
			for (int i = 0; i < names.length; i++) {
				InternalMethod im1 = im;
				String name = names[i];
				if (name.charAt(0) == '?') {
					name = name.substring(1);
					String recommended = null;
					int j = i;
					// find name without leading '?'
					while (j > 0 && (recommended = names[--j]) != null
						&& recommended.charAt(0) == '?') {}
					if (recommended != null) {
						im1 = new InternalMethod(im, recommended);
					}
				}
				hm.put(name, im1);
				PARSERS.put(name, c);
			}
		} catch (Exception ex) {
			throw new Error("Internal error: " + ex);
		}
	}

	/** Register method.
	 * @param typeId id of given type.
	 * that item is deprecated (and the previous item is a recommended method
	 * name - i.e. alias name can't be the first one).
	 * @param im Internal method object.
	 */
	private static void method(final short typeId,
		final InternalMethod im,
		final String... names) {
		Map<String, InternalMethod> hm;
		if ((hm = METHODS[typeId]) == null) {
			METHODS[typeId] = hm = new LinkedHashMap<String, InternalMethod>();
		}
		for (int i = 0; i < names.length; i++) {
			InternalMethod im1 = im;
			String name = names[i];
			if (name.charAt(0) == '?') {
				name = name.substring(1);
				int j = i;
				String recommended = null;
				while (j > 0 && (recommended = names[--j]) != null
					&& recommended.charAt(0) == '?') {}
				if (recommended != null) {
					im1 = new InternalMethod(im, recommended);
				}
			}
			hm.put(name, im1);
		}
	}

	/** Create KeyParam (descriptor of key parameter).
	 * @param key name of parameter.
	 * @param type type of parameter value.
	 * @param list true if parameter is list of values.
	 * @param seqIndex sequential parameter index or -1.
	 * @param fixed true if value is fixed.
	 * @param legalValues default value of parameter or <tt>null</tt>.
	 * @return created KeyParam object
	 */
	private static KeyParam keyParam(final String key,
		final short type,
		final boolean list,
		final int seqIndex,
		final boolean fixed,
		final XDValue... legalValues) {
		return new KeyParam(key,type,list,seqIndex,fixed,false,legalValues);
	}

	/** Create internal method descriptor of parser with keyword parameters.
	 * Note the parameter code is fixed value LD_CONST,
 parameter restrictions is ANY_MODE and resultType is XD_PARSER.
	 * @param minPars Minimal number of parameters.
	 * @param maxPars Maximal number of parameters.
	 * @param paramTypes List of type id's of parameters.
	 * @param parsedResult type of parsed result.
	 * @param keyparams keyword parameters.
	 */
	private static InternalMethod genParserMetnod(final int minPars,
		final int maxPars,
		final short[] paramTypes,
		final short parsedResult,
		KeyParam... keyparams) {
		return new InternalMethod(LD_CONST, XD_PARSER, parsedResult,
			ANY_MODE, minPars, maxPars, paramTypes, keyparams);
	}

////////////////////////////////////////////////////////////////////////////////
// Methods called from classes of compiler.
////////////////////////////////////////////////////////////////////////////////

	/** Create new internal method descriptor with void parsedResult type.
	 * @param code The code of method.
	 * @param resultType Id of the result type.
	 * @param restrictions Modes where the method is allowed.
	 * @param minPars Minimal number of parameters.
	 * @param maxPars Maximal number of parameters.
	 * @param paramTypes List of type id's of parameters.
	 */
	static InternalMethod genInternalMethod(final short code,
		final short resultType,
		final byte restrictions,
		final int minPars,
		final int maxPars,
		final short... paramTypes) {
		return new InternalMethod(code, resultType, XD_VOID,
			restrictions, minPars, maxPars, paramTypes, (KeyParam[]) null);
	}

	/** Get type ID from class name.
	 * @param className the name of class.
	 * @param classLoader the class loader used for the project.
	 * @return type ID or XD_UNDEF.
	 */
	static short getClassTypeID(final String className,
		final ClassLoader classLoader) {
		int ndx = TYPEIDS.indexOf(';' + className + ';');
		if (ndx > 0) {
			return (short) TYPEIDS.charAt(ndx - 1);
		}
		try {
			Class<?> clazz = Class.forName(className, false, classLoader);
			for (short i = 0; i < TYPECLASSES.length; i++) {
				if (clazz.equals(TYPECLASSES[i])) {
					return i;
				}
			}
		} catch (Exception ex) {} // we ignore exception, returns XD_UNDEF.
		return XD_UNDEF;
	}

	/** Get type ID from class.
	 * @param clazz class representing type.
	 * @return type ID or XD_UNDEF.
	 */
	static short getClassTypeID(final Class<?> clazz) {
		if (clazz == null || clazz.equals(java.lang.Void.TYPE) ||
			clazz.equals(java.lang.Void.class)) {
			return XD_VOID;
		} else if (clazz.equals(Long.TYPE) || clazz.equals(Long.class) ||
			clazz.equals(Integer.TYPE) || clazz.equals(Integer.class) ||
			clazz.equals(Short.TYPE) || clazz.equals(Short.class) ||
			clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)) {
			return XD_INT;
		} else if (clazz.equals(Float.TYPE) || clazz.equals(Float.class) ||
			clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
			return XD_FLOAT;
		} else if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
			return XD_BOOLEAN;
		}
		for (short i = 0; i < TYPECLASSES.length; i++) {
			if (TYPECLASSES[i] != null && clazz.equals(TYPECLASSES[i])) {
				return i;
			}
		}
		return XD_UNDEF;
	}

	/** Get type id from type name.
	 * @param name The name of type.
	 * @return The type Id or -1.
	 */
	public static short getTypeId(final String name) {
		for (short i = 0; i < NOTYPE_VALUE_ID; i++) {
			if (name.equals(TYPENAMES[i])) {
				return i;
			}
		}
		return -1;
	}

	/** Get type of parsed result of method.
	 * @param name of method,
	 * @return type of parsed result.
	 */
	static short getParsedType(final String name) {
		if (name == null) {
			return XD_STRING;
		}
		InternalMethod m = getTypeMethod(NOTYPE_VALUE_ID, name);
		return (m == null) ? XD_STRING : m.getParsedResult();
	}

	/** Get internal method.
	 * @param type base type.
	 * @param name name of method.
	 * @return InternalMethod object.
	 */
	public final static InternalMethod getTypeMethod(final short type,
		final String name) {
		Map<String, InternalMethod> hm = METHODS[type];
		return hm == null ? null : hm.get(name);
	}

	/** Return name of method or null. */
	static short methodType(final short code) {
		for (Map<String, InternalMethod> item : METHODS) {
			if (item == null) {
				continue;
			}
			for (InternalMethod im: item.values()) {
				if (im.getCode() == code) {
					return im._resultType;
				}
			}
		}
		return XD_UNDEF;
	}

	/** Get class object corresponding to the type.
	 * @param type type id.
	 * @return the Class object corresponding to type argument.
	 */
	static Class<?> getTypeClass(short type) {
		Class<?> result = TYPECLASSES[type];
		return result == null
			? org.xdef.impl.compile.CodeUndefined.class : result;
	}

	/** Get instance of object parser with given name.
	 * @param name of parser.
	 * @return instance of object of parser with given name.
	 */
	public static final XDParser getParser(final String name) {
		try {
			return (XDParser) ((Constructor)PARSERS.get(name)).newInstance();
		} catch (Exception ex) {
			return null;
		}
	}

	/** Get type name from type id.
	 * @param type The type id.
	 * @return The type name or null.
	 */
	public static final String getTypeName(final short type) {
		return type >= 0 && type < TYPENAMES.length
			? TYPENAMES[type] : "UNDEF_VALUE";
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used by compiler.
////////////////////////////////////////////////////////////////////////////////

	/** Description item of an internal method. */
	public final static class InternalMethod {
		/** Code of the method.*/
		private final short _code;
		/** Minimal number of parameters.*/
		private final int _minParams;
		/** Maximal number of parameters.*/
		private final int _maxParams;
		/** List of id's of types of parameters.*/
		private final short[] _paramTypes;
		/** Id of the result type.*/
		private final short _resultType;
		/** Set of modes where the method is allowed.*/
		private final byte _restrictions;
		/** Keyword parameters or null. */
		private final KeyParam[] _keyparams;
		/** Keyword names oof sequential parameters. */
		private final String[] _sqKeynames;
		/** Result of parsed object (only parsers). */
		private final short _parsedResult;
		/** Text of recommendation for deprecated methods. */
		private final String _recommended;

		/** Create new internal method descriptor of method with
		 * keyword parameters.
		 * @param code The code of method.
		 * @param restrictions Modes where the method is allowed.
		 * @param minPars Minimal number of parameters.
		 * @param maxPars Maximal number of parameters.
		 * @param paramTypes List of type id's of parameters.
		 * @param resultType Id of the result type.
		 * @param parsedResult type of parsed result.
		 * @param keyparams keyword parameters.
		 */
		private InternalMethod(final short code,
			final short resultType,
			final short parsedResult,
			final byte restrictions,
			final int minPars,
			final int maxPars,
			final short[] paramTypes,
			KeyParam... keyparams) {
			_code = code;
			_restrictions = restrictions;
			_minParams = minPars;
			_maxParams = maxPars;
			_paramTypes = paramTypes;
			_resultType = resultType;
			_parsedResult = parsedResult;
			_keyparams = keyparams;
			if (_keyparams == null) {
				_sqKeynames = null;
			} else {
				int maxpar = -1;
				for (int i = 0; i < _keyparams.length; i++) {
					int j = _keyparams[i].getSeqIndex();
					if (j > maxpar) {
						maxpar = j;
					}
				}
				_sqKeynames = new String[maxpar+1];
				for (int i = 0; i < _keyparams.length; i++) {
					int j = _keyparams[i].getSeqIndex();
					if (j >= 0) {
						_sqKeynames[j] = _keyparams[i].getName();
					}
				}
			}
			 _recommended = null;
		}

		/** Create clone of deprecated InternalMethod object and add the
		 * recommended name.
		 * @param x original InternalMethod.
		 * @param recommended name of recommended method instead of this one.
		 */
		private InternalMethod (final InternalMethod x,
			final String recommended) {
			_code = x._code;
			_minParams = x._minParams;
			_maxParams = x._maxParams;
			_paramTypes = x._paramTypes;
			_resultType = x._resultType;
			_restrictions = x._restrictions;
			_keyparams = x._keyparams;
			_sqKeynames = x._sqKeynames;
			_parsedResult = x._parsedResult;
			_recommended = recommended;
		}

		final short getCode() {return _code;}
		final byte getRestrictions() {return _restrictions;}

		public final int getMinParams() {return _minParams;}
		public final int getMaxParams() {return _maxParams;}
		public final String[] getSqParamNames() {return _sqKeynames;}
		public final short[] getParamTypes() {return _paramTypes;}
		public final boolean isDeprecated() {return _recommended != null;}
		public final String getRecommendedName() {return _recommended;}
		public short getResultType() {return _resultType;}
		public KeyParam[] getKeyParams() {return _keyparams;}
		public short getParsedResult() {return _parsedResult;}
		public XDContainer getFixedParams() {
			if (_keyparams == null) {
				return null;
			}
			XDContainer fixedParams = null;
			for (int i = 0; i < _keyparams.length; i++) {
				if (_keyparams[i].isFixed()) {
					if (fixedParams == null) {
						 fixedParams = new DefContainer();
					}
					fixedParams.setXDNamedItem(_keyparams[i].getName(),
						_keyparams[i].getLegalValues()[0]);
				}
			}
			return fixedParams;
		}
	}

	/** Description of keyword parameter. */
	public static class KeyParam {
		private final String _name; //name of parameter
		private final short _type;  //type of parameter value
		private final boolean _list; //true if parameter is list of values
		private final XDValue[] _legalValues; //legal values (default is first)
		private final int _seqIndex; // sequential param index
		private final boolean _fixed; //true if parameter fixed
		private final boolean _required; //true if parameter is required

		/** Create descriptor of key parameter.
		 * @param name name of parameter.
		 * @param type type of parameter value.
		 * @param list true if parameter is list of values.
		 * @param legalValues array with legal values or null
		 * @param seqIndex sequential parameter index or -1.
		 * @param fixed true if parameter value is fixed.
		 * @param required true if parameter value is required.
		 */
		private KeyParam(final String name,
			final short type,
			final boolean list,
			final int seqIndex,
			final boolean fixed,
			final boolean required,
			final XDValue... legalValues) {
			_name = name;
			_type = type;
			_list = list;
			_legalValues = legalValues;
			_seqIndex = seqIndex;
			_fixed = fixed;
			_required = required;
		}
		/** Get name of parameter.
		 * @return name of parameter.
		 */
		public final String getName() {return _name;}
		/** Get type of parameter value.
		 * @return type of parameter value.
		 */
		public final short getType() {return _type;}
		/** Get default value of parameter.
		 * @return default value of parameter or <tt>null</tt>.
		 */
		public final XDValue[] getLegalValues() {return _legalValues;}
		/** Get default value of parameter.
		 * @return default value of parameter or <tt>null</tt>.
		 */
		public final XDValue getDefaultValue() {
			return _legalValues == null || _legalValues.length == 0 ?
				null : _legalValues[0];
		}
		/** Get parameter index (if specified as sequential).
		 * @return sequential parameter index or -1.
		 */
		public final int getSeqIndex() {return _seqIndex;}
		/** Check if parameter is list of values or a single value.
		 * @return true if parameter is list of values.
		 */
		public boolean isList() {return _list;}
		/** Check if parameter value is fixed.
		 * @return true if parameter value is fixed.
		 */
		public final boolean isFixed() {return _fixed;}
		/** Check if parameter value is required.
		 * @return true if parameter value is required.
		 */
		public final boolean isRequired() {return _required;}
	}
}