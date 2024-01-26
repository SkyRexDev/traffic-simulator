package ina.vehicle.navigation.interfaces;

import java.util.Map;

import ina.vehicle.navigation.components.RoadSegment;
import ina.vehicle.navigation.types.TrafficLightTypes.LightColor;
import ina.vehicle.navigation.types.TrafficLightTypes.LightState;

public interface IDashboard {

	String getSmartDashboardID();

	void setSmartDashboardID(String smartDashboardID);

	RoadSegment getRoadSegment();

	void setRoadSegment(RoadSegment roadSegment);

	void setLightState(LightColor color, LightState state);

	LightState getLightState(LightColor color);

	Map<LightColor, LightState> getAllLightStates();
}
