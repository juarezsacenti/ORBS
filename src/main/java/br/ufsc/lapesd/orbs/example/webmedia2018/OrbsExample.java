package br.ufsc.lapesd.orbs.example.webmedia2018;

import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class OrbsExample {

	private final static String outputFile = "src/resources/main/examples/outputs.txt";
	
	private final static String[] engines = {
		"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-100.json",               // 0
		"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-25.json",                // 1
		"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-100.json",  // 2
		"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-25.json",   // 3
		"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-100.json",        // 4
		"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-25.json",         // 5
		"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-100.json",         // 6
		"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-25.json",          // 7
		"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-100.json",	   // 8
		"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-25.json"};    // 9
	
	public static void main(String[] args) {
		String enginePath = engines[1];

		System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
    	EngineParameter eparams = new EngineParameter(enginePath);
    	Engine engine;
    	
    	System.out.println(enginePath); 
    	switch (eparams.getEngineType()) {
			case "br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine":  
				engine = new UCFClassicEngine(eparams);
				break;
    		case "br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine":  
    			engine = new UCFMultiAttributeEngine(eparams);
    			break;			
			case "br.ufsc.lapesd.orbs.core.ProposalEngine":
				engine = new UCFProposalEngine(eparams);
				break;				
			default:
				System.out.println("Declared engine no expected. Using ClassicNeighborhoodAnalyzerEngine instead.");
				engine = new UCFClassicEngine(eparams);
				break;
    	}
		engine.train();
		engine.nativeEvaluation();	
	}
}
