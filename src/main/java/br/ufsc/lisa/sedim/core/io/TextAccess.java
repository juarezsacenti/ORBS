package br.ufsc.lisa.sedim.core.io;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;

/*
 *	TODO Defines object to save a KnowledgeBase object in a file rdf/xml,n3,turtle... format
 * */
public class TextAccess extends TripleStoreAccess {
	private String fileDirectory = "../Text/";
	private static final String rdfFormat = "RDF/XML-ABBREV";

	public TextAccess(String tripleStoreDirectory) {
		this.fileDirectory = tripleStoreDirectory;
	}
	
	public void saveInRepository(String repositoryName, OntModel ontologyModel) throws Exception {
        FileWriter fw = null;

		if(!hasSavedRepositoryInFile(repositoryName)) {createRepository(repositoryName);}
		
//		ontologyModel.commit();
        System.out.println("BEGIN: Ont --> File");

        fw = new FileWriter(this.fileDirectory + repositoryName);
        ontologyModel.write(fw, rdfFormat);

        System.out.println("END: Ont --> File");
	
//		ontologyModel.begin();
	}
	
	public OntModel loadFromRepository(String repositoryName) throws Exception {
		OntModel ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		if(!hasSavedRepositoryInFile(repositoryName)) {
			createRepository(repositoryName);
		} else {
		    try 
		    {
				InputStream in = FileManager.get().open(this.fileDirectory + repositoryName);
		        try 
		        {
		            ontologyModel.read(in, null);
		        } 
		        catch (Exception e) 
		        {
		            e.printStackTrace();
		        }
//		        LOGGER.info("Ontology " + ontoFile + " loaded.");
		    } 
		    catch (JenaException je) 
		    {
		        System.err.println("ERROR" + je.getMessage());
		        je.printStackTrace();
		        System.exit(0);
		    }
		}
		return ontologyModel;
	}

	private boolean hasSavedRepositoryInFile(String repositoryName) {
		File file = new File(this.fileDirectory + repositoryName);
		return (file.exists() && !file.isDirectory());
	}
	
	private void createRepository(String repositoryName) throws Exception {
		File repo = new File(this.fileDirectory + repositoryName);
		repo.createNewFile();
	}

	public String getFileDirectory() {
		return fileDirectory;
	}
}