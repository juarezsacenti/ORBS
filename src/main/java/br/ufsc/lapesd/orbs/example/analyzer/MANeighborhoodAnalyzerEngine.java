package br.ufsc.lapesd.orbs.example.analyzer;

import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributePreparator;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class MANeighborhoodAnalyzerEngine extends Engine {

	public MANeighborhoodAnalyzerEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UCFMultiAttributePreparator(eparams.getPreparatorParams());
		this.algorithms.add(new MANeighborhoodAnalyzerAlgorithm(eparams.getAlgorithmParams()));
	}

}
