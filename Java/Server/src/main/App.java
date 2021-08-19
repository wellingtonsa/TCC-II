package main;

import model.entity.Server;
import model.services.ProtocolService;
import util.protocol.application.HTTP;
import util.protocol.application.MQTT;
import util.protocol.transport.TCP;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProtocolService sp = new TCP();
		Server s = new Server(sp);
		
		s.connect("192.168.1.7", 8045);
	}

}
