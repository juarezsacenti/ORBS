package br.ufsc.lisa.sedim.core.io;

import org.apache.jena.ontology.OntModel;

public abstract class TripleStoreAccess {
	
	public void saveInRepository(String repositoryName, OntModel ontologyModel) throws Exception {
        throw new IllegalStateException(
                "There isn't a saveInRepository method in this subclass.");
	}
	
	public OntModel loadFromRepository(String repositoryName) throws Exception {
        throw new IllegalStateException(
                "There isn't a loadFromRepository method in this subclass.");
	}
}