package br.ufsc.lapesd.orbs.example.ucfclassic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
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

import br.ufsc.lapesd.orbs.tokit.Algorithm;
import br.ufsc.lapesd.orbs.tokit.AlgorithmParams;
import br.ufsc.lapesd.orbs.tokit.ItemScore;
import br.ufsc.lapesd.orbs.tokit.Model;
import br.ufsc.lapesd.orbs.tokit.PredictedResult;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.Query;

public class UCFClassicAlgorithm extends Algorithm {
	private DataModel mahoutModel;
	private RecommenderBuilder builder;
	private Recommender recommender;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	private boolean useTestSeed;
	private int neighborhoodSize;
	private boolean nativeEvaluatorEnabled;

	public UCFClassicAlgorithm(AlgorithmParams algorithmParams) {
		this.useTestSeed = algorithmParams.useTestSeed();
		this.neighborhoodSize = algorithmParams.getNeighborhoodSize();
		this.nativeEvaluatorEnabled = algorithmParams.isNativeEvaluatorEnabled();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {
			if(this.useTestSeed) { RandomUtils.useTestSeed(); } // do not randomize the evaluation result
			
			File file = ((UCFClassicPreparedData) preparedData).getFile();
			this.mahoutModel = new FileDataModel(file);
			this.similarity = new PearsonCorrelationSimilarity(this.mahoutModel);
			this.neighborhood = new NearestNUserNeighborhood (this.neighborhoodSize, this.similarity, this.mahoutModel);                
			
			this.builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, -10.d, similarity, model);       
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
	        
			//analyse(userId, itemModel, 3);

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
		RecommenderEvaluator evaluator;
		
		// RMSE
		//evaluator = new RMSRecommenderEvaluator();
	    //double evaluation_rmse = evaluator.evaluate(builder, null, model, 0.7, 1.0);
	    //System.out.println("RMSE: " +evaluation_rmse+"\n");
	    
		// MAE
		evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
	    double evaluation_mae = evaluator.evaluate(builder, null, model, 0.7, 1.0);
	    System.out.println("MAE: " +evaluation_mae+"\n");
	}

	private void analyse(long userId, DataModel model, int closestNeighborhoodSize) throws TasteException {    
        System.out.println("#### ANALYSING USER <"+userId+"> ####");

        // Items from userId's interactions
        FastIDSet itemsFromUser = model.getItemIDsFromUser(userId);
        System.out.println("|| has interactions with "+itemsFromUser.size()+" items:");
		System.out.print("...");
		for(long itemId : itemsFromUser) {
    		System.out.print(" "+itemId+"<"+model.getPreferenceValue(userId, itemId)+">,");
    	}
		System.out.println();
		
        // Neighborhood of userId
        long[] theNeighborhood = neighborhood.getUserNeighborhood(userId);
        System.out.println("|| is similar to "+theNeighborhood.length+" users:");
		System.out.print("...");
		for(long userSimilares: theNeighborhood){
	    	System.out.print(" "+userSimilares+",");
	    }
		System.out.println();			

	    // Items from userId's closest neighborhood
		long[] mostSimilarUserIds = ((GenericUserBasedRecommender) recommender).mostSimilarUserIDs(userId, closestNeighborhoodSize);
    	for(long simUserId : mostSimilarUserIds) {
            System.out.println("|| similarity degree with "+simUserId+": "+similarity.userSimilarity(userId, simUserId));
            itemsFromUser = model.getItemIDsFromUser(simUserId);
            System.out.println("... User <"+simUserId+"> has interactions with "+ itemsFromUser.size() +" items: ");
    		System.out.print("...");
    		for(long itemId : itemsFromUser) {
        		System.out.print(" "+itemId+"<"+model.getPreferenceValue(simUserId, itemId)+">,");
        	}
    		System.out.println();		
    	}
		System.out.println();			
	}
}
