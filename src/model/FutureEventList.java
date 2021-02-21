package model;

import java.util.PriorityQueue;
import java.util.Queue;

import model.event.ModelEvent;

public class FutureEventList {
	private Queue<ModelEvent> eventList = new PriorityQueue<>();

	private float currentEventTime = 0f;
	private boolean isDone = false;

	public void enqueueEvent(ModelEvent event) {
		if (!isDone) {
			currentEventTime = event.getEventTime();
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

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public float getCurrentEventTime() {
		return currentEventTime;
	}
}
