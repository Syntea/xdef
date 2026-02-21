package org.xdef.impl.code;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;
import org.xdef.XDPool;
import org.xdef.XDRegex;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_XPATH;
import static org.xdef.XDValueID.X_ATTR_REF;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_KEY;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.impl.XCodeDescriptor;
import org.xdef.impl.XData;
import org.xdef.impl.XDebugInfo;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XSelector;
import static org.xdef.impl.code.CodeTable.ATTR_EXIST;
import static org.xdef.impl.code.CodeTable.ATTR_REF;
import static org.xdef.impl.code.CodeTable.COMPILE_BNF;
import static org.xdef.impl.code.CodeTable.COMPILE_XPATH;
import static org.xdef.impl.code.CodeTable.EXTMETHOD;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_ARRAY;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_CHECK;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_CHKEL;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_CHKEL_ARRAY;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_CHKEL_XDARRAY;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_SET_ELEMENT;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_TEXT;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_VOID_ELEMENT;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_VOID_TEXT;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_XDARRAY;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_XXNODE;
import static org.xdef.impl.code.CodeTable.EXTMETHOD_XXNODE_XDARRAY;
import static org.xdef.impl.code.CodeTable.INIT_PARAMS_OP;
import static org.xdef.impl.code.CodeTable.LAST_CODE;
import static org.xdef.impl.code.CodeTable.LD_CODE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.LD_GLOBAL;
import static org.xdef.impl.code.CodeTable.LD_LOCAL;
import static org.xdef.impl.code.CodeTable.LD_XMODEL;
import static org.xdef.impl.code.CodeTable.SRCINFO_CODE;
import static org.xdef.impl.code.CodeTable.ST_GLOBAL;
import static org.xdef.impl.code.CodeTable.ST_LOCAL;
import static org.xdef.impl.code.CodeTable.ST_XMODEL;
import static org.xdef.impl.compile.CompileBase.getTypeName;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMStatementInfo;

/** Display compiled objects.
 * @author Vaclav Trojan
 */
public class CodeDisplay implements CodeTable, XDValueID {

	/** Get type abbreviation used in display code.
	 * @param type The type id.
	 * @return The type name or null.
	 */
	public static final String getTypeAbbrev(final short type) {
		switch (type) {
			case X_PARSEITEM: return "(#key item)";
			case X_UNIQUESET:
			case X_UNIQUESET_M: return "(uniqueSet)";
			case X_UNIQUESET_KEY: return "(#uniqueSet key)";
			case X_ATTR_REF: return "(#attribute reference)";
		}
		return "(" + getTypeName(type) + ")";
	}

