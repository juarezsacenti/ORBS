package br.ufsc.lisa.sedim.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.ufsc.lapesd.sro.tokit.UserItemEvent;
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
	
	public List<Counter> getCounters() {
		return counters;
	}
	
	public void run() {
		String hierarchyProperty;
		for(HierarchyParams hp : hbp.getHierarchiesParams()) {
			hierarchyProperty = hp.getHierarchyProperty();
			kb.addObjectStatement(hierarchyProperty, 
					"http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 
					"http://www.w3.org/2002/07/owl#topObjectProperty");
			kb.addObjectStatement(hierarchyProperty, 
					"http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 
					"http://www.lapesd.inf.ufsc.br/ontology/recont.owl#hasObjectFI");
			mapPropertiesToHierarchy(hierarchyProperty, hp.getMappingProperties());
			printHierarchy(hierarchyProperty,"genre1");
			removeAnticipatedAssociations(hierarchyProperty);
			printHierarchy(hierarchyProperty,"genre2");
		}
		
		for(CounterParams cp : hbp.getCountersParams()) {
			computeNumberOfHits(cp);
		}
	}
	

	private void mapPropertiesToHierarchy(String hierarchyProperty, List<PropertyParams> mappingProperties) {
		String property, object;
		ArrayList<String> subjects = new ArrayList<String>(), objects = new ArrayList<String>();
		
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

	private void removeAnticipatedAssociations(String hierarchyProperty) {		
		List<String> fathers;
		RDFTriple edge;
		String father;
		Iterator<RDFTriple> fathersIt, edgesIt = kb.getStatements(null, hierarchyProperty, null).iterator();
		System.out.println("#### Removing anticipated association ####");
		
		while(edgesIt.hasNext()) {
            edge = edgesIt.next();
       		fathers = new ArrayList<String>();
       		fathersIt = kb.getStatements(edge.getSubject(), hierarchyProperty, null).iterator();
            while(fathersIt.hasNext()) {
    			father = fathersIt.next().getObject();
    			fathers.add(father);
    		}
            
    		for(String father2 : fathers) {
    			if(isAnticipatedAssociation(hierarchyProperty, father2, fathers)) {
    				System.out.println("Removing anticipated association: "+edge.toString());
    				kb.removeStatement(edge);
    			}
    		}
        }
	}
	
	private boolean isAnticipatedAssociation(String hierarchyProperty, String father, List<String> allFathers) {
		boolean isAnticipated = false;
	
		String otherFather;
		HashSet<String> ancestors;
		Iterator<String> itR = allFathers.iterator();
		while(itR.hasNext() && !isAnticipated) {
			otherFather = itR.next();
			if(!father.equals(otherFather)) {
				ancestors = kb.getAncestors(hierarchyProperty, otherFather);
				if(ancestors.contains(father)) {
					isAnticipated = true;
				}
			}
		}
		return isAnticipated;
	}


	private void computeNumberOfHits(CounterParams cp) {
		for(Counter counter : counters) {
			counter.computeCount(kb);
			/*
			 *  Saving Hits could not be a good idea. Let our context ontology have 1M ratings from 6K users, 
			 *  if we save hits then each item and attribute must have 2*6K direct and indirect hits.
			 *  It means we just double the size of context ontology.
			 */
			//counter.saveHits(kb);
		}
	}
	
	private void printHierarchy(String hierarchyProperty, String name) {
		String sroPreparedItemModelPath= "src/resources/main/temp/hierarchy-"+name+".csv";
		Iterator<RDFTriple> triplesIt = kb.getStatements(null, hierarchyProperty, null).iterator();
		
		RDFTriple triple;
		try {
			OutputStream os = new FileOutputStream(sroPreparedItemModelPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
						
			while(triplesIt.hasNext()) {
				triple = triplesIt.next();
				writer.write(""+ triple.getSubject() +","+triple.getPredicate()+","+triple.getObject()+"\n");
	    	}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
