package br.ufsc.lapesd.orbs.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

import br.ufsc.lapesd.orbs.core.AnnotationSource;
import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.ItemResourceAnnotations;

public class TTLAnnotationSource implements AnnotationSource {
	
	private final DataSourceParams dsp;

	public TTLAnnotationSource(DataSourceParams dsp) {
		this.dsp = dsp;
	}

	   public AbstractMap<String, ItemResourceAnnotations> getAnnotations() {
			AbstractMap<String, ItemResourceAnnotations> itemAnnotationsRDD = new HashMap<String,ItemResourceAnnotations>();

	        String regex;
	        String delimiter = dsp.getDelimiter();
	        if(dsp.getEnclosure().equals("'")) {
		        regex = delimiter +"(?=(?:[^']*'[^']*')*[^']*$)";
	        } else {
		        regex = delimiter +"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
			}
	        
	        String line = "";
	        String sourceLocation = dsp.getSourceLocation().get(0);
	        try (BufferedReader br = new BufferedReader(new FileReader(sourceLocation))) {
           		String[] column;
           		String s, p, o;
                ItemResourceAnnotations annotations;
	        	while ((line = br.readLine()) != null) {
	        		column = line.split(regex, -1);
	        		
	        		s = column[0].substring(1, column[0].length()-1);
	        		p = column[1].substring(1, column[1].length()-1);
	                o = column[2].substring(1, column[2].length()-1);

	                if(!itemAnnotationsRDD.containsKey(s)) {
		        		annotations = new ItemResourceAnnotations(s);
		                annotations.add(p, o);
		        		itemAnnotationsRDD.put(s, annotations);
	                } else {
	                	annotations = itemAnnotationsRDD.get(s);
		                annotations.add(p, o);
	                }
	            }
			} catch(IOException e) {
	            e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

	        return itemAnnotationsRDD;
	    }
}
