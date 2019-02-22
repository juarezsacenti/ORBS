package br.ufsc.lapesd.sro.core;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;


public class SROEngineTest {

	@Test
	public void test() {
		try {
/*	    	System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
	    	EngineParameter eparams = new EngineParameter("src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-100.json");
	    	UCFProposalEngine engine = new UCFProposalEngine(eparams);
			engine.train();
*/
			assertFalse(false);
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
