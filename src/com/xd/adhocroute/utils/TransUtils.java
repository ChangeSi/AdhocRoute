//package com.xd.adhocroute.utils;
//
//
//
//public class TransUtils {
//
//	public static int toByteArray(int src, byte[] destBuf, int indest) {
////		byte[] tempbuf = new byte[4];
//		destBuf[indest] = (byte) (src & 0xff);
//		destBuf[indest+1] = (byte) (src >> 8 & 0xff);
//		destBuf[indest+2] = (byte) (src >> 16 & 0xff);
//		destBuf[indest+3] = (byte) (src >> 24 & 0xff);
//
////		System.arraycopy(tempbuf, 0, destBuf, indest, 4);
//		return 4;
//	}
//	public static int byteArray2int(byte[] data, int index) {
//		int i = 0;
//		try {
//			i = ((((int) data[index + 0] & 0xff) << 0)
//					| (((int) data[index + 1] & 0xff) << 8)
//					| (((int) data[index + 2] & 0xff) << 16) | (((int) data[index + 3] & 0xff) << 24));
//		} catch (ArrayIndexOutOfBoundsException e) {
//			e.printStackTrace();
//			i = 0;
//		}
//		return i;
//	}
//	public static byte int2Byte(int val) {
////		return (byte)val;
//		return (byte) (val > 127 ? val-256:val);
//	}
//	
//	public static int byte2Int(byte val) {
////		//仅将byte当成负数时
////		return 0xff & val;
//		if (val < 0) {
//			return val+256;
//		}else{
//			return val;
//		}
//	}
//	
////	public static String getNICName() {
////		if (android.os.Build.MODEL.contains("MI")) {
////			return "eth0";
////		}
////		return "wlan0";
////	}
//}
