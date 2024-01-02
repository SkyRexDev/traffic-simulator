package ina.myvehicle.test;

import ina.vehicle.navigation.components.Navigator;
import ina.vehicle.navigation.components.RoadPoint;
import ina.vehicle.navigation.components.Route;
import ina.vehicle.navigation.interfaces.INavigator;
import ina.vehicle.navigation.interfaces.IRoadPoint;
import ina.vehicle.navigation.interfaces.IRoute;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class NavigatorTester {

	public static void main(String[] args) {
		
		int STEP_MS = 3000; // simulator step time (in milliseconds)
		int my_current_vehicle_speed = 0; // in Km/h
		
		MySimpleLogger.trace("time", " <- t0");
		INavigator navigator = new Navigator("my-navigator");
		
		// el siguiente comando simula que el navegador recibe por 'GPS inverso' la ubicación actual
		// no es obligatorio establecer este valor, ya que al indicar una ruta, se ubica en el primer punto de la ruta
		navigator.setCurrentPosition(new RoadPoint("R1s1", 0));
		
		IRoute ruta = new Route();
		ruta.addRouteFragment("R1s1", 0, 29);
		ruta.addRouteFragment("R1s2a", 29, 320);
		ruta.addRouteFragment("R5s1", 0, 300);

		MySimpleLogger.info("main", "Setting Route " + ruta);
		navigator.setRoute(ruta);
		
		IRoadPoint roadpoint = navigator.getCurrentPosition();
		MySimpleLogger.info("main", "Current Position: " + roadpoint);
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		
		// iniciamos la navegación
		navigator.startRouting();
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		
		// establecemos la velocidad a la que indicaremos que estamos avanzando ...
		my_current_vehicle_speed = 40;
		
		// avanzamos un paso de simulación (STEP_MS milliseconds) a la velocidad indicada
		navigator.move(STEP_MS, my_current_vehicle_speed);
		MySimpleLogger.trace("time", " <- t1");
		MySimpleLogger.info("main", "Current Position: " + navigator.getCurrentPosition());
		MySimpleLogger.info("main", " Remaining Route: " + navigator.getRoute());
		
		// cambiamos la velocidad
		my_current_vehicle_speed = 60;
	
		// avanzamos un paso de simulación (STEP_MS milliseconds) a una velocidad indicada
		navigator.move(STEP_MS, my_current_vehicle_speed);		
		MySimpleLogger.trace("time", " <- t2");
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());

		// pausamos la navegación
		navigator.stopRouting();
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		navigator.move(STEP_MS, my_current_vehicle_speed);   // en pausa no hay movimiento
		MySimpleLogger.trace("time", " <- t3");
		MySimpleLogger.info("main", "Current Position: " + navigator.getCurrentPosition());
		
		// avanzamos varios pasos de simulación
		navigator.startRouting();
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());

		my_current_vehicle_speed = 80;
		navigator.move(STEP_MS, my_current_vehicle_speed);		
		MySimpleLogger.trace("time", " <- t4");
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		
		navigator.move(STEP_MS, my_current_vehicle_speed);		
		MySimpleLogger.trace("time", " <- t5");
		my_current_vehicle_speed = 60;
		navigator.move(STEP_MS, my_current_vehicle_speed);
		MySimpleLogger.trace("time", " <- t6");
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		
		navigator.move(STEP_MS, my_current_vehicle_speed);
		MySimpleLogger.trace("time", " <- t7");
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());

		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t8");
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t9");
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t10");		
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t11");
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		
		// Alcanzamos el destino en el siguiente paso
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t12");
		MySimpleLogger.info("main", "Current Position: " + navigator.getCurrentPosition());
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		
		// Ya no se mueve
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t13");
		MySimpleLogger.info("main", "Current Position: " + navigator.getCurrentPosition());
		MySimpleLogger.info("main", "Remaining Route: " + navigator.getRoute());
		
		
		// Establecemos otra ruta ...
		// Si no indicamos el mismo punto de inicio que el que está indicado, indicará que se está teleportando ...
		ruta = new Route();
		ruta.addRouteFragment("R11s1", 30, 200);
		ruta.addRouteFragment("R7s1a", 60, 250);
		
		navigator.setRoute(ruta);
		navigator.startRouting();
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t14");
		
		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t15");

		navigator.move(STEP_MS, 80);
		MySimpleLogger.trace("time", " <- t16");
		MySimpleLogger.info("main", "Navigator Status: " + navigator.getNavigatorStatus().getName());
		
	}

}

