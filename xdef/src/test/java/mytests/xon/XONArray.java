package mytests.xon;

import java.util.ArrayList;
import java.util.List;
import org.xdef.xon.XonUtils;

/** XONObject array
 * @author Vaclav Trojan
 */
public class XONArray extends ArrayList<Object> implements XONObject {

	XONArray() {super();}

	@Override
	public byte getType() {return XON_ARRAY;}
	@Override
	public List<Object> getObject() {return this;}

	@Override
	public String toString() {return XonUtils.toXonString(this);}
	@Override
	public int hashCode() {return super.isEmpty()? 0 : super.hashCode();}
	@Override
	public boolean equals(Object x) {
		return x instanceof List ? XonUtils.xonEqual(x, this) : false;
	}

	@Override
	public Object get(int index) {return super.get(index);}
	@Override
	public Object set(int index, Object e) {return super.set(index, e);}
	@Override
	public boolean add(Object e) {return super.add(e);}
	@Override
	public boolean remove(Object e) {return super.remove(e);}
	@Override
	public int indexOf(Object e) {return super.indexOf(e);}
	@Override
	public Object[] toArray() {return super.toArray();}
}