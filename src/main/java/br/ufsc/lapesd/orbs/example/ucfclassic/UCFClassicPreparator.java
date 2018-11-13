package br.ufsc.lapesd.orbs.example.ucfclassic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;

import br.ufsc.lapesd.orbs.tokit.Preparator;
import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.UserItemEvent;


public class UCFClassicPreparator implements Preparator {

	@Override
	public PreparedData prepare(TrainingData trainingData) {
		String path = "src/resources/main/temp/cucf_preparedData.csv";
		AbstractMap<String, UserItemEvent> events = trainingData.getEvents();

		try {
			OutputStream os = new FileOutputStream(path);
			Writer writer = new OutputStreamWriter(os, "UTF-8");
			
			for(UserItemEvent event : events.values()) {
					writer.write(""+event.getUser()+","+event.getItem()+","+event.getRatingValue()+","+event.getTime()+"\n");
	    	}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File file = new File(path);
		return new UCFClassicPreparedData(file);
    }
}
