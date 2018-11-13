package br.ufsc.lapesd.orbs.core;

import java.util.AbstractMap;

import br.ufsc.lapesd.orbs.tokit.ItemResourceAnnotations;

public interface AnnotationSource {

	public AbstractMap<String, ItemResourceAnnotations> getAnnotations();
	   
}
