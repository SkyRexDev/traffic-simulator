package ina.vehicle.navigation.interfaces;

import ina.vehicle.navigation.components.RoadPoint;

public interface ISmartCar extends IIdentifiable {

    public RoadPoint getCurrentPlace();

    public void changeKm(int km);

    public void changeRoad(String road, int km);
    
    public String getVehicleRole();
    
    public void setVehicleRole(String vehicle_role);
}