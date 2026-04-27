package test.common.bnf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import org.xdef.sys.SUtils;

/** Test of parsing and executions of BNF expressions an assignment commands.
 * @author Vaclav Trojan
 */
public class TestExprCompiler {

    public TestExprCompiler() {super();}

    static final Map<String, Byte> _codes = new HashMap<>();

    /* Code operators. */
    static final byte NOP_OP = 0;
    static final byte INFO_OP = NOP_OP + 1;
    static final byte INTCONST_OP = INFO_OP + 1;
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
    static final byte JMP_OP = NE_OP + 1;
    static final byte JMPF_OP = JMP_OP + 1;
    static final byte JMPT_OP = JMPF_OP + 1;
    static final byte JMPTF_OP = JMPT_OP + 1;
    static final byte INCBEFORE_OP = JMPTF_OP + 1;
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
    static final byte SWITCH_OP = FUNCTION_OP + 1;
    static final byte COMMAND_OP = SWITCH_OP + 1;
    static final byte IF_OP = COMMAND_OP + 1;
    static final byte ELSE_OP = IF_OP + 1;
    static final byte IF1_OP = ELSE_OP + 1;
    static final byte ENDIF_OP = IF1_OP + 1;
    static final byte WILEIF_OP = ENDIF_OP + 1;
    static final byte ENDWILE_OP = WILEIF_OP + 1;
    static final byte ENDDO_OP = ENDWILE_OP + 1;
    static final byte ENDSWITCH_OP = ENDDO_OP + 1;
    static final byte CASE_OP = ENDSWITCH_OP + 1;
    static final byte CASE1_OP = CASE_OP + 1;
    static final byte DEFAULT_OP = CASE1_OP + 1;
    static final byte SWITCHBREAK_OP = DEFAULT_OP + 1;


    /* Predefined functions and methods. */
    static final byte ABS = SWITCHBREAK_OP + 10; //Math.abs(x)
    static final byte ACOS = ABS + 1; // acos(x);
    static final byte ASIN = ACOS + 1; // asin(x);
    static final byte ATAN = ASIN + 1; // atan(x)
    static final byte CBRT = ATAN + 1; // cbrt(x)
    static final byte CEIL = CBRT + 1; // ceil(x)
    static final byte COS = CEIL + 1; // cos(x)
    static final byte COSH = COS + 1; // cosh(x)
    static final byte EXP = COSH + 1; // exp(x)
    static final byte EXPM1 = EXP + 1; // expm1(x)
    static final byte FLOOR = EXPM1 + 1; // floor(x)
    static final byte LOG = FLOOR + 1; // log(x)
    static final byte LOG10 = LOG + 1; // log10(x)
    static final byte LOG1P = LOG10 + 1; // log1p(x)
    static final byte RINT = LOG1P + 1; // rint(x)
    static final byte ROUND = RINT + 1; // round(x)
    static final byte SIGNUM = ROUND  + 1; // signum(x)
    static final byte SIN = SIGNUM + 1; // sin(x)
    static final byte SINH = SIN + 1; // sinh(x)
    static final byte SQRT = SINH + 1; // sinh(x)
    static final byte TAN = SQRT + 1; // tan(x)
    static final byte TANH = TAN + 1; // tahn(x)
    static final byte TODEGREES = TANH + 1; // toDegrees(x)
    static final byte TORADIANS = TODEGREES + 1; // toRadians(x)
    static final byte ULP = TORADIANS + 1; // ulp(x)

    static final byte ATAN2 = ULP + 1; //  Math.atan2(x, y);
    static final byte HYPOT = ATAN2 + 1; // Math.hypot(x, y);
    static final byte POW = HYPOT + 1; // Math.pow(x, y);

    static final byte MIN = POW + 1; // min(x,y)
    static final byte MAX = MIN + 1; // max(x,y)

    static final byte RANDOM = MAX + 1; //random();


