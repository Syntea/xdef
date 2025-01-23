package org.xdef.impl.compile;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Provides model of macro definition used in Xdefinitions script.
 * @author Vaclav Trojan
 */
public final class XScriptMacro {
	private final String _name;
	private final String _value;
	private final List<String> _paramNames;
	private final String[] _paramValues;
	private int[] _references;

	/** Create the new object ScriptMacro.
	 * @param name name of macro.
	 * @param defName name of Xdefinition.
	 * @param params table with macro parameters.
	 * @param body source of macro body.
	 * @param reporter reporter where errors are recorded (if this parameter
	 * is <i>null</i> then an runtime exception is thrown when an error
	 * occurs).
	 */
	public XScriptMacro(final String name,
		final String defName,
		final Map<String, String> params,
		final SBuffer body,
		final ReportWriter reporter) {
		SBuffer ibody = body == null ? new SBuffer("") : body;
		ReportWriter rwi = reporter == null ? new ArrayReporter() : reporter;
		_name = (defName != null ? defName + '#' + name : name).intern();
		int numParams = params.size();
		_paramNames = new ArrayList<>(params.keySet());
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
				p.error(XDEF.XDEF490); //Reference to macro parameter is not integer
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
	public int hashCode() {return _name.hashCode();}

	@Override
	/** This enables to use the method <i>indexOf(anObject)</i>.
	 * @param anObject The object to be compared with this one.
	 * @return <i>true</i> if and only if the object is considered to be equal with this one.
	 */
	public boolean equals(final Object anObject) {
		return (anObject instanceof XScriptMacro) ? ((XScriptMacro) anObject)._name.equals(_name) : false;
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
	public final List<String> getParamNames() {return _paramNames;}

	/** Get array with values of parameters,
	 * @return array with values of parameters,
	 */
	public final String[] getParamValues()  {return _paramValues;}

	/** Get string with value of macro.
	 * @return string with value of macro.
	 */
	public final String getParamValue()  {return _value;}
}