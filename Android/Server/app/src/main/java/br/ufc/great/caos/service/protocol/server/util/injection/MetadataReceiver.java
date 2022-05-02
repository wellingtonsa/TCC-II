package br.ufc.great.caos.service.protocol.server.util.injection;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MetadataReceiver extends Thread {

    final int BUFFER_SIZE = 2048;

    ServerSocket serverSocket;

    @Override
    public void run() {
        Socket socket = null;
        Log.i("MetadataReceiver", "Waiting for metadata");
        try {
            serverSocket = new ServerSocket(8088);

            while (true) {
                socket = serverSocket.accept();

                Log.i("MetadataReceiver", "Received Metadata");

                InputStream in = socket.getInputStream();
                String appName = "Client";
                /*try (DataInputStream d = new DataInputStream(in)) {
                    appName = d.readUTF();
                }*/

                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+appName).mkdirs();
                File file = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+appName), "metadata.caos");

                byte[] bytes = new byte[BUFFER_SIZE];
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                while (true) {
                    int bytesRead = in.read(bytes);
                    if (bytesRead <= 0) break;
                    bos.write(bytes, 0, bytesRead);
                    // Now it loops around to read some more.
                }


                Log.i("MetadataReceiver", "Metadata transfer completed successfully.");
                bos.close();
                socket.close();
                serverSocket.close();
                bos.close();
                in.close();

            }
        } catch (IOException e) {
            Log.i("MetadataReceiver", e.getMessage());
            e.printStackTrace();
        }
    }
}
