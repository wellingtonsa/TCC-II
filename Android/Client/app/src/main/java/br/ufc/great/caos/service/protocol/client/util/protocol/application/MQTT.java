package br.ufc.great.caos.service.protocol.client.util.protocol.application;


import android.util.Log;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MQTT implements ProtocolService {


	private MqttClient client;
	private final String BROKER_URL = "tcp://192.168.0.55:1885";
	long start = System.currentTimeMillis();
	long elapsed = 0;

	String response = "";


	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			Callback cb = new Callback();
			MemoryPersistence persistence = new MemoryPersistence();
			client = new MqttClient(BROKER_URL, UUID.randomUUID().toString(), persistence);
			client.connect();
			client.setCallback(cb);
			client.subscribe("/offloading/result", 0);
			Log.i("MQTT", "Connected to the server");
			return true;
		} catch (MqttException e) {
			Log.i("MQTT", "Connection error:"+e.getMessage());
			return false;
		}

	}

	@Override
	public boolean disconnect() {
		try {
			if(client != null) client.disconnect();
			return true;
		} catch (MqttException e) {
			Log.i("MQTT", "Error to disconnect:"+e.getMessage());
			return false;
		}

	}

	@Override
	public String sendMessage(String message) {
		start = System.currentTimeMillis();
		try {
			client.publish("/offloading/init", new MqttMessage(message.getBytes()));

			while(response.isEmpty()){

			}
			return response;
		} catch (MqttPersistenceException e) {
			Log.i("MQTT", "Error to send a message:"+e.getMessage());
			return "";
		} catch (MqttException e) {
			Log.i("MQTT", "Error to send a message:"+e.getMessage());
			return "";
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
			elapsed = System.currentTimeMillis() - start;
			response = message.toString();
			Log.i(isInstanceOf(), String.valueOf(elapsed));
		}

	}
}
