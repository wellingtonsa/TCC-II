package br.ufc.great.caos.service.protocol.client.util.protocol.transport;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import net.rudp.ReliableSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;


public class RUDP implements ProtocolService {
    ReliableSocket s;
    ObjectInputStream din;
    ObjectOutputStream dout;
    long start = System.currentTimeMillis();
    long elapsed = 0;

    @Override
    public boolean init() {
        try {
            s = new ReliableSocket();
            return true;
        } catch (IOException e) {
            Log.i(isInstanceOf(), "Failed to init "+e.getMessage());
            return false;
        }

    }

    @Override
    public boolean connect(String ip, Integer port) {
        try {
            s = new ReliableSocket();
            s.connect(new InetSocketAddress(ip, port));
            Log.i(isInstanceOf(), "Connected to the server ("+ip+") at port: "+port);
            return true;
        } catch (UnknownHostException e) {
            Log.i(isInstanceOf(), "Connection error: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.i(isInstanceOf(), "Connection error: " + e.getMessage());
            return false;
        }

    }

    @Override
    public boolean disconnect() {
        try {
            if(din != null && dout != null && s != null) {
                din.close();
                dout.close();
                s.close();
            }
            return true;
        } catch (IOException e) {
            Log.i(isInstanceOf(), "Error to disconnect:" + e.getMessage());
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Object executeOffload(InvocableMethod method) {
        start = System.currentTimeMillis();
        method.setTimestamp(start);
        Object response = "";
        try {
            if(method != null) {

                dout= new ObjectOutputStream(s.getOutputStream());
                dout.writeObject(method);

                dout.flush();

                din=new ObjectInputStream(s.getInputStream());
                response = din.readObject();

                elapsed = System.currentTimeMillis() - start;
                Log.i("TIMESTAMP", "TOTAL:"+isInstanceOf()+":"+String.valueOf(elapsed));

            }
            return response;
        } catch (IOException e) {
            Log.i(isInstanceOf(), "Error to send a message:"+e.getMessage());
            return "";
        } catch (ClassNotFoundException e) {
            Log.i(isInstanceOf(), "Error to send a message:"+e.getMessage());
        }

        return response;
    }


    @Override
    public String isInstanceOf() {
        return "RUDP";
    }

}
