package bugreports;

import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;

public class References {

	/** Display references from which this model is created.
	 * @param x model of element.
	 */
	private static void displayRefs(final XMElement x) {
		if (x.getKind() == XMNode.XMELEMENT && ((XMElement) x).isReference()) {
			XMNode xn = x;
			System.out.println(x.getXDPosition());
			while (xn.getKind() == XMNode.XMELEMENT
			&& ((XMElement) xn).isReference()) {
				XMElement xe = (XMElement) xn;
				String xref = xe.getReferencePos();
				System.out.println(" -> " + xref);
				xn = xe.getXDPool().findModel(xref);
			}
		}
		XMNode[] children = x.getChildNodeModels();
		if (children.length > 0) {
			for (XMNode y: x.getChildNodeModels()) {
				if (y.getKind() == XMNode.XMELEMENT) {
					displayRefs((XMElement) y);
				}
			}
		}
	}

	/** Run this example.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Ab\" root=\"A\" >\n" +
"  <A><B xd:script='*; ref C'/></A>\n"+
"  <C xd:script='*; ref D'/>\n"+
"  <D xd:script='*; ref E'/>\n"+
"  <E c='? string()'>? string</E>\n"+
"</xd:def>";
		XDPool xp = XDFactory.compileXD(null, xdef);
		XMDefinition xd = xp.getXMDefinition("Ab");
		XMElement xe = xd.getModel(null, "A");
		displayRefs(xe);
	}
}
