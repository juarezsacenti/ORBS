package br.ufsc.lisa.sedim.core.io;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.TDBFactory;

public class TDBAccess extends TripleStoreAccess {
	private static final Logger LOG = Logger.getLogger(TDBAccess.class.getName());
	private static final String TDBCLASSNAME = "org.apache.jena.tdb.TDBFactory";
	private String tdbDirectory = "../TDB/";
	
	public TDBAccess(String tripleStoreDirectory) {
		this.tdbDirectory = tripleStoreDirectory;
	}

	public void saveInRepository(String repositoryName, OntModel ontologyModel) throws Exception {
		if(!hasSavedRepositoryInFile(repositoryName)) {createRepository(repositoryName);}
		
		Dataset dataset = this.getConnection(repositoryName);
		ontologyModel.commit();

		dataset.begin(ReadWrite.WRITE);
	    Model model = dataset.getDefaultModel();
		model.add(ontologyModel.getRawModel());
		dataset.commit();
		dataset.end();

		ontologyModel.begin();
	}
	
	public OntModel loadFromRepository(String repositoryName) throws Exception {
		OntModel ontologyModel = ModelFactory.createOntologyModel();
		if(!hasSavedRepositoryInFile(repositoryName)) {
			createRepository(repositoryName);
		} else {
			Dataset dataset = this.getConnection(repositoryName);
			ontologyModel.begin();
	
			dataset.begin(ReadWrite.READ);
		    Model model = ontologyModel.getRawModel();
			model.add(dataset.getDefaultModel());
			dataset.end();
			
			ontologyModel.commit();
			ontologyModel.begin();
		}
		return ontologyModel;
	}

	private boolean hasSavedRepositoryInFile(String repositoryName) {
		File file = new File(this.tdbDirectory + repositoryName);
		return (file.exists() && !file.isDirectory());
	}
	
	private void createRepository(String fileName) throws Exception {
//		File repo = new File(this.tdbDirectory + fileName);
//		repo.mkdirs();
		
		File source = new File(this.tdbDirectory + "tdb-default.cfg");
		File dest = new File(this.tdbDirectory + fileName + "/tdb.cfg");
		
	    Files.copy(source.toPath(), dest.toPath());
	}

	public Dataset getConnection() {
		return getConnection("Default");
	}
	
	/**
	 * Get the connection to the database
	 * @return the connection or null if errors occured
	 */
	public Dataset getConnection(String repo) {
		Dataset dataset;

		try {
			Class.forName(TDBCLASSNAME);
		} catch (ClassNotFoundException e) {
			LOG.warning(e.fillInStackTrace().toString());
			return null;
		}
		
		try	{
			dataset = TDBFactory.createDataset(tdbDirectory + repo);
		} catch (TDBException e) {
			LOG.warning(e.fillInStackTrace().toString());
			return null;
		} 

		return dataset;
	}
	
	public String getTDBDirectory() {
		return tdbDirectory;
	}
}
