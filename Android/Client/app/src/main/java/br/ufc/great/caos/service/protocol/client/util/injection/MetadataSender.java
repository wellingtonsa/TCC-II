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

        try {
            socket =  new Socket(ip,port);

            DataOutputStream d = new DataOutputStream(socket.getOutputStream());
            d.writeUTF(appName);
            d.writeLong(file.length());
            d.close();
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
