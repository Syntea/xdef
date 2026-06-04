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

        Stack<SourceItem> stack = new Stack<>();
        Stack<Stack<SourceItem>> stackOfStack = new Stack<>();
        StringBuilder result = new StringBuilder();
        Map<String, SourceItem> variables = new TreeMap<>();
        for (int i = 0; i < code.length; i++) {
            String item = code[i].toString();
            if (item.startsWith("info: ")) { // parsed position
                for (SourceItem x: stack.toArray(new SourceItem[0])) {
                    String s = x._s;
                    if (s.endsWith(" else{") || s.endsWith(" else{")) {
                        s = s.substring(0, s.length() - 6) + " else ";
                    } else if (s.endsWith(" else")) {
                        s = s.substring(0, s.length() - 5) + "; else ";
                    } else if (!s.startsWith("if (") && !s.endsWith("}") && !s.startsWith("do ")
                        && !s.startsWith("switch(") && !s.endsWith(") {") && !s.endsWith(": ")) {
                        s += "; ";
                    }
                    result.append(s);
                }
                stack.clear();
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
                String assOp =  new Operation(item.substring(3)).getOperator().trim() + "=";
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
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                ii = code[++i].toString().split(" ");
                String name = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                variables.put(name, new SourceItem(""));
                stack.push(new SourceItem(s + " " + name));
            } else if ("paramList".equals(item)) { // parameter list
                stackOfStack.push(stack);
                stackOfStack.push(new Stack<>());
                stack = new Stack<>();
            } else if ("param".equals(item)) { // parameter
                stackOfStack.peek().push(stack.pop());
            } else if ("method".equals(item) || "function".equals(item)) {
                Stack<SourceItem> params = stackOfStack.pop();
                stack = stackOfStack.pop();
                StringBuilder s = new StringBuilder(stack.pop()._s + '('); // "name("
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
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
            } else if ("fltConst".equals(item)) {
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
            } else if ("boolConst".equals(item)) {
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
            } else if ("strConst".equals(item)) {
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
            } else if ("nullConst".equals(item)) {
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
            } else if ("name".equals(item)) {
                String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                stack.push(new SourceItem(s));
////////////////////////////////////////////////////////////////////////////////
            } else if ("idRef".equals(item)) { // reference to variable name
            } else if ("command".equals(item)) {  // clear stack
                if (!stack.empty()) {
                    result.append(stack.pop()._s).append(";\n");
                    if (!stack.empty()) {
                        result.append(Report.error("", "Stack item: " + stack.pop()).toString());
                    }
                }
            } else if ("blokStart".equals(item)) {
                stack.peek()._s += "{";
            } else if ("blok".equals(item)) {
                String s = stack.peek()._s;
                if (!s.isEmpty() && !s.endsWith("}") && !s.endsWith(";") && !s.endsWith(") ")) {
                    s += ";";
                }
                stack.peek()._s = s + "}";
            } else if ("if".equals(item)) {
            } else if ("if1".equals(item)) {
                stack.peek()._s = "if (" + stack.peek()._s + ") ";
            } else if ("else".equals(item)) {
                stack.peek()._s += " else";
            } else if ("continue".equals(item)) {
                String s = stack.peek()._s;
                if (!s.isEmpty() && !s.endsWith("}") && !s.endsWith(";") && !s.endsWith(") ")) {
                    s += ";";
                }
                stack.peek()._s = s + " continue;";
            } else if ("break".equals(item)) {
                String s = stack.peek()._s;
                if (!s.isEmpty() && !s.endsWith("}") && !s.endsWith(";") && !s.endsWith(") ")) {
                    s += ";";
                }
                stack.peek()._s = s + " break; ";
            } else if ("endIf".equals(item)) {
            } else if ("do".equals(item)) {  //???
                stack.push(new SourceItem("do "));
            } else if ("doExpr".equals(item)) {  //???
                String s = stack.peek()._s;
                if (!s.endsWith("}")) {
                    s += ";";
                }
                stack.peek()._s = s + " while (";
            } else if ("endDo".equals(item)) {  //???
                stack.peek()._s += ")";
            } else if ("switch".equals(item)) {  //???
                stack.push(new SourceItem("switch("));
            } else if ("switchBody".equals(item)) {  //???
                stack.peek()._s += ") {";
            } else if ("case".equals(item)) {  //???
                stack.peek()._s = "case " + stack.peek()._s  + ": ";
            } else if ("default".equals(item)) {  //???
                stack.peek()._s += "dafault: ";
            } else if ("endSwitch".equals(item)) {  //???
                String s = stack.peek()._s;
                if (!s.isEmpty() && !s.endsWith("}") && !s.endsWith(";")) {
                    s += ";";
                }
                stack.peek()._s =  s + "}";
            } else if ("nop".equals(item)) {  //???
            } else if ("jmptf".equals(item)) {  //???
            } else if ("jmp".equals(item)) {  //???
            } else {
                result.append(Report.error("", "Unknown code: " + item).toString());
                return result.toString();
            }
        }
        if (!stack.isEmpty()) {
            SourceItem[] items = new SourceItem[stack.size()];
            stack.toArray(items);
            for (int i = 0; i < items.length - 1; i++) {
                result.append(items[i]._s);
            }
            String s = items[items.length - 1]._s;
            if (!s.endsWith("}")) {
                s += ';';
            }
            result.append(s);
        }
        return result.toString().trim();
    }

    /** Contains information about operator. */
    private static final class Operation {
        private final int _level; // Level of operator
        private final String value; //String value of operator

        /** Create this object from code item. */
        private Operation(final String item) {
            char ch;
            int ndx = item.indexOf(' ');
            String code = ndx > 0 ? item.substring(0, ndx) : item;
            if (code.startsWith("ASS")) {
                Operation op = new Operation(code.substring(3));
                value = "=" + op.value;
                _level = 7;
            } else if ("MINUS".equals(code) || "NOT".equals(code) || "NEG".equals(code)){
                _level = 6; // unary oparator
                ch = code.charAt(1);
                value = ch == 'I' ? "-" : ch == 'O' ? "!" : "~";
            } else if ("LT".equals(code) || "GT".equals(code) || "LE".equals(code) || "GE".equals(code)
                || "EQ".equals(code) || "NE".equals(code)) {
                _level = 1; // comparing;
                ch = code.charAt(0);
                value = ch == 'E' ? "=="
                    : ch == 'N' ? "!="
                    : ch == 'L' ? code.charAt(1) == 'T' ? "<" : "<="
                    : code.charAt(1) == 'T' ? ">" : ">=";
            } else if ("AND".equals(code) || "OR".equals(code) || "XOR".equals(code)) {
                ch = code.charAt(0);
                _level = 2;
                value = ch == 'A' ? " & " : ch == 'O' ? " | " : " ^ ";
            } else if (code.endsWith("SH")) { // shifts
                _level = 3;
                ch = code.charAt(0);
                value = ch == 'L' ? "<<" : code.charAt(1) == 'S' ? ">>" : ">>>";
            } else if ("ADD".equals(code) || "SUB".equals(code)) {
                _level = 4;
                ch = code.charAt(0);
                value = ch == 'A' ? "+" : "-";
            } else if ("MUL".equals(code) || "DIV".equals(code) || "MOD".equals(code)) {
                _level = 5;
                ch = code.charAt(1);
                value = ch == 'U' ? "*" : ch == 'I' ? "/" : "%";
            } else {
                _level = Integer.MAX_VALUE;
                value = "";
            }
        }
        private int getLevel() {return _level;}
        private String getOperator() {return value;}
    }
}
