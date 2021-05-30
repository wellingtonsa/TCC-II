package main;

import model.entity.Protocol;
import model.entity.Server;
import util.protocol.application.HTTP;
import util.protocol.application.MQTT;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Protocol sp = new HTTP();
		Server s = new Server(sp);
		
		s.connect();
	}

}