	/** Create string with printable form of code item.
	 * @param item code item
	 * @return string with printable form of code item.
	 */
	public static final String codeToString(final XDValue item) {
		if (item == null) {
			return "null";
		}
		short code = item.getCode();
		short type = item.getItemId();
		String codeName = getCodeName(code);
		switch (code) {
			case LD_CONST:
				return "CONST" + getTypeAbbrev(type) + " "
					+ (type == XD_STRING ? item instanceof DefString
					? ((DefString) item).sourceValue() : item.toString()
					: type == XD_REGEX ? ((XDRegex) item).sourceValue()
					: type == XD_XPATH ? ((DefXPathExpr) item).sourceValue()
					: item.toString());
			case LD_CODE:
			case LD_LOCAL:
			case LD_GLOBAL:
			case ST_LOCAL:
			case ST_GLOBAL:
			case LD_XMODEL:
			case ST_XMODEL:
				return codeName + getTypeAbbrev(type) +" name="+ item.stringValue() +", "+ item.intValue();
			case INIT_PARAMS_OP: return codeName + " " + item.getParam() + "," + item.intValue();
			case ATTR_EXIST:
			case ATTR_REF: return codeName + " '" + item.stringValue() + "'";
			case COMPILE_XPATH: return codeName + " \"" + item.stringValue() + '"';
			case EXTMETHOD:
			case EXTMETHOD_ARRAY:
			case EXTMETHOD_CHECK:
			case EXTMETHOD_TEXT:
			case EXTMETHOD_SET_ELEMENT:
			case EXTMETHOD_VOID_TEXT:
			case EXTMETHOD_VOID_ELEMENT:
			case EXTMETHOD_CHKEL:
			case EXTMETHOD_XXNODE:
			case EXTMETHOD_CHKEL_ARRAY:
			case EXTMETHOD_CHKEL_XDARRAY :
			case EXTMETHOD_XXNODE_XDARRAY:
			case EXTMETHOD_XDARRAY:
				return codeName +" "+ ((CodeExtMethod) item).getExtMethod().toString() +","+ item.getParam();
			case COMPILE_BNF:
				return (codeName + " source:\n" + item.toString().trim() + "\n=== BNF source end ===");
			case SRCINFO_CODE: return "SRCINFO " + item.stringValue(); //source info
			default:
				if (item instanceof CodeSWTableInt) {
					CodeSWTableInt x = (CodeSWTableInt) item;
					StringBuilder sb = new StringBuilder(codeName);
					sb.append(' ').append(String.valueOf(item.getParam())).append('[');
					for (int i = 0; i < x._list.length; i++) {
						if (i > 0) {
							sb.append(',');
						}
						sb.append(String.valueOf(x._list[i])).append(':').append(String.valueOf(x._adrs[i]));
					}
					return sb.append(']').toString();
				} else if (item instanceof CodeSWTableStr) {
					CodeSWTableStr x = (CodeSWTableStr) item;
					StringBuilder sb = new StringBuilder(codeName);
					sb.append(' ').append(String.valueOf(item.getParam())).append('[');
					for (int i = 0; i < x._list.length; i++) {
						if (i > 0) {
							sb.append(',');
						}
						sb.append(x._list[i]).append(':').append(String.valueOf(x._adrs[i]));
					}
					return sb.append(']').toString();
				} else if (item instanceof CodeStringList) {
					CodeStringList x = (CodeStringList) item;
					StringBuilder sb = new StringBuilder(codeName);
					sb.append(' ').append(String.valueOf(item.getParam())).append('[');
					String[] list = x.getStringList();
					for (int i = 0; i < list.length; i++) {
						if (i > 0) {
							sb.append(',');
						}
						sb.append(list[i]);
					}
					return sb.append(']').toString();
				} else if (item instanceof CodeXD) {
					return ((CodeXD) item).toString();
				} else if (item instanceof CodeS1) {
					return codeName + " " + item.getParam() + ",'" + item.stringValue() + "'";
				} else if (item instanceof CodeL2) {
					return codeName + " " + item.getParam() + "," + item.longValue();
				} else if (item instanceof CodeI2) {
					return codeName + " " + item.getParam() + "," + item.intValue();
				} else if (item instanceof CodeI1) {
					return codeName + " " + item.getParam();
				} else if (item instanceof CodeOp) {
					return codeName.trim();
				} else {
					return codeName + "(" + item.getParam() + ")";
				}
		}
	}

	/** Get string with printable form of option.
	 * @param s String where to add option.
	 * @param name name of option.
	 * @param value byte of option type.
	 * @return string with printable form of option.
	 */
	private static String printOption(final String s, final String name, final byte value) {
		if (value == 0) {
			return s;
		}
		return (s.length() > 0 ? s + "\n": "") + name + "=" + (char) value;
	}

