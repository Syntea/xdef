package org.xdef.sys;

/** Interface for external method object.
 * External method must be declared in BNF source by command:
 * %define $name: $class_name(parameters)
 * <p>Example:</p><pre><code>
 * BNF source:
 * %define $p: $test.bnf.Mytest.myproc(int)
 * digit ::= [0-9]
 * signedInteger::= [-+]? digit+ $p(123)
 * ...
 * Java source:
 *	package test.bnf;
 *	...
 *	public class Mytest {
 *		/** External user method for BNF processor.
 *		 * @param p.
 *		 * External user method for BNF processor.
 *		 * /
 *		public static boolean myproc(BNFExtMethod p, int x) {
 *			System.out.println(p.getRuleName() + ", pos:" + p.getSPosition() +
 *				", " + p.getParsedString() + ", param: " + x);
 *			return true;
 *		}
 *	...
 *		public static void main(String... args) {
 *			BNFGrammar grammar = BNFGrammar.compile(source);
 *			if (!grammar.parse("-12345", "signedInteger")) {
 *				System.err.println("Error: " + );
 *			}
 *		}
 *	}
 * </code></pre>
 * @author Vaclav Trojan
 */
public interface BNFExtMethod {

		/** Get name of external method.
		 * @return name of external method.
		 */
		public String getMethodName();

		/** Get parsed part of string by this rule.
		 * @return parsed part of string by this rule.
		 */
		public String getParsedString();

		/** Get SParser used for parsing.
		 * @return SParser.
		 */
		public StringParser getParser();

		/** Get objects from internal stack.
		 * @return objects from  internal stack.
		 */
		public Object[] getParsedStack();

		/** Pop value from parsed stack.
		 * @return the top of parsed stack or null.
		 */
		public Object popParsedObject();

		/** Get the value of the top of parsed stack.
		 * @return the top of parsed stack or null.
		 */
		public Object peekParsedObject();

		/** Push object to parsed stack.
		 * @param o object to be pushed.
		 */
		public void pushParsedObject(Object o);

		/** Get root BNF rule.
		 * @return root BNF rule.
		 */
		public BNFRule getRootRule();

		/** Get actual BNF rule.
		 * @return actual BNF rule.
		 */
		public BNFRule getRule();

		/** Get actual rule name.
		 * @return actual rule name.
		 */
		public String getRuleName();

		/** Get actual source position.
		 * @return SPosition object with actual source position.
		 */
		public SPosition getSPosition();

		/** Get associated user object.
		 * @return associated user object.
		 */
		public Object getUserObject();

		/** Set user object.
		 * @param obj new value of user object.
		 * @return old value of user object.
		 */
		public Object setUserObject(Object obj);
}