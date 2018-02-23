package br.ufsc.lisa.sedim.core;

import java.util.List;

public class HierarchyParams {
	private String hierarchyProperty;
	private List<PropertyParams> mappingProperties;
	
	public String getHierarchyProperty() {
		return hierarchyProperty;
	}

	public List<PropertyParams> getMappingProperties() {
		return mappingProperties;
	}
}
