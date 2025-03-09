package mytests.xon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.XDConstants;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class TestXXON extends XDTester {
	public TestXXON() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		XXON x;

////////////////////////////////////////////////////////////////////////////////
		try {
			x = XXON.createNull();
			System.out.println("Null");
			System.out.println(x.isNull());
			System.out.println(x.isArray());
			System.out.println(x.isMap());

			x = XXON.createArray();
			System.out.println("Array");
			System.out.println(x.isNull());
			System.out.println(x.isArray());
			System.out.println(x.isMap());

			x = XXON.createMap();
			System.out.println("Map");
			System.out.println(x.isNull());
			System.out.println(x.isArray());
			System.out.println(x.isMap());
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
class XXON {
	private List<Object> _list;
	private Map<String, Object> _map;
	private XXON() {}
	public static XXON createNull() {return new XXON();}
	public static XXON createArray() {
		XXON x = new XXON();
		x._list = new ArrayList<Object>();
		return x;
	 }
	public static XXON createMap() {
		XXON x = new XXON();
		x._map = new LinkedHashMap<String, Object>();
		return x;
	}
	public static XXON createXXON() {
		XXON x = new XXON();
		x._list = new ArrayList<Object>();
		x._map = new LinkedHashMap<String, Object>();
		return x;
	}

	public List<Object> getList() {return _list;}
	public boolean isArray() {return _list != null;}
	public boolean isNull() {return _map == null && _list==null;}
	public boolean add(Object o) {return _list.add(o);}
	public void add(int index, Object o) {_list.add(index, o);}
	public Object get(int index) {return _list.get(index);}
	public Object remove(int index) {return _list.remove(index);}

	public Map<String, Object> getMap() {return _map;}
	public boolean isMap() {return _map != null;}
	public Object put(String key, Object o) {return _map.put(key, o); }
	public Object get(String key) {return _map.get(key); }
	public Object remove(String key) {return _map.remove(key);}
}