package br.ufsc.lapesd.sro.example.userfc;

import br.ufsc.lapesd.sro.tokit.Engine;
import br.ufsc.lapesd.sro.tokit.EngineParameter;

public class UserFCEngine extends Engine {

	public UserFCEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UserFCPreparator();
		this.algorithms.add(new UserFCAlgorithm(eparams.getAlgorithmParams()));
	}
}
