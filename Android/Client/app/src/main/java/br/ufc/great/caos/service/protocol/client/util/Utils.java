package br.ufc.great.caos.service.protocol.client.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {


    public static void zip(File[] files, File zipFile ) {
        final int BUFFER_SIZE = 2048;

        BufferedInputStream origin = null;
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        } catch (FileNotFoundException e) {
            Log.e("Zip", "Unzip exception", e);
        }

        try {
            byte data[] = new byte[BUFFER_SIZE];
            for ( File file : files ) {
                FileInputStream fileInputStream = new FileInputStream( file );
                origin = new BufferedInputStream(fileInputStream, BUFFER_SIZE);

                String filePath = file.getAbsolutePath();

                try {
                    ZipEntry entry = new ZipEntry( filePath.substring( filePath.lastIndexOf("/") + 1 ) );

                    out.putNextEntry(entry);

                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } catch (IOException e) {
                    Log.e("Zip", "Unzip exception", e);
                } finally {
                    try {
                        origin.close();
                    } catch (IOException e) {
                        Log.e("Zip", "Unzip exception", e);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("Zip", "Unzip exception", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Log.e("Zip", "Unzip exception", e);
            }
        }
    }

    public static void unzip(String zipFile, String location) {
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
                            FileOutputStream fout = new FileOutputStream(path, false);
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

    public void doSomething(String test){
        Log.i("doSomething", "Do something with"+test);
    }
}
