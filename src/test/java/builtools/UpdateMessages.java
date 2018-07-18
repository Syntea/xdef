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

import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.RegisterReportTables;
import java.io.File;

/** Update registered message files.
 * @author Trojan
 */
public class UpdateMessages {

	/** Generate error message files.
	 * @param args not used
	 */
	public static void main(String... args) {
		File dir = new File("src/cz/syntea/xdef/msg/");
		if (!dir.exists() || !dir.isDirectory()) {
			dir = new File("src/main/java/cz/syntea/xdef/msg/");
		}
		File temp = new File("temp");
		temp.mkdir();
		try {
			FUtils.deleteAll(temp, true);
			temp.mkdir();
			String msgPath = dir.getAbsolutePath();
			msgPath = msgPath.replace('\\', '/');
			if (!msgPath.endsWith("/")) {
				msgPath += '/';
			}
			RegisterReportTables.main(new String[] {
				"-i", msgPath + "*.properties",
				"-c", "UTF-8",
				"-p", "cz.syntea.xdef.msg",
				"-o", temp.getAbsolutePath(),
				"-r"});
			String msg = 
				FUtils.updateDirectories(temp, dir, "java", true, false);
			if (msg.isEmpty()) {
				System.out.println(
					"Nothing changed in registered report files");
			} else {
				System.out.println(msg); // print changes
			}
			
			FUtils.deleteAll(temp, true); // delete temp directory
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}