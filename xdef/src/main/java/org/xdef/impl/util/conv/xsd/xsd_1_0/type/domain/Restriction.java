package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
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

	/** Creates empty restriction. */
	private Restriction() {}

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
			Element simpleTypeElement = KXmlUtils.firstElementChildNS(
				restrictionElement, Utils.NSURI_SCHEMA, "simpleType");
			_base = new SimpleType(simpleTypeElement, schemaURL,schemaElements);
		}
		initRerstrictions(restrictionElement);
	}

	/** Inits all restrictions applicable to simple type.
	 * @param restrictionElement restriction element that contains restrictions.
	 */
	private void initRerstrictions(Element restrictionElement) {
		NodeList restrictions = KXmlUtils.getChildElementsNS(restrictionElement,
			Utils.NSURI_SCHEMA,
			new String[]{"enumeration", "fractionDigits", "length",
				"maxExclusive", "maxInclusive", "maxLength", "minExclusive",
				"minInclusive", "minLength", "pattern",
				"totalDigits", "whiteSpace"});
		for (int i = 0; i < restrictions.getLength(); i++) {
			Element restriction = (Element) restrictions.item(i);
			String name = restriction.getLocalName();
			String value = restriction.getAttribute("value");
			if ("enumeration".equals(name)) {
				_enumerations.add(value);
			} else if ("fractionDigits".equals(name)) {
				_restrictions[FRACTION_DIGITS] = value;
			} else if ("length".equals(name)) {
				_restrictions[LENGTH] = value;
			} else if ("maxExclusive".equals(name)) {
				_restrictions[MAX_EXCLUSIVE] = value;
			} else if ("maxInclusive".equals(name)) {
				_restrictions[MAX_INCLUSIVE] = value;
			} else if ("maxLength".equals(name)) {
				_restrictions[MAX_LENGTH] = value;
			} else if ("minExclusive".equals(name)) {
				_restrictions[MIN_EXCLUSIVE] = value;
			} else if ("minInclusive".equals(name)) {
				_restrictions[MIN_INCLUSIVE] = value;
			} else if ("minLength".equals(name)) {
				_restrictions[MIN_LENGTH] = value;
			} else if ("pattern".equals(name)) {
				_patterns.add(value);
			} else if ("totalDigits".equals(name)) {
				_restrictions[TOTAL_DIGITS] = value;
			} else if ("whiteSpace".equals(name)) {
				_restrictions[WHITE_SPACE] = value;
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
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Restriction other = (Restriction) obj;
		if (this._base != other._base && (this._base == null
			|| !this._base.equals(other._base))) {
			return false;
		}
		if (this._enumerations != other._enumerations
			&& (this._enumerations == null
			|| !this._enumerations.equals(other._enumerations))) {
			return false;
		}
		if (this._patterns != other._patterns && (this._patterns == null
			|| !this._patterns.equals(other._patterns))) {
			return false;
		}
		if ((this._fractionDigits == null) ?
			(other._fractionDigits != null) :
			!this._fractionDigits.equals(other._fractionDigits)) {
			return false;
		}
		if ((this._length == null) ?
			(other._length != null) :
			!this._length.equals(other._length)) {
			return false;
		}
		if ((this._maxExclusive == null) ?
			(other._maxExclusive != null) :
			!this._maxExclusive.equals(other._maxExclusive)) {
			return false;
		}
		if ((this._maxInclusive == null) ?
			(other._maxInclusive != null) :
			!this._maxInclusive.equals(other._maxInclusive)) {
			return false;
		}
		if ((this._maxLength == null) ?
			(other._maxLength != null) :
			!this._maxLength.equals(other._maxLength)) {
			return false;
		}
		if ((this._minExclusive == null) ?
			(other._minExclusive != null) :
			!this._minExclusive.equals(other._minExclusive)) {
			return false;
		}
		if ((this._minInclusive == null) ?
			(other._minInclusive != null) :
			!this._minInclusive.equals(other._minInclusive)) {
			return false;
		}
		if ((this._minLength == null) ?
			(other._minLength != null) :
			!this._minLength.equals(other._minLength)) {
			return false;
		}
		if ((this._totalDigits == null) ?
			(other._totalDigits != null) :
			!this._totalDigits.equals(other._totalDigits)) {
			return false;
		}
		return !((this._whiteSpace == null) ?
			(other._whiteSpace != null) :
			!this._whiteSpace.equals(other._whiteSpace));
	}

	@Override
	public int hashCode() {
		int hash = (this._base != null ? this._base.hashCode() : 0);
		hash = 53 * hash + (this._enumerations != null ?
			this._enumerations.hashCode() : 0);
		hash = 53 * hash + (this._patterns != null ?
			this._patterns.hashCode() : 0);
		hash = 53 * hash + (this._fractionDigits != null ?
			this._fractionDigits.hashCode() : 0);
		hash = 53 * hash + (this._length != null ?
			this._length.hashCode() : 0);
		hash = 53 * hash + (this._maxExclusive != null ?
			this._maxExclusive.hashCode() : 0);
		hash = 53 * hash + (this._maxInclusive != null ?
			this._maxInclusive.hashCode() : 0);
		hash = 53 * hash + (this._maxLength != null ?
			this._maxLength.hashCode() : 0);
		hash = 53 * hash + (this._minExclusive != null ?
			this._minExclusive.hashCode() : 0);
		hash = 53 * hash + (this._minInclusive != null ?
			this._minInclusive.hashCode() : 0);
		hash = 53 * hash + (this._minLength != null ?
			this._minLength.hashCode() : 0);
		hash = 53 * hash + (this._totalDigits != null ?
			this._totalDigits.hashCode() : 0);
		hash = 53 * hash + (this._whiteSpace != null ?
			this._whiteSpace.hashCode() : 0);
		return hash;
	}

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
	public String toString() {
		return "Restriction [base=" + _base.toString() + "]";
	}
}