package br.ufc.great.caos.service.protocol.server.model.entity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class Server extends AsyncTask<String, Void, Void> {
	private ProtocolService protocol;
	private Context context;
	private ArrayList<Message> messages;

	public Server(ProtocolService protocol, Context context) {
		super();
		this.context = context;
		this.protocol = protocol;
	}
	
	public void connect(String ip, Integer port, Context context) {
		if(protocol.init()) {
			protocol.connect(ip, port, context);
		} 
		
	}

	public void disconnect() {
		protocol.disconnect();
	}

	@Override
	protected Void doInBackground(String... params) {
		String ip = params[0];
		Integer port = Integer.parseInt(params[1]);
		connect(ip, port, context);
		return null;
	}
}
