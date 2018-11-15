import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;

public class Example {	
	public static void main(String[] args) {
		try {
			XDPool xp = XDFactory.compileXD(null, "Example.xdef");
			XDDocument xd = xp.createXDDocument();
			xd.xparse("Example.xml",null);
			System.out.println("Input data processed and no errors detected");
		} catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
