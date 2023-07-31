package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import org.xdef.model.XMDefinition;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMMIXED;
import org.xdef.model.XMSelector;
import org.xdef.sys.SUnsupportedOperationException;

/** Implementation of mixture, choice or sequence.
 * @author Vaclav Trojan
 */
public class XSelector extends XNode implements XMSelector {
	/** Flag if was declared attribute "empty". */
	boolean _emptyDeclared;
	/** Flag if selector may be empty. */
	boolean _empty;
	/** Flag if items of the selector items are optional (all in mixed or
	 * sequence, at least one in choice). */
	boolean _ignorable;
	/** Flag if the selector is selective in choice. */
	boolean _selective;
	/** Match code address or -1. */
	int _match;
	/** Initialize code address or -1. */
	int _init;
	/** Create code address or -1. */
	int _compose;
	/** Finally code address or -1. */
	int _finaly;
	/** Absence code address or -1. */
	int _onAbsence;
	/** Excess code address or -1. */
	int _onExcess;
	/** Where selector begins. */
	int _begIndex;
	/** Where selector ends. */
	int _endIndex;
	/** True if this selector is an "ALL" group. */
	private boolean _isAll;

	/* Creates a new instance of XSelector as the item of Xelement body.
	 * @param kind The type of selector.
	 */
	public XSelector(final short kind) {
		super(null,
			kind==XMMIXED ? "$mixed" : kind==XMCHOICE ? "$choice" : "$sequence",
			null,
			kind);
		_empty = false;
		_ignorable = true;
		_selective = false;
		_emptyDeclared = false;
		setOccurrence(1, 1);
		_match = _init = _compose = _onAbsence = _onExcess = _finaly = -1;
	}

	public XSelector(final XSelector x) {
		super(null, x.getName(), null, x.getKind());
		setOccurrence(x.minOccurs(), x.maxOccurs());
		_empty = x._empty;
		_emptyDeclared = x._emptyDeclared;
		_match = x._match;
		_init = x._init;
		_onAbsence = x._onAbsence;
		_onExcess = x._onExcess;
		_compose = x._compose;
		_onAbsence = x._onAbsence;
		_onExcess = x._onExcess;
		_finaly = x._finaly;
		setSPosition(x.getSPosition());
		setXDPosition(x.getXDPosition());
	}

	/** Set flag if selector may be empty sequence.
	 * @param empty the value of empty flag.
	 */
	public final void setEmptyDeclared(final boolean empty) {
		_emptyDeclared = true;
		_empty = empty;
	}

	/** Get emptyDeclared flag.
	 * @return the value of emptyDeclared flag.
	 */
	public final boolean isEmptyDeclared() {return _emptyDeclared;}

	/** Set flag if selector may be empty sequence.
	 * NOTE: use method setEmptyDeclared to set empty flag declared
	 * by parameter of choice or mixed!
	 * @param empty the value of empty flag.
	 */
	public final void setEmptyFlag(final boolean empty) {_empty = empty;}

	/** Return flag selector may be empty sequence.
	 * @return the value of empty flag.
	 */
	public final boolean isEmptyFlag() {return _empty;}

	/** Set ignorable flag.
	 * @param ignorable the ignorable flag.
	 */
	public final void setIgnorable(final boolean ignorable) {
		_ignorable = ignorable;
	}

	/** Returns value of ignorable flag.
	 * @return the ignorable flag.
	 */
	public final boolean isIgnorable() {return _ignorable;}

	/** Check if this selector is selective.
	 * @return value of _selective flag.
	 */
	public final boolean isSelective() {return _selective;}

	/** Set selective flag.
	 * @param selective  value of _selective flag
	 */
	public final void setSelective(final boolean selective) {
		_selective = selective;
	}

	/** Set index where selector begins.
	 * @param begIndex the index of beginning of the group.
	 */
	public final void setBegIndex(final int begIndex) {_begIndex = begIndex;}

	/** Set index where selector ends.
	 * @param endIndex the index of end of the group.
	 */
	public final void setEndIndex(final int endIndex) {_endIndex = endIndex;}

