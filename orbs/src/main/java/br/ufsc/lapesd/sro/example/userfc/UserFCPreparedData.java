package br.ufsc.lapesd.sro.example.userfc;

import java.io.File;

import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.TrainingData;

public class UserFCPreparedData extends PreparedData {

	private File file;

	public UserFCPreparedData(File file) {
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
