package test.common.bnf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.xdef.sys.SUtils;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExprCompiler {

    public TestExprCompiler() {super();}

    /* Value types. */
    static final int TYPE_VOID = 0;
    static final int TYPE_BOOLEAN = 1;
    static final int TYPE_INT = 2;
    static final int TYPE_FLOAT = 3;
    static final int TYPE_STRING = 4;
    static final int TYPE_OBJECT = 5;

    static final byte INTCONST_OP = 1;
    static final byte FLTCONST_OP = INTCONST_OP + 1;
    static final byte BOOLCONST_OP = FLTCONST_OP + 1;
    static final byte STRCONST_OP = BOOLCONST_OP + 1;
    static final byte NULLCONST_OP = STRCONST_OP + 1;
    static final byte NAME_OP = NULLCONST_OP + 1;
    static final byte TYPE_OP = NAME_OP + 1;
    static final byte MINUS_OP = TYPE_OP + 1;
    static final byte NOT_OP = MINUS_OP + 1;
    static final byte NEG_OP = NOT_OP + 1;
    static final byte IDREF_OP = NEG_OP + 1;
    static final byte AND_OP = IDREF_OP + 1;
    static final byte OR_OP = AND_OP + 1;
    static final byte XOR_OP = OR_OP + 1;
    static final byte LSH_OP = XOR_OP + 1;
    static final byte RSH_OP = LSH_OP + 1;
    static final byte RRSH_OP = RSH_OP + 1;
    static final byte ADD_OP = RRSH_OP + 1;
    static final byte SUB_OP = ADD_OP + 1;
    static final byte MUL_OP = SUB_OP + 1;
    static final byte DIV_OP = MUL_OP + 1;
    static final byte MOD_OP = DIV_OP + 1;
    static final byte GT_OP = MOD_OP + 1;
    static final byte LT_OP = GT_OP + 1;
    static final byte GE_OP = LT_OP + 1;
    static final byte LE_OP = GE_OP + 1;
    static final byte EQ_OP = LE_OP + 1;
    static final byte NE_OP = EQ_OP + 1;
    static final byte INCBEFORE_OP = NE_OP + 1;
    static final byte DECBEFORE_OP = INCBEFORE_OP + 1;
    static final byte INCAFTER_OP = DECBEFORE_OP + 1;
    static final byte DECAFTER_OP = INCAFTER_OP + 1;
    static final byte ASS_OP = DECAFTER_OP + 1;
    static final byte ASSADD_OP = ASS_OP + 1;
    static final byte ASSSUB_OP = ASSADD_OP + 1;
    static final byte ASSMUL_OP = ASSSUB_OP + 1;
    static final byte ASSDIV_OP = ASSMUL_OP + 1;
    static final byte ASSMOD_OP = ASSDIV_OP + 1;
    static final byte ASSAND_OP = ASSMOD_OP + 1;
    static final byte ASSOR_OP = ASSAND_OP + 1;
    static final byte ASSXOR_OP = ASSOR_OP + 1;
    static final byte ASSLSH_OP = ASSXOR_OP + 1;
    static final byte ASSRSH_OP = ASSLSH_OP + 1;
    static final byte ASSRRSH_OP = ASSRSH_OP + 1;
    static final byte PARAMLIST_OP = ASSRRSH_OP + 1;
    static final byte PARAM_OP = PARAMLIST_OP + 1;
    static final byte METHOD_OP = PARAM_OP + 1;
    static final byte FUNCTION_OP = METHOD_OP + 1;
    static final byte COMMAND_OP = FUNCTION_OP + 1;

    static final Map<String, Byte> _codes = new HashMap<>();

    static {
        _codes.put("intConst", INTCONST_OP);
        _codes.put("fltConst", FLTCONST_OP);
        _codes.put("boolConst", BOOLCONST_OP);
        _codes.put("strConst", STRCONST_OP);
        _codes.put("nullConst", NULLCONST_OP);
        _codes.put("name", NAME_OP);
        //TODO
    }

    public final static class CodeItem {
        final String _op;
        final Object _value;
        CodeItem(final String name, final Object value) {
            _op = name; _value = value;
        }

        @Override
        public String toString() {return _op + ' ' + _value;}
    }

    private static void compileIf(final int i, final Object[] code, CodeItem[] result, final String[] ii) {
        result[i] = new CodeItem("nop", 1000 + i);
        int x = -1;
        for (int j = i + 1; j < code.length; j++) {
            if (result[j] != null) continue;
            String[] s = ((String) code[j]).split(" ");
            if ("if".equals(s[0])) {
                if (s[1].equals(ii[1])) {
                    result[j] = new CodeItem("nop", 9000 + j);
                    x = j;
                } else {
                    compileIf(j, code, result, s);
                }
                continue;
            }
            if ("else".equals(s[0])) { //else
                result[x] = new CodeItem("jmpf", j);
                for (int k = j + 1; k < code.length; k++) {
                    if (result[k] != null) continue;
                    String[] kk = ((String) code[k]).split(" ");
                    if ("endIf".equals(kk[0]) && kk[1].equals(s[2])) {
                        result[j] = new CodeItem("jmp", k + 1);
                        result[k] = new CodeItem("nop", 9000 + i);
                        return;
                    }
                }
                throw new RuntimeException("endIf missing after else");
            }
            if ("endIf".equals(s[0])) {
                result[x] = new CodeItem("jmpf", j);
                result[j] = new CodeItem("nop", 9000 + i);
                return;
            }
        }
        throw new RuntimeException("endIf missing, i=" + i);
    }

    public static CodeItem[] precompile(final String source, final Object[] code) {
        CodeItem[] result = new CodeItem[code.length];
        for (int i = 0; i < code.length; i++) {
            String item = code[i].toString();
            if (item.startsWith("info: ")) { // parsed position
                result[i] = new CodeItem("info", item.substring(6).trim());
            } else if (item.startsWith("type ")) { // parsed position
                result[i] = new CodeItem("nop", 9999);
            }
            if (result[i] != null) {
                continue;
            }
            String[] ii;
            try {
                ii = item.split(" ");
            } catch (Exception ex) {
                throw new RuntimeException("i = " + i);
            }
            item = ii[0];
            switch (item) {
                case "intConst":
                    result[i] = new CodeItem(item,
                        Long.valueOf(source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    break;
                case "fltConst":
                    result[i] = new CodeItem(item,
                        Double.valueOf(source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    break;
                case "boolConst":
                    result[i] = new CodeItem(item, "true".equals(
                        source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    break;
                case "strConst": {
                    String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                    String delimiter = String.valueOf(s.charAt(0));
                    s = s.substring(1, s.length() - 1);
                    s = SUtils.modifyString(s, delimiter + delimiter, delimiter);
                    result[i] = new CodeItem(item, s);
                    break;
                }
                case "nullConst": result[i] = new CodeItem(item, null); break;
                case "name":
                    result[i] = new CodeItem(item, source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2])));
                    break;
                case "jmp":
                case "jmpf":
                case "jmpt": result[i] = new CodeItem(item, Integer.valueOf(ii[1])); break;
                case "jmptf": {
                    result[i] = new CodeItem(item, new int[] {Integer.parseInt(ii[1]), Integer.parseInt(ii[2])});
                    break;
                }
                case "if": compileIf(i, code, result, ii); break;
                case "while": 
                    code[i] = "nop 0 0";
                    result[i] = new CodeItem("nop", null);
                    for (int j = i+1; j < code.length; j++) {
                        String[] jj = ((String) code[j]).split(" ");
                        if ("while".equals(jj[0]) && jj[1].equals(ii[1])) {
                            for (int k = j+1; k < code.length; k++) {
                                String[] kk = ((String) code[k]).split(" ");
                                if ("while".equals(kk[0]) && kk[1].equals(jj[2])) {
                                    code[j] = "jmpf " + k;
                                    result[j] = new CodeItem("jmpf", k);
                                    code[k] = "jmp " + i;
                                    result[k] = new CodeItem("jmp", i);
                                }
                            }
                        }
                    }
                    break;
                case "do":
                    code[i] = "nop 0 0";
                    result[i] = new CodeItem("nop", null);
                    for (int j = i+1; j < code.length; j++) {
                        String[] jj = ((String) code[j]).split(" ");
                        if ("do".equals(jj[0]) && jj[1].equals(ii[2])) {
                            code[j] = "jmpt " + i;
                            result[j] = new CodeItem("jmpt", i);
                        }
                    }
                    break;
                case "for":
                    code[i] = "nop 0 0";
                    result[i] = new CodeItem("nop", null);
                    for (int j = i+1; j < code.length; j++) {
                        String[] jj = ((String) code[j]).split(" ");
                        if ("for".equals(jj[0]) && jj[1].equals(ii[1])) {
                            for (int k = j+1; k < code.length; k++) {
                                String[] kk = ((String) code[k]).split(" ");
                                if ("for".equals(kk[0]) && kk[1].equals(ii[1])) {
                                    for (int m = k+1; m < code.length; m++) {
                                        String[] mm = ((String) code[m]).split(" ");
                                        if ("for3".equals(mm[0]) && mm[1].equals(kk[2])) {
                                            code[j] = "jmptf " + k + " " + m;
                                            result[j] = new CodeItem("jmpf", new int[]{k,m});
                                            code[k] = "jmp " + i;
                                            result[k] = new CodeItem("jmp", i);
                                            code[m] = "jmp " + j;
                                            result[m] = new CodeItem("jmp", j);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    try {
                        result[i] = new CodeItem(item,
                            item.endsWith("type")? source.substring(Integer.parseInt(ii[1]),Integer.parseInt(ii[2])): null);
                    } catch (Exception ex) {
                        throw new RuntimeException("i = " + i + "; " + code[i]);
                    }
            }
/***************************************************************
            } else if ("jmp".equals(item) || "jmpf".equals(item) || "jmpt".equals(item) ) {
                result[i] = new CodeItem(item, new Integer(ii[1]));
            } else if ("jmptf".equals(item)) {
                result[i] = new CodeItem(item, new int[] {new Integer(ii[1]), new Integer(ii[2])});
            } else if ("if".equals(item)) {
                for (int j = i+1; j < code.length; j++) {
                    String[] jj = ((String) code[j]).split(" ");
                    if ("then".equals(jj[0]) && jj[1].equals(ii[2])) {
                        result[i] = new CodeItem("jmpf", j);
                        code[i] = "jmpf " + j;
                        code[j] = "nop 0 0";
                        for (int k = j + 1; k < code.length; k++) {
                            String[] kk = ((String) code[k]).split(" ");
                            if ("else".equals(kk[0]) && kk[1].equals(jj[2])) {
                                result[j] = new CodeItem("jmp", k);
                                code[j] = "jmp " + k;
                                code[k] = "nop 0 0";
                            }
                        }
                    }
                }
            } else if ("while".equals(item)) {
                code[i] = "nop 0 0";
                result[i] = new CodeItem("nop", null);
                for (int j = i+1; j < code.length; j++) {
                    String[] jj = ((String) code[j]).split(" ");
                    if ("while".equals(jj[0]) && jj[1].equals(ii[1])) {
                        for (int k = j+1; k < code.length; k++) {
                            String[] kk = ((String) code[k]).split(" ");
                            if ("while".equals(kk[0]) && kk[1].equals(jj[2])) {
                                code[j] = "jmpf " + k;
                                result[j] = new CodeItem("jmpf", k);
                                code[k] = "jmp " + i;
                                result[k] = new CodeItem("jmp", i);
                            }
                        }
                    }
                }
            } else if ("do".equals(item)) {
                code[i] = "nop 0 0";
                result[i] = new CodeItem("nop", null);
                for (int j = i+1; j < code.length; j++) {
                    String[] jj = ((String) code[j]).split(" ");
                    if ("do".equals(jj[0]) && jj[1].equals(ii[2])) {
                        code[j] = "jmpt " + i;
                        result[j] = new CodeItem("jmpt", i);
                    }
                }
            } else if ("for".equals(item)) {
                code[i] = "nop 0 0";
                result[i] = new CodeItem("nop", null);
                for (int j = i+1; j < code.length; j++) {
                    String[] jj = ((String) code[j]).split(" ");
                    if ("for".equals(jj[0]) && jj[1].equals(ii[1])) {
                        for (int k = j+1; k < code.length; k++) {
                            String[] kk = ((String) code[k]).split(" ");
                            if ("for".equals(kk[0]) && kk[1].equals(ii[1])) {
                                for (int m = k+1; m < code.length; m++) {
                                    String[] mm = ((String) code[m]).split(" ");
                                    if ("for3".equals(mm[0]) && mm[1].equals(kk[2])) {
                                        code[j] = "jmptf " + k + " " + m;
                                        result[j] = new CodeItem("jmpf", new int[]{k,m});
                                        code[k] = "jmp " + i;
                                        result[k] = new CodeItem("jmp", i);
                                        code[m] = "jmp " + j;
                                        result[m] = new CodeItem("jmp", j);
                                    }
                                }
                            }
                        }
                    }
                }
***************************************************************/
        }
        return result;
    }

    public static String printCode(final String source, final Object[] code, final CodeItem[] pc) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < code.length; j++) {
            String t = "";
            String[] x =((String) code[j]).split(" ");
            if (x.length == 3) {
                int k = -1, l = -1;
                try {
                    k = Integer.parseInt(x[1]);
                    l = Integer.parseInt(x[2]);
                    t = source.substring(k, l);
                } catch (Exception ex) {
                    t = "";
                }
            }
            String v = "";
            if (((String) code[j]).startsWith("command")) {
                 v = "; " + t;
            }
            String s = String.format("%3d: ", j) + (pc == null ? "" : pc[j]) + "\t";
            if (s.length() < 9) {
                s += "\t\t\t";
            } else if (s.length() < 17) {
                s += "\t\t";
            } else if (s.length() < 25) {
                s += "\t";
            }
            String u = code[j] + v;
            if (!t.isEmpty()) {
                u += "\t";
                if (u.length() < 9) {
                    u += "\t\t\t";
                } else if (u.length() < 17) {
                    u += "\t\t";
                } else if (u.length() < 25) {
                    u += "\t";
                }

            }
            sb.append(s).append(u).append(t).append('\n');            
        }
        return sb.toString();
    }

    /** Execute generated code.
     * @param src source text.
     * @param code generated code.
     * @param vars variable table.
     * @return result of execution (or null).
     */
    static Object execute(final String src, final Object[] code, final Map<String, Object> vars, final PrintStream out){
        final Stack<Object> stack = new Stack<>();
        vars.clear();
        CodeItem[] pc = precompile(src, code);
        for (int i = 0; i < pc.length; i++) {
            CodeItem item = pc[i];
if (item == null) {
    System.out.println("i: " + i);
    System.out.println(item._op); //throws exception
}
            switch (item._op) {
                case "intConst": stack.push((Long) item._value); break;
                case "fltConst": stack.push((Double) item._value); break;
                case "boolConst": stack.push((Boolean) item._value); break;
                case "strConst": stack.push((String) item._value); break;
                case "nullConst": stack.push(null); break;
                case "name": stack.push((String) item._value); break;
                case "type":
                    String s = (String) item._value;
                    s = s.substring(2); // name
                    vars.put(s, null);
                    stack.push(s);
                    break;
                case "MINUS": {
                        Number x = (Number) stack.pop();
                        if (x instanceof Long) {
                            stack.push(-x.longValue());
                        } else {
                            stack.push(-x.doubleValue());
                        }
                        break;
                    }
                case "NOT": stack.push(!((Boolean) stack.pop())); break;
                case "NEG": stack.push(~((Long) stack.pop())); break;
                case "idRef": stack.push(vars.get(stack.pop().toString())); break;
                case "AND":
                case "OR":
                case "XOR": {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Boolean && y instanceof Boolean) { // boolean operation
                            switch (item._op) {
                                case "AND": stack.push((Boolean) x && (Boolean) y); break;
                                case "OR": stack.push((Boolean) x || (Boolean) y); break;
                                default: stack.push((Boolean) x ^ (Boolean) y); // XOR
                            }
                        } else if (x instanceof Long && y instanceof Long) { // bitwise operation
                            switch (item._op) {
                                case "AND": stack.push((Long) x & (Long) y); break;
                                case "OR": stack.push((Long) x | (Long) y); break;
                                default: stack.push((Long) x ^ (Long) y);
                            }
                        } else {
                            throw new RuntimeException("Error: Operand types " + x.getClass() + "," + y.getClass());
                        }
                        break;
                    }
                case "LSH":
                case "RSH":
                case "RRSH": {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = "LSH".equals(item._op) ? xx << yy : "RSH".equals(item._op) ? xx >> yy : xx >>> yy;
                            stack.push(y);
                        } else {
                            throw new RuntimeException("Error: Operand types " + x.getClass() + "," + y.getClass());
                        }
                        break;
                    }
                case "ADD": {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Number && y instanceof Number) {
                            if (x instanceof Long && y instanceof Long) {
                                stack.push(((Number) x).longValue() + ((Number) y).longValue());
                            } else {
                                stack.push(((Number) x).doubleValue() + ((Number) y).doubleValue());
                            }
                        } else {
                            stack.push(x.toString() + y.toString());
                        }
                        break;
                    }
                case "SUB":
                case "MUL":
                case "DIV":
                case "MOD": {
                        Number y = (Number) stack.pop();
                        Number x = (Number) stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                           switch (item._op) {
                                case "SUB": stack.push(x.longValue() - y.longValue()); break;
                                case "MUL": stack.push(x.longValue() * y.longValue()); break;
                                case "DIV": stack.push(x.longValue() / y.longValue()); break;
                                default: stack.push(x.longValue() % y.longValue()); // MOD
                            }
                        } else {
                            switch (item._op) {
                                case "SUB": stack.push(x.doubleValue() - y.doubleValue()); break;
                                case "MUL": stack.push(x.doubleValue() * y.doubleValue()); break;
                                case "DIV": stack.push(x.doubleValue() / y.doubleValue()); break;
                                default: stack.push(x.doubleValue() % y.doubleValue());// MOD
                            }
                        }
                        break;
                    }
                case "GT":
                case "LT":
                case "GE":
                case "LE":
                case "EQ":
                case "NE": {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            stack.push("GT".equals(item._op) ? xx > yy
                                : "LT".equals(item._op) ? xx < yy
                                    : "GE".equals(item._op) ? xx >= yy
                                        : "LE".equals(item._op) ? xx <= yy
                                            : "EQ".equals(item._op) ? xx == yy : xx != yy);
                        } else if (x instanceof Number && y instanceof Number) {
                            double xx = ((Number) x).doubleValue();
                            double yy = ((Number) y).doubleValue();
                            stack.push("GT".equals(item._op) ? xx > yy
                                : "LT".equals(item._op) ? xx < yy
                                    : "GE".equals(item._op) ? xx >= yy
                                        : "LE".equals(item._op) ? xx <= yy
                                            : "EQ".equals(item._op) ? xx == yy : xx != yy);
                        } else if (x instanceof Boolean&& y instanceof Boolean){
                            boolean xx = (Boolean) x;
                            boolean yy = (Boolean) y;
                            switch (item._op) {
                                case "EQ": stack.push(xx == yy); break;
                                case "NE": stack.push(xx != yy); break;
                                default: throw new RuntimeException("Error: Operand types "
                                            + x.getClass() + "," + x.getClass());
                            }
                        }
                        break;
                    }
                case "INCBEFORE":
                case "DECBEFORE":
                case "INCAFTER":
                case "DECAFTER": {
                        String name = stack.pop().toString(); // name of var
                        Object x = vars.get(name);
                        if (x instanceof Long) {
                            if ("INCBEFORE".equals(item._op) || "DECBEFORE".equals(item._op)) {
                                x = (Long) x + ("INCBEFORE".equals(item._op) ? 1 : -1);
                                vars.put(name, x);
                                stack.push(x);
                            } else {
                                stack.push(x);
                                x = (Long) x + ("INCAFTER".equals(item._op) ? 1 : -1);
                                vars.put(name, x);
                            }
                        } else if (x instanceof Double) {
                            if ("INCBEFORE".equals(item._op) || "DECBEFORE".equals(item._op)) {
                                x = (Double) x+("INCBEFORE".equals(item._op) ? 1 : -1);
                                vars.put(name, x);
                                stack.push(x);
                            } else {
                                stack.push(x);
                                x = (Double) x + ("INCAFTER".equals(item._op) ? 1 : -1);
                                vars.put(name, x);
                            }
                        } else {
                            throw new RuntimeException("Error: Operand type " + x);
                        }
                        break;
                    }
                case "ASS": {
                        Object x = stack.pop();
                        vars.put((String) stack.pop(), x);
                        break;
                    }
                case "ASSADD":
                case "ASSSUB":
                case "ASSMUL":
                case "ASSDIV":
                case "ASSMOD": {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Number && y instanceof Number) {
                            boolean bothint = x instanceof Long && y instanceof Long;
                            Number xx = (Number) x;
                            Number yy = (Number) y;
                            if (x instanceof Long && y instanceof Long) {
                                switch (item._op) {
                                    case "ASSADD": y = yy.longValue() + xx.longValue(); break;
                                    case "ASSSUB": y = yy.longValue() - xx.longValue(); break;
                                    case "ASSMUL": y = yy.longValue() * xx.longValue(); break;
                                    case "ASSDIV": y = yy.longValue() / xx.longValue(); break;
                                    default: y = yy.longValue() % xx.longValue(); // ASSMOD
                                }
                            } else {
                                switch (item._op) {
                                    case "ASSADD": y = yy.doubleValue() + xx.doubleValue(); break;
                                    case "ASSSUB": y = yy.doubleValue() - xx.doubleValue(); break;
                                    case "ASSMUL": y = yy.doubleValue() * xx.doubleValue(); break;
                                    case "ASSDIV": y = yy.doubleValue() / xx.doubleValue(); break;
                                    default: y = yy.doubleValue() % xx.doubleValue(); // ASSMOD
                                }
                            }
                        } else if ("ASSADD".equals(item._op) && y instanceof String) {
                            y = y.toString() + x;
                        }
                        vars.put(name, y);
                        break;
                    }
                case "ASSAND":
                case "ASSOR":
                case "ASSXOR": {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Boolean && y instanceof Boolean) {
                            boolean xx = (Boolean) x;
                            boolean yy = (Boolean) y;
                            y = "ASSAND".equals(item._op) ? yy & xx : "ASSOR".equals(item._op) ? yy | xx : yy ^ xx;
                        } else if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = "ASSAND".equals(item._op) ? yy & xx : "ASSOR".equals(item._op) ? yy | xx : yy ^ xx;
                        }
                        vars.put(name, y);
                        break;
                    }
                case "ASSLSH":
                case "ASSRSH":
                case "ASSRRSH": {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = "ASSLSH".equals(item._op) ? yy << xx : "ASSRSH".equals(item._op) ? yy >> xx : yy >>> xx;
                        }
                        vars.put(name, y);
                        break;
                    }
                case "paramList":
                    stack.push(new PredefinedMethod(stack.pop().toString()));
                    break;
                case "param": { // parameter
                        Object x = stack.pop();
                        PredefinedMethod y =  (PredefinedMethod) stack.peek();
                        y.add(x);
                        break;
                    }
                case "method": ((PredefinedMethod) stack.pop()).invokeMethod(out); break;
                case "function": { // procedure or function
                        PredefinedMethod x = (PredefinedMethod) stack.pop();
                        Object y = x.invokeMethod(out);
                        if (y != null) {
                            stack.push(y);
                        } else {
                            throw new RuntimeException("Value of method " + x._name + " expected");
                        }
                        break;
                    }
                case "jmp": i = (Integer) item._value; break;
                case "jmpf":
                    if (!((Boolean)stack.pop())) {
                        i = (Integer) item._value;
                    }
                    break;
                case "jmpt":
                    if (((Boolean)stack.pop())) {
                        i = (Integer) item._value;
                    }
                    break;
                case "jmptf": i = ((int[]) (item._value))[((Boolean) stack.pop()) ? 0 : 1]; break;
                case "command": stack.clear(); break;
                case "nop":
                case "info": break;
                default: throw new RuntimeException("Unknown code at "+i+": " + item);
            }
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    /** Predefined method. */
    private static final class PredefinedMethod extends ArrayList<Object> {
        private final String _name; // name of method

        private PredefinedMethod(final String name) {
            super();
            _name = name;
        }

        private Object invokeMethod(PrintStream out) {
            if (isEmpty()) { // no parameters
                switch (_name) {
                    case "random": return Math.random();
                    case "empty": return "";
                    case "println": out.println(); return null;
                }
            } else { // one or more parameters
                Object o1 = get(0);
                if ("printf".equals(_name)) {
                    remove(0); // we have the first parametr in o1
                    out.printf(o1.toString(), toArray());
                    return null;
                }
                if (size() == 1) { // one parameter
                    switch (_name) {
                        case "println": out.println(o1); return null;
                        case "print": out.print(o1); return null;
                    }
                    if (o1 instanceof Number) {
                        double x = ((Number) o1).doubleValue();
                        switch (_name) {
                            case "abs": return Math.abs(x);
                            case "acos": return Math.acos(x);
                            case "asin": return Math.asin(x);
                            case "atan": return Math.atan(x);
                            case "cbrt": return Math.cbrt(x);
                            case "ceil": return Math.ceil(x);
                            case "cos": return Math.cos(x);
                            case "cosh": return Math.cosh(x);
                            case "exp": return Math.exp(x);
                            case "expm": return Math.expm1(x);
                            case "floor": return Math.floor(x);
                            case "log": return Math.log(x);
                            case "log10": return Math.log10(x);
                            case "log1p": return Math.log1p(x);
                            case "rint": return Math.rint(x);
                            case "round": return Math.round(x);
                            case "signum": return Math.signum(x);
                            case "sin": return Math.sin(x);
                            case "sinh": return Math.sinh(x);
                            case "sqrt": return Math.sqrt(x);
                            case "tan": return Math.tan(x);
                            case "tanh": return Math.tanh(x);
                            case "toDegrees": return Math.toDegrees(x);
                            case "toRadians": return Math.toRadians(x);
                            case "ulp":return Math.ulp(x);
                        }
                    }
                }
                if (size() == 2) { // two parameters
                    Object o2 = get(1);
                    if (o1 instanceof Long && o2 instanceof Long) {
                        switch (_name) {
                            case "min": return Math.min(((Long) o1), ((Long) o2));
                            case "max": return Math.max(((Long) o1), ((Long) o2));
                        }
                    }
                    if (o1 instanceof Number && o2 instanceof Number) {
                        double x = ((Number) o1).doubleValue();
                        double y = ((Number) o2).doubleValue();
                        switch (_name) {
                            case "atan2": return Math.atan2(x, y);
                            case "hypot": return Math.hypot(x, y);
                            case "min": return Math.min(x, y);
                            case "max": return Math.max(x, y);
                            case "pow": return Math.pow(x, y);
                        }
                    }
                }
            }
            throw new RuntimeException("Unknown method: " + _name);
        }
    }
}
