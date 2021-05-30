package model.entity;

import java.util.ArrayList;

public class Client {
	
	private Protocol protocol;
	private ArrayList<Message> messages;
	public Client(Protocol protocol) {
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
