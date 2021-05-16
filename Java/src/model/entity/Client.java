package model.entity;

import java.util.ArrayList;

public class Client {
	private Protocol protocol;
	private ArrayList<Message> messages;
	public Client(Protocol protocol) {
		super();
		this.protocol = protocol;
	}
	
	
}
