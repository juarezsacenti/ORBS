package br.ufsc.lapesd.sro.ui.control;

import br.ufsc.lapesd.sro.core.SROEngine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;

public class SROSetupControl {
	private EngineParameter eparams;
	private SROEngine activeEngine;
	
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
		this.activeEngine = new SROEngine(eparams);
		activeEngine.train();
	}

}
