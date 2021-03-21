package model.delay;

import model.entities.ComponentType;
import model.entities.ProductType;

public interface DelayGenerator {
	public float generateInspectionDelay(ComponentType type);
	public float generateProductionDelay(ProductType productType);
}