	/** Set match code address.
	 * @param addr the address of match code or -1.
	 */
	public final void setMatchCode(int addr) {_match = addr;}

	/** Set initialization code address.
	 * @param addr the address if initialization code or -1.
	 */
	public final void setInitCode(int addr) {_init = addr;}

	/** Set absence code address.
	 * @param addr the address of absence code or -1.
	 */
	public final void setOnAbsenceCode(int addr) {_onAbsence = addr;}

	/** Set excess code address.
	 * @param addr the address of excess code or -1.
	 */
	public final void setOnExcessCode(int addr) {_onExcess = addr;}

	/** Set create code address.
	 * @param addr the address of create code or -1.
	 */
	public final void setComposeCode(int addr) {_compose = addr;}

	/** Set finally method code address.
	 * @param addr the address of finally method or -1.
	 */
	public final void setFinallyCode(final int addr) {_finaly = addr;}

	/** Set flag group ALL.
	 * @param x value of the flag
	 */
	final void setGroupAll(final boolean x) {_isAll = x;}

	/** read selector from reader.*/
	static final XSelector readXSelector(
		final XDReader xr, final short kind) throws IOException {
		XSelector x;
		if (kind == XMMIXED) {
			x = new XMixed();
		} else if (kind == XMCHOICE) {
			x = new XChoice();
		} else {
			x = new XSequence();
		}
		x.setOccurrence(xr.readInt(), xr.readInt());
		x._emptyDeclared = xr.readBoolean();
		x._empty = xr.readBoolean();
		x._ignorable = xr.readBoolean();
		x._selective = xr.readBoolean();
		x._match = xr.readInt();
		x._init = xr.readInt();
		x._onAbsence = xr.readInt();
		x._onExcess = xr.readInt();
		x._compose = xr.readInt();
		x._finaly = xr.readInt();
		x._begIndex = xr.readInt();
		x._endIndex = xr.readInt();
		x.setSPosition(xr.readSPosition());
		x.setXDPosition(xr.readString());
		return x;
	}
	@Override
	public final int getCheckCode(){throw new SUnsupportedOperationException();}
	@Override
	public final int getOnTrueCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getOnFalseCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getDefltCode(){throw new SUnsupportedOperationException();}
	@Override
	public final int getMatchCode() {return _match;}
	@Override
	public final int getInitCode() {return _init;}
	@Override
	public final int getFinallyCode() {return _finaly;}
	@Override
	public final int getOnAbsenceCode() {return _onAbsence;}
	@Override
	public final int getOnStartElementCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getComposeCode() {return _compose;}
	@Override
	public final int getOnIllegalAttrCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getOnIllegalTextCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getOnIllegalElementCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final int getOnExcessCode() {return _onExcess;}
	@Override
	public final int getVarinitCode() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public final XMDefinition getXMDefinition() {return null;}
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get index where selector begins in child nodes list.
	 * @return index of beginning of the group.
	 */
	public final int getBegIndex() {return _begIndex;}
	@Override
	/** Get index where selector ends in child nodes list.
	 * @return the index of beginning of the group.
	 */
	public final int getEndIndex() {return _endIndex;}
	@Override
	public final void writeXNode(final XDWriter xw,
		final List<XNode> list) throws IOException {
		xw.writeShort(getKind());
		xw.writeInt(minOccurs());
		xw.writeInt(maxOccurs());
		xw.writeBoolean(_emptyDeclared);
		xw.writeBoolean(_empty);
		xw.writeBoolean(_ignorable);
		xw.writeBoolean(_selective);
		xw.writeInt(_match);
		xw.writeInt(_init);
		xw.writeInt(_onAbsence);
		xw.writeInt(_onExcess);
		xw.writeInt(_compose);
		xw.writeInt(_finaly);
		xw.writeInt(_begIndex);
		xw.writeInt(_endIndex);
		xw.writeSPosition(getSPosition());
		xw.writeString(getXDPosition());
	}
}