package org.xdef.xon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Implementation of XonParser for creating XON/JSON objects from source.
 * @author Vaclav Trojan
 */
public class XonObjParser implements XonParser {
	private final Stack<Integer> _kinds = new Stack<Integer>();
	private final Stack<List<Object>> _arrays = new Stack<List<Object>>();
	private final Stack<Map<String, Object>> _maps =
		new Stack<Map<String, Object>>();
	private int _kind; // 0..value, 1..array, 2..map
	private final Stack<String> _names = new Stack<String>();
	private Object _value;

	public XonObjParser() { _kinds.push(_kind = 0); }

////////////////////////////////////////////////////////////////////////////////
// XonParser interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Put value to result.
	 * @param value X_Value to be added to result object.
	 * @return null or name of pair if value pair already exists in
	 * the currently processed map.
	 */
	public String putValue(XonTools.JValue value) {
		if (_kind == 1) {
			_arrays.peek().add(value.getValue());
		} else if (_kind == 2) {
			String name = _names.pop();
			if (_maps.peek().put(name, value.getValue()) != null) {
				return name;
			}
		} else {
			_value = value.getValue();
		}
		return null;
	}
	@Override
	/** Set name of value pair.
	 * @param name value name.
	 */
	public void namedValue(SBuffer name) {_names.push(name.getString());}
	@Override
	/** Array started.
	 * @param pos source position.
	 */
	public void arrayStart(SPosition pos) {
		_kinds.push(_kind = 1);
		_arrays.push(new ArrayList<Object>());
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
		_maps.push(new LinkedHashMap<String, Object>());
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