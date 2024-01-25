package ina.vehicle.navigation.components;

import java.util.EnumMap;
import java.util.Map;

import ina.vehicle.navigation.types.TrafficLightTypes.LightColor;
import ina.vehicle.navigation.types.TrafficLightTypes.LightState;

public class Dashboard {

	protected String smartDashboardID = null;
	protected RoadSegment roadSegment = null;
	protected Map<LightColor, LightState> lightStates;
	protected TrafficLightController tlc = null;
	

	public Dashboard(String id, RoadSegment rs) {
		this.smartDashboardID = id;
		this.roadSegment = rs;
		this.lightStates = new EnumMap<>(LightColor.class);
		for (LightColor function : LightColor.values()) {
			lightStates.put(function, LightState.OFF); // Default state
		}
		
		this.tlc = new TrafficLightController(this);
		
	}

	public String getSmartDashboardID() {
		return smartDashboardID;
	}

	public void setSmartDashboardID(String smartDashboardID) {
		this.smartDashboardID = smartDashboardID;
	}

	public RoadSegment getRoadSegment() {
		return roadSegment;
	}

	public void setroadSegment(RoadSegment roadSegment) {
		this.roadSegment = roadSegment;
	}

	public void setLightState(LightColor function, LightState state) {
		lightStates.put(function, state);
	}

	public LightState getLightState(LightColor function) {
		return lightStates.get(function);
	}
}
