package br.ufsc.lapesd.sro.example.multiattributefc;

import br.ufsc.lapesd.sro.tokit.Engine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;

public class MultiAttributeFCEngine extends Engine {

	public MultiAttributeFCEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new MultiAttributeFCPreparator();
		this.algorithms.add(new MultiAttributeFCAlgorithm(eparams.getAlgorithmParams()));
	}

}
