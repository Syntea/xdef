package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDParserAbstract;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.XDContainer;
import static org.xdef.XDParser.ARGUMENT;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.FORMAT;
import static org.xdef.XDParser.FRACTIONDIGITS;
import static org.xdef.XDParser.ITEM;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXEXCLUSIVE;
import static org.xdef.XDParser.MAXINCLUSIVE;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINEXCLUSIVE;
import static org.xdef.XDParser.MININCLUSIVE;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.OUTFORMAT;
import static org.xdef.XDParser.PARAM_NAMES;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.SEPARATOR;
import static org.xdef.XDParser.TOTALDIGITS;
import static org.xdef.XDParser.WHITESPACE;
import org.xdef.XDRegex;
import static org.xdef.XDValueID.XD_CONTAINER;
import org.xdef.impl.code.DefJNull;
import org.xdef.xon.XonTools;

/** Abstract class as the base for all test value parsers.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParser extends XDParserAbstract implements XDParser, XDValue {

	/** WhiteSpace handling mode. Capital letter means fixed value.
	 * <br>'p' (preserve)<p> No normalization is done, the value is not changed (this is the behavior required
	 * by [XML 1.0 (Second Edition)]* for element content)
	 * <br>'r' (replace)<p>All occurrences of #x9 (tab), #xA (line feed) and #xD (carriage return)
	 * are replaced with #x20 (space)
	 * <br>'c' (collapse)<p> After the processing implied by replace, contiguous sequences of #x20's
	 * are collapsed to a single #x20, and leading and trailing #x20's are removed.
	 */
	protected byte _whiteSpace; //r replace, c collapse, 0 preserve
	protected XDRegex[] _patterns;

	protected XSAbstractParser() {}

	public abstract void initParams();

	/** Convert the argument to the relevant object.
	 * @param x input object (may be string).
	 * @param xnode actual XXNode object.
	 * @return relevant object.
	 */
	public XDValue iObject(final XXNode xnode, final Object x) {
		if (x == null) {
			throw new SRuntimeException(XDEF.XDEF819, parserName()); //Value of &{0} can't be null
		}
		if (x instanceof XDValue) {
			XDValue y = (XDValue) x;
			if (y.isNull()) {
				throw new SRuntimeException(XDEF.XDEF819, parserName()); //Value of &{0} can't be null
			}
			if (y.getItemId() == parsedType()) {
				return y;
			}
		}
		XDParseResult p = new DefParseResult(x.toString());
		parseObject(xnode, p);
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		if (!p.eos()) {
			//After the item '&{0}' follows an illegal character&{1}{: }
			p.errorWithString(XDEF.XDEF804, parserName());
		}
		if (p.matches()) {
			return p.getParsedValue();
		}
		ArrayReporter reporter = p.getReporter();
		throw new SRuntimeException(reporter.getLastErrorReport());
	}

	@Override
	/** Create parseResult object from StringParser.
	 * @param p String parser with source.
	 * @param xnode actual XXNode object.
	 */
	public void check(final XXNode xnode, final XDParseResult p) {
		if (xnode != null && xnode.getXMElement().getXonMode() != 0 && "null".equals(p.getSourceBuffer())) {
			p.setParsedValue(new DefJNull(XonTools.JNULL)); // set null
			p.setEos();
			return;
		}
		if (p.getSourceBuffer() == null) {
			p.error(XDEF.XDEF805, parserName()); //Parsed value in &{0} is null
			return;
		}
		XDParser base = getBase();
		if (base != null) {
			int i = p.getIndex();
			base.check(xnode, p);
			if (!p.matches()) {
				return;
			}
			p.setIndex(i);
		}
		parseObject(xnode, p);
		if (p.matches()) {
			if (_whiteSpace == 'c') {
				p.isSpaces();
			}
			if (!p.eos()) {
				//After the item '&{0}' follows an illegal character&{1}{: }
				p.errorWithString(XDEF.XDEF804, parserName());
			}
			finalCheck(xnode, p);
		}
	}

	@Override
	/** Get value of whiteSpace parameter.
	 * @return 0 .. preserve, 'r' .. replace, 'c' .. collapse
	 */
	public byte getWhiteSpaceParam() {return _whiteSpace;}

	public void setMinExclusive(final XDValue x) {}//default not specified

	public void setMaxExclusive(final XDValue x) {}//default not specified

	public void setMinInclusive(final XDValue x) {}//default not specified

	public void setMaxInclusive(final XDValue x) {}//default not specified

	public void setTotalDigits(final long x) {} //default: not specified

	public void setFractionDigits(final long x) {
		if (x != 0) {
			//Parameter '&{0}' can be only '&{1}' for '&{2}'
			throw new SRuntimeException(XDEF.XDEF812, "fractionDigits", "0", parserName());
		}
	} //default: not specified

	public void setLength(final long x) {} //default: not specified

	public void setMinLength(final long x) {} //default: not specified

	public void setMaxLength(final long x) {} //default: not specified

	public void setWhiteSpace(final String s) {
		byte old = _whiteSpace;
		switch (s) {
			case "collapse": _whiteSpace = 'c'; break;
			case "replace": _whiteSpace = 'r'; break;
			case "preserve": _whiteSpace = 0; break;
			default: //Parameter '&{0}' can be only '&{1}' for '&{2}'
				throw new SRuntimeException(XDEF.XDEF812,
					"whiteSpace","collapse, replace, preserve", parserName());
		}
		if (old == 'c' && _whiteSpace != 'c' ||
			old == 'p' && _whiteSpace == 0) {
			//Parameter '&{0}' can be only '&{1}' for '&{2}'
			throw new SRuntimeException(XDEF.XDEF812,
				"whiteSpace&", (old == 'c' ? "collapse" : "collapse, replace"), parserName());
		}
	}

	public void setItem(final XDValue item) {} //default: not specified

	abstract public byte getDefaultWhiteSpace();

	private int getKeyId(final String name) {
		int keyMask = getLegalKeys();
		for (int i = 0, id = 1; i < PARAM_NAMES.length; i++, id += id) {
			if (PARAM_NAMES[i].equals(name)) {
				return ((id & keyMask) != 0) ? id : -1;
			}
		}
		return -1;
	}

	private XDValue getParam(final XDValue[] params, final int id) {
		for (int i = 0, x = 1; i < PARAM_NAMES.length; i++,x += x) {
			if (x == id) {
				String name = PARAM_NAMES[i];
				for (int j = 0; j < params.length; j+=2) {
					if (name.equals(params[j].toString())) {
						XDValue result = params[j+1];
						return result != null ? result.isNull() ? null : result : null;
					}
				}
				break;
			}
		}
		return null;
	}

	private void setNamedParams(final XDValue[] params)	throws SException {
		initParams();
		int len;
		if (params == null || (len = params.length*2) == 0) {
			return;
		}
		if ((len & 1) != 0) {
			throw new SException(XDEF.XDEF571); //Number of parameters must be multile of 2
		}
		int ids = 0;
		for (int i = 0; i < params.length; i+=2) {
			String key = params[i].toString();
			int id = getKeyId(key);
			if (id < 0) {
				throw new SException(XDEF.XDEF801, key); //Illegal parameter name '&{0}'
			}
			if ((ids & id) != 0) {
				throw new SException(XDEF.XDEF802, key); //Parameter '&{0}' respecified
			}
			ids += id;
		}
		XDValue item;
		if ((item = getParam(params, ITEM)) != null) {
			setItem(item);
		}
		if ((item = getParam(params, SEPARATOR)) != null) {
			setSeparator(item.toString());
		}
		if ((item = getParam(params, FORMAT)) != null) {
			setFormat(item.toString());
		}
		if ((item = getParam(params, OUTFORMAT)) != null) {
			setOutFormat(item.toString());
		}
		if ((item = getParam(params, ARGUMENT)) != null) {
			setArgument(item);
		}
		XDValue minIncl = getParam(params, MININCLUSIVE);
		XDValue minExcl = getParam(params, MINEXCLUSIVE);
		XDValue maxIncl = getParam(params, MAXINCLUSIVE);
		XDValue maxExcl = getParam(params, MAXEXCLUSIVE);
		try {
			if (maxIncl != null) {
				if (maxExcl != null) {
					//Both parameters can't be specified: '&{0}', '&{1}
					throw new SException(XDEF.XDEF807, "maxInclusive", "maxExcelusive");
				}
				setMaxInclusive(maxIncl);
			} else if (maxExcl != null) {
				setMaxExclusive(maxExcl);
			}
			if (minIncl != null) {
				if (minExcl != null) {
					//Both parameters can't be specified: '&{0}', '&{1}'
					throw new SException(XDEF.XDEF807, "&{p1}minEnclusive", "minExcelusive");
				}
				setMinInclusive(minIncl);
				XDValue minIn = getMinInclusive();
				XDValue maxEx = getMaxExclusive();
				XDValue maxIn = getMaxInclusive();
				if (maxIn != null && maxIn.compareTo(minIn) < 0 ||
					maxEx != null && maxEx.compareTo(minIn) <= 0) {
					throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
				}
			} else if (minExcl !=null){
				setMinExclusive(minExcl);
				XDValue maxEx = getMaxExclusive();
				XDValue maxIn = getMaxInclusive();
				XDValue minEx = getMinExclusive();
				if (maxIn != null && maxIn.compareTo(minEx) < 0 ||
					maxEx != null && maxEx.compareTo(minEx) <= 0) {
					throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
				}
			}
		} catch (RuntimeException ex) {
			if (ex instanceof SThrowable) {
				throw new SException(((SThrowable) ex).getReport());
			}
			throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
		}
		if ((item = getParam(params, TOTALDIGITS)) != null && !item.isNull()) {
			setTotalDigits(Long.parseLong(item.toString()));
		}
		if ((item = getParam(params, FRACTIONDIGITS)) != null &&!item.isNull()){
			setFractionDigits(Long.parseLong(item.toString()));
		}
		if ((item = getParam(params, LENGTH)) != null && !item.isNull()) {
			setLength(Long.parseLong(item.toString()));
		}
		if ((item = getParam(params, MINLENGTH)) != null && !item.isNull()) {
			long i = Long.parseLong(item.toString());
			if (getLength() > 0 || i < 0) {
				throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
			}
			setMinLength(i);
		}
		if ((item = getParam(params, MAXLENGTH)) != null && !item.isNull()) {
			long i = Long.parseLong(item.toString());
			if (getLength() > 0 || getMinLength() > i || i < 0) {
				throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
			}
			setMaxLength(i);
		}
		if ((item = getParam(params, PATTERN)) != null && !item.isNull()) {
			if (item.getItemId() == XD_CONTAINER) {
				setPatterns(((DefContainer) item).getXDItems());
			} else {
				setPatterns(new XDValue[]{item});
			}
		}
		if ((item = getParam(params, ITEM)) != null) {
			setItem(item);
		}
		if ((item = getParam(params, ENUMERATION)) != null && !item.isNull()) {
			if (item.getItemId() == XD_CONTAINER) {
				setEnumeration(((DefContainer) item).getXDItems());
			} else {
				setEnumeration(new XDValue[]{item});
			}
		}
		if ((item = getParam(params, BASE)) != null && !item.isNull()) {
			setBase(valueToParser(item));
		}
		if ((item = getParam(params, WHITESPACE)) != null && !item.isNull()) {
			setWhiteSpace(item.toString());
		}
	}

	@Override
	public void setNamedParams(final XXNode xnode,
		final XDContainer params) throws SException {
		int len;
		if (params == null || (len = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		XDValue[] x = new XDValue[len*2];
		for (int i = 0; i < len; i++) {
			String name = params.getXDNamedItemName(i);
			x[i*2] = new DefString(name);
			x[i*2 + 1] = params.getXDNamedItemValue(name);
		}
		setNamedParams(x);
	}

	public void setEnumeration(final Object[] o) {}

	public void setPatterns(final Object[] pats) {
		if (pats == null || pats.length == 0) {
			_patterns = null;
		} else {
			_patterns = new XDRegex[pats.length];
			for (int i = 0; i < pats.length; i++) {
				_patterns[i] = new XDRegex(pats[i].toString(), true);
			}
		}
	}

	public void checkPatterns(final XDParseResult p) {
		if (_patterns == null || !p.matches()) {
			return;
		}
		for (XDRegex x: _patterns) {
			if (x.matches(p.getSourceBuffer())) {
				return; // found pattern; OK
			}
		}
		p.errorWithString(XDEF.XDEF811, parserName()); //Doesn't fit any pattern from list for '&{0}'
	}

	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		addNamedParams(map);
		XDParser base = getBase();
		if (base != null) {
			map.setXDNamedItem("base", base);
		}
		long i;
		if ((i = getTotalDigits()) >= 0) {
			map.setXDNamedItem("totalDigits", new DefLong(i));
		}
		if ((i = getFractionDigits()) >= 0) {
			map.setXDNamedItem("fractionDigits", new DefLong(i));
		}
		long min = getMinLength();
		long max = getMaxLength();
		if (min == max && min > 0) {
			map.setXDNamedItem("length", new DefLong(min));
		} else {
			if ((i = getMinLength()) >= 0) {
				map.setXDNamedItem("minLength", new DefLong(i));
			}
			if ((i = getMaxLength()) >= 0) {
				map.setXDNamedItem("maxLength", new DefLong(i));
			}
		}
		XDValue x;
		if ((x = getMinExclusive()) != null) {
			map.setXDNamedItem("minExclusive", x);
		}
		if ((x = getMaxExclusive()) != null) {
			map.setXDNamedItem("maxExclusive", x);
		}
		if ((x = getMinInclusive()) != null) {
			map.setXDNamedItem("minInclusive", x);
		}
		if ((x = getMaxInclusive()) != null) {
			map.setXDNamedItem("maxInclusive", x);
		}
		if (getDefaultWhiteSpace() != getWhiteSpace()) {
			switch (getWhiteSpace()) {
				case 'c':
					map.setXDNamedItem("whiteSpace", new DefString("collapse")); break;
				case 'r':
					map.setXDNamedItem("whiteSpace", new DefString("replace")); break;
				case 0:
					map.setXDNamedItem("whiteSpace", new DefString("preserve")); break;
			}
		}
		XDValue[] en = getEnumeration();
		if (en != null && en.length > 0) {
			map.setXDNamedItem("enumeration", new DefContainer(en));
		}
		XDRegex[] p = getPatterns();
		if (p != null && p.length > 0) {
			XDValue[] q = new XDValue[p.length];
			for (int j = 0; j < p.length; j++) {
				q[j] = new DefString((p[j]).sourceValue());
			}
			map.setXDNamedItem("pattern", new DefContainer(q));
		}
		return map;
	}

	public void addNamedParams(final XDContainer map) {}

	public long getTotalDigits() { return -1; }//default 0

	public long getFractionDigits() { return -1; } //default 0

	public XDValue getMinExclusive() { return null; }; //default null

	public XDValue getMaxExclusive() { return null; }; //default null

	public XDValue getMinInclusive() { return null; }; //default null

	public XDValue getMaxInclusive() { return null; }; //default null

	public long getLength() { return -1; }

	public long getMinLength() { return -1; }

	public long getMaxLength() { return -1; }

	public byte getWhiteSpace() { return _whiteSpace; }

	public XDValue[] getEnumeration() {return null;} //default null

	public void checkEnumeration(final XDParseResult p) {
		if (p.matches()) {
			XDValue[] enumeration = getEnumeration();
			if (enumeration != null) {
				XDValue val = p.getParsedValue();
				for (XDValue enumeration1 : enumeration) {
					if (enumeration1.equals(val)) {
						return;
					}
				}
				p.errorWithString(XDEF.XDEF810, parserName());//Doesn't fit enumeration list of '&{0}'&{1}{: }
			}
		}
	}

	public XDRegex[] getPatterns() {return _patterns;}

	@Override
	/** Get result type ID of parsing.
	 * @return result type ID of parsing.
	 */
	public abstract short parsedType();

	public boolean addTypeParser(final XDValue x){return false;}//must be Parser

	public void setFormat(final String x) {}

	public String getFormat() { return null; }

	public void setOutFormat(final String x) {}

	public String getOutFormat() { return null; }

	public void setArgument(final XDValue x) {}

	public XDValue getArgument() { return null; }

	protected final int getIdIndex(final int id, final int[] legalIds) {
		for (int i = 0; i < legalIds.length; i++) {
			if (id == legalIds[i]) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {return parserName();}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return this;}

	@Override
	/** Set result type of operation (ignored here).
	 * @param resultType id of result type.
	 */
	public void setItemType(final short resultType) {}

	@Override
	/** Get parameter of operation (returns 0 here).
	 * @return parameter.
	 */
	public int getParam() {return 0;}

	@Override
	/** Set parameter of operation (ignored here).
	 * @param param value of operation parameter.
	 */
	public void setParam(final int param) {}

	@Override
	/** Get name of value.
	 * @return The name.
	 */
	public abstract String parserName();

	@Override
	public boolean equals(final XDValue o) {
		if (!(o instanceof XSAbstractParser)) {
			return false;
		}
		XSAbstractParser x = (XSAbstractParser) o;
		if (!parserName().equals(x.parserName())) {
			return false;
		}
		if (getBase() == null) {
			if (x.getBase() != null) {
				return false;
			}
		} else {
			if (!getBase().equals(x.getBase())) {
				return false;
			}
		}
		if (_whiteSpace != x._whiteSpace) {
			return false;
		}
		if (_patterns == null) {
			if (x._patterns != null) {
				return false;
			}
		} else {
			if (x._patterns == null) {
				return false;
			}
			if (_patterns.length != x._patterns.length) {
				return false;
			}
			for (int i = 0; i < _patterns.length; i++) {
				if (!_patterns[i].equals(x._patterns[i])) {
					return false;
				}
			}
		}
		XDValue[] e1 = getEnumeration();
		XDValue[] e2 = x.getEnumeration();
		if (e1 == null) {
			if (e2 != null) {
				return false;
			}
		} else {
			if (e2 == null) {
				return false;
			}
			if (e1.length != e2.length) {
				return false;
			}
			for (int i = 0; i < e1.length; i++) {
				if (!e1[i].equals(e2[i])) {
					return false;
				}
			}
		}
		if (getTotalDigits() != x.getTotalDigits()) {
			return false;
		}
		if (getFractionDigits() != x.getFractionDigits()) {
			return false;
		}
		if (getMinLength() != x.getMinLength() || getMaxLength() != getMaxLength()) {
			return false;
		}
		if (getMinInclusive() == null) {
			if (x.getMinInclusive() != null) {
				return false;
			}
		} else if (!getMinInclusive().equals(x.getMinInclusive())) {
			return false;
		}
		if (getMaxInclusive() == null) {
			if (x.getMaxInclusive() != null) {
				return false;
			}
		} else if (!getMaxInclusive().equals(x.getMaxInclusive())) {
			return false;
		}
		if (getMinExclusive() == null) {
			if (x.getMinExclusive() != null) {
				return false;
			}
		} else if (!getMinExclusive().equals(x.getMinExclusive())) {
			return false;
		}
		if (getMaxExclusive() == null) {
			if (x.getMaxExclusive() != null) {
				return false;
			}
		} else if (!getMaxExclusive().equals(x.getMaxExclusive())) {
			return false;
		}
		String s1, s2;
		s1 = getSeparator();
		s2 = x.getSeparator();
		if (s1 == null) {
			if (s2 != null) {
				return false;
			}
		} else if (!s1.equals(s2)) {
			return false;
		}
		s1 = getFormat();
		s2 = x.getFormat();
		if (s1 == null) {
			if (s2 != null) {
				return false;
			}
		} else if (!s1.equals(s2)) {
			return false;
		}
		s1 = getOutFormat();
		s2 = x.getOutFormat();
		if (s1 == null) {
			if (s2 != null) {
				return false;
			}
		} else if (!s1.equals(s2)) {
			return false;
		}
		XDValue a1 = getArgument();
		XDValue a2 = x.getArgument();
		if (a1 == null) {
			if (a2 != null) {
				return false;
			}
		} else if (!a1.equals(a2)) {
			return false;
		}
		return true;
	}

	@Override
	abstract public int getLegalKeys();
}
