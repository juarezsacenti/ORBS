package br.ufsc.lapesd.sro.multiattributefc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.sro.example.multiattributefc.MultiAttributeFCEngine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;

public class MultiAttributeFCEngineTest {

	@Test
	public void test() {
		try {
			EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-MultiAttributeUCF.json");
			MultiAttributeFCEngine engine = new MultiAttributeFCEngine(eparams);
			engine.train();

			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
