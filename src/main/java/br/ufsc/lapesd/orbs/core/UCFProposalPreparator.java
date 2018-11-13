package br.ufsc.lapesd.orbs.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.ufsc.lisa.sedim.core.HierarchyBuilder;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparedData;
import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.Item;
import br.ufsc.lapesd.orbs.tokit.ItemResourceAnnotations;
import br.ufsc.lapesd.orbs.tokit.Pair;
import br.ufsc.lapesd.orbs.tokit.Preparator;
import br.ufsc.lapesd.orbs.tokit.PreparatorParams;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.User;
import br.ufsc.lapesd.orbs.tokit.UserItemEvent;

public class UCFProposalPreparator implements Preparator {
	private final PreparatorParams pp;
	private final ContextOntology contextOntology;
	private List<AnnotationSource> annotationSources;
	private List<String> propertiesOfInterest;
	
	public UCFProposalPreparator(PreparatorParams pp) {
		this.pp = pp;
    	this.contextOntology = new ContextOntology(pp.getContextOntologyParams());
    	this.annotationSources = new ArrayList<AnnotationSource>();
    	for(DataSourceParams dsp : pp.getAnnotationSources()) {
    		this.annotationSources.add(new TTLAnnotationSource(dsp));
    	}
    	this.propertiesOfInterest = pp.getPropertiesOfInterest();
	}
	
