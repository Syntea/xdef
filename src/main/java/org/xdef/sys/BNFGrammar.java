package org.xdef.sys;

import org.xdef.msg.BNF;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;

/** Provides BNF grammar parsing and compiling.
 * BNFGrammar object you can create by the static method compile
 * see:
 * <p>{@link org.xdef.sys.BNFGrammar#compile(BNFGrammar, String,
		ReportWriter)}</p> or
 * <p>{@link org.xdef.sys.BNFGrammar#compile(BNFGrammar, File,
		ReportWriter)}</p> or
 * <p>{@link org.xdef.sys.BNFGrammar#compile(BNFGrammar, URL,
		ReportWriter)}</p>.
 * <p>With the created BNFGrammar object you can parse data by method
 * {@link org.xdef.sys.BNFGrammar#parse(StringParser, String)}</p>
 * @author Vaclav Trojan
 */
public final class BNFGrammar {

	/** Predefined inline methods names. */
	private static final String[] INLINE_METHOD_NAMES = new String[] {
		"integer",				//0
		"float",				//1
		"decimal",				//2
		"digit",				//3
		"letter",				//4
		"lowercaseLetter",		//5
		"uppercaseLetter",		//6
		"letterOrDigit",		//7
		"boolean",				//8
		"datetime",				//9
		"date",					//10
		"time",					//11
		"yearMonth",			//12
		"monthDay",				//13
		"month",				//14
		"day",					//15
		"year",					//16
		"duration",				//17
		"base64",				//18
		"hexData",				//19
		"xmlName",				//20
		"ncName",				//21
		"nmToken",				//22
		"xmlChar",				//23
		"whitespace",			//24
		"xmlNamestartchar",		//25
		"xmlNameExtchar",		//26
		"clear",				//27
		"push",					//28
		"pop",					//29
		"anyChar",				//30
		"find",					//31
		"error",				//32
		"JavaName",				//33
		"JavaQName",			//34
		"tokens", 				//35
		"info",  				//36
		"eos",  				//37
		"rule",  				//38
		"true",  				//39
		"false",  				//40
		"stop",  				//41
	};

	/** Inline methods code identifiers. */
	private static final int INL_INTEGER = 0;
	private static final int INL_FLOAT = INL_INTEGER + 1;
	private static final int INL_DECIMAL = INL_FLOAT + 1;
	private static final int INL_DIGIT = INL_DECIMAL + 1;
	private static final int INL_LETTER = INL_DIGIT + 1;
	private static final int INL_LOWERCASELETTER = INL_LETTER + 1;
	private static final int INL_UPPERCASELETTER = INL_LOWERCASELETTER + 1;
	private static final int INL_LETTERORDIGIT = INL_UPPERCASELETTER + 1;
	private static final int INL_BOOLEAN = INL_LETTERORDIGIT + 1;
	private static final int INL_DATETIME = INL_BOOLEAN + 1;
	private static final int INL_DATE = INL_DATETIME + 1;
	private static final int INL_TIME = INL_DATE + 1;
	private static final int INL_YEARMONTH = INL_TIME + 1;
	private static final int INL_MONTHDAY = INL_YEARMONTH + 1;
	private static final int INL_MONTH = INL_MONTHDAY + 1;
	private static final int INL_DAY = INL_MONTH + 1;
	private static final int INL_YEAR = INL_DAY + 1;
	private static final int INL_DURATION = INL_YEAR + 1;
	private static final int INL_BASE64 = INL_DURATION + 1;
	private static final int INL_HEXDATA = INL_BASE64 + 1;
	private static final int INL_XMLNAME = INL_HEXDATA + 1;
	private static final int INL_NCNAME = INL_XMLNAME + 1;
	private static final int INL_NMTOKEN = INL_NCNAME + 1;
	private static final int INL_XMLCHAR = INL_NMTOKEN + 1;
	private static final int INL_WHITESPACE = INL_XMLCHAR + 1;
	private static final int INL_XMLNAMESTARTCHAR = INL_WHITESPACE + 1;
	private static final int INL_XMLNAMEEXTCHAR = INL_XMLNAMESTARTCHAR + 1;
	private static final int INL_CLEAR = INL_XMLNAMEEXTCHAR + 1;
	private static final int INL_PUSH = INL_CLEAR + 1;
	private static final int INL_POP = INL_PUSH + 1;
	private static final int INL_ANYCHAR = INL_POP + 1;
	private static final int INL_FIND = INL_ANYCHAR + 1;
	private static final int INL_ERROR = INL_FIND + 1;
	private static final int INL_JAVANAME = INL_ERROR + 1;
	private static final int INL_JAVAQNAME = INL_JAVANAME + 1;
	private static final int INL_TOKENS = INL_JAVAQNAME + 1;
	private static final int INL_INFO = INL_TOKENS + 1;
	private static final int INL_EOS = INL_INFO + 1;
	private static final int INL_RULE = INL_EOS + 1;
	private static final int INL_TRUE = INL_RULE + 1;
	private static final int INL_FALSE = INL_TRUE + 1;
	private static final int INL_STOP = INL_FALSE + 1;

	/** Parser used to parse source data. */
	private StringParser _p;
	/** User object available from rules. */
	private Object _userObject;
	/** Root rule. */
	private BNFRuleObj _rootRule;
	/** Current processed rule. */
	private BNFRuleObj _actRule;
	/** Table of function aliases. */
	Map<String, String> _aliases = new LinkedHashMap<String, String>();
	/** Array of rules.*/
	private final List<BNFRuleObj> _rules;
	/** Starting position of current rule. */
	private SPosition _spos;
	/** Parsed objects. */
	private Object[] _parsedObjects;
	/** Printer where to print trace information (in null then do not trace). */
	private PrintStream _traceOut;

	/** Create new empty instance of grammar. */
	BNFGrammar() {
		_rules = new ArrayList<BNFRuleObj>();
		_traceOut = null;
	}

	/** Get associated parser.
	 * @return  associated parser.
	 */
	public final StringParser getParser() {return _p;}

	/** Get parsed part of source.
	 * @return string with parsed part.
	 */
	public final String getParsedString() {
		return _p.getParsedBufferPartFrom(_spos.getIndex());
	}

	/** Get position of the parsed part of source.
	 * @return position of the parsed part of source.
	 */
	public final int getParsedPosition() {return _p.getIndex();}

	/** Get unparsed part of source buffer.
	 * @return unparsed part of source buffer.
	 */
	public final String getUnparsedSourceBuffer() {
		return _p.getUnparsedBufferPart();
	}

	/** Check if source was parsed to the end.
	 * @return true if and only if source was parsed to the end.
	 */
	public final boolean isEOS() {return _p.eos();}

	/** Get array with parsed objects.
	 * @return array with parsed objects or <tt>null</tt>.
	 */
	public final Object[] getParsedObjects() {return  _parsedObjects;}

	/** Clear array of parsed objects. */
	public final void clearParsedObjects() {_parsedObjects = null;}

	/** Set user object.
	 * @param obj new user object.
	 * @return old user object.
	 */
	public final Object setUserObject(final Object obj) {
		Object o = _userObject;
		_userObject = obj;
		return o;
	}

	/** Get user object.
	 * @return User object.
	 */
	public final Object getUserObject() {return _userObject;}

	/** Parse string with given rule.
	 * @param source string with source to be parsed.
	 * @param name rule name.
	 * @return true if text was parsed successfully.
	 */
	public final boolean parse(final String source, final String name) {
		return parse(new StringParser(source), name);
	}

	/** Set trace printer.
	 * @param out Printer where to print the tracer information or null.
	 */
	public final void trace(final PrintStream out) {_traceOut = out;}

	/** Parse text given by parser with given rule.
	 * @param p parser.
	 * @param name rule name.
	 * @return true if text was parsed successfully.
	 */
	public final boolean parse(final StringParser p, final String name) {
		_actRule = _rootRule = (BNFRuleObj) getRule(name);
		if (_rootRule == null) {
			//Rule '&{0}' doesn't exist
			throw new SRuntimeException(BNF.BNF901, name);
		}
		return parse(p);
	}

	/** Parse text from associated parser with given rule.
	 * @param name rule name.
	 * @return true if text was parsed successfully.
	 */
	public final boolean parse(final String name) {
		_actRule = _rootRule = (BNFRuleObj) getRule(name);
		_parsedObjects = null;
		return parse();
	}

	/** Parse text from associated parser with actual rule.
	 * @return true if text was parsed successfully.
	 */
	public final boolean parse() {
		_spos = _p.getPosition();
		_parsedObjects = null;
		try {
			return _rootRule.perform();
		} catch (SError ex) {
			if ("BNF stop".equals(ex.getMessage())) {
				return true;
			} else {
				throw ex;
			}
		}
	}

	/** Get actual root rule.
	 * @return actual root rule.
	 */
	public final BNFRule getRootRule() {return _rootRule;}

	/** Get rule from grammar with given index.
	 * @param index index of rule.
	 * @return rule from grammar with given index or <tt>null</tt>.
	 */
	public final BNFRule getRule(final int index) {
		return index < 0 || index >= _rules.size() ? null : _rules.get(index);
	}

	/** Get rule from grammar with given name.
	 * @param name name of rule.
	 * @return rule from grammar with given name or <tt>null</tt>.
	 */
	public final BNFRule getRule(final String name) {
		for (BNFRule r: _rules) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}

	/** Compile BNF grammar.
	 * @param source String with BNF grammar.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final String source)
		throws SRuntimeException {
		return compile(null, source, null);
	}

	@Override
	/** Returns String with source BNF grammar. */
	public String toString() {
		return display(false);
	}

