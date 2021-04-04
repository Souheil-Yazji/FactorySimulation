package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

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
		List<Results> betweenReplicationResults = new ArrayList<>();
		for (int i = 0; i < ApplicationContext.REPLICATIONS; i++) {
			betweenReplicationResults.add(singleRun(i + 1));
		}

		System.out.println("===============================================================");
		System.out.println("Between Replication Results: (P1 Throughput, P2 Throughput, P3 Throughput, C1 Block Time, C2C3 Block Time)");
		for (Results result : betweenReplicationResults) {
			StringBuilder out = new StringBuilder()
										.append(result.p1Throughput).append(',')
										.append(result.p2Throughput).append(',')
										.append(result.p3Throughput).append(',')
										.append(result.c1BlockTime).append(',')
										.append(result.c2c3BlockTime);
			System.out.println(out);
		}
	}

	private Results singleRun(int runNumber) {
		setUpModel();

		System.out.println("============================== SIM RUN " + runNumber + " START =================================");

		SortedMap<Integer, Results> historicalResults = new TreeMap<>();

		Results preCutOffResults = null;
		float cutOffTime = ApplicationContext.CUT_OFF_INTERVAL * ApplicationContext.COLLECT_METRIC_INTERVAL;

		while (!eventList.isDoneSim()) {
			ModelEvent nextEvent = eventList.dequeueEvent();
			if (nextEvent != null) {
				eventListeners.forEach(listener -> listener.onEvent(nextEvent));

				// Log Measurements
				int interval = (int)(nextEvent.getEventTime()) / ApplicationContext.COLLECT_METRIC_INTERVAL;
				
				// Cut off interval metrics need to be disregarded
				if (interval == ApplicationContext.CUT_OFF_INTERVAL && preCutOffResults == null) {
					preCutOffResults = new Results(p1Station.getThroughput(), p2Station.getThroughput(), p3Station.getThroughput(),
													c1Inspector.getBlockTime(cutOffTime, 0), 
													c2c3Inspector.getBlockTime(cutOffTime, 0));

				// Take Metrics for Each Interval
				} else if (interval > ApplicationContext.CUT_OFF_INTERVAL && !historicalResults.containsKey(interval) && preCutOffResults != null) {
					float intervalEndTime = interval * ApplicationContext.COLLECT_METRIC_INTERVAL;
					float intervalStartTime = (interval - 1) * ApplicationContext.COLLECT_METRIC_INTERVAL;
					historicalResults.put(interval, 
							new Results(
									p1Station.getThroughput() - preCutOffResults.p1Throughput, 
									p2Station.getThroughput() - preCutOffResults.p2Throughput,
									p3Station.getThroughput() - preCutOffResults.p3Throughput,
									c1Inspector.getBlockTime(intervalEndTime, intervalStartTime),
									c2c3Inspector.getBlockTime(intervalEndTime, intervalStartTime)));
				}
			}
		}

		// Log the throughput of each workstation
		int lastTime = ApplicationContext.CUT_OFF_INTERVAL;
		Results lastResult = new Results(0,0,0,0,0);

		for (Entry<Integer, Results> measurement : historicalResults.entrySet()) {
			int time = measurement.getKey();
			Results result = measurement.getValue();

			System.out.println("== Between Time: " + 
					lastTime * ApplicationContext.COLLECT_METRIC_INTERVAL + " to " +
					time * ApplicationContext.COLLECT_METRIC_INTERVAL);

			System.out.println("==== P1 WorkStation produced: " + 
					(result.p1Throughput - lastResult.p1Throughput) + " of Product " + p1Station.getProductType());
			System.out.println("==== P2 WorkStation produced: " + 
					(result.p2Throughput - lastResult.p2Throughput) + " of Product " + p2Station.getProductType());
			System.out.println("==== P3 WorkStation produced: " + 
					(result.p3Throughput - lastResult.p3Throughput) + " of Product " + p3Station.getProductType());
			System.out.println("==== C1 Inspector spent " + 
					(result.c1BlockTime) + " time units blocked");
			System.out.println("==== C2-C3 Inspector spent " + 
					(result.c2c3BlockTime) + " time units blocked");

			lastTime = time;
			lastResult = result;
		}

		System.out.println("===============================================================");

		Results cumulativeResults = new Results(lastResult.p1Throughput, lastResult.p2Throughput, lastResult.p3Throughput,
												c1Inspector.getBlockTime(ApplicationContext.STOP_SIM_TIME, cutOffTime),
												c2c3Inspector.getBlockTime(ApplicationContext.STOP_SIM_TIME, cutOffTime));

		System.out.println("== Cumulative Results");
		System.out.println("==== P1 WorkStation produced: " + 
				(cumulativeResults.p1Throughput) + " of Product " + p1Station.getProductType());
		System.out.println("==== P2 WorkStation produced: " + 
				(cumulativeResults.p2Throughput) + " of Product " + p2Station.getProductType());
		System.out.println("==== P3 WorkStation produced: " + 
				(cumulativeResults.p3Throughput) + " of Product " + p3Station.getProductType());
		System.out.println("==== C1 Inspector spent " + 
				(cumulativeResults.c1BlockTime) + " time units blocked");
		System.out.println("==== C2-C3 Inspector spent " + 
				(cumulativeResults.c2c3BlockTime) + " time units blocked");

		System.out.println("============================== SIM RUN " + runNumber + " END =================================");

		return cumulativeResults;
	}

	private void setUpModel() {
		// Reset Future Event List and Listeners
		eventList.resetSystem();
		eventListeners.clear();

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

	private class Results {
		final int p1Throughput;
		final int p2Throughput;
		final int p3Throughput;

		final float c1BlockTime;
		final float c2c3BlockTime;

		Results(int p1Throughput, int p2Throughput, int p3Throughput, float c1BlockTime, float c2c3BlockTime) {
			this.p1Throughput = p1Throughput;
			this.p2Throughput = p2Throughput;
			this.p3Throughput = p3Throughput;

			this.c1BlockTime = c1BlockTime;
			this.c2c3BlockTime = c2c3BlockTime;
			
		}
	}
}
