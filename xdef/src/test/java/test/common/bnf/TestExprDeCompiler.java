package test.common.bnf;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import org.xdef.sys.Report;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExprDeCompiler {

	/** Create source code from generated code.
	 * @param source source text.
	 * @param code the generated code.
	 * @return String with Java code.
	 */
	public static String toSource(final String source,  final Object[] code) {

		/** Contains information about the stack item. */
		class SourceItem {
			String _s;
			Operation _o; // how this item was created
			SourceItem(final String s, final Operation o){_s = s; _o = o;}
			SourceItem(final String s) {_s = s; _o = new Operation("");}
		}

		Stack<SourceItem> stack = new Stack<SourceItem>();
		Stack<Stack<SourceItem>> stackOfStack = new Stack<Stack<SourceItem>>();
		StringBuilder result = new StringBuilder();
		Map<String, SourceItem> variables = new TreeMap<String, SourceItem>();
		for (int i = 0; i < code.length; i++) {
			String item = code[i].toString();
			if (item.startsWith("info: ")) { // parsed position
				continue;
			}
			String[] ii = ((String) code[i]).split(" ");
			item = ii[0];
///// operators. ///////////////////////////////////////////////////////////////
			Operation op = new Operation(item);
			int level = op.getLevel();
			if (level == 7) { // assignment
				String s = stack.pop()._s;
				String name = stack.pop()._s;
				String assOp =  " " +
					new Operation(item.substring(3)).getOperator().trim() + "= ";
				stack.push(new SourceItem(name + ' ' + assOp + ' ' + s));
			} else if (level == 6) { // unary
				SourceItem x = stack.peek();
				String s = x._s;
				if (x._o.getLevel() <= 6) {
					s = '(' + s + ')';
				}
				x._s = op.getOperator() + s;
				x._o = op;
			} else if (level < 7) {
				SourceItem y = stack.pop();
				SourceItem x = stack.peek();
				if (level > y._o.getLevel()) {
					y._s = '(' + y._s + ')';
				}
				if (level > x._o.getLevel()) {
					x._s = '(' + x._s + ')';
				}
				x._s = x._s + op.getOperator() + y._s;
				x._o = op;
			} else if (item.endsWith("BEFORE")) {
				SourceItem x = stack.peek();
				x._s = (item.startsWith("INC") ? "++" : "--") + x._s;
			} else if (item.endsWith("AFTER")) {
				SourceItem x = stack.peek();
				x._s = x._s + (item.startsWith("INC") ? "++" : "--");
			} else if (item.endsWith("type")) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				if ("boolean".equals(s) || "int".equals(s) || "float".equals(s)
					|| "String".equals(s) || "Object".equals(s)) {
					// type decl
					char ch = item.charAt(0);
					if (i + 1 < code.length
						&& ((String)code[i + 1]).startsWith("name ")) {
						ii = code[++i].toString().split(" ");
						String name = source.substring(Integer.parseInt(ii[1]),
							Integer.parseInt(ii[2]));
						int type = ch == 'B' ? TestExprCompiler.TYPE_BOOLEAN
							: ch == 'I' ? TestExprCompiler.TYPE_INT
							: ch == 'F' ? TestExprCompiler.TYPE_FLOAT
							: ch == 'S' ? TestExprCompiler.TYPE_STRING
							: TestExprCompiler.TYPE_OBJECT;
						SourceItem val = new SourceItem("");
						variables.put(name, val);
						val = new SourceItem(new String[] {
							"","boolean","int","float","String","Object"} [type]
							+ " " + name);
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
					new StringBuilder(stack.pop()._s + '(');
				int len = params.size();
				if (len > 0) {
					s.append(params.get(0)._s); // parameter
					for (int j = 1; j < len; j++) { // more parameters
						s.append(", ").append(params.get(j)._s);
					}
				}
				s.append(')');
				stack.push(new SourceItem(s.toString()));
			} else if ("intConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				stack.push(x);
			} else if ("fltConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				stack.push(x);
			} else if ("boolConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				stack.push(x);
			} else if ("strConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				stack.push(x);
			} else if ("nullConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				SourceItem x = new SourceItem(s);
				stack.push(x);
			} else if ("name".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				stack.push(new SourceItem(s));
////////////////////////////////////////////////////////////////////////////////
			} else if ("idRef".equals(item)) { // reference to variable name
			} else if ("command".equals(item)) {  // clear stack
				result.append(stack.pop()._s).append(";\n");
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

	/** Contains information about operator. */
	private static final class Operation {
		/** Level of operator */
		final int _level;
		/** String value of operator.*/
		final String value;

		/** Create this object from code item. */
		Operation(final String item) {
			char ch;
			int ndx = item.indexOf(' ');
			String code = ndx > 0 ? item.substring(0, ndx) : item;
			if (code.startsWith("ASS")) {
				Operation op = new Operation(code.substring(3));
				value = "=" + op.value;
				_level = 7;
			} else if ("MINUS".equals(code) || "NOT".equals(code)
				|| "NEG".equals(code)){
				_level = 6; // unary oparator
				ch = code.charAt(1);
				value = ch == 'I' ? "-" : ch == 'O' ? "!" : "~";
			} else if ("LT".equals(code) || "GT".equals(code)
				|| "LE".equals(code) || "GE".equals(code)
				|| "EQ".equals(code) || "NE".equals(code)) {
				_level = 1; // comparing;
				ch = code.charAt(0);
				value = ch == 'E' ? "=="
					: ch == 'N' ? "!="
					: ch == 'L' ? code.charAt(1) == 'T' ? "<" : "<="
					: code.charAt(1) == 'T' ? ">" : ">=";
			} else if ("AND".equals(code) || "OR".equals(code)
				|| "XOR".equals(code)) {
				ch = code.charAt(0);
				_level = 2;
				value = ch == 'A' ? " & " : ch == 'O' ? " | " : " ^ ";
			} else if (code.endsWith("SH")) { // shifts
				_level = 3;
				ch = code.charAt(0);
				value = ch == 'L' ? "<<"
					: code.charAt(1) == 'S' ? ">>" : ">>>";
			} else if ("ADD".equals(code) || "SUB".equals(code)) {
				_level = 4;
				ch = code.charAt(0);
				value = ch == 'A' ? "+" : "-";
			} else if ("MUL".equals(code) || "DIV".equals(code)
				|| "MOD".equals(code)) {
				_level = 5;
				ch = code.charAt(1);
				value = ch == 'U' ? "*" : ch == 'I' ? "/" : "%";
			} else {
				_level = Integer.MAX_VALUE;
				value = "";
			}
		}

		final int getLevel() {return _level;}

		final String getOperator() {return value;}
	}
}