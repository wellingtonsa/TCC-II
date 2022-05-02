package br.ufc.great.caos.service.protocol.client.util.injection;


import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.ufc.great.caos.service.protocol.client.util.Utils;

public class MetadataExtractor {

    private String name;
    private String packageName;
    private File file;
    private Context context;

    public MetadataExtractor() {
    }

    public MetadataExtractor(Context context, String packageName, PackageManager manager) {

        try {
            this.name = (String) manager.getApplicationLabel(context.getApplicationInfo());
            this.packageName = packageName;
            this.file = new File(manager.getPackageInfo(packageName, 0).applicationInfo.publicSourceDir);
            this.context = context;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File createFolder(String path) {
        File dir = new File(path);

        if (dir.isDirectory() || !dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public void extract() {
        createFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+name);
        ExtractThread extractThread = new ExtractThread(context, file,  Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+ name);
        extractThread.start();

    }

    class ExtractThread extends Thread {
        private File file, newFile;
        private Context context;
        private String path;

        public ExtractThread(Context context, File file, String path) {
            this.file = file;
            this.context = context;
            this.path = path;
        }

        @Override
        public void run() {
            super.run();
            Utils.extract(file.getAbsolutePath(), path);
            Log.d("ExceptionApk", "Extraction finished!");

        }
    }

}
