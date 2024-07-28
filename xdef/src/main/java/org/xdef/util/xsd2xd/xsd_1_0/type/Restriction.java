package org.xdef.util.xsd2xd.xsd_1_0.type;

import org.xdef.xml.KDOMUtils;
import org.xdef.util.xsd2xd.Utils;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.*;

/** Represents restriction of XML schema simple type.
 * @author Ilia Alexandrov
 */
public class Restriction extends Specification {

	/** Fraction digits facet constant. */
	public static final short FRACTION_DIGITS = 0;
	/** Length facet constant. */
	public static final short LENGTH = 1;
	/** Max exclusive facet constant. */
	public static final short MAX_EXCLUSIVE = 2;
	/** Max inclusive facet constant. */
	public static final short MAX_INCLUSIVE = 3;
	/** Max length facet constant. */
	public static final short MAX_LENGTH = 4;
	/** Min exclusive facet constant. */
	public static final short MIN_EXCLUSIVE = 5;
	/** Min inclusive facet constant. */
	public static final short MIN_INCLUSIVE = 6;
	/** Min length facet constant. */
	public static final short MIN_LENGTH = 7;
	/** Total digits facet constant. */
	public static final short TOTAL_DIGITS = 8;
	/** White space facet constant. */
	public static final short WHITE_SPACE = 9;
	/** Base type of restriction. */
	private Type _base;
	/** Final set of enumeration facets. */
	private final Set<String> _enumerations = new HashSet<String>();
	/** Final set of pattern facets. */
	private final Set<String> _patterns = new HashSet<String>();
	/** Array of final restriction facets. */
	private final String[] _restrictions = new String[10];
	/** Fraction digits restriction facet. */
	private String _fractionDigits;
	/** Length restriction facet. */
	private String _length;
	/** MAx exclusive restriction facet. */
	private String _maxExclusive;
	/** Max inclusive restriction facet. */
	private String _maxInclusive;
	/** MAx length restriction facet. */
	private String _maxLength;
	/** Min exclusive restriction facet. */
	private String _minExclusive;
	/** Min inclusive restriction facet. */
	private String _minInclusive;
	/** Min length restriction facet. */
	private String _minLength;
	/** Total digits restriction facet. */
	private String _totalDigits;
	/** White space restriction facet. */
	private String _whiteSpace;

	/** Creates instance of restriction.
	 * @param restrictionElement    restriction declaration element.
	 * @param schemaURL URL of schema containing restriction.
	 * @param schemaElements all schema elements.
	 */
	public Restriction(Element restrictionElement,
		URL schemaURL,
		Map<URL, Element> schemaElements) {
		String base = restrictionElement.getAttribute("base");
		if (!"".equals(base)) {
			_base = Type.getType(base,
				restrictionElement, schemaURL, schemaElements);
		} else {
			Element simpleTypeElement = KDOMUtils.firstElementChildNS(
				restrictionElement, Utils.NSURI_SCHEMA, "simpleType");
			_base = new SimpleType(simpleTypeElement, schemaURL,schemaElements);
		}
		initRerstrictions(restrictionElement);
	}

	/** Inits all restrictions applicable to simple type.
	 * @param restrictionElement restriction element that contains restrictions.
	 */
	private void initRerstrictions(Element restrictionElement) {
		NodeList restrictions = KDOMUtils.getChildElementsNS(restrictionElement,
			Utils.NSURI_SCHEMA,
			new String[]{"enumeration", "fractionDigits", "length",
				"maxExclusive", "maxInclusive", "maxLength", "minExclusive",
				"minInclusive", "minLength", "pattern",
				"totalDigits", "whiteSpace"});
		for (int i = 0; i < restrictions.getLength(); i++) {
			Element restriction = (Element) restrictions.item(i);
			String name = restriction.getLocalName();
			String value = restriction.getAttribute("value");
			switch (name) {
				case "enumeration": _enumerations.add(value); break;
				case"fractionDigits":_restrictions[FRACTION_DIGITS]=value;break;
				case "length": _restrictions[LENGTH] = value; break;
				case "maxExclusive": _restrictions[MAX_EXCLUSIVE] = value;break;
				case "maxInclusive": _restrictions[MAX_INCLUSIVE] = value;break;
				case "maxLength": _restrictions[MAX_LENGTH] = value; break;
				case "minExclusive": _restrictions[MIN_EXCLUSIVE] = value;break;
				case "minInclusive": _restrictions[MIN_INCLUSIVE] = value;break;
				case "minLength": _restrictions[MIN_LENGTH] = value; break;
				case "pattern": _patterns.add(value); break;
				case "totalDigits": _restrictions[TOTAL_DIGITS] = value; break;
				case "whiteSpace": _restrictions[WHITE_SPACE] = value; break;
			}
		}
	}

