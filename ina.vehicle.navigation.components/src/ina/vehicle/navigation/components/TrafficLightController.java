package ina.vehicle.navigation.components;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.json.JSONObject;

import ina.vehicle.navigation.types.ERoadStatus;
import ina.vehicle.navigation.types.TrafficLightTypes.LightColor;
import ina.vehicle.navigation.types.TrafficLightTypes.LightState;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class TrafficLightController implements MqttCallback {

	private MqttClient client;
	private String BROKER_URL = "tcp://ttmi008.iot.upv.es:1883";
	private String roadTopic;
	private String STEP_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/step";
	private Dashboard dashboard; // Assuming Dashboard manages the light states
	MqttConnectOptions connOpt;

	public TrafficLightController(Dashboard dashboard) {
		this.dashboard = dashboard;
		this.roadTopic = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/"
				+ dashboard.getRoadSegment().getRoadSegmentCode() + "/info";
		try {
			connOpt = new MqttConnectOptions();
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(30);
			client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
			client.setCallback(this);
			client.connect(connOpt);
			client.subscribe(STEP_TOPIC);
			MySimpleLogger.info(this.getClass().toString(), "Subscribed to " + STEP_TOPIC);
//			client.subscribe(roadTopic);
//			MySimpleLogger.info(this.getClass().toString(), "Subscribed to " + roadTopic);

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		MySimpleLogger.warn(this.getClass().toString(), "Connection lost with " + BROKER_URL + "lost");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String payload = new String(message.getPayload());
		if (topic.equals(roadTopic)) {
			
			JSONObject jsonObj = new JSONObject(payload);
			if (jsonObj.has("msg") && jsonObj.getJSONObject("msg").has("status")) {
				String status = jsonObj.getJSONObject("msg").getString("status");
				ERoadStatus roadStatus = ERoadStatus.valueOf(status);
				handleRedLightScenario(roadStatus);
			}
		}
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic);
		System.out.println("| Message: " + payload);
		System.out.println("-------------------------------------------------");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Message delivery confirmation
	}

	// Method to publish messages to the MQTT broker
	public void publishMessage(String topic, String payload) {
		try {
			MqttMessage message = new MqttMessage(payload.getBytes());
			MySimpleLogger.info(this.getClass().toString(), "VOY A ENVIAR");
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void handleRedLightScenario(ERoadStatus roadStatus) {
		if (roadStatus.equals(ERoadStatus.Free_Flow) || roadStatus.equals(ERoadStatus.Mostly_Free_Flow)) {
			dashboard.setLightState(LightColor.RED, LightState.OFF);
			MySimpleLogger.info(this.getClass().toString(),
					"Dashboard " + dashboard.getSmartDashboardID() + " changed RED light to OFF");
			String payload = "{\"state\":\"OFF\",\"id\":\"f1\"}";
			MySimpleLogger.info(this.getClass().toString(), "HE LLEGADO");
			notify("dispositivo/ttmi056/funcion/f1/", payload);
		}
		if (roadStatus.equals(ERoadStatus.Limited_Manouvers)) {
			dashboard.setLightState(LightColor.RED, LightState.BLINKING);
			MySimpleLogger.info(this.getClass().toString(),
					"Dashboard " + dashboard.getSmartDashboardID() + " changed RED light to BLINKING");
			String payload = "{\"state\":\"BLINKING\",\"id\":\"f1\"}";
			MySimpleLogger.info(this.getClass().toString(), "HE LLEGADO");
			notify("dispositivo/ttmi056/funcion/f1/", payload);
		}
		if (roadStatus.equals(ERoadStatus.No_Manouvers) || roadStatus.equals(ERoadStatus.Collapsed)) {
			dashboard.setLightState(LightColor.RED, LightState.ON);
			MySimpleLogger.info(this.getClass().toString(),
					"Dashboard " + dashboard.getSmartDashboardID() + " changed RED light to ON");
			String payload = "{\"state\":\"ON\",\"id\":\"f1\"}";
			notify("dispositivo/ttmi056/funcion/f1/", payload);
		}
	}
	
	public void notify(String topic, String infoMessage) {

		MqttTopic mqtttopic = client.getTopic(topic);

		int pubQoS = 0;
		MqttMessage message = new MqttMessage(infoMessage.getBytes());
		message.setQos(pubQoS);
		message.setRetained(false);

		// Publish the message
		MySimpleLogger.info(this.getClass().toString(),"Publishing to topic \"" + topic + "\" qos " + pubQoS);
		MqttDeliveryToken token = null;
		try {
			// publish message to broker
			token = mqtttopic.publish(message);
			MySimpleLogger.info(this.getClass().toString(),"DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
