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
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_EMAIL;
import static org.xdef.XDValueType.EMAIL;
import org.xdef.sys.SException;

/** Implements the internal object with Email value.
 * @author Vaclav Trojan
 */
public final class DefEmailAddr extends XDValueAbstract implements XDEmailAddr {
	/** BNF grammar rules of email address according to RFC 5321. */
	private static final BNFGrammar BNF = BNFGrammar.compile(
"FWS           ::= [ #9]+\n"+ // Folding white space
"ASCIICHAR     ::= [ -~]\n"+ // Printable ASCII character
"Domain        ::= sub_domain ( '.' sub_domain )*\n"+
"sub_domain    ::= Let_dig+ Ldh_str*\n" +
"Let_dig       ::= [0-9] | $letter\n" +
"Ldh_str       ::= '-'+ Let_dig+\n"+
"General_addr  ::= Std_tag ':' ( dcontent )+\n" +
"Std_tag       ::= Ldh_str\n"+ // Std-tag MUST be specified in a Standards-Track RFC and registered with IANA
"dcontent      ::= [!-Z] | [^-~]\n" + // %d33-90 | %d94-126 Printable US-ASCII; excl. [, ', ]

/*#if RFC5321*#/
// START RFC5321
"IPv4          ::= Snum ('.'  Snum){3}\n"+
"Snum          ::= ( '2' ([0-4] [0-9] | '5' [0..5]) ) | [0-1] [0-9]{2} | [0-9]{1,2}\n"+
"IPv6          ::= IPv6_full | IPv6_comp | IPv6v4_full | IPv6v4_comp\n" +
"IPv6_hex      ::= [0-9a-fA-F]{1,4}\n" +
"IPv6_full     ::= IPv6_hex ( ':' IPv6_hex ){7}\n" +
"IPv6_comp     ::= ( IPv6_hex ( ':' IPv6_hex ){0,5} )? '::' ( IPv6_hex (':' IPv6_hex){0,5} )?\n" +
			   // The '::' represents at least 2 16-bit groups of
			   // zeros. No more than 6 groups in addition to the '::' may be present.
"IPv6v4_full   ::= IPv6_hex ( ':' IPv6_hex ){5} ':' IPv4_addr\n" +
"IPv6v4_comp   ::= ( IPv6_hex (':' IPv6_hex){0,3} )? '::'\n" +
"                  ( IPv6_hex (':' IPv6_hex){0,3} ':' )? IPv4_addr\n" +
			   // The '::' represents at least 2 16-bit groups of zeros.  No more than 4 groups in
			   // addition to the '::' and IPv4-address-literal may be present.
"IPv4_addr     ::= IPv4\n"+
"IPv6_addr     ::= 'IPv6:' IPv6\n"+
"address       ::= '[' ( IPv4_addr | IPv6_addr | General_addr ) ']'\n" +  // See Section 4.1.3
"quoted_pair   ::= '\\' ASCIICHAR\n" + // %d92 %d32-126
			   // i.e., backslash followed by any ASCII graphic (including itself) or SPace
"qtextSMTP     ::= [ !#-Z^-~] | '[' | ']'\n" +
			   // i.e., within a quoted string, any ASCII graphic or space is permitted without
			   // blackslash-quoting except double-quote and the backslash itself.
"QcontentSMTP  ::= quoted_pair | qtextSMTP\n" +
"Quoted_string ::= '\"' QcontentSMTP* '\"'\n" +
"atext         ::= ( $letter | ('\\' ('[' | ']' | [\\\"@/ ()<>,;.:])) | [0-9_!#$%&'*+/=?^`{|}~] )+\n"+
"Local_part    ::= Dot_string | Quoted_string\n" + // MAY be case-sensitive
"Mailbox       ::= Local_part '@' ( address | Domain ) $rule\n"+
// END RFC5321
/*#else*/
// START not RFC5321 (i.e. RFC2822?)
"atext         ::= ($letter | [0-9_!#$%&'*+/=?^`{|}~\\])+\n"+
"Local_part    ::= Dot_string\n" + // MAY be case-sensitive, quoted string not allowed
"Mailbox       ::= Local_part '@' Domain $rule\n"+
// END not RFC5321 (i.e. RFC2822?)
/*#end*/

"Atom          ::= atext ('-' atext)*\n" +
"Dot_string    ::= Atom ('.'  Atom)*\n" +
"comment       ::= ( commentList $rule ) FWS?\n"+
"commentList   ::= ( FWS? '(' commentPart* ')' )+\n"+
"commentPart   ::= ( (ASCIICHAR - [()]) | $letter )+ ( commentList)? $rule\n"+
"text          ::= ( ( comment* (textItem | comment)* ) | comment* ptext )? comment*\n"+
"textItem      ::= FWS? '=?' charsetName ( 'Q?' qtext | 'B?' btext ) '?='\n"+
"charsetName   ::= ( [a-zA-Z] ('-'? [a-zA-Z0-9]+)* ) $rule '?' \n"+
"ptext         ::= FWS? ( ASCIICHAR - [@><()=] )+ $rule\n"+ // Printable ASCII character without @><()=
"qtext         ::= FWS? ( hexOctet | ASCIICHAR - [=?] )+ $rule \n"+ // Quoted text
"hexOctet      ::= '=' [0-9A-F] [0-9A-F]\n"+
"btext         ::= [a-zA-Z0-9+/]+ '='? '='? $rule\n"+ // Base64 text
"emailAddr     ::= ( text? FWS? '<' Mailbox '>' | comment* Mailbox ) comment*");

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

