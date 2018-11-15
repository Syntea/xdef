import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;


public class Example1 {	
	public static void main(String[] args) {
		XDPool xp = XDFactory.compileXD(null, "Example1.xdef");
		XDDocument xd = xp.createXDDocument();
		xd.setXDContext(KXmlUtils.parseXml("Example1.xml").getDocumentElement());
		Element el = xd.xcreate("Contract", null);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}
