package org.xdef.impl;

import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefRegex;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.msg.SYS;
import org.xdef.sys.SObjectWriter;
import org.xdef.sys.SError;
import org.xdef.sys.SIOException;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.CodeExtMethod;
import org.xdef.impl.code.CodeI2;
import org.xdef.impl.code.CodeI1;
import org.xdef.impl.code.CodeL2;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeOp;
import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeSWTableInt;
import org.xdef.impl.code.CodeSWTableStr;
import org.xdef.impl.code.CodeStringList;
import org.xdef.impl.code.CodeTable;
import java.io.IOException;
import java.io.OutputStream;
import org.xdef.impl.xml.KNamespace;
import org.xdef.impl.code.CodeXD;
import org.xdef.impl.compile.CompileBase;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.impl.code.DefLocale;
import java.lang.reflect.Method;
import java.util.Locale;

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
	final void writeXD(final XDValue x) throws IOException {
		if (x == null) {
			writeShort((short) -1);
			return;
		}
		short code = x.getCode();
		short type = x.getItemId();
		writeShort(code);
		writeShort(type);
		switch (code) {
			case CodeTable.COMPILE_BNF:
				writeBNF((DefBNFGrammar) x);
				return;
			case CodeTable.COMPILE_XPATH:
				writeXPath((DefXPathExpr) x);
				return;
			case CodeTable.LD_CONST:
				switch (type) {
					case XDValueID.XD_BNFGRAMMAR:
						writeBNF((DefBNFGrammar) x);
						return;
					case XDValueID.XD_BNFRULE: {
						DefBNFRule y = (DefBNFRule) x;
						writeString(y.getName());
						return;
					}
					case XDValueID.XD_BOOLEAN:
						writeBoolean(x.booleanValue());
						return;
					case XDValueID.XD_BYTES:
						writeBytes(x.getBytes());
						return;
					case XDValueID.XD_DATETIME: {
						writeSDatetime(x.datetimeValue());
						return;
					}
					case XDValueID.XD_DECIMAL:
						writeBigDecimal(x.decimalValue());
						return;
					case XDValueID.XD_DURATION: {
						writeSDuration(x.durationValue());
						return;
					}
					case XDValueID.XD_ELEMENT:
						return;
					case XDValueID.XD_EXCEPTION: {
						DefException y = (DefException) x;
						writeReport(y.reportValue());
						writeString(y.getXPos());
						writeInt(y.getCodeAddr());
						return;
					}
					case XDValueID.XD_LOCALE: {
						DefLocale y = (DefLocale) x;
						Locale z = y.getLocale();
						writeString(z.getLanguage());
						writeString(z.getCountry());
						writeString(z.getVariant());
						return;
					}
					case XDValueID.XD_XPATH:
						writeXPath((DefXPathExpr) x);
						return;
					case XDValueID.XD_FLOAT:
						writeDouble(x.doubleValue());
						return;
					case XDValueID.XD_INT:
						writeLong(x.longValue());
						return;
					case XDValueID.XD_CONTAINER: {
						XDContainer y = (XDContainer) x;
						int len = y.getXDItemsNumber();
						writeInt(len);
						if (len < 0) {
							return; //null context
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
					case XDValueID.XD_NAMEDVALUE: {
						XDNamedValue y = (XDNamedValue) x;
						writeString(y.getName());
						writeXD(y.getValue());
						return;
					}
					case XDValueID.XD_PARSERESULT: {
						DefParseResult y = ((DefParseResult) x);
						writeString(y.getSourceBuffer());
						writeXD(y.getParsedValue());
						return;
					}
					case XDValueID.XD_PARSER: {
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
					case XDValueID.XD_REGEX: {
						DefRegex y = (DefRegex) x;
						writeString(y.sourceValue());
						return;
					}
					case XDValueID.XD_STRING:
						writeString(x.stringValue());
						return;
					case XDValueID.XD_XQUERY:
						writeString(x.stringValue());
						return;
					case CompileBase.UNIQUESET_VALUE:
					case CompileBase.UNIQUESET_M_VALUE: {
						CodeUniqueset y = (CodeUniqueset) x;
						CodeUniqueset.ParseItem[] keys = y.getParsedItems();
						writeLength(keys.length);
						for (CodeUniqueset.ParseItem key : keys) {
							writeString(key.getParseName());
							writeString(key.getDeclaredTypeName());
							writeInt(key.getParseMethodAddr());
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
						for (int i = 0; i < pars.length; i++) {
							writeString(pars[i].getName());
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
					//Internal error&{0}{: }
					throw new SIOException(SYS.SYS066,
						"Illegal object: " + x.getClass().getName() +
						"/code=" + code + "/type" + type);
				}
		}
	}

}