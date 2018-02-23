package br.ufsc.lisa.sedim.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufsc.lisa.sedim.core.io.KnowledgeBase;
import br.ufsc.lisa.sedim.core.io.RDFTriple;

public class GlobalCounter extends Counter {

	private HashMap<String, Integer> directHits;
	private HashMap<String, Integer> indirectHits;

	public GlobalCounter(CounterParams cp) {
		super(cp);
	}

	@Override
	public void computeCount(KnowledgeBase kb) {
		directHits = new HashMap<String, Integer>();
		indirectHits = new HashMap<String, Integer>();
		String user, interaction, annotatedObject;
		List<String> indirects = new ArrayList<String>();
		
		/**
		 * Counting direct hits
		 */
		for(RDFTriple isUserTriple : kb.getStatements(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", this.userType) ) {
			user = isUserTriple.getSubject();
			for(RDFTriple userPropertyTriple : kb.getStatements(user, this.userProperty, null) ) {
				interaction = userPropertyTriple.getObject();
				for(RDFTriple triple3 : kb.getStatements(interaction, this.countedProperty, null) ) {
					annotatedObject = triple3.getObject();
					directHits.put(annotatedObject, directHits.getOrDefault(annotatedObject, 0) + 1);
				}
			}
		}

		/**
		 * Counting indirect hits
		 */
		for(Entry<String, Integer> entry : directHits.entrySet()) {
			annotatedObject = entry.getKey();
			indirectHits.put(annotatedObject, entry.getValue());
			indirects.add(annotatedObject);
		}
		for(int i = 0; i < indirects.size(); ++i) {
			String indirect = indirects.get(i);
			for(RDFTriple triple : kb.getStatements(indirect, this.hierarchyProperty, null) ) {
				annotatedObject = triple.getObject();
				indirectHits.put(annotatedObject, indirectHits.getOrDefault(annotatedObject, 0) + indirectHits.get(indirect));
				indirects.add(annotatedObject);
			}
		}
		

	}

	@Override
	public void saveHits(KnowledgeBase kb) {
		for(Entry<String, Integer> entry : directHits.entrySet()) {
			kb.addLiteralStatement(entry.getKey(), this.directHitProperty, entry.getValue());
		}
		for(Entry<String, Integer> entry : indirectHits.entrySet()) {
			kb.addLiteralStatement(entry.getKey(), this.indirectHitProperty, entry.getValue());
		}		
	}

	@Override
	public HashMap<String, HashMap<String, Integer>> getDirectHits() {
		HashMap<String, HashMap<String, Integer>> result = new HashMap<String, HashMap<String, Integer>>();
		result.put("global", directHits);
		return result;
	}

	@Override
	public HashMap<String, HashMap<String, Integer>> getIndirectHits() {
		HashMap<String, HashMap<String, Integer>> result = new HashMap<String, HashMap<String, Integer>>();
		result.put("global", indirectHits);
		return result;
	}

}
