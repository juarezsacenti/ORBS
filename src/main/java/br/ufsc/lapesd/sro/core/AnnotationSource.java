package br.ufsc.lapesd.sro.core;

import java.util.AbstractMap;

import br.ufsc.lapesd.sro.tokit.ItemResourceAnnotations;

public interface AnnotationSource {

	public AbstractMap<String, ItemResourceAnnotations> getAnnotations();
	   
}
