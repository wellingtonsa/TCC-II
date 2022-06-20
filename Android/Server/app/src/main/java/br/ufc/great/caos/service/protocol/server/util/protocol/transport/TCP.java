package br.ufc.great.caos.service.protocol.server.util.protocol.transport;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;
import br.ufc.great.caos.service.protocol.core.offload.RemoteMethodExecutionService;


public class TCP implements ProtocolService {

	Context context;

	ServerSocket ss;
	Socket s;
	ObjectInputStream din;
	ObjectOutputStream dout;
	BufferedReader br;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port, Context context) {
		try {
			this.context = context;
			ss = new ServerSocket(port);
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

					s = ss.accept();
					dout = new ObjectOutputStream(s.getOutputStream());
					din = new ObjectInputStream(s.getInputStream());
					br = new BufferedReader(new InputStreamReader(System.in));

					request = (InvocableMethod) din.readObject();
					if(request != null) {
						RemoteMethodExecutionService remoteMethodExecution = new RemoteMethodExecutionService(context);
						Object response = remoteMethodExecution.executeMethod(request);

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
		return "TCP";
	}
}
