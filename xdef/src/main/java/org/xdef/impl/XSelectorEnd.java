package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import org.xdef.model.XMDefinition;
import org.xdef.sys.SUnsupportedOperationException;

/** End mark of selector.
 * @author Vaclav Trojan
 */
public final class XSelectorEnd extends XNode {

	/* Create the new instance of XSelectorEnd object. */
	public XSelectorEnd() {super(null, "$selector_end", null, XMSELECTOR_END);}

	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	@Override
	public XMDefinition getXMDefinition() {return null;}

	@Override
	public int getInitCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getFinallyCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getMatchCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getComposeCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getCheckCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnTrueCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnFalseCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getDefltCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnStartElementCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnAbsenceCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnExcessCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnIllegalAttrCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnIllegalTextCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getOnIllegalElementCode() {throw new SUnsupportedOperationException();}

	@Override
	public int getVarinitCode() {throw new SUnsupportedOperationException();}

///////////////////////////////////////////////////////////////////////////////////

	/** Write this X object to XDWriter.
	 * @param xw XDWriter used for writing.
	 * @param list list of XNodes to be written.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public final void writeXNode(final XDWriter xw, final List<XNode> list) throws IOException {
		xw.writeShort(getKind());
	}
}