    static final byte PRINT = RANDOM + 1; //random();
    static final byte PRINTLN = PRINT + 1; //random();
    static final byte PRINTF = PRINTLN+ 1; //random();

    static final byte EMPTY = PRINTF + 1; //empty();

    static {
        // code operators
        _codes.put("nop", NOP_OP);
        _codes.put("info", INFO_OP);
        _codes.put("intConst", INTCONST_OP);
        _codes.put("fltConst", FLTCONST_OP);
        _codes.put("boolConst", BOOLCONST_OP);
        _codes.put("strConst", STRCONST_OP);
        _codes.put("nullConst", NULLCONST_OP);
        _codes.put("name", NAME_OP);
        _codes.put("type", TYPE_OP);
        _codes.put("MINUS", MINUS_OP);
        _codes.put("NOT", NOT_OP);
        _codes.put("NEG", NEG_OP);
        _codes.put("idRef", IDREF_OP);
        _codes.put("AND", AND_OP);
        _codes.put("OR", OR_OP);
        _codes.put("XOR", XOR_OP);
        _codes.put("LSH", LSH_OP);
        _codes.put("RSH", RSH_OP);
        _codes.put("RRSH", RRSH_OP);
        _codes.put("ADD", ADD_OP);
        _codes.put("SUB", SUB_OP);
        _codes.put("MUL", MUL_OP);
        _codes.put("DIV", DIV_OP);
        _codes.put("MOD", MOD_OP);
        _codes.put("GT", GT_OP);
        _codes.put("LT", LT_OP);
        _codes.put("GE", GE_OP);
        _codes.put("LE", LE_OP);
        _codes.put("EQ", EQ_OP);
        _codes.put("NE", NE_OP);
        _codes.put("jmp", JMP_OP);
        _codes.put("jmpf", JMPF_OP);
        _codes.put("jmpt", JMPT_OP);
        _codes.put("jmptf", JMPTF_OP);
        _codes.put("INCBEFORE", INCBEFORE_OP);
        _codes.put("DECBEFORE", DECBEFORE_OP);
        _codes.put("INCAFTER", INCAFTER_OP);
        _codes.put("DECAFTER", DECAFTER_OP);
        _codes.put("ASS", ASS_OP);
        _codes.put("ASSADD", ASSADD_OP);
        _codes.put("ASSSUB", ASSSUB_OP);
        _codes.put("ASSMUL", ASSMUL_OP);
        _codes.put("ASSDIV", ASSDIV_OP);
        _codes.put("ASSMOD", ASSMOD_OP);
        _codes.put("ASSAND", ASSAND_OP);
        _codes.put("ASSOR", ASSOR_OP);
        _codes.put("ASSXOR", ASSXOR_OP);
        _codes.put("ASSLSH", ASSLSH_OP);
        _codes.put("ASSRSH", ASSRSH_OP);
        _codes.put("ASSRRSH", ASSRRSH_OP);
        _codes.put("paramList", PARAMLIST_OP);
        _codes.put("param", PARAM_OP);
        _codes.put("method", METHOD_OP);
        _codes.put("function", FUNCTION_OP);
        _codes.put("seitch", SWITCH_OP);
        _codes.put("command", COMMAND_OP);

        _codes.put("if", IF_OP);
        _codes.put("if1", IF1_OP);
        _codes.put("else", ELSE_OP);
        _codes.put("endIf", ENDIF_OP);
        _codes.put("endSwith", ENDSWITCH_OP);

        _codes.put("whileIf", WILEIF_OP);
        _codes.put("endWhile", ENDWILE_OP);
        _codes.put("endDo", ENDDO_OP);

        _codes.put("case", CASE_OP);
        _codes.put("case1", CASE1_OP);

        _codes.put("default", DEFAULT_OP);

        _codes.put("switchBreak", SWITCHBREAK_OP);

        // predefined functions and methods
        _codes.put("abs", ABS);
        _codes.put("acos", ACOS);
        _codes.put("asin", ASIN);
        _codes.put("atan", ATAN);
        _codes.put("cbrt", CBRT);
        _codes.put("cos", COS);
        _codes.put("cosh", COSH);
        _codes.put("exp", EXP);
        _codes.put("expm1", EXPM1);
        _codes.put("floor", FLOOR);
        _codes.put("log", LOG);
        _codes.put("log10", LOG10);
        _codes.put("log1p", LOG1P);
        _codes.put("rint", RINT);
        _codes.put("round", ROUND);
        _codes.put("signum", SIGNUM);
        _codes.put("sin", SIN);
        _codes.put("sinh", SINH);
        _codes.put("sqrt", SQRT);
        _codes.put("tanh", TANH);
        _codes.put("toDegrees", TODEGREES);
        _codes.put("toRadians", TORADIANS);
        _codes.put("ulp", ULP);
        _codes.put("atan2", ATAN2);
        _codes.put("hypot", HYPOT);
        _codes.put("pow", POW);
        _codes.put("min", MIN);
        _codes.put("max", MAX);
        _codes.put("random", RANDOM);
        _codes.put("print", PRINT);
        _codes.put("println", PRINTLN);
        _codes.put("printf", PRINTF);
        _codes.put("empty", EMPTY);
    }

