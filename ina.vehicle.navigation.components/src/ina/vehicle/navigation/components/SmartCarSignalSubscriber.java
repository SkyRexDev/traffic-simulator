package ina.vehicle.navigation.components;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarSignalSubscriber implements MqttCallback {

	private static final String SIGNAL_TOPIC = "es/upv/pros/tatami/smartcities/traffic/PTPaterna/road/%s/signals";

	
	private SmartCar smartCar;
	private String broker;
	private MqttConnectOptions connectionOptions;
	private MqttClient signalClient;
	
	public SmartCarSignalSubscriber(SmartCar smartCar, String brokerUrl, String roadSegment) {
		this.smartCar = smartCar;
		this.broker = brokerUrl;
		connect(brokerUrl);
	}
	
	public void connect(String brokerUrl) {
		String clientID = this.smartCar.getId() + ".subscriber";
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
			MySimpleLogger.info(this.getClass().getName(), "smartcar " + smartCar.getId() + " connected to the broker ");
		}
		//subscribe(STEP_TOPIC);
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
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
