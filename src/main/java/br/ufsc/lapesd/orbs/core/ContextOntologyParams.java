package br.ufsc.lapesd.orbs.core;

import java.util.List;

public class ContextOntologyParams {
	private String ontologyURI;	
	private String ontologyModelSpec;
	private String tripleStore;
	private String tripleStoreDirectory;
	private String repositoryName;
	private List<ImportedOntologyParams> importedOntologyParams;

	public String getTripleStore() {
		return tripleStore;
	}

	public String getOntologyURI() {
		return ontologyURI;
	}

	public String getOntologyModelSpec() {
		return ontologyModelSpec;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public String getTripleStoreDirectory() {
		return tripleStoreDirectory;
	}
	
	public List<ImportedOntologyParams> getImportedOntologyParams() {
		return importedOntologyParams;
	}
}
