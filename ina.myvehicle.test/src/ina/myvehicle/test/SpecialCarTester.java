package ina.myvehicle.test;

import ina.vehicle.navigation.components.Route;
import ina.vehicle.navigation.components.SmartCar;
import ina.vehicle.navigation.components.SpeedLimit;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class SpecialCarTester {

	public static void main(String[] args) throws InterruptedException {
		
		MySimpleLogger.info(SmartCarTester.class.toString(), "Creating smartCar");
		Route route = new Route();
		route.addRouteFragment("R1s1", 0, 29);
		route.addRouteFragment("R1s2a", 29, 320);
		route.addRouteFragment("R5s1", 0, 300);
		
		//Ajuste dinámico de la velocidad del coche
		SpeedLimit speedLimit1 = new SpeedLimit("speed_signal_R1s1", 90, "R1s1", 0, 29);	//vel carretera 40 < coche 50 < señal 90
		SpeedLimit speedLimit2 = new SpeedLimit("speed_signal_R1s2a", 70, "R1s2a", 29, 320); // vel  coche 50 < carretera 60 < señal 70
		SpeedLimit speedLimit3 = new SpeedLimit("speed_signal_R5s1", 40, "R5s1", 0, 300);	// vel señal 40 < coche 50 < carretera 60
		
		//Vehículos especiales ignoran restricciones "Police" igual
		SmartCar smartcar = new SmartCar("2567KLM", "Police", route);
		smartcar.setCruiseSpeed(70);
	}
}