	/** Print code descriptor.
	 * @param sc code descriptor.
	 * @param out where to print.
	 */
	private static void displayDesriptor(final XCodeDescriptor sc, final PrintStream out) {
		out.println(sc.getXDPosition() + ": " + sc.getName() + " " + sc.minOccurs() + ".."
			+ (sc.maxOccurs() == Integer.MAX_VALUE ? "*" : String.valueOf(sc.maxOccurs())));
		if (sc.getKind() == XMELEMENT) {
			if (((XElement)sc)._forget != 0) {
				out.print("forget= " + (char) ((XElement)sc)._forget);
			}
		} else if (sc.getKind() == XMATTRIBUTE ||
			sc.getKind() == XMTEXT) {
			out.print(" (" + ((XMData) sc).getValueTypeName() + ")");
		}
		if (sc._xon > 0) {
			out.print(",xon=" + sc._xon);
		}
		if (sc._check >= 0) {
			out.print(",check=" + sc._check);
		}
		if (sc._deflt >= 0) {
			out.print(",default=" + sc._deflt);
		}
		if (sc._compose >= 0) {
			out.print(",compose=" + sc._compose);
		}
		if (sc._finaly >= 0) {
			out.print(",finally=" + sc._finaly);
		}
		if (sc._varinit >= 0) {
			out.print(",varinit=" + sc._varinit);
		}
		if (sc._init >= 0) {
			out.print(",init=" + sc._init);
		}
		if (sc._onAbsence >= 0) {
			out.print(",onAbsence=" + sc._onAbsence);
		}
		if (sc._onStartElement >= 0) {
			out.print(",onStartElement=" + sc._onStartElement);
		}
		if (sc._onExcess >= 0) {
			out.print(",onExcess=" + sc._onExcess);
		}
		if (sc._onTrue >= 0) {
			out.print(",onTrue=" + sc._onTrue);
		}
		if (sc._onFalse >= 0) {
			out.print(",onFalse=" + sc._onFalse);
		}
		if (sc._onIllegalAttr >= 0) {
			out.print(",onIllegalAttr=" + sc._onIllegalAttr);
		}
		if (sc._onIllegalText >= 0) {
			out.print(",onIllegalText=" + sc._onIllegalText);
		}
		if (sc._onIllegalElement >= 0) {
			out.print(",onIllegalElement=" + sc._onIllegalElement);
		}
		if (sc._match >= 0) {
			out.print(",match=" + sc._match);
		}
		out.println();
		String s = printOption("", "attrWhiteSpaces", sc._attrWhiteSpaces);
		s = printOption(s, "attrWhiteSpaces", sc._attrWhiteSpaces);
		s = printOption(s, "ignoreComments", sc._ignoreComments);
		s = printOption(s, "ignoreEmptyAttributes", sc._ignoreEmptyAttributes);
		s = printOption(s, "textWhiteSpaces", sc._textWhiteSpaces);
		s = printOption(s, "moreAttributes", sc._moreAttributes);
		s = printOption(s, "moreElements", sc._moreElements);
		s = printOption(s, "moreText", sc._moreText);
		s = printOption(s, "attrValuesCase", sc._attrValuesCase);
		s = printOption(s, "textValuesCase", sc._textValuesCase);
		s = printOption(s, "trimAttr", sc._trimAttr);
		s = printOption(s, "acceptQualifiedAttr", sc._acceptQualifiedAttr);
		s = printOption(s, "trimText", sc._trimText);
		if (s.length() > 0) {
			out.println(s);
		}
	}

	/** Print model selector type.
	 * @param xn model selector
	 * @param out where to print.
	 */
	private static void displaySelector(final XNode xn, final PrintStream out) {
		XSelector xsel = (XSelector) xn;
		switch (xsel.getKind()) {
			case XMSEQUENCE: out.print("-- Sequence: "); break;
			case XMMIXED: out.print("-- Mixed:"); break;
			case XMCHOICE: out.print("-- Choice:"); break;
			default: return;
		}
		out.print("min=" + xsel.minOccurs());
		out.print(",max=" + xsel.maxOccurs());
		out.print(",beg=" + xsel.getBegIndex());
		out.print(",end=" + xsel.getEndIndex());
		if (xsel.isSelective()) {
			out.print(",selective");
		}
		if (xsel.isIgnorable()) {
			out.print(",ignorable");
		}
		if (xsel.isEmptyDeclared()) {
			out.print(",emptyDeclared");
		}
		if (xsel.isEmptyFlag()) {
			out.print(",empty");
		}
		if (xsel.getMatchCode() >= 0) {
			out.print(",match=" + xsel.getMatchCode());
		}
		if (xsel.getInitCode() >= 0) {
			out.print(",init=" + xsel.getInitCode());
		}
		if (xsel.getComposeCode() >= 0) {
			out.print(",setSourceMethod=" + xsel.getComposeCode());
		}
		if (xsel.getOnAbsenceCode() >= 0) {
			out.print(",absence=" + xsel.getOnAbsenceCode());
		}
		if (xsel.getOnExcessCode() >= 0) {
			out.print(",excess=" + xsel.getOnAbsenceCode());
		}
		if (xsel.getFinallyCode() >= 0) {
			out.print(",finally=" + xsel.getFinallyCode());
		}
		out.println();
	}

