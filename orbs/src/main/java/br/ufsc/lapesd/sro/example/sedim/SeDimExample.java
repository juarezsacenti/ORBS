package br.ufsc.lapesd.sro.example.sedim;

/*
import br.ufsc.lisa.sedim.core.io.PropertiesReader;
import br.ufsc.lisa.sedim.core.io.CSVReader;
import br.ufsc.lisa.sedim.core.SemanticExpansioner;
import br.ufsc.lisa.sedim.core.HierarchyBuilder;
import br.ufsc.lisa.sedim.core.DimensionTailor;
import br.ufsc.lisa.sedim.core.DimensionWriter;
import br.ufsc.lisa.sedim.core.tokit.ToolDescription;
import br.ufsc.lisa.sedim.core.tokit.SimplePipeline;
import static br.ufsc.lisa.sedim.core.factory.ToolFactory.createToolDescription;
*/


/**
 * This pipeline uses SeDim toolkit to create a semantic dimension about URI resources in 
 * the input collection of semantic annotations.
 * The {@link CSVReader} reads semantic annotations from a CSV file. 
 * The {@link SemanticExpansioner} looks up resources from source domains which are linked
 * by properties to semantic annotations' resources and stores them in the triple store.
 * The {@link HierarchyDefiner} defines a resource hierarchy by mapping RDF statements of 
 * listed properties from triple store to RDF statements of a given hierarchy property and 
 * stores them in the triple store.
 * The {@link DimensionTailor} tailors a semantic dimension by removing from triple store 
 * RDF statements of a given hierarchy property when the number of indirect associated 
 * annotated objects (hits) is under a given threshold.
 * The {@link DimensionWriter} writes a semantic dimension defined by a given hierarchy
 * property to the target file.
 * <p>
 * Use {@code SeDimExtendedProcessTest} in the test directory to check the output.
 * </p>
 */
public class SeDimExample {

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, Exception {
// TODO		 
/*    	PropertiesReader.loadProperties("src/main/resources/SeDimConfig.properties");

    	ToolDescription reader = new CSVReader();
		ToolDescription expansioner = new SemanticExpansioner();
		ToolDescription hierarchyDefiner1 = createToolDescription(
				HierarchyBuilder.class,
				HierarchyBuilder.PARAM_HIERARCHY_PROPERTY_NAME, "http://www.lisa.inf.ufsc.br/sedim-ontology#myHierarchy1",
				HierarchyBuilder.PARAM_HIERARCHY_PROPERTY_MAPPING, new String[]{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#subClassOf"});
		ToolDescription hierarchyDefiner2 = createToolDescription(
				HierarchyBuilder.class,
				HierarchyBuilder.PARAM_HIERARCHY_PROPERTY_NAME, "http://www.lisa.inf.ufsc.br/sedim-ontology#myHierarchy2",
				HierarchyBuilder.PARAM_HIERARCHY_PROPERTY_MAPPING, new String[]{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#subClassOf"});
		ToolDescription dimensionTailor1 = createToolDescription(
				DimensionTailor.class,
				DimensionTailor.PARAM_HIERARCHY_PROPERTY_NAME, "http://www.lisa.inf.ufsc.br/sedim-ontology#myHierarchy1",
				DimensionTailor.PARAM_DIMENSION_THRESHOLD, 20);
		ToolDescription dimensionTailor2 = createToolDescription(
				DimensionTailor.class,
				DimensionTailor.PARAM_HIERARCHY_PROPERTY_NAME, "http://www.lisa.inf.ufsc.br/sedim-ontology#myHierarchy2",
				DimensionTailor.PARAM_DIMENSION_THRESHOLD, 40);
		ToolDescription writer = new DimensionWriter();
		
		ToolDescription[] pipe = {reader, expansioner, hierarchyDefiner1, hierarchyDefiner2, dimensionTailor1, dimensionTailor2, writer};
		SimplePipeline.runPipeline(pipe);
*/
	}
}
