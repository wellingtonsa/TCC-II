package util.protocol.application;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import model.services.ProtocolService;

public class MQTT implements ProtocolService{
	
	private MqttClient client;
	private final String BROKER_URL = "tcp://localhost:1884";

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String address) {
		try {
			Callback cb = new Callback();
			client = new MqttClient(BROKER_URL, UUID.randomUUID().toString());
			client.connect();
			client.setCallback(cb);
			client.subscribe("/offloading/result", 0);
			return true;
		} catch (MqttException e) {
			System.out.println("MQTT - Connection error:"+e.getMessage());
			return false;
		}

	}

	@Override
	public boolean disconnect() {
		try {
			client.disconnect();
			return true;
		} catch (MqttException e) {
			System.out.println("MQTT - Error to disconnect:"+e.getMessage());
			return false;
		}

	}

	@Override
	public String sendMessage(String message) {
	
		try {
			client.publish("/offloading/init", new MqttMessage(message.getBytes()));
			return "";
		} catch (MqttPersistenceException e) {
			System.out.println("MQTT - Error to send a message:"+e.getMessage());
			return "";
		} catch (MqttException e) {
			System.out.println("MQTT - Error to send a message:"+e.getMessage());
			return "";
		}
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
			// message.getPayload();
			System.out.println(message.toString());
			
		}
		
	}

}
