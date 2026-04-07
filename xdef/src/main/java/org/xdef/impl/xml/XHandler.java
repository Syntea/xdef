package org.xdef.impl.xml;

import org.xml.sax.InputSource;

/** Interface for XML handler (Stack of readers)
 * @author Vaclav Trojan
 */
public interface XHandler {

    /** Pop reader in the stack of readers. */
    public void popReader();

    /** Push reader the the stack of readers.
     * @param mr reader to be pushed.
     * @return reader on the top of stack before pushing.
     */
    public InputSource pushReader(final XAbstractReader mr);
}