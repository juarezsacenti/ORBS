package br.ufsc.lapesd.orbs.example.analyzer;

import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicPreparator;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class ClassicNeighborhoodAnalyzerEngine extends Engine {

	public ClassicNeighborhoodAnalyzerEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UCFClassicPreparator();
		this.algorithms.add(new ClassicNeighborhoodAnalyzerAlgorithm(eparams.getAlgorithmParams()));
	}
}
