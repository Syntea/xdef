package org.xdef.util;

import org.xdef.XDConstants;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.proc.XXElement;
import java.util.Properties;

/** Provides checking of X-definition types and values.
 * @author Vaclav Trojan
 */
public class XDChecker {

	private final Class<?>[] _classes;
	private final String _options;
	private final String _methods;
	private final String _declarations;
	private final Properties _properties;

	/** Constructor of XDChecker.*/
	public XDChecker() {
		_classes = null;
		_methods = null;
		_declarations = null;
		_options = null;
		_properties = new Properties();
	}

	/** Constructor of XDChecker.
	 * @param classes array with external classes or <tt>null</tt>.
	 * @param methods string with methods or <tt>null</tt>.
	 * (see "methods attribute in X-definitions").
	 * @param declarations string with declaration of variables,
	 * types and methods or <tt>null</tt>.
	 * @param options string with options list <tt>null</tt>.
	 * @throws SRuntimeException if there is an error.
	 */
	public XDChecker(Class<?>[] classes,
		String methods,
		String declarations,
		String options) throws SRuntimeException {
		_classes = classes;
		_methods = methods;
		_declarations = declarations;
		_options = options;
		_properties = new Properties();
		// just check if all parameters are correct
		String xdef =
"<x:def xmlns:x='"+ XDConstants.XDEF40_NS_URI + "' root = 'a'>\n";
		if (_declarations != null || _methods != null) {
			xdef +=	"<x:declaration>\n";
			if (_methods != null) {
				xdef +=	"external method {\n" + _methods + "\n}\n";
			}
			if (_declarations != null) {
				xdef += _declarations +"\n";
			}
			xdef += "</x:declaration>\n";
		}
		xdef += "<a";
		if (_options != null) {
			xdef += " x:script = 'options " + _options + "'\n  ";
		}
		xdef += " a='?'/>\n</x:def>";
		XDFactory.compileXD(_properties, xdef);
	}

	@SuppressWarnings("deprecation")
	/** Check if the value is correct type.
	 * @param type the string with type.
	 * @param value value to be checked.
	 * @return XDParseResult containing value and errors. It may be checked
	 * with method errors().
	 */
	final public XDParseResult checkType(final String type, final String value){
		XDPool xp;
		ArrayReporter ar = new ArrayReporter();
		XDParseResult result;
		try {
			XDBuilder xb = XDFactory.getXDBuilder(ar, _properties);
			if (_classes != null && _classes.length > 0) {
				xb.setExternals(_classes);
			}
			String xdef =
"<x:def xmlns:x='"+ XDConstants.XDEF40_NS_URI + "' root = 'a'>";
		if (_declarations != null || _methods != null) {
			xdef +=	"<x:declaration>\n";
			if (_methods != null) {
				xdef +=	"external method {\n" + _methods + "\n}\n";
			}
			if (_declarations != null) {
				xdef += _declarations +"\n";
			}
			xdef += "</x:declaration>\n";
		}
		xdef += "<a\n";
		if (_options != null) {
			xdef += " x:script = 'options " + _options + "'\n";
		}
		xdef += " a=\""+
			SUtils.modifyString(SUtils.modifyString(type,
				"\"","&quot;"),	"'", "&apos;")
				+ "\"/>\n</x:def>";
			xb.setSource(xdef);
			xp = xb.compileXD();
			if (ar.errors()) {
				result = XDFactory.createParseResult(value);
				result.addReports(ar);
				return result;
			}
		} catch (Exception ex) {
			result = XDFactory.createParseResult(value);
			result.addReports(ar);
			return result;
		}
		XDDocument xd = xp.createXDDocument();
		XXElement xel = xd.prepareRootXXElement("a", true);
		if (value == null) {
			result = XDFactory.createParseResult(null);
			if (xel.getXMElement().getAttr("a").getOccurence().isRequired()) {
				//Missing required attribute &amp;{0}
				result.error(XDEF.XDEF526, "a");
			} else {
				result = XDFactory.createParseResult("");
//				result.setSourceBuffer(null);
			}
		} else {
			xel.addAttribute("a", value);
			result = xel.getParseResult();
			if (result == null) {
				result = XDFactory.createParseResult(value);
			}
		}
		if (xd.errors()) {
			result.addReports(
				(ArrayReporter) xd.getReporter().getReportWriter());
		}
		return result;
	}
}