	/** Compile BNF grammar.
	 * @param grammar the grammar to be extended or <tt>null</tt>.
	 * @param source String with BNF grammar.
	 * @param reporter The reporter where error reports are recorded. If this
	 * argument is <tt>null</tt> then there is thrown an exception with
	 * error information message.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final BNFGrammar grammar,
		final String source,
		final ReportWriter reporter) throws SRuntimeException {
		BNFCompiler compiler = new BNFCompiler();
		if (reporter != null) {
			compiler.setReportWriter(reporter);
		}
		compiler.setSourceBuffer(source);
		if (grammar == null) {
			compiler.newGrammar();
		} else {
			compiler._grammar = grammar.cloneGrammar();
		}
		BNFGrammar result = compiler.compile();
		compiler.checkAndThrowErrors();
		return result;
	}

	/** Compile BNF grammar.
	 * @param grammar the grammar to be extended or <tt>null</tt>.
	 * @param source SBuffer with BNF grammar.
	 * @param reporter The reporter where error reports are recorded. If this
	 * argument is <tt>null</tt> then there is thrown an exception with
	 * error information message.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final BNFGrammar grammar,
		final SBuffer source,
		final ReportWriter reporter) throws SRuntimeException {
		BNFCompiler compiler = new BNFCompiler();
		if (reporter != null) {
			compiler.setReportWriter(reporter);
		}
		compiler.setSourceBuffer(source);
		if (grammar == null) {
			compiler.newGrammar();
		} else {
			compiler._grammar = grammar.cloneGrammar();
		}
		BNFGrammar result = compiler.compile();
		if (reporter == null) {
			compiler.checkAndThrowErrors();
		}
		return result;
	}

	/** Compile BNF grammar.
	 * @param source The file with BNF grammar.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final File source)
		throws SRuntimeException {
		return compile(null, source, null);
	}

	/** Compile BNF grammar.
	 * @param grammar the grammar to be extended or <tt>null</tt>.
	 * @param source file with BNF grammar.
	 * @param reporter The reporter where error reports are recorded. If this
	 * argument is <tt>null</tt> then there is thrown an exception with
	 * error information message.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final BNFGrammar grammar,
		final File source,
		final ReportWriter reporter) throws SRuntimeException {
		BNFCompiler compiler = new BNFCompiler();
		if (reporter != null) {
			compiler.setReportWriter(reporter);
		}
		compiler.setSourceReader(source, (String) null);
		if (grammar == null) {
			compiler.newGrammar();
		} else {
			compiler._grammar = grammar.cloneGrammar();
		}
		BNFGrammar result = compiler.compile();
		if (reporter == null) {
			compiler.checkAndThrowErrors();
		}
		return result;
	}

	/** Compile BNF grammar.
	 * @param grammar the grammar to be extended or <tt>null</tt>.
	 * @param source URL pointing to BNF grammar.
	 * @param reporter The reporter where error reports are recorded. If this
	 * argument is <tt>null</tt> then there is thrown an exception with
	 * error information message.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(final BNFGrammar grammar,
		final URL source,
		final ReportWriter reporter) throws SRuntimeException {
		BNFCompiler compiler = new BNFCompiler();
		compiler.setSourceReader(source);
		if (grammar == null) {
			compiler.newGrammar();
		} else {
			compiler._grammar = grammar.cloneGrammar();
		}
		BNFGrammar result = compiler.compile();
		compiler.checkAndThrowErrors();
		return result;
	}

	/** Compile BNF grammar.
	 * @param source The reader with BNF grammar.
	 * @return compiled grammar.
	 * @throws SRuntimeException if the reporter argument is <tt>null</tt> and
	 * an error occurs.
	 */
	public static BNFGrammar compile(Reader source) throws SRuntimeException {
		BNFCompiler compiler = new BNFCompiler();
		compiler.setSourceReader(source, (String) null);
		compiler.newGrammar();
		BNFGrammar result = compiler.compile();
		compiler.checkAndThrowErrors();
		return result;
	}

	/** Display grammar.
	 * @param numLines if true numbers of lines are printed at line start.
	 * @return string with BNF grammar.
	 */
	public final String display(final boolean numLines) {
		StringBuilder sb = new StringBuilder();
		BNFCompiler.display(this, sb, numLines);
		return sb.toString();
	}

	/** Add object to array of objects.
	 * @param o the object.
	 */
	public final void pushObject(final Object o) {
		if (_parsedObjects == null) {
			_parsedObjects = new Object[]{o};
		} else {
			Object[] old = _parsedObjects;
			_parsedObjects = new Object[_parsedObjects.length + 1];
			System.arraycopy(old, 0, _parsedObjects, 0, old.length);
			_parsedObjects[old.length] = o;
		}
	}

