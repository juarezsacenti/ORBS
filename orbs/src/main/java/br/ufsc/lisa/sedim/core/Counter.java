package br.ufsc.lisa.sedim.core;

import java.util.HashMap;

import br.ufsc.lisa.sedim.core.io.KnowledgeBase;

public abstract class Counter {
	protected String userType;
	protected String userProperty;
	protected String countedProperty;
	protected String hierarchyProperty;
	protected String directHitProperty;
	protected String indirectHitProperty;
	
	public Counter(CounterParams cp) {
		this.userType = cp.getUserType();
		this.userProperty = cp.getUserProperty();
		this.countedProperty = cp.getCountedProperty();
		this.hierarchyProperty = cp.getHierarchyProperty();
		this.directHitProperty = cp.getDirectHitProperty();
		this.indirectHitProperty = cp.getIndirectHitProperty();
	}
	
	public abstract void computeCount(KnowledgeBase kb);
	public abstract void saveHits(KnowledgeBase kb);
	public abstract HashMap<String, HashMap<String, Integer>> getDirectHits();
	public abstract HashMap<String, HashMap<String, Integer>> getIndirectHits();
}
