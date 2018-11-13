package br.ufsc.lapesd.orbs.example.ucfmultiattribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.lapesd.orbs.tokit.PreparedData;
import br.ufsc.lapesd.orbs.tokit.TrainingData;

public class UCFMultiAttributePreparedData extends PreparedData {

	private File itemModelFile;
	private List<File> FoIMatrixesFiles;

	public UCFMultiAttributePreparedData(File itemModelFile, List<File> FoIMatrixesFiles) {
		super();
		this.itemModelFile = itemModelFile;
		this.FoIMatrixesFiles = FoIMatrixesFiles;
	}
	
	public UCFMultiAttributePreparedData(File itemModelFile, File FoIMatrixFile) {
		super();
		this.itemModelFile = itemModelFile;
		this.FoIMatrixesFiles = new ArrayList<File>();
		this.FoIMatrixesFiles.add(FoIMatrixFile);
	}
	
	public File getItemModelFile() {
		return itemModelFile;
	}

	public List<File> getFoIMatrixesFiles() {
		return FoIMatrixesFiles;
	}

	@Override
	public TrainingData getTrainingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toPrint() {
		System.out.println(itemModelFile.toString());
		System.out.println(FoIMatrixesFiles.toString());
	}

}
