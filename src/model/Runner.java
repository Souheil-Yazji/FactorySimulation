package model;

import java.util.ArrayList;
import java.util.List;

import model.entities.Buffer;
import model.entities.ComponentType;
import model.entities.Inspector;
import model.entities.ProductType;
import model.entities.WorkStation;
import model.event.InspectEvent;
import model.event.ModelEvent;
import model.event.ModelEventListener;

public class Runner extends Thread {
	public static void main(String[] args) {
		new Runner().start();
	}

	private List<ModelEventListener> eventListeners = new ArrayList<>();
	private FutureEventList eventList = ApplicationContext.getInstance().getFutureEventList();

	private Inspector c1Inspector;
	private Inspector c2c3Inspector;

	private WorkStation p1Station;
	private WorkStation p2Station;
	private WorkStation p3Station;

	@Override
	public void run() {
		setUpModel();

		System.out.println("Starting Simulation");
		System.out.println("===============================================================");
		while (!eventList.isDoneSim()) {
			ModelEvent nextEvent = eventList.dequeueEvent();
			if (nextEvent != null) {
				System.out.println("Consuming Event: " + nextEvent.getType() + " which occurs at " + nextEvent.getEventTime());
				eventListeners.forEach(listener -> listener.onEvent(nextEvent));
			}
		}

		// Log the throughput of each workstation
		System.out.println("===============================================================");
		System.out.println("P1 WorkStation produced: " + p1Station.getThroughput() + " of Product " + p1Station.getProductType());
		System.out.println("P2 WorkStation produced: " + p2Station.getThroughput() + " of Product " + p2Station.getProductType());
		System.out.println("P3 WorkStation produced: " + p3Station.getThroughput() + " of Product " + p3Station.getProductType());

		// Log Blocked Time
		System.out.println("C1 Inspector " + c1Inspector.getId() + " spent " + c1Inspector.getBlockTime() + " time units blocked");
		System.out.println("C2-C3 Inspector " + c2c3Inspector.getId() + " spent " + c2c3Inspector.getBlockTime() + " time units blocked");
	}

	private void setUpModel() {
		// Create Entities
		c1Inspector = new Inspector(1, List.of(ComponentType.C1));
		c2c3Inspector = new Inspector(2, List.of(ComponentType.C2, ComponentType.C3));

		p1Station = new WorkStation(1, ProductType.P1);
		p2Station = new WorkStation(2, ProductType.P2);
		p3Station = new WorkStation(3, ProductType.P3);

		Buffer p1c1Buffer = new Buffer(ComponentType.C1);
		Buffer p2c1Buffer = new Buffer(ComponentType.C1);
		Buffer p2c2Buffer = new Buffer(ComponentType.C2);
		Buffer p3c1Buffer = new Buffer(ComponentType.C1);
		Buffer p3c3Buffer = new Buffer(ComponentType.C3);

		// Link Buffers with Produces + Consumers
		c1Inspector.addTargetBuffer(p1c1Buffer);
		c1Inspector.addTargetBuffer(p2c1Buffer);
		c1Inspector.addTargetBuffer(p3c1Buffer);

		c2c3Inspector.addTargetBuffer(p2c2Buffer);
		c2c3Inspector.addTargetBuffer(p3c3Buffer);

		p1Station.addRequiredComponentBuffer(p1c1Buffer);

		p2Station.addRequiredComponentBuffer(p2c1Buffer);
		p2Station.addRequiredComponentBuffer(p2c2Buffer);

		p3Station.addRequiredComponentBuffer(p3c1Buffer);
		p3Station.addRequiredComponentBuffer(p3c3Buffer);

		// Register Entities as Event Listeners
		eventListeners.add(c1Inspector);
		eventListeners.add(c2c3Inspector);
		eventListeners.add(p1Station);
		eventListeners.add(p2Station);
		eventListeners.add(p3Station);

		// Add Initial Events (one per inspector)
		eventList.enqueueEvent(new InspectEvent(0f, 1, ComponentType.C1));
		eventList.enqueueEvent(new InspectEvent(0f, 2, ComponentType.C2));
	}
}
