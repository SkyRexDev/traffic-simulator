package ina.vehicle.navigation.components;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarSignalSubscriber implements MqttCallback {

	private static final String SIGNAL_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/signals";

	
	private SmartCar smartCar;
	private String broker;
	private MqttConnectOptions connectionOptions;
	private MqttClient signalClient;
	private String roadSegment;
	
	private int speedLimit = 100;
	
	public SmartCarSignalSubscriber(SmartCar smartCar, String brokerUrl, String roadSegment) {
		this.smartCar = smartCar;
		this.broker = brokerUrl;
		this.roadSegment = roadSegment;
		connect(broker);
	}
	
	public void connect(String brokerUrl) {
		String clientID = this.smartCar.getId() + ".signalSubscriber";
		connectionOptions = new MqttConnectOptions();

		connectionOptions.setCleanSession(true);
		connectionOptions.setKeepAliveInterval(30);

		// Connect to broker
		try {
			signalClient = new MqttClient(brokerUrl, clientID);
			signalClient.setCallback(this);
			signalClient.connect(connectionOptions);
			MySimpleLogger.info(this.getClass().getName(), "Attempting to connect vehicle " + smartCar.getId());
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to connect to broker");
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().getName(), "connected to broker signal");
		}
		subscribe(String.format(SIGNAL_TOPIC, this.roadSegment));
	}

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
		//Parse incoming json
		JSONObject json = new JSONObject(message.toString());
		if (!json.has("msg")) {
			MySimpleLogger.info(this.getClass().getName(), "Bad json recieved");
			return;
		}
		JSONObject messageField = json.getJSONObject("msg");
		//speed-limit
		if (messageField.get("signal-type").equals("SPEED_LIMIT")) {
			this.speedLimit = (int) messageField.get("value");
			MySimpleLogger.trace("SIGNAL SUBSC", "" + this.speedLimit);
			this.smartCar.smartCarClient.signalSpeed = this.speedLimit;
		}
		//traffic-light
		if (messageField.get("signal-type").equals("TRAFFIC_LIGHT")) {
			int distanceToLight = (int) messageField.get("starting-position") - this.smartCar.navigator.getCurrentPosition().getPosition();
			if (distanceToLight <= 50) {
				if (messageField.get("value").equals("HLL")) {
					//stop car
					smartCar.smartCarClient.signalSpeed = 0;
				} else {
					//go car
					smartCar.smartCarClient.signalSpeed = smartCar.getCruiseSpeed();
				}
			}
		}
	}
	
	public void subscribe(String topic) {
		try {
			this.signalClient.subscribe(topic, 0);
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to subscribe to topic" + topic);
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().getName(), "Signal subscribed to " + topic);
		}
	}
	
	public void unsubscribe(String topic) {
		try {
			this.signalClient.unsubscribe(topic);
			this.smartCar.smartCarClient.signalSpeed = 1000;
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to unsubscribe to topic" + topic);
			e.printStackTrace();
		}
		MySimpleLogger.info(this.getClass().getName(), "Signal unsubscribed to " + topic);
	}
}
