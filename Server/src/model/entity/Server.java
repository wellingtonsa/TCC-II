package model.entity;

import java.util.ArrayList;

public class Server {
	private Protocol protocol;
	private ArrayList<Message> messages;
	public Server(Protocol protocol) {
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
