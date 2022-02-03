package br.ufc.great.caos.service.protocol.client.model.entity;

import android.os.AsyncTask;

import java.util.ArrayList;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;

public class Client extends AsyncTask<String, Void, Client> {
	
	private ProtocolService protocol;
	private ArrayList<Message> messages;
	public Client(ProtocolService protocol) {
		super();
		this.protocol = protocol;
	}
	
	public void connect(String ip, Integer port) {
		if(protocol.init()) {
			protocol.connect(ip, port);
		}
	}

	public boolean disconnect() {
		return protocol.disconnect();
	}
	
	
	public String sendMessage(String message) {
		return protocol.sendMessage(message);
	}

	@Override
	protected Client doInBackground(String... params) {
		String ip = params[0];
		Integer port = Integer.parseInt(params[1]);
		connect(ip, port);
		return this;
	}
}
