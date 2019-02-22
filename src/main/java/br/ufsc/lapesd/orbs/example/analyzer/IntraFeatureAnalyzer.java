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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *  Classe que descreve os algoritmos do experimento que contabiliza a quantidade de filmes que possuem 
 *  desvio padrão de notas inferior a um dado limite, formatado por gênero.
 *  Dois métodos podem ser utilizados para selecionar os usuários a serem analisados.
 *  O método selectUsersFromFile seleciona os usuários pertencentes a uma lista descrita no arquivo de entrada.
 *  O método selectUsers seleciona os usuários cuja porcentagem dada dos filmes avaliados sejam de determinado gênero.
 */
public class IntraFeatureAnalyzer {

    /**
     * Default reference file.
     */
    public static final String INPUT_FILE= "src/resources/main/example/datasets/Mov1M/ML1M-MAGenrePercPerFilms.csv";
    /**
     * Optional file including userIDs used to filtering INPUT_FILE.
     */
    public static final String INPUT_FILE2= "src/resources/main/analyzer/ml-1m/featureAnalysis-Genre-userIDs.csv";
    /**
     * Default reference file describing each feature of an item.
     */
    public static final String INPUT_FILE3= "src/resources/main/data/ml-1m/ml-1m/movies.dat";
    /**
     * Default genre.
     */
    public static final String[] GENRE = { "Action", // 1
    		"Adventure",    // 2
    		"Animation",    // 3
    		"Children's",   // 4
    		"Comedy",       // 5
    		"Crime",		// 6
    		"Documentary", 	// 7
    		"Drama",		// 8
    		"Fantasy",		// 9
    		"Film-Noir",	// 10
    		"Horror",		// 11
    		"Musical",		// 12
    		"Mystery",		// 13
    		"Romance",		// 14
    		"Sci-Fi",		// 15
    		"Thriller",		// 16
    		"War",			// 17
    		"Western"};		// 18
    /**
     * Default genre.
     */
    public static final int[] ITEMS_PER_GENRE = { 503, // 1
    		283,   		// 2
    		105,    	// 3
    		251,   		// 4
    		1200,       // 5
    		211,		// 6
    		127, 		// 7
    		1603,		// 8
    		68,			// 9
    		44,			// 10
    		343,		// 11
    		114,		// 12
    		106,		// 13
    		471,		// 14
    		276,		// 15
    		492,		// 16
    		143,		// 17
    		68};		// 18
    /**
     * Default minimal threshold for percent of movies that belong to the user's main genre.
     * Only user's with movie percent of main genre above this threshold are selected.
     */
    public static final double TH_USER = 0.8;
    /**
     * Default item threshold.
     */
    public static final int TH_RATES = 1;
    /**
     * Default relevance threshold.
     */
    public static final double TH_STD = 0.5;
    /**
     * Define if it uses general mean.
     */
    public static final boolean USES_GENERAL_MEAN = true;
    /**
     * Utility classes should not have a public or default constructor.
     */
    private IntraFeatureAnalyzer() {
    }

    /**
     * Main method. Parameter is not used.
     *
     * @param args the arguments (not used)
     */
    public static void main(final String[] args) {
        String modelPath = "src/resources/main/analyzer/ml-1m/";
        String dataFile = "src/resources/main/data/ml-1m/ml-1m/ratings.dat";
        String inputFile = INPUT_FILE;
        String inputFile2 = INPUT_FILE2;
        String inputFile3 = INPUT_FILE3;
        int genre = 17;
		String outPath = modelPath+"genre"+genre+".csv";
        double thUser = TH_USER;
        double thRates = TH_RATES;
		double thStd = TH_STD;
    	String delimiter = ", ";
    	
    	System.out.println("genreID"+delimiter+"genreName"+delimiter+"foundUsers"+delimiter+"totalItems"
        		+delimiter+"foundItems"+delimiter+"itemsUnderRateThreshold"+delimiter+"foundThatGenre"
    			+delimiter+"foundOtherGenres"+delimiter+"itemsUnderStdThreshold"
        		+delimiter+"thatGenre-meanMean"+delimiter+"thatGenre-meanStd"+delimiter+"thatGenre-stdMean"+delimiter+"thatGenre-stdStd"
        		+delimiter+"otherGenre-meanMean"+delimiter+"otherGenre-meanStd"+delimiter+"otherGenre-stdMean"+delimiter+"otherGenre-stdStd"
        		+delimiter+"general-meanMean"+delimiter+"general-meanStd"+delimiter+"general-stdMean"+delimiter+"general-stdStd");

    	//oneGenre(genre, thUser, inputFile, dataFile, thRates, thStd, inputFile3, delimiter);
    	allGenres(thUser, inputFile, dataFile, thRates, thStd, inputFile2, inputFile3, delimiter);
    	
    }
    
