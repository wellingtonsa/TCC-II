package br.ufc.great.caos.service.protocol.client.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {


    public static void extract(String zipFile, String location) {
        final int BUFFER_SIZE = 2048;
        int size;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = zin.getNextEntry();
                while (ze != null) {
                    String path = location +"/"+ ze.getName();
                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    }
                    else {
                        if (path.endsWith(".dex")) {
                            FileOutputStream fout = new FileOutputStream(path.replace("classes.dex", "metadata.caos"), false);
                            try {
                                while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
                                    fout.write(buffer, 0, size);
                                }
                                zin.closeEntry();
                            }
                            finally {
                                fout.flush();
                                fout.close();
                            }
                        }else {
                            zin.closeEntry();
                        }
                    }

                    ze = zin.getNextEntry();
               }

            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {
            Log.e("Unzip", "Unzip exception", e);
        }
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
