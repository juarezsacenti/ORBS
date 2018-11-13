package br.ufsc.lapesd.orbs.core;

import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeAlgorithm;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;

public class UCFProposalEngine extends Engine {

	public UCFProposalEngine(EngineParameter eparams) {
		super(eparams);
		this.preparator = new UCFProposalPreparator(eparams.getPreparatorParams());
		this.algorithms.add(new UCFMultiAttributeAlgorithm(eparams.getAlgorithmParams()));
	}

}
