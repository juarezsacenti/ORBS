package br.ufsc.lapesd.orbs.example.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.common.RandomUtils;

import com.google.common.base.Preconditions;

import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicModel;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledSymmetricSimilarity;
import br.ufsc.lapesd.orbs.tokit.Algorithm;
import br.ufsc.lapesd.orbs.tokit.AlgorithmParams;
import br.ufsc.lapesd.orbs.tokit.ItemScore;
import br.ufsc.lapesd.orbs.tokit.Model;
import br.ufsc.lapesd.orbs.tokit.PredictedResult;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.Query;

/*
 *  A classe MANeighborhoodAnalyzerAlgorithm é responsável pelo algoritmo de análise de 
 *  concordância entre um usuário e seus usuários vizinhos utilizando funções como 
 *  cohen.kappa, fleiss.kappa, tau de kendall, scott.pi e krippendorff.alpha.
 *  Esta classe produz as medidas de valor médio de concordância do dataset e
 *  valor de concordância por usuário.
 * */
public class MANeighborhoodAnalyzerAlgorithm extends Algorithm {
	private DataModel itemModel;
	private NearestNUserNeighborhood neighborhood;
	private EnsembledSymmetricSimilarity similarity;
	private int neighborhoodSize;
	public static List<File> FoIMatrixesFiles;
	
	public MANeighborhoodAnalyzerAlgorithm(AlgorithmParams algorithmParams) {
		this.neighborhoodSize = algorithmParams.getNeighborhoodSize();
	}

	@Override
	public Model train(PreparedData preparedData) {
		PearsonCorrelationSimilarity s;
		Long userID1, userID2;
		double value, weight = 1;
				
		Model model = null;
		try {			
			RandomUtils.useTestSeed(); // to not randomize the evaluation result
			
			File itemModelFile = ((br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparedData) preparedData).getItemModelFile();
			FoIMatrixesFiles = ((br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparedData) preparedData).getFoIMatrixesFiles();
			this.itemModel = new FileDataModel(itemModelFile);
			
			LongPrimitiveIterator users2, users1;
			this.similarity = new EnsembledSymmetricSimilarity(itemModel.getNumUsers());

			FileDataModel attributeModel;
			// Similarity summarization
			//System.out.println("Files: "+FoIMatrixesFiles);
			for(File FoIMatrixFile : FoIMatrixesFiles) {
				//System.out.println(FoIMatrixFile.getName());
				attributeModel = new FileDataModel(FoIMatrixFile);
				s = new PearsonCorrelationSimilarity(attributeModel);
				
				users1 = itemModel.getUserIDs();
				while(users1.hasNext()) {
					userID1 = users1.next();
					users2 = this.itemModel.getUserIDs();
					while(users2.hasNext()) {
						userID2 = users2.next();
						//if(userID1%500==0) System.out.println("SUM: "+userID1+", "+userID2);
						value = this.similarity.userSimilarity(userID1, userID2) + (weight * s.userSimilarity(userID1, userID2));
						this.similarity.setUserSimilarity(userID1, userID2, value);
					}
				}
			}
			
			// Similarity mean division
			users1 = itemModel.getUserIDs();
			while(users1.hasNext()) {
				userID1 = users1.next();
				users2 = this.itemModel.getUserIDs();
				while(users2.hasNext()) {
					userID2 = users2.next();				
					//if(userID1%500==0) System.out.println("DIV: "+userID1+", "+userID2);
					value = (this.similarity.userSimilarity(userID1, userID2) / (double) FoIMatrixesFiles.size());
					this.similarity.setUserSimilarity(userID1, userID2, value);
				}
			}
			this.similarity.save();
			
			this.neighborhood = new NearestNUserNeighborhood(neighborhoodSize, this.similarity, itemModel);                

			
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
			DataModel itemModel = ufcModel.getModel();
			
			List<RecommendedItem> recommendedItens = this.recommend(itemModel, userId, query.getNumber(), includeKnownItems);
			for(RecommendedItem item : recommendedItens) {
		    	is = new ItemScore(""+item.getItemID(), item.getValue());
				itemScores.add(is);
			}

			analyse(userId, itemModel);
			
			result = new PredictedResult(itemScores);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
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
		String outputFilePath = "src/resources/main/example/analysis/MANeighborhoodAnalysis.csv";
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
	
	private void analyseAttributes(long userId, DataModel model) throws IOException, TasteException {    
		System.out.println("#### ANALYSING USER <"+userId+"> ####");
        
		// User's number of interacted items
        FastIDSet itemsFromUser = model.getItemIDsFromUser(userId);
        System.out.println("... || has interactions with "+itemsFromUser.size()+" items.");

		// User's Neighborhood Size
		long[] theNeighborhood = neighborhood.getUserNeighborhood(userId);
		System.out.println("... || is similar to "+theNeighborhood.length+" users.");
		
        // User's Neighborhood PUs		
		String outputFilePath = "src/resources/main/example/analysis/MANeighborhoodAnalysis.csv";
		System.out.println("... || saving on file '"+outputFilePath+"'.");					
		File outputFile = new File(outputFilePath);
		//outputFile.createNewFile();
				
		try {
			OutputStream os = new FileOutputStream(outputFile, false);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			BufferedWriter out = new BufferedWriter(writer);

			// HEADER		
			out.append("Attr/User");
			out.append(", "+userId);
			for(long neighbor: theNeighborhood){
				out.append(", "+neighbor);
			}
			out.newLine();
			
			
			// DATA
			Float value;
			FileDataModel attributeModel;
			for(File FoIMatrixFile : FoIMatrixesFiles) {	        
				attributeModel = new FileDataModel(FoIMatrixFile);
				FastIDSet attributesFromUser = attributeModel.getItemIDsFromUser(userId);
				for(long attrId : attributesFromUser) {
					// Attribute's name
					out.append(FoIMatrixFile.getName()+"_"+attrId);
					
					// User's rating
					value = attributeModel.getPreferenceValue(userId, attrId);
		    		if(value == null) {
		    			out.append(", ");
		    		} else {
		    			out.append(", "+value);
		    		}
		    		
		    		// Neighbors' ratings
		    		for(long neighbor: theNeighborhood){
						value = attributeModel.getPreferenceValue(neighbor, attrId);
			    		if(value == null) {
			    			out.append(", ");
			    		} else {
			    			out.append(", "+value);
			    		}
			    	}
					out.newLine();
		    	}
			}
			
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("####       END OF ANALYSIS       ####");			
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

	@Override
	public void nativeEvaluation() {
		// TODO Auto-generated method stub
		
	}
}
