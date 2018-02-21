package br.ufsc.lapesd.sro.example.multiattributefc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import br.ufsc.lapesd.sro.tokit.DataSource;
import br.ufsc.lapesd.sro.tokit.DataSourceParams;
import br.ufsc.lapesd.sro.tokit.Preparator;
import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.TrainingData;
import br.ufsc.lapesd.sro.tokit.UserItemEvent;

public class MultiAttributeFCPreparator implements Preparator {

	private Set<String> userInItemModelFile;

	@Override
    public PreparedData prepare(TrainingData trainingData) {
		File itemModelFile = convert2ItemModelFile(trainingData);
		File attributeModelFile = convert2AttributeModelFile();

		return new MultiAttributeFCPreparedData(itemModelFile, attributeModelFile);
    }
	
	private File convert2ItemModelFile(TrainingData trainingData) {
		String path = "src/resources/main/mafc_itemModel.csv";
		AbstractMap<String, UserItemEvent> events = trainingData.getEvents();
		userInItemModelFile = new HashSet<String>();
		
		try {
			OutputStream os = new FileOutputStream(path);
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
		return new File(path);
	}
	
	private File convert2AttributeModelFile() {
		AbstractMap<String, UserItemEvent> events;
		String path = "src/resources/test/userGenreNormalized-dataset.csv";
		DataSourceParams params = new DataSourceParams("MultiAttributeFCRS", path, "\"", ",", false);
		TrainingData bruteGenreModelFile = new DataSource(params).readTraining();

		path = "src/resources/main/mafc_attributeModel.csv";
		try {
			OutputStream os = new FileOutputStream(path);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			events = bruteGenreModelFile.getEvents();
			String user;
			for(UserItemEvent event : events.values()) {
				user = event.getUser();
				if(userInItemModelFile.contains(user))	{
					writer.write(""+ user +","+event.getItem()+","+event.getRatingValue()+","+System.currentTimeMillis()+"\n");
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
