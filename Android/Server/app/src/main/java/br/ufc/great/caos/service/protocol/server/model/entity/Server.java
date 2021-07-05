package br.ufc.great.caos.service.protocol.server.model.entity;

import java.util.ArrayList;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class Server {
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
}
