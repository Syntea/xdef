package org.xdef.impl.code;

import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.compile.CompileBase;

/** Implements CodeParser with string as the second parameter.
 * @author  Vaclav Trojan
 */
public class CodeParser extends CodeS1 {
	private final XDParser _parser;
	private final String[] _sqParamNames;

	/** Creates a new instance of CodeString.
	 * @param resultType type of result.
	 * @param code code.
	 * @param name name of parser.
	 * @param paramNames Array with names of sequential parameters.
	 */
	public CodeParser(final short resultType, final short code, final String name, final String[] paramNames){
		this(resultType, code, 0, name, paramNames);
	}

	/** Creates a new instance of CodeString.
	 * @param resultType type of result.
	 * @param code code.
	 * @param param integer parameter.
	 * @param name name of parser.
	 * @param paramNames Array with names of sequential parameters.
	 */
	public CodeParser(final short resultType,
		final short code,
		int param,
		final String name,
		final String[] paramNames) {
		super(resultType, code, param, name);
		_parser = CompileBase.getParser(name);
		_sqParamNames = paramNames;
	}

	/** Get the list of names of sequential parameters.
	 * @return list of strings.
	 */
	public String[] getSqParamNames() {return _sqParamNames;}

	/** Get parser from this object.
	 * @return parser.
	 */
	public XDParser getParser() {return _parser;}

	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeParser)) {
			return false;
		}
		CodeParser x = (CodeParser) o;
		return getCode() == x.getCode() && getParam() == x.getParam()
			&& (_parser == null && x._parser == null || _parser != null && _parser.equals(x._parser));
	}
	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}