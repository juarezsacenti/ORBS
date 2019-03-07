package br.ufsc.lapesd.orbs.example.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparator;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparedData;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledSymmetricSimilarity;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparator;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparedData;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;

/*
 *  A classe SimilaryLevelAnalyzer é responsável pelo algoritmo de análise do
 *  erro médio entre a similaridade entre usuários calculada via matriz de avaliação 
 *  e a similaridade obtida por matriz de preferência.
 * */
public class SimilarityLevelAnalyzer {

    /**
     * Default bias threshold.
     */
    public static final int TH_BIAS = 2;

    /**
     * Default min similarity 1 threshold.
     */
    public static final double TH_MIN_SIM_1 = -1;
    
    /**
     * Default max similarity 1 threshold.
     */
    public static final double TH_MAX_SIM_1 = 1;
    
    /**
     * Default min similarity 2 threshold.
     */
    public static final double TH_MIN_SIM_2 = -1;
    
    /**
     * Default max similarity 2 threshold.
     */
    public static final double TH_MAX_SIM_2 = 1;
    
	private final static String[] engines = {
		"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-25.json",                // 0
		"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-100.json",               // 1
		"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-25.json",   // 2
		"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-100.json",  // 3
		"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-25.json",         // 4
		"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-100.json",        // 5
		"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-25.json",          // 6
		"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-100.json",         // 7
		"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-25.json",     // 8
		"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-100.json"	}; // 9
	
	public static void main(String[] args) {
		int thBias = TH_BIAS;
		double thMinSim1 = TH_MIN_SIM_1;
		double thMaxSim1 = TH_MAX_SIM_1;
		double thMinSim2 = TH_MIN_SIM_2;
		double thMaxSim2 = TH_MAX_SIM_2;
		
		int length = args.length;
		for(int i = 0; i < length; ++i) {
			switch (args[i]) {
			case "-b":
				if(++i < length) { thBias = Integer.parseInt(args[i]); }
				break;
			
			case "-minsim1":
				if(++i < length) { thMinSim1 = Double.parseDouble(args[i]); }
				break;
				
			case "-maxsim1":
				if(++i < length) { thMaxSim1 = Double.parseDouble(args[i]); }
				break;
				
			case "-minsim2":
				if(++i < length) { thMinSim2 = Double.parseDouble(args[i]); }
				break;
				
			case "-maxsim2":
				if(++i < length) { thMaxSim2 = Double.parseDouble(args[i]); }
				break;
				
			default:
				break;
			}
		}
		
		String enginePath1 = engines[1];
		String enginePath2 = engines[3];
		
		System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
    	EngineParameter eparams1 = new EngineParameter(enginePath1);
    	EngineParameter eparams2 = new EngineParameter(enginePath2);

		LongPrimitiveIterator users1, users2;
		Long userID1, userID2;
		
		try {
			DataSource datasource = new DataSource(eparams1.getDataSouceParams());
			TrainingData trainingData1 = datasource.readTraining();
			UCFClassicPreparator preparator = new UCFClassicPreparator();
			PreparedData preparedData1 = preparator.prepare(trainingData1);
			File file1 = ((UCFClassicPreparedData) preparedData1).getFile();
			DataModel ratingMatrix = new FileDataModel(file1);

			String path = "src/resources/main/analyzer/ml-1m/numOfComumItems.csv";			
			if(!(new File(path)).exists()) {
				saveNumberOfComumItems(ratingMatrix, path);
			}
			
			UserSimilarity similarityMatrix1 = classicSimilarity(eparams1, ratingMatrix);
			
			UserSimilarity similarityMatrix2 = preferenceSimilarity(eparams2, ratingMatrix);

			int numUsers = ratingMatrix.getNumUsers();
			int progress = 0, total = numUsers *(numUsers+1)/2;
			
			// Comparing similarities
			System.out.println("Comparing similarities");
			EnsembledSymmetricSimilarity numOfComumItems = new EnsembledSymmetricSimilarity(path);
			int count = 0;
			double sim1, sim2, mae = 0;
			users1 = ratingMatrix.getUserIDs();
			while(users1.hasNext()) {
				userID1 = users1.next();
				users2 = ratingMatrix.getUserIDs();
				while(users2.hasNext()) {
					userID2 = users2.next();
					if((long)userID1 < (long)userID2) { // IGNORING userID1 == userID2
						if(numOfComumItems.userSimilarity(userID1, userID2) >= thBias) { // FILTERING TH_BIAS
							sim1 = similarityMatrix1.userSimilarity(userID1, userID2); 
							sim2 = similarityMatrix2.userSimilarity(userID1, userID2);
							if((!Double.isNaN(sim1)) &&  (!Double.isNaN(sim2))) {
								if(sim1 >= thMinSim1 && sim1 <= thMaxSim1 
										&& sim2 >= thMinSim2 && sim2 <= thMaxSim2 ) // FILTERING TH_SIM
								// MAE
								mae += Math.abs(sim1 - sim2);
								count++;
							}
							if(progress+1 % 100000 == 0) {
								System.out.println(Math.abs(sim1 - sim2) + " ," + sim1 + " ," + sim2);
							}					
						}
						if(progress++ % 100000 == 0) {
							System.out.println(progress +" / "+ total);
						}					
					}
				}
			}
			mae = mae / (double) count;
			
			System.out.println("count: "+ count + " | mae: " + mae);
		} catch (IOException e) {
    		System.out.println("There was an IO exception.");
			e.printStackTrace();
    	} catch (TasteException e) {
    		System.out.println("There was an Taste exception.");
			e.printStackTrace();
    	}    

	}

