package model.event;

public class ProduceEvent extends ModelEvent {
	private final int workStationID;

	public ProduceEvent(float eventTime, int id) {
		super(eventTime);
		this.workStationID = id;
	}

	@Override
	public ModelEventType getType() {
		return ModelEventType.PRODUCE;
	}

	public int getWorkStationId() {
		return workStationID;
	}
}
