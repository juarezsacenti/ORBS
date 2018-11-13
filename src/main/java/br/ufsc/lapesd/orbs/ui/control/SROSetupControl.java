package br.ufsc.lapesd.orbs.ui.control;

import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class SROSetupControl {
	private EngineParameter eparams;
	private UCFProposalEngine activeEngine;
	
	public EngineParameter onLoadSROSettings() {
		System.out.println("Load SRO Settings");
		// load from engine.json
		this.eparams = new EngineParameter();
		// update field value of view
		return eparams;
	}
	
	public void onSaveSROSettings(String[] input) {
		System.out.println("Save SRO Settings");
		// get field values from view
		this.eparams = new EngineParameter(input);
		// save in engine.json
		eparams.save("src/resources/main/engine_save.json", "UTF-8");
	}
	
	public void onStartTraining(String[] input) {
		System.out.println("Start Training");
		this.eparams = new EngineParameter(input);
		this.activeEngine = new UCFProposalEngine(eparams);
		activeEngine.train();
	}

}
