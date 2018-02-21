package br.ufsc.lapesd.sro.core;

import br.ufsc.lapesd.sro.example.multiattributefc.MultiAttributeFCAlgorithm;
import br.ufsc.lapesd.sro.tokit.Engine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;

public class SROEngine extends Engine {

	public SROEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new SROPreparator(eparams.getPreparatorParams());
		this.algorithms.add(new MultiAttributeFCAlgorithm(eparams.getAlgorithmParams()));
	}

}
