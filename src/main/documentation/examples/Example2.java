import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import org.w3c.dom.Element;

public class Example2 {	
	public static void main(String[] args) {
		XDPool xp = XDFactory.compileXD(null, "Example2.xdef");
		XDDocument xd = xp.createXDDocument();
		Element el = xd.xparse("Example2.xml", null);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}
