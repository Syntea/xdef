package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.model.XMDebugInfo;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.xdef.impl.debug.XEditor;

/** Interface for debug mode.
 * @author Vaclav Trojan
 */
public interface XDDebug {
	/** Debug action no step. */
	public static final int NOSTEP = 0;
	/** Debug action step into. */
	public static final int STEPINTO = 1;
	/** Debug action step over. */
	public static final int STEPOVER = 2;
	/** Debug action step kill X-definition processing. */
	public static final int KILL = 3;

	/** Event "onTrue". */
	public static final char ONTRUE = 'A';
	/** Event "onFalse". */
	public static final char ONFALSE = 'B';
	/** Event "onAbsence". */
	public static final char ONABSENCE = 'C';
	/** Event "onExcess". */
	public static final char ONEXCESS = 'D';
	/** Event "onStartElement". */
	public static final char ONSTARTELEMENT = 'E';
	/** Event "onIllegalAttr". */
	public static final char ONILLEGALATTR = 'F';
	/** Event "onIllegalText". */
	public static final char ONILLEGALTEXT = 'G';
	/** Event "onIllegalElement". */
	public static final char ONILLEGALELEMENT = 'H';
	/** Event "onIllegalRoot". */
	public static final char ONILLEGALROOT = 'I';
	/** Event "create". */
	public static final char CREATE = 'J';
	/** Event "init". */
	public static final char INIT = 'K';
//	/** Event "match". */
//	public static final char MATCH = 'L';
	/** Event "finally". */
	public static final char FINALLY = 'M';
	/** Event "parse". */
	public static final char PARSE = 'N';

//	/** Event "match" in selector. */
//	public static final char SELECTORMATCH = 'a';
	/** Event "init" in selector. */
	public static final char SELECTORINIT = 'd';
	/** Event "create" in selector. */
	public static final char SELECTORCREATE = 'g';
	/** Event Selector "finally" in selector. */
	public static final char SELECTORFINALLY = 'j';
	/** Event "onAbsence" in selector. */
	public static final char SELECTORONABSENCE = 'm';
	/** Event "onExcess" in selector. */
	public static final char SELECTORONEXCESS = 'p';

	/** Open debugger.
	 * @param props Properties or null.
	 * @param xp XDPool.
	 */
	public void openDebugger(Properties props, XDPool xp);
	
	/** Close debugger and display message.
	 * @param msg message to be displayed.
	 */
	public void closeDebugger(String msg);

	/** Close debugger */
	public void closeDebugger();

	/** Get debug PrintStream.
	 * @return debug PrintStream.
	 */
	public PrintStream getOutDebug();

	/** Get debug InputStream.
	 * @return debug InputStream.
	 */
	public InputStream getInDebug();

	/** Set debug PrintStream.
	 * @param outDebug debug PrintStream.
	 */
	public void setOutDebug(PrintStream outDebug);

	/** Set debug InputStream.
	 * @param inDebug debug InputStream.
	 */
	public void setInDebug(InputStream inDebug);

	/** This method is called from x-script methods pause or trace.
	 * @param xxNode actual XXNode.
	 * @param code executive code.
	 * @param pc program counter.
	 * @param sp stack pointer.
	 * @param stack stack.
	 * @param localVariables array with local variables.
	 * @param debugInfo XMDebugInfo object.
	 * @param callList call list.
	 * @param stepMode step mode (NOSTEP, STEPINTO, STEPOVER).
	 * @return step mode.
	 * @throws Error if program should stop.
	 */
	public int debug(final XXNode xxNode,
		final XDValue[] code,
		final int pc,
		final int sp,
		final XDValue[] stack,
		final XDValue[] localVariables,
		final XMDebugInfo debugInfo,
		final XDCallItem callList,
		final int stepMode) throws Error;

	/** Clear XScript breakpoint area. */
	public void clearStopAddrs();

	/** Check if breakpoint area contains the stop address.
	 * @param addr where to search.
	 * @return true if breakpoint area contains the stop address.
	 */
	public boolean hasStopAddr(int addr);

	/** Set stop address to the breakpoint area.
	 * @param addr stop address. If value of argument is -1 debugger is closed.
	 */
	public void setStopAddr(int addr);

	/** Remove stop address from the breakpoint area.
	 * @param addr stop address.
	 * @return true stop address was removed.
	 */
	public boolean removeStopAddr(int addr);

	/** Clear XPos breakpoint area. */
	public void clearXPosArea();

	/** Check if breakpoint area contains the XPos item.
	 * @param xpos XPos item.
	 * @return true if breakpoint area contains the XPos item.
	 */
	public boolean hasXPos(String xpos);

	/** Set XPos item to the breakpoint area.
	 * @param xpos the string with XPos item.
	 */
	public void setXpos(String xpos);

	/** Remove XPos item from the breakpoint area.
	 * @param xpos the string with XPos item.
	 */
	public void removeXpos(String xpos);

}