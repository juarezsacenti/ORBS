package br.ufsc.lapesd.sro.example.userfc;

import org.apache.mahout.cf.taste.model.DataModel;

import br.ufsc.lapesd.sro.tokit.Model;

public class UserFCModel implements Model {
	private DataModel model;

	public UserFCModel(DataModel model) {
		this.model = model;
	}
	
	public DataModel getModel() {
		return model;
	}
}
