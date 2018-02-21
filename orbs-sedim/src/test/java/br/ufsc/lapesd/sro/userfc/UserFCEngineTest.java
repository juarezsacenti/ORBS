package br.ufsc.lapesd.sro.userfc;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.AbstractMap;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.sro.example.userfc.UserFCEngine;
import br.ufsc.lapesd.sro.tokit.DataSource;
import br.ufsc.lapesd.sro.tokit.EngineParameter;
import br.ufsc.lapesd.sro.tokit.Query;
import br.ufsc.lapesd.sro.tokit.TrainingData;
import br.ufsc.lapesd.sro.tokit.User;

public class UserFCEngineTest {

	@Test
	public void test() {
		try {
			EngineParameter eparams = new EngineParameter();
			UserFCEngine engine = new UserFCEngine(eparams);
			engine.train();
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
