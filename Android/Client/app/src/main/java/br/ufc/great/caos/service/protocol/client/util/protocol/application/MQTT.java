package br.ufc.great.caos.service.protocol.client.util.protocol.application;


import android.util.Log;

import com.google.gson.Gson;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

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
	long start = System.currentTimeMillis();
	long elapsed = 0;

	Object response = null;


	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			Callback cb = new Callback();
			MemoryPersistence persistence = new MemoryPersistence();

			String BROKER_URL = "tcp://"+ip+":1883";

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
	public Object executeOffload(InvocableMethod method) {
		start = System.currentTimeMillis();
		try {
			Gson gson = new Gson();
			byte[] out = gson.toJson(method).getBytes();

			client.publish("/offloading/init", new MqttMessage(out));

			while(response == null){}

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
			response = new Gson().fromJson(message.toString(), Object.class);
			Log.i(isInstanceOf(), String.valueOf(elapsed));
		}

	}
}
