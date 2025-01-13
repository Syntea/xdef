package org.xdef.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.xdef.XDContainer;
import org.xdef.XDGPSPosition;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDPrice;
import org.xdef.XDRegex;
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
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_EMAIL;
import static org.xdef.XDValueID.XD_EXCEPTION;
import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueID.XD_LOCALE;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_OBJECT;
import static org.xdef.XDValueID.XD_OUTPUT;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_PRICE;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_SERVICE;
import static org.xdef.XDValueID.XD_STATEMENT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_UNIQUESET_KEY;
import static org.xdef.XDValueID.XD_XPATH;
import static org.xdef.XDValueID.XD_XQUERY;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import static org.xdef.impl.XDWriter.ID_CODEEXT;
import static org.xdef.impl.XDWriter.ID_CODEI1;
import static org.xdef.impl.XDWriter.ID_CODEI2;
import static org.xdef.impl.XDWriter.ID_CODEL2;
import static org.xdef.impl.XDWriter.ID_CODEOP;
import static org.xdef.impl.XDWriter.ID_CODEPARSER;
import static org.xdef.impl.XDWriter.ID_CODES1;
import static org.xdef.impl.XDWriter.ID_CODESLIST;
import static org.xdef.impl.XDWriter.ID_CODESWTABI;
import static org.xdef.impl.XDWriter.ID_CODESWTABS;
import static org.xdef.impl.XDWriter.ID_CODEXD;
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
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefLocale;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefObject;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefSQLResultSet;
import org.xdef.impl.code.DefSQLService;
import org.xdef.impl.code.DefSQLStatement;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefURI;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.DefXQueryExpr;
import org.xdef.impl.code.ParseItem;
import static org.xdef.impl.compile.CompileBase.getParser;
import org.xdef.impl.xml.KNamespace;
import org.xdef.msg.SYS;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SException;
import org.xdef.sys.SIOException;
import org.xdef.sys.SObjectReader;

/** Provides reading of XD objects from InputStream.
 * @author Vaclav Trojan
 */
public final class XDReader extends SObjectReader {

	/** Creates a new instance of XDReader.
	 * @param in Input stream with data of XD objects
	 */
	public XDReader(InputStream in) {super(in);}

	private static Class<?> getClassForName(final String name)
		throws IOException {
		switch(name) { //first check primitive type names
			case "boolean": return java.lang.Boolean.TYPE;
			case "byte": return java.lang.Byte.TYPE;
			case "short": return java.lang.Short.TYPE;
			case "int": return java.lang.Integer.TYPE;
			case "long": return java.lang.Long.TYPE;
			case "char": return java.lang.Character.TYPE;
			case "float": return java.lang.Float.TYPE;
			case "double": return java.lang.Double.TYPE;
		}
		try { //return class
			return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException ex) {
			throw new SIOException(SYS.SYS066,"Class not found: "+name+"; "+ex); //Internal error&{0}{: }
		}
	}

	private XDValue readBNF() throws IOException {
		int extVar = readInt();
		String source = readString();
		if (extVar == -1) {
			return new DefBNFGrammar(source, null);
		}
		DefBNFGrammar y = new DefBNFGrammar();
		y.setParam(extVar);
		y.setSource(source);
		return y;
	}

