package ina.vehicle.navigation.components;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

import ina.vehicle.navigation.interfaces.IRoadPoint;
import ina.vehicle.navigation.types.ENavigatorStatus;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarClient implements MqttCallback {

	private static final String STEP_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/step";
	// road-segment
	private static final String TRAFFIC_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/traffic";
	private static final String SIGNAL_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/signals";
	// action, vehicleRole, vehicleId, roadSegment, point
	private static final String TRAFFIC_MSG = "{\"msg\":{\"action\": \"%s\", \"vehicle-role\": \"%s\", \"vehicle-id\": \"%s\", \"road-segment\": \"%s\", \"position\": 0},\"id\": \"MSG_1638979846783\",\"type\": \"TRAFFIC\",\"timestamp\": 1638979846783}\n";
	// + roadSegment
	private static final String ROAD_SEGMENT_URL = "http://ttmi008.iot.upv.es:8182/segment/";
	

	private SmartCar smartCar;
	private MqttConnectOptions connectionOptions;
	private MqttClient vehicleClient;
	private String broker;
	private int roadSegmentSpeed = -1;
	private int signalSpeed = 1000;

	public SmartCarClient(SmartCar smartCar, String brokerUrl) {
		this.smartCar = smartCar;
		this.broker = brokerUrl;
		connect(brokerUrl);
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		MySimpleLogger.warn(this.getClass().getName(), "Connection lost smartCar " + smartCar.getId());
		arg0.printStackTrace();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		MySimpleLogger.trace(this.getClass().getName(), "Published current info for " + smartCar.getId());
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		Navigator navigator = this.smartCar.navigator;
		String lastRoadSegment = navigator.getCurrentPosition().getRoadSegment();

		//If end of the road, vehicle out & unsubscribe
		if (navigator.getNavigatorStatus() == ENavigatorStatus.REACHED_DESTINATION) {
			publish(TRAFFIC_TOPIC, lastRoadSegment, buildPositionMessage("VEHICLE_OUT", lastRoadSegment));
			
			vehicleClient.unsubscribe(STEP_TOPIC);
			MySimpleLogger.trace(this.getClass().getName(), "Route finished, stopping...");
		}
				
		// On each simulation step, ask the car (Navigation) to move
		if (topic.equals(STEP_TOPIC)) {
			navigator.move(3000, setVehicleSpeed());

			updatePositionToBroker(lastRoadSegment);
			
			//change subscription to signal
			if(lastRoadSegment != navigator.getCurrentPosition().getRoadSegment()) {
				this.smartCar.smartCarSignalSubscriber.unsubscribe(String.format(SIGNAL_TOPIC,lastRoadSegment));
				this.smartCar.smartCarSignalSubscriber.subscribe(String.format(SIGNAL_TOPIC,navigator.getCurrentPosition().getRoadSegment()));
			}
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
			MySimpleLogger.info(this.getClass().getName(), "Attempting to connect vehicle " + smartCar.getId());
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to connect to broker");
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().getName(), "smartcar " + smartCar.getId() + " connected to the broker ");
		}
		subscribe(STEP_TOPIC);
	}

	public void subscribe(String topic) {
		try {
			vehicleClient.subscribe(topic, 0);
		} catch (MqttException e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to subscribe to topic" + topic);
			e.printStackTrace();
		} finally {
			MySimpleLogger.info(this.getClass().getName(), "smartcar " + smartCar.getId() + " subscribed to " + topic);
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
			MySimpleLogger.error(this.getClass().getName(), "Failed to publish message");
			e.printStackTrace();
		}

	}
	
	public void disconnect() {
		try {
			vehicleClient.disconnect();
		} catch (Exception e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to disconnect");
			e.printStackTrace();
		}
	}
	
	private void updatePositionToBroker(String lastRoadSegment) {
		SmartCar smartCar = this.smartCar;
		Navigator navigator = smartCar.navigator;
		// On each step update position to broker
		String currentRoadSegment = navigator.getCurrentPosition().getRoadSegment().toString();
		publish(TRAFFIC_TOPIC, currentRoadSegment, buildPositionMessage("VEHICLE_IN", currentRoadSegment));

		// If enter new road segment, VEHICLE-OUT
		if (lastRoadSegment != currentRoadSegment) {
			publish(TRAFFIC_TOPIC, lastRoadSegment, buildPositionMessage("VEHICLE_OUT", lastRoadSegment));
		}
	}
	
	private String buildPositionMessage(String inOrOut, String roadSegment) {
		return String.format(TRAFFIC_MSG, inOrOut, smartCar.getVehicleRole(), smartCar.getId(),
							roadSegment, this.smartCar.navigator.getCurrentPosition().getPosition());
	} 
	
	private Boolean isSpecialVehicle() {
		return (this.smartCar.getVehicleRole().equals("Ambulance")) || (this.smartCar.getVehicleRole().equals("Police"));
	}
	
	private void checkRoadSegmentMaxSpeed(String lastRoadSegment) {
		String currentRoadSegment = this.smartCar.navigator.getCurrentPosition().getRoadSegment();
		roadSegmentSpeed = getRoadSegmentMaxSpeed(currentRoadSegment);
	}
	
	private int getRoadSegmentMaxSpeed(String roadSegment) {		
		URL roadSegmentUrl;
		JSONObject json = new JSONObject();
		try {
			roadSegmentUrl = new URL(ROAD_SEGMENT_URL + roadSegment);
			HttpURLConnection connection = (HttpURLConnection) roadSegmentUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			
			InputStream is = connection.getInputStream();			
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuffer response = new StringBuffer();
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
			json = new JSONObject(response.toString());
		} catch (Exception e) {
			MySimpleLogger.error(this.getClass().getName(), "Failed to get roadSegment info");
			e.printStackTrace();
		}
		
		if(json.equals(new JSONObject())) {
			MySimpleLogger.error(this.getClass().getName(), "Empty JSON bad http petitition");
			return 0;
		}
		
		int segmentSpeed = 0;
		try {
			segmentSpeed= (int) json.get("max-speed");
		} catch (JSONException e) {
			MySimpleLogger.error(this.getClass().getName(), "No max-speed field");
			e.printStackTrace();
		}
		
		return segmentSpeed;
	}
	
	private int setVehicleSpeed() {	
		checkRoadSegmentMaxSpeed(ROAD_SEGMENT_URL);
		if(isSpecialVehicle()) {
			return this.smartCar.getCurrentSpeed();
		}
		return Math.min(this.smartCar.getCurrentSpeed(), Math.min(roadSegmentSpeed, signalSpeed));
	}
}
