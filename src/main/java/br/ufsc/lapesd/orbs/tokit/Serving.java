package br.ufsc.lapesd.orbs.tokit;

import java.util.List;

public class Serving {

	public PredictedResult serve(Query q, List<PredictedResult> predictions) {
		PredictedResult finalAnwser = null;
		
		if(predictions.size() > 0) {
			finalAnwser = predictions.get(0); // head or first
			System.out.println("User " + q.getUserEntityId() + " should see also "+q.getNumber()+" item<estimated rating>:");
			System.out.println(finalAnwser.toString());
		} else {
			System.out.println("User " + q.getUserEntityId() + " has no predictions.");
			System.out.println();
		}
		
		return finalAnwser; 
	}

}