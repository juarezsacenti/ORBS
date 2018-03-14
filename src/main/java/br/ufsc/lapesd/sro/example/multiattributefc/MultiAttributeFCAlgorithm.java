package br.ufsc.lapesd.sro.example.multiattributefc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

import com.google.common.base.Preconditions;

import br.ufsc.lapesd.sro.example.userfc.UserFCModel;
import br.ufsc.lapesd.sro.tokit.Algorithm;
import br.ufsc.lapesd.sro.tokit.AlgorithmParams;
import br.ufsc.lapesd.sro.tokit.ItemScore;
import br.ufsc.lapesd.sro.tokit.Model;
import br.ufsc.lapesd.sro.tokit.PredictedResult;
import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.Query;

/*
 * A vizinhança e similaridade entre usuários é calculada a partir do modelo de gênero.
 * A recomendação utiliza o modelo de itens para obter os itens pertencentes a vizinhança e não conhecidos pelo usuário consultante.
 * Corrigido o cálculo de estimativa do ranking do item para o usuário consultante:
 *  - é realizado via média de atributos do item ponderado pelo perfil de atributos do usuário consultante;
 *  - mais média de avaliações quando houver ponderada pela similaridade da vizinhança com o consultante
 * */
public class MultiAttributeFCAlgorithm extends Algorithm {
	private Recommender recommender;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	private boolean nativeEvaluatorEnabled;
	private int neighborhoodSize;
	public static FileDataModel attributeModel;
	
	public MultiAttributeFCAlgorithm(AlgorithmParams algorithmParams) {
		this.neighborhoodSize = algorithmParams.getNeighborhoodSize();
		this.nativeEvaluatorEnabled = algorithmParams.isNativeEvaluatorEnabled();
	}

