package model.delay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import model.entities.ComponentType;
import model.entities.ProductType;

public class DistributionDelayGenerator implements DelayGenerator {
	private static final float COMPONENT_1_LAMBDA = 0.097f;
	private static final float COMPONENT_2_LAMBDA = 0.064f;
	private static final float COMPONENT_3_LAMBDA = 0.048f;

	private static final float PRODUCT_1_LAMBDA = 0.217f;
	private static final float PRODUCT_3_LAMBDA = 0.114f;

	// Maps Storing the Empirical Data for Product 2
	// Stores the Upper Bound of the Range and the CDF
	private static final SortedMap<Float, Float> PRODUCT_2_DATA = new TreeMap<>();
	{ // Set Product 2 Data
		PRODUCT_2_DATA.put(3.196f, 81f/300f);
		PRODUCT_2_DATA.put(6.300f, 138f/300f);
		PRODUCT_2_DATA.put(9.405f, 183f/300f);
		PRODUCT_2_DATA.put(12.509f, 211f/300f);
		PRODUCT_2_DATA.put(15.614f, 237f/300f);
		PRODUCT_2_DATA.put(18.718f, 247/300f);
		PRODUCT_2_DATA.put(21.823f, 254f/300f);
		PRODUCT_2_DATA.put(24.928f, 259f/300f);
		PRODUCT_2_DATA.put(28.032f, 271f/300f);
		PRODUCT_2_DATA.put(31.137f, 275f/300f);
		PRODUCT_2_DATA.put(34.241f, 279f/300f);
		PRODUCT_2_DATA.put(37.346f, 282f/300f);
		PRODUCT_2_DATA.put(40.451f, 288f/300f);
		PRODUCT_2_DATA.put(43.555f, 291f/300f);
		PRODUCT_2_DATA.put(46.66f, 293f/300f);
		PRODUCT_2_DATA.put(49.764f, 295f/300f);
		PRODUCT_2_DATA.put(52.869f, 299f/300f);
		PRODUCT_2_DATA.put(55.973f, 299f/300f);
		PRODUCT_2_DATA.put(59.078f, 1.0f);
	}

	// Stores the Slopes across the range of Product 2 Product Times
	private static final List<EmpiricalSlopeInfo> PRODUCT_2_SLOPES = new ArrayList<>();

	private static final Random rand = new Random();

	public DistributionDelayGenerator() {
		initializeProduct2();
	}

	@Override
	public float generateInspectionDelay(ComponentType type) {
		switch (type) {
			case C1:
				return generateRandomExponential(COMPONENT_1_LAMBDA);
			case C2:
				return generateRandomExponential(COMPONENT_2_LAMBDA);
			case C3:
				return generateRandomExponential(COMPONENT_3_LAMBDA);
			default:
				return 0f;
		}
	}

	@Override
	public float generateProductionDelay(ProductType type) {
		switch (type) {
			case P1:
				return generateRandomExponential(PRODUCT_1_LAMBDA);
			case P2:
				return generateRandomEmpiricalForProduct2();
			case P3:
				return generateRandomExponential(PRODUCT_3_LAMBDA);
			default:
				return 0f;
		}
	}

	private void initializeProduct2() {
		float lastCDF = 0f;
		float lastUpperBound = 0f;
		for (Float upperBound : PRODUCT_2_DATA.keySet()) {
			float cdf = PRODUCT_2_DATA.get(upperBound);

			float currentSlope = (cdf - lastCDF)/(upperBound-lastUpperBound);
			PRODUCT_2_SLOPES.add(new EmpiricalSlopeInfo(currentSlope, lastCDF, cdf, lastUpperBound));

			lastUpperBound = upperBound;
			lastCDF = cdf;
		}
	}

	private float generateRandomExponential(float lambda) {
		return (float) (Math.log(1-rand.nextFloat())/(-lambda));
	}

	private float generateRandomEmpiricalForProduct2() {
		float foundLowerBound = 0f;
		float foundSlope = 0f;
		float foundCDF = 0f;
		float randomNum = rand.nextFloat();

		for (EmpiricalSlopeInfo section : PRODUCT_2_SLOPES) {
			float lowBoundCDF = section.lowBoundCDF;
			float highBoundCDF = section.highBoundCDF;
			if (lowBoundCDF <= randomNum && randomNum < highBoundCDF) {
				foundSlope = section.slope;
				foundCDF = lowBoundCDF;
				foundLowerBound = section.lowerBound;
				break;
			}
		}

		return foundLowerBound + foundSlope * (randomNum - foundCDF);
	}

	private class EmpiricalSlopeInfo {
		final float slope;

		final float lowBoundCDF;
		final float highBoundCDF;

		final float lowerBound;

		EmpiricalSlopeInfo(float slope, float lowBoundCDF, float highBoundCDF, float lowerBound) {
			this.slope = slope;
			this.lowBoundCDF = lowBoundCDF;
			this.highBoundCDF = highBoundCDF;
			this.lowerBound = lowerBound;
		}
	}
}
