package model.event;

import model.entities.ComponentType;

public class InspectEvent extends ModelEvent {
	private final int inspectorId;
	private final ComponentType componentType;

	public InspectEvent(float eventTime, int inspectorId, ComponentType componentType) {
		super(eventTime);
		this.inspectorId = inspectorId;
		this.componentType = componentType;
	}

	@Override
	public ModelEventType getType() {
		return ModelEventType.INSPECT;
	}

	public int getInspectorId() {
		return inspectorId;
	}

	public ComponentType getComponentType() {
		return componentType;
	}
}
