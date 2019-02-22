package br.ufsc.lapesd.orbs.example.crossvalidation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufsc.lapesd.orbs.tokit.ItemScore;
import br.ufsc.lapesd.orbs.tokit.PredictedResult;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.Serving;

public class ServingToFile extends Serving {

	private String engineName;
	private String outFolder = "src/resources/main/crossValid/ml-1m/recommendations/";
	
	public ServingToFile(String engineName) {
		super();
		this.engineName = engineName;
	}

	@Override
	public PredictedResult serve(Query q, List<PredictedResult> predictions) {
		PredictedResult finalAnswer = null;
		String outPath= outFolder+"recs_"+ engineName.charAt(engineName.length()-1) +".csv";
		
		//System.out.println("Querying user " + q.getUserEntityId() + ".");
		if(predictions.size() > 0) {
			finalAnswer = predictions.get(0); // head or first
						
			try {
				OutputStream os = new FileOutputStream(outPath, true);
				Writer writer = new OutputStreamWriter(os, "UTF-8");
				
				List<ItemScore> list = finalAnswer.getItemScores();
				for(ItemScore is: list) {
						writer.write(""+ q.getUserEntityId() +"	"+is.getItemEntityId()+"	"+is.getScore()+"\n");
		    	}
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return finalAnswer; 
	}
}
