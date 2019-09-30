package test.xdef;

import java.io.File;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.SRuntimeException;

/**
 *
 * @author Vaclav Trojan
 */
public class TestDebugGUI1 {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty(XDConstants.XDPROPERTY_XDEF_EDITOR,
			"oxygeneditor.OxygenEditor");
        props.setProperty(XDConstants.XDPROPERTY_DISPLAY,
			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE);
        File xd0 = new File("src/testexternaleditor/test1.xdef");
        File xd1 = new File("src/testexternaleditor/test2.xml");
        Object[] srcs = new Object[]{xd0};
//		Object[] srcs = null;
        try {
            XDPool xp = XDFactory.compileXD(props, srcs); 
            System.out.println("Ok.");
            
        } catch (SRuntimeException ex) {
            System.out.println(ex.getMessage());
        }
	}
	
}
