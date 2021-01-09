package org.xdef.impl.xml;

import org.xdef.msg.XML;
import org.xdef.sys.SRuntimeException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/** Implementation of interface javax.xml.namespace.NamespaceContext.
 * There are implemented more methods pushContext(), popContext(),
 * getRecentPrefixes(), setPrefix(String), getAllNamespaceURIs(),
 * getAllPrefixes() and clearContext().
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
		_stackTop--;
		int prevSize;
		if (_size > (prevSize = _stack[_stackTop])) {
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
			? XMLConstants.DEFAULT_NS_PREFIX : prefix.trim();
		if (s.startsWith("xml")) {
			throw new SRuntimeException(XML.XML802, s); //Cant set prefix &{0}
		}
		String myUri = uri==null || uri.trim().equals(XMLConstants.NULL_NS_URI)
			? null : uri.trim();
		int ndx = _size;
		for (int i = 0; i < _size; i++) {
			if (_prefixes[i].equals(prefix)) {
				_uris[ndx] = myUri;
				return;
			}
		}
		if (++_size >= _prefixes.length) {
			String[] w = _prefixes;
			_prefixes = new String[ndx + STEP];
			System.arraycopy(w, 0, _prefixes, 0, ndx);
			 w = _uris;
			_uris = new String[ndx + STEP];
			System.arraycopy(w, 0, _uris, 0, ndx);
		}
		_prefixes[ndx] = s;
		_uris[ndx] = myUri;
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

	@Override
	public final String toString() {
		StringBuilder result = new StringBuilder();
		for(String prefix: getAllPrefixes()) {
			result.append("\n\"").append(prefix).append("\": \"")
				.append(getNamespaceURI(prefix)).append('"');
		}
		return "Level=" + _stackTop + result;
	}
}