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

public class MultiAttributeFCEnginePredictionTest {

	@Test
	public void test() {
		try {
			System.out.println("#######################################################################");
			System.out.println("Training Engine");	
			EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-MultiAttributeUCF.json");
			MultiAttributeFCEngine engine = new MultiAttributeFCEngine(eparams);
			engine.train();
			
			System.out.println("#######################################################################");
			System.out.println("Quering Engine");
/*			DataSource datasource = new DataSource(eparams.getDataSouceParams());
			TrainingData td = datasource.readTraining();
			AbstractMap<String, User> users = td.getUsers();

			Query q;
			for(String str : users.keySet()) {
				q = new Query(str, 10, null, null, null);
				engine.query(q);
			}
			*/
			engine.query(new Query("2116", 10, null, null, null));
/*			engine.query(new Query("2223", 10, null, null, null));
			engine.query(new Query("2077", 10, null, null, null));
			engine.query(new Query("699", 10, null, null, null));
*/
			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
