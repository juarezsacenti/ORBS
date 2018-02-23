package br.ufsc.lapesd.sro.tokit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class EngineParameter {
	private static final String engineParametersPath = "src/resources/main/engine.json";
	private static final String engineParametersEncoding = "UTF-8";
	private JsonObject paramsJsonObject;

	public EngineParameter() {
		load(engineParametersPath, engineParametersEncoding);
	}
	
	public EngineParameter(String engineParametersPath) {
		load(engineParametersPath, engineParametersEncoding);
	}
	
	public EngineParameter(String[] input) {
		paramsJsonObject = new JsonObject();
		paramsJsonObject.addProperty("id", "default");
		paramsJsonObject.addProperty("description", "Default settings");
		paramsJsonObject.addProperty("engineFactory", "br.ufsc.lapesd.sro.example.SROEngine");
		
		JsonObject datasource = new JsonObject();
		JsonObject datasourceParams = new JsonObject();
		datasourceParams.addProperty("appName", "MySRO");
		datasourceParams.addProperty("sourceLocation", input[0]);
		datasourceParams.addProperty("enclosure", input[1]);
		datasourceParams.addProperty("delimiter", input[2]);
		datasourceParams.addProperty("hasHeaderLine", input[3].equals("true") );
		datasource.add("params", datasourceParams);
		paramsJsonObject.add("datasource", datasource);
	
		JsonObject preparator = new JsonObject();
		JsonObject preparatorParams = new JsonObject();
		
		JsonObject contextOntologyParams = new JsonObject();
		contextOntologyParams.addProperty("ontologyURI", input[4]);
		contextOntologyParams.addProperty("ontologyModelSpec", input[5]);
		contextOntologyParams.addProperty("tripleStore", input[6] );
		contextOntologyParams.addProperty("tripleStoreDirectory", input[7]);
		contextOntologyParams.addProperty("repositoryName", input[8]);
		contextOntologyParams.addProperty("contextOntologyURI", input[9] );
		contextOntologyParams.addProperty("contextOntologyLocation", input[10]);
		contextOntologyParams.addProperty("contextOntologyFormat", input[11] );
		
		JsonObject annotationSourceParams = new JsonObject();
		annotationSourceParams.addProperty("appName", "MySRO");
		annotationSourceParams.addProperty("sourceLocation", input[12]);
		annotationSourceParams.addProperty("enclosure", input[13]);
		annotationSourceParams.addProperty("delimiter", input[14]);
		annotationSourceParams.addProperty("hasHeaderLine", input[15].equals("true") );
		
		JsonObject semanticExpansionerParams = new JsonObject();
		
		JsonObject hierarchyBuilderParams = new JsonObject();

		preparatorParams.add("contextOntologyParams", contextOntologyParams);
		preparatorParams.add("annotationSourceParams", annotationSourceParams);
		preparatorParams.add("semanticExpansionerParams", semanticExpansionerParams);
		preparatorParams.add("hierarchyBuilderParams", hierarchyBuilderParams);
		preparator.add("params", preparatorParams);
		paramsJsonObject.add("preparator", preparator);
	}
	
	public String[] toViewFields() {
		String[] viewFields = new String[16];
		JsonObject jOBJ;
		jOBJ= paramsJsonObject.getAsJsonObject("datasource").getAsJsonObject("params");
		viewFields[0] = jOBJ.get("sourceLocation").getAsString();
		viewFields[1] = jOBJ.get("enclosure").getAsString();
		viewFields[2] = jOBJ.get("delimiter").getAsString();
		viewFields[3] = jOBJ.get("hasHeaderLine").getAsString();
		
		jOBJ= paramsJsonObject.getAsJsonObject("preparator").getAsJsonObject("params").getAsJsonObject("contextOntologyParams");
		viewFields[4] = jOBJ.get("ontologyURI").getAsString();
		viewFields[5] = jOBJ.get("ontologyModelSpec").getAsString();
		viewFields[6] = jOBJ.get("tripleStore").getAsString();
		viewFields[7] = jOBJ.get("tripleStoreDirectory").getAsString();
		viewFields[8] = jOBJ.get("repositoryName").getAsString();
		viewFields[9] = jOBJ.get("contextOntologyURI").getAsString();
		viewFields[10] = jOBJ.get("contextOntologyLocation").getAsString();
		viewFields[11] = jOBJ.get("contextOntologyFormat").getAsString();
		
		jOBJ= paramsJsonObject.getAsJsonObject("preparator").getAsJsonObject("params").getAsJsonObject("annotationSourceParams");
		viewFields[12] = jOBJ.get("sourceLocation").getAsString();
		viewFields[13] = jOBJ.get("enclosure").getAsString();
		viewFields[14] = jOBJ.get("delimiter").getAsString();
		viewFields[15] = jOBJ.get("hasHeaderLine").getAsString();		
		
		return viewFields;
	}
	
	public DataSourceParams getDataSouceParams() {
		Gson g = new Gson();
		DataSourceParams dsp = g.fromJson(paramsJsonObject.get("datasource").getAsJsonObject().get("params"), DataSourceParams.class);
		return dsp;
	}
	
	public PreparatorParams getPreparatorParams() {
		Gson g = new Gson();
		PreparatorParams pp = g.fromJson(paramsJsonObject.get("preparator").getAsJsonObject().get("params"), PreparatorParams.class);
		return pp;
	}
	
	public AlgorithmParams getAlgorithmParams() {
		Gson g = new Gson();
		AlgorithmParams ap = g.fromJson(paramsJsonObject.get("algorithm").getAsJsonObject().get("params"), AlgorithmParams.class);
		return ap;
	}

	public void load(String path, String encoding) {
		try{
			InputStream is = new FileInputStream(path);
			Reader reader = new InputStreamReader(is, encoding);
			this.paramsJsonObject = new JsonParser().parse(reader).getAsJsonObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save(String path, String encoding) {
		if(paramsJsonObject != null) {
			try{
				OutputStream os = new FileOutputStream(path);
				Writer writer = new OutputStreamWriter(os, encoding);
				writer.write(paramsJsonObject.toString());
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
}
