package br.ufsc.lapesd.sro.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.ufsc.lapesd.sro.tokit.Item;
import br.ufsc.lapesd.sro.tokit.User;
import br.ufsc.lapesd.sro.tokit.UserItemEvent;
import br.ufsc.lisa.sedim.core.io.KnowledgeBase;
import br.ufsc.lisa.sedim.core.io.RDFTriple;

public class ContextOntology {
	private KnowledgeBase kb;
	private String recommenderTaskOntologyURI;
	private String ontologyURI;
	private String repositoryName;

	private final HashSet<User> userIndex;
	private final HashSet<Item> itemIndex;
	private int eventCount = 0;
	
	public ContextOntology(ContextOntologyParams cop) {
		this.repositoryName = cop.getRepositoryName();
		this.recommenderTaskOntologyURI = "http://www.lapesd.inf.ufsc.br/ontology/recont.owl";
		this.ontologyURI = cop.getOntologyURI();
		this.kb = new KnowledgeBase(cop.getTripleStore(), cop.getTripleStoreDirectory(), ontologyURI, cop.getOntologyModelSpec());
		this.userIndex = new HashSet<User>();
		this.itemIndex = new HashSet<Item>();
		for(ImportedOntologyParams iop : cop.getImportedOntologyParams()) {
			this.kb.importOntology(iop.getOntologyURI(), iop.getFileLocation(), iop.getFileFormat());
		}
	}

	public void addUser(User user) {
		if(!userIndex.contains(user)) {
			userIndex.add(user);
			String subject = this.ontologyURI+ "#" + "audience" + user.getEntityId();
			String property = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
			String object = this.recommenderTaskOntologyURI + "#" + "Audience";
			kb.addObjectStatement(subject ,  property , object);
		}
	}

	public void addItem(Item item) {
		if(!itemIndex.contains(item)) {
			itemIndex.add(item);
			String subject = this.ontologyURI+ "#" + "item" + item.getEntityId();
			String property = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
			String object = this.recommenderTaskOntologyURI + "#" + "Item";
			kb.addObjectStatement(subject ,  property , object);
		}
	}

	public void addEvent(UserItemEvent event) {	
		String interactionType = this.ontologyURI + "#" + "Rate"; //event.getType();
		String interaction = this.ontologyURI+ "#" + "rate" + ++eventCount;
		String user = this.ontologyURI+ "#" + "audience" + event.getUser();
		String item = this.ontologyURI+ "#" + "item" + event.getItem();
		String p1 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String p2 = this.recommenderTaskOntologyURI + "#" + "doInteraction"; 
		String p3 = this.recommenderTaskOntologyURI + "#" + "happensWith"; 
		String p4 = this.recommenderTaskOntologyURI + "#" + "hasRatingValue1-5"; 

		kb.addObjectStatement(interaction,  p1, interactionType);
		kb.addObjectStatement(user,  p2, interaction);
		kb.addObjectStatement(interaction,  p3, item);
		kb.addLiteralStatement(interaction,  p4, event.getRatingValue());
	}

	public void addAnnotation(Item item, String property, String annotationURI) {
			String subject = this.ontologyURI+ "#" + "item" + item.getEntityId();
			kb.addObjectStatement(subject ,  property , annotationURI);
	}

	public void save() {
		kb.saveKBinFile(repositoryName);
	}
	
	public void load() {
		kb.loadKBfromFile(repositoryName);
	}

	public boolean hasItem(Item item) {
		return itemIndex.contains(item);
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}
	
	public List<String> getFactorsOfInterest() {
		List<String> factorsOfInterest = new ArrayList<String>();
		String factor, predicate = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
		String superProperty = "http://www.lapesd.inf.ufsc.br/ontology/recont.owl#hasObjectFI";
		List<RDFTriple> triples = kb.getStatements(null, predicate, superProperty);
		for(RDFTriple triple : triples) {
			//System.out.println(triple.getSubject());
			predicate = triple.getSubject();
			for(RDFTriple triple2 : kb.getStatements(null, predicate, null)) {
				factor = triple2.getObject();
				if(!factorsOfInterest.contains(factor)) {
					//System.out.println(factor);
					factorsOfInterest.add(factor);
				}
			}
		}
		//System.out.println(factorsOfInterest.size());
		return factorsOfInterest ;
	}
	
	public HashSet<User> getUsers() {
		return userIndex;
	}
}
