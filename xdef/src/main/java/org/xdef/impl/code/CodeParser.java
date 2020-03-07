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
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param spar The parameter string.
	 * @param sqParamNames Array with names of sequential parameters.
	 */
	public CodeParser(final short resultType,
		final short code,
		final String spar,
		final String[] sqParamNames) {
		this(resultType, code, 0, spar, sqParamNames);
	}

	/** Creates a new instance of CodeString.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param param The integer parameter.
	 * @param spar The parameter string.
	 * @param sqParamNames Array with names of sequential parameters.
	 */
	public CodeParser(final short resultType,
		final short code,
		int param,
		final String spar,
		final String[] sqParamNames) {
		super(resultType, code, param, spar);
		_parser = CompileBase.getParser(spar);
		_sqParamNames = sqParamNames;
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
			&& (_parser == null && x._parser == null
				|| _parser != null && _parser.equals(x._parser));
	}
	@Override
	public String toString() {return CodeDisplay.codeToString(this);}
}