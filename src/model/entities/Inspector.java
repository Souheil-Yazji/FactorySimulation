package model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.ApplicationContext;
import model.event.AddToBufferEvent;
import model.event.InspectEvent;
import model.event.ModelEvent;
import model.event.ModelEventListener;
import model.event.ModelEventType;
import model.event.ProductionEvent;

public class Inspector implements ModelEventListener {
	private final int id;

	private AddToBufferEvent blockedEvent;
	private List<Float> blockTimes = new ArrayList<>();

	private List<Buffer> targetBuffers = new ArrayList<>();
	private List<ComponentType> componentTypes = new ArrayList<>();

	public Inspector(int id, List<ComponentType> componentTypes) {
		this.id = id;
		this.componentTypes.addAll(componentTypes);
	}

	public void addTargetBuffer(Buffer targetBuffer) {
		targetBuffers.add(targetBuffer);
	}

	public int getId() {
		return id;
	}

	public float getBlockTime() {
		if (blockedEvent != null) {
			blockTimes.add(ApplicationContext.getInstance().getFutureEventList().getSystemTime() - blockedEvent.getEventTime());
		}
		return blockTimes.stream().reduce((a, b) -> a + b).orElse(0f);
	}

	@Override
	public void onEvent(ModelEvent event) {
		if (event.getType() == ModelEventType.PRODUCTION) {
			handleProduceEvent((ProductionEvent) event);

		} else if (event.getType() == ModelEventType.INSPECT) {
			handleInspectEvent((InspectEvent) event);

		} else if (event.getType() == ModelEventType.ADD_TO_BUFFER) {
			handleAddToQueueEvent((AddToBufferEvent) event);
		}
	}

	private void handleProduceEvent(ProductionEvent produceEvent) {
		// If we were blocked, try to see if we can push to a buffer
		if (blockedEvent != null) {
			// check if our target buffers could be impacted
			if (targetBuffers.stream().noneMatch(buffer -> buffer.getOwner() == produceEvent.getWorkStationId())) {
				return;
			}

			Buffer targetBuffer = determineTargetBuffer(blockedEvent.getComponentType());
			if (targetBuffer.addComponent()) {
				// if we can push, notify via event and record the amount of time we spent blocked
				AddToBufferEvent addToQueue = new AddToBufferEvent(produceEvent.getEventTime(), id, blockedEvent.getComponentType());
				ApplicationContext.getInstance().getFutureEventList().enqueueEvent(addToQueue);
				
				blockTimes.add(produceEvent.getEventTime() - blockedEvent.getEventTime());

				// unblock
				blockedEvent = null;

				// immediately start inspecting next component
				ComponentType nextComponent = determineNextComponent();
				ApplicationContext.getInstance().getFutureEventList().enqueueEvent(
						new InspectEvent(produceEvent.getEventTime(), id, nextComponent));
			}
		}
	}

	private void handleInspectEvent(InspectEvent event) {
		if (event.getInspectorId() != id) {
			return; // This event isn't for me
		}

		// determine time to inspect and schedule buffer push
		float addQueueTime = event.getEventTime() + determineInspectionTime(event.getComponentType());
		AddToBufferEvent addToQueue = new AddToBufferEvent(addQueueTime, id, event.getComponentType());
		ApplicationContext.getInstance().getFutureEventList().enqueueEvent(addToQueue);
	}

	private void handleAddToQueueEvent(AddToBufferEvent event) {
		if (event.getInspectorId() != id) {
			return; // This event isn't for me
		}

		// find a buffer to push this component to
		Buffer targetBuffer = determineTargetBuffer(event.getComponentType());
		if (!targetBuffer.addComponent()) {
			// We can't add to the full buffer, block this until the buffer is freed
			blockedEvent = event;

		} else {
			// immediately start inspecting next component
			ComponentType nextComponent = determineNextComponent();
			ApplicationContext.getInstance().getFutureEventList().enqueueEvent(
					new InspectEvent(event.getEventTime(), id, nextComponent));
		}
	}

	private float determineInspectionTime(ComponentType componentType) {
		return ApplicationContext.getInstance().getDelayGenerator().generateInspectionDelay(componentType);
	}

	private Buffer determineTargetBuffer(ComponentType componentType) {
		// get target buffers that can consume the componentType
		// and finds the least populated target buffers
		return targetBuffers.stream().filter(buffer -> buffer.getComponentType() == componentType)
				.min(this::compareBuffers).orElseThrow();
	}

	private int compareBuffers(Buffer a, Buffer b) {
		// 1. send to a buffer with the least waiters
		// 2. send to station 1, then station 2, then station 3 if tie.
		if (a.size() < b.size() ) {
			return -1;
		} else if (a.size() == b.size()) {
			if (a.getOwner() < b.getOwner() ) {
				return -1;
			} else if (a.getOwner() == b.getOwner()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	private ComponentType determineNextComponent() {
		Random random = new Random();
		int r = random.nextInt(componentTypes.size());
		return componentTypes.get(r);
	}
}
