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

public class UserFCEngineTest {

	@Test
	public void test() {
		try {
			EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-ClassicUCF.json");
			UCFClassicEngine engine = new UCFClassicEngine(eparams);
			engine.train();

			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
