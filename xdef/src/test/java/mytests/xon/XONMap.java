package mytests.xon;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xdef.xon.XonUtils;

/** XONObject map
 * @author Vaclav Trojan
 */
public class XONMap extends LinkedHashMap<String, Object> implements XONObject {

	XONMap() {super();}

	@Override
	public byte getType() {return XON_MAP;}
	@Override
	public Map<String, Object> getObject() {return this;}

	@Override
	public String toString() {return XonUtils.toXonString(this);}
	@Override
	public int hashCode() {return super.isEmpty()? 0 : super.hashCode();}
	@Override
	public boolean equals(Object x) {
		return x instanceof Map ? XonUtils.xonEqual(x, this) : false;
	}

	@Override
	public Object get(Object key) {return super.get(key);}
	@Override
	public Object put(String key, Object value) {return super.put(key, value);}
	@Override
	public Object remove(Object key) {return super.remove(key);}
	@Override
	public Set<String> keySet() {return super.keySet();}
	@Override
	public Set<Entry<String, Object>> entrySet() {return super.entrySet();}
}