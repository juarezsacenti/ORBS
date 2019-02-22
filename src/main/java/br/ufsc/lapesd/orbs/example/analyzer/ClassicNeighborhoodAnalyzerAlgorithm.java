package br.ufsc.lapesd.orbs.example.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicModel;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparedData;
import br.ufsc.lapesd.orbs.tokit.Algorithm;
import br.ufsc.lapesd.orbs.tokit.AlgorithmParams;
import br.ufsc.lapesd.orbs.tokit.ItemScore;
import br.ufsc.lapesd.orbs.tokit.Model;
import br.ufsc.lapesd.orbs.tokit.PredictedResult;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.Query;

public class ClassicNeighborhoodAnalyzerAlgorithm extends Algorithm {
	private DataModel mahoutModel;
	private RecommenderBuilder builder;
	private Recommender recommender;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	private int neighborhoodSize;
	private boolean nativeEvaluatorEnabled;

	public ClassicNeighborhoodAnalyzerAlgorithm(AlgorithmParams algorithmParams) {
		this.neighborhoodSize = algorithmParams.getNeighborhoodSize();
		this.nativeEvaluatorEnabled = algorithmParams.isNativeEvaluatorEnabled();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File file = ((UCFClassicPreparedData) preparedData).getFile();
			this.mahoutModel = new FileDataModel(file);
			this.similarity = new PearsonCorrelationSimilarity(this.mahoutModel);
			this.neighborhood = new NearestNUserNeighborhood (this.neighborhoodSize, this.similarity, this.mahoutModel);                
			
			this.builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood (neighborhoodSize, -10.d, similarity, model);       
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
				}
			};
			this.recommender = this.builder.buildRecommender(this.mahoutModel);

			if(nativeEvaluatorEnabled) {nativeEvaluator(builder, mahoutModel);}

		    model = new UCFClassicModel(mahoutModel);
		} catch (IOException e) {
    		System.out.println("There was an IO exception.");
			e.printStackTrace();
    	} catch (TasteException e) {
    		System.out.println("There was an Taste exception.");
			e.printStackTrace();
    	}    
		return model;
	}
	
	@Override
	public PredictedResult predict(Model model, Query query) {
		PredictedResult result = null;
		boolean includeKnownItems = true;
		long userId = Long.parseLong(query.getUserEntityId());

		List<ItemScore> itemScores = new ArrayList<ItemScore>();
		ItemScore is;
		try {
			if(model.getClass() != UCFClassicModel.class) {
				throw new Exception("Model class differs from UCFClassicModel.class");
			}
			UCFClassicModel ufcModel = (UCFClassicModel) model;
			DataModel itemModel = ufcModel.getModel();
			
		    List<RecommendedItem> recommendedItens = recommender.recommend(userId, query.getNumber(), includeKnownItems);
			for(RecommendedItem item : recommendedItens) {
		    	is = new ItemScore(""+item.getItemID(), item.getValue());
				itemScores.add(is);
			}
	        
			analyse(userId, itemModel);

    		result = new PredictedResult(itemScores);
		} catch (Exception e) { e.printStackTrace(); }
		return result;
	}
	
	@Override
	public void nativeEvaluation() {
		try {
			nativeEvaluator(this.builder, this.mahoutModel);
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}
	
	private void nativeEvaluator(RecommenderBuilder builder, DataModel model) throws TasteException {    
		RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
	    double evaluation_rmse = evaluator.evaluate(builder, null, model, 0.7, 1.0);

	    System.out.println("RMSE: " +evaluation_rmse+"\n");
	    
	    evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
	    double evaluation_mae = evaluator.evaluate(builder, null, model, 0.7, 1.0);

	    System.out.println("MAE: " +evaluation_mae+"\n");
	}
	
	private void analyse(long userId, DataModel model) throws IOException, TasteException {   
	    System.out.println("#### ANALYSING USER <"+userId+"> ####");	    
	    
		// User's number of interacted items
	    FastIDSet itemsFromUser = model.getItemIDsFromUser(userId);
	    System.out.println("... || has interactions with "+itemsFromUser.size()+" items.");
	
		// User's Neighborhood Size
		long[] theNeighborhood = neighborhood.getUserNeighborhood(userId);
		System.out.println("... || is similar to "+theNeighborhood.length+" users.");

		// User's Neighborhood PUs		
		String outputFilePath = "src/resources/main/example/analysis/classicNeighborhoodAnalysis.csv";
		System.out.println("... || saving on file '"+outputFilePath+"'.");					
		File outputFile = new File(outputFilePath);
		//outputFile.createNewFile();
				
		try {
			OutputStream os = new FileOutputStream(outputFile, false);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			BufferedWriter out = new BufferedWriter(writer);

			// HEADER
			out.append("Item/User");
			out.append(", "+userId);
			for(long neighbor: theNeighborhood){
				out.append(", "+neighbor);
			}
			out.newLine();
			
			// DATA
			Float value;
			for(long itemId : itemsFromUser) {
				// ItemId
    			out.append("i"+itemId);    			

				// User's rating
				value = model.getPreferenceValue(userId, itemId);
	    		if(value == null) {
	    			out.append(", ");
	    		} else {
	    			out.append(", "+value);
	    		}

	    		
	    		// Neighbors' ratings
	    		for(long neighbor: theNeighborhood){
					value = model.getPreferenceValue(neighbor, itemId);
		    		if(value == null) {
		    			out.append(", ");
		    		} else {
		    			out.append(", "+value);
		    		}
		    	}
				out.newLine();
	    	}
			
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("####       END OF ANALYSIS       ####");			
	}
}
