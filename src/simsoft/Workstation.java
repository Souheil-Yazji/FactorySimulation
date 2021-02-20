package simsoft;

import java.util.ArrayList;
import java.util.List;

public class Workstation {

	private List<Buffer> bufferList;

	
	public Workstation() {
		bufferList = new ArrayList<Buffer>();
	}
	
	public void addBuffer(Buffer b) {
		bufferList.add(b);
	}
	
}
