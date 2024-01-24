package ina.vehicle.navigation.components;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import ina.vehicle.navigation.interfaces.IRoadPoint;
import ina.vehicle.navigation.types.ENavigatorStatus;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarClient implements MqttCallback {

	private static final String STEP_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/step";
	// road-segment
	private static final String TRAFFIC_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/traffic";
	// action, vehicleRole, vehicleId, roadSegment, point
	private static final String TRAFFIC_MSG = "{\"msg\":{\"action\": \"%s\", \"vehicle-role\": \"%s\", \"vehicle-id\": \"%s\", \"road-segment\": \"%s\", \"position\": 0},\"id\": \"MSG_1638979846783\",\"type\": \"TRAFFIC\",\"timestamp\": 1638979846783}\n";

	private SmartCar smartCar;
	private MqttConnectOptions connectionOptions;
	private MqttClient vehicleClient;
	private String broker;

	public SmartCarClient(SmartCar smartCar, String brokerUrl) {
		this.smartCar = smartCar;
		this.broker = brokerUrl;
		connect(brokerUrl);
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		MySimpleLogger.warn(this.getClass().toString(), "Connection lost smartCar " + smartCar.getId());
		arg0.printStackTrace();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		MySimpleLogger.trace(this.getClass().toString(), "Published current info for " + smartCar.getId());
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		SmartCar smartcar = this.smartCar;
		Navigator navigator = this.smartCar.navigator;
		IRoadPoint roadPoint = navigator.getCurrentPosition();
		String lastRoadSegment = roadPoint.getRoadSegment();
		String publishMessage = "";
		
		//If end of the road, vehicle out & unsubscribe
		if (navigator.getNavigatorStatus() == ENavigatorStatus.REACHED_DESTINATION) {
			publishMessage = String.format(TRAFFIC_MSG, "VEHICLE_OUT", smartcar.getVehicleRole(), smartCar.getId(),
			smartcar.getId(), lastRoadSegment, navigator.getCurrentPosition().getPosition());
			publish(TRAFFIC_TOPIC, lastRoadSegment, publishMessage);
			
			MySimpleLogger.trace(this.getClass().getName(), "Route finished, stopping...");
			vehicleClient.unsubscribe(STEP_TOPIC);
		}
		
		// On each simulation step, ask the car (Navigation) to move
		navigator.move(3000, this.smartCar.getCurrentSpeed());

		// On each step update position to broker
		publishMessage = String.format(TRAFFIC_MSG, "VEHICLE_IN", smartcar.getVehicleRole(), smartCar.getId(),
				smartcar.getId(), navigator.getCurrentPosition().getRoadSegment().toString(), navigator.getCurrentPosition().getPosition());
		publish(TRAFFIC_TOPIC, navigator.getCurrentPosition().getRoadSegment().toString(), publishMessage);

		// If enter new road segment, VEHICLE-OUT
		if (lastRoadSegment != navigator.getCurrentPosition().getRoadSegment().toString()) {

			publishMessage = String.format(TRAFFIC_MSG, "VEHICLE_OUT", smartcar.getVehicleRole(), smartCar.getId(),
					smartcar.getId(), lastRoadSegment, navigator.getCurrentPosition().getPosition());
			publish(TRAFFIC_TOPIC, lastRoadSegment, publishMessage);

		}
	}

	public void connect(String brokerUrl) {
		String clientID = this.smartCar.getId() + ".subscriber";
		connectionOptions = new MqttConnectOptions();

		connectionOptions.setCleanSession(true);
		connectionOptions.setKeepAliveInterval(30);

		// Connect to broker
		try {
			vehicleClient = new MqttClient(brokerUrl, clientID);
			vehicleClient.setCallback(this);
			vehicleClient.connect(connectionOptions);
			MySimpleLogger.info(this.getClass().toString(), "Attempting to connect vehicle " + smartCar.getId());
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to connect to broker");
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().toString(), "smartcar " + smartCar.getId() + " connected to the broker ");
		}
		subscribe(STEP_TOPIC);
	}

	public void subscribe(String topic) {
		try {
			vehicleClient.subscribe(topic, 0);
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to subscribe to topic" + topic);
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().toString(), "smartcar " + smartCar.getId() + " subscribed to " + topic);
		}
	}

	public void publish(String topic, String topicSubstring, String message) {
		topic = String.format(topic, topicSubstring);
		MqttTopic mqttTopic = vehicleClient.getTopic(topic);
		
		MqttMessage messageToPublish = new MqttMessage(message.getBytes());
		messageToPublish.setQos(0);
		messageToPublish.setRetained(false);
		MqttDeliveryToken token = null;
		try {
			token = mqttTopic.publish(messageToPublish);
		} catch (Exception e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to publish message");
			e.printStackTrace();
		}

	}
	
	public void disconnect() {
		try {
			vehicleClient.disconnect();
		} catch (Exception e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to disconnect");
			e.printStackTrace();
		}
	}

}
