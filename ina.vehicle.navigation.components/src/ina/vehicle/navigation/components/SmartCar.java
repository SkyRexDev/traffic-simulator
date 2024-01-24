package ina.vehicle.navigation.components;

import java.util.ArrayList;

import ina.vehicle.navigation.interfaces.IRouteFragment;
import ina.vehicle.navigation.interfaces.ISmartCar;
import ina.vehicle.navigation.utils.MyBean;

public class SmartCar implements ISmartCar {

	protected MyBean bean = null;
	private String vehicleRole = null;
	protected RoadPoint roadPoint = null;
	protected Navigator navigator;
	private Route route;
	private int vehicleSpeed = 40; //km/h

	public SmartCar(String id, String vehicleRole) {
		this.bean = new MyBean(id);
		this.setVehicleRole(vehicleRole);
		
		this.navigator = new Navigator("vehicleNavigator" + id);
		
		route = new Route();
		route.addRouteFragment("R1s1", 0, 29);
		route.addRouteFragment("R1s2a", 29, 320);
		route.addRouteFragment("R5s1", 0, 300);
		this.navigator.setRoute(route);
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
	
	public ArrayList<IRouteFragment> getRoute() {
		return this.route;
	}
    
    public void setRoute(Route route) {
    	this.navigator.setRoute(route);
    }
}