	/** Remove the last object from array of objects and return it.
	 * @return last object or <tt>null</tt>.
	 */
	public final Object popObject() {
		if (_parsedObjects == null) {
			return null;
		} else {
			Object[] old = _parsedObjects;
			int len;
			if ((len = old.length -1) == 0) {
				_parsedObjects = null;
			} else {
				_parsedObjects = new Object[len];
				System.arraycopy(old, 0, _parsedObjects, 0, len);
			}
			return old[len];
		}
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse text given by parser with actual rule.
	 * @param p parser.
	 * @return true if text was parsed successfully.
	 */
	private boolean parse(final StringParser p) {
		_p = p;
		return parse();
	}

	private BNFRuleObj newRule(final String name) {
		return new BNFRuleObj(name, this);
	}

	private boolean addRule(final BNFRuleObj rule) {
		for (BNFRule r: _rules) {
			if (r.getName().equals(rule._name)) {
				return false;
			}
		}
		_rules.add(rule);
		return true;
	}
	private BNFChar newItemChar(final char c) {return new BNFChar(c);}
	private BNFToken newItemToken(final String s) {return new BNFToken(s);}
	private BNFSequence newItemSequence() {return new BNFSequence();}
	private BNFSelection newItemUnion() {return new BNFSelection();}
	private BNFConstrain newItemConstraint() {return new BNFConstrain();}
	private BNFReference newItemReference() {return new BNFReference();}
	private BNFSet newItemIsSet() {return new BNFIsSet();}
	private BNFSet newItemNotSet() {return new BNFNotSet();}
	private BNFExtMethodObj newItemExtMethod(final String name,
		final String fullName,
		final ArrayList<Object> params ) {
		return new BNFExtMethodObj(name, fullName, params);
	}
	private BNFPredefined newInlineMethod(final String methodName,
		final String name,
		final ArrayList<Object> params) {
		for(int i = 0; i < INLINE_METHOD_NAMES.length; i++) {
			if (methodName.equals(INLINE_METHOD_NAMES[i])) {
				return new BNFInline(i, name, params);
			}
		}
		// compatibility with old version
		if ("pushParsedObject".equals(methodName)
			|| "pushParsedString".equals(methodName)) {
			return new BNFInline(INL_PUSH, name, params);
		} else if ("popParsedObject".equals(methodName)) {
			return new BNFInline(INL_POP, name, params);
		}
		return null;
	}

	/** Create clone of grammar.
	 * @return clone of this grammar.
	 */
	private BNFGrammar cloneGrammar() {
		final BNFGrammar result = new BNFGrammar();
		for (String s: _aliases.keySet()) {
			result._aliases.put(s, _aliases.get(s));
		}
		for (BNFRuleObj rule: _rules) {
			BNFRuleObj newRule = result.newRule(rule._name);
			newRule._item = null;
			result._rules.add(newRule);
		}
		for (int i = 0; i < result._rules.size(); i++) {
			BNFRuleObj rule = _rules.get(i);
			BNFRuleObj rule1 = result._rules.get(i);
			rule1._item = rule._item.adoptTo(result);
		}
		return result; //TODO
	}

////////////////////////////////////////////////////////////////////////////////
// write/read object
////////////////////////////////////////////////////////////////////////////////

	public final void writeObj(final SObjectWriter w) throws IOException {
		w.writeString(display(false));
	}

	public static BNFGrammar readObj(final SObjectReader r) throws IOException {
		return compile(null, r.readString(), null);
	}

////////////////////////////////////////////////////////////////////////////////

	private static String genBNFChars(final String s) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c <= 0x20 || c >= 0x7f) {
				result.append("#x").append(Integer.toHexString(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private final static String SPECCHARS;
	static {
		final StringBuilder sb = new StringBuilder();
		for (char c = 0; c < 32; c++) {
			sb.append(c);
		}
		SPECCHARS = sb.toString();
	}

	/** Generate BNF source string,
	 * @param s string to be generated.
	 * @param genBrackets if true the string is closed in brackets.
	 * @return BNF string.
	 */
	private static String genBNFString(final String s,
		final boolean genBrackets) {
		String separators = SPECCHARS;
		separators += (s.indexOf('"') >= 0) ? '"' : '\'';
		StringTokenizer st = new StringTokenizer(s, separators, true);
		int numTokens = st.countTokens();
		final StringBuilder sb = new StringBuilder();
		if (genBrackets && numTokens > 1) {
			sb.append('(');
		}
		for (;;) {
			String t = st.nextToken();
			if (t.length() == 1) {
				char c;
				if ((c = t.charAt(0)) < 0x20 || c > 0x7e) {
					sb.append("#x").append(Integer.toHexString(t.charAt(0)));
				} else {
					if (c == '"') {
						sb.append("'\"'");
					} else {
						sb.append('"').append(c).append('"');
					}
				}
			} else {
				char delimiter = t.indexOf('"') < 0 ? '"' : '\'';
				sb.append(delimiter);
				sb.append(t);
				sb.append(delimiter);
			}
			if (st.hasMoreElements()) {
				sb.append(' ');
			} else {
				break;
			}
		}
		if (genBrackets && numTokens > 1) {
			sb.append(')');
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Internal classes
////////////////////////////////////////////////////////////////////////////////

	private final class BNFRuleObj implements BNFRule {
		SPosition _pos;
		private final String _name;
		private BNFItem _item;

		final void setPosition() {_pos = _p.getPosition();}

		final SPosition getPosition() {return _pos;}

		final void resetPosition() {_p.resetPosition(_pos);}

		BNFRuleObj(final String name, BNFGrammar grammar) {
			_name = name.intern();
		}

		@Override
		/** Get name of this rule.
		 * @return name of this rule.
		 */
		public final String getName() {return _name;}

		@Override
		/** Get string with parsed part by this rule.
		 * @return string with parsed part by this rule.
		 */
		public final String getParsedString() {
			return _pos==null ? "":_p.getParsedBufferPartFrom(_pos.getIndex());
		}

		@Override
		/** Get position of parsed part of string by this rule.
		 * @return position of parsed part by this rule.
		 */
		public int getParsedPosition() {return _pos==null ? 0 : _p.getIndex();}

		final BNFItem getItem() {return _item;}

		final void setItem(final BNFItem item) {_item = item;}

		final boolean perform() {
			if (_traceOut != null) {
				try {
					_traceOut.println(this.toString());
					_traceOut.flush();
				} catch (Exception ex) {}
			}
			setPosition();
			try {
				if (_item.perform()) {
					if (_traceOut != null) {
						try {
							if (_pos.getIndex() < _p.getIndex()) {
								_traceOut.println(_name+"; ("
									+ _pos.getIndex() + "," + _p.getIndex()
									+ "); true");
								_traceOut.flush();
							}
						} catch (Exception ex) {}
					}
					return true;
				}
				if (_traceOut != null) {
					try {
						_traceOut.println(_name + "; "
							+ _pos.getIndex() + "; false");
						_traceOut.flush();
					} catch (Exception ex) {}
				}
				resetPosition();
				_p.freeBuffer();
				return false;
			} catch (StackOverflowError ex) {
				StringBuilder sb = new StringBuilder(
					"BNF recursive call of rule:\n" +
					_name + " ::= ");
				_item.display(sb);
				sb.append("\nParsed source:\n");
				sb.append(_p.getParsedBufferPart());
				if (_parsedObjects != null) {
					sb.append("\nInternal stack:\n");
					for (Object o : _parsedObjects) {
						sb.append(o.toString()).append('\n');
					}
				}
				throw new StackOverflowError(sb.toString());
			}
		}

		@Override
		/** Get array of objects created by this rule.
		 * @return array of objects created by this rule or null.
		 */
		public Object[] getParsedObjects() {return _parsedObjects;}

		@Override
		/** Parse string assigned to SParser by this rule.
		 * @param p SParser containing string and position from which parsing
		 * will be started.
		 * @return true if parsing was successful.
		 */
		public final boolean parse(final StringParser p) {
			_parsedObjects = null;
			_actRule = _rootRule = this;
			_p = p;
			_spos = p.getPosition();
			return perform();
		}

		/** Display rule.
		 * @param sb StringBuilder where to generate.
		 */
		final void display(final StringBuilder sb) {
			sb.append(getName()).append(" ::= ");
			if (getItem() instanceof BNFSequence) {
				BNFSequence sq = (BNFSequence) getItem();
				if (sq._min == 1 && sq._max == 1) {
					for (int i = 0; i < sq._items.length; i++) {
						sq._items[i].display(sb);
					}
				} else {
					sq.display(sb);
				}
			} else {
				getItem().display(sb);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			display(sb);
			return sb.toString();
		}

	}

	private abstract class BNFItem {
		SPosition _pos;
		SPosition setPosition() {return _pos = _p.getPosition();}
		SPosition getPosition() {return _pos;}
		void resetPosition() {_p.resetPosition(_pos);}
		void resetPosition(SPosition pos) {_p.resetPosition(_pos = pos);}
		int _min; //minimum occurrence
		int _max; //maximum occurrence
		BNFItem() {_min = 1; _max = 1;}
		void setMin(int min) {_min = min;}
		void setMax(int max) {_max = max;}
		int getMin() {return _min;}
		int getMax() {return _max;}
		abstract boolean perform();
		abstract BNFItem adoptTo(BNFGrammar grammar);
		abstract void display(final StringBuilder sb);
		String genQuantifier() {
			if (_min == 1 && _max == 1) {
				return " ";
			} else if (_min == 0 && _max == 1) {
				return "? ";
			} else if (_min == 1 && _max == Integer.MAX_VALUE) {
				return "+ ";
			} else if (_min == 0 && _max == Integer.MAX_VALUE) {
				return "* ";
			} else {
				String result = '{' + String.valueOf(_min);
				if (_min != _max) {
					result += ',';
					result += _max == Integer.MAX_VALUE ? "" :
						String.valueOf(_max);
				}
				return result + "} ";
			}
		}
		Object[] getStack() {return _parsedObjects;}
		Object peekStack() {
			int len = _parsedObjects == null ? 0 : _parsedObjects.length;
			return len > 0 ? _parsedObjects[len - 1] : null;
		}
		Object popStack() {
			int len = _parsedObjects == null ? 0 : _parsedObjects.length;
			if (len == 0) {
				return null;
			}
			Object result =  _parsedObjects[len - 1];
			Object[] old = _parsedObjects;
			_parsedObjects = new Object[len - 1];
			if (len > 1) {
				System.arraycopy(old, 0, _parsedObjects, 0, len - 1);
			}
			return result;
		}
		void pushStack(final Object o) {
			int len = _parsedObjects == null ? 0 : _parsedObjects.length;
			if (len == 0) {
				_parsedObjects = new Object[] {o};
			} else {
				Object[] old = _parsedObjects;
				_parsedObjects = new Object[len + 1];
				System.arraycopy(old, 0, _parsedObjects, 0, len);
				_parsedObjects[len] = o;
			}
		}
	}

	private final class BNFChar extends BNFItem {
		char _c;
		BNFChar(final char c) {super(); _c = c;}
		final char getChar() {return _c;}
		@Override
		final boolean perform() {
			if (_max == 1) {
				return _p.isChar(_c) || _min == 0;
			}
			setPosition();
			for (int count = 0; count < _max; count++) {
				if (!_p.isChar(_c)) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
			}
			return true;
		}
		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFChar item = grammar.newItemChar(_c);
			item._min = _min;
			item._max = _max;
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			sb.append(genBNFString(String.valueOf(_c), false))
				.append(genQuantifier());
		}
	}

	private final class BNFToken extends BNFItem {
		final String _token;
		String getToken() {return _token;}
		BNFToken(String token) {super(); _token = token.intern();}
		@Override
		final boolean perform() {
			if (_max == 1) {
				return _p.isToken(_token) || _min == 0;
			}
			setPosition();
			for (int count = 0; count < _max; count++) {
				if (!_p.isToken(_token)) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
			}
			return true;
		}
		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFItem item = grammar.newItemToken(_token);
			item._min = _min;
			item._max = _max;
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			sb.append(genBNFString(_token, true)).append(genQuantifier());
		}
	}

	private final class BNFTokens extends BNFItem {
		final String[] _tokens;

		BNFTokens(final String... tokens) {
			super();
			_tokens = tokens;
		}

		@Override
		final boolean perform() {
			if (_max == 1) {
				return _p.isOneOfTokens(_tokens) >= 0;
			}
			setPosition();
			for (int count = 0; count < _max; count++) {
				if (_p.isOneOfTokens(_tokens) < 0) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
			}
			return true;
		}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFItem item = new BNFTokens(_tokens);
			item._min = _min;
			item._max = _max;
			return item;
		}

		@Override
		final void display(final StringBuilder sb) {
			sb.append("(");
			sb.append(genBNFString(_tokens[0], true));
			for (int i=1; i < _tokens.length; i++) {
				sb.append(" | ").append(genBNFString(_tokens[i], true));
			}
			sb.append(")").append(genQuantifier());
		}
	}

	private abstract class BNFSet extends BNFItem {
		String _chars;
		char[] _intervals;
		BNFSet() {}
		String getChars() {return _chars;}
		char[] getIntervals() {return _intervals;}
		abstract boolean isNot();
		@Override
		final void display(final StringBuilder sb) {
			sb.append("[");
			if (isNot()) {
				sb.append("^");
			}
			sb.append(genBNFChars(_chars));
			for (int i = 0; i < _intervals.length; i+=2) {
				sb.append(genBNFChars(String.valueOf(_intervals[i]))).
					append('-').
					append(genBNFChars(String.valueOf(_intervals[i + 1])));
			}
			sb.append("]").append(genQuantifier());
		}
	}

	private final class BNFIsSet extends BNFSet {
		private BNFIsSet() {}

		@Override
		final boolean isNot() {return false;}

		private boolean isSet() {
			char c = _p.getCurrentChar();
			if (_chars.length() > 0 && _chars.indexOf(c) >= 0) {
				_p.nextChar();
				return true;
			}
			for (int i = 0; i < _intervals.length; i+=2) {
				if (c >= _intervals[i] && c <= _intervals[i + 1]) {
					_p.nextChar();
					return true;
				}
			}
			return false;
		}

		@Override
		final boolean perform() {
			if (_max == 1) {
				return isSet() || _min == 0;
			}
			setPosition();
			for (int count = 0; count < _max; count++) {
				if (!isSet()) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
			}
			return true;
		}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFSet item = grammar.newItemIsSet();
			item._min = _min;
			item._max = _max;
			item._chars = _chars;
			item._intervals = _intervals;
			return item;
		}
	}

	private final class BNFNotSet extends BNFSet {

		BNFNotSet() {}

		@Override
		final boolean isNot() {return true;}

		private boolean isNotSet() {
			char c = _p.getCurrentChar();
			if (_chars.length() > 0 && _chars.indexOf(c) >= 0) {
				return false;
			}
			for (int i = 0; i < _intervals.length; i+=2) {
				if (c >= _intervals[i] && c <= _intervals[i + 1]) {
					return false;
				}
			}
			if (_p.eos()) {
				return false; //eos
			}
			_p.nextChar();
			return true;
		}

		@Override
		final boolean perform() {
			if (_max == 1) {
				return isNotSet() || _min == 0;
			}
			setPosition();
			for (int count = 0; count < _max; count++) {
				if (!isNotSet()) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
			}
			return true;
		}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFSet item = grammar.newItemNotSet();
			item._min = _min;
			item._max = _max;
			item._chars = _chars;
			item._intervals = _intervals;
			return item;
		}
	}

	private abstract class BNFGroup extends BNFItem {
		BNFItem[] _items;
		BNFGroup() {super();}

		BNFItem[] getItems() {return _items;}

		final void addItem(final BNFItem item) {
			if (item == this) {
				throw new SRuntimeException(BNF.BNF035); //Internal loop
			} else if (item == null) {
				throw new SRuntimeException(BNF.BNF036); //Item is null
			}
			if (_items == null) {
				_items = new BNFItem[]{item};
			} else {
				BNFItem[] old = _items;
				_items = new BNFItem[old.length + 1];
				System.arraycopy(old, 0, _items, 0, old.length);
				_items[old.length] = item;
			}
		}
	}

	private final class BNFSelection extends BNFGroup {
		BNFSelection() {super();}
		@Override
		final boolean perform() {
			SPosition startPos = setPosition();
			int count = 0;
			for (; count < _max; count++) {
				SPosition xPos = null; //successfull position
				Object xUserObject = null;
				Object[] xParsedObjects = null, parsedObjects = _parsedObjects;
				Object userObject = _userObject;
				setPosition();
				for (int i = 0;  !_p.eos() && i < _items.length; i++) {
					if (_items[i].perform()) { // success
						SPosition p = _p.getPosition();
						if (xPos == null ||
							xPos.getFilePos() + xPos.getIndex()
							< p.getFilePos() + p.getIndex()) {
							xPos = p;
							xParsedObjects = _parsedObjects;
							xUserObject = _userObject;
						}
					}
					_parsedObjects = parsedObjects;
					_userObject = userObject;
					resetPosition();
				}
				if (xPos == null) { // no variant found
					if (count < _min) {
						resetPosition(startPos);
						return false;
					}
					break;
				} else { // set to the most sucessfull variant
					count++;
					resetPosition(xPos);
					_parsedObjects = xParsedObjects;
					_userObject = xUserObject;
				}
			}
			return true;
		}
		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFSelection item = grammar.newItemUnion();
			item._min = _min;
			item._max = _max;
			item._items = new BNFItem[_items.length];
			for (int i = 0; i < _items.length; i++) {
				item._items[i] = _items[i].adoptTo(grammar);
			}
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			if (_items.length == 1) {
				_items[0].display(sb);
				return;
			}
			sb.append("( ");
			_items[0].display(sb);
			for (int i = 1; i < _items.length; i++) {
				sb.append("| ");
				_items[i].display(sb);
			}
			sb.append(")").append(genQuantifier());
		}

		BNFTokens newTokens(final String... tokens) {
			return new BNFTokens(tokens);
		}
	}

