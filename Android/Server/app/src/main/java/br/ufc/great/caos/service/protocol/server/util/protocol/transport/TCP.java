package br.ufc.great.caos.service.protocol.server.util.protocol.transport;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class TCP implements ProtocolService {

	ServerSocket ss;
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
		try {
			ss = new ServerSocket(port);
			Log.i("TCP", "Server started on port " + port);
			waitingForRequests();

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("TCP", "Connection error: " + e.getMessage());
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
			Log.i("TCP", "Error to disconnect:" + e.getMessage());
			return false;
		}

	}

	private void waitingForRequests() {
		String request = "";

		try {

			s = ss.accept();
			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(System.in));

			while (true) {

				request = din.readUTF();
				dout.writeUTF("Welcome " + request);
				dout.flush(); 
			}
		} catch (IOException e) {
			Log.i("TCP", "Error to proccess:" + e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public String isInstanceOf() {
		return "TCP";
	}
}