    /**
     *  Classic Similarity
     */	
	private static UserSimilarity classicSimilarity(EngineParameter eparams, DataModel ratingMatrix) throws IOException, TasteException {
		PearsonCorrelationSimilarity similarityMatrix = new PearsonCorrelationSimilarity(ratingMatrix);
		
		return similarityMatrix;
	}
	
    /**
     * Preference Similarity
     */
	private static UserSimilarity preferenceSimilarity(EngineParameter eparams2, DataModel ratingMatrix) throws TasteException, IOException {
		DataSource datasource = new DataSource(eparams2.getDataSouceParams());
		TrainingData trainingData2 = datasource.readTraining();
		UCFMultiAttributePreparator preparator = new UCFMultiAttributePreparator(eparams2.getPreparatorParams());
		PreparedData preparedData2 = preparator.prepare(trainingData2);
		List<File> FoIMatrixesFiles = ((UCFMultiAttributePreparedData) preparedData2).getFoIMatrixesFiles();
		FileDataModel preferenceMatrix;
		PearsonCorrelationSimilarity s;
		int numUsers = ratingMatrix.getNumUsers();
		EnsembledSymmetricSimilarity similarityMatrix2 = new EnsembledSymmetricSimilarity(numUsers);
		// Preference similarity SUM
		System.out.println("Preference similarity SUM:");
		double value, weight = 1;
		int progress = 0, total = numUsers*(numUsers+1)/2;
		LongPrimitiveIterator users1;
		Long userID1;
		LongPrimitiveIterator users2;
		Long userID2;
		for(File FoIMatrixFile : FoIMatrixesFiles) {
			System.out.println(FoIMatrixFile.getName());
			preferenceMatrix = new FileDataModel(FoIMatrixFile);
			s = new PearsonCorrelationSimilarity(preferenceMatrix);
			
			users1 = ratingMatrix.getUserIDs();
			while(users1.hasNext()) {
				userID1 = users1.next();
				users2 = ratingMatrix.getUserIDs();
				while(users2.hasNext()) {
					userID2 = users2.next();
					if((long)userID1 <= (long)userID2) {
						if(progress++ % 100000 == 0) {System.out.println(progress +" / "+ total);}
						value = similarityMatrix2.userSimilarity(userID1, userID2) + (weight * s.userSimilarity(userID1, userID2));
						similarityMatrix2.setUserSimilarity(userID1, userID2, value);
					}
				}
			}
		}
		
		users1 = null;
		users2 = null;
		userID1 = null;
		userID2 = null;
		progress = 0;
		
		// Preference similarity DIV
		System.out.println("Preference similarity DIV:");
		users1 = ratingMatrix.getUserIDs();
		while(users1.hasNext()) {
			userID1 = users1.next();
			users2 = ratingMatrix.getUserIDs();
			while(users2.hasNext()) {
				userID2 = users2.next();				
				if((long)userID1 <= (long)userID2) {
					if(progress++ % 100000 == 0) {System.out.println(progress +" / "+ total);}
					value = (similarityMatrix2.userSimilarity(userID1, userID2) / (double) FoIMatrixesFiles.size());
					similarityMatrix2.setUserSimilarity(userID1, userID2, value);
				}
			}
		}		
		return similarityMatrix2;
	}

	private static int numberOfComumItems(Long u1, Long u2, DataModel ratingMatrix) throws TasteException {
		FastIDSet is1 = ratingMatrix.getItemIDsFromUser(u1);
		FastIDSet is2 = ratingMatrix.getItemIDsFromUser(u2);
		
		LongPrimitiveIterator itSmall, itBig; 
		HashSet<Long> itemHash = new HashSet<Long>();
		if(is1.size() <= is2.size()) {
			itSmall = ratingMatrix.getItemIDsFromUser(u1).iterator();
			itBig = ratingMatrix.getItemIDsFromUser(u2).iterator();
		} else {
			itSmall = ratingMatrix.getItemIDsFromUser(u2).iterator();
			itBig = ratingMatrix.getItemIDsFromUser(u1).iterator();			
		}
		
		is1 = null;
		is2 = null;
		
		while(itSmall.hasNext()) {
			itemHash.add(itSmall.next());
		}
		
		Long itemID;
		int count = 0;
		while(itBig.hasNext()) {
			itemID = itBig.next();
			if(itemHash.contains(itemID)) {
				count++;
			}
		}
		itemHash = null;
		
		return count;
	}
	
	private static void saveNumberOfComumItems(DataModel ratingMatrix, String path) throws TasteException {
		int numUsers = ratingMatrix.getNumUsers();
		EnsembledSymmetricSimilarity numOfComumItemsMatrix = new EnsembledSymmetricSimilarity(numUsers);

		LongPrimitiveIterator ui1, ui2;
		Long u1, u2;
		int res, progress = 0, total = numUsers*(numUsers+1)/2;
		ui1 = ratingMatrix.getUserIDs();
		while(ui1.hasNext()) {
			u1 = ui1.next();
			ui2 = ratingMatrix.getUserIDs();
			while(ui2.hasNext()) {
				u2 = ui2.next();
				if((long) u1 <= (long) u2) {
					res = numberOfComumItems(u1, u2, ratingMatrix);
					numOfComumItemsMatrix.setUserSimilarity(u1, u2, res);
					if(progress++ % 100000 == 0) {System.out.println(progress +" / "+ total);}
				}
			}
		}
		numOfComumItemsMatrix.save(path);
	}
}