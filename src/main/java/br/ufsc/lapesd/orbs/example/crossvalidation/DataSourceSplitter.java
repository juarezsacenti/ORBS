package br.ufsc.lapesd.orbs.example.crossvalidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.Item;
import br.ufsc.lapesd.orbs.tokit.User;
import br.ufsc.lapesd.orbs.tokit.UserItemEvent;
import br.ufsc.lapesd.orbs.tokit.UserItemEventType;

public class DataSourceSplitter {
    
	private final String tempFolder = "src/resources/main/temp/crossValid/";

	/**
     * The datasource's  parameters for reading.
     */
	private final DataSourceParams dsp;
    /**
     * The number of folds that the data will be split into.
     */
    protected int nFolds;
    /**
     * The flag that indicates if the split should be done in a per user basis.
     */
    protected boolean perUser;
    /**
     * An instance of a Random class.
     */
    protected Random rnd;
    
    protected AbstractMap<String, User> users;
    protected AbstractMap<String, Item> items;
    protected AbstractMap<String, ArrayList<UserItemEvent>> ratingEventsPerUser;
    protected AbstractMap<String, UserItemEvent> ratingEventsPerLine;
    
	public DataSourceSplitter(DataSourceParams dsp, int nFolds, boolean perUser, long seed) {
		this.dsp = dsp;
		this.nFolds = nFolds;
		this.perUser = perUser;
		rnd = new Random(seed);

		users = new HashMap<String,User>();
		items = new HashMap<String,Item>();
		ratingEventsPerUser = new HashMap<String,ArrayList<UserItemEvent>>();
		ratingEventsPerLine = new HashMap<String,UserItemEvent>();
	}

    public void readTraining() {
        String regex;
        String delimiter = dsp.getDelimiter();
        if(dsp.getEnclosure().equals("'")) {
	        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
        } else {
	        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		} 

        int lineCount = 0;
        String line = "";
        String sourceLocation = dsp.getSourceLocation().get(0);
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
                
                if(!users.containsKey(column[0])) {
                	User user = new User(column[0]);
                	users.put(column[0], user);
                }
                
                if(!items.containsKey(column[1])) {
                	Item item = new Item(column[1]);
                	items.put(column[1], item);
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

            	if(perUser) {
            		if(!ratingEventsPerUser.containsKey(column[0])) {
            			ratingEventsPerUser.put(column[0], new ArrayList<UserItemEvent>());
            		}
            		ratingEventsPerUser.get(column[0]).add(rating);
            	} else {
            		ratingEventsPerLine.put(""+lineCount, rating);
            	}
            	lineCount++;
            }
		} catch(IOException e) {
            e.printStackTrace();
		}
    }
    
    public String split() {
    	readTraining();
    	
    	Path p = Paths.get(dsp.getSourceLocation().get(0));
    	String baseName = "split";
    	//String baseName = p.getFileName().toString().split("\\.")[0];
    	
    	List<String> splittedFileNames = new ArrayList<String>();    	
    	String s;
    	for(int i=0; i<nFolds;++i){
			s = tempFolder+baseName+"_"+i+".csv";
			splittedFileNames.add(s);
			File f = new File(s);
			f.getParentFile().mkdirs(); 
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	if(perUser) {
    		 int n = 0;
             for (User user : users.values()) {
                 List<UserItemEvent> eventsFromUser = new ArrayList<UserItemEvent>();
                 for (UserItemEvent e : ratingEventsPerUser.get(user.getEntityId())) {
                     eventsFromUser.add(e);
                 }
                 Collections.shuffle(eventsFromUser, rnd);
                 for (UserItemEvent e : eventsFromUser) {
                     int curFold = n % nFolds;
                     append(e, tempFolder+baseName+"_"+curFold+".csv");
                     n++;
                 }
             }
    	} else {
    		 List<UserItemEvent> events = new ArrayList<>();
             for (UserItemEvent e : ratingEventsPerLine.values()) {
                 events.add(e);
             }
             Collections.shuffle(events, rnd);
             int n = 0;
             for (UserItemEvent e : events) {
                 int curFold = n % nFolds;
                 append(e, tempFolder+baseName+"_"+curFold+".csv");
                 n++;
             }
    	}
    	return tempFolder;
    }
    
    private void append(UserItemEvent e, String filePath) {
    	boolean append = true;
        String delimiter = dsp.getDelimiter();    
        
		OutputStream os = null;
		try {
			os = new FileOutputStream(filePath, append);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			writer.write(""+e.getUser()+delimiter+e.getItem()+delimiter+e.getRatingValue()+delimiter+e.getTime()+"\n");
	    	
			writer.flush();
			writer.close();
		}
		catch (FileNotFoundException e1) {
		    System.out.println("File not found" + e1);
		}
		catch (IOException ioe) {
		    System.out.println("Exception while writing file " + ioe);
		}
		finally {
		    // close the streams using close method
		    try {
		        if (os != null) {
		            os.close();
		        }
		    }
		    catch (IOException ioe1) {
		        System.out.println("Error while closing stream: " + ioe1);
		    }
		}

	}

	public List<Item> getUserItems(User u) {
		List<Item> itemsFromUser = new ArrayList<Item>();
		Item i;
		for(UserItemEvent e : ratingEventsPerUser.get(u.getEntityId())) {
			i = items.get(e.getItem());
    		if(!itemsFromUser.contains(i)) {
    			itemsFromUser.add(i);
    		}
    	}
		
		return itemsFromUser;
	}

	public Float getUserItemPreference(User u, Item i) {
		Float r = null;
		for(UserItemEvent e : ratingEventsPerUser.get(u.getEntityId())) {
    		if(e.getItem().equals(i.getEntityId())) {
    			r = e.getRatingValue();
    		}
		}
		
		return r;
	}
}