package br.ufsc.lapesd.sro.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufsc.lisa.sedim.core.Counter;

public class HitAggregator {
	private String hitType;
	private String aggregationType;

	public HitAggregator(HitAggregatorParams hap) {
  	    this.hitType = hap.getHitType();
  	    this.aggregationType = hap.getAggregationType();
  	}

	public HashMap<String, HashMap<String, Float>> aggregate(List<Counter> counters) {
		HashMap<String, HashMap<String, Float>> ha = new HashMap<String, HashMap<String, Float>>();
		HashMap<String, Float> userAttributes;
		HashMap<String, HashMap<String, Integer>> counterHits;
		HashMap<String, Integer> attributeSet;
		String user, key;
		float value, directHitsTotal;
		
		for(Counter counter : counters) {			
			switch(hitType) {
			case "direct":
				counterHits = counter.getDirectHits();
				break;
			case "indirect":
				counterHits = counter.getIndirectHits();
				break;
			default:
				counterHits = counter.getIndirectHits();				
			}
			
			switch(aggregationType) {
			case "sum":
				for(Entry<String, HashMap<String, Integer>> entry1 : counterHits.entrySet()) {
					user = entry1.getKey();
					if(!ha.containsKey(user)) {ha.put(user, new HashMap<String, Float>()); } 
					userAttributes = ha.get(user);
					attributeSet = entry1.getValue();
					for(Entry<String,Integer> entry2 : attributeSet.entrySet()) {
						key = entry2.getKey();
						value = userAttributes.getOrDefault(key, (float) 0) + (float) entry2.getValue();
						userAttributes.put(key, value);
					}
				}
				break;
			case "mean":
				for(Entry<String,HashMap<String,Integer>> entry1 : counterHits.entrySet()) {
					user = entry1.getKey();
					if(!ha.containsKey(user)) {ha.put(user, new HashMap<String, Float>()); } 
					userAttributes = ha.get(user);
					attributeSet = entry1.getValue();
					
					directHitsTotal = counter.getDirectHits().get(user).size();
					
					for(Entry<String,Integer> entry2 : attributeSet.entrySet()) {
						key = entry2.getKey();
						value = userAttributes.getOrDefault(key, (float) 0) + ( ((float) entry2.getValue()) / directHitsTotal);
						userAttributes.put(key, value);
					}	
				}
				break;
			default:
				for(Entry<String,HashMap<String,Integer>> entry1 : counterHits.entrySet()) {
					user = entry1.getKey();
					if(!ha.containsKey(user)) {ha.put(user, new HashMap<String, Float>()); } 
					userAttributes = ha.get(user);
					attributeSet = entry1.getValue();
					for(Entry<String,Integer> entry2 : attributeSet.entrySet()) {
						key = entry2.getKey();
						value = userAttributes.getOrDefault(key, 0f) + ((float) entry2.getValue());
						userAttributes.put(key, value);
					}
				}
				return ha;
			}
		}
		return ha;
	}

}
