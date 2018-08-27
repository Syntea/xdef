/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.syntea.xdef;

import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;

/** Unique set object.
 * @author Vaclav Trojan
 */
public interface XDUniqueset extends XDValue {

	/** Get name of this uniqueSet.
	 * @return name of this uniqueSet.
	 */
	public String getName();

	/** Write error reports to reporter and clear map.
	 * @param reporter report writer.
	 * @return true if reporter was empty.
	 */
	public boolean checkAndClear(final ReportWriter reporter);

	/** Get address of parsing method.
	 * @return the address of code.
	 */
	public int getParseMethod();

	/** Get key part index of the actual item.
	 * @return actual key index.
	 */
	public int getKeyItemIndex();

	/** Set object as marker.
	 * @param marker Object which is used for markers.
	 * @return true if the marker is new in the set of makers.
	 */
	public boolean setMarker(final Object marker);

	/** Check if all item are marked with given object in this unique set.
	 * @param marker Object used as marker.
	 * @return list unmarked keys in this unique set or return the empty string.
	 */
	public String checkNotMarked(final Object marker);

	/** Set named named value assigned to to the actual unique set item.
	 * If the key not exists this method does nothing.
	 * @param name name of value.
	 * @param value value to be set.
	 */
	public void setNamedValue(final String name, final XDValue value);

	/** Get named named value assigned to the actual unique set item.
	 * If the key not exists this method returns null.
	 * @param name name of value.
	 * @return saved value.
	 */
	public XDValue getNamedValue(final String name);

	/** Get printable form of actual value of the key (used in error reporting).
	 * @return printable form of actual value of the key.
	 */
	public String printActualKey();

}