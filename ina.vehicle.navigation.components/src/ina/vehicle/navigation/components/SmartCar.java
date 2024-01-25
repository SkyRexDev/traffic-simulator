package ina.vehicle.navigation.components;

import java.util.ArrayList;

import ina.vehicle.navigation.interfaces.IRoadSegment;
import ina.vehicle.navigation.interfaces.IRouteFragment;
import ina.vehicle.navigation.interfaces.ISmartCar;
import ina.vehicle.navigation.utils.MyBean;

public class SmartCar implements ISmartCar {

	private static final String BROKER_URL = "tcp://ttmi008.iot.upv.es:1883";
	
	public Navigator navigator;
	public SmartCarClient smartCarClient;
	public SmartCarSignalSubscriber smartCarSignalSubscriber;
	protected MyBean bean = null;
	private String vehicleRole = null;
	protected RoadPoint roadPoint = null;
	private Route route;
	private int vehicleSpeed = 40; //km/h

	public SmartCar(String id, String vehicleRole, Route route) {
		this.bean = new MyBean(id);
		this.setVehicleRole(vehicleRole);
		
		this.navigator = new Navigator("vehicleNavigator" + id);
		
		this.navigator.setRoute(route);
		this.navigator.startRouting(); 
		
		smartCarClient = new SmartCarClient(this, BROKER_URL);
		//smartCarSignalSubscriber = new SmartCarSignalSubscriber();
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
