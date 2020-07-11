package test.common.bnf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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

	private final static class CodeItem {
		final String _op;
		final Object _value;
		CodeItem(final String name, final Object value) {
			_op = name; _value = value;
		}
	}

	private static List<CodeItem> precompile(final String source,
		final Object[] code) {
		List<CodeItem> newCode = new ArrayList<CodeItem>();
		for (int i = 0; i < code.length; i++) {
			String item = code[i].toString();
			if (item.startsWith("info: ")) { // parsed position
//				newCode.add(new CodeItem("info", item.substring(6).trim()));
				continue;
			}
			String[] ii = ((String) code[i]).split(" ");
			item = ii[0];
			if ("intConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				newCode.add(new CodeItem(item, Long.parseLong(s)));
			} else if ("fltConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				newCode.add(new CodeItem(item, Double.parseDouble(s)));
			} else if ("boolConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				newCode.add(new CodeItem(item, "true".equals(s)));
			} else if ("strConst".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				String delimiter = String.valueOf(s.charAt(0));
				s = s.substring(1, s.length() - 1);
				s = SUtils.modifyString(s, delimiter + delimiter, delimiter);
				newCode.add(new CodeItem(item, s));
			} else if ("nullConst".equals(item)) {
				newCode.add(new CodeItem(item, null));
			} else if ("name".equals(item)) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				newCode.add(new CodeItem(item, s));
			} else if (item.endsWith("type")) {
				String s = source.substring(Integer.parseInt(ii[1]),
					Integer.parseInt(ii[2]));
				newCode.add(new CodeItem(item, s));
			} else {// unary operator minus
				newCode.add(new CodeItem(item, null));
			}
		}
		return newCode;
	}

	private static PrintStream _out;

	/** Execute generated code.
	 * @param source source text.
	 * @param code generated code.
	 * @param variables variable table.
	 * @return result of execution (or null).
	 */
	static Object execute(final String source,
		final Object[] code,
		final Map<String, Object> variables,
		ByteArrayOutputStream byteArray) {
		final Stack<Object> stack = new Stack<Object>();
		variables.clear();
		byteArray.reset();
		try { // prepare printing
			byteArray.reset();
			_out = new PrintStream(byteArray, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {/* never happens */}
		List<CodeItem> pc = precompile(source, code);
		for (int i = 0; i < pc.size(); i++) {
			CodeItem item = pc.get(i);
			if ("intConst".equals(item._op)) {
				stack.push((Long) item._value);
			} else if ("fltConst".equals(item._op)) {
				stack.push((Double) item._value);
			} else if ("boolConst".equals(item._op)) {
				stack.push((Boolean) item._value);
			} else if ("strConst".equals(item._op)) {
				stack.push((String) item._value);
			} else if ("nullConst".equals(item._op)) {
				stack.push(null);
			} else if ("name".equals(item._op)) {
				stack.push((String) item._value);
			} else if ("type".equals(item._op)) {
				String s = (String) item._value;
				s = s.substring(2); // name
				variables.put(s, null);
				stack.push(s);
			} else if ("MINUS".equals(item._op)) {
				Number x = (Number) stack.pop();
				if (x instanceof Long) {
					stack.push(-x.longValue());
				} else {
					stack.push(-x.doubleValue());
				}
			} else if ("NOT".equals(item._op)) {
				stack.push(!((Boolean) stack.pop()));
			} else if ("NEG".equals(item._op)) {
				stack.push(~((Long) stack.pop()));
			} else if ("idRef".equals(item._op)) {
				stack.push(variables.get(stack.pop().toString()));
			} else if ("AND".equals(item._op) || "OR".equals(item._op)
				|| "XOR".equals(item._op)) {
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Boolean &&
					y instanceof Boolean) { // logical operation
					if ("AND".equals(item._op)) {
						stack.push((Boolean) x && (Boolean) y);
					} else if ("OR".equals(item._op)) {
						stack.push((Boolean) x || (Boolean) y);
					} else {
						stack.push((Boolean) x ^ (Boolean) y);
					}
				} else if (x instanceof Long &&
					y instanceof Long) { // bitwise operation
					if ("AND".equals(item._op)) {
						stack.push((Long) x & (Long) y);
					} else if ("OR".equals(item._op)) {
						stack.push((Long) x | (Long) y);
					} else {
						stack.push((Long) x ^ (Long) y);
					}
				} else {
					throw new RuntimeException("Error: Operand types "
						+ x.getClass() + "," + y.getClass());
				}
			} else if ("LSH".equals(item._op) || "RSH".equals(item._op)
				|| "RRSH".equals(item._op)) {
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "LSH".equals(item._op) ? xx << yy
						: "RSH".equals(item._op) ? xx >> yy : xx >>> yy;
					stack.push(y);
				} else {
					throw new RuntimeException("Error: Operand types "
						+ x.getClass() + "," + y.getClass());
				}
			} else if ("ADD".equals(item._op)) {
				Object y = stack.pop();
				Object x = stack.pop();
				if (x instanceof Number && y instanceof Number) {
					if (x instanceof Long && y instanceof Long) {
						stack.push(((Number) x).longValue()
							+ ((Number) y).longValue());
					} else {
						stack.push(((Number) x).doubleValue()
							+ ((Number) y).doubleValue());
					}
				} else {
					stack.push(x.toString() + y.toString());
				}
			} else if ("SUB".equals(item._op) || "MUL".equals(item._op)
				|| "DIV".equals(item._op) || "MOD".equals(item._op)) {
				Number y = (Number) stack.pop();
				Number x = (Number) stack.pop();
				if (x instanceof Long && y instanceof Long) {
					if ("SUB".equals(item._op)) {
						stack.push(x.longValue() - y.longValue());
					} else if ("MUL".equals(item._op)) {
						stack.push(x.longValue() * y.longValue());
					} else if ("DIV".equals(item._op)) {
						stack.push(x.longValue() / y.longValue());
					} else { // MOD
						stack.push(x.longValue() % y.longValue());
					}
				} else {
					if ("SUB".equals(item._op)) {
						stack.push(x.doubleValue() - y.doubleValue());
					} else if ("MUL".equals(item._op)) {
						stack.push(x.doubleValue() * y.doubleValue());
					} else if ("DIV".equals(item._op)) {
						stack.push(x.doubleValue() / y.doubleValue());
					} else { // MOD
						stack.push(x.doubleValue() % y.doubleValue());
					}
				}
			} else if ("GT".equals(item._op) || "LT".equals(item._op)
				|| "GE".equals(item._op) || "LE".equals(item._op)
				|| "EQ".equals(item._op) || "NE".equals(item._op)) {
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
					if ("EQ".equals(item._op)) {
						stack.push(xx == yy);
					} else if ("NE".equals(item._op)) {
						stack.push(xx != yy);
					} else {
						throw new RuntimeException("Error: Operand types "
							+ x.getClass() + "," + x.getClass());
					}
				}
			} else if ("INCBEFORE".equals(item._op)
				|| "DECBEFORE".equals(item._op)
				|| "INCAFTER".equals(item._op) || "DECAFTER".equals(item._op)) {
				String name = stack.pop().toString(); // name of var
				Object x = variables.get(name);
				if (x instanceof Long) {
					if ("INCBEFORE".equals(item._op)
						|| "DECBEFORE".equals(item._op)){
						x = (Long) x + ("INCBEFORE".equals(item._op) ? 1 : -1);
						variables.put(name, x);
						stack.push(x);
					} else {
						stack.push(x);
						x = (Long) x + ("INCAFTER".equals(item._op) ? 1 : -1);
						variables.put(name, x);
					}
				} else if (x instanceof Double) {
					if ("INCBEFORE".equals(item._op)
						|| "DECBEFORE".equals(item._op)) {
						x = (Double) x+("INCBEFORE".equals(item._op) ? 1 : -1);
						variables.put(name, x);
						stack.push(x);
					} else {
						stack.push(x);
						x = (Double) x + ("INCAFTER".equals(item._op) ? 1 : -1);
						variables.put(name, x);
					}
				} else {
					throw new RuntimeException("Error: Operand type "
						+ x.getClass());
				}
			} else if ("ASS".equals(item._op)) {
				Object x = stack.pop();
				variables.put((String) stack.pop(), x);
			} else if ("ASSADD".equals(item._op) || "ASSSUB".equals(item._op)
				|| "ASSMUL".equals(item._op) || "ASSDIV".equals(item._op)
				|| "ASSMOD".equals(item._op)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Number && y instanceof Number) {
					boolean bothint =
						x instanceof Long && y instanceof Long;
					Number xx = (Number) x;
					Number yy = (Number) y;
					if ("ASSADD".equals(item._op)) {
						if (bothint) {
							y = yy.longValue() + xx.longValue();
						} else {
							y = yy.doubleValue() + xx.doubleValue();
						}
					} else if ("ASSSUB".equals(item._op)) {
						if (bothint) {
							y = yy.longValue() - xx.longValue();
						} else {
							y = yy.doubleValue() - xx.doubleValue();
						}
					} else if ("ASSMUL".equals(item._op)) {
						if (bothint) {
							y = yy.longValue() * xx.longValue();
						} else {
							y = yy.doubleValue() * xx.doubleValue();
						}
					} else if ("ASSDIV".equals(item._op)) {
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
				} else if ("ASSADD".equals(item._op)
					&& y instanceof String) {
					y = y.toString() + x;
				}
				variables.put(name, y);
			} else if ("ASSAND".equals(item._op) || "ASSOR".equals(item._op)
				|| "ASSXOR".equals(item._op)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Boolean && y instanceof Boolean) {
					boolean xx = (Boolean) x;
					boolean yy = (Boolean) y;
					y = "ASSAND".equals(item._op) ? yy & xx
						: "ASSOR".equals(item._op) ? yy | xx : yy ^ xx;
				} else if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "ASSAND".equals(item._op) ? yy & xx
						: "ASSOR".equals(item._op) ? yy | xx : yy ^ xx;
				}
				variables.put(name, y);
			} else if ("ASSLSH".equals(item._op) || "ASSRSH".equals(item._op)
				|| "ASSRRSH".equals(item._op)) {
				Object x = stack.pop();
				String name = (String) stack.pop();
				Object y = variables.get(name);
				if (x instanceof Long && y instanceof Long) {
					long xx = (Long) x;
					long yy = (Long) y;
					y = "ASSLSH".equals(item._op) ? yy << xx
						: "ASSRSH".equals(item._op) ? yy >> xx
						: yy >>> xx;
				}
				variables.put(name, y);
			} else if ("paramList".equals(item._op)) {
				stack.push(new PredefinedMethod(stack.pop().toString()));
			} else if ("param".equals(item._op)) {  // parameter
				Object x = stack.pop();
				PredefinedMethod y =  (PredefinedMethod) stack.peek();
				y.add(x);
			} else if ("method".equals(item._op)
				|| "function".equals(item._op)) {
				// procedure or function
				PredefinedMethod x = (PredefinedMethod) stack.pop();
				if ("function".equals(item._op)) {
					Object y = x.invoke();
					if (y != null) {
						stack.push(y);
					} else {
						throw new RuntimeException("Value of method "
							+ x._name + " expected");
					}
				} else {
					x.invoke();
				}
			} else if ("command".equals(item._op)) {
				stack.clear();
			} else {
				throw new RuntimeException("Unknown code: " + item._op);
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
}