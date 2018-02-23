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
	private double threshold;
	private RecommenderBuilder builder;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	public static FileDataModel genreModel;
	
	public MultiAttributeFCAlgorithm(AlgorithmParams algorithmParams) {
		// TODO Auto-generated constructor stub
		this.threshold = algorithmParams.getLambda();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File itemModelFile = ((MultiAttributeFCPreparedData) preparedData).getItemModelFile();
			File genreModelFile = ((MultiAttributeFCPreparedData) preparedData).getGenreModelFile();
			DataModel itemModel = new FileDataModel(itemModelFile);
			genreModel = new FileDataModel(genreModelFile);
			this.similarity = new PearsonCorrelationSimilarity(genreModel);
			this.neighborhood = new NearestNUserNeighborhood (100, similarity, genreModel);                

			this.builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					UserSimilarity similarity = new PearsonCorrelationSimilarity(genreModel);
					UserNeighborhood neighborhood = new NearestNUserNeighborhood (100, similarity, genreModel);                
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
	        }
			};
		    
			RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
		    double evaluetion_rmse = evaluator.evaluate(builder, null, itemModel, threshold, 1.0);
		    System.out.println("RMSE: " + evaluetion_rmse);	
		
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
			Recommender recommender = this.builder.buildRecommender(itemModel);
			
		    System.out.println("\n\nRecommended items to user <"+userId+">: ");
			List<RecommendedItem> recommendedItens = this.recommend(itemModel, userId, query.getNumber(), includeKnownItems);
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