    public final static class CodeItem {
        final byte _op;
        final Object _value;
        CodeItem(final String name, final Object value) {
            try {
                _op = _codes.get(name);
                _value = value;
            } catch (Exception ex) {
                throw new RuntimeException("name: " + name + "; value: " + value);
            }
        }

        @Override
        public String toString() {
            for (Entry<String, Byte> e: _codes.entrySet()) {
                if (_op == e.getValue()) {
                    return e.getKey() + " " + _value;
                }
            }
            return "UNKNOWN OP: " + _op + ", " + _value;
        }
    }

    private static int compileIf(final int i, final Object[] code, CodeItem[] result) {
        result[i] = new CodeItem("nop", 1000 + i);
        int x = -1;
        for (int j = i + 1; j < code.length; j++) {
            if (result[j] != null) continue;
            if (((String) code[j]).startsWith("if ")) {
                j = compileIf(j, code, result);
                continue;
            }
            if (((String) code[j]).startsWith("if1 ")) {
                result[j] = new CodeItem("nop", 9000 + j);
                x = j;
                continue;
            }
            if (((String) code[j]).startsWith("else ")) {
                result[x] = new CodeItem("jmpf", j + 1);
                for (int k = j + 1; k < code.length; k++) {
                    if (result[k] != null) continue;
                    if (((String) code[k]).startsWith("endIf ")) {
                        result[j] = new CodeItem("jmp", k + 1);
                        result[k] = new CodeItem("nop", 9000 + i);
                        return k;
                    }
                }
                throw new RuntimeException("endIf missing after else");
            }
            if (((String) code[j]).startsWith("endIf ")) {
                result[x] = new CodeItem("jmpf", j);
                result[j] = new CodeItem("nop", 9000 + i);
                return j;
            }
        }
        throw new RuntimeException("endIf missing, i=" + i);
    }

    private static int compileWhile(final int i, final Object[] code, CodeItem[] result) {
        result[i] = new CodeItem("nop", null);
        for (int j = i+1; j < code.length; j++) {
            if (result[j] != null) {
                continue;
            }
            if (((String) code[j]).startsWith("whileIf ")) {
                for (int k = j + 1; k < code.length; k++) {
                    if (result[k] != null) {
                        continue;
                    }
                    String s = (String) code[k];
                    if (s.startsWith("while ")) {
                        k = compileWhile(k, code, result);
                        continue;
                    }
                    if (s.startsWith("endWhile ")) {
                        result[j] = new CodeItem("jmpf", k + 1);
                        result[k] = new CodeItem("jmp", i);
                        return k;
                    }
                }
            }
        }
        throw new RuntimeException("endWhile missing, i=" + i);
    }

