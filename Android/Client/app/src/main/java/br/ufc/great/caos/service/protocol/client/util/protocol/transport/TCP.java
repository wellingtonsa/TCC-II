package br.ufc.great.caos.service.protocol.client.util.protocol.transport;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;


public class TCP implements ProtocolService {
	Socket s;
	DataInputStream din;
	DataOutputStream dout;
	BufferedReader br;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		Socket s;
		try {
			s = new Socket(ip,port);
			din=new DataInputStream(s.getInputStream());  
			dout=new DataOutputStream(s.getOutputStream());  
			br=new BufferedReader(new InputStreamReader(System.in));
			Log.i("TCP", "Connected to the server ("+ip+") at port: "+port);
			return true;
		} catch (UnknownHostException e) {
			Log.i("TCP", "Connection error: " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.i("TCP", "Connection error: " + e.getMessage());
			return false;
		}  

	}

	@Override
	public boolean disconnect() {
		try {
			din.close();
			s.close();
			return true;
		} catch (IOException e) {
			Log.i("TCP", "Error to disconnect:" + e.getMessage());
			return false;
		}
	}

	@Override
	public String sendMessage(String message) {
		try {
			dout.writeUTF(message);
			Log.i("TCP", "Message "+message+" sent.");
			dout.flush();  
			return din.readUTF();  
		} catch (IOException e) {
			Log.i("TCP", "Error to send a message:"+e.getMessage());
			return "";
		}  

	}

	@Override
	public String isInstanceOf() {
		return "TCP";
	}


}
