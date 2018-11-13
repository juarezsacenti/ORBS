package br.ufsc.lapesd.sro.multiattributefc;

import static org.junit.Assert.*;

import org.apache.mahout.cf.taste.common.TasteException;
import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledAsymmetricSimilarity;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.EnsembledSymmetricSimilarity;


public class EnsembledSimilarityTest {

	@Test
	public void testSame() {
		try {
			int numUsers = 10;
			EnsembledAsymmetricSimilarity similarity1 = new EnsembledAsymmetricSimilarity(numUsers);

			int numSims = 2;			
			int[] weight = new int[numSims];
			weight[0] = 1;
			weight[1] = 1;
			
			long userID1, userID2;
			double newValue, updatedValue;
			for(int i = 0; i<numSims; i++) {
				for(int j = 0 ; j < numUsers; ++j) {
					userID1 = j;
					for(int k = 0 ; k < numUsers; ++k) {
						userID2 = k;
						newValue = root( abs((double) (userID1 - userID2)) / ((double) numUsers), i+1);
						updatedValue = similarity1.userSimilarity(userID1, userID2) + (weight[i] * newValue);
						similarity1.setUserSimilarity(userID1, userID2, updatedValue);
					}
				}
			}
			for(int i = 0; i<numSims; i++) {
				for(int j = 0 ; j < numUsers; ++j) {
					userID1 = j;
					for(int k = 0 ; k < numUsers; ++k) {
						userID2 = k;
						updatedValue = similarity1.userSimilarity(userID1, userID2)  / ((double) numSims );
						similarity1.setUserSimilarity(userID1, userID2, updatedValue);
					}
				}
			}
			similarity1.save();
			EnsembledSymmetricSimilarity similarity2 = new EnsembledSymmetricSimilarity("src/resources/main/temp/sim_ensembledAsymSim.csv");

			assertTrue(similarity1.equals(similarity2));

		} catch (JsonIOException | JsonSyntaxException | TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double root(double base, int exp) {
		double result = 1;
		
		for(int i=0; i < exp; ++i) {
			result = result * base;
		}
		
		return result;
	}

	private double abs(double value) {
		double result = value;
		
		if(value < 0) {
			result = result * -1;
		}
		
		return result;
	}
}