	/** Creates instance of restriction with restrictions from given restriction
	 * element and with given base type.
	 * @param restrictionElement restriction elements that contains restrictions.
	 * @param base base type.
	 */
	public Restriction(Element restrictionElement, Type base) {
		_base = base;
		initRerstrictions(restrictionElement);
	}

	/** Base type setter.
	 * @param base base type.
	 */
	public void setBase(Type base) {_base = base;}

	/** Base type getter.
	 * @return base type.
	 */
	public Type getBase() {return _base;}

	/** Restrictions array getter.
	 * @return restrictions array.
	 */
	public String[] getRestrictions() {return _restrictions;}

	/** Final set of enumerations.
	 * @return set of enumerations.
	 */
	public Set<String> getEnumerations() {return _enumerations;}

	/** Fraction digits facet getter.
	 * @return fraction digits.
	 */
	public String getFractionDigits() {return _fractionDigits;}

	/** Length facet getter.
	 * @return length facet.
	 */
	public String getLength() {return _length;}

	/** Max exclusive facet getter.
	 * @return max exclusive facet.
	 */
	public String getMaxExclusive() {return _maxExclusive;}

	/** Max inclusive facet getter.
	 * @return max inclusive facet.
	 */
	public String getMaxInclusive() {return _maxInclusive;}

	/** Max length facet getter.
	 * @return max length facet.
	 */
	public String getMaxLength() {return _maxLength;}

	/** Min exclusive facet getter.
	 * @return min exclusive facet.
	 */
	public String getMinExclusive() {return _minExclusive;}

	/** Min inclusive facet getter.
	 * @return min inclusive facet.
	 */
	public String getMinInclusive() {return _minInclusive;}

	/** Min length facet getter.
	 * @return min length facet.
	 */
	public String getMinLength() {return _minLength;}

	/** Pattern facets set getter.
	 * @return pattern facets.
	 */
	public Set<String> getPatterns() {return _patterns;}

	/** Total digits facet getter.
	 * @return total digits facet.
	 */
	public String getTotalDigits() {return _totalDigits;}

	/** White space facet getter.
	 * @return white space facet.
	 */
	public String getWhiteSpace() {return _whiteSpace;}

	@Override
	public String getTypeMethod() {
		if (_base instanceof SimpleType) {
			Stack<Specification> stack = getSpecificationStack();
			if (stack.peek() instanceof Restriction) {
				FinalRestriction finalRestriction =
					new FinalRestriction((Restriction) stack.pop());
				while (!stack.isEmpty()) {
					finalRestriction.addRestriction((Restriction) stack.pop());
				}
				return finalRestriction.getTypeMethod();
			}
			return stack.pop().getTypeMethod();
		}
		FinalRestriction finalRestriction = new FinalRestriction(this);
		return finalRestriction.getTypeMethod();
	}

	/** Returns stack of specifications.
	 * @return  stack of specifications.
	 */
	private Stack<Specification> getSpecificationStack() {
		Stack<Specification> stack = new Stack<Specification>();
		return getSpecificationStack(stack, this);
	}

