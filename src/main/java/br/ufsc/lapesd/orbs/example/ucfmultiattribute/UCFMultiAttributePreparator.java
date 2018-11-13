package br.ufsc.lapesd.orbs.example.ucfmultiattribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.Preparator;
import br.ufsc.lapesd.orbs.tokit.PreparatorParams;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.UserItemEvent;

public class UCFMultiAttributePreparator implements Preparator {
	
	private List<DataSource> attributeSources;
	private Set<String> userInItemModelFile;

	public UCFMultiAttributePreparator(PreparatorParams pp) {
		this.attributeSources = new ArrayList<DataSource>();
    	for(DataSourceParams dsp : pp.getAnnotationSources()) {
    		this.attributeSources.add(new DataSource(dsp));
    	}
	}

	@Override
    public PreparedData prepare(TrainingData trainingData) {
		File itemModelFile = convert2ItemModelFile(trainingData);
		File attributeModelFile = convert2AttributeModelFile();

		return new UCFMultiAttributePreparedData(itemModelFile, attributeModelFile);
    }
	
	private File convert2ItemModelFile(TrainingData trainingData) {
		String path = "src/resources/main/temp/maucf_preparedData_item.csv";
		AbstractMap<String, UserItemEvent> events = trainingData.getEvents();
		userInItemModelFile = new HashSet<String>();
		
		try {
			OutputStream os = new FileOutputStream(path);
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
		return new File(path);
	}
	
	private File convert2AttributeModelFile() {
		AbstractMap<String, UserItemEvent> events;
		List<TrainingData> bruteAttributeModelFile = new ArrayList<TrainingData>();

    	for(DataSource ds : attributeSources) {
    		TrainingData td = ds.readTraining();
    		bruteAttributeModelFile.add(td);
    	}
		
		String path = "src/resources/main/temp/maucf_preparedData_attribute.csv";
		try {
			OutputStream os = new FileOutputStream(path);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			System.out.println(userInItemModelFile.size());
			for(String user : userInItemModelFile) {
				for(TrainingData td : bruteAttributeModelFile) {
					events = td.getEvents();
					for(UserItemEvent event : events.values()) {
						//System.out.println(""+ user +","+event.getUser());
						if(Integer.parseInt(event.getUser()) == Integer.parseInt(user))	{
							writer.write(""+ user +","+event.getItem()+","+event.getRatingValue()+","+0+"\n");
							//System.out.println(""+ user +","+event.getItem()+","+event.getRatingValue()+","+0);
						}
					}
				}
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new File(path);
	}
}
