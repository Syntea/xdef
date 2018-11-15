import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Example2 {	
	public static void main(String[] args) {
		XDPool xp = XDFactory.compileXD(null, "Example2.xdef");
		XDDocument xd = xp.createXDDocument();
		Element el = xd.xparse("Example2.xml", null);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}
