package mytests.xon;

/** Interface of XONObject objects (value, map or array).
 * @author Vaclav Trojan
 */
public interface XONObject {
	public final byte XON_VALUE = 0;
	public final byte XON_ARRAY = 1;
	public final byte XON_MAP = 2;
	public final byte XON_ELEMENT = 3;

	public byte getType();
	public Object getObject();
}