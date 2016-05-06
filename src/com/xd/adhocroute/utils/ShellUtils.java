package com.xd.adhocroute.utils;

import java.io.OutputStream;

public class ShellUtils {

    public static final String COMMAND_SU       = "su";
    
    public static boolean exec(String cmd) {
    	Process process;
		try {
			process = Runtime.getRuntime().exec(COMMAND_SU);
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