    private static void oneGenre(int genre, double thUser, String inputFile, String dataFile, double thRates, double thStd, String inputFile2, String inputFile3, String delimiter) {
    	//List<Integer> users = selectUsers(thUser, genre, inputFile);
    	List<Integer> users = selectUsersFromFile(inputFile2);
    	List<AnItem> items = selectRatings(users, dataFile, USES_GENERAL_MEAN);
    	double[] results = calculeMetrics(items, genre, thRates, thStd, inputFile3);

    	System.out.println((genre+1)+delimiter+GENRE[genre]+delimiter+users.size()+delimiter+ITEMS_PER_GENRE[genre]
    		+delimiter+items.size()+delimiter+Math.round(results[0])+delimiter+Math.round(results[1])
    		+delimiter+Math.round(results[2])+delimiter+Math.round(results[3])
    		+delimiter+results[4]+delimiter+results[5]+delimiter+results[6]+delimiter+results[7]
    		+delimiter+results[8]+delimiter+results[9]+delimiter+results[10]+delimiter+results[11]
    		+delimiter+results[12]+delimiter+results[13]+delimiter+results[14]+delimiter+results[15]);
    }
    
    private static void allGenres(double thUser, String inputFile, String dataFile, double thRates, double thStd, String inputFile2, String inputFile3, String delimiter) {
    	for(int i=0; i < GENRE.length; ++i) {
    		oneGenre(i, thUser, inputFile, dataFile, thRates, thStd, inputFile2, inputFile3, delimiter);
    	}    	
    }
    
