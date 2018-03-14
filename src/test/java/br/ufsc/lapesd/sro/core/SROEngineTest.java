package br.ufsc.lapesd.sro.core;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.sro.core.SROEngine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;


public class SROEngineTest {

	@Test
	public void test() {
		try {
	    	System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
	    	EngineParameter eparams = new EngineParameter("src/resources/main/Mov1M/Mov1M-SRO.json");
	    	SROEngine engine = new SROEngine(eparams);
			engine.train();

			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
