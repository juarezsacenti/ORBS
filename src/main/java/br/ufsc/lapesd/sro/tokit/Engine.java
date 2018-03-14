package br.ufsc.lapesd.sro.tokit;

import java.util.ArrayList;
import java.util.List;

public abstract class Engine {
	protected DataSource datasource;
	protected Preparator preparator;
	protected List<Algorithm> algorithms;
	protected List<Model> models;
	protected Serving serving;
	
	public Engine(EngineParameter eparams) {
		this.datasource = new DataSource(eparams.getDataSouceParams());
		this.algorithms = new ArrayList<Algorithm>();
		this.models = new ArrayList<Model>();
		this.serving = new Serving();
	}

	public void train() {
		TrainingData trainingData = datasource.readTraining();
    	System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
		PreparedData pd = preparator.prepare(trainingData);
		pd.toPrint();
		for(Algorithm algorithm : algorithms) {
			models.add(algorithm.train(pd));
		}
	}
	
	public void query(Query q) {
		List<PredictedResult> predictions = new ArrayList<PredictedResult>();
		PredictedResult result;
		for(int i = 0; i < algorithms.size(); ++i) {
			result = algorithms.get(i).predict(models.get(i), q);
			predictions.add(result);
		}

		serving.serve(q, predictions);
	}
}
