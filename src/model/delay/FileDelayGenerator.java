package model.delay;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.ApplicationContext;
import model.entities.ComponentType;
import model.entities.ProductType;

public class FileDelayGenerator implements DelayGenerator {
	// read files and put them into 6 arrays
	//    C1 Inspection Time, C2 Inspection Time, C3 Inspection Time
	//    P1 Production Time, P2 Production Time, P3 Production Time
	private Map<String, List<Float>> delayData = new HashMap<>();
	private Random random = new Random();

	public FileDelayGenerator() {
		// Read all the files and place into arrays
		try {
			readFile(ApplicationContext.C1_INSPECTION_DATA, "C1");
			readFile(ApplicationContext.C2_INSPECTION_DATA, "C2");
			readFile(ApplicationContext.C3_INSPECTION_DATA, "C3");
			readFile(ApplicationContext.W1_PRODUCTION_DATA, "P1");
			readFile(ApplicationContext.W2_PRODUCTION_DATA, "P2");
			readFile(ApplicationContext.W3_PRODUCTION_DATA, "P3");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float generateInspectionDelay(ComponentType type) {
		List<Float> data = delayData.get(type.toString());
		if (data != null) {
			return data.get(random.nextInt(data.size()));
		}
		return 0f;
	}

	public float generateProductionDelay(ProductType productType) {
		List<Float> data = delayData.get(productType.toString());
		if (data != null) {
			return data.get(random.nextInt(data.size()));
		}
		return 0f;
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
