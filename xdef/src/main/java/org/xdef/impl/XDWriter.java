package org.xdef.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;
import org.xdef.XDBytes;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ANYURI;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.XDValueID.XD_BNFRULE;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTES;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_EMAIL;
import static org.xdef.XDValueID.XD_EXCEPTION;
import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueID.XD_LOCALE;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_PRICE;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_XPATH;
import static org.xdef.XDValueID.XD_XQUERY;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.impl.code.CodeExtMethod;
import org.xdef.impl.code.CodeI1;
import org.xdef.impl.code.CodeI2;
import org.xdef.impl.code.CodeL2;
import org.xdef.impl.code.CodeOp;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeSWTableInt;
import org.xdef.impl.code.CodeSWTableStr;
import org.xdef.impl.code.CodeStringList;
import static org.xdef.impl.code.CodeTable.COMPILE_BNF;
import static org.xdef.impl.code.CodeTable.COMPILE_XPATH;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.CodeXD;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefGPSPosition;
import org.xdef.impl.code.DefLocale;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefPrice;
import org.xdef.impl.code.DefRegex;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.ParseItem;
import org.xdef.impl.xml.KNamespace;
import org.xdef.msg.SYS;
import org.xdef.sys.SError;
import org.xdef.sys.SIOException;
import org.xdef.sys.SObjectWriter;

/** Provides writing of XD objects to OutputStream.
 * @author Vaclav Trojan
 */
public final class XDWriter extends SObjectWriter {
	static final byte ID_CODEOP = 0;
	static final byte ID_CODEI1 = 1;
	static final byte ID_CODEI2 = 2;
	static final byte ID_CODEL2 = 3;
	static final byte ID_CODEPARSER = 4;
	static final byte ID_CODES1 = 5;
	static final byte ID_CODESWTABI = 6;
	static final byte ID_CODESWTABS = 7;
	static final byte ID_CODESLIST = 8;
	static final byte ID_CODEXD = 9;
	static final byte ID_CODEEXT = 10;
	static final byte ID_CODEUNDEF = 11;

	/** Creates a new instance of XDWriter.
	 * @param out Output stream where data of XD objects will be written.
	 */
	public XDWriter(OutputStream out) {super(out);}

	private void writeBNF(final DefBNFGrammar y) throws IOException {
		writeInt(y.getParam());
		writeString(y.stringValue());
	}

	private void writeXPath(final DefXPathExpr y) throws IOException {
		KNamespace nc = (KNamespace) y.getNamespaceContext();
		String[] prefixes = nc == null ? new String[0] : nc.getAllPrefixes();
		int len = prefixes.length;
		writeLength(len);
		for (int i = 0; i < len; i++) {
			writeString(prefixes[i]);
			writeString(nc.getNamespaceURI(prefixes[i]));
		}
		writeString(y.stringValue());
	}

