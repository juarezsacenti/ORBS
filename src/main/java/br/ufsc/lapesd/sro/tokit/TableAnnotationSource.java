package br.ufsc.lapesd.sro.tokit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

import br.ufsc.lapesd.sro.core.AnnotationSource;

public class TableAnnotationSource implements AnnotationSource {
	
	private final DataSourceParams dsp;
	
	public TableAnnotationSource(DataSourceParams dsp) {
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
        String sourceLocation = dsp.getSourceLocation();
        try (BufferedReader br = new BufferedReader(new FileReader(sourceLocation))) {
        	if(!dsp.hasHeaderLine()) {
        		throw new Exception("TableAnnotationSource file without header.");
            } else {
            	line = br.readLine();
           		String[] column = line.split(regex, -1);             
                int length = column.length;
                String[] annotationType = new String[length-1];
                for(int i = 1; i < length; i++) {
                    annotationType[i-1] = column[i].trim();                 	
                }
            
	        	while ((line = br.readLine()) != null) {
	        		column = line.split(regex, -1);             
	
                	ItemResourceAnnotations annotations = new ItemResourceAnnotations(column[0]);
	                
	                length = column.length;
	                for(int i = 1; i < length; i++) {
	                    column[i] = column[i].trim();
	                    
	               		String[] values = column[i].split("::", -1);
	               		int jLength = values.length;
	               		for(int j = 0; j < jLength; j++) {
	               			annotations.add(annotationType[i-1], values[j]);
	               		}
	                }
	                itemAnnotationsRDD.put(column[0], annotations);
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
