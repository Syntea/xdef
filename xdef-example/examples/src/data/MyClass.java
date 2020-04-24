package data;


import org.xdef.component.XComponent;
import org.xdef.proc.XXData;

public class MyClass {

	/** Counter of the name "Smith". */
	private int _numSmith = 0;

	public static void isSmith(XXData x) {
		if ("Smith".equals(x.getTextValue())) { // if name is "Smith"
			// the X-component Town extends MyClass
			XComponent y = x.getRootXXElement().getXComponent();
			((MyClass) y)._numSmith++;
		}
	}

	/** Return number of the name "Smith". */
	public int getNumSmith() {return _numSmith;}
}