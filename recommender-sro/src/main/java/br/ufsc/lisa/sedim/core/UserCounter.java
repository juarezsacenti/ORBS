package br.ufsc.lisa.sedim.core;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufsc.lisa.sedim.core.io.KnowledgeBase;
import br.ufsc.lisa.sedim.core.io.RDFTriple;

public class UserCounter extends Counter {
	private HashMap<String, HashMap<String, Integer>> usersDirectHits;
	private HashMap<String, HashMap<String, Integer>> usersIndirectHits;
	
	public UserCounter(CounterParams cp) {
		super(cp);
	}

	@Override
	public void computeCount(KnowledgeBase kb) {
		usersDirectHits = new HashMap<String, HashMap<String,Integer>>();
		usersIndirectHits = new HashMap<String, HashMap<String,Integer>>();
		
		HashMap<String, Integer> directHits;
		HashMap<String, Integer> indirectHits;
		String user, interaction, annotatedObject, upperLevel;
		List<String> indirects;
		
		/**
		 * Counting direct hits
		 */
		for(RDFTriple isUserTriple : kb.getStatements(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", this.userType) ) {
			directHits = new HashMap<String, Integer>();
			user = isUserTriple.getSubject();
			for(RDFTriple userPropertyTriple : kb.getStatements(user, this.userProperty, null) ) {
				interaction = userPropertyTriple.getObject();
				for(RDFTriple triple3 : kb.getStatements(interaction, this.countedProperty, null) ) {
					annotatedObject = triple3.getObject();
					directHits.put(annotatedObject, directHits.getOrDefault(annotatedObject, 0) + 1);
				}
			}
			usersDirectHits.put(user, directHits);
		}
		
		/**
		 * Counting indirect hits
		 */
		for(Entry<String, HashMap<String, Integer>> entry1 : usersDirectHits.entrySet()) {
			indirectHits = new HashMap<String, Integer>();
			indirects = new ArrayList<String>();
			user = entry1.getKey();
			
			directHits = entry1.getValue();
			for(Entry<String, Integer> entry2 : directHits.entrySet()) {
				annotatedObject = entry2.getKey();
				indirectHits.put(annotatedObject, entry2.getValue());
				indirects.add(annotatedObject);
			}

			for(int i = 0; i < indirects.size(); ++i) {
				String indirect = indirects.get(i);
				for(RDFTriple triple : kb.getStatements(indirect, this.hierarchyProperty, null) ) {
					upperLevel = triple.getObject();
					indirectHits.put(upperLevel, indirectHits.getOrDefault(upperLevel, 0) + indirectHits.get(indirect));
					indirects.add(upperLevel);
				}
			}		
			usersIndirectHits.put(user, indirectHits);
		}		
	}
	
	/**
	 * Saves direct and indirect hits inside a KnowledgeBase.
	 * @param kb a KnowledgeBase object giving the saving repository
	 * @see KnowledgeBase
	 */
	@Override
	public void saveHits(KnowledgeBase kb) {
		AbstractMap<String, Integer> directHits;
		String userId;
		for(Entry<String, HashMap<String, Integer>> entry1 : usersDirectHits.entrySet()) {
			userId = entry1.getKey().substring(61);
			directHits = entry1.getValue();
			for(Entry<String, Integer> entry2 : directHits.entrySet()) {
				kb.addLiteralStatement(entry2.getKey(), this.directHitProperty+userId, entry2.getValue());
			}
		}
		
		for(Entry<String, HashMap<String, Integer>> entry1 : usersIndirectHits.entrySet()) {
			userId = entry1.getKey().substring(61);
			directHits = entry1.getValue();
			for(Entry<String, Integer> entry2 : directHits.entrySet()) {
				kb.addLiteralStatement(entry2.getKey(), this.indirectHitProperty + userId, entry2.getValue());
			}
		}
	}

	@Override
	public HashMap<String, HashMap<String, Integer>> getDirectHits() {
		return usersDirectHits;
	}

	@Override
	public HashMap<String, HashMap<String, Integer>> getIndirectHits() {
		return usersIndirectHits;
	}

}
