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
	private double threshold;
	private RecommenderBuilder builder;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;

	public UserFCAlgorithm(AlgorithmParams algorithmParams) {
		// TODO Auto-generated constructor stub
		this.threshold = algorithmParams.getLambda();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File file = ((UserFCPreparedData) preparedData).getFile();
			DataModel mahoutModel = new FileDataModel(file);
			this.similarity = new PearsonCorrelationSimilarity(mahoutModel);
			this.neighborhood = new NearestNUserNeighborhood (100, similarity, mahoutModel);                
			
			this.builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {   
					UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood (100, similarity, model);         
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
				}
			};
		    
			RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
		    double evaluetion_rmse = evaluator.evaluate(builder, null, mahoutModel, threshold, 1.0);
		    System.out.println("RMSE: " + evaluetion_rmse);	
		
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
			Recommender recommender = this.builder.buildRecommender(itemModel);
			
		    System.out.println("\n\nRecommended items to user <"+userId+">: ");
		    List<RecommendedItem> recommendedItens = recommender.recommend(userId, query.getNumber(), includeKnownItems);
			for(RecommendedItem item : recommendedItens) {
		    	System.out.println(item);
		    	is = new ItemScore(""+item.getItemID(), item.getValue());
				itemScores.add(is);
			}
	           
			long[] theNeighborhood = neighborhood.getUserNeighborhood(userId);
	        System.out.print("\nUser <"+userId+"> is similar to "+theNeighborhood.length+" users: ");
		    for(long userSimilares: theNeighborhood){
		    	System.out.print(userSimilares+", ");
		    }

			long[] mostSimilarUserIDs = ((GenericUserBasedRecommender) recommender).mostSimilarUserIDs(userId, 3);
        	for(long recID:mostSimilarUserIDs) {
                System.out.println("\nThe similarity between user <"+userId+"> and <"+recID+"> is: "+similarity.userSimilarity(userId, recID));
                FastIDSet itemsFromUser = itemModel.getItemIDsFromUser(recID);
                System.out.println("User <"+recID+"> has interactions with "+ itemsFromUser.size() +" items: ");
            	for(long item:itemsFromUser) {
            		System.out.print(" "+item);
            	}
            }
    		System.out.println();			

    		result = new PredictedResult(itemScores);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
