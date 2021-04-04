package model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
	private List<BlockInterval> blockTimes = new ArrayList<>();
	private final String bufferPolicy;
	private int currentBuffer = 0;

	private List<Buffer> targetBuffers = new ArrayList<>();

	private List<ComponentType> componentTypes = new ArrayList<>();

	public Inspector(int id, List<ComponentType> componentTypes, String bufferPolicy) {
		this.id = id;
		this.componentTypes.addAll(componentTypes);
		this.bufferPolicy = bufferPolicy;
	}

	public void addTargetBuffer(Buffer targetBuffer) {
		targetBuffers.add(targetBuffer);
	}

	public int getId() {
		return id;
	}

	public float getBlockTime(float measureTime, float startTime) {
		float totalBlockedTime = 0f;
		for (BlockInterval interval : blockTimes) {
			if (interval.startBlockTime >= startTime && interval.endBlockTime < measureTime) {
				totalBlockedTime += (interval.endBlockTime - interval.startBlockTime);

			} else if (interval.startBlockTime < startTime && interval.endBlockTime < measureTime && interval.endBlockTime > startTime) {
				totalBlockedTime += (interval.endBlockTime - startTime);

			} else if (interval.startBlockTime >= startTime && interval.endBlockTime > measureTime) {
				totalBlockedTime += (measureTime - interval.startBlockTime);
			}
		}
		
		if (blockedEvent != null && blockedEvent.getEventTime() >= startTime) {
			totalBlockedTime += (measureTime - blockedEvent.getEventTime());
		} else if (blockedEvent != null && blockedEvent.getEventTime() < startTime) {
			totalBlockedTime += (measureTime - startTime);
		}

		return totalBlockedTime;
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
				
				blockTimes.add(new BlockInterval(blockedEvent.getEventTime(), produceEvent.getEventTime()));
				AddToBufferEvent addToQueue = new AddToBufferEvent(produceEvent.getEventTime(), id, blockedEvent.getComponentType());

				// unblock
				blockedEvent = null;

				ApplicationContext.getInstance().getFutureEventList().enqueueEvent(addToQueue);
			}
		}
	}

	private void handleInspectEvent(InspectEvent event) {
		if (event.getInspectorId() != id) {
			return; // This event isn't for me
		}

		if (blockedEvent != null) {
			return; // I am blocked
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

		if (blockedEvent != null) {
			return; // I am blocked
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
		if (bufferPolicy.equals(ApplicationContext.INSPECTOR_POLICY_LEAST_POP)) {
			// get target buffers that can consume the componentType
			// and finds the least populated target buffers
			return targetBuffers.stream().filter(buffer -> buffer.getComponentType() == componentType)
					.min(this::compareBuffers).orElseThrow();

		} else if (bufferPolicy.equals(ApplicationContext.INSPECTOR_POLICY_ORDERED)) {
			List<Buffer> eligibleBuffers = targetBuffers.stream().filter(buffer -> buffer.getComponentType() == componentType).collect(Collectors.toList());
			
			Buffer nextBuffer = eligibleBuffers.get(currentBuffer++);
			if (currentBuffer == eligibleBuffers.size()) {
				currentBuffer = 0;
			}

			if (eligibleBuffers.stream().allMatch(buf -> buf.size() == ApplicationContext.BUFFER_SIZE)) {
				return nextBuffer;
			}

			while (nextBuffer.size() == ApplicationContext.BUFFER_SIZE) {
				nextBuffer = eligibleBuffers.get(currentBuffer++);
				if (currentBuffer == eligibleBuffers.size()) {
					currentBuffer = 0;
				}
			}
			return nextBuffer;

		} else if (bufferPolicy.equals(ApplicationContext.INSPECTOR_POLICY_INVERTED)) {
			return targetBuffers.stream().filter(buffer -> buffer.getComponentType() == componentType)
					.min(this::compareBuffersInvertedPriority).orElseThrow();

		} else {
			System.out.println("Invalid Buffer Policy - " + bufferPolicy);
			return null;
		}
	}

	private int compareBuffers(Buffer a, Buffer b) {
		// 1. send to a buffer with the least waiters
		// 2. send to station 1, then station 2, then station 3 if tie.
		if (a.size() < b.size()) {
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

	private int compareBuffersInvertedPriority(Buffer a, Buffer b) {
		// 1. send to a buffer with the least waiters
		// 2. send to station 3, then station 2, then station 1 if tie.
		if (a.size() < b.size()) {
			return -1;
		} else if (a.size() == b.size()) {
			if (a.getOwner() < b.getOwner() ) {
				return 1;
			} else if (a.getOwner() == b.getOwner()) {
				return 0;
			} else {
				return -1;
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

	private class BlockInterval {
		final float startBlockTime;
		final float endBlockTime;

		BlockInterval(float startTime, float endTime) {
			startBlockTime = startTime;
			endBlockTime = endTime;
		}
	}
}