	private static List<Integer> selectUsers(double thUser, int genre, String refFile) {
    	String delimiter = ";";
        String enclosure = "'";
        boolean hasHeaderLine = false;
    	
        String regex;
        if(enclosure.equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 

    	List<Integer> users = new ArrayList<Integer>();

    	String line = "";
    	int user;
        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                column[1] = column[1].trim(); 
                column[2] = column[2].trim(); 
                
                user = Integer.parseInt(column[0]);
                if(!users.contains(user)) {
                    if(genre+1 == Integer.parseInt(column[1])) {
                    	if(Double.parseDouble(column[2]) >= thUser) {
                    		users.add(user);
                    	}
                    }
                }
            }
		} catch(IOException e) {
            e.printStackTrace();
		}        
		return users;
    }
	
	private static List<Integer> selectUsersFromFile(String refFile) {
    	String delimiter = ";";
        String enclosure = "'";
        boolean hasHeaderLine = false;
    	
        String regex;
        if(enclosure.equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 

    	List<Integer> users = new ArrayList<Integer>();

    	String line = "";
    	int user;
        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                
                user = Integer.parseInt(column[0]);
                if(!users.contains(user)) {
            		users.add(user);
                }
            }
		} catch(IOException e) {
            e.printStackTrace();
		}        
		return users;
    }
	
	private static List<AnItem> selectRatings(List<Integer> users, String dataFile, boolean usesGeneralUserMeanRate) {
        Collections.sort(users);

		String delimiter = "::";
        String enclosure = "'";
        boolean hasHeaderLine = false;        
        String regex;
		if(enclosure.equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 
	   	String line = "";
		
    	/*
    	 * First reading: gather users mean rating
    	 */
    	List<AnUser> anUsers = new ArrayList<AnUser>();
    	int user;
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                column[1] = column[1].trim(); 
                column[2] = column[2].trim(); 
                column[3] = column[3].trim();                 
                
                user = Integer.parseInt(column[0]);
                if(Collections.binarySearch(users, user) >= 0) {
	                AnUser theUser = null;
	                for(AnUser u : anUsers) {
	                	if(u.usedID == user) {
	                		theUser = u;
	                		break;
	                	}
	                }
	                
	                if(theUser == null) {
	                	theUser = new AnUser(user);
	                	anUsers.add(theUser);
	                }
	            	theUser.sumOfRatings += Double.parseDouble(column[2]);
	            	theUser.numOfItems++;
                }
            }
		} catch(IOException e) {
            e.printStackTrace();
		}
	   	
        /*	
         * Second reading: gather user's comparable scores 
         */
    	List<AnItem> items = new ArrayList<AnItem>();
        int item;
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                column[1] = column[1].trim(); 
                column[2] = column[2].trim(); 
                column[3] = column[3].trim();                 
                
                user = Integer.parseInt(column[0]);
                if(Collections.binarySearch(users, user) >= 0) {
	                // Search or create item
                	item = Integer.parseInt(column[1]);
	                AnItem theItem = null;
	                for(AnItem i : items) {
	                	if(i.itemID == item) {
	                		theItem = i;
	                		break;
	                	}
	                }
	                if(theItem == null) {
	                	theItem = new AnItem(item);
	                	items.add(theItem);
	                }
	               
	                // Search user for user's mean rating
	                AnUser theUser = null;
	                for(AnUser u : anUsers) {
	                	if(u.usedID == user) {
	                		theUser = u;
	                		break;
	                	}
	                }              
	                
	                double comparableScore = Double.parseDouble(column[2]) - theUser.meanRating();
	                theItem.scores.add(comparableScore);
	            }
        	}
		} catch(IOException e) {
            e.printStackTrace();
		}
        return items;
	}
	
	/**
	 *  Método que 
	 */
	private static double[] calculeMetrics(List<AnItem> items, int genre, double thRates, double thStd, String refFile) {
        List<AnItem> itemGenreList = prepareItemFeatureList(refFile);  
        List<Double> thatClassMean = new ArrayList<Double>();
        List<Double> thatClassStd = new ArrayList<Double>();
        List<Double> othersMean = new ArrayList<Double>();
        List<Double> othersStd = new ArrayList<Double>();
        
		double[] results = new double[16];
        int size;
        double[] aux;
        double stDev, mean;
		
		int n = 0, countThItem = 0, countThRate = 0;
		for(AnItem i : items) {
			size = i.scores.size();
			if(size <= thRates) {
				countThItem++;
			} else {
    			aux = new double[size];
    			n = 0;
    			for(Double s : i.scores) 
    				aux[n++] = s;

    			mean = new Statistics(aux).getMean();
    			stDev = new Statistics(aux).getStdDev();		
    			if(stDev <= thStd) {
    				countThRate++;
    			}
    			
    			if(hasGenre(itemGenreList, i.itemID, genre)) {
    				thatClassMean.add(mean);
    				thatClassStd.add(stDev);
    			} else {
    				othersMean.add(mean);
    				othersStd.add(stDev);
    			} 			
			}
		}
	
		results[0] = (double) countThItem;			// singleRatedItems
		results[1] = (double) thatClassMean.size(); // foundThatGenre
		results[2] = (double) othersMean.size();	// foundOtherGenre
		results[3] = (double) countThRate;			// itemsUnderThreshold
		
		aux = new double[thatClassMean.size()];
		n = 0;
		for(Double s : thatClassMean) 
			aux[n++] = s;
		results[4] = new Statistics(aux).getMean();	// thatGenre-meanMean
		results[5] = new Statistics(aux).getStdDev();//thatGenre-meanStd
		
		aux = new double[thatClassStd.size()];
		n = 0;
		for(Double s : thatClassStd) 
			aux[n++] = s;		
		results[6] = new Statistics(aux).getMean(); // thatGenre-stdMean
		results[7] = new Statistics(aux).getStdDev();//thatGenre-stdStd
				
		aux = new double[othersMean.size()];
		n = 0;
		for(Double s : othersMean) 
			aux[n++] = s;
		results[8] = new Statistics(aux).getMean();	// otherGenres-meanMean
		results[9] = new Statistics(aux).getStdDev();//otherGenres-meanStd
		
		aux = new double[othersStd.size()];
		n = 0;
		for(Double s : othersStd) 
			aux[n++] = s;
		results[10] = new Statistics(aux).getMean();// otherGenres-stdMean
		results[11] = new Statistics(aux).getStdDev();//otherGenres-stdStd
		
		aux = new double[thatClassMean.size()+othersMean.size()];
		n = 0;
		for(Double s : thatClassMean) 
			aux[n++] = s;
		for(Double s : othersMean) 
			aux[n++] = s;
		results[12] = new Statistics(aux).getMean();// general-meanMean
		results[13] = new Statistics(aux).getStdDev();//general-meanStd
		
		aux = new double[thatClassStd.size()+othersStd.size()];
		n = 0;
		for(Double s : thatClassStd) 
			aux[n++] = s;
		for(Double s : othersStd) 
			aux[n++] = s;
		results[14] = new Statistics(aux).getMean();// general-stdMean
		results[15] = new Statistics(aux).getStdDev();//general-stdStd
	
    	return results;
	}
	
	private static boolean hasGenre(List<AnItem> itemGenreList, int itemID, int genre) {
        boolean result = false;
        
        AnItem theItem = null;
        for(AnItem i : itemGenreList) {
        	if(i.itemID == itemID) {
        		theItem = i;
        		break;
        	}
        }    			
        
        if(theItem == null) {
        	System.err.println("ALGO FORA DO LUGAR");
        } else {
        	for(Double d : theItem.scores) {
        		if(Math.round(d)== genre) {
        			result = true;
        			break;
        		}
        	}
        }
		return result;
	}

	private static List<AnItem> prepareItemFeatureList(String refFile) {
		List<AnItem> itemFeatureList = new ArrayList<AnItem>();
		AnItem theItem;
		
		String delimiter = "::";
	   	String delimiter1 = "\\|";
        String enclosure = "\"";
        boolean hasHeaderLine = false;
    	
        String regex, regex1;
        if(enclosure.equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
	        regex1 = delimiter1 +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	        regex1 = delimiter1 +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		}

    	String line = "";
    	int itemID;
        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
        	if(hasHeaderLine) {
            	line = br.readLine();
            }
        	while ((line = br.readLine()) != null) {
        		String[] column = line.split(regex, -1);             
                column[0] = column[0].trim(); 
                column[1] = column[1].trim(); 
                column[2] = column[2].trim(); 
        		String[] featureIDs = column[2].split(regex1, -1);             
                
                itemID = Integer.parseInt(column[0]);
                theItem = new AnItem(itemID);
                for(int i = 0; i < featureIDs.length; ++i) {
                	theItem.scores.add(Double.parseDouble(getGenreID(featureIDs[i])));
                }
                itemFeatureList.add(theItem);
        	}
		} catch(IOException e) {
            e.printStackTrace();
		}
        return itemFeatureList;
	}
	
	private static String getGenreID(String string) {
		int genreID = -1;
		for(int i=0; i<GENRE.length; ++i) {
			if(string.equals(GENRE[i])) {
				genreID=i;
			}
		}
		return ""+genreID;
	}
}

class AnUser {
	int usedID;
	int numOfItems;
	double sumOfRatings;
	
	public AnUser(int usedID) {
		this.usedID = usedID;
		this.numOfItems = 0;
		this.sumOfRatings = 0;
	}

	public double meanRating() {
		return sumOfRatings / (double) numOfItems;
	}
}

class AnItem {
	int itemID;
	List<Double> scores;
	
	public AnItem(int itemID) {
		super();
		this.itemID = itemID;
		this.scores = new ArrayList<Double>();
	}
}

class Statistics {
    double[] data;
    int size;   

    public Statistics(double[] data) {
        this.data = data;
        size = data.length;
    }   

    double getMean() {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/size;
    }

    double getVariance() {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        if(size == 1)
        	return 0;
        return temp/(size-1);
    }

    double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double median() {
       Arrays.sort(data);
       if (data.length % 2 == 0)
          return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
       return data[data.length / 2];
    }
}