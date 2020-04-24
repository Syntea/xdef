package org.xdef.impl.compile;

import org.xdef.msg.XDEF;
import org.xdef.impl.XDReader;
import org.xdef.impl.XDWriter;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** Provides model of macro definition used in X-definitions script.
 * @author Vaclav Trojan
 */
public final class XScriptMacro {
	private final String _name;
	private ArrayList<String> _paramNames;
	private String[] _paramValues;
	private String _value;
	private int[] _references;
	private SPosition _sourcePosition;

	private XScriptMacro(final String name) {_name = name.intern();}

	/** Create the new object ScriptMacro.
	 * @param name name of macro.
	 * @param defName name of X-definition.
	 * @param params table with macro parameters.
	 * @param body source of macro body.
	 * @param reporter reporter where errors are recorded (if this parameter
	 * is <tt>null</tt> then an runtime exception is thrown when an error
	 * occurs).
	 */
	public XScriptMacro(final String name,
		final String defName,
		final Map<String, String> params,
		final SBuffer body,
		final ReportWriter reporter) {
		SBuffer ibody = body == null ? new SBuffer("") : body;
		ReportWriter rwi =
			reporter == null ? new ArrayReporter() : reporter;
		_name = (defName != null ? defName + '#' + name : name).intern();
		int numParams = params.size();
		_paramNames = new ArrayList<String>(params.keySet());
		_paramValues = new String[numParams];
		for (int i = 0, j = _paramNames.size(); i < j; i++) {
			_paramValues[i] = params.get(_paramNames.get(i));
		}
		_references = new int[0];
		StringParser p = new StringParser(ibody.getString(), rwi);
		int lastPos = p.getIndex();
		StringBuilder sb = new StringBuilder();
		while (p.findTokenAndSkip("#{")) {
			if (p.getIndex() > 2 && p.getCharAtPos(p.getIndex() - 3) == '\\') {
				continue;
			}
			sb.append(p.getBufferPart(lastPos,p.getIndex() - 2));
			if (!isName(p)) {
				//Reference to macro parameter is not integer
				p.error(XDEF.XDEF490);
				break;
			}
			String paramName = p.getParsedString();
			int index = _paramNames.indexOf(paramName);
			if (!p.isChar('}')) {
				p.error(XDEF.XDEF491);//Incorrect reference to macro parameter
				break;
			}
			lastPos = p.getIndex();
			if (index < 0) {
				p.error(XDEF.XDEF492); //Incorrect macro parameter index
				break;
			} else {
				//add reference of parameter
				int len = _references.length;
				if (len > 0) {
					int[] w = _references;
					_references = new int[len + 2];
					System.arraycopy(w, 0, _references, 0, len);
					_references[len] = sb.length();
					_references[len + 1] = index;
				} else {
					_references = new int[] {sb.length(), index};
				}
			}
		}
		if (lastPos < p.getEndBufferIndex()) {
			sb.append(p.getBufferPartFrom(lastPos));
		}
		_value = sb.toString();
		if (rwi.errors() && reporter == null) {
			rwi.checkAndThrowErrors();
		}
	}

	private boolean isName(final StringParser p) {
		char c;
		if ((c = p.isLetter()) == SParser.NOCHAR) {
			if (p.isChar('_')) {
				c = '_';
			} else {
				return false;
			}
		}
		StringBuilder sb = new StringBuilder(String.valueOf(c));
		for (;;) {
			if ((c = p.isLetterOrDigit()) == SParser.NOCHAR) {
				if (p.isChar('_')) {
					sb.append('_');
				} else {
					p.setParsedString(sb.toString());
					return true;
				}
			} else {
				sb.append(c);
			}
		}
	}

	@Override
	/** Returns hash code of the object. */
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	/** This enables to use the method <tt>indexOf(anObject)</tt>.
	 * @param anObject The object to be compared with this one.
	 * @return <tt>true</tt> if and only if the object is considered to be
	 * equal with this one.
	 */
	public boolean equals(final Object anObject) {
		return (anObject instanceof XScriptMacro) ?
			((XScriptMacro) anObject)._name.equals(_name) : false;
	}

	/** Expand macro reference
	 * @param params parameters of macro reference.
	 * @return string with expanded macro.
	 */
	public final String expand(final String... params) {
		if (_references.length == 0) {
			return _value; // no parameters
		}
		StringBuilder sb = new StringBuilder();
		int lastPos = 0;
		for (int i = 0; i < _references.length;) {
			sb.append(_value.substring(lastPos, lastPos=_references[i++]));
			sb.append(params[_references[i++]]);
		}
		if (lastPos < _value.length()) {
			sb.append(_value.substring(lastPos));
		}
		return sb.toString();
	}

	/** Get macro name.
	 * @return macro name.
	 */
	public String getName() {return _name;}

	/** Get list of names of macro parameters.
	 * @return list of names of macro parameters.
	 */
	public final ArrayList<String> getParamNames() {return _paramNames;}

	public final String[] getParamValues()  {return _paramValues;}

	void writeScriptMacro(XDWriter xw) throws IOException {
		xw.writeString(_name);
		xw.writeString(_value);
		int len = _paramNames.size();
		xw.writeInt(len);
		for (int i = 0; i < len; i++) {
			xw.writeString(_paramNames.get(i));
		}
		len = _paramValues.length;
		xw.writeInt(len);
		for (int i = 0; i < len; i++) {
			xw.writeString(_paramValues[i]);
		}
		len = _references.length;
		xw.writeInt(len);
		for (int i = 0; i < len; i++) {
			xw.writeInt(_references[i]);
		}
		xw.writeSPosition(_sourcePosition);
	}

	static XScriptMacro readScriptMacro(XDReader xr) throws IOException {
		XScriptMacro result = new XScriptMacro(xr.readString());
		result._value = xr.readString();
		int len = xr.readInt();
		result._paramNames = new ArrayList<String>(len);
		for (int i = 0; i < len; i++) {
			result._paramNames.add(xr.readString());
		}
		len = xr.readInt();
		result._paramValues = new String[len];
		for (int i = 0; i < len; i++) {
			result._paramValues[i] = xr.readString();
		}
		len = xr.readInt();
		result._references = new int[len];
		for (int i = 0; i < len; i++) {
			result._references[i] = xr.readInt();
		}
		result._sourcePosition = xr.readSPosition();
		return result;
	}

}