	public static final void displayDefNode(final XNode xn, final PrintStream out,final Set<XNode> processed){
		if (!processed.add(xn)) {
			out.println(" * ref " + xn.getXDPosition());
			return;
		}
		switch (xn.getKind()) {
			case XMATTRIBUTE:
			case XMTEXT: {
				XData xd = (XData)xn;
				out.print("-- XMAttr: ");
				displayDesriptor(xd, out);
				return;
			}
			case XMELEMENT: {
				XElement defEl = (XElement)xn;
				out.print("-- Start XMElement: ");
				displayDesriptor(defEl, out);
				for (XMData x: defEl.getAttrs()) {
					displayDefNode((XData) x, out, processed);
				}
				for (XMNode x: defEl.getChildNodeModels()) {
					displayDefNode((XNode) x, out, processed);
				}
				out.println("-- End XMElement: " + xn.getName());
				return;
			}
			case XMSELECTOR_END: out.println("-- End of selector: "); return;
			case XMSEQUENCE:
			case XMMIXED:
			case XMCHOICE: displaySelector(xn, out); return;
			case XMDEFINITION: {
				XDefinition def = (XDefinition)xn;
				out.print("=== Start XMDefinition: ");
				displayDesriptor(def, out);
				if (!def._rootSelection.isEmpty()) {
					Iterator<String> e=def._rootSelection.keySet().iterator();
					out.println("Root: " + e.next());
					while (e.hasNext()) {
						out.println("    | " + e.next());
					}
				} else {
					out.println("Root: null");
				}
				for (XMElement elem: def.getModels()){
					displayDefNode((XElement) elem, out, processed);
				}
				out.println("=== End XMDefinition: " + def.getName() + "\n");
				return;
			}
			default: out.println("UNKNOWN: " + xn.getName() + "; " + xn.getKind());
		}
	}

	/** Display script code.
	 * @param code array of script code.
	 * @param out PrintStream where pool is printed.
	 */
	public static final void displayCode(final XDValue[] code, final PrintStream out) {
		displayCode(code, out, 0, code != null ? code.length : -1);
	}

	/** Display script code part.
	 * @param code array of script code.
	 * @param out PrintStream where pool is printed.
	 * @param fromAddr from address.
	 * @param toAddr to address.
	 */
	public static final void displayCode(final XDValue[] code,
		final PrintStream out,
		final int fromAddr,
		final int toAddr) {
		int from = fromAddr < 0 ? 0 : fromAddr;
		if (code != null && from < code.length) {
			int to = toAddr > code.length ? code.length : toAddr;
			if (to >= fromAddr) {
				for (int i = from; i < to; i++) {
					out.println(new java.text.DecimalFormat("000000").format(i) +" "+ codeToString(code[i]));
				}
			}
		}
	}

	/** Display debugging information.
	 * @param xp XDPool object.
	 * @param out PrintStream where debug information is printed.
	 */
	public static final void displayDebugInfo(final XDPool xp, final PrintStream out) {
		XDebugInfo di = (XDebugInfo) xp.getDebugInfo();
		if (di == null) {
			out.println("No debug information");
			return;
		}
		XMStatementInfo si;
		int i = 0;
		while ((si = di.getStatementInfo(i++)) == null){}
		while (si != null){
			out.println(si);
			si = di.nextStatementInfo(si);
		}
	}

	/** Get name of code from code number.
	 * @param code code number.
	 * @return name of code from code number.
	 */
	public static final String getCodeName(final short code) {
		if (code < LAST_CODE) {
			final Field[] codetable = CodeTable.class.getDeclaredFields();
			for (Field f : codetable) {
				try {
					if (f.getShort(null) == code) {
						return f.getName();
					}
				} catch (IllegalAccessException | IllegalArgumentException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return "UNDEF_" + code + "_";
	}

	/** Get code number from name of code.
	 * @param codename name of code.
	 * @return code number.
	 */
	public static final short getCodeNumber(final String codename) {
		final Field[] codetable = CodeTable.class.getDeclaredFields();
		for (Field f : codetable) {
			if (codename.equals(f.getName())) {
				try {
					return f.getShort(null);
				} catch (IllegalAccessException | IllegalArgumentException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return -1;
	}
}