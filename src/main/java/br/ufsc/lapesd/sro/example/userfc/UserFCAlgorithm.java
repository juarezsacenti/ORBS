package br.ufsc.lapesd.sro.example.userfc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
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

import br.ufsc.lapesd.sro.tokit.Algorithm;
import br.ufsc.lapesd.sro.tokit.AlgorithmParams;
import br.ufsc.lapesd.sro.tokit.ItemScore;
import br.ufsc.lapesd.sro.tokit.Model;
import br.ufsc.lapesd.sro.tokit.PredictedResult;
import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.Query;

public class UserFCAlgorithm extends Algorithm {
	private Recommender recommender;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	private boolean isNativeEvaluatorEnabled;
	private int neighborhoodSize;

	public UserFCAlgorithm(AlgorithmParams algorithmParams) {
		this.isNativeEvaluatorEnabled = algorithmParams.isNativeEvaluatorEnabled();
		this.neighborhoodSize = algorithmParams.getNeighborhoodSize();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File file = ((UserFCPreparedData) preparedData).getFile();
			DataModel mahoutModel = new FileDataModel(file);
			this.similarity = new PearsonCorrelationSimilarity(mahoutModel);
			this.neighborhood = new NearestNUserNeighborhood (neighborhoodSize, similarity, mahoutModel);                
			
			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood (neighborhoodSize, -10.d, similarity, model);         
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
				}
			};
			this.recommender = builder.buildRecommender(mahoutModel);

			if(isNativeEvaluatorEnabled) {nativeEvaluator(builder, mahoutModel);}

		    model = new UserFCModel(mahoutModel);
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
			if(model.getClass() != UserFCModel.class) {
				throw new Exception("Model class differs from UserFCModel.class");
			}
			UserFCModel ufcModel = (UserFCModel) model;
			DataModel itemModel = ufcModel.getModel();
			
		    List<RecommendedItem> recommendedItens = recommender.recommend(userId, query.getNumber(), includeKnownItems);
			for(RecommendedItem item : recommendedItens) {
		    	is = new ItemScore(""+item.getItemID(), item.getValue());
				itemScores.add(is);
			}
	        
			analyse(userId, itemModel, 3);

    		result = new PredictedResult(itemScores);
		} catch (Exception e) { e.printStackTrace(); }
		return result;
	}

	private void nativeEvaluator(RecommenderBuilder builder, DataModel model) throws TasteException {    
		RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
	    double evaluetion_rmse = evaluator.evaluate(builder, null, model, 0.7, 1.0);
	    System.out.println("RMSE: "+evaluetion_rmse+"\n");	
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
