/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: ExternalXDEditor.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.debug;

import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.impl.XDReader;
import cz.syntea.xdef.impl.XDSourceItem;
import cz.syntea.xdef.impl.XDWriter;
import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.SRuntimeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/** Provides tools fro connecting an external editor to X-definitions.
 * @author Trojan
 */
public abstract class ExternalXDEditor implements XEditor {

	@Override
	/** Open the GUI.
	 * @param xp XDPool.
	 * @param err error reporter.
	 * @return if true the GUI was finished else recompile is supposed.
	 */
	public boolean setXEditor(final XDPool xpool, final ArrayReporter reporter){
		try {
			// prepare files containing the xpool and reporter
			File poolFile = File.createTempFile("defPool", "dp");
			poolFile.deleteOnExit();
			File reportFile = File.createTempFile("reports", "rep");
			reportFile.deleteOnExit();
			File resultFile = File.createTempFile("result", "txt");
			resultFile.deleteOnExit();

			// write data to files
			xpool.writeXDPool(poolFile);
			reporter.writeReportsToFile(reportFile);

			// we need file names to pass as parameters (see "main"  method)
			String defPool = poolFile.getAbsolutePath();
			String reports = reportFile.getAbsolutePath();
			// the name of file where the external process will write
			// the result information
			String resultInfo = resultFile.getAbsolutePath();
			executeExternalXDEditor(defPool, reports, resultInfo);

			// Read result information from the result file.
			FileInputStream fis = new FileInputStream(resultFile);
			XDReader xr = new XDReader(fis);
			// read flag if editing was finished
			boolean editingFinished = xr.readBoolean();
			int len = xr.readInt();
			Map<String, XDSourceItem> sources = xpool.getXDSourcesMap();
			// read and update source map
			for (int i = 0; i < len; i++) {
				String key = xr.readString();
				sources.put(key, XDSourceItem.readXDSourceItem(xr));
			}
			// delete unnecessary files (for sure)
			poolFile.delete();
			reportFile.delete();
			resultFile.delete();
			return editingFinished;
		} catch (Exception ex) {
			//Internal error&{0}{: }
			throw new SRuntimeException(
				SYS.SYS066, ex.getCause(), ex.getMessage());
		}
	}

	@Override
	/** Close XEditor.
	 * @param msg text of message to be shown. If null no message is shown.
	 */
	public final void closeXEditor(final String msg) {}

	/** Execute the external editor of X-definitions (must be implemented).
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

	/** Create XDPool from the file.
	 * @param defPool filename of the file with XDPool.
	 * @return XDPool generated from the file from the argument defPool.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(
		final String defPool) throws IOException {
		File pool = new File(defPool);
		pool.deleteOnExit(); // we do not need this file more.
		XDPool result = XDFactory.readXDPool(pool);
		pool.delete();
		return result;
	}

	/** Create ArrayReporter from the file.
	 * @param reports filename with reports.
	 * @return ArrayReporter created from the file from the argument defPool.
	 * @throws Exception if an error occurs.
	 */
	public final static ArrayReporter readReporter(final String reports)
		throws Exception {
		File reps = new File(reports);
		reps.deleteOnExit(); // we do not need this file more.
		ArrayReporter ar = new ArrayReporter();
		ar.addReportsFromFile(reps);
		reps.delete();
		return ar;
	}

	/** Create file with results of editor.
	 * @param fileName the filename of the file to be created.
	 * @param editingFinished true if editing was finished, otherwise
	 * recompile XDPool with the files returned from editor.
	 * @param sourceItems map of description of items returned from the editor.
	 * @throws Exception if an error occurs.
	 */
	public final static void genResultFile(final String fileName,
		final boolean editingFinished,
		final Map<String, XDSourceItem> sourceItems) throws Exception {
		File resultFile = new File(fileName);
		// write the result information to the the result file.
		FileOutputStream fos = new FileOutputStream(resultFile);
		XDWriter xw = new XDWriter(fos);
		// write editingFinished value
		xw.writeBoolean(editingFinished);
		// write sourcemap (it might be changed!)
		if (sourceItems == null || sourceItems.isEmpty()) {
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
	}

}