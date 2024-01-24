package ina.vehicle.navigation.interfaces;

import java.util.ArrayList;

import ina.vehicle.navigation.components.RoadPoint;
import ina.vehicle.navigation.components.Route;

public interface ISmartCar extends IIdentifiable {

    public RoadPoint getCurrentPlace();

    public void changeKm(int km);

    public void changeRoad(String road, int km);
    
    public String getVehicleRole();
    
    public void setVehicleRole(String vehicleRole);
    
    public int getCurrentSpeed();
    
    public void setCurrentSpeed(int vehicleSpeed);
    
    public ArrayList<IRouteFragment> getRoute();
    
    public void setRoute(Route route);
}