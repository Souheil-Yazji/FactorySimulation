package model;

import model.delay.DelayGenerator;
import model.delay.DistributionDelayGenerator;

public class ApplicationContext {
	private static final ApplicationContext INSTANCE = new ApplicationContext();
	public static final int BUFFER_SIZE = 2;

	public static final int REPLICATIONS = 39;
	public static final int STOP_SIM_TIME = 5000;
	public static final int COLLECT_METRIC_INTERVAL = 250;
	public static final int CUT_OFF_INTERVAL = 5;

	public static final String C1_INSPECTION_DATA = "servinsp1.dat";
	public static final String C2_INSPECTION_DATA = "servinsp22.dat";
	public static final String C3_INSPECTION_DATA = "servinsp23.dat";
	public static final String W1_PRODUCTION_DATA = "ws1.dat";
	public static final String W2_PRODUCTION_DATA = "ws2.dat";
	public static final String W3_PRODUCTION_DATA = "ws3.dat";

	public static final String INSPECTOR_POLICY_LEAST_POP = "LEAST_POP";
	public static final String INSPECTOR_POLICY_ORDERED = "ORDERED";
	public static final String INSPECTOR_POLICY_INVERTED = "LEAST_POP_INVERTED";

	private final FutureEventList eventList = new FutureEventList();
	private final DelayGenerator delayGenerator = new DistributionDelayGenerator();

	private ApplicationContext() {}

	public static ApplicationContext getInstance() {
		return INSTANCE;
	}
	
	public FutureEventList getFutureEventList() {
		return eventList;
	}

	public DelayGenerator getDelayGenerator() {
		return delayGenerator;
	}
}
