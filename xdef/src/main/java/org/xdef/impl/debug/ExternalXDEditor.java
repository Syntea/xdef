package org.xdef.impl.debug;

import org.xdef.XDPool;
import org.xdef.impl.XDReader;
import org.xdef.impl.XDSourceItem;
import org.xdef.impl.XDWriter;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.SRuntimeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import org.xdef.XDFactory;

/** Provides tools for connection of an external editor of X-definitions.
 * @author Vaclav Trojan
 */
public abstract class ExternalXDEditor implements XEditor {

	/** Execute the external editor of X-definitions (must be implemented).
	 * The external editor MUST return the file with the result information.
	 * @param defPool filename of the file with XDPool.
	 * @param reports filename of the file with reports written by compiler.
	 * @param resultInfo the file name of the file where the external editor
	 * writes results of editing (if edition was finished and the map of
	 * source items).
	 * @throws Exception if an error occurs.
	 */
	abstract public void executeExternalXDEditor(String defPool,
		String reports,
		String resultInfo) throws Exception;

	@Override
	/** Set and open the XEditor.
	 * @param xp XDPool.
	 * @param err error reporter.
	 * @return if true the editing was finished otherwise XDPool will
	 * be recompiled.
	 */
	public boolean setXEditor(final XDPool xpool, final ArrayReporter reporter){
		try {
			// prepare files containing the xpool and reporter
			File poolFile = File.createTempFile("defPool", ".dp");
			poolFile.deleteOnExit();
			File reportFile = File.createTempFile("reports", ".rep");
			reportFile.deleteOnExit();
			File resultFile = File.createTempFile("result", ".txt");
			resultFile.deleteOnExit();
			resultFile.delete();
			// write XDPool files
			XDFactory.writeXDPool(poolFile, xpool);
			FileReportWriter frw = new FileReportWriter(reportFile);
			reporter.writeReports(frw);
			frw.close();
			// we need file names to pass as parameters (see "main"  method)
			String defPool = poolFile.getCanonicalPath();
			String reports = reportFile.getCanonicalPath();
			// the name of file where the external process will write
			// the result information
			String resultInfo = resultFile.getCanonicalPath();
			executeExternalXDEditor(defPool, reports, resultInfo);
			// wait max. 4 hours for the resultFile (14400 = 2*2*3600)
			for (int i = 0; i < 14400; i++) {
				if (waitForFileExists(resultFile)) {
					break;
				}
			}
			if (!resultFile.exists() || !resultFile.canRead()) {
				//No response from the external editor
				throw new SRuntimeException(XDEF.XDEF860);
			}
			if (resultFile.length() == 0) {
				//In the external editor is aleady opened the other project
				throw new SRuntimeException(XDEF.XDEF861);
			}
			// Read result information from the result file.
			FileInputStream fis = new FileInputStream(resultFile);
			XDReader xr = new XDReader(fis);
			// read flag if editing was finished
			boolean editingFinished = xr.readBoolean();
			int len = xr.readInt();
			Map<String,XDSourceItem> sources = xpool.getXDSourceInfo().getMap();
			// read and update source map
			for (int i = 0; i < len; i++) {
				String key = xr.readString();
				sources.put(key, XDSourceItem.readXDSourceItem(xr));
			}
			xr.close();
			// delete all created files (for sure)
			poolFile.delete();
			reportFile.delete();
			resultFile.delete();
			return editingFinished;
		} catch (Exception ex) {
			//Internal error&{0}{: }
			throw new SRuntimeException(SYS.SYS066, ex.getCause(), ex);
		}
	}

	/** Wait 0.5 sec for the file exists.
	 * @param f the checked file.
	 * @return true if the file exists.
	 * @throws InterruptedException if the task was interrupted.
	 */
	private static boolean waitForFileExists(final File f)
		throws InterruptedException {
		if (f.exists() && f.canRead()) {
			return true;
		}
		synchronized(f) {f.wait(500);} // 0.5 SEC
		return f.exists() && f.canRead();
	}

	@Override
	/** Close XEditor.
	 * @param msg text of message to be shown at the end of editing.
	 * If null no message is shown.
	 */
	public final void closeXEditor(final String msg) {}

	/** Create XDPool from the file. The external editor can use this method
	 * to get XDPool from the file it was prepared and passed to it.
	 * @param defPool filename of the file with XDPool.
	 * @return XDPool generated from the file from the argument defPool.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(final String defPool)
		throws IOException {
		File pool = new File(defPool);
		pool.deleteOnExit(); // we do not need this file more.
		try {
			XDPool result = XDFactory.readXDPool(pool);
			pool.delete();
			return result;
		} finally {
			pool.delete();
		}
	}

	/** Create ArrayReporter from the file. The external editor can use this
	 * method to get ArrayReporter from the file it was prepared and
	 * passed to it.
	 * @param reports filename with reports.
	 * @return ArrayReporter created from the file from the argument defPool.
	 * @throws Exception if an error occurs.
	 */
	public final static ArrayReporter readReporter(final String reports)
		throws Exception {
		File reps = new File(reports);
		reps.deleteOnExit(); // we do not need this file more.
		FileReportReader frr = new FileReportReader(reps);
		ArrayReporter ar = new ArrayReporter();
		ar.addReports(frr);
		frr.close();
		reps.delete();
		return ar;
	}

	/** Create file with results of editor. This file is used by
	 * the X-definition engine when the external editor finished.
	 * @param fileName the filename of the file to be created.
	 * @param editingFinished true if editing was finished, otherwise
	 * recompile XDPool with the files returned from editor.
	 * @param sourceItems map of description of items returned from the editor.
	 * @throws Exception if an error occurs.
	 */
	public final static void genResultFile(final String fileName,
		final boolean editingFinished,
		final Map<String, XDSourceItem> sourceItems) throws Exception {
		// create the tmp file
		File tmpFile = new File(fileName+".tmp");
		// for sure, it should be not an already existing file
		for (int i = 1; tmpFile.exists(); i++) {
			tmpFile = new File(fileName+".tmp" + i);
		}
		tmpFile.deleteOnExit();
		// write the result information to the the tmp file.
		XDWriter xw = new XDWriter(new FileOutputStream(tmpFile));
		// write editingFinished value
		xw.writeBoolean(editingFinished);
		// write sourcemap (because it might be changed!)
		if (sourceItems == null) { // never should happen
			xw.writeInt(0);
		} else {
			int len = sourceItems.size();
			xw.writeInt(len);
			for (Map.Entry<String, XDSourceItem> entry: sourceItems.entrySet()){
				xw.writeString(entry.getKey());
				entry.getValue().writeXDSourceItem(xw);
			}
		}
		xw.close();
		// rename the tmp file to the result file
		File resultFile = new File(fileName); // the result file
		resultFile.delete();
		int count = 0;
		for (; !tmpFile.renameTo(resultFile) && count < 100; count++) {
			synchronized(tmpFile) {
				tmpFile.wait(100); // wait 0.1 sec
			}
		}
		if (count >= 100) { // Can't rename file &{0} to &{1}
			throw new SRuntimeException(SYS.SYS031,
				tmpFile.getAbsolutePath(), resultFile.getAbsolutePath());
		}
	}
}