package model.entity;

import java.util.ArrayList;

import model.services.ProtocolService;

public class Server {
	private ProtocolService protocol;
	private ArrayList<Message> messages;
	public Server(ProtocolService protocol) {
		super();
		this.protocol = protocol;
	}
	
	public void connect() {
		if(protocol.init()) {
			protocol.connect("");
		} 
		
	}
	public void disconnect() {
		protocol.disconnect();
	}
}
