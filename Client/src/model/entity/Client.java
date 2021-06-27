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
	
	public void connect(String address) {
		if(protocol.init()) {
			protocol.connect(address);
		}
	}
	
	
	public String sendMessage(String message) {
		return protocol.sendMessage(message);
	}
}
