/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.ufsc.lapesd.orbs.example.crossvalidation;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.examples.DataDownloader;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.mahout.GenericRecommenderBuilder;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import net.recommenders.rival.split.parser.MovielensParser;
import net.recommenders.rival.split.splitter.CrossValidationSplitter;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.TrainingData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.evaluation.metric.error.RMSE;

/**
 * RiVal Movielens100k Mahout Example, using 5-fold cross validation.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public final class RiValCrossValidatedRecsys {

    /**
     * Default number of folds.
     */
    public static final int N_FOLDS = 5;
    /**
     * Default neighbohood size.
     */
    public static final int NEIGH_SIZE = 50;
    /**
     * Default cutoff for evaluation metrics.
     */
    public static final int AT = 10;
    /**
     * Default relevance threshold.
     */
    public static final double REL_TH = 3.0;
    /**
     * Default seed.
     */
    public static final long SEED = 2048L;

    private final static String[] engines = {
			"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-100.json",               // 0
			"src/resources/main/example/engines/U-CF-Classic-Pearson-Mahout-Mov1M-25.json",                // 1
			"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-100.json",  // 2
			"src/resources/main/example/engines/U-CF-Multiattribute_Genre-Pearson-Mahout-Mov1M-25.json",   // 3
			"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-100.json",        // 4
			"src/resources/main/example/engines/U-CF-Proposal_Genre-Pearson-Mahout-Mov1M-25.json",         // 5
			"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-100.json",         // 6
			"src/resources/main/example/engines/U-CF-Proposal_Date-Pearson-Mahout-Mov1M-25.json",          // 7
			"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-100.json",	   // 8
	"src/resources/main/example/engines/U-CF-Proposal_GenreDate-Pearson-Mahout-Mov1M-25.json"};    // 9
    /**
     * Utility classes should not have a public or default constructor.
     */
    private RiValCrossValidatedRecsys() {
    }

    /**
     * Main method. Parameter is not used.
     *
     * @param args the arguments (not used)
     */
    public static void main(final String[] args) {
    	String enginePath = engines[8];
		EngineParameter eparams = new EngineParameter(enginePath);

		String url = "http://files.grouplens.org/datasets/movielens/ml-1m.zip";
        String folder = "src/resources/main/data/ml-1m";
        String modelPath = "src/resources/main/crossValid/ml-1m/model/";
        String recPath = "src/resources/main/crossValid/ml-1m/recommendations/";
        String dataFile = eparams.getDataSouceParams().getSourceLocation().get(0);
        int nFolds = N_FOLDS;
        		
        System.out.println("Preparing splits...");
        prepareSplits(url, nFolds, dataFile, folder, modelPath);
        
        System.out.println("Gathering recomendations...");
        //recommend(nFolds, modelPath, recPath); // RiVal's original step.
        //orbsRecommend(nFolds, eparams, modelPath); // Based on RiVal' step
        mixedRecommend(nFolds, eparams, modelPath); // Mixed step
        
        //System.out.println("Preparing strategy...");
        // the strategy files are (currently) being ignored
        //prepareStrategy(nFolds, modelPath, recPath, modelPath);

        System.out.println("Evaluating...");
        evaluate(nFolds, modelPath, recPath);
    }

    /**
     * Downloads a dataset and stores the splits generated from it.
     *
     * @param url url where dataset can be downloaded from
     * @param nFolds number of folds
     * @param inFile file to be used once the dataset has been downloaded
     * @param folder folder where dataset will be stored
     * @param outPath path where the splits will be stored
     */
    public static void prepareSplits(final String url, final int nFolds, final String inFile, final String folder, final String outPath) {
        DataDownloader dd = new DataDownloader(url, folder);
        dd.downloadAndUnzip();

        boolean perUser = true;
        long seed = SEED;
        Parser<Long, Long> parser = new MovielensParser();

        DataModelIF<Long, Long> data = null;
        try {
            data = parser.parseData(new File(inFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataModelIF<Long, Long>[] splits = new CrossValidationSplitter<Long, Long>(nFolds, perUser, seed).split(data);
        File dir = new File(outPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.println("Directory " + dir + " could not be created");
                return;
            }
        }
        for (int i = 0; i < splits.length / 2; i++) {
            DataModelIF<Long, Long> training = splits[2 * i];
            DataModelIF<Long, Long> test = splits[2 * i + 1];
            String trainingFile = outPath + "train_" + i + ".csv";
            String testFile = outPath + "test_" + i + ".csv";
            System.out.println("train: " + trainingFile);
            System.out.println("test: " + testFile);
            boolean overwrite = true;
            try {
                DataModelUtils.saveDataModel(training, trainingFile, overwrite, "\t");
                DataModelUtils.saveDataModel(test, testFile, overwrite, "\t");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Recommends using an UB algorithm. Based on RiVal step.
     *
     * @param nFolds number of folds
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void orbsRecommend(final int nFolds, EngineParameter eparams, final String inPath) {
		List<String> trainingFiles;
		String testFile;
		
		for (int curFold = 0; curFold < nFolds; curFold++) {
			trainingFiles = new ArrayList<String>();
			trainingFiles.add(inPath + "train_" + curFold + ".csv");
    		testFile = inPath+"test_"+curFold+".csv";

    		DataSourceParams dsp = eparams.getDataSouceParams();
    		eparams.setDataSource(trainingFiles, dsp.getEnclosure(), "	", false, false);
    		eparams.setEngineName("EngineFold"+curFold);
    		eparams.setTestFile(testFile);
    		eparams.setServing("br.ufsc.lapesd.orbs.example.crossvalidation.ServingToFile");

    		Engine engine;
    		switch (eparams.getEngineType()) {
    		case "br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine":  
    			engine = new UCFClassicEngine(eparams);
    			break;
    		case "br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine":  
    			engine = new UCFMultiAttributeEngine(eparams);
    			break;			
    		case "br.ufsc.lapesd.orbs.core.ProposalEngine":
    			engine = new UCFProposalEngine(eparams);
    			break;				
    		default:
    			System.out.println("Declared engine no expected. Using ClassicNeighborhoodAnalyzerEngine instead.");
    			engine = new UCFClassicEngine(eparams);
    			break;		
    		}
    		
    		System.out.println("EngineFold "+ curFold +" will begin training.");
    		engine.train();

            //Query especial para todos os usuários do teste, recomendar todos os itens do treino
    		List<String> ls = new ArrayList<String>();
    		ls.add(testFile);
    		eparams.setDataSource(ls, dsp.getEnclosure(), "	", false, false);
    		DataSource testSource = new DataSource(eparams.getDataSouceParams());
    		TrainingData testData = testSource.readTraining();
    		Set<String> userIDs = testData.getUsers().keySet();
    		
    		for(String userID: userIDs){
            	engine.query(new Query(userID, 4000, null, null, null));
            }
        }
    }
    
    /**
     * Recommends using an UB algorithm. Mixed step.
     *
     * @param nFolds number of folds
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void mixedRecommend(final int nFolds, EngineParameter eparams, final String inPath) {
		List<String> trainingFiles;
		String trainPath, testPath;
		
		for (int i = 0; i < nFolds; i++) {
    		trainPath = inPath+"train_"+i+".csv";
    		testPath = inPath+"test_"+i+".csv";

            org.apache.mahout.cf.taste.model.DataModel trainModel;
            org.apache.mahout.cf.taste.model.DataModel testModel;
            try {
                trainModel = new FileDataModel(new File(trainPath));
                testModel = new FileDataModel(new File(testPath));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

			trainingFiles = new ArrayList<String>();
			trainingFiles.add(trainPath);
    		DataSourceParams dsp = eparams.getDataSouceParams();
    		eparams.setDataSource(trainingFiles, dsp.getEnclosure(), "	", false, false);
    		eparams.setEngineName("EngineFold"+i);
    		eparams.setTestFile(testPath);
    		eparams.setServing("br.ufsc.lapesd.orbs.example.crossvalidation.ServingToFile");

    		Engine engine;
    		switch (eparams.getEngineType()) {
    		case "br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine":  
    			engine = new UCFClassicEngine(eparams);
    			break;
    		case "br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine":  
    			engine = new UCFMultiAttributeEngine(eparams);
    			break;			
    		case "br.ufsc.lapesd.orbs.core.ProposalEngine":
    			engine = new UCFProposalEngine(eparams);
    			break;				
    		default:
    			System.out.println("Declared engine no expected. Using ClassicNeighborhoodAnalyzerEngine instead.");
    			engine = new UCFClassicEngine(eparams);
    			break;		
    		}
       		System.out.println("EngineFold "+ i +" is start training.");
    		engine.train();

    		System.out.println("EngineFold "+ i +" is receiving queries.");
    		LongPrimitiveIterator users;
    		try {
    			users = testModel.getUserIDs();
                while (users.hasNext()) {
                    long u = users.nextLong();
                	engine.query(new Query(String.valueOf(u), trainModel.getNumItems(), null, null, null));
                }
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Recommends using an UB algorithm. RiVal original step.
     *
     * @param nFolds number of folds
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void recommend(final int nFolds, final String inPath, final String outPath) {
        for (int i = 0; i < nFolds; i++) {
            org.apache.mahout.cf.taste.model.DataModel trainModel;
            org.apache.mahout.cf.taste.model.DataModel testModel;
            try {
                trainModel = new FileDataModel(new File(inPath + "train_" + i + ".csv"));
                testModel = new FileDataModel(new File(inPath + "test_" + i + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            GenericRecommenderBuilder grb = new GenericRecommenderBuilder();
            String recommenderClass = "org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender";
            String similarityClass = "org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity";
            int neighborhoodSize = NEIGH_SIZE;
            Recommender recommender = null;
            try {
                recommender = grb.buildRecommender(trainModel, recommenderClass, similarityClass, neighborhoodSize);
            } catch (RecommenderException e) {
                e.printStackTrace();
            }

            String fileName = "recs_" + i + ".csv";

            LongPrimitiveIterator users;
            try {
                users = testModel.getUserIDs();
                boolean createFile = true;
                while (users.hasNext()) {
                    long u = users.nextLong();
                    assert recommender != null;
                    List<RecommendedItem> items = recommender.recommend(u, trainModel.getNumItems());
                    //
                    List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
                    for (RecommendedItem ri : items) {
                        prefs.add(new RecommenderIO.Preference<>(u, ri.getItemID(), ri.getValue()));
                    }
                    //
                    RecommenderIO.writeData(u, prefs, outPath, fileName, !createFile, null);
                    createFile = false;
                }
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prepares the strategies to be evaluated with the recommenders already
     * generated.
     *
     * @param nFolds number of folds
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     * @param outPath path where the filtered recommendations will be stored
     */
    @SuppressWarnings("unchecked")
    public static void prepareStrategy(final int nFolds, final String splitPath, final String recPath, final String outPath) {
        for (int i = 0; i < nFolds; i++) {
            File trainingFile = new File(splitPath + "train_" + i + ".csv");
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModelIF<Long, Long> trainingModel;
            DataModelIF<Long, Long> testModel;
            DataModelIF<Long, Long> recModel;
            try {
                trainingModel = new SimpleParser().parseData(trainingFile);
                testModel = new SimpleParser().parseData(testFile);
                recModel = new SimpleParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Double threshold = REL_TH;
            String strategyClassName = "net.recommenders.rival.evaluation.strategy.UserTest";
            EvaluationStrategy<Long, Long> strategy = null;
            try {
                strategy = (EvaluationStrategy<Long, Long>) (Class.forName(strategyClassName)).getConstructor(DataModelIF.class, DataModelIF.class, double.class).
                        newInstance(trainingModel, testModel, threshold);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }

            DataModelIF<Long, Long> modelToEval = DataModelFactory.getDefaultModel();
            for (Long user : recModel.getUsers()) {
                assert strategy != null;
                for (Long item : strategy.getCandidateItemsToRank(user)) {
                    if (!Double.isNaN(recModel.getUserItemPreference(user, item))) {
                        modelToEval.addPreference(user, item, recModel.getUserItemPreference(user, item));
                    }
                }
            }
            try {
                DataModelUtils.saveDataModel(modelToEval, outPath + "strategymodel_" + i + ".csv", true, "\t");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Evaluates the recommendations generated in previous steps.
     *
     * @param nFolds number of folds
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     */
    public static void evaluate(final int nFolds, final String splitPath, final String recPath) {
        double ndcgRes = 0.0;
        double precisionRes = 0.0;
        double rmseRes = 0.0;
        for (int i = 0; i < nFolds; i++) {
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModelIF<Long, Long> testModel = null;
            DataModelIF<Long, Long> recModel = null;
            try {
                testModel = new SimpleParser().parseData(testFile);
                recModel = new SimpleParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NDCG<Long, Long> ndcg = new NDCG<>(recModel, testModel, new int[]{AT});
            ndcg.compute();
            ndcgRes += ndcg.getValueAt(AT);

            RMSE<Long, Long> rmse = new RMSE<>(recModel, testModel);
            rmse.compute();
            rmseRes += rmse.getValue();

            Precision<Long, Long> precision = new Precision<>(recModel, testModel, REL_TH, new int[]{AT});
            precision.compute();
            precisionRes += precision.getValueAt(AT);
        }
        System.out.println("NDCG@" + AT + ": " + ndcgRes / nFolds);
        System.out.println("RMSE: " + rmseRes / nFolds);
        System.out.println("P@" + AT + ": " + precisionRes / nFolds);

    }
}
