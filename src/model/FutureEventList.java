package model;

import java.util.PriorityQueue;
import java.util.Queue;

import model.event.ModelEvent;

public class FutureEventList {
	private final Queue<ModelEvent> eventList = new PriorityQueue<>();

	private boolean isDone = false;

	public void enqueueEvent(ModelEvent event) {
		if (!isDone) {
			eventList.add(event);
		}
	}

	public ModelEvent dequeueEvent() {
		return eventList.poll();
	}

	public boolean isDoneSim() {
		isDone = eventList.peek() == null || eventList.peek().getEventTime() >= ApplicationContext.STOP_SIM_TIME;
		return isDone && eventList.isEmpty();
	}

	public void resetSystem() {
		eventList.clear();
		isDone = false;
	}
}
