package cz.syntea.xdef;

//import cz.syntea.xd.impl.DefContainer;
import cz.syntea.xdef.proc.XXNode;
import org.w3c.dom.Node;

/** Datetime in x-script.
 * @author Vaclav Trojan
 */
public interface XDXQueryExpr extends XDValue {

	/** Execute XQuery expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return The string representation of value of the object.
	 */
	public XDContainer exec(Node node, XXNode xNode);

}
