package br.ufsc.lapesd.orbs.example.analyzer;

import java.io.File;
import java.io.IOException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparator;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparedData;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledSymmetricSimilarity;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Preparator;
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
    public static final int TH_BIAS = 5;
    	
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
		String enginePath1 = engines[1];
		String enginePath2 = engines[3];
		
		System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
    	EngineParameter eparams1 = new EngineParameter(enginePath1);
    	EngineParameter eparams2 = new EngineParameter(enginePath2);

    	DataSource datasource;
		Preparator preparator;
		try {

			// similaridade classica
			
			datasource = new DataSource(eparams1.getDataSouceParams());
			TrainingData trainingData1 = datasource.readTraining();
			preparator =  new UCFClassicPreparator();
			PreparedData preparedData1 = preparator.prepare(trainingData1);
			File file1 = ((UCFClassicPreparedData) preparedData1).getFile();
			DataModel ratingMatrix = new FileDataModel(file1);

			String path = "src/resources/main/analyzer/ml-1m/numOfComumItems.csv";
			saveNumberOfComumItems(ratingMatrix, path);
			
			
/*			PearsonCorrelationSimilarity similarityMatrix1 = new PearsonCorrelationSimilarity(ratingMatrix);
			
			// similaridade preferências
			
			datasource = new DataSource(eparams2.getDataSouceParams());
			TrainingData trainingData2 = datasource.readTraining();
			preparator = new UCFMultiAttributePreparator(eparams2.getPreparatorParams());
			PreparedData preparedData2 = preparator.prepare(trainingData2);
			
	// BEGIN Preference Similarity
			List<File> FoIMatrixesFiles = ((UCFMultiAttributePreparedData) preparedData2).getFoIMatrixesFiles();
			
			LongPrimitiveIterator users2, users1;
			int numUsers = ratingMatrix.getNumUsers();
			System.out.println("NumUsers: "+ numUsers);
			EnsembledSymmetricSimilarity similarityMatrix2 = new EnsembledSymmetricSimilarity(numUsers);

			PearsonCorrelationSimilarity s;
			Long userID1, userID2;
			double value, weight = 1;
			// Similarity summarization
			//System.out.println("Files: "+FoIMatrixesFiles);
			for(File FoIMatrixFile : FoIMatrixesFiles) {
				System.out.println(FoIMatrixFile.getName());
				UCFMultiAttributeAlgorithm.preferenceMatrix = new FileDataModel(FoIMatrixFile);
				s = new PearsonCorrelationSimilarity(UCFMultiAttributeAlgorithm.preferenceMatrix);
				
				users1 = ratingMatrix.getUserIDs();
				while(users1.hasNext()) {
					userID1 = users1.next();
					users2 = ratingMatrix.getUserIDs();
					while(users2.hasNext()) {
						userID2 = users2.next();
						//if(userID1%500==0) System.out.println("SUM: "+userID1+", "+userID2);
						value = similarityMatrix2.userSimilarity(userID1, userID2) + (weight * s.userSimilarity(userID1, userID2));
						similarityMatrix2.setUserSimilarity(userID1, userID2, value);
					}
				}
			}
			
			// Similarity mean division
			users1 = ratingMatrix.getUserIDs();
			while(users1.hasNext()) {
				userID1 = users1.next();
				users2 = ratingMatrix.getUserIDs();
				while(users2.hasNext()) {
					userID2 = users2.next();				
					//if(userID1%500==0) System.out.println("DIV: "+userID1+", "+userID2);
					value = (similarityMatrix2.userSimilarity(userID1, userID2) / (double) FoIMatrixesFiles.size());
					similarityMatrix2.setUserSimilarity(userID1, userID2, value);
				}
			}
	// END Preference Similarity
			
			LongPrimitiveIterator ui1, ui2;
			Long u1, u2;
			int count = 0, progress = 0, total = 6040 * 6040;
			double mae = 0;
			ui1 = ratingMatrix.getUserIDs();
			while(ui1.hasNext()) {
				u1 = ui1.next();
				ui2 = ratingMatrix.getUserIDs();
				while(ui2.hasNext()) {
					u2 = ui2.next();
					if(!u2.equals(u1)) {
						// filtro
						if(numberOfComumItems(u1, u2, ratingMatrix) >= TH_BIAS) {
							// MAE 
							mae += Math.abs(similarityMatrix2.userSimilarity(u1, u2) - similarityMatrix1.userSimilarity(u1, u2));
							count++;
						}
					}
					if(progress++ % 100000 == 0) {System.out.println(progress +" / "+ total);}
				}
			}
			mae = mae / (double) count;
			
			System.out.println("count: "+ count + " | mae: " + mae);
*/		} catch (IOException e) {
    		System.out.println("There was an IO exception.");
			e.printStackTrace();
    	} catch (TasteException e) {
    		System.out.println("There was an Taste exception.");
			e.printStackTrace();
    	}    

	}

	private static int numberOfComumItems(Long u1, Long u2, DataModel ratingMatrix) throws TasteException {
		LongPrimitiveIterator ii1, ii2; 
		Long i1, i2;
		int count = 0;
		ii1 = ratingMatrix.getItemIDsFromUser(u1).iterator();
		while(ii1.hasNext()) {
			i1 = ii1.next();
			ii2 = ratingMatrix.getItemIDsFromUser(u1).iterator();
			while(ii2.hasNext()) {
				i2 = ii2.next();
				if(i1.equals(i2)) {
					count++;
				}
			}
		}
		return count;
	}
	
	private static void saveNumberOfComumItems(DataModel ratingMatrix, String path) throws TasteException {
		int numUsers = ratingMatrix.getNumUsers();
		EnsembledSymmetricSimilarity numOfComumItemsMatrix = new EnsembledSymmetricSimilarity(numUsers);

		LongPrimitiveIterator ui1, ui2;
		Long u1, u2;
		int res, count = 0, progress = 0, total = numUsers * numUsers;
		ui1 = ratingMatrix.getUserIDs();
		while(ui1.hasNext()) {
			u1 = ui1.next();
			ui2 = ratingMatrix.getUserIDs();
			while(ui2.hasNext()) {
				u2 = ui2.next();
				res = numberOfComumItems(u1, u2, ratingMatrix);
				numOfComumItemsMatrix.setUserSimilarity(u1, u2, res);
				count++;
				if(progress++ % 100000 == 0) {System.out.println(progress +" / "+ total);}
			}
		}
		numOfComumItemsMatrix.save(path);
	}
}