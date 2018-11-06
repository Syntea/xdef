import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import org.w3c.dom.Element;


public class Example1 {	
	public static void main(String[] args) {
		XDPool xp = XDFactory.compileXD(null, "Example1.xdef");
		XDDocument xd = xp.createXDDocument();
		xd.setXDContext(KXmlUtils.parseXml("Example1.xml").getDocumentElement());
		Element el = xd.xcreate(null, "Contract", null);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}
