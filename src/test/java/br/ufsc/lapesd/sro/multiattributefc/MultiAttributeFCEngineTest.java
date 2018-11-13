package br.ufsc.lapesd.sro.multiattributefc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class MultiAttributeFCEngineTest {

	@Test
	public void test() {
		try {
			EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-MultiAttributeUCF.json");
			UCFMultiAttributeEngine engine = new UCFMultiAttributeEngine(eparams);
			engine.train();

			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
