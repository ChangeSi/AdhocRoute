package com.xd.adhocroute.utils;

import java.io.BufferedReader;
import java.io.OutputStream;

import com.xd.adhocroute.log.Lg;
import com.xd.adhocroute.route.RouteServices;

public class ShellUtils {

    private static final String CHECK_ROOT = "cd /data/data";
	public static final String COMMAND_SU = "su";
    
	public static void safeStopOlsrd(String olsrdKillCMD) {
    	exec(olsrdKillCMD);
    	int i = 0;
    	
    	while (i < 5 && !checkOlsrStoped()/*检查是否已经启动了这个进程*/) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		exec(olsrdKillCMD);
    		i++;
    	}
    	if(5 == i) {
    	}
	}
	
	
	
	
	private static boolean checkOlsrStoped() {
		return true;
	}




	public static void safeStartOlsrd(String olsrdStartCMD) {
    	exec(olsrdStartCMD);
    	int i = 0;
    	while (i < 5 && !checkOlsrStarted()/*检查是否已经启动了这个进程*/) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		exec(olsrdStartCMD);
    		i++;
    	}
    	if(5 == i) {
    		// 10次都没有启动成功
    		// 提示用户
    		//:TODO
    	}
	}
	
	private static boolean checkOlsrStarted() {
		try {
			Process process = Runtime.getRuntime().exec("su");
			OutputStream os = process.getOutputStream();
			os.write("ps".getBytes());
			os.close();
			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
			String line = br.readLine();;
	        while(line != null){
	            if (line.contains(RouteServices.CMD_OLSR)) {
					return true;
				}
	            line = br.readLine();
	        }
			process.waitFor();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
    public static boolean exec(String cmd) {
    	Lg.d("adhocroute-test 开始执行exec");
    	Process process;
		try {
			process = Runtime.getRuntime().exec(COMMAND_SU);
			OutputStream os = process.getOutputStream();
			os.write(cmd.getBytes());
			os.flush();
			os.close();
			process.waitFor();
			Lg.d("adhocroute-test 执行exec正常");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Lg.d("adhocroute-test 执行exec异常");
			return false;
		}
		
	}
    
    // 判断是否root
    private boolean isDeviceRoot() {
		Process process;
		try {
			process = Runtime.getRuntime().exec(COMMAND_SU);
			OutputStream os = process.getOutputStream();
			os.write(CHECK_ROOT.getBytes());
			os.flush();
			os.close();
			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
			String line = br.readLine();
            StringBuilder sb = new StringBuilder();
			while(line != null){
                sb.append(line);
                line = br.readLine();
            }
			process.waitFor();
			if (sb.toString().length() > 50) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
    
}