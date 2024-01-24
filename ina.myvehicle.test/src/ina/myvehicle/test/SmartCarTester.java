package ina.myvehicle.test;

import ina.vehicle.navigation.components.Route;
import ina.vehicle.navigation.components.SmartCar;
import ina.vehicle.navigation.types.ENavigatorStatus;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class SmartCarTester {

	public static void main(String[] args) throws InterruptedException {
	
		MySimpleLogger.info(SmartCarTester.class.toString(), "Creating smartCar");
		Route route = new Route();
		route.addRouteFragment("R1s1", 0, 29);
		route.addRouteFragment("R1s2a", 29, 320);
		route.addRouteFragment("R5s1", 0, 300);
		SmartCar smartcar = new SmartCar("2567KLM", "PrivateUsage", route);
		
		while (true) {
			if (smartcar.navigator.getNavigatorStatus() == ENavigatorStatus.REACHED_DESTINATION) {
				Thread.sleep(1000);
				smartcar.smartCarClient.disconnect();
			}
		}
	}
}
