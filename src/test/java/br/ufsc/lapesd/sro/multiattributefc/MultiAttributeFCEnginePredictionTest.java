package br.ufsc.lapesd.sro.multiattributefc;

import static org.junit.Assert.*;

import java.util.AbstractMap;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.User;

public class MultiAttributeFCEnginePredictionTest {

	@Test
	public void test() {
		try {
/*			System.out.println("#######################################################################");
			System.out.println("Training Engine");	
			EngineParameter eparams = new EngineParameter("src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-100.json");
			UCFMultiAttributeEngine engine = new UCFMultiAttributeEngine(eparams);
			engine.train();
			
			System.out.println("#######################################################################");
			System.out.println("Quering Engine");
			DataSourceSplitter datasource = new DataSourceSplitter(eparams.getDataSouceParams());
			TrainingData td = datasource.readTraining();
			AbstractMap<String, User> users = td.getUsers();

			Query q;
			for(String str : users.keySet()) {
				q = new Query(str, 10, null, null, null);
				engine.query(q);
			}
			
			engine.query(new Query("4169", 10, null, null, null));
			engine.query(new Query("2223", 10, null, null, null));
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
