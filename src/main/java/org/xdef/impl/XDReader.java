package org.xdef.impl;

import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefSQLService;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefSQLResultSet;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefSQLStatement;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefObject;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXQueryExpr;
import org.xdef.impl.code.DefRegex;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SException;
import org.xdef.sys.SIOException;
import org.xdef.sys.SObjectReader;
import org.xdef.impl.xml.KNamespace;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
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
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.code.CodeTable;
import org.xdef.impl.code.CodeXD;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.impl.code.DefLocale;

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
		//first check primitive type names
		if ("boolean".equals(name)) {
			return java.lang.Boolean.TYPE;
		} else if ("byte".equals(name)) {
			return java.lang.Byte.TYPE;
		} else if ("short".equals(name)) {
			return java.lang.Short.TYPE;
		} else if ("int".equals(name)) {
			return java.lang.Integer.TYPE;
		} else if ("long".equals(name)) {
			return java.lang.Long.TYPE;
		} else if ("char".equals(name)) {
			return java.lang.Character.TYPE;
		} else if ("float".equals(name)) {
			return java.lang.Float.TYPE;
		} else if ("double".equals(name)) {
			return java.lang.Double.TYPE;
		}
		try {
			//return class
			return Class.forName(name, false,
				Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException ex) {
			//Internal error&{0}{: }
			throw new SIOException(SYS.SYS066,"Class not found: "+name+"; "+ex);
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
		KNamespace nc;
		if (len == 0) {
			nc = null;
		} else {
			nc = new KNamespace();
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
	final XDValue readXD() throws IOException {
		short code = readShort();
		if (code < 0) {
			if (code == -1) {
				return null;
			}
			//Internal error&{}{: }
			throw new SIOException(SYS.SYS066, "Illegal code: " + code);
		}
		short type = readShort();
		switch(code) {
			case CodeTable.COMPILE_BNF:
				return readBNF();
			case CodeTable.COMPILE_XPATH:
				return readXPath();
			case CodeTable.LD_CONST: {
				switch (type) {
					case XDValueID.XD_BNFGRAMMAR:
						return readBNF();
					case XDValueID.XD_BNFRULE:
//						return new DefBNFRule(null, readString());
						return new DefBNFRule(null);
					case XDValueID.XD_BOOLEAN:
						return new DefBoolean(readBoolean());
					case XDValueID.XD_BYTES:
						return new DefBytes(readBytes());
					case XDValueID.XD_DATETIME: {
						SDatetime x = readSDatetime();
						return x == null ? new DefDate() : new DefDate(x);
					}
					case XDValueID.XD_DECIMAL:
						return new DefDecimal(readBigDecimal());
					case XDValueID.XD_DURATION: {
						SDuration x = readSDuration();
						return x==null ? new DefDuration() : new DefDuration(x);
					}
					case XDValueID.XD_ELEMENT:
						return new DefElement();
					case XDValueID.XD_EXCEPTION:
						return new DefException(readReport(),
							readString(), readInt());
					case XDValueID.XD_FLOAT:
						return new DefDouble(readDouble());
					case XDValueID.XD_INT:
						return new DefLong(readLong());
					case XDValueID.XD_CONTAINER: {
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
					case XDValueID.XD_LOCALE: {
						return new DefLocale(
							readString(), readString(), readString());
					}
					case XDValueID.XD_NAMEDVALUE:
						return new DefNamedValue(readString(), readXD());
					case XDValueID.XD_OBJECT:
						return new DefObject();
					case XDValueID.XD_OUTPUT:
						return new DefOutStream();
					case XDValueID.XD_PARSERESULT: {
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
					case XDValueID.XD_PARSER: {//STRING_PARSER
						String declaredName = readString();
						String name = readString();
						if (declaredName == null && name == null) {
							return new DefNull(XDValueID.XD_PARSER);
						}
						XDContainer pars = (XDContainer) readXD();
						XDParser y = CompileBase.getParser(name);
						try {
							y.setNamedParams(null, pars);
						} catch (SException ex) {
							throw new SIOException(ex.getReport());
						}
						y.setDeclaredName(declaredName);
						return y;
					}
					case XDValueID.XD_REGEX:
						return new DefRegex(readString());
					case XDValueID.XD_STRING:
						return new DefString(readString());
					case XDValueID.XD_XQUERY: {
						return new DefXQueryExpr(readString());
					}
					case XDValueID.XD_XPATH:
						return readXPath();
					case CompileBase.UNIQUESET_VALUE:
					case CompileBase.UNIQUESET_M_VALUE: {
						int len = readLength();
						CodeUniqueset.ParseItem[] keys =
							new CodeUniqueset.ParseItem[len];
						if (len > 0) {
							for (int i = 0; i < len; i++) {
								keys[i] = new CodeUniqueset.ParseItem(
									readString(), //key name
									readString(), // reference type name
									readInt(), // address of validation method
									i, // item index
									readShort(), // parsed type
									readBoolean()); // optional flag
							}
						}
						return new CodeUniqueset(keys, readString());
					}
					case XDValueID.XD_SERVICE:
						return new DefSQLService();
					case XDValueID.XD_STATEMENT:
						return new DefSQLStatement();
					case XDValueID.XD_RESULTSET:
						return new DefSQLResultSet();
					case CompileBase.PARSEITEM_VALUE: // TODO ???
					case XDValueID.XD_NULL:
						return new DefNull(type);
					default:
						//Internal error&{0}{: }
						throw new SIOException(SYS.SYS066,
							"Illegal type: "+type);
				}
			}
			default: {
				// 01ILSXMPVWT
				byte c = readByte();
				switch (c) {
					case XDWriter.ID_CODEOP:
						return new CodeOp(type, code);
					case XDWriter.ID_CODEI1:
						return new CodeI1(type, code, readInt());
					case XDWriter.ID_CODEI2:
						return new CodeI2(type, code, readInt(), readInt());
					case XDWriter.ID_CODEL2:
						return new CodeL2(type, code, readInt(), readLong());
					case XDWriter.ID_CODES1:
						return new CodeS1(type, code, readInt(), readString());
					case XDWriter.ID_CODEXD:
						return new CodeXD(type, code, readInt(), readXD());
					case XDWriter.ID_CODEEXT: {
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
							Method method =
								declaringClass.getMethod(methodName, pars);
							return new CodeExtMethod(name,
								type, code, p1, method);
						} catch (NoSuchMethodException ex) {
							//Internal error&{0}{: }
							throw new SIOException(SYS.SYS066,
								"No such method: "+name+"/"+methodName);
						}
					}
					case XDWriter.ID_CODEPARSER: {
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
						return new CodeParser(type,//resultType,
							code, p1, name, sqParamNames);
					}
					case XDWriter.ID_CODESLIST: {
						int p1 = readInt();
						String[] pars = new String[p1];
						for (int i = 0; i < p1; i++) {
							pars[i] = readString();
						}
						return new CodeStringList(type, code, pars);
					}
					case XDWriter.ID_CODESWTABI: {
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
					case XDWriter.ID_CODESWTABS: {
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
						//Internal error&{0}{: }
						throw new SIOException(SYS.SYS066,
							"Illegal ID:"+type+"/"+c);
				}
			}
		}
	}
}