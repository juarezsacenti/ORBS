package br.ufsc.lapesd.orbs.tokit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

public class DataSource {
	
	private final DataSourceParams dsp;
	
	public DataSource(DataSourceParams dsp) {
		this.dsp = dsp;
	}
	
    public TrainingData readTraining() {
    	AbstractMap<String, User> usersRDD = new HashMap<String,User>();
		AbstractMap<String, Item> itemsRDD = new HashMap<String,Item>();
		AbstractMap<String, UserItemEvent> ratingEventsRDD = new HashMap<String,UserItemEvent>();

        String regex;
        String delimiter = dsp.getDelimiter();
        if(dsp.getEnclosure().equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 

        int lineCount = 0;
        
        for(String sourceLocation : dsp.getSourceLocation()) {
        	System.out.println("Reading from "+ sourceLocation);
            String line = "";
	        long initialTimeSeconds = System.currentTimeMillis() / 1000;

	        try (BufferedReader br = new BufferedReader(new FileReader(sourceLocation))) {
	        	if(dsp.hasHeaderLine()) {
	            	line = br.readLine();
	            }
	        	while ((line = br.readLine()) != null) {
	        		String[] column = line.split(regex, -1);             
	                column[0] = column[0].trim(); 
	                column[1] = column[1].trim(); 
	                column[2] = column[2].trim(); 
	                
	                if(!usersRDD.containsKey(column[0])) {
	                	User user = new User(column[0]);
	                	usersRDD.put(column[0], user);
	                }
	                
	                if(!itemsRDD.containsKey(column[1])) {
	                	Item item = new Item(column[1]);
	                	itemsRDD.put(column[1], item);
	                }
	                
	                
	                long timeSeconds;
	                if(dsp.hasEventTimestamp()) {
	                	timeSeconds = Long.parseLong(column[3].trim());
	                } else {
	                	timeSeconds = (System.currentTimeMillis() / 1000) - initialTimeSeconds;
	                }
	                
	            	UserItemEvent rating = new UserItemEvent(column[0], column[1], 
	            			timeSeconds, 
	            			UserItemEventType.RATING, Float.parseFloat(column[2]));
					ratingEventsRDD.put(""+lineCount, rating);
	
		        	lineCount++;
	            }
	
			} catch(IOException e) {
	            e.printStackTrace();
			}
        }

        //System.out.println("Read events in DataSourceSplitter: " + ratingEventsRDD.size());
        return new TrainingData(usersRDD, itemsRDD, ratingEventsRDD);
    }
}