	@Override
	public Model train(PreparedData preparedData) {
    	Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File itemModelFile = ((MultiAttributeFCPreparedData) preparedData).getItemModelFile();
			File genreModelFile = ((MultiAttributeFCPreparedData) preparedData).getGenreModelFile();
			DataModel itemModel = new FileDataModel(itemModelFile);
			attributeModel = new FileDataModel(genreModelFile);
			this.similarity = new PearsonCorrelationSimilarity(attributeModel);
			this.neighborhood = new NearestNUserNeighborhood (neighborhoodSize, similarity, attributeModel);                

			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					UserSimilarity similarity = new PearsonCorrelationSimilarity(attributeModel);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood (neighborhoodSize, similarity, attributeModel);                
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
	        }
			};
			
			this.recommender = builder.buildRecommender(itemModel);

			if(nativeEvaluatorEnabled) {nativeEvaluator(builder, itemModel);}
			
		    model = new UserFCModel(itemModel);
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
			
			List<RecommendedItem> recommendedItens = this.recommend(itemModel, userId, query.getNumber(), includeKnownItems);
			for(RecommendedItem item : recommendedItens) {
		    	is = new ItemScore(""+item.getItemID(), item.getValue());
				itemScores.add(is);
			}

			analyse(userId, itemModel, 3);
			
			result = new PredictedResult(itemScores);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	private void nativeEvaluator(RecommenderBuilder builder, DataModel model) throws TasteException {    
		RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
	    double evaluetion_rmse = evaluator.evaluate(builder, null, model, 0.7, 1.0);
	    System.out.println("RMSE: " +evaluetion_rmse+"\n");	
	}
	
	private void analyse(long userId, DataModel model, int closestNeighborhoodSize) throws TasteException {    
        System.out.println("#### ANALYSING USER <"+userId+"> ####");

        // Attributes of userId's items
        FastIDSet attributesFromUser = attributeModel.getItemIDsFromUser(userId);
        System.out.println("|| has items with attribute<percent>:");
		System.out.print("...");
		for(long attrId : attributesFromUser) {
    		System.out.print(" "+attrId+"<"+attributeModel.getPreferenceValue(userId, attrId)+">,");
    	}
		System.out.println();
        
        // Items from userId's interactions
        FastIDSet itemsFromUser = model.getItemIDsFromUser(userId);
        System.out.println("|| has interactions with "+itemsFromUser.size()+" 'items<rating>':");
		System.out.print("...");
		//for(long itemId : itemsFromUser) {
    	//	System.out.print(" "+itemId+"<"+model.getPreferenceValue(userId, itemId)+">,");
    	//}
		System.out.println();
		
        // Neighborhood of userId
        long[] theNeighborhood = neighborhood.getUserNeighborhood(userId);
        System.out.println("|| is similar to "+theNeighborhood.length+" users:");
		System.out.print("...");
		//for(long userSimilares: theNeighborhood){
	    //	System.out.print(" "+userSimilares+",");
	    //}
		System.out.println();			

	    // Items from userId's closest neighborhood
		long[] mostSimilarUserIds = ((GenericUserBasedRecommender) recommender).mostSimilarUserIDs(userId, closestNeighborhoodSize);
    	for(long simUserId : mostSimilarUserIds) {
            System.out.println("|| similarity degree with "+simUserId+": "+similarity.userSimilarity(userId, simUserId));
            itemsFromUser = model.getItemIDsFromUser(simUserId);
            System.out.println("... User <"+simUserId+"> has interactions with "+ itemsFromUser.size() +" items: ");
    		//System.out.print("...");
    		//for(long itemId : itemsFromUser) {
        	//	System.out.print(" "+itemId+"<"+model.getPreferenceValue(simUserId, itemId)+">,");
        	//}
    		//System.out.println();

    		attributesFromUser = attributeModel.getItemIDsFromUser(simUserId);
            System.out.println("|| has items with attribute<percent>:");
    		System.out.print("...");
    		for(long attrId : attributesFromUser) {
        		System.out.print(" "+attrId+"<"+attributeModel.getPreferenceValue(simUserId, attrId)+">,");
        	}
    		System.out.println();
    	}
		System.out.println();			
	}
	
	private List<RecommendedItem> recommend(DataModel dataModel, long userID, int howMany, boolean includeKnownItems) 
	throws TasteException {
	    Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");

	    long[] theNeighborhood = neighborhood.getUserNeighborhood(userID);

	    if (theNeighborhood.length == 0) {
	      return Collections.emptyList();
	    }

	    FastIDSet allItemIDs = getAllOtherItems(dataModel, theNeighborhood, userID, includeKnownItems);

	    TopItems.Estimator<Long> estimator = new MultiAttributeFCEstimator(dataModel, userID, theNeighborhood);

	    List<RecommendedItem> topItems = TopItems
	        .getTopItems(howMany, allItemIDs.iterator(), null, estimator);
	    
	    return topItems;
	}

	private FastIDSet getAllOtherItems(DataModel dataModel, long[] theNeighborhood, long theUserID, boolean includeKnownItems)
	throws TasteException {
		FastIDSet possibleItemIDs = new FastIDSet();
	    for (long userID : theNeighborhood) {
	      possibleItemIDs.addAll(dataModel.getItemIDsFromUser(userID));
	    }
	    if (!includeKnownItems) {
	      possibleItemIDs.removeAll(dataModel.getItemIDsFromUser(theUserID));
	    }
	    return possibleItemIDs;
	}

	private float doEstimatePreference(DataModel dataModel, long theUserID, long[] theNeighborhood, long itemID) throws TasteException {
	    if (theNeighborhood.length == 0) {
	      return Float.NaN;
	    }
	    double preference = 0.0;
	    double totalSimilarity = 0.0;
	    int count = 0;
	    for (long userID : theNeighborhood) {
	      if (userID != theUserID) {
	        // See GenericItemBasedRecommender.doEstimatePreference() too
	        Float pref = dataModel.getPreferenceValue(userID, itemID);
	        if (pref != null) {
	          double theSimilarity = similarity.userSimilarity(theUserID, userID);
	          if (!Double.isNaN(theSimilarity)) {
	            preference += theSimilarity * pref;
	            totalSimilarity += theSimilarity;
	            count++;
	          }
	        }
	      }
	    }
	    // Throw out the estimate if it was based on no data points, of course, but also if based on
	    // just one. This is a bit of a band-aid on the 'stock' item-based algorithm for the moment.
	    // The reason is that in this case the estimate is, simply, the user's rating for one item
	    // that happened to have a defined similarity. The similarity score doesn't matter, and that
	    // seems like a bad situation.
	    if (count <= 1) {
	      return Float.NaN;
	    }
	    float estimate = (float) (preference / totalSimilarity);
	    return estimate;
	  }
	
	private final class MultiAttributeFCEstimator implements TopItems.Estimator<Long> {
   	    private final long theUserID;
	    private final long[] theNeighborhood;
	    private final DataModel dataModel;
	    
	    MultiAttributeFCEstimator(DataModel dataModel, long theUserID, long[] theNeighborhood) {
	      this.theUserID = theUserID;
	      this.theNeighborhood = theNeighborhood;
	      this.dataModel = dataModel;
	    }
	    
	    @Override
	    public double estimate(Long itemID) throws TasteException {
	      return doEstimatePreference(dataModel, theUserID, theNeighborhood, itemID);
	    }
	}
}
