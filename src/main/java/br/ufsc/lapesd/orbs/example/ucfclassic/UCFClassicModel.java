package br.ufsc.lapesd.orbs.example.ucfclassic;

import org.apache.mahout.cf.taste.model.DataModel;

import br.ufsc.lapesd.orbs.tokit.Model;

public class UCFClassicModel implements Model {
	private DataModel model;

	public UCFClassicModel(DataModel model) {
		this.model = model;
	}
	
	public DataModel getModel() {
		return model;
	}
}
