package br.ufsc.lapesd.orbs.example.ucfclassic;

import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class UCFClassicEngine extends Engine {

	public UCFClassicEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UCFClassicPreparator();
		this.algorithms.add(new UCFClassicAlgorithm(eparams.getAlgorithmParams()));
	}
}