    private static int compileDo(final int i, final Object[] code, CodeItem[] result) {
        result[i] = new CodeItem("nop", 0);
        for (int j = i+1; j < code.length; j++) {
            if (result[j] != null) continue;
            String s = (String) code[j];
            if (s.startsWith("do ")) {
                j = compileDo(j, code, result);
                continue;
            }
            if (s.startsWith("endDo ")) {
                result[j] = new CodeItem("jmpt", i + 1);
                return j;
            }
        }
        throw new RuntimeException("endDo missing, i=" + i);
    }

    private static int compileFor(final int i, final Object[] code, CodeItem[] result) {
        result[i] = new CodeItem("nop", 0);
        int c = i;
        int j = i+1;
        for (; j < code.length; j++) {
            if (result[j] != null || !((String) code[j]).startsWith("for ")) continue;
            result[j] = new CodeItem("nop", 9000);
            c = j++;
            break;
        }
        if (c == i) {
            j = i+1;
        }
        for (; j < code.length; j++) {
            if (result[j] != null) continue;
            String s = (String) code[j];
            if (s.startsWith("for ")) {
                j = compileFor(j, code, result);
                continue;
            }
            if (!s.startsWith("for1 ")) continue;
            result[j] = new CodeItem("nop", 9001);
            int j1 = j;
            for (; j1 < code.length; j1++) {
                if (result[j1] != null) continue;
                s = (String) code[j1];
                if (s.startsWith("for ")) {
                    j1 = compileFor(j1, code, result);
                    continue;
                }
                if (!s.startsWith("for1 ")) continue;
                break;
            }
            for (int k = j1 + 1; k < code.length; k++) {
                if (result[k] != null) continue;
                s = (String) code[k];
                if (s.startsWith("for ")) {
                    k = compileFor(k, code, result);
                    continue;
                }
                if (!s.startsWith("for2 ")) continue;
                result[k] = new CodeItem("nop", 9003);
                for (int m = k+1; m < code.length; m++) {
                    for (int n = m+1; n < code.length; n++) {
                        if (result[n] != null) continue;
                        s = (String) code[n];
                        if (s.startsWith("for ")) {
                            m = compileFor(n, code, result);
                            continue;
                        }
                        if (!s.startsWith("for3 ")) continue;
                        result[j] = new CodeItem("jmpf", n + 1);
                        result[j+1] = new CodeItem("jmp", k + 1);
                        result[k] = new CodeItem("jmp", c + 1);
                        result[n] = new CodeItem("jmp", j + 2);
                        return n;
                    }
                }
            }
        }
        throw new RuntimeException("for3 missing, i=" + i);
    }

    private static int compileSwitch(final int i, final Object[] code, CodeItem[] result) {
        result[i] = new CodeItem("nop", 0);
        for (int j = i + 1; j < code.length; j++) {
            String s = (String) code[j];
            System.out.println("***** [" + j + "]: " + s); //TODO
            if (result[j] != null) {
                continue;
            }
            if (s.startsWith("switch ")) {
                j = compileSwitch(j, code, result);
                continue;
            }
            if (s.startsWith("endSwitch ")) {
                result[j] = new CodeItem("nop", 999);
                return j;
            }
            if (s.startsWith("case ")
                || s.startsWith("case1 ")
                || s.startsWith("endCase ")
                || s.startsWith("default ")
                || s.startsWith("endDefault ")) {
                result[j] = new CodeItem("nop", 0);
            }
        }
        throw new RuntimeException("endSwitch missing, i=" + i);
    }

