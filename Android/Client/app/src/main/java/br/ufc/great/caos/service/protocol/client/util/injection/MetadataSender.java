package br.ufc.great.caos.service.protocol.client.util.injection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

public class MetadataSender extends Thread {

    final int BUFFER_SIZE = 2048;

    String ip;
    Integer port;
    String appName;
    Socket socket;

    public MetadataSender(String ip,  Context context, PackageManager manager){
        this.ip = ip;
        this.port = 8088;
        this.appName = (String) manager.getApplicationLabel(context.getApplicationInfo());;
    }

    @Override
    public void run() {


        File file = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+appName), "metadata.caos");

        byte[] bytes = new byte[(int) file.length()];

        BufferedInputStream bis;
        try {
            socket =  new Socket(ip,port);

            Log.i("MetadataSender","Sent metadata to "+ip+":"+port);

            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bytes, 0, bytes.length);
            OutputStream os = socket.getOutputStream();

            /*try (DataOutputStream d = new DataOutputStream(os)) {
                d.writeUTF(appName);
            }
            os.flush();*/

            os.write(bytes, 0, bytes.length);
            os.flush();

            socket.close();

            Log.i("MetadataSender","Sent metadata to "+ip+":"+port);

        } catch (FileNotFoundException e) {
            Log.i("MetadataSender",e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("MetadataSender", e.getMessage());
            e.printStackTrace();
        }
    }
}
