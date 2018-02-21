package br.ufsc.lisa.sedim.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import br.ufsc.lisa.sedim.core.io.KnowledgeBase;
import br.ufsc.lisa.sedim.core.io.RDFTriple;

public class HierarchyBuilder {
	private HierarchyBuilderParams hbp;
	private KnowledgeBase kb;
	private List<Counter> counters;

	public HierarchyBuilder(HierarchyBuilderParams hierarchyBuilderParams, KnowledgeBase knowledgeBase) {
		this.hbp = hierarchyBuilderParams;
    	this.kb = knowledgeBase;
    	this.counters = new ArrayList<Counter>();
    	for (CounterParams cp : hierarchyBuilderParams.getCountersParams()) {
    		switch(cp.getCounterType()) {
    		case "userCounter":
        		this.counters.add(new UserCounter(cp));
        		break;
    		case "globalCounter":
        		this.counters.add(new GlobalCounter(cp));
        		break;
    		default:			
        		this.counters.add(new UserCounter(cp));
    		}
    	}
	}

	private void mapPropertiesToHierarchy(String hierarchyProperty, List<PropertyParams> mappingProperties) {
		String property, object;
		ArrayList<String> subjects = new ArrayList<String>(), objects = new ArrayList<String>();
		
		kb.addObjectStatement(hierarchyProperty, 
				"http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 
				"http://www.w3.org/2002/07/owl#topObjectProperty");
		
		for(PropertyParams pp : mappingProperties) {
			property = pp.getProperty();
			if(!pp.isTransitive()) {
				if(subjects.isEmpty()) {
					for(RDFTriple triple : kb.getStatements(null, property, null) ) {
						object = triple.getObject();
						kb.addObjectStatement(triple.getSubject(), hierarchyProperty, object);
						//System.out.println("1: "+triple.getSubject() +", "+ hierarchyProperty +", "+  object);
						if(!objects.contains(object)) { objects.add(object); }
					}			
				} else { 
					for(String subject : subjects) {
						for(RDFTriple triple : kb.getStatements(subject, property, null) ) {
							object = triple.getObject();
							kb.addObjectStatement(triple.getSubject(), hierarchyProperty, object);
							//System.out.println("2: "+ triple.getSubject() +", "+ hierarchyProperty +", "+  object);
							if(!objects.contains(object)) { objects.add(object); }
						}
					}
				}
			} else {
				if(subjects.isEmpty()) {
					for(RDFTriple triple : kb.getStatements(null, property, null) ) {
						object = triple.getObject();
						kb.addObjectStatement(triple.getSubject(), hierarchyProperty, object);
						//System.out.println("3: "+triple.getSubject() +", "+ hierarchyProperty +", "+  object);
						if(!subjects.contains(object)) { subjects.add(object); }
						if(!objects.contains(object)) { objects.add(object); }
					}
				} else {
					for(int i=0;subjects.size() > i;++i){ 
						String subject = subjects.get(i);
						for(RDFTriple triple : kb.getStatements(subject, property, null) ) {
							object = triple.getObject();
							kb.addObjectStatement(triple.getSubject(), hierarchyProperty, object);
							//System.out.println("4: "+triple.getSubject() +", "+ hierarchyProperty +", "+  object);
							if(!subjects.contains(object)) { subjects.add(object); }
							if(!objects.contains(object)) { objects.add(object); }
						}
					}
				}
			}
			if(objects.size() == 0) break;
			subjects = objects;
			objects = new ArrayList<String>();
		}
	}

	private void removeAntecipatedAssociations(String hierarchyProperty) {}

	private void computeNumberOfHits(CounterParams cp) {
		for(Counter counter : counters) {
			counter.computeCount(kb);
			counter.saveHits(kb);
		}
	}
	
	public void run() {
		String hierarchyProperty;
		for(HierarchyParams hp : hbp.getHierarchiesParams()) {
			hierarchyProperty = hp.getHierarchyProperty();
			mapPropertiesToHierarchy(hierarchyProperty, hp.getMappingProperties());
			removeAntecipatedAssociations(hierarchyProperty);
		}
		for(CounterParams cp : hbp.getCountersParams()) {
			computeNumberOfHits(cp);
		}
	}
	
	public List<Counter> getCounters() {
		return counters;
	}
}
