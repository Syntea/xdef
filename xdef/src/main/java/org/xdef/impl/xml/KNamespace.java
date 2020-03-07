package org.xdef.impl.xml;

import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.SRuntimeException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/** Implementation of interface javax.xml.namespace.NamespaceContext.
 * @author Vaclav Trojan
 */
public class KNamespace implements NamespaceContext {
	/** First step of stack size (must be >= 2).*/
	private final int STEP = 4;
	/** Next steps of stack size (must be >= STEP).*/
	private final int STEP2 = 8;

	/** Actual number of prefixes.*/
	private int _size;
	/** Stack of sizes of namespace context.*/
	private int[] _stack;
	/** Top of stack.*/
	private int _stackTop;
	/** Table of namespace prefixes. */
	private String[] _prefixes;
	/** Table of namespace URIs. */
	private String[] _uris;

	/** Creates a new instance of KNamespaceImpl with default items
	 * for prefixes "xml" and "xmlns".
	 */
	public KNamespace() {}

	private void init() {
		//allocate arrays
		_stack = new int[STEP];
		_prefixes = new String[STEP];
		_uris = new String[STEP];
		//set prefixes "xml" and "xmlns"
		_prefixes[0] = XMLConstants.XML_NS_PREFIX;
		_uris[0] = XMLConstants.XML_NS_URI;
		_prefixes[1] = XMLConstants.XMLNS_ATTRIBUTE;
		_uris[1] = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		//pushContext
		_stack[0] = _size = 2;
		_stackTop = 1;
	}

	/** Creates a new instance of KNamespaceImpl with default items
	 * for prefixes "xml" and "xmlns" and one prefix and nsUri.
	 * @param prefix String with prefix (may be empty string).
	 * @param uri namespace URI assigned to this prefix.
	 */
	public KNamespace(final String prefix, final String uri) {
		init();
		setPrefix(prefix, uri);
	}

	/** Creates a new instance of KNamespaceImpl with default items
	 * for prefixes "xml" and "xmlns" and prefixes and uris from arguments.
	 * Size of both arguments must be equal.
	 * @param prefix array of strings with prefixes.
	 * @param uri array of strings with namespace URIa assigned to prefixes.
	 */
	public KNamespace(final String[] prefix, final String[] uri) {
		init();
		if (prefix.length != uri.length) {
			throw new SRuntimeException(SYS.SYS080); //Index out of array
		}
		for (int i = 0; i < prefix.length; i++) {
			setPrefix(prefix[i], uri[i]);
		}
	}

	/** Creates a new instance of KNamespaceImpl as clone of given context.
	 * @param ns Context from which new instance will be created.
	 */
	public KNamespace(KNamespace ns) {
		if (ns != null && ns._size > 0) {
			_stack = new int[ns._stack.length];
			System.arraycopy(ns._stack, 0, _stack, 0, _stack.length);
			_stackTop = ns._stackTop;
			_prefixes = new String[ns._prefixes.length];
			System.arraycopy(ns._prefixes, 0, _prefixes, 0, _prefixes.length);
			_uris = new String[ns._uris.length];
			System.arraycopy(ns._uris, 0, _uris, 0, _uris.length);
			_size = ns._size;
		}
	}

////////////////////////////////////////////////////////////////////////////////
//  Implementation of methods from interface NamespaceContext.
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Find namespace URI assigned to prefix from argument.
	 * @param prefix namespace prefix to be searched.
	 * @return namespace URI or <tt>null</tt> if no URI was found.
	 */
	public final String getNamespaceURI(final String prefix) {
		if (prefix != null && _size > 0) {
			for (int i = _size - 1; i >= 0; i--) {
				if (prefix.equals(_prefixes[i])) {
					return _uris[i];
				}
			}
		}
		return null;
	}

	@Override
	/** Find most recent prefix assigned to given namespace URI.
	 * @param uri namespace URI to be searched.
	 * @return namespace prefix or <tt>null</tt>.
	 */
	public final String getPrefix(final String uri) {
		if (uri != null && _size > 0) {
			for (int i = _size - 1; i >= 0; i--) {
				String p;
				if (uri.equals(getNamespaceURI(p = _prefixes[i]))) {
					return p;
				}
			}
		}
		return null;
	}

