package org.xdef.impl;

import org.xdef.model.XMOccurrence;
import static org.xdef.model.XMOccurrence.FIXED;
import org.xdef.sys.StringParser;

/** Contains minimum and maximum occurrence requirement.
 * @author Trojan
 */
public class XOccurrence implements XMOccurrence {
	/** Object is accepted but ignored. */
	public static final int IGNORE = -1;
	/** Object is illegal. */
	public static final int ILLEGAL = -2;
	/** Object is undefined. */
	public static final int UNDEFINED = -3;

	/** Minimum. */
	private int _min;
	/** Maximum. */
	private int _max;

	/** Creates new instance of occurrence. */
	public XOccurrence() {setUnspecified();}

	/** Creates a copy of object given by argument.
	 * @param occ object to be cloned.
	 */
	public XOccurrence(final XOccurrence occ) {_min=occ._min; _max=occ._max;}

	/** Creates new instance of occurrence.
	 * @param min minimum.
	 * @param max maximum.
	 */
	public XOccurrence(final int min, final int max) {_min = min; _max = max;}

	/** Creates new instance of occurrence from source form.
	 * @param source source value of occurrence.
	 */
	public XOccurrence(final String source) {
		if (source == null) {
			setUnspecified();
			return;
		}
		StringParser sp = new StringParser(source);
		sp.isSpaces();
		if (sp.isToken("occurs")) {
			sp.isSpaces();
		}
		if (sp.isToken("required")) {
			setRequired();
		} else if (sp.isToken("optional") || sp.isChar('?')) {
			setOptional();
		} else if (sp.isToken("illegal")) {
			setIllegal();
		} else if (sp.isToken("ignore")) {
			setIgnore();
		} else if (sp.isChar('*')) {
			setUnbounded();
		} else if (sp.isChar('+')) {
			_min = 1; _max = Integer.MAX_VALUE;
		} else if (sp.isInteger()) {
			_min = sp.getParsedInt();
			sp.isSpaces();
			if (sp.isToken("..")) {
				sp.isSpaces();
				if (sp.isInteger()) {
					_max = sp.getParsedInt();
				} else { //if (isChar('*'))
					_max = Integer.MAX_VALUE;
				}
			} else {
				_max = _min;
			}
		} else {
			setUnspecified();
		}
	}

////////////////////////////////////////////////////////////////////////////////
// XMOccurrence interface
////////////////////////////////////////////////////////////////////////////////

	/** Get min occurrence.
	 * @return min occurrence.
	 */
	@Override
	public final int minOccurs() {return _min;}

	/** Get max occurrence.
	 * @return max occurrence.
	 */
	@Override
	public final int maxOccurs() {return _max;}

	/** Return true if value of occurrence had been specified.
	 * @return true if and only if occurrence is specified.
	 */
	@Override
	public final boolean isSpecified() {return _min != UNDEFINED;}


	/** Return true if value of occurrence is set as illegal.
	 * @return true if and only if occurrence is set as illegal.
	 */
	@Override
	public final boolean isIllegal() {return _min == ILLEGAL;}

	/** Return true if value of occurrence is set as ignored.
	 * @return true if and only if occurrence is set as ignored.
	 */
	@Override
	public final boolean isIgnore() {return _min == IGNORE;}

	/** Return true if value of occurrence is set as fixed.
	 * @return true if and only if occurrence is set as fixed.
	 */
	@Override
	public final boolean isFixed() {return _min == XData.FIXED && _max == 1;}

	/** Return true if value of occurrence is set as required.
	 * @return true if and only if occurrence is set as required.
	 */
	@Override
	public final boolean isRequired() {return _min == 1 && _max == 1;}

	/** Return true if value of occurrence is set as optional.
	 * @return true if and only if occurrence is set as optional.
	 */
	@Override
	public final boolean isOptional() {return _min == 0 && _max == 1;}

	/** Return true if value of occurrence is set as unbounded.
	 * @return true if and only if occurrence is set as unbounded.
	 */
	@Override
	public final boolean isUnbounded() {return _min == 0 && _max == Integer.MAX_VALUE;}

	/** Return true if minimum is greater then 0 and maximum is unbounded.
	 * @return true if and only if minimum is greater then 0 and
	 * maximum is unbounded..
	 */
	@Override
	public final boolean isMaxUnlimited() {return _min > 0 && _max == Integer.MAX_VALUE;}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {return toString(true);}

	@Override
	public int hashCode() {return 31 * (_min + 217) + _max;} //217 == 7 * 31

	@Override
	public boolean equals(final Object o) {
		return (o instanceof XMOccurrence) ? equals((XMOccurrence) o) : false;
	}

////////////////////////////////////////////////////////////////////////////////
// Methods added to XMOccurrence
////////////////////////////////////////////////////////////////////////////////

	/** Compare with other XMOccurrence.
	 * @param x XMOccurrence to be compared.
	 * @return true if and only if the occurrence value from the argument x is equal to this object.
	 */
	public final boolean equals(final XMOccurrence x) {return _min == x.minOccurs() && _max == x.maxOccurs();}

	/** Get string with canonized form of occurrence specification.
	 * @param isValue if <i>true</i> the script describes a value of
	 * an attribute or of a text node, otherwise it is form of en element
	 * or a sequence.
	 * @return string with canonized form of the occurrence.
	 */
	public final String toString(final boolean isValue) {
		if (isRequired() || !isSpecified()) {
			return isValue ? "required" : "occurs 1";
		} else if (isFixed()) {
			return "fixed";
		} else if (isIgnore()) {
			return "ignore";
		} else if (isIllegal()) {
			return "illegal";
		} else if (isOptional()) {
			return isValue ? "optional" : "occurs ?";
		} else {
			return "occurs " + (_max==Integer.MAX_VALUE ? _min==0 ? "*"
				: _min==1 ? "+" : (_min + "..*") : (_min==_max ? String.valueOf(_min) : _min + ".." + _max));
		}
	}

	/** Set min occurrence.
	 * @param min value of minimal occurrence.
	 */
	public final void setMinOccur(final int min) {_min = min;}

	/** Set max occurrence.
	 * @param max value of maximal occurrence.
	 */
	public final void setMaxOccur(final int max) {_max = max;}

	/** Set occurrence values.
	 * @param occ occurrence object from which values are imported.
	 */
	public final void setOccurrence(final XMOccurrence occ) {_min = occ.minOccurs(); _max = occ.maxOccurs();}

	/** Set occurrence from parameters.
	 * @param min minimum.
	 * @param max maximum.
	 */
	public final void setOccurrence(final int min,final int max) {_min = min; _max = max;}

	/** Set value of occurrence as illegal. */
	public final void setIllegal() {_min = ILLEGAL; _max = 0;}

	/** Set value of occurrence as ignored. */
	public final void setIgnore() {_min = IGNORE; _max=Integer.MAX_VALUE;}

	/** Set value of occurrence as fixed. */
	public final void setFixed() {_min = FIXED; _max = 1;}

	/** Set value of occurrence as required. */
	public final void setRequired() {_min = _max = 1;}

	/** Set value of occurrence as optional. */
	public final void setOptional() {_min = 0; _max = 1;}

	/** Set value of occurrence as unspecified. */
	public final void setUnspecified() {_min = UNDEFINED; _max = 0;}

	/** Set value of occurrence as unbounded. */
	public final void setUnbounded() {_min = 0; _max = Integer.MAX_VALUE;}
}