	private XDValue readXPath() throws IOException {
		int len = readLength();
		KNamespace nc = new KNamespace();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				String prefix = readString();
				String uri = readString();
				if (!prefix.startsWith("xml")) {
					nc.setPrefix(prefix, uri);
				}
			}
		}
		String xpath = readString();
		return new DefXPathExpr(xpath, nc, null, null);
	}

	/** Read XD object.
	 * @return the XD object constructed from input stream.
	 * @throws IOException if an error occurs.
	 */
	public final XDValue readXD() throws IOException {
		short code = readShort();
		if (code < 0) {
			if (code == -1) {
				return null;
			}
			throw new SIOException(SYS.SYS066, "Illegal code: " + code); //Internal error&{}{: }
		}
		short type = readShort();
		switch (code) {
			case COMPILE_BNF:
				return readBNF();
			case COMPILE_XPATH:
				return readXPath();
			case LD_CONST: {
				switch (type) {
					case XD_BNFGRAMMAR: return readBNF();
					case XD_BNFRULE:
						readString(); // ???
						return new DefBNFRule(null);
					case XD_BOOLEAN: return new DefBoolean(readBoolean());
					case XD_BYTES: return new DefBytes(readBytes(), readBoolean());
					case XD_DATETIME: {
						SDatetime x = readSDatetime();
						return x == null ? new DefDate() : new DefDate(x);
					}
					case XD_DECIMAL: return new DefDecimal(readBigDecimal());
					case XD_BIGINTEGER: return new DefBigInteger(readBigInteger());
					case XD_DURATION: {
						SDuration x = readSDuration();
						return x==null ? new DefDuration() : new DefDuration(x);
					}
					case XD_ANYURI: return new DefURI(readString());
					case XD_EMAIL: return new DefEmailAddr(readString());
					case XD_ELEMENT: return new DefElement();
					case XD_EXCEPTION: return new DefException(readReport(), readString(), readInt());
					case XD_DOUBLE: return new DefDouble(readDouble());
					case XD_LONG: return new DefLong(readLong());
					case XD_CONTAINER: {
						int len = readInt();
						if (len == -1) {
							return new DefContainer((Object) null);
						}
						DefContainer y = new DefContainer();
						for (int i = 0; i < len; i++) {
							y.addXDItem(readXD());
						}
						len = readLength();
						for (int i = 0; i < len; i++) {
							y.setXDNamedItem((XDNamedValue) readXD());
						}
						return y;
					}
					case XD_GPSPOSITION:
						return new XDGPSPosition(
							new GPSPosition(readDouble(), readDouble(), readDouble(), readString()));
					case XD_PRICE: return new XDPrice(new Price(readBigDecimal(), readString()));
					case XD_LOCALE: return new DefLocale(readString(), readString(), readString());
					case XD_NAMEDVALUE:return new DefNamedValue(readString(), readXD());
					case XD_OBJECT:return new DefObject();
					case XD_OUTPUT:return new DefOutStream();
					case XD_PARSERESULT: {
						DefParseResult y = new DefParseResult();
						String s = readString();
						if (s != null) {
							y.setSourceBuffer(s);
						}
						XDValue v = readXD();
						if (v != null) {
							y.setParsedValue(v);
						}
						return y;
					}
					case XD_PARSER: {//STRING_PARSER
						String declaredName = readString();
						String name = readString();
						if (declaredName == null && name == null) {
							return new DefNull(XD_PARSER);
						}
						XDContainer pars = (XDContainer) readXD();
						XDParser y = getParser(name);
						try {
							y.setNamedParams(null, pars);
						} catch (SException ex) {
							throw new SIOException(ex.getReport());
						}
						y.setDeclaredName(declaredName);
						return y;
					}
					case XD_REGEX:return new XDRegex(readString(), readBoolean());
					case XD_STRING: return new DefString(readString());
					case XD_XQUERY: return new DefXQueryExpr(readString());
					case XD_XPATH: return readXPath();
					case X_UNIQUESET:
					case X_UNIQUESET_M: {
						int len = readLength();
						ParseItem[] keys = new ParseItem[len];
						if (len > 0) {
							for (int i = 0; i < len; i++) {
								keys[i] = new ParseItem(readString(), //key name
									readString(), // reference type name
									readInt(), // address of validation method
									readInt(), // key index
									readShort(), // parsed type
									readBoolean()); // optional flag
							}
						}
						len = readLength();
						String[] varNames = new String[len];
						if (len > 0) {
							for (int i = 0; i < len; i++) {
								varNames[i] = readString();
							}
						}
						return new CodeUniqueset(keys, varNames, readString());
					}
					case XD_SERVICE: return new DefSQLService();
					case XD_STATEMENT: return new DefSQLStatement();
					case XD_RESULTSET: return new DefSQLResultSet();
					case XD_UNIQUESET_KEY:
					case X_PARSEITEM: // TODO ???
					case XD_NULL: return new DefNull(type);
					default:
						throw new SIOException(SYS.SYS066, "Illegal type: "+type); //Internal error&{0}{: }
				}
			}
			default: {
				// 01ILSXMPVWT
				byte c = readByte();
				switch (c) {
					case ID_CODEOP: return new CodeOp(type, code);
					case ID_CODEI1: return new CodeI1(type, code, readInt());
					case ID_CODEI2: return new CodeI2(type, code, readInt(), readInt());
					case ID_CODEL2: return new CodeL2(type, code, readInt(), readLong());
					case ID_CODES1: return new CodeS1(type, code, readInt(), readString());
					case ID_CODEXD: return new CodeXD(type, code, readInt(), readXD());
					case ID_CODEEXT: {
						int p1 = readInt();
						String name = readString();
						String methodName = readString();
						String className = readString();
						Class<?> declaringClass = getClassForName(className);
						int len = readLength();
						Class<?>[] pars = new Class<?>[len];
						for (int i = 0; i < len; i++) {
							pars[i] = getClassForName(readString());
						}
						try {
							Method method = declaringClass.getMethod(methodName, pars);
							return new CodeExtMethod(name, type, code, p1, method);
						} catch (NoSuchMethodException ex) {
							//Internal error&{0}{: }
							throw new SIOException(SYS.SYS066, "No such method: "+name+"/"+methodName);
						}
					}
					case ID_CODEPARSER: {
						int p1 = readInt();
						String name = readString();
						int len = readInt();
						String [] sqParamNames;
						if (len < 0) {
							sqParamNames = null;
						} else {
							sqParamNames = new String[len];
							for (int i = 0; i < len; i++) {
								sqParamNames[i] = readString();
							}
						}
						return new CodeParser(type, code, p1, name, sqParamNames);
					}
					case ID_CODESLIST: {
						int p1 = readInt();
						String[] pars = new String[p1];
						for (int i = 0; i < p1; i++) {
							pars[i] = readString();
						}
						return new CodeStringList(type, code, pars);
					}
					case ID_CODESWTABI: {
						CodeSWTableInt y = new CodeSWTableInt();
						int p1 = readInt();
						y.setParam(p1);
						int len = readLength();
						y._adrs = new int[len];
						y._list = new long[len];
						for (int i = 0; i < len; i++) {
							y._adrs[i] = readInt();
							y._list[i] = readLong();
						}
						return y;
					}
					case ID_CODESWTABS: {
						CodeSWTableStr y = new CodeSWTableStr();
						int p1 = readInt();
						y.setParam(p1);
						int len = readLength();
						y._adrs = new int[len];
						y._list = new String[len];
						for (int i = 0; i < len; i++) {
							y._adrs[i] = readInt();
							y._list[i] = readString();
						}
						return y;
					}
					default:
						throw new SIOException(SYS.SYS066, "Illegal ID:"+type+"/"+c);//Internal error&{0}{: }
				}
			}
		}
	}
}
