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
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparator;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparedData;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledSymmetricSimilarity;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparator;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparedData;
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
public class MaxminNum {

    /**
     * Default bias threshold.
     */
    public static final int TH_BIAS = 2;
    	
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
		
		System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
    	EngineParameter eparams1 = new EngineParameter(enginePath1);

    	DataSource datasource;
		Preparator preparator;
		LongPrimitiveIterator users1, users2;
		Long userID1, userID2;
		
		try {
			// similaridade classica
			
			datasource = new DataSource(eparams1.getDataSouceParams());
			TrainingData trainingData1 = datasource.readTraining();
			preparator =  new UCFClassicPreparator();
			PreparedData preparedData1 = preparator.prepare(trainingData1);
			File file1 = ((UCFClassicPreparedData) preparedData1).getFile();
			DataModel ratingMatrix = new FileDataModel(file1);

			String path = "src/resources/main/analyzer/ml-1m/numOfComumItems.csv";			
			if(!(new File(path)).exists()) {
				saveNumberOfComumItems(ratingMatrix, path);
			}
		
			int numUsers = 6040;	
			int progress = 0, total = numUsers*(numUsers+1)/2;
			EnsembledSymmetricSimilarity numOfComumItems = new EnsembledSymmetricSimilarity(path);
			int count = 0, min = 100, max = 0, num=0;
			users1 = ratingMatrix.getUserIDs();
			while(users1.hasNext()) {
				userID1 = users1.next();
				users2 = ratingMatrix.getUserIDs();
				while(users2.hasNext()) {
					userID2 = users2.next();
					if((long)userID1 < (long)userID2) {
						num = (int) numOfComumItems.userSimilarity(userID1, userID2);
						if(num > max) { max = num; }
						if(num < min) { min = num; }	
						if(progress++ % 100000 == 0) {
							System.out.println(progress +" / "+ total);
						}
					}			
				}
			}
			System.out.println("count: "+ count + " | min: " + min + " | max: " + max);
		} catch (IOException e) {
    		System.out.println("There was an IO exception.");
			e.printStackTrace();
    		} catch (TasteException e) {
    			System.out.println("There was an Taste exception.");
			e.printStackTrace();
    		}    
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
