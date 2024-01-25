package ina.vehicle.navigation.components;

import ina.vehicle.navigation.interfaces.ISmartCar;
import ina.vehicle.navigation.utils.MyBean;
import org.eclipse.paho.client.mqttv3.*;

public class SmartCar implements ISmartCar {

	protected MyBean bean = null;
	private String vehicleRole = null;
	protected RoadPoint roadPoint = null;
	private Navigator navigator;
	private int vehicleSpeed = 40; //km/h

	public SmartCar(String id, String vehicleRole) {
		this.bean = new MyBean(id);
		this.setVehicleRole(vehicleRole);
		
		this.navigator = new Navigator("vehicleNavigator" + id);
	}

	@Override
	public String getId() {
		return (String) this.bean.getProperty("id");
	}

	protected ISmartCar setId(String id) {
		this.bean.getProperties().put("id", id);
		return this;
	}

	@Override
	public RoadPoint getCurrentPlace() {
		return this.roadPoint;
	}

	@Override
	public void changeKm(int km) {
		this.getCurrentPlace().setPosition(km);
	}

	@Override
	public void changeRoad(String road, int km) {
		this.getCurrentPlace().setRoadSegment(road);
		this.getCurrentPlace().setPosition(km);

	}

	@Override
	public String getVehicleRole() {
		return vehicleRole;
	}

	@Override
	public void setVehicleRole(String vehicleRole) {
		this.vehicleRole = vehicleRole;
	}

	@Override
	public int getCurrentSpeed() {
		return this.vehicleSpeed;
	}

	@Override
	public void setCurrentSpeed(int vehicleSpeed) {
		this.vehicleSpeed = vehicleSpeed;
	}

}