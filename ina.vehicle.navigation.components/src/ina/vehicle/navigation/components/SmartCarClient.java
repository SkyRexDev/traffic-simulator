package ina.vehicle.navigation.components;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarClient implements MqttCallback {

	private static final String BROKER_URL = "tcp://ttmi008.iot.upv.es";
	private static final String STEP_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/step";
	// road-segment
	private static final String TRAFFIC_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/traffic";
	// action, vehicleRole, vehicleId, roadSegment, point
	private static final String TRAFFIC_MSG = "{\"msg\":{\"action\": \"%s\", \"vehicle-role\": \"%s\", \"vehicle-id\": \"%s\", \"road-segment\": \"%s\", \"position\": 0},\"id\": \"MSG_1638979846783\",\"type\": \"TRAFFIC\",\"timestamp\": 1638979846783}\n";

	private SmartCar smartCar;
	private MqttConnectOptions connectionOptions;
	private MqttClient vehicleClient;

	@Override
	public void connectionLost(Throwable arg0) {
		MySimpleLogger.warn(this.getClass().toString(), "Connection lost smartCar " + smartCar.getId());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		MySimpleLogger.trace(this.getClass().toString(), "Delivery completed smartCar:" + smartCar.getId());
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		SmartCar smartcar = this.smartCar;
		Navigator navigator = smartcar.navigator;
		String lastRoadSegment = navigator.getCurrentRoadSegment().toString();

		MySimpleLogger.trace("smartCar" + smartCar.getId(), "Recieved: " + message.getPayload().toString());

		// On each simulation step, ask the car (Navigation) to move
		navigator.move(3000, this.smartCar.getCurrentSpeed());

		// On each step update position to broker
		String publishMessage = String.format(TRAFFIC_MSG, "VEHICLE_IN", smartcar.getVehicleRole(), smartCar.getId(),
				smartcar.getId(), navigator.getCurrentRoadSegment(), navigator.getCurrentPosition());
		publish(TRAFFIC_TOPIC, navigator.getCurrentRoadSegment().toString(), publishMessage);

		// If enter new road segment, VEHICLE-OUT
		if (lastRoadSegment != navigator.getCurrentRoadSegment().toString()) {

			publishMessage = String.format(TRAFFIC_MSG, "VEHICLE_OUT", smartcar.getVehicleRole(), smartCar.getId(),
					smartcar.getId(), lastRoadSegment, navigator.getCurrentPosition());
			publish(TRAFFIC_TOPIC, lastRoadSegment, publishMessage);

		}

	}

	public void connect(String url) {
		String clientID = "vehicleSubscriber" + this.smartCar.getId();
		connectionOptions = new MqttConnectOptions();

		connectionOptions.setCleanSession(true);
		connectionOptions.setKeepAliveInterval(30);

		// Connect to broker
		try {
			vehicleClient = new MqttClient(BROKER_URL, clientID);
			vehicleClient.setCallback(this);
			vehicleClient.connect(connectionOptions);
			MySimpleLogger.info(this.getClass().toString(), "Attempting to connect vehicle" + smartCar.getId());
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to connect to broker");
			e.printStackTrace();
		}
		MySimpleLogger.info(this.getClass().toString(), "smartcar" + smartCar.getId() + "connected to the broker");

		subscribe(STEP_TOPIC);
	}

	public void subscribe(String topic) {
		try {
			vehicleClient.subscribe(topic, 0);
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to subscribe to topic" + topic);
			e.printStackTrace();
		}
		MySimpleLogger.info(this.getClass().toString(), "smartcar" + smartCar.getId() + "subscribed to " + topic);
	}

	public void publish(String topic, String topicSubstring, String message) {
		MqttMessage messageToPublish = new MqttMessage();
		messageToPublish.setPayload(message.getBytes());
		try {
			vehicleClient.publish(String.format(topic, topicSubstring), messageToPublish);
		} catch (Exception e) {
			MySimpleLogger.error(this.getClass().toString(), "Failed to publish message");
			e.printStackTrace();
		}

	}

}
