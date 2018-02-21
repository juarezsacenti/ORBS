package br.ufsc.lapesd.sro.multiattributefc;

import static org.junit.Assert.*;

import java.util.AbstractMap;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.sro.example.multiattributefc.MultiAttributeFCEngine;
import br.ufsc.lapesd.sro.tokit.DataSource;
import br.ufsc.lapesd.sro.tokit.EngineParameter;
import br.ufsc.lapesd.sro.tokit.Query;
import br.ufsc.lapesd.sro.tokit.TrainingData;
import br.ufsc.lapesd.sro.tokit.User;

public class MultiAttributeFCEngineTest {

	@Test
	public void test() {
		try {
			System.out.println("#######################################################################");
			System.out.println("Training Engine");	
			EngineParameter eparams = new EngineParameter();
			MultiAttributeFCEngine engine = new MultiAttributeFCEngine(eparams);
			engine.train();
			
			System.out.println("#######################################################################");
			System.out.println("Quering Engine");
			DataSource datasource = new DataSource(eparams.getDataSouceParams());
			TrainingData td = datasource.readTraining();
			AbstractMap<String, User> users = td.getUsers();

			Query q;
			for(String str : users.keySet()) {
				q = new Query(str, 2, null, null, null);
				engine.query(q);
			}
			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