	/** Parse source data with email address.
	 * @param p parser with source data.
	 * @return null if not correct or array of strings where the items are:<p>
	 * [0] .. text of parsed email source.<p>
	 * [1] .. local part of internet address.<p>
	 * [2] .. domain part of internet address.<p>
	 * [3] .. string with personal information.
	 */
	public static final String[] parseEmail(final StringParser p) {
		p.isSpaces();
		Object[] code;
		String parsedString;
		synchronized (BNF) {
			if (!BNF.parse(p, "emailAddr")) {
				return null;
			}
			parsedString = BNF.getParsedString();
			code = BNF.getParsedObjects();
		}
		String domain; // Email domain
		String localPart; // Email user
		String charsetName; // name of text charset name
		domain = localPart = charsetName = null;
		String userName = ""; // Email user name
		String s = p.getParsedBufferPart();
		if (code != null) {
			for (Object code1 : code) {
				StringParser q = new StringParser((String) code1);
				String t;
				if ((t = readStackItem(q, "Mailbox", s)) != null) {
					int ndx = t.indexOf('@');
					localPart = t.substring(0, ndx);
					domain = t.substring(ndx + 1);
				} else if ((t = readStackItem(q, "commentPart", s)) != null) {
					userName += t;
				} else if ((t = readStackItem(q, "charsetName", s)) != null) {
					charsetName = t;
				} else if ((t = readStackItem(q, "qtext", s)) != null) {
					t = readQtext(t, charsetName);
					userName += t;
				} else if ((t = readStackItem(q, "btext", s)) != null) {
					t = readBtext(t, charsetName);
					userName += t;
				} else if ((t = readStackItem(q, "ptext", s)) != null) {
					userName += t.trim();
				}
			}
			p.isSpaces();
			if (localPart != null && domain != null) {
				localPart = removeWS(localPart);
				domain = removeWS(domain);
				if (localPart.length() <= 64 && domain.length() <= 256) {
					return new String[] {parsedString, localPart, domain, userName};
				}
			}
		}
		return null;
	}

	/** Read string from stack item.
	 * @param q StringParser starting with name of item.
	 * @param rule name of rule
	 * @param src parsed source data.
	 * @return part of parsed source data.
	 */
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

	/** Get decoded base64 text.
	 * @param s base64 text.
	 * @param charsetName name of charset.
	 * @return decoded base64 text.
	 */
	private static String readBtext(final String s, final String charsetName) {
		try {
			return new String(SUtils.decodeBase64(s), charsetName);
		} catch (SException ex) {
			throw new SRuntimeException(ex.getReport());
		} catch (UnsupportedEncodingException ex) {
			//Incorrect value of &{0}&{1}&{: }
			throw new SRuntimeException(XDEF.XDEF809, "email", ex.toString());
		}
	}

	/** Get quoted part of source data with given charset.
	 * @param s extracted quoted part
	 * @param charsetName name of charset.
	 * @return quoted part of source data (decode given charset).
	 */
	private static String readQtext(final String s, final String charsetName) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '=' && i + 2 < s.length()) {
				String hexMask = "0123456789ABCDEF";
				ch = (char) ((hexMask.indexOf(s.charAt(++i)) << 4) + hexMask.indexOf(s.charAt(++i)));
			}
			b.write((byte) ch);
		}
		try {
			return new String(b.toByteArray(), charsetName);
		} catch (UnsupportedEncodingException ex) {
			//Incorrect value of &{0}&{1}&{: }
			throw new SRuntimeException(XDEF.XDEF809, "email", ex.toString());
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Remove white spaces from argument.
	 * @param s string where remove white spaces.
	 * @return string with removed white spaces.
	 */
	private static String removeWS(final String s) {
		String result = s;
		int i;
		while ((i = result.indexOf(' ')) >= 0 || (i = result.indexOf('\t')) >=0) {
			result = result.substring(0,i) + result.substring(i + 1);
		}
		return result;
	}

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
