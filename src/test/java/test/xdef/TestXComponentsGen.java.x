/*
 * Copyright 2013 Syntea software group a.s. All rights reserved.
 *
 * File: TestXComponentsGen.java, created 2013-09-21.
 * Package: test.xdef
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package test.xdef;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.component.GenXComponent;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.XXNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Generate XComponents Java source files.
 * @author Vaclav Trojan
 */
public class TestXComponentsGen {

	XComponent _X, _Y, _G;

	private String _XX;
	private int _flags;

	public TestXComponentsGen() {}

	public static void setXX(XXNode xx, String s) {
		if (xx.getXComponent() != null) {
			((TestXComponentsGen) xx.getXComponent())._XX = s;
		}
	}

	public static void genXC(XXNode xnode) {
		String name = xnode.getXXName();
		TestXComponentsGen xm = (TestXComponentsGen) xnode.getUserObject();
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

////////////////////////////////////////////////////////////////////////////////
	private final List<XComponent> _YYY = new ArrayList<XComponent>();
	private String _g;
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

	/** Generate XComponents from XDPool.
	 * @param args the command line arguments.
	 */
	public static void main(String... args) {
		String dir = "test/";
		File f = new File(dir);
		if (!f.isDirectory()) {
			System.err.println(f.getAbsolutePath() + "\" is not directory");
			return;
		}
		f = new File(f, "test/xdef/data/test/");
		if (!f.isDirectory()) {
			System.err.println(f.getAbsolutePath() + "\" is not directory");
			return;
		}
		XDPool xp = XDFactory.compileXD(null,
			new File(f, "TestXComponentGen.xdef").getAbsoluteFile(),
			new File(f,	"TestXComponent_Z.xdef").getAbsolutePath());
		try {
			// force following classes to be compiled!
			TestXComponents_C.class.getClass();
			TestXComponents_Y04.class.getClass();
			TestXComponents_Y06Container.class.getClass();
			TestXComponents_Y06Domain.class.getClass();
			TestXComponents_Y06DomainContainer.class.getClass();
			TestXComponents_Y06XCDomain.class.getClass();
			TestXComponents_Y07Operation.class.getClass();
			// generate from xp the class containing the XDPool
			XDFactory.genXDPoolClass(xp, dir, "test.xdef.component.Pool", null);
			// generate XComponents from xp
			ArrayReporter reporter =
				GenXComponent.genXComponent(xp, dir, null, false, false, true);
			// should generate warning XCOMPONENT037 on xdef Y19
			if (reporter.getWarningCount() != 1
				|| !reporter.printToString().contains("W XDEF377")
				|| !reporter.printToString().contains("Y19#A/B/B_1/C/B")) {
				System.err.println("Warning XDEF377 not reported.");
			}
			System.out.println("XComponents generated.");
		} catch (IOException ex) {ex.printStackTrace(System.err);}
	}
}