package org.xdef.impl.code;

import java.io.ByteArrayOutputStream;
import org.xdef.XDEmail;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Implements the internal object with Email value.
 * @author Vaclav Trojan
 */
public final class DefEmail extends XDValueAbstract implements XDEmail {
	/** BNF grammar of email address. */

	/** Email source value. */
	private final String _value;
	/** Email domain. */
	private final String _domain;
	/** Email user. */
	private final String _localPart;
	/** Email user name. */
	private final String _userName;

	/** Creates a new instance of DefEmail as null.*/
	public DefEmail() {this((String) null);}

	private static String readStackItem(final StringParser q,
		final String s,
		final String src) {
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
		} catch (Exception ex) {
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
		} catch (Exception ex) {
			return "?";
		}
	}

	/** Creates a new instance of DefEmail.
	 * @param value string with email address.
	 */
	public DefEmail(final String value) {
		if (value == null || value.isEmpty()) {
			_value = _localPart = _domain = _userName = null;
			return;
		}
		StringParser p = new StringParser(value);
		String[] result = parseEmail(p);
		if (result == null || !p.eos()) {
			//Incorrect value of &{0}&{1}&{: }
			throw new SRuntimeException(XDEF.XDEF809, "email");
		}
		_value = result[0];
		_localPart = result[1];
		_domain = result[2];
		_userName = result[3];
	}

	public DefEmail(StringParser p) {
		String[] result = parseEmail(p);
		if (result == null) {
			//Incorrect value of &{0}&{1}&{: }
			throw new SRuntimeException(XDEF.XDEF809, "email");
		}
		_value = result[0];
		_localPart = result[1];
		_domain = result[2];
		_userName = result[3];
	}

	/** Creates a new instance of DefEmail
	 * @param p parser with source data.
	 * @return null if not correct or array of strings where the items are:<p>
	 * [0] .. text of parsed email source.<p>
	 * [1] .. local part of internet address.<p>
	 * [2] .. domain part of internet address.<p>
	 * [3] .. string with personal information.
	 */
	public final static String[] parseEmail(final StringParser p) {
		BNFGrammar g = BNFGrammar.compile(
"S ::= (' ' | #9)+ /* linear white space */\n" +
"comment ::= commentList $rule\n" +
"commentList ::= ( '(' commentPart* ')' (S? '(' commentPart* ')')* ) S?\n" +
"commentPart ::= ($ASCIIChar - [()])+ (S? commentList)?\n" +
"delimiters  ::=  specials | S | comment\n" +
"specials ::=  '(' | ')' | '<' | '>' | '@' | ',' | ';' \n" +
"              | ':' | '\\' | '\"' |  '.' | '[' | ']'\n"+
"atom ::= ($ASCIIChar - $ctlrChar - specials - ' ')+\n" +
"emailAddr ::= localPart domain $rule\n" +
"emailAddr1 ::= '<' emailAddr '>' \n" +
"localPart ::= atom ('.' atom)*\n" +
"domain ::= '@' atom ('.' atom)*\n" +
"email ::= (text? S? emailAddr1 | (comment* emailAddr)) (S? comment)*\n"+
"text ::= ((comment* (textItem | comment)*) | S? ptext)?\n"+
"textItem ::= S? '=?' charsetName ('Q?' qtext | 'B?' btext) '?='\n"+
"charsetName ::= ([a-zA-Z] ('-'? [a-zA-Z0-9]+)*) $rule '?' \n"+
"ptext ::=  (($ASCIIChar - $ctlrChar - [@><()=])+) $rule\n"+
"qtext ::= ((hexOctet | $ASCIIChar - $ctlrChar - [=?])+) $rule /*quoted*/\n"+
"hexOctet ::= '=' [0-9A-F] [0-9A-F]\n"+
"btext ::= ([a-zA-Z0-9+/]+ '='? '='?) $rule /* base64 */");
		p.isSpaces();
		if (g.parse(p, "email")) {
			String domain; // Email domain
			String localPart; // Email user
			String userName; // Email user name
			String charsetName; // name of text charset name
			domain = localPart = userName = charsetName = null;
			Object[] code = g.getParsedObjects();
			String s = p.getParsedBufferPart();
			if (code != null) {
				for (int i = 0; i < code.length; i++) {
					StringParser q = new StringParser((String) code[i]);
					String t;
					if ((t = readStackItem(q, "emailAddr", s)) != null) {
						int ndx = t.indexOf('@');
						localPart = t.substring(0, ndx);
						domain = t.substring(ndx + 1);
					} else if ((t = readStackItem(q, "comment", s)) != null) {
						t = (t = t.trim()).substring(1, t.length() -1).trim();
						if (!t.isEmpty()) {
							if (localPart == null) {
								if (userName == null) {
									userName = t;
								} else {
									userName += t;
								}
							} else if (userName == null) {
								userName = t;
							}
						}
					} else if ((t = readStackItem(q, "ptext", s)) != null) {
						if (userName == null) {
							userName = t;
						} else {
							userName += t;
						}
					} else if ((t = readStackItem(q, "charsetName", s)) != null) {
						charsetName = t;
					} else if ((t = readStackItem(q, "qtext", s)) != null) {
						t = readQtext(t, charsetName);
						if (userName == null) {
							userName = t;
						} else {
							userName += t;
						}
					} else if ((t = readStackItem(q, "btext", s)) != null) {
						t = readBtext(t, charsetName);
						if (userName == null) {
							userName = t;
						} else {
							userName += t;
						}
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
	public short getItemId() {return XDValueID.XD_EMAIL;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.EMAIL;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return isNull() ? "" : _value;}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return isNull()? null: toString();}

	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefEmail(_value);}

	@Override
	public int hashCode() {
		return isNull() ? 1 : _localPart.hashCode() + _domain.hashCode()*3;
	}

	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ?  equals((XDValue) arg) : false;
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		} else if (arg == null || arg.isNull()) {
			return false;
		} else if (arg instanceof XDEmail) {
			return _localPart.equals(((XDEmail)arg).getLocalPart())
				&& _domain.equals(((XDEmail)arg).getDomain());
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
		if (arg.getItemId() == XDValueID.XD_BOOLEAN) {
			if (equals(arg)) return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	public boolean isNull() {return _value == null;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDEmail interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getDomain() {return _domain;}
	@Override
	public String getLocalPart() {return _localPart;}
	@Override
	public String getUserName() {return _userName;}
	@Override
	public String getEmailAddr() {return _value;}
}