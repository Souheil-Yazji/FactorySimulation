package model.entities;

import model.ApplicationContext;

public class Buffer {
	private int numComponents;
	private final ComponentType componentType;
	private int owner;

	public Buffer(ComponentType componentType) {
		this.componentType = componentType;
		numComponents = 0;
	}

	public int size() {
		return numComponents;
	}

	public boolean isEmpty() {
		return numComponents == 0;
	}

	public void removeComponent() {
		numComponents--;
	}

	public boolean addComponent() {
		if (numComponents < ApplicationContext.BUFFER_SIZE) {
			numComponents++;
			return true;
		} else {
			return false;
		}
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owningStationId) {
		this.owner = owningStationId;
	}
}
