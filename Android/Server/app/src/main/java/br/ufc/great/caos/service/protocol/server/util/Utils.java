package br.ufc.great.caos.service.protocol.server.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.*;

public class Utils {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for(int idx=0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }


    public static byte[] getUTF8Bytes(String str) {
        try { return str.getBytes("UTF-8"); } catch (Exception ex) { return null; }
    }

    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN=1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8=false;
            int read,count=0;
            while((read=is.read(bytes)) != -1) {
                if (count==0 && bytes[0]==(byte)0xEF && bytes[1]==(byte)0xBB && bytes[2]==(byte)0xBF ) {
                    isUTF8=true;
                    baos.write(bytes, 3, read-3);
                } else {
                    baos.write(bytes, 0, read);
                }
                count+=read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try{ is.close(); } catch(Exception ignored){}
        }
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:",aMac));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
        return "";
    }

    public static byte[] serializeObject(Object obj) {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;

            oos = new ObjectOutputStream(bytesOut);

            oos.writeObject(obj);
            oos.flush();
            byte[] bytes = bytesOut.toByteArray();
            bytesOut.close();
            oos.close();
            return bytes;
        } catch (IOException e) {
            Log.i("serializeObject", "Error to serialize the object: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Object deserializeObject(byte[] data){
        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bytesIn);
            Object obj = (Object) ois.readObject();
            ois.close();

            return obj;
        } catch( IOException e){
            Log.i("deserializeObject", "Error to deserialize the object: "+e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Log.i("deserializeObject", "Error to find the class: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
