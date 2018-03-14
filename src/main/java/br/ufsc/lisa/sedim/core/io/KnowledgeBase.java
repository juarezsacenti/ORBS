package br.ufsc.lisa.sedim.core.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class KnowledgeBase {
	private OntModel ontologyModel;
	private TripleStoreAccess tripleStoreAccess;
	private String ontologyName;
	
	public KnowledgeBase(String tripleStore, String tripleStoreDirectory, String ontologyName, String ontologyModelSpec) {
		this.ontologyName = ontologyName;
		switch(ontologyModelSpec) {
		case "OWL_MEM":
			this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			break;
		case "OWL_DL_MEM":
			this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			break;
		default:			
			this.ontologyModel = ModelFactory.createOntologyModel();
		}
		this.ontologyModel.createOntology( ontologyName );
		this.ontologyModel.setDynamicImports(true);
		this.ontologyModel.setNsPrefix("mysro", "http://www.lapesd.inf.ufsc.br/projetos/sro/mysro.owl#");
		this.ontologyModel.setNsPrefix("recont", "http://www.lapesd.inf.ufsc.br/ontology/recont.owl#");
		this.ontologyModel.setNsPrefix("conont", "http://www.lapesd.inf.ufsc.br/projetos/sro/contextOntology.owl#");
		
		switch(tripleStore) {
		case "TDB":
			this.tripleStoreAccess = new TDBAccess(tripleStoreDirectory);
			break;
		case "Text":
			this.tripleStoreAccess = new TextAccess(tripleStoreDirectory);
			break;
		default:			
			this.tripleStoreAccess = new TextAccess(tripleStoreDirectory);
		}
	}
	
	public OntModel getOntologyModel() {
		return ontologyModel;
	}
	
	public void saveKBinFile(String repositoryName) {
		try {
			this.tripleStoreAccess.saveInRepository(repositoryName, this.ontologyModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadKBfromFile(String repositoryName) {
		try {
			this.ontologyModel = this.tripleStoreAccess.loadFromRepository(repositoryName);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void importOntology(String ontologyURI, String ontologyLocation, String ontologyFormat) {
		this.ontologyModel.setNsPrefix("mysro", ontologyURI+"#");
		OntDocumentManager dm = this.ontologyModel.getDocumentManager();
		dm.addAltEntry( ontologyURI, "file:" + ontologyLocation );
		Ontology ont = this.ontologyModel.getOntology(ontologyName);
		ont.addImport( this.ontologyModel.createResource(ontologyURI));
	}

	public void addObjectStatement(String subject, String property, String object) {
		Resource s = this.ontologyModel.getResource(subject);
		Property p = this.ontologyModel.getProperty(property);
		Resource o = this.ontologyModel.getResource(object);
		//System.out.println(s.toString() + " , " + p.toString() + " , " + o.toString());
		this.ontologyModel.add(s, p, o);
	}
	
	public void addLiteralStatement(String subject, String property, float literal) {
		Resource s = this.ontologyModel.getResource(subject);
		Property p = this.ontologyModel.getProperty(property);
		//System.out.println(s.toString() + " , " + p.toString() + " , " + literal);
		this.ontologyModel.addLiteral(s, p, literal);
	}

	public void addLiteralStatement(String subject, String property, int literal) {
		Resource s = this.ontologyModel.getResource(subject);
		Property p = this.ontologyModel.getProperty(property);
		//System.out.println(s.toString() + " , " + p.toString() + " , " + literal);
		this.ontologyModel.addLiteral(s, p, literal);
	}
	
	public List<RDFTriple> getStatements(String subject, String property, String object) {
		List<RDFTriple> triples = new ArrayList<RDFTriple>();
		Resource sub, obj;
		Property pro;
		if(subject == null) { sub = null;} else { sub = ontologyModel.getResource(subject); }
		if(property == null) { pro = null;} else { pro = ontologyModel.getProperty(property); }
		if(object == null) { obj = null;} else { obj = ontologyModel.getResource(object); }
		
		Selector selector = new SimpleSelector(sub, pro, (RDFNode) obj);
		
		StmtIterator si = ontologyModel.listStatements(selector);
		Statement stmt;
		RDFTriple triple;
		while(si.hasNext()) {
			stmt = si.nextStatement();
			triple = new RDFTriple(
					stmt.getSubject().toString(),
					stmt.getPredicate().toString(),
					stmt.getObject().toString()
					);  
			triples.add(triple);
		}
			
		return triples;
	}

	public HashSet<String> getAncestors(String hierarchyProperty, String otherFather) {
		HashSet<String> ancestors = new HashSet<String>();
		String ancestor;
		
        Iterator<RDFTriple> ancestorsIt = getStatements(otherFather, hierarchyProperty, null).iterator();
        while(ancestorsIt.hasNext()) {
        	ancestor = ancestorsIt.next().getObject();
    		if(!ancestors.contains(ancestor)) {
    			ancestors.add(ancestor);
    			ancestors.addAll(getAncestors(hierarchyProperty, ancestor));
    		}
        }
		return ancestors;
	}
	
	public long getSize() {
		return ontologyModel.size();
	}

	public void removeStatement(RDFTriple triple) {
		Resource s = this.ontologyModel.getResource(triple.getSubject());
		Property p = this.ontologyModel.getProperty(triple.getPredicate());
		Resource o = this.ontologyModel.getResource(triple.getObject());
		this.ontologyModel.remove(s, p, o);
	}
	
	
}
