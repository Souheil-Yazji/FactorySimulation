package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.entities.ComponentType;

public class DelayGenerator {
	// read files and put them into 6 arrays
	//    C1 Inspection Time, C2 Inspection Time, C3 Inspection Time
	//    P1 Production Time, P2 Production Time, P3 Production Time
	private Map<String, List<Float>> delayData = new HashMap<>();
	private Random random = new Random();
	private int c1Count = 0;

	public DelayGenerator() {
		// Read all the files and place into arrays
		try {
			readFile(ApplicationContext.C1_INSPECTION_DATA, "C1");
			readFile(ApplicationContext.C2_INSPECTION_DATA, "C2");
			readFile(ApplicationContext.C3_INSPECTION_DATA, "C3");
			readFile(ApplicationContext.W1_PRODUCTION_DATA, "1");
			readFile(ApplicationContext.W2_PRODUCTION_DATA, "2");
			readFile(ApplicationContext.W3_PRODUCTION_DATA, "3");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float generateInspectionDelay(ComponentType type) {
//		if (type == ComponentType.C1) {
//			// do this linearly
//			List<Float> data = delayData.get(type.toString());
//			float delay = data.get(c1Count);
//			c1Count++;
//
//			ApplicationContext.getInstance().getFutureEventList().setDone(c1Count == 300f);
//
//			return delay;
//		} else {
			List<Float> data = delayData.get(type.toString());
			if (data != null) {
				return data.get(random.nextInt(data.size()));
			}
			return 0f;
//		}
	}

	public float generateProductionDelay(int workStationId) {
		List<Float> data = delayData.get(new Integer(workStationId).toString());
		if (data != null) {
			return data.get(random.nextInt(data.size()));
		}
		return 20f;
	}

	private void readFile(String fileName, String listName) throws IOException {
        try (FileReader dataFile = new FileReader("resources/" + fileName)) {
        	try (BufferedReader reader = new BufferedReader(dataFile)) {
        		List<Float> data = new ArrayList<>();
                String line = null;
                while ((line = reader.readLine()) != null && !line.equals("")) {
                	data.add(Float.parseFloat(line));
                }
                delayData.put(listName, data);
        	}
        }
	}
}
