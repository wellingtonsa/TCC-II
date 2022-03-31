package br.ufc.great.caos.service.protocol.server.model.entity;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class Server extends AsyncTask<String, Void, Void> {
	private ProtocolService protocol;
	private ArrayList<Message> messages;

	public Server(ProtocolService protocol) {
		super();
		this.protocol = protocol;
	}
	
	public void connect(String ip, Integer port) {
		if(protocol.init()) {
			protocol.connect(ip, port);

		} 
		
	}
	public void disconnect() {
		protocol.disconnect();
	}

	@Override
	protected Void doInBackground(String... params) {
		String ip = params[0];
		Integer port = Integer.parseInt(params[1]);
		connect(ip, port);
		return null;
	}
}
