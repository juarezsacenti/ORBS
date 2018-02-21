package br.ufsc.lapesd.sro.tokit;

import java.util.List;

public class Serving {

	public PredictedResult serve(Query q, List<PredictedResult> predictions) {
		PredictedResult finalAnwser = null;
		
		if(predictions.size() > 0) {
			finalAnwser = predictions.get(0); // head or first
			System.out.println("User " + q.getUserEntityId() + " should see also: "+ finalAnwser.toString());
		} else {
			System.out.println("User " + q.getUserEntityId() + " has no predictions.");
		}
		
		return finalAnwser; 
	}

}