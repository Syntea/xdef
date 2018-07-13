/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 * 
 * File: UpdateMessages.java, created 2018-07-13.
 * 
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package builtools;

import cz.syntea.xdef.sys.ExtensionFileFilter;
import cz.syntea.xdef.sys.FUtils;
import static cz.syntea.xdef.sys.FUtils.deleteAll;
import cz.syntea.xdef.sys.RegisterReportTables;
import java.io.File;
import java.io.FileFilter;

/** Update message generated files.
 * @author Trojan
 */
public class UpdateMessages {
	
	/** Generate error message files.
	 * @param args not used
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception{
		String msgSourceDir = new File(
			"src/main/java/cz/syntea/xdef/msg/").getAbsolutePath();
		msgSourceDir = msgSourceDir.replace('\\', '/');
		if (!msgSourceDir.endsWith("/")) {
			msgSourceDir += '/';
		}
		File out = new File("temp");
		out.mkdir();
		deleteAll(out, true);
		out.mkdir();
		RegisterReportTables.main(new String[] {
			"-i", msgSourceDir + "*.properties",
			"-c", "windows-1250",
			"-p", "cz.syntea.xdef.msg",
			"-o", out.getAbsolutePath(),
			"-r"});
		FileFilter javaFiles = new ExtensionFileFilter("java");
		File[] fmsg = new File(msgSourceDir).listFiles(javaFiles);
		boolean changed = false;
		for (File f: fmsg) {
			String name = f.getName();
			if (!new File(out, name).exists()) {
				System.out.println("Delete: " + name);
				changed = true;
			}
		}
		File[] fout = out.listFiles();
		for (File f: fout) {
			String name = f.getName();
			File oldf = new File(msgSourceDir, name);
			if (oldf.exists()) {
				if (FUtils.compareFile(f, oldf) != -1L) {
					FUtils.copyToFile(f, oldf);
					System.out.println("changed: " + name);
					changed = true;				
				}
			} else {
				System.out.println("new: " + name);
				FUtils.copyToFile(f, oldf);
				changed = true;				
			}
		}		
		if (changed) {
			System.out.println("New report classes generated to "+msgSourceDir);
		} else {
			System.out.println("No new report classes generated");
		}
		deleteAll(out, true);
	}
	
}
