package ina.vehicle.navigation.components;

import ina.vehicle.navigation.interfaces.ISmartCar;
import ina.vehicle.navigation.utils.MyBean;

public class SmartCar implements ISmartCar {

	protected MyBean bean = null;
	private String vehicleRole = null;
	protected RoadPoint roadPoint = null;

	public SmartCar(String id, String vehicleRole) {
		this.bean = new MyBean(id);
		this.setVehicleRole(vehicleRole);
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
}