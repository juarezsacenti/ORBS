package br.ufsc.lapesd.orbs.tokit;

import java.util.List;
import java.util.ArrayList;

public class ItemResourceAnnotations {
    private final String item;
    private final List<Pair<String, String>> annotations;

	public ItemResourceAnnotations(String itemId) {
		this.item = itemId;
		this.annotations = new ArrayList<Pair<String, String>>();
	}

	public void add(String type, String annotation) {
		annotations.add(new Pair<String, String>(type, annotation));
	}
	
	public List<Pair<String, String>> getAnnotations() {
		return annotations;
	}

	public String getItem() {
		return item;
	}

}
