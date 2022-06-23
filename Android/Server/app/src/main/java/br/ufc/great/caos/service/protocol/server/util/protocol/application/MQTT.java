package br.ufc.great.caos.service.protocol.server.util.protocol.application;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.eclipse.moquette.server.Server;

import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;
import br.ufc.great.caos.service.protocol.core.offload.RemoteMethodExecutionService;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class MQTT implements ProtocolService {

	private Context context;
	private MqttClient server;


	@Override
	public boolean init() {
		Broker broker = new Broker();
		broker.run();
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port, Context context) {
		try {
			this.context = context;

			Callback cb = new Callback();
			MemoryPersistence persistence = new MemoryPersistence();

			String BROKER_URL = "tcp://localhost:1883";

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

			InvocableMethod request = new Gson().fromJson(message.toString(), InvocableMethod.class);

			Log.i("TIMESTAMP", "UPLOAD:"+isInstanceOf()+":"+ (System.currentTimeMillis() - request.getTimestamp()));

			RemoteMethodExecutionService remoteMethodExecution = new RemoteMethodExecutionService(context);

			long start = System.currentTimeMillis();
			long elapsed = 0;

			Object response = remoteMethodExecution.executeMethod(request);

			elapsed = System.currentTimeMillis() - start;
			Log.i("TIMESTAMP", "METHOD:"+isInstanceOf()+":"+String.valueOf(elapsed));

			server.publish("/offloading/result", new MqttMessage(new Gson().toJson(response).getBytes()));

		}

	}

	class Broker implements Runnable {
		@Override
		public void run() {
			try {
				Server server = new Server();
				server.startServer();
				Log.i("MQTT", "MQTT Broker initialized successfully.");

			} catch (IOException e) {
				Log.i("MQTT", "Error for initiate the MQTT Broker");
				Log.i("MQTT", e.getMessage());
			}
		}
	}

}
