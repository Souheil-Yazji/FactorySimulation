package simsoft;

import java.util.ArrayList;
import java.util.List;

public class Inspector{

	private List<Buffer> bufferList;
	private int id;

	public Inspector(int id) {
		this.id = id;
		bufferList = new ArrayList<Buffer>();
	}
	
	public void addBuffer(Buffer b) {
		bufferList.add(b);
	}
	
	
	/**
	 * @return the id
	 */
	public int getid() {
		return id;
	}

		
}
