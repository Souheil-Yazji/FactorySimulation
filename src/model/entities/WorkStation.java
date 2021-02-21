package model.entities;

import java.util.ArrayList;
import java.util.List;

import model.ApplicationContext;
import model.event.ModelEvent;
import model.event.ModelEventListener;
import model.event.ModelEventType;
import model.event.ProductionEvent;

public class WorkStation implements ModelEventListener {
	private final List<Buffer> requiredComponents = new ArrayList<>();
	private final ProductType productType;

	private int id;

	private int throughput = 0;
	private boolean isBusy = false;

	public WorkStation(int id, ProductType productType) {
		this.id = id;
		this.productType = productType;
	}

	public void addRequiredComponentBuffer(Buffer componentBuffer) {
		componentBuffer.setOwner(id);
		requiredComponents.add(componentBuffer);
	}

	public ProductType getProductType() {
		return productType;
	}

	public int getThroughput() {
		return throughput;
	}

	@Override
	public void onEvent(ModelEvent event) {
		// Workstations only care about add to queue events and Produce Events
		if (event.getType() == ModelEventType.ADD_TO_BUFFER && !isBusy) {
			// try to start production
			attemptProduction(event.getEventTime());

		} else if (event.getType() == ModelEventType.PRODUCTION) {
			if (((ProductionEvent) event).getWorkStationId() != id) {
				return;
			}

			// create product
			throughput++;
			System.out.println("WorkStation " + id + " Produced " + productType);
			isBusy = false;

			// try to start another production
			attemptProduction(event.getEventTime());
		}
	}

	public float determineProcessTime() {
		return ApplicationContext.getInstance().getDelayGenerator().generateProductionDelay(id);
	}

	private void attemptProduction(float startTime) {
		// Check if required components are available
		if (requiredComponents.stream().anyMatch(Buffer::isEmpty)) {
			return;
		}

		// Remove required components
		requiredComponents.forEach(Buffer::removeComponent);
		
		// figure out how long it took and notify others that you produced a product
		ApplicationContext.getInstance().getFutureEventList().enqueueEvent(new ProductionEvent(startTime + determineProcessTime(), id));
		System.out.println("WorkStation " + id + " Starting Production of " + productType);
		isBusy = true;
	}
}
