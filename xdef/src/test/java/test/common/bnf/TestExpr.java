package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExpr extends STester {

	public TestExpr() {super();}

	/* Value types. */
	private static final int TYPE_VOID = 0;
	private static final int TYPE_BOOLEAN = 1;
	private static final int TYPE_INT = 2;
	private static final int TYPE_FLOAT = 3;
	private static final int TYPE_STRING = 4;
	private static final int TYPE_UNDEF = 5;

	/** Switch to print generated code. */
	private static boolean _displayCode = false;
	/** used to get printed text. */
	private static ByteArrayOutputStream _byteArray;
	/** used for printing. */
	private static PrintStream _out;

	/** Map of variables. */
	private final Map<String, Object> _variables =
		new TreeMap<String, Object>();

	/** Get value of a variable.
	 * @param name Name of variable.
	 * @return value of variable or null.
	 */
	private Object getVar(String name) { return _variables.get(name); }

	/** Parse string with source data according a rule from given BNF grammar,
	 * @param grammar BNF grammar.
	 * @param name name of rule in BNF grammar.
	 * @param source source data,
	 * @return parsed data or error message.
	 */
	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			grammar.setUserObject(this);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			return "Exception " + ex;
		}
	}

	/** Parse source data according the rule "expr" from given BNF grammar.
	 * Compare parsed result with the expected value from argument.
	 * @param expected expected result.
	 * @param grammar BNF grammar.
	 * @param source source data.
	 * @return empty string or error message.
	 */
	private String expr(String expected, BNFGrammar grammar, String source) {
		String s = parse(grammar,"expr", source);
		if (!source.equals(s)) {
			return "Error parsed: " + s;
		}
		Object[] code = grammar.getParsedObjects();
		if (_displayCode) {
			System.out.println(codeToString(code));
		}
		Object result = execute(source, code, _variables);
		if (result == null) {
			return "No result: null";
		}
		if (expected.equals(result.toString())) {
			return "";
		}
		return "Error result: " + result;
	}

	/** Parse source data according the rule "program" from given BNF grammar.
	 * @param grammar BNF grammar.
	 * @param source source data.
	 * @return empty string or error message.
	 */
	private String prog(BNFGrammar grammar, String source) {
		String result = parse(grammar, "program", source);
		if (!source.equals(result)) {
			return result;
		}
		Object[] code = grammar.getParsedObjects();
		if (_displayCode) {
			System.out.println(codeToString(code));
		}
//printObjects(grammar);
//System.out.println("====");
//System.out.println(toSource(code, 0) + "\n====");
		String s = toSource(source, code, 0);
		parse(grammar, "program", s);
		code = grammar.getParsedObjects();
		execute(s, code, _variables);
		try {
			return SUtils.modifyString(_byteArray.toString("UTF-8"),"\r\n","\n");
		} catch (UnsupportedEncodingException ex) {}
		return ""; // never happens
	}

	/** Contains information about operator. */
	private static final class Operator {
		/** Level of operator */
		final int _level;
		/** String value of operator.*/
		final String _operator;

		/** Create this object from code item. */
		Operator(String item) {
			char ch;
			int ndx = item.indexOf(' ');
			String code = ndx > 0 ? item.substring(0, ndx) : item;
			if (code.startsWith("ASS")) {
				Operator op = new Operator(code.substring(3));
				_operator = "=" + op._operator;
				_level = 7;
			} else if ("MINUS".equals(code) || "NOT".equals(code)
				|| "NEG".equals(code)){
				_level = 6; // unary oparator
				ch = code.charAt(1);
				_operator = ch == 'I' ? "-" : ch == 'O' ? "!" : "~";
			} else if ("LT".equals(code) || "GT".equals(code)
				|| "LE".equals(code) || "GE".equals(code)
				|| "EQ".equals(code) || "NE".equals(code)) {
				_level = 1; // comparing;
				ch = code.charAt(0);
				_operator = ch == 'E' ? "=="
					: ch == 'N' ? "!="
					: ch == 'L' ? code.charAt(1) == 'T' ? "<" : "<="
					: code.charAt(1) == 'T' ? ">" : ">=";
			} else if ("AND".equals(code) || "OR".equals(code)
				|| "XOR".equals(code)) {
				ch = code.charAt(0);
				_level = 2;
				_operator = ch == 'A' ? " & " : ch == 'O' ? "|" : "^";
			} else if (code.endsWith("SH")) { // shifts
				_level = 3;
				ch = code.charAt(0);
				_operator = ch == 'L' ? "<<"
					: code.charAt(1) == 'S' ? ">>" : ">>>";
			} else if ("ADD".equals(code) || "SUB".equals(code)) {
				_level = 4;
				ch = code.charAt(0);
				_operator = ch == 'A' ? "+" : "-";
			} else if ("MUL".equals(code) || "DIV".equals(code)
				|| "MOD".equals(code)) {
				_level = 5;
				ch = code.charAt(1);
				_operator = ch == 'U' ? "*" : ch == 'I' ? "/" : "%";
			} else {
				_level = Integer.MAX_VALUE;
				_operator = "";
			}
		}

		private int getLevel() {return _level;}

		private String getOperator() {return _operator;}
	}

	/** Contains information about the stack item. */
	private static class SourceItem {
		String _s;
		Operator _o; // how this item was created
		int _type;
		SourceItem(final String s, final Operator o){_s = s; _o = o;}
		SourceItem(final String s) {_s = s; _o = new Operator("");}
		private int getLevel() {return _o.getLevel();}
		private void setOperator(final Operator o) {_o = o;}
		private String getString() {return _s;}
		private void setString(String s) {_s = s;}
		private int getType() {return _type;}
		private void setType(int type) {_type = type;}
	}

	/** Create source code from generated code.
	 * @param source source text.
	 * @param code the generated code.
	 * @return String with Java code.
	 */
	private static String toSource(final String source,
		final Object[] code,
		final int start) {
		Stack<SourceItem> stack = new Stack<SourceItem>();
		Stack<Stack<SourceItem>> stackOfStack = new Stack<Stack<SourceItem>>();
		StringBuilder result = new StringBuilder();
		Map<String, SourceItem> variables = new TreeMap<String, SourceItem>();
		for (int i = start; i < code.length; i++) {
			String item = code[i].toString();
			if (item.startsWith("info: ")) { // parsed position
				continue;
			}
			String[] ii = ((String) code[i]).split(" ");
			item = ii[0];
///// operators. ///////////////////////////////////////////////////////////////
			Operator op = new Operator(item);
			int level = op.getLevel();
			if (level == 7) { // assignment
				String s = stack.pop().getString();
				String name = stack.pop().getString();
				String assOp =  " " +
					new Operator(item.substring(3)).getOperator().trim() + "= ";
				stack.push(new SourceItem(name + ' ' + assOp + ' ' + s));
			} else if (level == 6) { // unary
				SourceItem x = stack.peek();
				String s = x.getString();
				if (x.getLevel() <= 6) {
					s = '(' + s + ')';
				}
				x.setString(op.getOperator() + s);
				x.setOperator(op);
			} else if (level < 7) {
				SourceItem y = stack.pop();
				SourceItem x = stack.peek();
				if (level > y.getLevel()) {
					y.setString('(' + y.getString() + ')');
				}
				if (level > x.getLevel()) {
					x.setString('(' + x.getString() + ')');
				}
				x.setString(x.getString() + op.getOperator() + y.getString());
				x.setOperator(op);
			} else if (item.endsWith("BEFORE")) {
				SourceItem x = stack.peek();
				x.setString((item.startsWith("INC") ? "++" : "--")
					+ x.getString());
			} else if (item.endsWith("AFTER")) {
				SourceItem x = stack.peek();
				x.setString(x.getString()
					+ (item.startsWith("INC") ? "++" : "--"));
			} else if (item.endsWith("type")) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				if ("boolean".equals(s) || "int".equals(s)
					|| "float".equals(s) || "String".equals(s)) {
					// type decl
					char ch = item.charAt(0);
					if (i + 1 < code.length
						&& ((String)code[i + 1]).startsWith("name ")) {
						ii = code[++i].toString().split(" ");
						String name = source.substring(Integer.parseInt(ii[1]),
							Integer.parseInt(ii[2]));
						int type = ch == 'B' ? TYPE_BOOLEAN
							: ch == 'I' ? TYPE_INT
							: ch == 'F' ? TYPE_FLOAT : TYPE_STRING;
						SourceItem val = new SourceItem("");
						val.setType(type);
						variables.put(name, val);
						val = new SourceItem(new String[] {
							"", "boolean", "int", "float", "String"}[type]
							+ " " + name);
						val.setType(type);
						stack.push(val);
					}
				}
			} else if ("paramList".equals(item)) { // parameter list
				stackOfStack.push(stack);
				stackOfStack.push(new Stack<SourceItem>());
				stack = new Stack<SourceItem>();
			} else if ("param".equals(item)) { // parameter
				stackOfStack.peek().push(stack.pop());
			} else if ("method".equals(item) || "function".equals(item)) {
				Stack<SourceItem> params = stackOfStack.pop();
				stack = stackOfStack.pop();
				StringBuilder s = // "name("
					new StringBuilder(stack.pop().getString() + '(');
				int len = params.size();
				if (len > 0) {
					s.append(params.get(0).getString()); // parameter
					for (int j = 1; j < len; j++) { // more parameters
						s.append(", ").append(params.get(j).getString());
					}
				}
				s.append(')');
				stack.push(new SourceItem(s.toString()));
			} else if ("intConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				x.setType(TYPE_INT);
				stack.push(x);
			} else if ("fltConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				x.setType(TYPE_FLOAT);
				stack.push(x);
			} else if ("boolConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				x.setType(TYPE_BOOLEAN);
				stack.push(x);
			} else if ("strConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				x.setType(TYPE_STRING);
				stack.push(x);
			} else if ("name".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push(new SourceItem(s));
////////////////////////////////////////////////////////////////////////////////
			} else if ("idRef".equals(item)) { // reference to variable name
			} else if ("command".equals(item)) {  // clear stack
				result.append(stack.pop().getString()).append(";\n");
				if (!stack.empty()) {
					result.append(Report.error("",
						"Stack item: " + stack.pop()).toString());
				}
			} else {
				result.append(
					Report.error("", "Unknown code: " + item).toString());
				return result.toString();
			}
		}
		if (!stack.isEmpty()) {
			result.append(stack.pop());
			stack.clear();
		}
		return result.toString().trim();
	}

	/** Execute generated code.
	 * @param source source text.
	 * @param code generated code.
	 * @param variables variable table.
	 * @return result of execution (or null).
	 */
	private static Object execute(final String source,
		final Object[] code,
		final Map<String, Object> variables) {
		final Stack<Object> stack = new Stack<Object>();
		variables.clear();
		try { // prepare printing
			_byteArray = new ByteArrayOutputStream();
			_out = new PrintStream(_byteArray, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {/* never happens */}
		for (int i = 0; i < code.length; i++) {
			String item = code[i].toString();
			if (item.startsWith("info: ")) { // parsed position
				continue;
			}
			String[] ii = ((String) code[i]).split(" ");
			item = ii[0];
			if ("intConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push(Long.parseLong(s));
			} else if ("fltConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push(Double.parseDouble(s));
			} else if ("boolConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push("true".equals(s));
			} else if ("strConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				String delimiter = String.valueOf(s.charAt(0));
				s = s.substring(1, s.length() - 1);
				s = SUtils.modifyString(s, delimiter + delimiter, delimiter);
				stack.push(s);
			} else if ("name".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push(s);
			} else if (item.endsWith("type")) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				if ("boolean".equals(s) || "int".equals(s)
					|| "float".equals(s) || "String".equals(s)) {
					// type decl
					char ch = item.charAt(0);
					if (i + 2 < code.length && "name".equals(code[i + 1])) {
						String name = code[i += 2].toString();
						int type = ch == 'B' ? TYPE_BOOLEAN
							: ch == 'I' ? TYPE_INT
							: ch == 'F' ? TYPE_FLOAT : TYPE_STRING;
						variables.put(name, null);
						stack.push(name);
					}
				}
			} else if ("MINUS".equals(item)) {// unary operator minus
				Number x = (Number) stack.pop();
				if (x instanceof Long) {
					stack.push(-x.longValue());
				} else {
					stack.push(-x.doubleValue());
				}
			} else if ("NOT".equals(item)) { // unary operator boolean !
				stack.push(!((Boolean) stack.pop()));
			} else if ("NEG".equals(item)) { // unary operator bitwise ~
				stack.push( ~ (Long) stack.pop());
			} else if ("idRef".equals(item)) { // reference to object name
				stack.push(variables.get(stack.pop().toString()));
			} else if ("AND".equals(item) || "OR".equals(item)
				|| "XOR".equals(item)) { // operators & | ~
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Boolean &&
					y instanceof Boolean) { // logical operation
					stack.push("AND".equals(item) ? (Boolean) x && (Boolean) y
						: "OR".equals(item) ? (Boolean) x || (Boolean) y
						: (Boolean) x ^ (Boolean) y);
				} else if (x instanceof Long &&
					y instanceof Long) { // bitwise operation
					stack.push("AND".equals(item) ? (Long) x & (Long) y
						: "OR".equals(item) ? (Long) x | (Long) y
						: (Long) x ^ (Long) y);
				} else {
					return Report.error("","Error: Operand types "
						+ x.getClass() + "," + y.getClass());
				}
			} else if ("LSH".equals(item) || "RSH".equals(item)
				|| "RRSH".equals(item)) { // operators << >> >>>
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "LSH".equals(item) ? xx << yy
						: "RSH".equals(item) ? xx >> yy : xx >>> yy;
					stack.push(y);
				} else {
					return Report.error("","Error: Operand types "
						+ x.getClass() + "," + y.getClass());
				}
			} else if ("ADD".equals(item)) { // +
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Number && y instanceof Number) {
					if (x instanceof Long && y instanceof Long){
						stack.push(((Number) x).longValue()
							+ ((Number) y).longValue());
					} else {
						stack.push(((Number) x).doubleValue()
							+ ((Number) y).doubleValue());
					}
				} else {
					stack.push(x.toString() + y.toString());
				}
			} else if ("SUB".equals(item)) { // -
				Number y = (Number) stack.pop();
				Number x = (Number) stack.pop();
				if (x instanceof Long && y instanceof Long) {
					stack.push(x.longValue() - y.longValue());
				} else {
					stack.push(x.doubleValue() - y.doubleValue());
				}
			} else if ("MUL".equals(item)) { // *
				Number y = (Number) stack.pop();
				Number x = (Number) stack.pop();
				if (x instanceof Long && y instanceof Long) {
					stack.push(x.longValue() * y.longValue());
				} else {
					stack.push(x.doubleValue() * y.doubleValue());
				}
			} else if ("DIV".equals(item)) { // /
				Number y = (Number) stack.pop();
				Number x = (Number) stack.pop();
				if (x instanceof Long && y instanceof Long) {
					stack.push(x.longValue() / y.longValue());
				} else {
					stack.push(x.doubleValue() / y.doubleValue());
				}
			} else if ("MOD".equals(item)) { // /
				Number y = (Number) stack.pop();
				Number x = (Number) stack.pop();
				if (x instanceof Long && y instanceof Long) {
					stack.push(x.longValue() % y.longValue());
				} else {
					return Report.error("","Error: Operand types "
						+ x.getClass() + "," + y.getClass());
				}
			} else if ("GT".equals(item) || "LT".equals(item)
				|| "GE".equals(item) || "LE".equals(item)
				|| "EQ".equals(item) || "NE".equals(item) ) {
				Object y = stack.pop();
				Object x = stack.pop();
				Boolean z = null;
				if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					z = "GT".equals(item) ? xx > yy
						: "LT".equals(item) ? xx < yy
						: "GE".equals(item) ? xx >= yy
						: "LE".equals(item) ? xx <= yy
						: "EQ".equals(item) ? xx == yy : xx != yy;
				} else if (x instanceof Number && y instanceof Number) {
					double xx = ((Number) x).doubleValue();
					double yy = ((Number) y).doubleValue();
					z = "GT".equals(item) ? xx > yy
						: "LT".equals(item) ? xx < yy
						: "GE".equals(item) ? xx >= yy
						: "LE".equals(item) ? xx <= yy
						: "EQ".equals(item) ? xx == yy : xx != yy;
				} else if (x instanceof Boolean&& y instanceof Boolean){
					boolean xx = (Boolean) x;
					boolean yy = (Boolean) y;
					z = "EQ".equals(item) ? xx == yy
						: "NE".equals(item) ? xx != yy : null;
				}
				if (z == null) {
					return Report.error("","Error: Operand types "
						+ x.getClass() + "," + x.getClass());
				}
				stack.push(z);
			} else if ("INCBEFORE".equals(item) || "DECBEFORE".equals(item)
				|| "INCAFTER".equals(item) || "DECAFTER".equals(item) ) {
				String name = stack.pop().toString(); // name of var
				Object x = variables.get(name);
				if (x instanceof Long) {
					if ("INCBEFORE".equals(item) || "DECBEFORE".equals(item)){
						x = (Long) x + ("INCBEFORE".equals(item) ? 1 : -1);
						variables.put(name, x);
						stack.push(x);
					} else {
						stack.push(x);
						x = (Long) x + ("INCAFTER".equals(item) ? 1 : -1);
						variables.put(name, x);
					}
				} else if (x instanceof Double) {
					if ("INCBEFORE".equals(item) || "DECBEFORE".equals(item)){
						x = (Double) x + ("INCBEFORE".equals(item) ? 1 : -1);
						variables.put(name, x);
						stack.push(x);
					} else {
						stack.push(x);
						x = (Double) x + ("INCAFTER".equals(item) ? 1 : -1);
						variables.put(name, x);
					}
				} else {
					return Report.error("","Error: Operand type "
						+ x.getClass());
				}
			} else if ("ASS".equals(item)) { // assignment
				Object x = stack.pop();
				variables.put((String) stack.pop(), x);
			} else if ("ASSADD".equals(item) || "ASSSUB".equals(item)
				|| "ASSMUL".equals(item) || "ASSDIV".equals(item)
				|| "ASSMOD".equals(item)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Number && y instanceof Number) {
					boolean bothint = x instanceof Long && y instanceof Long;
					Number xx = (Number) x;
					Number yy = (Number) y;
					if ("ASSADD".equals(item)) {
						if (bothint) {
							y = yy.longValue() + xx.longValue();
						} else {
							y = yy.doubleValue() + xx.doubleValue();
						}
					} else if ("ASSSUB".equals(item)) {
						if (bothint) {
							y = yy.longValue() - xx.longValue();
						} else {
							y = yy.doubleValue() - xx.doubleValue();
						}
					} else if ("ASSMUL".equals(item)) {
						if (bothint) {
							y = yy.longValue() * xx.longValue();
						} else {
							y = yy.doubleValue() * xx.doubleValue();
						}
					} else if ("ASSDIV".equals(item)) {
						if (bothint) {
							y = yy.longValue() / xx.longValue();
						} else {
							y = yy.doubleValue() / xx.doubleValue();
						}
					} else {// ASSMOD
						if (bothint) {
							y = yy.longValue() % xx.longValue();
						} else {
							y = yy.doubleValue() % xx.doubleValue();
						}
					}
				} else if ("ASSADD".equals(item) && y instanceof String) {
					y = y.toString() + x;
				}
				variables.put(name, y);
			} else if ("ASSAND".equals(item) || "ASSOR".equals(item)
				|| "ASSXOR".equals(item)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Boolean && y instanceof Boolean) {
					boolean xx = (Boolean) x;
					boolean yy = (Boolean) y;
					y = "ASSAND".equals(item) ? yy & xx
						: "ASSOR".equals(item) ? yy | xx : yy ^ xx;
				} else if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "ASSAND".equals(item) ? yy & xx
						: "ASSOR".equals(item) ? yy | xx : yy ^ xx;
				}
				variables.put(name, y);
			} else if ("ASSLSH".equals(item) || "ASSRSH".equals(item)
				|| "ASSRRSH".equals(item)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "ASSLSH".equals(item) ? yy << xx
						: "ASSRSH".equals(item) ? yy >> xx : yy >>> xx;
				}
				variables.put(name, y);
			} else if ("paramList".equals(item)) { // parameter list
				stack.push(new PredefinedMethod(stack.pop().toString()));
			} else if ("param".equals(item)) { // parameter
				Object x = stack.pop();
				PredefinedMethod y =  (PredefinedMethod) stack.peek();
				y.add(x);
			} else if ("method".equals(item) || "function".equals(item)) {
				// procedure or function
				PredefinedMethod x = (PredefinedMethod) stack.pop();
				if ("function".equals(item)) {
					Object y = x.invoke();
					if (y != null) {
						stack.push(y);
					} else {
						return Report.error("",
							"Value of method " + x._name + " expected");
					}
				} else {
					x.invoke();
				}
			} else if ("command".equals(item)) {  // clear stack
				stack.clear();
			} else {
				return Report.error("", "Unknown code: " + item);
			}
		}
		return stack.isEmpty() ? null : stack.pop();
	}

	/** Predefined method. */
	private static final class PredefinedMethod extends ArrayList<Object> {
		private final String _name; // name of method

		private PredefinedMethod(final String name) {
			super();
			_name = name.intern();
		}

		private Object invoke() {
			if (isEmpty()) { // no parameters
				if ("random".equals(_name)) {
					return Math.random();
				} else if ("empty".equals(_name)) {
					return "";
				} else if ("println".equals(_name)) {
					_out.println();
					return null;
				}
			} else {
				Object o1 = get(0);
				if (size() == 1) { // one parameter
					if ("println".equals(_name)) {
						_out.println(o1);
						return null;
					} else if ("print".equals(_name)) {
						_out.print(o1);
						return null;
					}
					if (o1 instanceof Number) {
						double x = ((Number) o1).doubleValue();
						if ("abs".equals(_name)) {
							return Math.abs(x);
						} else if ("acos".equals(_name)) {
							return Math.acos(x);
						} else if ("asin".equals(_name)) {
							return Math.asin(x);
						} else if ("atan".equals(_name)) {
							return Math.atan(x);
						} else if ("cbrt".equals(_name)) {
							return Math.cbrt(x);
						} else if ("ceil".equals(_name)) {
							return Math.ceil(x);
						} else if ("cos".equals(_name)) {
							return Math.cos(x);
						} else if ("cosh".equals(_name)) {
							return Math.cosh(x);
						} else if ("exp".equals(_name)) {
							return Math.exp(x);
						} else if ("expm".equals(_name)) {
							return Math.expm1(x);
						} else if ("floor".equals(_name)) {
							return Math.floor(x);
						} else if ("log".equals(_name)) {
							return Math.log(x);
						} else if ("log10".equals(_name)) {
							return Math.log10(x);
						} else if ("log1p".equals(_name)) {
							return Math.log1p(x);
						} else if ("rint".equals(_name)) {
							return Math.rint(x);
						} else if ("round".equals(_name)) {
							return Math.round(x);
						} else if ("signum".equals(_name)) {
							return Math.signum(x);
						} else if ("sin".equals(_name)) {
							return Math.sin(x);
						} else if ("sinh".equals(_name)) {
							return Math.sinh(x);
						} else if ("sqrt".equals(_name)) {
							return Math.sqrt(x);
						} else if ("tan".equals(_name)) {
							return Math.tan(x);
						} else if ("tanh".equals(_name)) {
							return Math.tanh(x);
						} else if ("toDegrees".equals(_name)) {
							return Math.toDegrees(x);
						} else if ("toRadians".equals(_name)) {
							return Math.toRadians(x);
						} else if ("ulp".equals(_name)) {
							return Math.ulp(x);
						}
					}
				}
				if ("printf".equals(_name)) { // one or more parameters
					remove(0); // we have the first parametr in o1
					_out.printf(o1.toString(), toArray());
					return null;
				} else if (size() == 2) {
					Object o2 = get(1);
					if (o1 instanceof Long && o2 instanceof Long) {
						if ("min".equals(_name)) {
							return Math.min(((Long) o1), ((Long) o2));
						} else if ("max".equals(_name)) {
							return Math.max(((Long) o1), ((Long) o2));
						}
					}
					if (o1 instanceof Number && o2 instanceof Number) {
						double x = ((Number) o1).doubleValue();
						double y = ((Number) o2).doubleValue();
						if ("atan2".equals(_name)) {
							return Math.atan2(x, y);
						} else if ("hypot".equals(_name)) {
							return Math.hypot(x, y);
						} else if ("min".equals(_name)) {
							return Math.min(x, y);
						} else if ("max".equals(_name)) {
							return Math.max(x, y);
						} else if ("pow".equals(_name)) {
							return Math.pow(x, y);
						}
					}
				}
			}
			throw new RuntimeException("Unknown method: " + _name);
		}
	}

	/** Get string with the printable form of generated code.
	 * @param code generated stack.
	 * @return string with printable form of generated stack.
	 */
	private static String codeToString(final Object[] code) {
		final DecimalFormat numFormat =	new DecimalFormat("0000 ");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < code.length; i++) {
			sb.append(numFormat.format(i))
				.append("\"").append(code[i].toString()).append("\"\n");
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Run test and print error information. */
	public void test() {
		BNFGrammar g = BNFGrammar.compile(null,
			new File(getDataDir() + "TestExpr.bnf"), null);
		g.setUserObject(this);
		try {
			String s;
			g.trace(null);
			assertEq("", expr("13", g, " /*x*/ 12/*x*//*x*/ + 1 /*x*/ "));
			assertEq("", expr("abcdef", g, "'abc' + 'def'"));
			assertEq("", expr("25abc", g, "((3+2)*5)+'abc'"));
			assertEq("", expr("25abc", g, "( 5 * ( 3 + 2 ) ) + 'abc'"));
			assertEq("", expr("50abc", g, "+( +5 * -(-7 + 2))*2+'abc'"));
			assertEq("", expr("true", g, "true"));
			assertEq("", expr("false", g, "!true"));
			assertEq("", expr("true", g, "true | false"));
			assertEq("", expr("false", g, "true&false"));
			assertEq("", expr("true", g, "true | false"));
			assertEq("", expr("true", g, "! (true & false)"));
			assertEq("", expr("1abc", g, "( (3 + 3)/5 ) + \"abc\" "));
			assertEq("", expr("1.26abc", g, "((3 + 3.3)/5) + 'abc'"));
			assertEq("", expr("2", g, "-1 + 3"));
			assertEq("", expr("1", g, "min(2,1)"));
			assertEq("", expr("1.0", g, "min(2.0,1.0)"));
			assertEq("", expr("1.0", g, "min(2,1.0)"));
			assertEq("", expr("1.0", g, "min(2.0,1)"));
			assertEq("", expr(String.valueOf(Math.sin(3.14)), g, "sin(3.14)"));
			assertEq("", expr(String.valueOf(Math.cos(3.14)), g, "cos(3.14)"));
			assertEq("", prog(g, "float i;"));
			assertEq("", prog(g, "empty();"));

			assertEq("", prog(g, "j = empty() + 'abc';"));
			assertEq("abc", getVar("j"));

			assertEq("", prog(g, "i = ~1;"));
			assertEq(-2, getVar("i"));

			assertEq("", prog(g, "i=~(~1);"));
			assertEq(1, getVar("i"));

			assertEq("", prog(g, "i=8; i=i<< 2;"));
			assertEq(32, getVar("i"));
			assertEq("", prog(g, "j ='abc'+empty()/*x*/; k=j+'d';"));
			assertEq("abc", getVar("j"));
			assertEq("abcd", getVar("k"));

			assertEq("", prog(g, "/*xx*/i=sin(/*xx*/3.14/*xx*/) ;/*xx*/"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "/*xx*/i/*xx*/=/*xx*/3/*xx*/;/*xx*/ "));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i=3;j=i*5;"));
			assertEq(3, getVar("i"));
			assertEq(15, getVar("j"));

			assertEq("", prog(g, "i=''; j=i+(5*3);"));
			assertEq("", getVar("i"));
			assertEq("15", getVar("j"));

			assertEq("", prog(g, "i=\"\";j=i+(5*3);"));
			assertEq("", getVar("i"));
			assertEq("15", getVar("j"));

			assertEq("", prog(g, "i=\"\"\"\"; j=i+(5*3);"));
			assertEq("\"15", getVar("j"));

			assertEq("", prog(g, "i=''''; j=i+(5*3);"));
			assertEq("'15", getVar("j"));

			assertEq("", prog(g, "i = ''''''; j = i + (5 *3);"));
			assertEq("''15", getVar("j"));

			assertEq("", prog(g, "i = 'x''y';"));
			assertEq("x'y", getVar("i"));

			assertEq("", prog(g, "i = '''x'''; j = i + (5 *3);"));
			assertEq("'x'15", getVar("j"));

			assertEq("", prog(g, "i = '''''x'''''; j = i + (5 *3);"));
			assertEq("''x''15", getVar("j"));

			assertEq("", prog(g, "i = '\"x\"'; j = i + (5 *3);"));
			assertEq("\"x\"15", getVar("j"));

			assertEq("", prog(g, "i = 'abc'; j = (5 *3) + i;"));
			assertEq("abc", getVar("i"));
			assertEq("15abc", getVar("j"));

			assertEq("", prog(g, "i = sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "i = min(3.14, sin(3.14));"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("",  prog(g, "i = max(3.14, sin(3.14 + 4));"));
			assertEq(3.14, getVar("i"));

			assertEq("", prog(g, "i = max(3.15, sin(3.14));"));
			assertEq(3.15, getVar("i"));

			assertEq("", prog(g, "i = min(3.15, sin(3.14));"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "i = sin(0);"));
			assertEq(Math.sin(0), getVar("i"));

			assertEq("", prog(g, "i = sin(1.5);"));
			assertEq(Math.sin(1.5), getVar("i"));

			assertEq("", prog(g, "i = tanh(1.5);"));
			assertEq(Math.tanh(1.5), getVar("i"));

			assertEq("", prog(g, "i=sqrt(2);"));
			assertEq(Math.sqrt(2), getVar("i"));

			assertEq("", prog(g, "i = cbrt(2);"));
			assertEq(Math.cbrt(2), getVar("i"));

			assertEq("", prog(g, "i = 3.15 == sin(0);"));
			assertEq(false, getVar("i"));

			assertEq("", prog(g, "i = 8; i = i << 2;"));
			assertEq(32, getVar("i"));

			assertEq("", prog(g, "i = 8; i <<= 2;"));
			assertEq(32, getVar("i"));

			assertEq("", prog(g, "i = 8; i = i >> 2;"));
			assertEq(2, getVar("i"));

			assertEq("", prog(g, "i = 8; i >>= 2;"));
			assertEq(2, getVar("i"));

			assertEq("", prog(g, "i = -8; i = i >> 2;"));
			assertEq(-2, getVar("i"));

			assertEq("", prog(g, "i = -8; i = i >>> 2;"));
			assertEq(-8L >>> 2, getVar("i"));

			assertEq("", prog(g, "i = -8; i >>>= 2;"));
			assertEq(-8L >>> 2, getVar("i"));

			assertEq("", prog(g, "i = 3==3.0;"));
			assertEq(true, getVar("i"));

			assertEq("", prog(g, "i = 3.15!=sin(0);"));
			assertEq(true, getVar("i"));

			assertEq("", prog(g, "i = 1; j = i++;"));
			assertEq(2, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; j = ++i;"));
			assertEq(2, getVar("i"));
			assertEq(2, getVar("j"));

			assertEq("", prog(g, "i = 1; j = i--;"));
			assertEq(0, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; j = i--;"));
			assertEq(0, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 2.1; j = --i;"));
			assertEq(1.1, getVar("i"));
			assertEq(1.1, getVar("j"));

			assertEq("", prog(g, "i = 1; i += 2;"));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i = 1; i -= 2;"));
			assertEq(-1, getVar("i"));

			assertEq("", prog(g, "i = 1; i += 3.14;"));
			assertEq(3.14 + 1, getVar("i"));

			assertEq("", prog(g, "i = 3; j = i % 2;"));
			assertEq(3, getVar("i"));
			assertEq(1, getVar("j"));

			assertEq("", prog(g, "i = 1; i += 2;"));
			assertEq(3, getVar("i"));

			assertEq("", prog(g, "i = 1; j = 1; k = i++; m = ++j;"));
			assertEq(2, getVar("i"));
			assertEq(2, getVar("j"));
			assertEq(1, getVar("k"));
			assertEq(2, getVar("m"));

			assertEq("", prog(g, "i=(3 + 5)*2; j=i+1; k=i/2; l=j/2;m=j/2.0;"));
			assertEq(16, getVar("i"));
			assertEq(17, getVar("j"));
			assertEq(8, getVar("k"));
			assertEq(8, getVar("l"));
			assertEq(8.5, getVar("m"));

			assertEq("", prog(g,
				"i = true; j = false; k = i == j; m = i != j;\n" +
				"o = 1; p = 0; q = o > p; r = 1.0; s = 2.0; t = r <= s;"));
			assertEq(true, getVar("i"));
			assertEq(false, getVar("j"));
			assertEq(false, getVar("k"));
			assertEq(true, getVar("m"));
			assertEq(true, getVar("q"));
			assertEq(true, getVar("t"));

			assertEq("", prog(g, "i=sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			assertEq("", prog(g, "sin(3.14); i=1;"));
			assertEq(1, getVar("i"));

			assertEq("", prog(g, "i=1*2+-(1+1);"));
			assertEq(0, getVar("i"));

			assertEq("", prog(g, "float i; i = 0.0; i += sin(3.14);"));
			assertEq(Math.sin(3.14), getVar("i"));

			prog(g, "print(min(3,14)); println('x');");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("3x\n", s);

			prog(g, "printf('Ahoj'); println();");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("Ahoj\n", s);

			prog(g, "printf('%d, %d, %d\nx\n', 3,4,5);");
			s = SUtils.modifyString(_byteArray.toString("UTF-8"), "\r\n", "\n");
			assertEq("3, 4, 5\nx\n", s);

		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}