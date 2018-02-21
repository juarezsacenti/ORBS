package br.ufsc.lapesd.sro.tokit;

import br.ufsc.lapesd.sro.core.ContextOntologyParams;
import br.ufsc.lapesd.sro.core.HitAggregatorParams;
import br.ufsc.lisa.sedim.core.HierarchyBuilderParams;
import br.ufsc.lisa.sedim.core.HierarchyTailorParams;
import br.ufsc.lisa.sedim.core.SemanticExpansionerParams;

public class PreparatorParams {
	private String appName;
	private ContextOntologyParams contextOntologyParams;
	private DataSourceParams annotationSourceParams;
	private SemanticExpansionerParams semanticExpansionerParams;
	private HierarchyBuilderParams hierarchyBuilderParams;
	private HierarchyTailorParams hierarchyTailorParams;
	private HitAggregatorParams hitAggregatorParams;
	
	public String getAppName() {
		return appName;
	}
	
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

	public DataSourceParams getAnnotationSourceParams() {
		return annotationSourceParams;
	}

	public HitAggregatorParams getHitAggregatorParams() {
		return hitAggregatorParams;
	}

}