	/** Write XD object.
	 * @param x the object to be written.
	 * @throws IOException if an error occurs.
	 */
	public final void writeXD(final XDValue x) throws IOException {
		if (x == null) {
			writeShort((short) -1);
			return;
		}
		short code = x.getCode();
		short type = x.getItemId();
		writeShort(code);
		writeShort(type);
		switch (code) {
			case COMPILE_BNF:
				writeBNF((DefBNFGrammar) x);
				return;
			case COMPILE_XPATH:
				writeXPath((DefXPathExpr) x);
				return;
			case LD_CONST:
				switch (type) {
					case XD_BNFGRAMMAR:
						writeBNF((DefBNFGrammar) x);
						return;
					case XD_BNFRULE: {
						DefBNFRule y = (DefBNFRule) x;
						writeString(y.getName()); // ???
						return;
					}
					case XD_BOOLEAN:
						writeBoolean(x.booleanValue());
						return;
					case XD_BYTES:
						writeBytes(x.getBytes());
						writeBoolean(((XDBytes) x).isBase64());
						return;
					case XD_DATETIME: {
						writeSDatetime(x.datetimeValue());
						return;
					}
					case XD_DECIMAL:
						writeBigDecimal(x.decimalValue());
						return;
					case XD_BIGINTEGER:
						writeBigInteger(x.integerValue());
						return;
					case XD_DURATION: {
						writeSDuration(x.durationValue());
						return;
					}
					case XD_ANYURI: {
						URI u = (URI) x.getObject();
						writeString(u == null ? null : u.toASCIIString());
						return;
					}
					case XD_EMAIL:
						writeString(x.stringValue());
						return;
					case XD_EXCEPTION: {
						DefException y = (DefException) x;
						writeReport(y.reportValue());
						writeString(y.getXPos());
						writeInt(y.getCodeAddr());
						return;
					}
					case XD_LOCALE: {
						DefLocale y = (DefLocale) x;
						Locale z = y.getLocale();
						writeString(z.getLanguage());
						writeString(z.getCountry());
						writeString(z.getVariant());
						return;
					}
					case XD_XPATH:
						writeXPath((DefXPathExpr) x);
						return;
					case XD_DOUBLE:
						writeDouble(x.doubleValue());
						return;
					case XD_LONG:
						writeLong(x.longValue());
						return;
					case XD_CONTAINER: {
						XDContainer y = (XDContainer) x;
						int len = y.getXDItemsNumber();
						writeInt(len);
						if (len < 0) {
							return; //null Container
						}
						for (int i = 0; i < len; i++) {
							writeXD(y.getXDItem(i));
						}
						len = y.getXDNamedItemsNumber();
						writeLength(len);
						for (int i = 0; i < len; i++) {
							writeXD(y.getXDNamedItem(y.getXDNamedItemName(i)));
						}
						return;
					}
					case XD_GPSPOSITION: {
						DefGPSPosition y = (DefGPSPosition) x;
						writeDouble(y.latitude());
						writeDouble(y.longitude());
						writeDouble(y.altitude());
						writeString(y.name());
					}
					case XD_PRICE: {
						DefPrice y = (DefPrice) x;
						writeBigDecimal(y.amount());
						writeString(y.currencyCode());
					}
					case XD_NAMEDVALUE: {
						XDNamedValue y = (XDNamedValue) x;
						writeString(y.getName());
						writeXD(y.getValue());
						return;
					}
					case XD_PARSERESULT: {
						DefParseResult y = ((DefParseResult) x);
						writeString(y.getSourceBuffer());
						writeXD(y.getParsedValue());
						return;
					}
					case XD_PARSER: {
						if (x.isNull()) {
							writeString(null);
							writeString(null);
						} else {
							XDParser y = (XDParser) x;
							writeString(y.getDeclaredName());
							writeString(y.parserName());
							writeXD((XDValue) y.getNamedParams());
						}
						return;
					}
					case XD_REGEX: {
						DefRegex y = (DefRegex) x;
						writeString(y.sourceValue());
						writeBoolean(y.isXML());
						return;
					}
					case XD_STRING:
						writeString(x.stringValue());
						return;
					case XD_XQUERY:
						writeString(x.stringValue());
						return;
					case X_UNIQUESET:
					case X_UNIQUESET_M: {
						CodeUniqueset y = (CodeUniqueset) x;
						ParseItem[] keys = y.getParsedItems();
						writeLength(keys.length);
						for (ParseItem key : keys) {
							writeString(key.getParseName());
							writeString(key.getDeclaredTypeName());
							writeInt(key.getParseMethodAddr());
							writeInt(key.getKeyIndex());
							writeShort(key.getParsedType());
							writeBoolean(key.isOptional());
						}
						String[] varNames = y.getVarNames();
						int len = varNames == null ? 0 : varNames.length;
						writeLength(len);
						if (len > 0) {
							for (int i = 0; i < len; i++) {
								writeString(varNames[i]);
							}
						}
						writeString(y.getName());
						return;
					}
					default:
						if (x.isNull()) return;
						//Internal error&{0}{: }
						throw new SError(SYS.SYS066,
							"TODO type: " + type + ";" + x);
				}
				default: {
					if (x instanceof CodeExtMethod) {
						writeByte(ID_CODEEXT);
						CodeExtMethod y = (CodeExtMethod) x;
						writeInt(y.getParam());
						writeString(y.getName());
						Method m = y.getExtMethod();
						writeString(m.getName());
						writeString(m.getDeclaringClass().getName());
						Class<?>[] pars = m.getParameterTypes();
						writeLength(pars.length);
						for (Class<?> par : pars) {
							writeString(par.getName());
						}
						return;
					}
					if (x instanceof CodeParser) {
						writeByte(ID_CODEPARSER);
						CodeParser y = (CodeParser) x;
						writeInt(y.getParam());
						writeString(y.stringValue());
						String [] sqParamNames = y.getSqParamNames();
						int len = sqParamNames!=null ? sqParamNames.length : -1;
						writeInt(len);
						for (int i = 0; i < len; i++) {
							writeString(sqParamNames[i]);
						}
						return;
					}
					if (x instanceof CodeStringList) {
						writeByte(ID_CODESLIST);
						CodeStringList y = (CodeStringList) x;
						int len = y.getParam();
						writeInt(len);
						for (int i = 0; i < len; i++) {
							writeString(y.getStringList()[i]);
						}
						return;
					}
					if (x instanceof CodeSWTableInt) {
						writeByte(ID_CODESWTABI);
						CodeSWTableInt y = (CodeSWTableInt) x;
						writeInt(y.getParam());
						int len = y._adrs.length;
						writeLength(len);
						for (int i = 0; i < len; i++) {
							writeInt(y._adrs[i]);
							writeLong(y._list[i]);
						}
						return;
					}
					if (x instanceof CodeSWTableStr) {
						writeByte(ID_CODESWTABS);
						CodeSWTableStr y = (CodeSWTableStr) x;
						writeInt(y.getParam());
						int len = y._adrs.length;
						writeLength(len);
						for (int i = 0; i < len; i++) {
							writeInt(y._adrs[i]);
							writeString(y._list[i]);
						}
						return;
					}
					if (x instanceof CodeXD) {
						CodeXD y = (CodeXD) x;
						writeByte(ID_CODEXD);
						writeInt(y.getParam());
						writeXD(y.getParam2());
						return;
					}
					if (x instanceof CodeS1) {
						CodeS1 y = (CodeS1) x;
						writeByte(ID_CODES1);
						writeInt(y.getParam());
						writeString(y.stringValue());
						return;
					}
					if (x instanceof CodeI2) {
						writeByte(ID_CODEI2);
						CodeI2 y = (CodeI2) x;
						writeInt(y.getParam());
						writeInt(y.intValue());
						return;
					}
					if (x instanceof CodeL2) {
						CodeL2 y = (CodeL2) x;
						writeByte(ID_CODEL2);
						writeInt(y.getParam());
						writeLong(y.longValue());
						return;
					}
					if (x instanceof CodeI1) {
						CodeI1 y = (CodeI1) x;
						writeByte(ID_CODEI1);
						writeInt(y.getParam());
						return;
					}
					if (x instanceof CodeOp) {
						writeByte(ID_CODEOP);
						return;
					}
					throw new SIOException(SYS.SYS066, //Internal error&{0}{: }
						"Illegal object: " + x.getClass().getName() +
						"/code=" + code + "/type" + type);
				}
		}
	}
}