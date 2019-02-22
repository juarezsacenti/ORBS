package br.ufsc.lapesd.sro.userfc;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.AbstractMap;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.User;

public class UserFCEnginePredictionTest {

	@Test
	public void test() {
		try {
/*			System.out.println("#######################################################################");
			System.out.println("Training Engine");
			EngineParameter eparams = new EngineParameter("src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-25.json");
			UCFClassicEngine engine = new UCFClassicEngine(eparams);
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

			engine.query(new Query("2116", 10, null, null, null));
			engine.query(new Query("271", 10, null, null, null));
			engine.query(new Query("637", 10, null, null, null));
			
			System.out.println("\n###################  INTERACTS WITH 16 ITEM:  ###################");
			engine.query(new Query("5458", 10, null, null, null));
			
			System.out.println("\n###################  INTERACTS WITH 5 ITEM:  ###################");
			engine.query(new Query("203", 10, null, null, null));
			
			System.out.println("\n###################  INTERACTS WITH 2 ITEM:  ###################");
			engine.query(new Query("5487", 10, null, null, null));
			
			System.out.println("###################  INTERACTS WITH 1 ITEM:  ###################");
			engine.query(new Query("206", 10, null, null, null));
			engine.query(new Query("5496", 10, null, null, null));
*/			
			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
