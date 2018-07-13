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
import static cz.syntea.xdef.sys.FUtils.deleteAll;
import cz.syntea.xdef.sys.RegisterReportTables;
import java.io.File;

/** Update message generated files.
 * @author Trojan
 */
public class UpdateMessages {
	
	/** Generate error message files.
	 * @param args not used
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception{
		File msgDir = new File("src/cz/syntea/xdef/msg/");
		if (!msgDir.exists() || !msgDir.isDirectory()) {
			msgDir = new File("src/main/java/cz/syntea/xdef/msg/");
		}
		File temp = new File("temp");
		temp.mkdir();
		deleteAll(temp, true);
		temp.mkdir();
		String msgPath = msgDir.getAbsolutePath();
		msgPath = msgPath.replace('\\', '/');
		if (!msgPath.endsWith("/")) {
			msgPath += '/';
		}
		RegisterReportTables.main(new String[] {
			"-i", msgSourceDir + "*.properties",
			"-c", "UTF-8",
			"-p", "cz.syntea.xdef.msg",
			"-o", temp.getAbsolutePath(),
			"-r"});
		String msg = FUtils.updateDirectories(temp, msgDir, "java", true, false);
		if (msg.isEmpty()) {
			System.out.println("No report Java sources were changed");
		} else {
			System.out.println(msg);
		}
		deleteAll(temp, true);
	}
	
}
