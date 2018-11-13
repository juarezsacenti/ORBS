package br.ufsc.lapesd.orbs.example.ucfmultiattribute;

import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class UCFMultiAttributeEngine extends Engine {

	public UCFMultiAttributeEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UCFMultiAttributePreparator(eparams.getPreparatorParams());
		this.algorithms.add(new UCFMultiAttributeAlgorithm(eparams.getAlgorithmParams()));
	}

}
