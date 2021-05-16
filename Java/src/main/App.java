package main;

import model.entity.Client;
import model.entity.Protocol;
import util.protocol.application.HTTP;
import util.protocol.application.MQTT;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Protocol cp = new MQTT();
		Client c = new Client(cp);
	}

}