	@Override
	public PreparedData prepare(TrainingData trainingData) {
		System.out.println("#################  POPULATE ONTOLOGY  ################# Size:"+ Runtime.getRuntime().totalMemory());
		populateContextOntology(trainingData);

		System.out.println("#################  BUILDING HIERARCHY  ################# Size:"+ Runtime.getRuntime().totalMemory());
    	// Para cada hasObjectFI (belongsToGenre, hasActor, hasDirector, hasFilmLocation, hasReleasingCountry, isAwardedWith, isTranslatedTo, nominatedFor)
    	HierarchyBuilder hb = new HierarchyBuilder(
    			pp.getHierarchyBuilderParams(),
    			contextOntology.getKnowledgeBase());
    	hb.run();
      	
    	// DimensionTailoring.(pp.getDimensionTailoringParams(), contextOntology);
		//System.out.println("#################  SAVING ONTOLOGY  #################");

    	//contextOntology.save();

		System.out.println("#################  AGGREGATE HITS  #################");
    	HitAggregator ha = new HitAggregator(pp.getHitAggregatorParams());
    	HashMap<String, HashMap<String, Float>> attributeHitsByUser = ha.aggregate(hb.getCounters());
    	
    	//System.out.println(attributeHitsByUser.toString());
    	
		System.out.println("#################  ADDING FACTORS OF INTEREST #################");
		for(String propertyOfInterest : propertiesOfInterest) {
			contextOntology.addTriple(propertyOfInterest, 
					"http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 
					"http://www.lapesd.inf.ufsc.br/ontology/recont.owl#hasObjectFI");
		}
		
		System.out.println("#################  CONVERTING TO MATRIXES #################");
    	File itemModelFile = convert2ItemModelFile(trainingData);
    	List<File> FoIMatrixesFiles = convertAO2FoIMatrix(attributeHitsByUser);
    	
		System.out.println("#################  ENDING PREPARATIONS #################");
		return new UCFMultiAttributePreparedData(itemModelFile, FoIMatrixesFiles);
    }

	
	private void populateContextOntology(TrainingData trainingData) {
    	for(User user : trainingData.getUsers().values()) {
	        contextOntology.addUser(user);
    	}

    	for(Item item : trainingData.getItems().values()) {
	        if(!contextOntology.hasItem(item)) {
	        	contextOntology.addItem(item);
	        }
    	}
/*        		List<Pair<String, String>> thisItemAnnotations = itemAnnotations.get(item.getEntityId()).getAnnotations();
		        for(Pair<String, String> pair : thisItemAnnotations) {
		        	contextOntology.addAnnotation(item, pair.getKey(), pair.getValue());
		           	//SemanticExpansion.(pp.getSemanticExpansionParams(), annotation);
		        }
	        }
    	}
*/
    	int ntriples = 0;
    	AbstractMap<String, ItemResourceAnnotations> triples;
    	for(AnnotationSource as : annotationSources) {
    		triples = as.getAnnotations();
    		for(String subject : triples.keySet()) {
    			List<Pair<String, String>> subjectTriples = triples.get(subject).getAnnotations();
		        for(Pair<String, String> pair : subjectTriples) {
		        	contextOntology.addTriple(subject, pair.getKey(), pair.getValue());
		        	ntriples++;
		        }
    		}
    	}
    	System.out.println(" Triples: "+ ntriples);
        
    	
    	//System.out.println(" Size: "+ Runtime.getRuntime().totalMemory());
        //int i = 0;
    	for(UserItemEvent event : trainingData.getEvents().values()) {
	        contextOntology.addEvent(event);
        	//if(i++%10000==0) System.out.println(i +" "+event.getUser()+" "+event.getItem()+" "+event.getRatingValue()+" "+event.getTime()+ " Size:"+ Runtime.getRuntime().totalMemory());
    	}
	}

		
	private File convert2ItemModelFile(TrainingData trainingData) {
		String sroPreparedItemModelPath= "src/resources/main/temp/sro_preparedData_item.csv";
		AbstractMap<String, UserItemEvent> events = trainingData.getEvents();
		Set<String> userInItemModelFile = new HashSet<String>();
		
		try {
			OutputStream os = new FileOutputStream(sroPreparedItemModelPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			String user;
			for(UserItemEvent event : events.values()) {
					user = event.getUser();
					userInItemModelFile.add(user);
					writer.write(""+ user +","+event.getItem()+","+event.getRatingValue()+","+event.getTime()+"\n");
	    	}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new File(sroPreparedItemModelPath);
	}
	
	private List<File> convertAO2FoIMatrix(HashMap<String, HashMap<String, Float>> userAttributeMatrix) {
		List<File> fList;

		switch (pp.getFoIMatrixType()) {
		case "SingleFoIMatrix":
			fList = new ArrayList<File>();
			fList.add(convertAO2SingleFoIMatrix(userAttributeMatrix));
			break;
		
		case "MultipleFoIMatrixes":
			fList = convertAO2MultipleFoIMatrixes(userAttributeMatrix);
			break;
			
		default:
			fList = new ArrayList<File>();
			fList.add(convertAO2SingleFoIMatrix(userAttributeMatrix));
			break;
		}

		return fList;
    }

	
	private File convertAO2SingleFoIMatrix (HashMap<String, HashMap<String, Float>> attributeHitsByUser) {
		String singleFoIMatrixPath = "src/resources/main/temp/orbs_preparedData_singleFoIMatrix.csv";
		Float value;
		int nodeInt, nextInt = 0;
		String userId, userURI;
    	List<String> FoIHierarchyNodes = contextOntology.getFoIHierarchyNodes();
    	String prefix = pp.getContextOntologyParams().getOntologyURI()+"#audience";
		HashMap<String, Integer> FoIHierarchyNodeEnum = new HashMap<String, Integer>();
System.out.println("FoIHierarchyNodes: "+FoIHierarchyNodes.size());
		
		try {
			OutputStream os = new FileOutputStream(singleFoIMatrixPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");

	    	for(User user : contextOntology.getUsers()) {
	    		userId = user.getEntityId();
	    		userURI = prefix+userId;

	    		for(String node : FoIHierarchyNodes) {
	        		if(attributeHitsByUser.containsKey(userURI) 
	        		  && attributeHitsByUser.get(userURI).containsKey(node)) {
	        			value = attributeHitsByUser.get(userURI).get(node);
	        		} else {
	        			value = 0f;
	        		}
	        		//if(Integer.parseInt(userId)%1000==0) System.out.println(userId+" :: "+factor+" :: "+value);
					
	        		// Save
					if(!FoIHierarchyNodeEnum.containsKey(node)) {
						nodeInt = nextInt++;
						FoIHierarchyNodeEnum.put(node, nodeInt);
					} else {
						nodeInt = FoIHierarchyNodeEnum.get(node);
					}
					writer.write(userId+","+ nodeInt +","+ value+",0\n");
					//System.out.println(userId+","+ nodeInt +","+ value+",0");     		
	        	}
	        }
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    	
		// TODO Save FoIHierarchyNodeEnum on file orbs_FoIHierarchyNodeEnum.csv 
		saveFoIHierarchyNodeEnum(FoIHierarchyNodeEnum, "single");
		
	    return new File(singleFoIMatrixPath );
	}

	private List<File> convertAO2MultipleFoIMatrixes(HashMap<String, HashMap<String, Float>> attributeHitsByUser) {
		List<File> fileList = new ArrayList<File>();
		String multipleFoIMatrixesPath = "src/resources/main/temp/orbs_preparedData_";
		Float value;
		int nodeInt, nextInt = 0;
		String userId, userURI, FoIName;
    	List<String> FoIHierarchyNodes, FoIs = contextOntology.getFoIs();
    	String prefix = pp.getContextOntologyParams().getOntologyURI()+"#audience";
		HashMap<String, Integer> FoIHierarchyNodeEnum = new HashMap<String, Integer>();

		for(String FoI : FoIs) {
		//for(int i = 1;i>-1;i--) { String FoI = FoIs.get(i); 
			FoIHierarchyNodes = contextOntology.getFoIHierarchyNodes(FoI);
    		FoIName=FoI.substring(pp.getContextOntologyParams().getOntologyURI().length()+1);
    		
			try {
				OutputStream os = new FileOutputStream(multipleFoIMatrixesPath+FoIName+"Matrix.csv");
//    			System.out.println(multipleFoIMatrixesPath+FoIName+"Matrix.csv  || "+FoI);
				Writer writer = new OutputStreamWriter(os, "UTF-8");
	
		    	for(User user : contextOntology.getUsers()) {
		    		userId = user.getEntityId();
		    		userURI = prefix+userId;
	
		    		for(String node : FoIHierarchyNodes) {
//		    			System.out.println(node);
		        		if(attributeHitsByUser.containsKey(userURI) 
		        		  && attributeHitsByUser.get(userURI).containsKey(node)) {
		        			value = attributeHitsByUser.get(userURI).get(node);
		        		} else {
		        			value = 0f;
		        		}
		        		//if(Integer.parseInt(userId)%1000==0) System.out.println(userId+" :: "+factor+" :: "+value);
						
		        		// Save
						if(!FoIHierarchyNodeEnum.containsKey(node)) {
							nodeInt = nextInt++;
							FoIHierarchyNodeEnum.put(node, nodeInt);
						} else {
							nodeInt = FoIHierarchyNodeEnum.get(node);
						}
						writer.write(userId+","+ nodeInt +","+ value+",0\n");
//						System.out.println(userId+","+ nodeInt +","+ value+",0");     		
		        	}
		    		
		    		// TODO Save FoIHierarchyNodeEnum on file orbs_FoIHierarchyNodeEnum.csv 
		    		saveFoIHierarchyNodeEnum(FoIHierarchyNodeEnum, FoIName);
		    		FoIHierarchyNodeEnum.clear();
		    		nextInt = 0;
	    		}
	        
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileList.add(new File(multipleFoIMatrixesPath+FoIName+"Matrix.csv"));
		}
		
	    return fileList;
    }
	
	private void saveFoIHierarchyNodeEnum(HashMap<String, Integer> attributeMap, String fileNameSufix) {
		String orbsFoIHierarchyNodeEnumPath= "src/resources/main/temp/orbs_FoIHierarchyNodeEnum_"+fileNameSufix+".csv";
		
		try {
			OutputStream os = new FileOutputStream(orbsFoIHierarchyNodeEnumPath);
			Writer writer = new OutputStreamWriter(os, "UTF-8");

			String attributeName;
			int attributeNumber;
			for(Entry<String, Integer> entry1 : attributeMap.entrySet()) {
	    		attributeName = entry1.getKey();
	    		attributeNumber = entry1.getValue();
				writer.write(attributeNumber+","+ attributeName+"\n");
				//System.out.println(attributeNumber+" : "+ attributeName);
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}