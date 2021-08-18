package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class MQTT implements ProtocolService {

	private MqttClient server;
	private final String BROKER_URL = "tcp://192.168.1.7:1884";

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			Callback cb = new Callback();
			MemoryPersistence persistence = new MemoryPersistence();
			server = new MqttClient(BROKER_URL, UUID.randomUUID().toString(), persistence);
			server.connect();
			server.setCallback(cb);
			server.subscribe("/offloading/init", 0);
			Log.i("MQTT", "Server running at /offloading/init");
			return true;
		} catch (MqttException e) {
			Log.i("MQTT", "Connection error: "+e.getMessage());
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean disconnect() {
		try {
			server.disconnect();
			return true;
		} catch (MqttException e) {
			Log.i("MQTT", "Error to disconnect:"+e.getMessage());
			return false;
		}

	}

	@Override
	public String isInstanceOf() {
		return "MQTT";
	}


	class Callback implements MqttCallback {

		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			String parsedMessage = "Welcome " + message.toString();

			server.publish("/offloading/result", new MqttMessage(parsedMessage.getBytes()));

		}

	}

}
