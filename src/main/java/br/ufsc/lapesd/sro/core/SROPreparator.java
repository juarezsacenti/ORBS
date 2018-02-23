package br.ufsc.lapesd.sro.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.ufsc.lisa.sedim.core.HierarchyBuilder;
import br.ufsc.lapesd.sro.example.multiattributefc.MultiAttributeFCPreparedData;
import br.ufsc.lapesd.sro.tokit.AnnotationSource;
import br.ufsc.lapesd.sro.tokit.Item;
import br.ufsc.lapesd.sro.tokit.ItemResourceAnnotations;
import br.ufsc.lapesd.sro.tokit.Pair;
import br.ufsc.lapesd.sro.tokit.Preparator;
import br.ufsc.lapesd.sro.tokit.PreparatorParams;
import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.TrainingData;
import br.ufsc.lapesd.sro.tokit.User;
import br.ufsc.lapesd.sro.tokit.UserItemEvent;

public class SROPreparator implements Preparator {
	private final PreparatorParams pp;
	private final ContextOntology contextOntology;
	private AnnotationSource annotationSource;
	private final AbstractMap<String, ItemResourceAnnotations> itemAnnotations;
	
	public SROPreparator(PreparatorParams pp) {
		this.pp = pp;
    	this.contextOntology = new ContextOntology(pp.getContextOntologyParams());
    	this.annotationSource = new AnnotationSource(pp.getAnnotationSourceParams());
    	this.itemAnnotations = annotationSource.readAnnotations();
	}
	
	@Override
	public PreparedData prepare(TrainingData trainingData) {
		populateContextOntology(trainingData);

    	// Para cada hasObjectFI (belongsToGenre, hasActor, hasDirector, hasFilmLocation, hasReleasingCountry, isAwardedWith, isTranslatedTo, nominatedFor)
    	HierarchyBuilder hb = new HierarchyBuilder(
    			pp.getHierarchyBuilderParams(),
    			contextOntology.getKnowledgeBase());
    	hb.run();
      	
    	// DimensionTailoring.(pp.getDimensionTailoringParams(), contextOntology);
    	
    	contextOntology.save();

    	HitAggregator ha = new HitAggregator(pp.getHitAggregatorParams());
    	HashMap<String, HashMap<String, Float>> attributeHitsByUser = ha.aggregate(hb.getCounters());
    	
    	//System.out.println(attributeHitsByUser.toString());
    	
    	HashMap<String, HashMap<String, Float>> userAttributeMatrix = contextOntology2Matrix(attributeHitsByUser);
    	
    	File itemModelFile = convert2ItemModelFile(trainingData);
    	File attributeModelFile = convert2AttributeModelFile(userAttributeMatrix);
    	
		return new MultiAttributeFCPreparedData(itemModelFile, attributeModelFile);
    }

	private void populateContextOntology(TrainingData trainingData) {
    	for(User user : trainingData.getUsers().values()) {
	        contextOntology.addUser(user);
    	}
        
    	for(Item item : trainingData.getItems().values()) {
	        if(!contextOntology.hasItem(item)) {
	        	contextOntology.addItem(item);
	        	List<Pair<String, String>> thisItemAnnotations = itemAnnotations.get(item.getEntityId()).getAnnotations();
		        for(Pair<String, String> pair : thisItemAnnotations) {
		        	contextOntology.addAnnotation(item, pair.getKey(), pair.getValue());
		           	// SemanticExpansion.(pp.getSemanticExpansionParams(), annotation);
		        }
	        }
    	}
    	
    	for(UserItemEvent event : trainingData.getEvents().values()) {
	        contextOntology.addEvent(event);
    	}

	}
	
	private HashMap<String, HashMap<String, Float>> contextOntology2Matrix(
			HashMap<String, HashMap<String, Float>> attributeHitsByUser) {
    	HashMap<String, HashMap<String, Float>> userAttributeMatrix = new HashMap<String, HashMap<String, Float>>();
    	HashMap<String, Float> attributeValue;
		Float value;
		String userId, userURI;
    	List<String> factorsOfInterest = contextOntology.getFactorsOfInterest();
    	for(User user : contextOntology.getUsers()) {
    		userId = user.getEntityId();
    		userURI = "http://www.lapesd.inf.ufsc.br/projetos/sro/mysro.owl#audience"+userId;
    		attributeValue = new HashMap<String, Float>();
        	for(String factor : factorsOfInterest) {
        		if(attributeHitsByUser.containsKey(userURI) 
        		  && attributeHitsByUser.get(userURI).containsKey(factor)) {
        			value = attributeHitsByUser.get(userURI).get(factor);
        		} else {
        			value = 0f;
        		}
        		//System.out.println(userId+" :: "+factor+" :: "+value);
				attributeValue.put(factor, value);
        		userAttributeMatrix.put(user.getEntityId(), attributeValue);
        	}
        }
		
		return userAttributeMatrix;
	}
	
	private File convert2ItemModelFile(TrainingData trainingData) {
		String sroPreparedItemModelPath= "src/resources/main/sro_itemModel.csv";
		AbstractMap<String, UserItemEvent> events = trainingData.getEvents();
		Set<String> userInItemModelFile = new HashSet<String>();
		
		try {
			OutputStream os = new FileOutputStream(sroPreparedItemModelPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			String user;
			for(UserItemEvent event : events.values()) {
					user = event.getUser();
					userInItemModelFile.add(user);
					writer.write(""+ user +","+event.getItem()+","+event.getRatingValue()+","+System.currentTimeMillis()+"\n");
	    	}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new File(sroPreparedItemModelPath);
	}
	
	private File convert2AttributeModelFile(HashMap<String, HashMap<String, Float>> userAttributeMatrix) {
		String sroAttributeModelPath= "src/resources/main/sro_attributeModel.csv";
		
		HashMap<String, Integer> attributeMap = new HashMap<String, Integer>();
    	HashMap<String, Float> attributeSet;
    	String user, attribute;
    	int nextInteger = 0, attributeInteger;
		try {
			OutputStream os = new FileOutputStream(sroAttributeModelPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");

			for(Entry<String, HashMap<String, Float>> entry1 : userAttributeMatrix.entrySet()) {
	    		user = entry1.getKey();
	    		attributeSet = entry1.getValue();
				for(Entry<String, Float> entry2 : attributeSet.entrySet()) {
					attribute = entry2.getKey();
					if(!attributeMap.containsKey(attribute)) {
						attributeInteger = nextInteger++;
						attributeMap.put(attribute, attributeInteger);
					} else {
						attributeInteger = attributeMap.get(attribute);
					}
					writer.write(user+","+ attributeInteger +","+ entry2.getValue()+","+System.currentTimeMillis()+"\n");
					System.out.println(user+", "+ attributeInteger +", "+ entry2.getValue());
				}
			}
    			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new File(sroAttributeModelPath);
    }
}