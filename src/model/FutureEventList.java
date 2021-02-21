package model;

import java.util.PriorityQueue;
import java.util.Queue;

import model.event.ModelEvent;

public class FutureEventList {
	private Queue<ModelEvent> eventList = new PriorityQueue<>();

	private float systemTime = 0f;
	private boolean isDone = false;

	public void enqueueEvent(ModelEvent event) {
		if (!isDone) {
			eventList.add(event);
		}
	}

	public ModelEvent dequeueEvent() {
		ModelEvent nextEvent = eventList.poll();
		systemTime = nextEvent.getEventTime();
		return nextEvent;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public boolean isDoneSim() {
		isDone = eventList.peek() == null || eventList.peek().getEventTime() >= ApplicationContext.STOP_SIM_TIME;
		return isDone && eventList.isEmpty();
	}

	public float getSystemTime() {
		return systemTime;
	}
}