	@Override
	/** Find all prefixes assigned to given namespace URI.
	 * @param uri namespace URI to be searched.
	 * @return iterator with list of prefixes.
	 */
	public final Iterator<String> getPrefixes(final String uri) {
		ArrayList<String> a = new ArrayList<String>();
		if (uri != null && _size > 0) {
			for (int i = _size - 1; i >= 0; i--) {
				String p;
				if (uri.equals(getNamespaceURI(p = _prefixes[i]))) {
					if (!a.contains(p)) {
						a.add(p);
					}
				}
			}
		}
		return a.iterator();
	}

////////////////////////////////////////////////////////////////////////////////
// Methods of KNamespace.
////////////////////////////////////////////////////////////////////////////////

	/** Push new namespace context space. */
	public final void pushContext() {
		if (_size == 0) {
			init();
		}
		int ndx = _stackTop++;
		if (_stackTop >= _stack.length) {
			int[] old = _stack;
			_stack = new int[ndx + STEP];
			System.arraycopy(old, 0, _stack, 0, ndx);
		}
		_stack[ndx] = _size;
	}

	/** Pop namespace context space (return to previous one). */
	public final void popContext() {
		if (_stackTop <= 0) {
			return;
		}
		int prevSize;
		if (_size > (prevSize = _stack[--_stackTop])) {
			if (prevSize + STEP2 <= _uris.length) {
				String[] w = _prefixes;
				_prefixes = new String[prevSize + STEP];
				System.arraycopy(w, 0, _prefixes, 0, prevSize);
				w = _uris;
				_uris = new String[prevSize + STEP];
				System.arraycopy(w, 0, _uris, 0, prevSize);
				if (_size > prevSize) {
					_size =  prevSize;
				}
			}
			for (int i = _size - 1; i >= prevSize; i--) {
				// call endPrefixMapping
				_uris[i] = null;
				_prefixes[i] = null;
			}
			_size = prevSize;
		}
		if (_stackTop + STEP2 <= _stack.length) {
			int[] w = _stack;
			_stack = new int[_stackTop + STEP];
			System.arraycopy(w, 0, _stack, 0, _stackTop + 1);
		}
	}

	/** Get array with prefixes from the top of context stack. If no new
	 * prefixes are on the top this method returns empty array. Also prefixes
	 * defining the empty namespace are returned in the array.
	 * @return array with prefixes from the top of context stack.
	 */
	public String[] getRecentPrefixes() {
		if (_stackTop <= 0) {
			return new String[0];
		}
		int start = _stack[_stackTop - 1];
		int len = _size - start;
		String[] result = new String[len];
		System.arraycopy(_prefixes, start, result, 0, len);
		return result;
	}

	/** Set prefix item (assign a namespace URI to a prefix name).
	 * @param prefix namespace prefix.
	 * @param uri namespace URI.
	 * @throws SRuntimeException or NullpointerException if an error occurs.
	 */
	public final void setPrefix(final String prefix, final String uri) {
		if (_size == 0) {
			init();
		}
		String s = prefix == null
			? XMLConstants.DEFAULT_NS_PREFIX : prefix.trim().intern();
		if (s.startsWith("xml")) {
			throw new SRuntimeException(XML.XML802, s); //Cant set prefix &{0}
		}
		int ndx = _size;
		if (++_size >= _prefixes.length) {
			String[] w = _prefixes;
			_prefixes = new String[ndx + STEP];
			System.arraycopy(w, 0, _prefixes, 0, ndx);
			 w = _uris;
			_uris = new String[ndx + STEP];
			System.arraycopy(w, 0, _uris, 0, ndx);
		}
		_prefixes[ndx] = s;
		_uris[ndx] = uri == null || uri.trim().equals(XMLConstants.NULL_NS_URI)
			? null : uri.trim().intern();
	}

	/** Get array with all available namespace URIs.
	 * @return array of strings with all URIs.
	 */
	public final String[] getAllNamespaceURIs() {
		ArrayList<String> a = new ArrayList<String>();
		if (_size > 0) {
			String[] prefixes = getAllPrefixes();
			for (int i = prefixes.length - 1; i >= 2; i--) {
				String uri = getNamespaceURI(prefixes[i]);
				if (uri != null && !a.contains(uri)) {
					a.add(0, uri);
				}
			}
		}
		return a.toArray(new String[a.size()]);
	}

	/** Get array with all available prefixes.
	 * @return array of strings with all prefixes.
	 */
	public final String[] getAllPrefixes() {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = _size - 1; i >= 2; i--) {
			if (!a.contains(_prefixes[i])) {
				a.add(0, _prefixes[i]);
			}
		}
		return a.toArray(new String[a.size()]);
	}

	/** Clear the context stack (except of predefined namespaces for prefixes
	 * "xml" and "xmlns"). */
	public final void clearContext() {
		if (_size > 0) {
			init();
		}
	}
}