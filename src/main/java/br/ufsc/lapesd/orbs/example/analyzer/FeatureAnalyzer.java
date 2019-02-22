package br.ufsc.lapesd.orbs.example.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Classe que descreve os algoritmos do experimento que, dado um conjunto de dados e uma porcentagem de corte, 
 *  obtém a quantidade de gêneros necessários para contabilizar a dada porcentagem de filmes avaliados de cada usuário, 
 *  formatado em uma tabela de quantidade de gêneros por número de usuários.
 **/
public class FeatureAnalyzer {

    /**
     * Default reference file.
     */
    public static final String INPUT_FILE= "src/resources/main/example/datasets/Mov1M/ML1M-MAGenrePercPerFilms.csv";
    /**
     * Default percent.
     */
    public static final double TH_PERCENT = 0.8;
    /**
     * List of group of users to print.
     */
    public static final int[] GROUP_TO_PRINT= {1,2};
    /**
     * Utility classes should not have a public or default constructor.
     */
    private FeatureAnalyzer() {
    }

    /**
     * Main method. Parameter is not used.
     * FeatureAnalyzer.java prints the number of users which the percentage of items belonging to one or more
     * categories of analyzed feature is greater than thPercent threshold in outPath1 file, like the example above.
     * 
     * 1;525
     * 2;2534
     * 3;2779
     * 4;201
     * 5;1
     *
     * It means that the group of 525 users have at least thPercent (80%) items belonging to 1 unique category of analyzed
     * feature (genre) in input dataset (ML1M).
     * 
     * If groupToPrint is not empty, FeatureAnalyzer.java also prints the userID of each group of groupToPrint in outPath2 file.
     *
     * @param args the arguments (not used)
     */
    public static void main(final String[] args) {
        String modelPath = "src/resources/main/analyzer/ml-1m/";
        
        String featureName = "Genre";
        String inputFile = INPUT_FILE;
        double th_percent = TH_PERCENT;
		String outPath1 = modelPath+"featureAnalysis-"+featureName+".csv";
		int[] groupToPrint = GROUP_TO_PRINT;
		String outPath2 = modelPath+"featureAnalysis-"+featureName+"-userIDs.csv";
		
        System.out.println("Warning: The file \""+inputFile+"\" must be sorted by userID.");        
		analyseFeature(th_percent, inputFile, outPath1, groupToPrint , outPath2);
    }

	private static void analyseFeature(double th_percent, String refFile, String outPath, int[] groupToPrint, String outPath2) {
        Map<Integer,Integer> histogram = new HashMap<Integer,Integer>();
    	String delimiter = ";";
        String enclosure = "'";
        boolean hasHeaderLine = false;
    	
        String regex;
        if(enclosure.equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 

        List<Double> percentOfItems = new ArrayList<Double>();
    	
    	String line = "";
    	int userID = -1, curUser;
        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
        	File outputFile = new File(outPath);
        	outputFile.createNewFile();
        	OutputStream os = new FileOutputStream(outPath, false);
    		Writer writer = new OutputStreamWriter(os, "UTF-8");

        	File outputFile2 = new File(outPath2);
        	outputFile2.createNewFile();
        	OutputStream os2 = new FileOutputStream(outPath2, false);
    		Writer writer2 = new OutputStreamWriter(os2, "UTF-8");
    		
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                column[1] = column[1].trim(); 
                column[2] = column[2].trim(); 
                
                curUser = Integer.parseInt(column[0]);
                if(userID == -1) {
                	userID = curUser;
            		percentOfItems.add(Double.parseDouble(column[2]));
                } else {
                	if(userID == curUser) {
                		percentOfItems.add(Double.parseDouble(column[2]));
                	} else {
                		Collections.sort(percentOfItems, Collections.reverseOrder());
                		double sum = 0;
                		int n = 0;
                		while(sum < th_percent) {
                			sum = sum + percentOfItems.get(n);
                			n++;
                		}
                		
                		if(histogram.containsKey(n)) {
                    		histogram.put(n, histogram.get(n)+1);                		
                		} else {
                    		histogram.put(n, 1);
                		}
	    		
        	    		for(int i = 0; i < groupToPrint.length; ++i) {
        	    			if(n == groupToPrint[i]) {
        	            	    writer2.write(""+userID+"\n"); 
        	    			}	
        	    		}
        	    		
        				userID = curUser;
        				percentOfItems.clear();
                	}
                }
        	}
        	
        	{
	        	Collections.sort(percentOfItems, Collections.reverseOrder());
	    		double sum = 0;
	    		int n = 0;
	    		while(sum < th_percent) {
	    			sum = sum + percentOfItems.get(n);
	    			n++;
	    		}
	    		
	    		if(histogram.containsKey(n)) {
	        		histogram.put(n, histogram.get(n)+1);                		
	    		} else {
	        		histogram.put(n, 1);
	    		}
	    		
	    		for(int i = 0; i < groupToPrint.length; ++i) {
	    			if(n == groupToPrint[i]) {
	            	    writer2.write(""+userID+"\n"); 
	    			}	
	    		}				
				percentOfItems.clear();
	        }
		
        	for(Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
        	    Integer key = entry.getKey();
        	    Integer value = entry.getValue();
        	    
        	    writer.write(""+ key +delimiter+ value+"\n"); 
        	}        	
        	writer.flush();
    		writer.close();        		
        	writer2.flush();
    		writer2.close();    
    	} catch(IOException e) {
            e.printStackTrace();
		}
	}
}