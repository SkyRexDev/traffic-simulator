package ina.myvehicle.test;

import ina.vehicle.navigation.components.Dashboard;
import ina.vehicle.navigation.components.RoadSegment;
import ina.vehicle.navigation.utils.MySimpleLogger;

public class DashboardTester {

	public static void main(String[] args) {
		MySimpleLogger.info(DashboardTester.class.toString(), "Creating dashboard");
		RoadSegment rs = new RoadSegment("R1S2", "R1", "R1S2", 0, 100, 20, 40);
		Dashboard db = new Dashboard("123", rs);
	}
}
