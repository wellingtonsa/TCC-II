package br.ufc.great.caos.service.protocol.server.util.protocol.transport;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.rudp.ReliableServerSocket;
import net.rudp.ReliableSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;
import br.ufc.great.caos.service.protocol.core.offload.RemoteMethodExecutionService;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;

public class RUDP implements ProtocolService {

    Context context;

    ReliableServerSocket ss;
    ReliableSocket s;
    ObjectInputStream din;
    ObjectOutputStream dout;

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean connect(String ip, Integer port, Context context) {
        try {
            this.context = context;
            ss = new ReliableServerSocket(port);
            Log.i(isInstanceOf(), "Server started on port " + port);

            new RequestHandler().start();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(isInstanceOf(), "Connection error: " + e.getMessage());
            return false;
        }

    }

    @Override
    public boolean disconnect() {
        try {
            din.close();
            s.close();
            ss.close();
            return true;
        } catch (IOException e) {
            Log.i(isInstanceOf(), "Error to disconnect:" + e.getMessage());
            return false;
        }

    }

    private class RequestHandler extends Thread {

        @Override
        public void run() {
            InvocableMethod request = null;
            try {

                while (true) {

                    s = (ReliableSocket) ss.accept();

                    din = new ObjectInputStream(s.getInputStream());

                    request = (InvocableMethod) din.readObject();

                    if(request != null) {
                        RemoteMethodExecutionService remoteMethodExecution = new RemoteMethodExecutionService(context);
                        Object response = remoteMethodExecution.executeMethod(request);

                        dout = new ObjectOutputStream(s.getOutputStream());

                        dout.writeObject(response);
                        dout.flush();
                    }
                }
            } catch (IOException e) {
                Log.i(isInstanceOf(), "Error to proccess:" + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.i(isInstanceOf(), "Error to proccess:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public String isInstanceOf() {
        return "RUDP";
    }
}