	/** Returns specification stack. Is called recursively until base type
	 * or unsupported restriction is reached.
	 * @param stk                  stack of specifications.
	 * @param currentSpecification current processed specification.
	 * @return                     stack of specifications.
	 */
	private Stack<Specification> getSpecificationStack(Stack<Specification> stk,
		Specification currentSpecification) {
		if (stk.contains(currentSpecification)) {
			throw new RuntimeException(
				"Specifications are cycling, can not get specification stack");
		}
		//pridani specifikace do zasobniku
		stk.push(currentSpecification);
		//specification is not a restriction
		if (!(currentSpecification instanceof Restriction)) {
			throw new RuntimeException(
				"Restriction on list or union is not supported yet");
		}
		Restriction restriction = (Restriction) currentSpecification;
		if (restriction.getBase() instanceof BaseType) {
			return stk;
		}
		return getSpecificationStack(stk,
			((SimpleType) restriction.getBase()).getSpecification());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Restriction other = (Restriction) obj;
		if (_base!=other._base && (_base==null || !_base.equals(other._base))) {
			return false;
		}
		if (_enumerations != other._enumerations && (_enumerations == null
			|| !_enumerations.equals(other._enumerations))) {
			return false;
		}
		if (_patterns != other._patterns && (_patterns == null
			|| !_patterns.equals(other._patterns))) {
			return false;
		}
		if ((_fractionDigits == null) ? (other._fractionDigits != null)
			: !_fractionDigits.equals(other._fractionDigits)) {
			return false;
		}
		if ((_length == null) ?
			(other._length != null) : !_length.equals(other._length)) {
			return false;
		}
		if ((_maxExclusive == null) ? (other._maxExclusive != null) :
			!_maxExclusive.equals(other._maxExclusive)) {
			return false;
		}
		if ((_maxInclusive == null) ? (other._maxInclusive != null) :
			!_maxInclusive.equals(other._maxInclusive)) {
			return false;
		}
		if ((_maxLength == null) ? (other._maxLength != null) :
			!_maxLength.equals(other._maxLength)) {
			return false;
		}
		if ((_minExclusive == null) ? (other._minExclusive != null) :
			!_minExclusive.equals(other._minExclusive)) {
			return false;
		}
		if ((_minInclusive == null) ? (other._minInclusive != null) :
			!_minInclusive.equals(other._minInclusive)) {
			return false;
		}
		if ((_minLength == null) ? (other._minLength != null) :
			!_minLength.equals(other._minLength)) {
			return false;
		}
		if ((_totalDigits == null) ? (other._totalDigits != null) :
			!_totalDigits.equals(other._totalDigits)) {
			return false;
		}
		return !((_whiteSpace == null) ? (other._whiteSpace != null) :
			!_whiteSpace.equals(other._whiteSpace));
	}
	@Override
	public int hashCode() {
		int hash = (_base != null ? _base.hashCode() : 0);
		hash = 53 * hash + (_enumerations != null ? _enumerations.hashCode():0);
		hash = 53 * hash + (_patterns != null ? _patterns.hashCode() : 0);
		hash = 53 * hash + (_fractionDigits != null
			? _fractionDigits.hashCode() : 0);
		hash = 53 * hash + (_length != null ? _length.hashCode() : 0);
		hash = 53 * hash + (_maxExclusive != null ? _maxExclusive.hashCode():0);
		hash = 53 * hash + (_maxInclusive != null ? _maxInclusive.hashCode():0);
		hash = 53 * hash + (_maxLength != null ? _maxLength.hashCode() : 0);
		hash = 53 * hash + (_minExclusive != null ? _minExclusive.hashCode():0);
		hash = 53 * hash + (_minInclusive != null ? _minInclusive.hashCode():0);
		hash = 53 * hash + (_minLength != null ? _minLength.hashCode() : 0);
		hash = 53 * hash + (_totalDigits != null ? _totalDigits.hashCode() : 0);
		hash = 53 * hash + (_whiteSpace != null ? _whiteSpace.hashCode() : 0);
		return hash;
	}
	@Override
	public String toString() {return "Restriction [base="+_base.toString()+"]";}
}