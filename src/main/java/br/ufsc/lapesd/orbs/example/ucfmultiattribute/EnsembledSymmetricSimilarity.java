package br.ufsc.lapesd.orbs.example.ucfmultiattribute;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;

public class EnsembledSymmetricSimilarity implements UserSimilarity {

	//private Map<Long, HashMap<Long, Double>> map; 
	private double[] userUserMatrix;
	int numUsers;
	private Map<Long,Integer> userIndex;
	private int indexCounter;
	
    public EnsembledSymmetricSimilarity() {
		this(10000);
    }

    public EnsembledSymmetricSimilarity(int numUsers) {
		super();
		//this.map = new HashMap<Long, HashMap<Long, Double>>(this.numUsers);
		this.numUsers = numUsers;
		this.userUserMatrix = new double[numUsers*(numUsers+1)/2];
		this.userIndex = new HashMap<Long,Integer>(numUsers+1);
		this.indexCounter = 0;
	}

    public EnsembledSymmetricSimilarity(String filePath) {
		long userID1, userID2;
		double sim;
		String csvLine;
		String[] splitedCsv;
		
		try {
			File f = new File(filePath);
			Scanner sc = new Scanner(f);

			if(sc.hasNextInt()) {
				this.numUsers = sc.nextInt(); 
				this.userUserMatrix = new double[numUsers*(numUsers+1)/2];
				this.userIndex = new HashMap<Long,Integer>(numUsers+1);
				this.indexCounter = 0;

				while(sc.hasNextLine()) {
					sc.nextLine();
					if(sc.hasNext()) {
						csvLine=sc.next();
						splitedCsv = csvLine.split(",");
						if(splitedCsv.length == 3) {
							userID1 = Long.parseLong(splitedCsv[0]);
							userID2 = Long.parseLong(splitedCsv[1]);	
							sim = Double.parseDouble(splitedCsv[2]);

							setUserSimilarity(userID1, userID2, sim);
						} else { throw new IOException("Empty line"); }
					}
				}
			} else { throw new IOException("No numUsers."); }
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	private int getUserIndex(long userID1) {
    	int index = -1;
		
    	if(this.userIndex.containsKey(userID1)) {
    		index = this.userIndex.get(userID1);
    	}
		
    	return index;
    }

	public void setUserSimilarity(long userID1, long userID2, double value) {
		int index1 = getUserIndex(userID1);
		if(index1 == -1) {
    		this.userIndex.put(userID1, indexCounter);
    		index1=indexCounter;
    		this.indexCounter++;
    	}

    	int index2 = getUserIndex(userID2);
    	if(index2 == -1) {
    		this.userIndex.put(userID2, indexCounter);
    		index2=indexCounter;
    		this.indexCounter++;
    	}
    	
		this.userUserMatrix[fromMatrixToVector(index1, index2)] = value;
    }
    
	@Override
	public double userSimilarity(long userID1, long userID2)
	throws TasteException {
		int index1 = getUserIndex(userID1);
		int index2 = getUserIndex(userID2);
		double value = 0;

		if(index1 != -1 && index2 != -1) {
			value = this.userUserMatrix[fromMatrixToVector(index1, index2)];
		}
		return value;
	}

	public void save() {
		save("src/resources/main/temp/sim_ensembledSymSim.csv");
	}
	
	public void save(String path) {
		long userID1, userID2;
		double sim;
		
		try {
			OutputStream os = new FileOutputStream(path);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(numUsers+"\n");
		
			for(int i=0;i < this.numUsers; i++) {			
				userID1 = getKeyByValue(this.userIndex, i);
				for(int j=0;j < this.numUsers; j++) {
					userID2 = getKeyByValue(this.userIndex, j);
					sim = userUserMatrix[fromMatrixToVector(i, j)];
					writer.write(userID1+","+ userID2 +","+ sim+"\n");
				}
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public int fromMatrixToVector(int index1, int index2) {
		double result = -1;
		double i = (double) index1, j = (double) index2;
		if (i <= j) {
			result = (2*numUsers - i - 1)*(i/2) + j;
		} else {
	      result = (2*numUsers - j - 1)*(j/2) + i;
		}
		return (int) result;
	}
	
	@Override
	public void refresh(Collection alreadyRefreshed) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean equals(Object o) 
	{
		long userID1, userID2;
		double thisSimValue;
	    if(this == o) { 
	    	return true;
	    }
		try {
			if(o instanceof EnsembledSymmetricSimilarity) {
				EnsembledSymmetricSimilarity anotherSim = (EnsembledSymmetricSimilarity) o;
				if(this.numUsers == anotherSim.numUsers) {
					for(int i=0;i < numUsers; i++) {			
						userID1 = getKeyByValue(this.userIndex, i);
						for(int j=0;j < numUsers; j++) {
							userID2 = getKeyByValue(this.userIndex, j);
							thisSimValue = userUserMatrix[fromMatrixToVector(i, j)];
							if(thisSimValue != anotherSim.userSimilarity(userID1, userID2) ) {
								return false;
							}
						}
					}
				}
			}
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return true;
	}
}