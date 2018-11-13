package br.ufsc.lapesd.sro.core;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.AbstractMap;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.TrainingData;
import br.ufsc.lapesd.orbs.tokit.User;

public class SROEnginePredictionTest {

	@Test
	public void test() {
		try {
			System.out.println("#######################################################################");
			System.out.println("Training Engine");
			EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-SRO2hierarchies.json");
			//EngineParameter eparams = new EngineParameter("src/resources/main/SROengine.json");
			UCFProposalEngine engine = new UCFProposalEngine(eparams);
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
/*			engine.query(new Query("549", 10, null, null, null));
			engine.query(new Query("1861", 10, null, null, null));
			engine.query(new Query("3272", 10, null, null, null));

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
