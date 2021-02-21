package model.event;

import model.entities.ComponentType;

public class AddToBufferEvent extends ModelEvent {
	private final ComponentType componentType;
	private final int inspectorId;

	public AddToBufferEvent(float eventTime, int inspectorId, ComponentType componentType) {
		super(eventTime);
		this.inspectorId = inspectorId;
		this.componentType = componentType;
	}

	@Override
	public ModelEventType getType() {
		return ModelEventType.ADD_TO_BUFFER;
	}

	public int getInspectorId() {
		return inspectorId;
	}

	public ComponentType getComponentType() {
		return componentType;
	}
}
