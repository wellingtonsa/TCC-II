package main;

import model.entity.Server;
import model.services.ProtocolService;
import util.protocol.application.HTTP;
import util.protocol.application.MQTT;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProtocolService sp = new MQTT();
		Server s = new Server(sp);
		
		s.connect();
	}

}
