package test.xdef;

import java.util.ArrayList;
import java.util.List;
import org.xdef.component.XComponent;
import org.xdef.proc.XXNode;

public class TestXComponents_G {
	XComponent _X, _Y, _G;

	private String _XX;
	private int _flags;

	public static void setXX(XXNode xx, String s) {
		if (xx.getXComponent() != null) {
			((TestXComponents_G) xx.getXComponent())._XX = s;
		}
	}

	public static void genXC(XXNode xnode) {
		String name = xnode.getXXName();
		TestXComponents_G xm = (TestXComponents_G) xnode.getUserObject();
		if ("G".equals(name)) {
			xm._G = xnode.getXComponent();
		} else if ("XXX".equals(name)) {
			xm._X = xnode.getXComponent();
		} else if ("YYY".equals(name)) {
			xm._Y = xnode.getXComponent();
		} else {
			throw new RuntimeException("Unknown element:" + name);
		}
	}

	public String getXX() {return _XX;}
	public final void xSetFlags(final int flags) {_flags |= flags;}
	public final void xClearFlags(final int flags) {_flags &= ~flags;}
	public final boolean xCheckFlags(final int flags) {
		return (flags & _flags) == flags;
	}
	public final int xGetFlags() {return _flags;}

	private final List<XComponent> _YYY = new ArrayList<XComponent>();
	private String _g;
	private String XD_NAME_g;
	private XComponent _XXX;

	public String getg() {return _g;}
	public void setg(String x) {_g = x + '_';}
	public XComponent getXXX() {return _XXX;}
	public void setXXX(XComponent x) {_XXX = x;}
	public List<XComponent> listOfYYY() {return _YYY;}
	public void setYYY(List<XComponent> x) {
		_YYY.clear();
		if (x != null) {_YYY.addAll(x);}
	}
}