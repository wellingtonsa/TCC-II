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
    BufferedOutputStream bos;
    DataInputStream d;

    @Override
    public void run() {
        Socket socket = null;
        Log.i("MetadataReceiver", "Waiting for metadata");
        try {
            serverSocket = new ServerSocket(8088);

            String appName = "";
            int fileLength = 0;

            while (true) {
                socket = serverSocket.accept();

                Log.i("MetadataReceiver", "Received Metadata");
                d = new DataInputStream(socket.getInputStream());
                appName = d.readUTF();
                fileLength = (int) d.readLong();


                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+appName).mkdirs();
                File file = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/CAOS/"+appName), "metadata.caos");

                byte[] bytes = new byte[fileLength];
                bos = new BufferedOutputStream(new FileOutputStream(file));

                bos.write(bytes, 0, fileLength);


                Log.i("MetadataReceiver", "Metadata transfer completed successfully.");

                if(!appName.isEmpty() && fileLength > 0) {
                    bos.close();
                    socket.close();
                    serverSocket.close();
                    d.close();
                }

            }
        } catch (IOException e) {
            try {
                d.close();
                bos.close();
                socket.close();
                serverSocket.close();
            } catch (IOException ioException) {
                Log.i("MetadataReceiver", ioException.getMessage());
                ioException.printStackTrace();
            }
        }
    }
}
