package simsoft;

import java.util.LinkedList;

public class Buffer {

	public static int BUFFER_SIZE = 2;
	private Component componentType;
	private LinkedList<Component> comp;
	
	public Buffer(Component c) {
		this.componentType = c;
		comp = new LinkedList<Component>();
	}

	/**
	 * @return the componentType
	 */
	public Component getComponentType() {
		return componentType;
	}

	
	public Component pop() {
		if (comp.isEmpty()) {
			
			return null; 			// Block workstation
		}
		return comp.pop();
	}

	public boolean push(Component c) {
		if (isFull() || c != componentType) {
			return false;
		} 
		comp.add(c);
		return true;
	}
	
	/**
	 * @return the full
	 */
	public boolean isFull() {
		return comp.size() == BUFFER_SIZE;
	}
	
}
