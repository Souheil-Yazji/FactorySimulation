package model.event;

public abstract class ModelEvent implements Comparable<ModelEvent> {
	private final float eventTime;

	public ModelEvent(float eventTime) {
		this.eventTime = eventTime;
	}

	public abstract ModelEventType getType();

	public float getEventTime() {
		return eventTime;
	}

	@Override
	public int compareTo(ModelEvent other) {
		Float thisTime = this.eventTime;
		Float otherTime = other.eventTime;
		return thisTime.compareTo(otherTime);
	}
}
