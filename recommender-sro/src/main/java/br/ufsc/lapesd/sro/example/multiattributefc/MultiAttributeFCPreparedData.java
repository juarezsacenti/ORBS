package br.ufsc.lapesd.sro.example.multiattributefc;

import java.io.File;

import br.ufsc.lapesd.sro.tokit.PreparedData;
import br.ufsc.lapesd.sro.tokit.TrainingData;

public class MultiAttributeFCPreparedData extends PreparedData {

	private File itemModelFile;
	private File genreModelFile;

	public MultiAttributeFCPreparedData(File itemModelFile, File genreModelFile) {
		super();
		this.itemModelFile = itemModelFile;
		this.genreModelFile = genreModelFile;
	}
	
	public File getItemModelFile() {
		return itemModelFile;
	}

	public File getGenreModelFile() {
		return genreModelFile;
	}

	@Override
	public TrainingData getTrainingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toPrint() {
		System.out.println(itemModelFile.toString());		
		System.out.println(genreModelFile.toString());		
	}

}
