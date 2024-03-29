package org.xdef.xon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.XDBytes;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Implementation of XonParser for creating XON objects from source.
 * @author Vaclav Trojan
 */
public class XonObjParser implements XonParser {
	private final Stack<Integer> _kinds;
	private final Stack<List<Object>> _arrays;
	private final Stack<Map<String, Object>> _maps;
	private int _kind; // 0..value, 1..array, 2..map
	private final Stack<String> _names;
	private Object _value;
	private final boolean _convertXDBytes; // if XDBytes are conterted to byte[]

	/** Create new instance of XonObjParser.
	 * @param convertXDBytes flag if XDBytes objects are conterted to byte[].
	 */
	public XonObjParser(final boolean convertXDBytes) {
		_kinds = new Stack<>();
		_arrays = new Stack<>();
		_maps = new Stack<>();
		_names = new Stack<>();
		_kinds.push(_kind = 0);
		_convertXDBytes = convertXDBytes;
	}

////////////////////////////////////////////////////////////////////////////////
// XonParser interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Put value to result.
	 * @param value X_Value to be added to result object.
	 */
	public void putValue(XonTools.JValue value) {
		Object o = value.getValue();
		if (_convertXDBytes && o instanceof XDBytes) {
			o = ((XDBytes) o).getBytes();
		}
		switch (_kind) {
			case 1:
				_arrays.peek().add(o);
				break;
			case 2:
				String name = _names.pop();
				_maps.peek().put(name, o);
				break;
			default:
				_value = o;
		}
	}

	@Override
	/** Set name of value pair.
	 * @param name value name.
	 * @return true if the name of pair already exists, otherwise return false.
	 */
	public boolean namedValue(SBuffer name) {
		_names.push(name.getString());
		return _maps.peek().containsKey(name.getString());
	}

	@Override
	/** Array started.
	 * @param pos source position.
	 */
	public void arrayStart(SPosition pos) {
		_kinds.push(_kind = 1);
		_arrays.push(new ArrayList<>());
	}

	@Override
	/** Array ended.
	 * @param pos source position.
	 */
	public void arrayEnd(SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		_value = _arrays.peek();
		_arrays.pop();
		if (_kind == 2) {
			_maps.peek().put(_names.pop(), _value);
		} else if (_kind == 1) {
			_arrays.peek().add(_value);
		}
	}

	@Override
	/** Map started.
	 * @param pos source position.
	 */
	public void mapStart(SPosition pos) {
		_kinds.push(_kind = 2);
		_maps.push(new LinkedHashMap<>());
	}

	@Override
	/** Map ended.
	 * @param pos source position.
	 */
	public void mapEnd(SPosition pos) {
		_kinds.pop();
		_kind = _kinds.peek();
		_value = _maps.peek();
		_maps.pop();
		if (_kind == 2) {
			_maps.peek().put(_names.pop(), _value);
		} else if (_kind == 1) {
			_arrays.peek().add(_value);
		}
	}

	@Override
	/** Processed comment.
	 * @param value SBuffer with the value of comment.
	 */
	public void comment(SBuffer value){/*we ingore it here*/}

	@Override
	/** X-script item parsed, not used methods for XON/JSON parsing
	 * (used in X-definition compiler).
	 * @param name name of item.
	 * @param value value of item.
	 */
	public void xdScript(SBuffer name, SBuffer value) {}

	@Override
	/** Get result of parser.
	 * @return parsed object.
	 */
	public final Object getResult() {return _value;}
}
