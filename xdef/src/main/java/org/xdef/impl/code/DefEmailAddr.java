package org.xdef.impl.code;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.XDEmailAddr;
import static org.xdef.XDValueType.EMAIL;
import org.xdef.sys.SException;

/** Implements the internal object with Email value.
 * @author Vaclav Trojan
 */
public final class DefEmailAddr extends XDValueAbstract implements XDEmailAddr {
	/** Email source value. */
	private final String _value;
	/** Email domain. */
	private final String _domain;
	/** Email user. */
	private final String _localPart;
	/** Email user name. */
	private final String _userName;

	/** Creates a new instance of DefEmail as null.*/
	public DefEmailAddr() {this((String) null);}

	private static String readStackItem(final StringParser q, final String s, final String src) {
		if (q.isToken(s) && q.isSpaces()) {
			if (q.isInteger()) {
				int i = q.getParsedInt();
				q.isSpaces();
				if (q.isInteger()) {
					return src.substring(i, q.getParsedInt());
				}
			}
		}
		return null;
	}

	private static String readBtext(final String s, final String charsetName) {
		try {
			return new String(SUtils.decodeBase64(s), charsetName);
		} catch (UnsupportedEncodingException | SException ex) {
			return "?";
		}
	}

	private static String readQtext(final String s, final String charsetName) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '=' && i + 2 < s.length()) {
				String hexMask = "0123456789ABCDEF";
				ch = (char) ((hexMask.indexOf(s.charAt(++i)) << 4)
					+ hexMask.indexOf(s.charAt(++i)));
			}
			b.write((byte) ch);
		}
		try {
			return new String(b.toByteArray(), charsetName);
		} catch (UnsupportedEncodingException ex) {
			return "?";
		}
	}

	/** Creates a new instance of DefEmail.
	 * @param value string with email address.
	 */
	public DefEmailAddr(final String value) {
		if (value == null || value.isEmpty()) {
			_value = _localPart = _domain = _userName = null;
			return;
		}
		StringParser p = new StringParser(value);
		String[] result = parseEmail(p);
		if (result == null || !p.eos()) {
			throw new SRuntimeException(XDEF.XDEF809, "email"); //Incorrect value of &{0}&{1}&{: }
		}
		_value = result[0];
		_localPart = result[1];
		_domain = result[2];
		_userName = result[3];
	}

	public DefEmailAddr(StringParser p) {
		String[] result = parseEmail(p);
		if (result == null) {
			throw new SRuntimeException(XDEF.XDEF809, "email"); //Incorrect value of &{0}&{1}&{: }
		}
		_value = result[0];
		_localPart = result[1];
		_domain = result[2];
		_userName = result[3];
	}

	/** Creates a new instance of DefEmailAddr
	 * @param p parser with source data.
	 * @return null if not correct or array of strings where the items are:<p>
	 * [0] .. text of parsed email source.<p>
	 * [1] .. local part of internet address.<p>
	 * [2] .. domain part of internet address.<p>
	 * [3] .. string with personal information.
	 */
	public static final String[] parseEmail(final StringParser p) {
		BNFGrammar g = BNFGrammar.compile(
"S ::= [ #9]+ /* linear white space */\n"+
"asciiChar ::= [ -~]\n"+
"comment ::=  S? ( commentList $rule) S?\n"+
"commentList ::= ( '(' commentPart* ')' (S? '(' commentPart* ')')* )\n"+
"commentPart ::= (asciiChar - [()])+ (S? commentList)?\n"+
"atom ::= ([-0-9a-zA-Z_])+\n"+
"emailAddr ::= localPart domain $rule\n"+
"emailAddr1 ::= '<' emailAddr '>' \n"+
"localPart ::= atom ('.' atom)*\n"+
"domain ::= '@' atom ('.' atom)*\n"+
"text ::= ((comment* (textItem | comment)*) | comment* S? ptext)? comment*\n"+
"textItem ::= S? '=?' charsetName ('Q?' qtext | 'B?' btext) '?='\n"+
"charsetName ::= ([a-zA-Z] ('-'? [a-zA-Z0-9]+)*) $rule '?' \n"+
"ptext ::= ((asciiChar - [@><()=])+) $rule\n"+
"qtext ::= ((hexOctet | asciiChar - [=?])+) $rule /*quoted*/\n"+
"hexOctet ::= '=' [0-9A-F] [0-9A-F]\n"+
"btext ::= ([a-zA-Z0-9+/]+ '='? '='?) $rule /* base64 */\n"+
"email ::= (text? S? emailAddr1 | (comment* emailAddr)) (S? comment)*");
		p.isSpaces();
		if (g.parse(p, "email")) {
			String domain; // Email domain
			String localPart; // Email user
			String charsetName; // name of text charset name
			domain = localPart = charsetName = null;
			String userName = ""; // Email user name
			Object[] code = g.getParsedObjects();
			String s = p.getParsedBufferPart();
			if (code != null) {
				for (Object code1 : code) {
					StringParser q = new StringParser((String) code1);
					String t;
					if ((t = readStackItem(q, "emailAddr", s)) != null) {
						int ndx = t.indexOf('@');
						localPart = t.substring(0, ndx);
						domain = t.substring(ndx + 1);
					} else if ((t = readStackItem(q, "comment", s)) != null) {
						t = (t = t.trim()).substring(1, t.length() -1).trim();
						if (!t.isEmpty()) {
							if (localPart == null) {
								userName += t;
							} else {
								userName = t;
							}
						}
					} else if ((t = readStackItem(q, "ptext", s)) != null) {
						userName += t.trim();
					} else if ((t = readStackItem(q, "charsetName", s)) != null) {
						charsetName = t;
					} else if ((t = readStackItem(q, "qtext", s)) != null) {
						t = readQtext(t, charsetName);
						userName += t;
					} else if ((t = readStackItem(q, "btext", s)) != null) {
						t = readBtext(t, charsetName);
						userName += t;
					}
				}
				p.isSpaces();
				if (localPart != null && domain != null) {
					return new String[] {
						g.getParsedString(), localPart, domain, userName};
				}
			}
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return this;}
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_EMAIL;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return EMAIL;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return stringValue();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return isNull() ? "" : _value;}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefEmailAddr(_value);}
	@Override
	public int hashCode() {return isNull() ? 1 : _localPart.hashCode() + _domain.hashCode()*3;}
	@Override
	public boolean equals(final Object arg) {return arg instanceof XDValue ?  equals((XDValue) arg) : false;}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value of the object is comparable
	 * and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		} else if (arg == null || arg.isNull()) {
			return false;
		} else if (arg instanceof XDEmailAddr) {
			return _localPart.equals(((XDEmailAddr)arg).getLocalPart())
				&& _domain.equals(((XDEmailAddr)arg).getDomain());
		}
		return false;
	}
	@Override
	/** Compares this object with the other DefEmail object.
	 * @param arg other DefEmail object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (arg.getItemId() == XD_BOOLEAN) {
			if (equals(arg)) return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	/** Check if the object is null.
	 * @return true if the object is null otherwise return false.
	 */
	public boolean isNull() {return _value == null;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDEmailAddr interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get domain part of this email address.
	 * @return string with domain part of this email address.
	 */
	public String getDomain() {return _domain;}
	@Override
	/** Get local part of email this address (user).
	 * @return string with local part of this email address.
	 */
	public String getLocalPart() {return _localPart;}
	@Override
	/** Get user name (display form) of this email address.
	 * @return string with user name of email this address (or an empty string).
	 */
	public String getUserName() {return _userName;}
	@Override
	/** Get source form of this email address.
	 * @return source form of this email address.
	 */
	public String getEmailAddr() {return _value;}
}