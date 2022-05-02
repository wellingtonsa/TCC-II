package br.ufc.great.caos.service.protocol.server.util.injection;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import dalvik.system.DexClassLoader;

public class MetadataLoader {

    private Context context;
    private DexClassLoader classLoader;

    public MetadataLoader(Context context){
        this.context = context;
    }

    public DexClassLoader getClassLoader(){
        return this.classLoader;
    }

    public void loadMetadata(String applicationName){
        try {

            File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE),
                    "classes.dex");
            BufferedInputStream bis = null;
            OutputStream dexWriter = null;

            int BUF_SIZE = 8 * 1024;
            File file = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+applicationName), "metadata.caos");
            FileInputStream fileInputStream = new FileInputStream(file);
            bis = new BufferedInputStream(fileInputStream);
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            fileInputStream.close();
            dexWriter.close();
            bis.close();

            File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);

            this.classLoader = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                    optimizedDexOutputPath.getAbsolutePath(),
                    null,
                    getClassLoader());

            Log.i("MetadataLoader", "Metadata loaded successfully.");
        } catch (Exception e) {
            Log.i("MetadataLoader", "Failed to load the metadata.");
            e.printStackTrace();
        }
    }
}
