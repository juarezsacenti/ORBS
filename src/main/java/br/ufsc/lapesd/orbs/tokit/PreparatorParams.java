package br.ufsc.lapesd.orbs.tokit;

import java.util.List;

import br.ufsc.lapesd.orbs.core.ContextOntologyParams;
import br.ufsc.lapesd.orbs.core.HitAggregatorParams;
import br.ufsc.lisa.sedim.core.HierarchyBuilderParams;
import br.ufsc.lisa.sedim.core.HierarchyTailorParams;
import br.ufsc.lisa.sedim.core.SemanticExpansionerParams;

public class PreparatorParams {
	private ContextOntologyParams contextOntologyParams;
	private List<DataSourceParams> annotationSources;
	private SemanticExpansionerParams semanticExpansionerParams;
	private HierarchyBuilderParams hierarchyBuilderParams;
	private HierarchyTailorParams hierarchyTailorParams;
	private HitAggregatorParams hitAggregatorParams;
	private List<String> propertiesOfInterest;
	private String FoIMatrixType;
	
	public ContextOntologyParams getContextOntologyParams() {
		return contextOntologyParams;
	}

	public SemanticExpansionerParams getSemanticExpansionerParams() {
		return semanticExpansionerParams;
	}

	public HierarchyBuilderParams getHierarchyBuilderParams() {
		return hierarchyBuilderParams;
	}

	public HierarchyTailorParams getHierarchyTailorParams() {
		return hierarchyTailorParams;
	}

	public List<DataSourceParams> getAnnotationSources() {
		return annotationSources;
	}

	public HitAggregatorParams getHitAggregatorParams() {
		return hitAggregatorParams;
	}

	public List<String> getPropertiesOfInterest() {
		return propertiesOfInterest;
	}

	public String getFoIMatrixType() {
		return FoIMatrixType;
	}

}