    public static CodeItem[] precompile(final String source, final Object[] code) {
        CodeItem[] result = new CodeItem[code.length];
        for (int i = 0; i < code.length; i++) {
            if (result[i] != null) continue;
            String item = code[i].toString();
            if (item.startsWith("info: ")) { // parsed position
                result[i] = new CodeItem("info", item.substring(6).trim());
                continue;
            }
            String[] ii = ((String) code[i]).split(" ");
            switch (item = ii[0]) {
                case "intConst":
                    result[i] = new CodeItem(item,
                        Long.valueOf(source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    continue;
                case "fltConst":
                    result[i] = new CodeItem(item,
                        Double.valueOf(source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    continue;
                case "boolConst":
                    result[i] = new CodeItem(item,
                        "true".equals(source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]))));
                    continue;
                case "strConst": {
                    String s = source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2]));
                    String delimiter = String.valueOf(s.charAt(0));
                    s = s.substring(1, s.length() - 1);
                    s = SUtils.modifyString(s, delimiter + delimiter, delimiter);
                    result[i] = new CodeItem(item, s);
                    continue;
                }
                case "nullConst": result[i] = new CodeItem(item, null); break;
                case "name":
                    result[i] = new CodeItem(item, source.substring(Integer.parseInt(ii[1]), Integer.parseInt(ii[2])));
                    continue;
                case "if": compileIf(i, code, result); continue;
                case "while": compileWhile(i, code, result); continue;
                case "do": compileDo(i, code, result); continue;
                case "for": compileFor(i, code, result); continue;
                case "switch": compileSwitch(i, code, result); continue;
                default: if (result[i] == null) {
                    result[i] = new CodeItem(item,
                        item.endsWith("type")? source.substring(Integer.parseInt(ii[1]),Integer.parseInt(ii[2])): null);
                }
            }
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
                } else if (u.length() < 16) {
                    u += "\t\t";
                } else if (u.length() < 24) {
                    u += "\t";
                }

            }
            sb.append(s).append(u).append(t).append('\n');
        }
        return sb.toString();
    }

    /** Execute generated code.
     * @param pc generated program code;
     * @param vars variable table.
     * @param out where to print
     * @return result of execution (or null).
     */
    public static Object execute(final CodeItem[] pc, final Map<String, Object> vars, final PrintStream out) {
        vars.clear();
        final Stack<Object> stack = new Stack<>();
        for (int i = 0; i < pc.length; i++) {
            CodeItem item = pc[i];
            switch (item._op) {
                case NOP_OP: continue;
                case INTCONST_OP: stack.push((Long) item._value); break;
                case FLTCONST_OP: stack.push((Double) item._value); break;
                case BOOLCONST_OP: stack.push((Boolean) item._value); break;
                case STRCONST_OP: stack.push((String) item._value); break;
                case NULLCONST_OP: stack.push(null); break;
                case NAME_OP: stack.push((String) item._value); break;
                case TYPE_OP:
                    String s = (String) item._value;
                    s = s.substring(2); // name
                    vars.put(s, null);
                    stack.push(s);
                    break;
                case MINUS_OP: {
                        Number x = (Number) stack.pop();
                        if (x instanceof Long) {
                            stack.push(-x.longValue());
                        } else {
                            stack.push(-x.doubleValue());
                        }
                        break;
                    }
                case NOT_OP: stack.push(!((Boolean) stack.pop())); break;
                case NEG_OP: stack.push(~((Long) stack.pop())); break;
                case IDREF_OP: stack.push(vars.get(stack.pop().toString())); break;
                case AND_OP:
                case OR_OP:
                case XOR_OP: {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Boolean && y instanceof Boolean) { // boolean operation
                            switch (item._op) {
                                case AND_OP: stack.push((Boolean) x && (Boolean) y); break;
                                case OR_OP: stack.push((Boolean) x || (Boolean) y); break;
                                default: stack.push((Boolean) x ^ (Boolean) y); // XOR
                            }
                        } else if (x instanceof Long && y instanceof Long) { // bitwise operation
                            switch (item._op) {
                                case AND_OP: stack.push((Long) x & (Long) y); break;
                                case OR_OP: stack.push((Long) x | (Long) y); break;
                                default: stack.push((Long) x ^ (Long) y);
                            }
                        } else {
                            throw new RuntimeException("Error: Operand types " + x.getClass() + "," + y.getClass());
                        }
                        break;
                    }
                case LSH_OP:
                case RSH_OP:
                case RRSH_OP: {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = LSH_OP == item._op ? xx << yy : RSH_OP == item._op ? xx >> yy : xx >>> yy;
                            stack.push(y);
                        } else {
                            throw new RuntimeException("Error: Operand types " + x.getClass() + "," + y.getClass());
                        }
                        break;
                    }
                case ADD_OP: {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Number && y instanceof Number) {
                            if (x instanceof Long && y instanceof Long) {
                                stack.push(((Number) x).longValue() + ((Number) y).longValue());
                            } else {
                                stack.push(((Number) x).doubleValue() + ((Number) y).doubleValue());
                            }
                        } else {
                            stack.push(x.toString() + y);
                        }
                        break;
                    }
                case SUB_OP:
                case MUL_OP:
                case DIV_OP:
                case MOD_OP: {
                        Number y = (Number) stack.pop();
                        Number x = (Number) stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                           switch (item._op) {
                                case SUB_OP: stack.push(x.longValue() - y.longValue()); break;
                                case MUL_OP: stack.push(x.longValue() * y.longValue()); break;
                                case DIV_OP: stack.push(x.longValue() / y.longValue()); break;
                                default: stack.push(x.longValue() % y.longValue()); // MOD
                            }
                        } else {
                            switch (item._op) {
                                case SUB_OP: stack.push(x.doubleValue() - y.doubleValue()); break;
                                case MUL_OP: stack.push(x.doubleValue() * y.doubleValue()); break;
                                case DIV_OP: stack.push(x.doubleValue() / y.doubleValue()); break;
                                default: stack.push(x.doubleValue() % y.doubleValue());// MOD
                            }
                        }
                        break;
                    }
                case GT_OP:
                case LT_OP:
                case GE_OP:
                case LE_OP:
                case EQ_OP:
                case NE_OP: {
                        Object y = stack.pop();
                        Object x = stack.pop();
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            stack.push(GT_OP == item._op ? xx > yy
                                : LT_OP == item._op ? xx < yy
                                    : GE_OP == item._op ? xx >= yy
                                        : LE_OP == item._op ? xx <= yy
                                            : EQ_OP == item._op ? xx == yy : xx != yy);
                        } else if (x instanceof Number && y instanceof Number) {
                            double xx = ((Number) x).doubleValue();
                            double yy = ((Number) y).doubleValue();
                            stack.push(GT_OP == item._op ? xx > yy
                                : LT_OP == item._op ? xx < yy
                                    : GE_OP == item._op ? xx >= yy
                                        : LE_OP == item._op ? xx <= yy
                                            : EQ_OP == item._op ? xx == yy : xx != yy);
                        } else if (x instanceof Boolean&& y instanceof Boolean){
                            boolean xx = (Boolean) x;
                            boolean yy = (Boolean) y;
                            switch (item._op) {
                                case EQ_OP: stack.push(xx == yy); break;
                                case NE_OP: stack.push(xx != yy); break;
                                default: throw new RuntimeException("Error: Operand types "
                                            + x.getClass() + "," + x.getClass());
                            }
                        }
                        break;
                    }
                case INCBEFORE_OP:
                case DECBEFORE_OP:
                case INCAFTER_OP:
                case DECAFTER_OP: {
                        String name = stack.pop().toString(); // name of var
                        Object x = vars.get(name);
                        if (x instanceof Long) {
                            if (INCBEFORE_OP == item._op || DECBEFORE_OP == item._op) {
                                x = (Long) x + (INCBEFORE_OP == item._op ? 1 : -1);
                                vars.put(name, x);
                                stack.push(x);
                            } else {
                                stack.push(x);
                                x = (Long) x + (INCAFTER_OP == item._op ? 1 : -1);
                                vars.put(name, x);
                            }
                        } else if (x instanceof Double) {
                            if (INCBEFORE_OP == item._op || DECBEFORE_OP == item._op) {
                                x = (Double) x+(INCBEFORE_OP == item._op ? 1 : -1);
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
                case ASS_OP: {
                        Object x = stack.pop();
                        vars.put((String) stack.pop(), x);
                        break;
                    }
                case ASSADD_OP:
                case ASSSUB_OP:
                case ASSMUL_OP:
                case ASSDIV_OP:
                case ASSMOD_OP: {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Number && y instanceof Number) {
                            Number xx = (Number) x;
                            Number yy = (Number) y;
                            if (x instanceof Long && y instanceof Long) {
                                switch (item._op) {
                                    case ASSADD_OP: y = yy.longValue() + xx.longValue(); break;
                                    case ASSSUB_OP: y = yy.longValue() - xx.longValue(); break;
                                    case ASSMUL_OP: y = yy.longValue() * xx.longValue(); break;
                                    case ASSDIV_OP: y = yy.longValue() / xx.longValue(); break;
                                    default: y = yy.longValue() % xx.longValue(); // ASSMOD
                                }
                            } else {
                                switch (item._op) {
                                    case ASSADD_OP: y = yy.doubleValue() + xx.doubleValue(); break;
                                    case ASSSUB_OP: y = yy.doubleValue() - xx.doubleValue(); break;
                                    case ASSMUL_OP: y = yy.doubleValue() * xx.doubleValue(); break;
                                    case ASSDIV_OP: y = yy.doubleValue() / xx.doubleValue(); break;
                                    default: y = yy.doubleValue() % xx.doubleValue(); // ASSMOD
                                }
                            }
                        } else if ("ASSADD".equals(item._op) && y instanceof String) {
                            y = y.toString() + x;
                        }
                        vars.put(name, y);
                        break;
                    }
                case ASSAND_OP:
                case ASSOR_OP:
                case ASSXOR_OP: {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Boolean && y instanceof Boolean) {
                            boolean xx = (Boolean) x;
                            boolean yy = (Boolean) y;
                            y = ASSAND_OP == item._op ? yy & xx : ASSOR_OP == item._op ? yy | xx : yy ^ xx;
                        } else if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = ASSAND_OP == item._op ? yy & xx : ASSOR_OP == item._op ? yy | xx : yy ^ xx;
                        }
                        vars.put(name, y);
                        break;
                    }
                case ASSLSH_OP:
                case ASSRSH_OP:
                case ASSRRSH_OP: {
                        Object x = stack.pop();
                        String name = (String) stack.pop();
                        Object y = vars.get(name);
                        if (x instanceof Long && y instanceof Long) {
                            long xx = (Long) x;
                            long yy = (Long) y;
                            y = ASSLSH_OP == item._op ? yy << xx : ASSRSH_OP == item._op ? yy >> xx : yy >>> xx;
                        }
                        vars.put(name, y);
                        break;
                    }
                case PARAMLIST_OP:
                    stack.push(new PredefinedMethod(stack.pop().toString()));
                    break;
                case PARAM_OP: { // parameter
                        Object x = stack.pop();
                        PredefinedMethod y =  (PredefinedMethod) stack.peek();
                        y.add(x);
                        break;
                    }
                case METHOD_OP: ((PredefinedMethod) stack.pop()).invokeMethod(out); break;
                case FUNCTION_OP: { // procedure or function
                        PredefinedMethod x = (PredefinedMethod) stack.pop();
                        Object y = x.invokeMethod(out);
                        if (y != null) {
                            stack.push(y);
                        } else {
                            throw new RuntimeException("Value of method " + x + " expected");
                        }
                        break;
                    }
                case JMP_OP: i = ((Integer) item._value) - 1; break;
                case JMPF_OP:
                    if (!((Boolean)stack.pop())) {
                        i = ((Integer) item._value) - 1;
                    }
                    break;
                case JMPT_OP:
                    if (((Boolean)stack.pop())) {
                        i = ((Integer) item._value) - 1;
                    }
                    break;
                case JMPTF_OP: i = ((int[]) (item._value))[((Boolean) stack.pop()) ? 0 : 1]; break;
                case COMMAND_OP: stack.clear(); break;
                case INFO_OP: break;
                default: throw new RuntimeException("Unknown code at "+i+": " + item);
            }
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    /** Predefined method. */
    private static final class PredefinedMethod extends ArrayList<Object> {
        private final byte _op; // name of method

        private PredefinedMethod(final String name) {
            super();
            _op = _codes.get(name);
        }

        private Object invokeMethod(PrintStream out) {
            if (isEmpty()) { // no parameters
                switch (_op) {
                    case RANDOM: return Math.random();
                    case EMPTY: return "";
                    case PRINTLN: out.println(); return null;
                }
            } else { // one or more parameters
                Object o1 = get(0);
                if (PRINTF == _op) {
                    remove(0); // we have the first parametr in o1
                    out.printf(o1.toString(), toArray());
                    return null;
                }
                if (size() == 1) { // one parameter
                    switch (_op) {
                        case PRINTLN: out.println(o1); return null;
                        case PRINT: out.print(o1); return null;
                    }
                    if (o1 instanceof Number) {
                        double x = ((Number) o1).doubleValue();
                        switch (_op) {
                            case ABS: return Math.abs(x);
                            case ACOS: return Math.acos(x);
                            case ASIN: return Math.asin(x);
                            case ATAN: return Math.atan(x);
                            case CBRT: return Math.cbrt(x);
                            case CEIL: return Math.ceil(x);
                            case COS: return Math.cos(x);
                            case COSH: return Math.cosh(x);
                            case EXP: return Math.exp(x);
                            case EXPM1: return Math.expm1(x);
                            case FLOOR: return Math.floor(x);
                            case LOG: return Math.log(x);
                            case LOG10: return Math.log10(x);
                            case LOG1P: return Math.log1p(x);
                            case RINT: return Math.rint(x);
                            case ROUND: return Math.round(x);
                            case SIGNUM: return Math.signum(x);
                            case SIN: return Math.sin(x);
                            case SINH: return Math.sinh(x);
                            case SQRT: return Math.sqrt(x);
                            case TAN: return Math.tan(x);
                            case TANH: return Math.tanh(x);
                            case TODEGREES: return Math.toDegrees(x);
                            case TORADIANS: return Math.toRadians(x);
                            case ULP: return Math.ulp(x);
                        }
                    }
                }
                if (size() == 2) { // two parameters
                    Object o2 = get(1);
                    if (o1 instanceof Long && o2 instanceof Long) {
                        switch (_op) {
                            case MIN: return Math.min(((Long) o1), ((Long) o2));
                            case MAX: return Math.max(((Long) o1), ((Long) o2));
                        }
                    }
                    if (o1 instanceof Number && o2 instanceof Number) {
                        double x = ((Number) o1).doubleValue();
                        double y = ((Number) o2).doubleValue();
                        switch (_op) {
                            case ATAN2: return Math.atan2(x, y);
                            case HYPOT: return Math.hypot(x, y);
                            case MIN: return Math.min(x, y);
                            case MAX: return Math.max(x, y);
                            case POW: return Math.pow(x, y);
                        }
                    }
                }
            }
            throw new RuntimeException("Unknown method: " + this);
        }
        @Override
        public String toString() {
            for (Entry<String, Byte> e: _codes.entrySet()) {
                if (_op == e.getValue()) {
                    return e.getKey();
                }
            }
            return "UNKNOWN METHOD: " + _op;
        }
    }
}
