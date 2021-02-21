package model.event;

public class ProductionEvent extends ModelEvent {
	private final int workStationID;

	public ProductionEvent(float eventTime, int id) {
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