	private final class BNFConstrain extends BNFGroup {
		BNFConstrain() {super();}
		@Override
		final boolean perform() {
			setPosition();
			int count = 0;
			while (count < _max && !_p.eos()) {
				SPosition pos = _p.getPosition();
				if (!_items[0].perform()) {
					if (count == 0) {
						//first item in first loop so we
						// need not reset position
						return _min == 0;
					}
					break;
				}
				SPosition pos1 = _p.getPosition();
				Object[] parsedObjects = _parsedObjects;
				boolean parsed = true;
				for (int i = 1; i < _items.length; i++) {
					_p.resetPosition(pos);
					if (_items[i].perform()) {
						SPosition pos2 = _p.getPosition();
						if (pos1.getIndex() <= pos2.getIndex()) {
							_p.resetPosition(pos);
							_parsedObjects = parsedObjects;
							if (count == 0) {
								//first item in first loop so we
								// need not reset position
								return _min == 0;
							}
							parsed = false;
							break;
						}
					}
				}
				if (!parsed) {
					if (count < _min) {
						resetPosition();
						return false;
					}
					break;
				}
				count++;
				_p.resetPosition(pos1); //OK
				_parsedObjects = parsedObjects;
			}
			return count >= _min;
		}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFConstrain item = grammar.newItemConstraint();
			item._min = _min;
			item._max = _max;
			item._items = new BNFItem[_items.length];
			for (int i = 0; i < _items.length; i++) {
				item._items[i] = _items[i].adoptTo(grammar);
			}
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			_items[0].display(sb);
			for (int i = 1; i < _items.length; i++) {
				sb.append("- ");
				_items[i].display(sb);
			}
			sb.append(genQuantifier());
		}
	}

	private final class BNFSequence extends BNFGroup {
		BNFSequence() {super();}
		@Override
		final boolean perform() {
			setPosition();
			int count = 0;
		loop:
			while (count < _max) {
				SPosition pos = _p.getPosition();
				Object[] parsedObjects = _parsedObjects;
				for (int i = 0; i < _items.length; i++) {
					if (!_items[i].perform()) {
						_parsedObjects = parsedObjects;
						if (count == 0 && i == 0) {
							//first item in first loop so we
							// need not reset position
							return _min == 0;
						}
						_p.resetPosition(pos);
						break loop;
					}
				}
				count++;
			}
			if (count < _min) {
				resetPosition();
				return false;
			}
			return true;
		}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFSequence item = grammar.newItemSequence();
			item._min = _min;
			item._max = _max;
			item._items = new BNFItem[_items.length];
			for (int i = 0; i < _items.length; i++) {
				item._items[i] = _items[i].adoptTo(grammar);
			}
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			if (_items.length == 1) {
				_items[0].display(sb);
				return;
			}
			sb.append("( ");
			_items[0].display(sb);
			for (int i = 1; i < _items.length; i++) {
				_items[i].display(sb);
			}
			sb.append(")").append(genQuantifier());
		}
	}

	private final class BNFReference extends BNFItem {
		BNFRuleObj _rule;
		BNFReference() {super();}
		final BNFRuleObj getRule() {return _rule;}

		@Override
		final boolean perform() {
			BNFRuleObj savedRule = _actRule; //save
			_actRule = _rule;
			if (_max == 1) {
				boolean result = _rule.perform() || _min == 0;
				_actRule = savedRule;
				return result;
			}
			SPosition savedPos = _rule._pos;
			int count = 0;
			setPosition();
			if (_rule.perform()) {
				while (++count < _max && _rule.perform()) {}
			}
			_rule._pos = savedPos;
			_actRule = savedRule;
			if (count < _min) {
				if (count > 0) {
					resetPosition();
				}
				return false;
			} else {
				return true;
			}
		}

		final void setRule(final BNFRuleObj rule) {_rule = rule;}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFReference item = grammar.newItemReference();
			item._min = _min;
			item._max = _max;
			item._rule = (BNFRuleObj) grammar.getRule(_rule._name);
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			sb.append(_rule.getName()).append(genQuantifier());
		}
	}

	public static final boolean dummy(final BNFExtMethod p) {return true;}
	private final static Method DUMMY_METHOD;
	static {
		Method method;
		try {
			// set dummy method
			method = BNFGrammar.class.getMethod("dummy",BNFExtMethod.class);
		} catch (Exception ex) {
			method = null; // shouldn't happen
		}
		DUMMY_METHOD = method;
	}

	private final class BNFExtMethodObj extends BNFItem implements BNFExtMethod{
		private Method _method;
		private String _name;
		private Object[] _params;

		BNFExtMethodObj(final String name,
			 final String fullName,
			final ArrayList<Object> params) {
			super();
			_name = name;
			_method = null;
			_params = null;
			if (name == null) {
				return;
			}
			int ndx =  fullName.lastIndexOf('.');
			if (ndx > 0) { // external method
				int size = params.size();
				_params = new Object[size + 1];
				Class<?>[] parameterTypes = new Class<?>[size + 1];
				parameterTypes[0] = org.xdef.sys.BNFExtMethod.class;
				String className = fullName.substring(0, ndx);
				String methodName = fullName.substring(ndx + 1);
				if (size == 0) {
					if (findMethod(className, methodName, parameterTypes)) {
						return;
					}
				}
				for (int i = 0; i < size; i++) {
					parameterTypes[i + 1] = params.get(i).getClass();
					_params[i + 1] = params.get(i);
				}
				if (findMethod(className, methodName, parameterTypes)) {
					return;
				}
				parameterTypes = new Class<?>[2];
				parameterTypes[0] = org.xdef.sys.BNFExtMethod.class;
				parameterTypes[1] = Object[].class;
				if (findMethod(className, methodName, parameterTypes)) {
					return;
				}
				// set dummy method
				_params = new Object[0];
				_method = DUMMY_METHOD;
			}
		}

		private boolean findMethod(final String className,
			final String methodName,
			final Class<?>[] parameterTypes) {
			try {
				_method = Class.forName(className)
					.getMethod(methodName, parameterTypes);
				if (!"boolean".equals(_method.getReturnType().getName())
					&& !"Boolean".equals(_method.getReturnType().getName())) {
					_method = null;
					return false;
				}
				return true;
			} catch (Exception ex) {
				_method = null;
				return false;
			}
		}

		@Override
		final boolean perform() {
			try {
				_params[0] = this; //the first parameter is always "this".
				_pos = _actRule._pos; // save position of active rule
				SPosition pos = _p.getPosition();
				int count = 0;
				Class<?>[] parTypes = _method.getParameterTypes();
				Object[] pars;
				if (parTypes.length == 2 && parTypes[1].equals(Object[].class)){
					Object[] par1 = new Object[_params.length - 1];
					for (int i = 0; i < par1.length; i++) {
						par1[i] = _params[i+1];
					}
					pars = new Object[]{_params[0], par1};
				} else {
					pars = _params;
				}
				if (((Boolean)_method.invoke(_userObject, pars))) {
					while (++count < _max
						&& ((Boolean)_method.invoke(_userObject,_params))){}
					if (count < _min) {
						_p.resetPosition(pos);
						return false;
					}
					_pos = _p.getPosition();
					return true;
				}

				_pos = _p.getPosition();
				return _min == 0;
			} catch (Exception ex) {
				Report rep;
				if ((_method.getModifiers() & Modifier.STATIC) == 0 &&
					_userObject == null) {
					//External method '&{0}' (rule &{1}) should be
					//static or set user object
					rep = Report.fatal(BNF.BNF903, _name, _actRule._name);
				} else {
					//Runtime error in external method '&{0}', rule &{1}: &{2}
					rep = Report.fatal(BNF.BNF902,_name, _actRule._name, ex);
				}
				_p.putReport(rep);
				return false;
			}
		}

		@Override
		/** Get actual source position.
		 * @return SPosition object with actual source position.
		 */
		public final SPosition getSPosition() {return _pos;}

		@Override
		/** Set user object.
		 * @param obj new user object.
		 * @return old user object.
		 */
		public final Object setUserObject(final Object obj) {
			Object o = _userObject;
			_userObject = obj;
			return o;
		}

		@Override
		/** Get associated user object.
		 * @return associated user object.
		 */
		public final Object getUserObject() {return _userObject;}

		@Override
		/** Get parsed part of string by this rule.
		 * @return parsed part of string by this rule.
		 */
		public final String getParsedString() {
			return _pos==null ? "" : _p.getParsedBufferPartFrom(_pos.getIndex());
		}

		@Override
		/** Get objects from internal stack.
		 * @return objects from  internal stack.
		 */
		public Object[] getParsedStack() {
			return _actRule.getItem().getStack();
		}

		@Override
		/** Pop value from parsed stack.
		 * @return the top of parsed stack or null.
		 */
		public Object popParsedObject() {
			return _actRule.getItem().peekStack();
		}

		@Override
		/** Get the value of the top of grammar stack.
		 * @return the top of grammar stack or null.
		 */
		public Object peekParsedObject() {
			return _actRule.getItem().peekStack();
		}

		@Override
		/** Push object to grammar stack.
		 * @param o object to be pushed.
		 */
		public void pushParsedObject(Object o) {
			_actRule.getItem().pushStack(o);
		}

		@Override
		/**  Get actual BNF rule.
		 * @return actual BNF rule.
		 */
		public final BNFRule getRule() {return _actRule;}

		@Override
		/** Get actual rule name.
		 * @return actual rule name.
		 */
		public String getRuleName() {return _actRule.getName();}

		@Override
		/** Get root rule.
		 * @return  root rule.
		 */
		public final BNFRule getRootRule() {return _rootRule;}

		@Override
		/** Get SParser used for parsing.
		 * @return SParser.
		 */
		public final StringParser getParser() {return _p;}

		@Override
		/** Get name of external method.
		 * @return name of external method.
		 */
		public final String getMethodName() {return _name;}

		@Override
		final BNFItem adoptTo(final BNFGrammar grammar) {
			BNFExtMethodObj item = grammar.newItemExtMethod(null, null, null);
			item._name = _name;
			item._method = _method;
			item._params = _params;
			item._params[0] = item; //VERY IMPORTANT: instance of item.

			item._min = _min;
			item._max = _max;
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			sb.append('$');
			sb.append(_name);
			sb.append(genQuantifier());
		}
	}

	private abstract class BNFPredefined extends BNFItem {

		abstract String getName();

		@Override
		BNFItem adoptTo(final BNFGrammar grammar) {
			BNFPredefined item =
				grammar.newInlineMethod(getName(), getName(), null);
			item._min = _min;
			item._max = _max;
			return item;
		}
		@Override
		final void display(final StringBuilder sb) {
			sb.append('$').append(getName()).append(genQuantifier());
		}
	}

	/** Provides parsing with predefined methods.*/
	private class BNFInline extends BNFPredefined {
		/** code of parse method (see PREDEFINED_METHOD_NAMES. */
		private final int _code;
		/** declared name of item. */
		private final String _name; //original name
		/** Parsed object. */
		private Object _param;

		/** Create "inline" item.
		 * @param code parse method id (see PREDEFINED_METHOD_NAMES).
		 * @param name declared name of item.
		 * @param params array with parameters.
		 */
		private BNFInline(final int code,
			final String name,
			final ArrayList<Object> params) {
			_code = code;
			_name = name.intern();
			if (params != null && params.size() > 0) {
				if (code == INL_PUSH && params.size() == 1) {
					_param = params.get(0).toString();
				} else if (code == INL_ERROR) {
					if (_param == null) {
						_param = new String[0];
					} else {
						String[] pars = new String[params.size()];
						for (int i = 0; i < pars.length; i++) {
							pars[i] = params.get(i).toString();
						}
						_param = pars;
					}
				} else if (code == INL_INFO || code == INL_STOP) {
					if (params.isEmpty()) {
						_param = null;
					} else {
						String s = genMethodParam(params.get(0));
						for (int i = 1; i < params.size(); i++) {
							s += "," + genMethodParam(params.get(i));
						}
						_param = s;
					}
				} else if (code == INL_DATETIME	&& params.size() == 1
					&& (params.get(0) instanceof String)) {
					_param = (String) params.get(0);
				} else if (code == INL_TOKENS && params.size() >= 1) {
					 String[] x = new String[params.size()];
					 for (int i = 0; i < params.size(); i++) {
						 x[i] = params.get(i).toString();
					 }
					 _param = x;
				} else {
					//No parameters are alowed in method &{0}
					throw new SRuntimeException(BNF.BNF038, name);
				}
//			} else { // must be parameter
//				//Parameter of method &{0} is expected
//				throw new SRuntimeException(BNF.BNFx037, name);
			}
		}
		private String genMethodParam(Object o) {
			if (o == null) {
				return "null";
			} else if (o instanceof String) {
				return genBNFString((String) o, false);
			} else {
				return o.toString();
			}
		}

		private final class MyReader extends Reader {

			MyReader() {}

			@Override
			public final int read(final char[] cbuf) {
				if (_p.eos()) {
					return -1;
				}
				for (int i = 0; i < cbuf.length; i++) {
					cbuf[i] = _p.peekChar();
					if (_p.eos()) {
						return i;
					}
				}
				return cbuf.length;
			}

			@Override
			public final int read(final char[] cbuf,
				final int off,
				final int len) {
				if (_p.eos()) {
					return -1;
				}
				int i = off;
				int max = off + len;
				if (max > cbuf.length) {
					max = cbuf.length;
				}
				for (; i < max; i++) {
					cbuf[i] = _p.peekChar();
					if (_p.eos()) {
						break;
					}
				}
				return i - off;
			}

			@Override
			public final int read() {
				int result = _p.peekChar();
				return result != SParser.NOCHAR ? result : -1;
			}

			@Override
			public final void close() {}
		}

		private boolean isBase64() {
			try {
				Reader r = new MyReader();
				ByteArrayOutputStream bw = new ByteArrayOutputStream();
				SUtils.decodeBase64(r, bw);
				r.close();
				pushObject(bw.toByteArray());
				return true;
			} catch (Exception ex) {
				return false;
			}
		}

		private boolean isHexdata() {
			try {
				Reader r = new MyReader();
				ByteArrayOutputStream bw = new ByteArrayOutputStream();
				SUtils.decodeHex(r, bw);
				r.close();
				pushObject(bw.toByteArray());
				return true;
			} catch (Exception ex) {
				return false;
			}
		}

		/** Perform the inline method.*/
		private boolean invoke() {
			int pos = _p.getIndex();
			switch (_code) { //index of PREDEFINED_METHOD_NAMES
				case INL_INTEGER: //integer
					if (_p.isInteger()) {
						pushObject(_p.getParsedLong());
						return true;
					}
					return false;
				case INL_FLOAT: //float
					if (_p.isFloat()) {
						pushObject(_p.getParsedDouble());
						return true;
					}
					return false;
				case INL_DECIMAL: //decimal
					if (_p.isFloat()) {
						pushObject(_p.getParsedBufferPartFrom(pos));
						return true;
					}
					return false;
				case INL_DIGIT: //digit
					return _p.isDigit() >= 0;
				case INL_LETTER: //letter
					return _p.isLetter() != SParser.NOCHAR;
				case INL_LOWERCASELETTER: //lowerCaseLetter
					return _p.isLowerCaseLetter() != SParser.NOCHAR;
				case INL_UPPERCASELETTER: //upperCaseLetter
					return _p.isUpperCaseLetter() != SParser.NOCHAR;
				case INL_LETTERORDIGIT: //letterordigit
					return _p.isLetterOrDigit() != SParser.NOCHAR;
				case INL_BOOLEAN: //boolean
					return _p.isOneOfTokens("true", "false") >= 0;
				case INL_DATETIME: //datetime
					return _param == null ? _p.isISO8601DateAndTime()
						: _p.isDatetime((String) _param);
				case INL_DATE: //date
					return _p.isISO8601Date();
				case INL_TIME: //time
					return _p.isISO8601Time();
				case INL_YEARMONTH: //yearMonth
					return _p.isDatetime("yyyy[-]MM[Z]");
				case INL_MONTHDAY: //monthDay
					return _p.isDatetime("--MM[-]dd");
				case INL_YEAR: //year
					return _p.isDatetime("[-]yyyy[Z]");
				case INL_MONTH: //month
					return _p.isDatetime("--MM");
				case INL_DAY: //day
					return _p.isDatetime("---dd");
				case INL_DURATION: //duration
					return _p.isDuration();
				case INL_BASE64: //base64
					return isBase64();
				case INL_HEXDATA: //hexdata
					return isHexdata();
				case INL_XMLNAME: //xmlname
					return _p.isXMLName((byte) 10);
				case INL_NCNAME: //ncname
					return _p.isNCName((byte) 10);
				case INL_NMTOKEN: //nmtoken
					return _p.isNMToken((byte) 10);
				case INL_XMLCHAR: //xmlchar
					return _p.isXMLChar((byte) 10) != SParser.NOCHAR;
				case INL_WHITESPACE: //whitespace
					return _p.isXMLWhitespaceChar() != SParser.NOCHAR;
				case INL_XMLNAMESTARTCHAR: //xmlNamestartchar
					return _p.isXMLNamestartChar((byte) 10) != SParser.NOCHAR;
				case INL_XMLNAMEEXTCHAR: //xmlNameExtchar
					return
						_p.isXMLNameExtensionChar((byte) 10) != SParser.NOCHAR;
				case INL_CLEAR: //clearParsedObjects
					_parsedObjects = null;
					return true;
				case INL_PUSH: //pushParsedObject
					if (_param != null) {
						pushObject(_param);
						return true;
					}
					pushObject(_actRule.getParsedString());
					return true;
				case INL_POP: //popParsedObject
					return popObject() != null;
				case INL_ANYCHAR: //anyChar
					return _p.peekChar() != SParser.NOCHAR;
				case INL_FIND:{ //find
					String s = ((String) _param);
					return s.length() == 1
						? _p.findChar(s.charAt(0)) : _p.findToken(s);
				}
				case INL_ERROR: { //error
					String id = "";
					String txt = "";
					String modif = "";
					Object[] pars = (Object[]) _param;
					if (pars.length > 0) {
						id = pars[0].toString();
						txt = pars[1].toString();
						if (pars.length > 1) {
							for (int i = 2; i < pars.length; i++) {
								modif += "&{"+(i-2)+"}" + pars[i].toString();
							}
						}
					}
					_p.error(id, txt, modif);
					return true;
				}
				case INL_JAVANAME: //JavaName
					return _p.isJavaName();
				case INL_JAVAQNAME: //JavaQName
					return _p.isJavaQName();
				case INL_INFO:
					pushObject("info: " + _actRule._name
						+ (_param != null ? "(" +_param + ")": "")
						+ "; pos=" + _p.getIndex());
					return true;
				case INL_TOKENS:
					return _p.isOneOfTokens((String[]) _param) >= 0;
				case INL_EOS:
					return _p.eos();
				case INL_RULE:
					pushObject(_actRule._name + " " +
						_actRule._pos.getIndex() + " " + _p.getIndex());
					return true;
				case INL_TRUE:
					return true;
				case INL_FALSE:
					return true;
				case INL_STOP: {
					if (_param != null) {
						pushObject("STOP " + _param);
					}
					throw new SError("BNF stop");
				}
				default:
					//Illegal BNF runtime code: &{0}
					throw new SRuntimeException(BNF.BNF040, _code);
			}
		}

		@Override
		/** Get declared name of parse item.
		 * @return the name of parse item.
		 */
		final String getName() {return _name;}

		@Override
		/** Provide parsing.
		 * @return true if and only if parsed text fits to item.
		 */
		final boolean perform() {
			if (_max == 1) {
				return invoke() || _min == 0;
			}
			setPosition();
			if (invoke()) {
				int count = 0;
				while (++count < _max && invoke()) {}
				if (count < _min) {
					resetPosition();
					return false;
				}
				return true;
			}
			return _min == 0;
		}

		@Override
		/** Adopt this item to other grammar.
		 * @param grammar the other grammar.
		 * @return the adopted BNFItem.
		 */
		final BNFItem adoptTo(final BNFGrammar grammar) {
			final ArrayList<Object> params = new ArrayList<Object>();
			if (_param != null) {
				params.add(_param);
			}
			BNFPredefined item = grammar.newInlineMethod(
				INLINE_METHOD_NAMES[_code],
				_name,
				params);
			item._min = _min;
			item._max = _max;
			return item;
		}
	}

	/** Compiles BNF grammar from source.*/
	private static class BNFCompiler extends StringParser
		implements org.xdef.msg.BNF {

		private static final short EOS_SYM = 0; //end of source
		private static final short ALIAS_DECL_SYM = EOS_SYM + 1; //alias
		private static final short ASSIGN_SYM = ALIAS_DECL_SYM + 1; //assign
		private static final short RULE_DECL_SYM = ASSIGN_SYM + 1; //rule
		private static final short ITEM_SYM = RULE_DECL_SYM + 1; //item
		private static final short OR_SYM = ITEM_SYM + 1; //or symbol("|")
		private static final short MINUS_SYM = OR_SYM + 1; //minus symbol("-")
		private static final short LBR_SYM = MINUS_SYM + 1; //left bracket ("(")
		private static final short RBR_SYM = LBR_SYM + 1;  //right bracket (")")
		private static final short ERROR_SYM = RBR_SYM + 1;//error(undef symbol)

		/** symbol id of last parsed symbol. */
		private short _sym;

		/** grammar object. */
		BNFGrammar _grammar;
		/** actually created item object. */
		private BNFItem _item;
		/** buffer with actually parsed item.*/
		private final StringBuilder _parsedChars = new StringBuilder();
		/** array with unresolved references. */
		private final ArrayList<UnresolvedReference> _unresolvedRefs =
			new ArrayList<UnresolvedReference>();
		/** This object holds unresolved reference to a rule. */
		private final class UnresolvedReference {
			String _name;
			BNFReference _reference;
			SPosition _pos;
			UnresolvedReference(String name,
				BNFReference reference,
				SPosition pos) {
				_name = name;
				_reference = reference;
				_pos = pos;
			}
			final String getName() {return _name;}
			final SPosition getPosition() {return _pos;}
			final BNFReference getReference() {return _reference;}
		}

		/** Create new instance of compile (source will be assigned later). */
		BNFCompiler() {super();}

		/** Compile BNF grammar.
		 * @return object with compiled BNF grammar.
		 */
		final BNFGrammar compile() {
			if (_grammar == null) {
				newGrammar();
			}
			if (!isGrammar()) {
				fatal(BNF001); //BNF grammar not exists
				_grammar = null;
			}
			if (errors()) {
				return null;
			}
			return _grammar;
		}

		/** initialize new compilation of BNF grammar.
		 * @return new compilation of BNF grammar.
		 */
		final BNFGrammar newGrammar() {
			closeGrammar();
			_grammar = new BNFGrammar();
			return _grammar;
		}

		/** close compiler.*/
		private void closeGrammar() {
			_parsedChars.setLength(0);
			_unresolvedRefs.clear();
			_item = null;
			_sym = EOS_SYM;
		}

////////////////////////////////////////////////////////////////////////////////

		/** Skip white spaces and comments. */
		private void skipSeparators() {
			if (eos()) {
				return;
			}
			for (;;) {
				skipSpaces();
				if (isToken("/*")) {//comment
					if (!findTokenAndSkip("*/")) {
						error(BNF002); //Unclosed comment
						break;
					}
				} else if (isToken("//")) {
					skipToNextLine();
				} else {
					return;
				}
			}
			setEos();
		}

		/** Read a numeric character specification.
		 * @return the value of the specified character (UTF16) or -1
		 * if the value was incorrect.
		 */
		private int readSpecChar() {
			char c;
			long i;
			increaseBuffer();
			if (isChar('x') || isChar('X')) { //hexadecimal
				if ((c = isOneOfChars("0123456789ABCDEFabcdef")) == NOCHAR) {
					setBufIndex(getIndex() - 1);
					return -1;
				}
				i = "0123456789ABCDEF".indexOf(Character.toUpperCase(c));
				while((c = isOneOfChars("0123456789ABCDEFabcdef")) != NOCHAR) {
					i = i * 16 +
						"0123456789ABCDEF".indexOf(Character.toUpperCase(c));
				}
			} else { //decimal
				if ((i = isDigit()) == -1) {
					return -1;
				}
				int d;
				while((d = isDigit()) != -1) {
					i = i * 10 + d;
				}
			}
			if (i > 65535 || i < 0) {
				error(BNF027); //Maximum numeric value of character is 65535
			}
			return (int) i;
		}

		/** Check repeating quantifier of an item.
		 * @param item where set quantifier.
		 * @return <tt>true</tt> if quantifier was parsed.
		 */
		private boolean checkQuantifier(final BNFItem item) {
			skipSeparators();
			if (isChar('*')) {
				item.setMin(0);
				item.setMax(Integer.MAX_VALUE);
				skipSeparators();
				return true;
			} else if (isChar('+')) {
				item.setMin(1);
				item.setMax(Integer.MAX_VALUE);
				skipSeparators();
				return true;
			} else if (isChar('?')) {
				item.setMin(0);
				item.setMax(1);
				skipSeparators();
				return true;
			} else if (isChar('{')) {
				skipSeparators();
				if (!isInteger()) {
					error(BNF020); //Number of occurrences expected
				} else {
					int i = getParsedInt();
					item.setMin(i);
					skipSeparators();
					if (isChar(',')) {
						skipSeparators();
						i = Integer.MAX_VALUE;
						if (isChar('*')) {
						} else if (isInteger()) {
							i = getParsedInt();
							if (i < item.getMin()) {
								// Maximal occurrence must be greater
								// or equal to theminimum
								error(BNF028);
							}
						}
						skipSeparators();
					}
					item.setMax(i);
				}
				if (!isChar('}')) {
					//End character '}' of occurrence specification expected
					error(BNF029);
					skipToNextLine();
				}
				skipSeparators();
				return true;
			} else {
				return false;
			}
		}

		/** Parse set specification. */
		private void parseSetDecl() {
			BNFSet item;
			if (isChar('^')) {
				item = _grammar.newItemNotSet();
			} else {
				item = _grammar.newItemIsSet();
			}
			_item = item;
			_parsedChars.setLength(0);
			char c, last;
			if ((c = notOneOfChars("]\n\r")) == NOCHAR) {
				error(BNF005); //Error in character set
				return;
			}
			last = 0;
			StringBuilder intervals = new StringBuilder();
			OUTER:
			do {
				switch (c) {
					case '#': {
						int i = readSpecChar();
						if (i == -1) {
							c = '#';
						} else {
							c = (char) i;
						}	_parsedChars.append(c);
						last = c;
						break;
					}
					case '-': {
						if (last == 0) {
							_parsedChars.append('-');
							last = '-';
						} else {
							_parsedChars.setLength(_parsedChars.length() - 1);
							if ((c = notOneOfChars("]\n\r")) == NOCHAR) {
								break OUTER;
							}
							if (c == '#') {
								int i = readSpecChar();
								if (i == -1) {
									c = '#';
								} else {
									c = (char) i;
								}
							}	if (c < last) {
								error(BNF006); //Incorrect interval in char set
							} else {
								intervals.append(last).append(c);
								last = 0;
							}
						}
						break;
					}
					default:
						_parsedChars.append(c);
						last = c;
						break;
				}
			} while ((c = notOneOfChars("]\n\r")) != NOCHAR);
			if (!isChar(']')) {
				error(BNF007); //Incorrect character set declaration
				findOneOfCharsAndSkip("-]\n\r");
				return;
			}
			item._chars = _parsedChars.toString();
			item._intervals = new char[intervals.length()];
			intervals.getChars(0, intervals.length(), item._intervals, 0);
			if (_parsedChars.length() == 0 && intervals.length() == 0) {
				error(BNF008); //Empty character set
			}
			checkQuantifier(_item);
		}

		private boolean isBNFName() {
			char c;
			if ((c = isChar('_') ? '_' : isLetter()) != NOCHAR) {
				_parsedChars.setLength(0);
				_parsedChars.append(c);
				while ((c = isChar('_') ? '_' : isLetterOrDigit()) != NOCHAR) {
					_parsedChars.append(c);
				}
				return true;
			}
			return false;
		}

		private void readLiteral(final char delimiter) {
			_parsedChars.setLength(0);
			char d = delimiter;
			if (d == '#') {
				int i = readSpecChar();
				if (i == -1) {
					error(BNF003); //Incorrect character specification
				} else {
					_parsedChars.append((char) i);
				}
			} else if (d == '"' || d == '\'') {
				char c;
				while ((c = notChar(d)) != NOCHAR) {
					_parsedChars.append(c);
				}
				if (!isChar(d)) {
					fatal(BNF004); //Unclosed literal
				}
			}
			skipSeparators();
		}

		private	void readAliasParams(final ArrayList<Object> params) {
			if (!isChar('(')) {
				return;
			}
			skipSeparators();
			if (isChar(')')) {
				return;
			}
			for (;;) {
				if (eos()) {
					error(BNF012); //Missing ')'
					return;
				}
				char c;
				if (isDuration()) {
					params.add(getParsedSDuration());
				} else if (isDatetime("[y]-MM-d[THH:mm[:ss[.S]]]")) {
					params.add(getParsedSDatetime());
				} else if (isSignedFloat()) {
					try {
						params.add(getParsedDouble());
					} catch (Exception ex) {
						error(BNF013); //Incorrect number
					}
				} else if (isSignedInteger()) {
					params.add(getParsedLong());
				} else if (isToken("false")) {
					params.add(Boolean.FALSE);
				} else if (isToken("true")) {
					params.add(Boolean.TRUE);
				} else if ((c = isOneOfChars("#'\"")) != NOCHAR) {
					readLiteral(c);
					skipSeparators();
					StringBuilder sb = new StringBuilder(
						_parsedChars.toString());
					while ((c = isOneOfChars("#'\"")) != NOCHAR) {
						readLiteral(c);
						skipSeparators();
						sb.append(_parsedChars);
					}
					params.add(sb.toString());
				} else {
					error(BNF014); //Incorrect method parameter
				}
				skipSeparators();
				if (isChar(',')) {
					skipSeparators();
				} else if (isChar(')')) {
					return;
				} else {
					error(BNF011); //Expected ',' or ')'
					return;
				}
			}
		}

		/** Read next symbol - lexical analyzer.
		 * @return id of recognized symbol.
		 */
		private short nextSymbol() {
			skipSeparators();
			if (eos()) {
				return _sym = EOS_SYM;
			}
			char c;
			if (isBNFName()) {
				String name = _parsedChars.toString();
				skipSeparators();
				if (isToken("::=")) { //rule definition
					skipSeparators();
					return _sym = RULE_DECL_SYM;
				} else { // rule reference
					_item = _grammar.newItemReference();
					BNFRuleObj cmd = (BNFRuleObj) _grammar.getRule(name);
					if (cmd == null) {
						_unresolvedRefs.add(new UnresolvedReference(name,
							(BNFReference)_item, getPosition()));
					} else {
						((BNFReference)_item).setRule(cmd);
					}
					checkQuantifier(_item);
					return _sym = ITEM_SYM;
				}
			} else if (isChar('|')) {
				skipSeparators();
				return _sym = OR_SYM;
			} else if (isChar('-')) {
				skipSeparators();
				return _sym = MINUS_SYM;
			} else if ((c = isOneOfChars("#'\"")) != NOCHAR) {
				readLiteral(c);
				if (_parsedChars.length() > 1) {
					_item = _grammar.newItemToken(_parsedChars.toString());
				} else if (_parsedChars.length() == 1) {
					_item = _grammar.newItemChar(_parsedChars.charAt(0));
				} else {
					_item = _grammar.newItemChar('?');
					error(BNF009); //Empty literal
				}
				checkQuantifier(_item);
				return _sym = ITEM_SYM;
			} else if (isChar('[')) {
				parseSetDecl();
				return _sym = ITEM_SYM;
			} else if (isChar('(')) {
				skipSeparators();
				if (isChar(')')) {
					error(BNF030); //Empty sequence
					skipSeparators();
					return _sym = ERROR_SYM;
				}
				return _sym = LBR_SYM;
			} else if (isChar(')')) {
				skipSeparators();
				return _sym = RBR_SYM;
			} else if (isChar('$')) {//externalMethod method or number
				if (isIdentifier()) { //identifier or integer
					String name = _parsedChars.toString();
					externalMethod(name);
					skipSeparators();
					return _sym = ITEM_SYM;
				} else {
					externalMethod("$");
					skipSeparators();
					return _sym = ITEM_SYM;
				}
			} else if (isToken("%define")) {
				skipSeparators();
				return _sym = ALIAS_DECL_SYM;
			}
			if (eos()) {
				return _sym = EOS_SYM;
			}
			error(BNF010); //Undefined symbol
			skipSeparators();
			return _sym = ERROR_SYM;
		}

		private boolean isIdentifier() {
			char ch;
			if ((ch = isLetterOrDigit()) == NOCHAR) {
				return false;
			}
			_parsedChars.setLength(0);
			_parsedChars.append(ch);
			while (isChar('.') || (ch = isLetterOrDigit()) != NOCHAR) {
				_parsedChars.append(ch);
				ch = '.';
			}
			return true;
		}

		private void externalMethod(final String name) {
			_parsedChars.setLength(0);
			ArrayList<Object> params = new ArrayList<Object>();
			String fullName = name;
			if (Character.isDigit(name.charAt(0))) { // $digits
				params.add(name);
				_item = _grammar.newInlineMethod("push", name, params);
				return;
			}
			String s = _grammar._aliases.get(name);
			if (s != null) {
				int ndx;
				fullName = s;
				if ((ndx = s.indexOf('(')) > 0) {
					fullName = s.substring(0, ndx);
					pushParser(null);
					setSourceBuffer(s.substring(ndx));
					readAliasParams(params);
					popParser();
				}
			}
			if (fullName.indexOf('.') < 0 &&
				(_item=_grammar.newInlineMethod(fullName, name, params))!=null){
			} else {
				BNFExtMethodObj method;
				method = _grammar.newItemExtMethod(name, fullName, params);
				if (method._method == null) {
					//Undefined external method '&{0}'
					error(BNF015, method.getMethodName());
				} else if (method._method.toString().contains(
					"boolean org.xdef.sys.BNFGrammar.dummy(")) {
					s = method.getMethodName();
					if (s.charAt(0) != '$') {
						error(BNF015, '$' + s);
					} else {
						warning(BNF015, s);
					}
				}
				_item = method;
			}
			checkQuantifier(_item);
		}

////////////////////////////////////////////////////////////////////////////////

		/** Parse BNF grammar.
		 * @return true if a grammar was recognized and parsed.
		 */
		private boolean isGrammar() {
			nextSymbol();
			while(isAlias()){} // parse alias declarations

			//parse rules
			if (!isRule()) {
				fatal(BNF017); // at least one BNF rule expected
				_grammar = null;
				return false;
			}
			while(isRule()){}

			if (!eos()) {
				error(BNF018); //BNF Syntax error
			}

			resolveReferences();
			optimize();

			closeGrammar();
			return true;
		}

		/** Parse alias.
		 * @return true if a rule was recognized and parsed.
		 */
		private boolean isAlias() {
			if (_sym != ALIAS_DECL_SYM) {
				return false;
			}
			if (!isChar('$')) {
				error(BNF021); //Alias argument result must start with '$'
			}
			String name;
			if (isIdentifier()) {
				name = _parsedChars.toString();
				for (String s : INLINE_METHOD_NAMES) {
					if (s.equals(name)) {
						error(BNF032, name); //Alias name '&{0}' is predefined
						break;
					}
				}
			} else {
				name = "$";
			}
			skipSeparators();
			if (!isChar(':')) {
				error(BNF031, name);//After alias name '&{0}' is expected':'
			}
			skipSeparators();
			if (!isChar('$')) {
				error(BNF021); //Alias argumement result must start with '$'
			}
			if (isIdentifier()) {
				String method = _parsedChars.toString();
				skipSeparators();
				ArrayList<Object> params = new ArrayList<Object>();
				readAliasParams(params);
				if (params.size() > 0) {
					method += '(';
					for (int i = 0; i < params.size(); i++) {
						if (i > 0) {
							method += ',';
						}
						Object obj = params.get(i);
						method += obj instanceof String
							? genBNFString((String) obj, false)//MUST generate
							: obj.toString();
					}
					method += ')';
				}
				_grammar._aliases.put(name, method);
			} else {
				error(BNF022); //Alias specification error
			}
			nextSymbol();
			return true;
		}

		/** Parse rule.
		 * @return true if a rule was recognized and parsed.
		 */
		private boolean isRule() {
			if (_sym != RULE_DECL_SYM) {
				return false;
			}
			String name = _parsedChars.toString();
			if (_grammar.getRule(name) != null) {
				error(BNF019, name); //Redefinifion of rule '&{0}'
			}
			BNFRuleObj rule = _grammar.newRule(name);
			_grammar.addRule(rule);
			_item = null;
			nextSymbol();
			BNFItem item;
			if ((item = isUnion()) != null) {
				rule.setItem(item);
			} else {
				error(BNF017); //BNF rule body expected
			}
			return true;
		}

		/** Parse BNF item.
		 * @return BNFItem if an BNF item was recognized and parsed otherwise
		 * return <tt>null</tt>.
		 */
		private BNFItem isItem() {
			BNFItem item;
			switch (_sym) {
				case ITEM_SYM:
					item = _item;
					_item = null;
					nextSymbol();
					return item;
				case LBR_SYM:
					_item = null;
					nextSymbol();
					BNFSequence seq = _grammar.newItemSequence();
					if ((item = isUnion()) == null) {
						error(BNF023); //Expected BNF command section
					} else {
						seq.addItem(item);
					}
					if (_sym == RBR_SYM) {
						item = seq._items.length == 1 ? seq._items[0] :  seq;
						_item = null;
						checkQuantifier(item);
						nextSymbol();
					} else {
						item = seq;
						error(BNF024); //Expected ')'
					}
					return item;
				default:
					return null;
			}
		}

		/** Parse constraint.
		 * @return BNFItem if an constraint was recognized and parsed otherwise
		 * return <tt>null</tt>.
		 */
		private BNFItem isConstraint() {
			BNFItem item;
			if ((item = isItem()) == null) {
				return null;
			}
			if (_sym != MINUS_SYM) {
				return item;
			}
			BNFConstrain constraint = _grammar.newItemConstraint();
			constraint.addItem(item);
			do {
				nextSymbol();
				if ((item = isItem()) == null) {
					error(BNF025); //BNF Item expected
				} else {
					constraint.addItem(item);
				}
			} while (_sym == MINUS_SYM);
			return constraint;
		}

		/** Parse sequence.
		 * @return BNFItem if an sequence was recognized and parsed otherwise
		 * return <tt>null</tt>.
		 */
		private BNFItem isSequence() {
			BNFItem item;
			if ((item = isConstraint()) == null) {
				return null;
			}
			if (_sym == ITEM_SYM || _sym == LBR_SYM) {
				BNFSequence sequence = _grammar.newItemSequence();
				sequence.addItem(item);
				do {
					if ((item = isConstraint()) == null) {
						break;
					}
					sequence.addItem(item);
				} while (_sym == ITEM_SYM || _sym == LBR_SYM);
				item = sequence;
			}
			return item;
		}

		/** Parse union.
		 * @return BNFItem if an union was recognized and parsed otherwise
		 * return <tt>null</tt>.
		 */
		private BNFItem isUnion() {
			BNFItem item;
			if ((item = isSequence()) == null) {
				return null;
			}
			if (_sym == OR_SYM) {
				BNFSelection union = _grammar.newItemUnion();
				union.addItem(item);
				do {
					nextSymbol();
					if ((item = isSequence()) == null) {
						error(BNF025); //BNF Item expected
					} else {
						union.addItem(item);
					}
				} while (_sym == OR_SYM);
				item = union;
			}
			return item;
		}

		/** Resolve unresolved references (stored in <tt>_unresolvedRefs</tt>).
		 * @return <tt>true</tt> if all references were resolved.
		 */
		private boolean resolveReferences() {
			boolean result = true;
			for (UnresolvedReference ur: _unresolvedRefs) {
				BNFRuleObj rule = (BNFRuleObj) _grammar.getRule(ur.getName());
				if (rule == null) {
					result = false;
					//Undefined rule reference: '&{0}'
					getPosition().putReport(Report.error(BNF026,
						ur.getName()),getReportWriter());
				} else {
					ur.getReference().setRule(rule);
				}
			}
			return result;
		}

		private void optimize() {
			for (int i = 0; ; i++) {
				BNFRuleObj rule = (BNFRuleObj) _grammar.getRule(i);
				if (rule == null) {
					break;
				}
				rule.setItem(optimize(rule.getItem()));
			}
		}

		private BNFItem optimize(BNFItem item) {
			if (item instanceof BNFSequence) {
				BNFSequence sq = (BNFSequence) item;
				if (sq._items != null) {
					if (sq._min == 1 && sq._max == 1 && sq._items.length == 1) {
						return sq._items[0];
					}
					for (int i = 0; i < sq._items.length; i++) {
						sq._items[i] = optimize(sq._items[i]);
					}
				}
			} else if (item instanceof BNFSelection) {
				BNFSelection sel = (BNFSelection) item;
				if (sel._items != null) {
					boolean allTokens = true;
					for (int i = 0; i < sel._items.length; i++) {
						BNFItem bi = optimize(sel._items[i]);
						sel._items[i] = bi;
						if (bi._max!=1 || bi._min!=1
							|| !(bi instanceof BNFToken)) {
							allTokens = false;
						}
					}
					if (allTokens && sel instanceof BNFSelection) {
						String[] tokens = new String[sel._items.length];
						for (int i = 0; i < tokens.length; i++) {
							tokens[i] = ((BNFToken) sel._items[i])._token;
						}
						item = sel.newTokens(tokens);
					}
				}
			} else if (item instanceof BNFConstrain) {
				BNFConstrain con = (BNFConstrain) item;
				if (con._items != null) {
					for (int i = 0; i < con._items.length; i++) {
						con._items[i] = optimize(con._items[i]);
					}
				}
			}
			return item;
		}

	////////////////////////////////////////////////////////////////////////////////
	// Display gompiled BNF
	////////////////////////////////////////////////////////////////////////////////

		/** Display grammar.
		 * @param grammar Grammar to be displayed.
		 * @param out stream where to display,
		 * @param numLines if true numbers of lines are printed at line start.
		 */
		private static void display(final BNFGrammar grammar,
			final StringBuilder out,
			final boolean numLines) {
			display(out, grammar, numLines);
		}

		private static void display(final StringBuilder out,
			final BNFGrammar grammar,
			final boolean numLines) {
			if (grammar._aliases != null && grammar._aliases.size() > 0) {
				for (String key: grammar._aliases.keySet()) {
					out.append("%define $");
					if (!"$".equals(key)) {
						out.append(key);
					}
					out.append(": $");
					String s = grammar._aliases.get(key);
					if (s != null && !"$".equals(s)) {
						out.append(grammar._aliases.get(key));
					}
					out.append('\n');
				}
				out.append('\n');
			}
			for (int i = 0;;) {
				BNFRuleObj rule = (BNFRuleObj) grammar.getRule(i++);
				if (rule == null) {
					break;
				}
				if (numLines) {
					out.append("/*");
					if (i < 100) {
						out.append(i < 10 ? "00" : "0");
					}
					out.append(i).append("*/ ");
				}
				rule.display(out);
				out.append('\n');
			}
		}
	}
}