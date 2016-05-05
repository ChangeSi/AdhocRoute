package com.xd.adhocroute.utils;

import java.io.IOException;
import java.io.OutputStream;

public class ShellUtils {

    public static final String COMMAND_SU       = "su";
    public static final String COMMAND_SH       = "sh";
    public static final String COMMAND_EXIT     = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    
    public static boolean exec(String cmd) {
    	Process process;
		try {
			process = Runtime.getRuntime().exec("su");
			OutputStream os = process.getOutputStream();
			os.write(cmd.getBytes());
			os.flush();
			os.close();
			process.waitFor();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}