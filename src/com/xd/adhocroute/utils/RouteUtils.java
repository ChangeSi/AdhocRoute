package com.xd.adhocroute.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.xd.adhocroute.AdhocRouteApp;

import android.util.Log;

public class RouteUtils {
	public static void execCmd(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec("/system/xbin/su");
			OutputStream os = process.getOutputStream();
			os.write(cmd.getBytes());
			os.flush();
			InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line = null;
		    while (null != (line = br.readLine())) {
		        Log.e("########", line);
		    }
		    try {
		        process.waitFor();
		    } catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void execCmd2(String cmd) throws IOException {
	    Runtime runtime = Runtime.getRuntime();
	    Process process = runtime.exec(cmd);
	    InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    while (null != (line = br.readLine())) {
	        Log.e("########", line);
	    }     
	    try {
	        process.waitFor();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	}
	public static void exec(String cmd) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("/system/xbin/su"); 
        pb.directory(null);
       Process proc = pb.start();  
       //获取输入流，可以通过它获取SHELL的输出。   
       BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));  
       BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));  
       //获取输出流，可以通过它向SHELL发送命令。   
       PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc  
                       .getOutputStream())), true);  
       out.println(cmd);
       proc.waitFor();
      String line;  
       while ((line = in.readLine()) != null) {  
           Log.i(AdhocRouteApp.TAG,line);   // 打印输出结果
       }  
       while ((line = err.readLine()) != null) {  
           Log.e(AdhocRouteApp.TAG,line);  // 打印错误输出结果
       }  
       in.close();  
       out.close();  
       proc.destroy(); 
	}
}
