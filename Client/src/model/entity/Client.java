package model.entity;

import java.util.ArrayList;

import model.services.ProtocolService;

public class Client {
	
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
	
	
	public String sendMessage(String message) {
		return protocol.sendMessage(message);
	}
}
