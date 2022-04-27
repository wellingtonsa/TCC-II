package br.ufc.great.caos.service.protocol.client.model.entity;

import android.os.AsyncTask;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

public class Client extends AsyncTask<String, Void, Client> {
	
	private ProtocolService protocol;
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
	
	
	public Object executeOffload(InvocableMethod method) {
		return protocol.executeOffload(method);
	}

	@Override
	protected Client doInBackground(String... params) {
		String ip = params[0];
		Integer port = Integer.parseInt(params[1]);
		connect(ip, port);
		return this;
	}
}
