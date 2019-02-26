package br.ufsc.lapesd.orbs.example.ucfmultiattribute;

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
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicModel;
import br.ufsc.lapesd.orbs.tokit.Algorithm;
import br.ufsc.lapesd.orbs.tokit.AlgorithmParams;
import br.ufsc.lapesd.orbs.tokit.ItemScore;
import br.ufsc.lapesd.orbs.tokit.Model;
import br.ufsc.lapesd.orbs.tokit.PredictedResult;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.Query;

/*
 * A vizinhança e similaridade entre usuários é calculada a partir do modelo de gênero.
 * A recomendação utiliza o modelo de itens para obter os itens pertencentes a vizinhança e não conhecidos pelo usuário consultante.
 * A estimativa do ranking do item para o usuário consultante é realizada via média da preferência ponderada pela similaridade da vizinhança com o consultante.
 * */
public class WebMediaProposalAlgorithm extends Algorithm {
	private double threshold;
	private RecommenderBuilder builder;
	private NearestNUserNeighborhood neighborhood;
	private PearsonCorrelationSimilarity similarity;
	private FileDataModel genreModel;
	private FileDataModel itemModel;
	
	public WebMediaProposalAlgorithm(AlgorithmParams algorithmParams) {
		// TODO Auto-generated constructor stub
		this.threshold = algorithmParams.getLambda();
	}

	@Override
	public Model train(PreparedData preparedData) {
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to randomize the evaluation result
			
			File itemModelFile = ((UCFMultiAttributePreparedData) preparedData).getItemModelFile();
			File genreModelFile = ((UCFMultiAttributePreparedData) preparedData).getFoIMatrixesFiles().get(0);
			this.itemModel = new FileDataModel(itemModelFile);
			this.genreModel = new FileDataModel(genreModelFile);
			this.similarity = new PearsonCorrelationSimilarity(genreModel);
			this.neighborhood = new NearestNUserNeighborhood (100, similarity, genreModel);                
			
			this.builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) throws TasteException {
					FileDataModel genreModel;
					UserSimilarity similarity = null;
					UserNeighborhood neighborhood = null;                
					try {
						genreModel = new FileDataModel(genreModelFile);
						similarity = new PearsonCorrelationSimilarity(genreModel);
						neighborhood = new NearestNUserNeighborhood (100, similarity, genreModel);                
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return new GenericUserBasedRecommender(model, neighborhood, similarity);                
	        }
			};
		    
			RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
		    double evaluetion_rmse = evaluator.evaluate(builder, null, itemModel, threshold, 1.0);
		    System.out.println("RMSE: " + evaluetion_rmse);	
		
		    model = new UCFClassicModel(itemModel);
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
			Recommender recommender = this.builder.buildRecommender(ufcModel.getModel());
			
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

	@Override
	public void nativeEvaluation() {
		// TODO Auto-generated method stub
		
	}

}
