package main;

import model.entity.Client;
import model.services.ProtocolService;
import util.protocol.application.HTTP;
import util.protocol.application.MQTT;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProtocolService cp = new MQTT();
		Client c = new Client(cp);
		
		c.connect("http://127.0.0.1:8001");
		System.out.println(c.sendMessage("Wellington"));
	}

}
