package ina.vehicle.navigation.components;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

import ina.vehicle.navigation.utils.MyBean;
import ina.vehicle.navigation.utils.MySimpleLogger;


public class SpeedLimit implements MqttCallback {

	MqttClient myClient;
	MqttConnectOptions connOpt;
	static final String BROKER_URL = "tcp://tambori.dsic.upv.es:10083";
	private String id;
	private String myTopic;
	private RoadSegment road;
	private int speed;
	private float maxRoad;
	private float minRoad;
	
	public SpeedLimit(String idA, int maxSpeed, RoadSegment roadSegment, float firstKM, float lastKM) {
		id = "speedSignal." + idA;
		speed = maxSpeed;
		road = roadSegment;
		minRoad = firstKM;
		maxRoad = lastKM;
		myTopic = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/step";
	}
	
	protected void _debug(String message) {
		System.out.println("(SpeedLimit: " + this.id + ") " + message);
	}
	
	@Override
	public void connectionLost(Throwable t) {
		this._debug("Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message2) throws Exception {
		
		String payload = new String(message2.getPayload());
		String topic1 = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/" + road.getId() + "/signals";
		//System.out.println("-------------------------------------------------");
		//System.out.println("| Topic:" + topic);
		//System.out.println("| Message: " + payload);
		//System.out.println("-------------------------------------------------");
		
		MqttTopic topic2 = myClient.getTopic(topic);
    	JSONObject pubMsg = new JSONObject();
		try {
			pubMsg.put("signal-type", "SPEED_LIMIT");
			pubMsg.put("id", id);
			pubMsg.put("road", road.getId());
			pubMsg.put("max-speed", speed);
			pubMsg.put("first-km", minRoad);
			pubMsg.put("last-km", maxRoad);
	   		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
   		int pubQoS = 0;
   		
   		MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(true);
    	// Publish the message
    	this._debug("Publishing to topic \"" + topic2 + "\" qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	try {
    		// publish message to broker
			token = topic2.publish(message);
			this._debug(pubMsg.toString());
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		


	}
	
	public void connect() {
		// setup MQTT Client
		String clientID = this.id + ".speedLimit.subscriber";
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, clientID);
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		this._debug("Connected to " + BROKER_URL);
	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(120000);

			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	
	public void subscribe(String myTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(myTopic, subQoS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	public void unsubscribe(String myTopic) {
		
		// unsubscribe to topic
		try {
			int subQoS = 0;
			myClient.unsubscribe(myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}