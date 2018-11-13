package br.ufsc.lapesd.orbs.example.ucfclassic;

import java.io.File;

import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;

public class UCFClassicPreparedData extends PreparedData {

	private File file;

	public UCFClassicPreparedData(File file) {
		super();
		this.file = file;
	}

	@Override
	public void toPrint() {
		System.out.println(file.toString());
	}

	public File getFile() {
		return file;
	}

	@Override
	public TrainingData getTrainingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
