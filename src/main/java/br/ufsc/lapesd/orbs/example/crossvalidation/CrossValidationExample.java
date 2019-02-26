package br.ufsc.lapesd.orbs.example.crossvalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import br.ufsc.lapesd.orbs.core.UCFProposalEngine;
import br.ufsc.lapesd.orbs.example.ucfclassic.UCFClassicEngine;
import br.ufsc.lapesd.orbs.example.ucfmultiattribute.UCFMultiAttributeEngine;
import br.ufsc.lapesd.orbs.tokit.DataSource;
import br.ufsc.lapesd.orbs.tokit.DataSourceParams;
import br.ufsc.lapesd.orbs.tokit.Engine;
import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.tokit.Query;
import br.ufsc.lapesd.orbs.tokit.TrainingData;

public class CrossValidationExample {

	/**
	 * Default number of folds.
	 */
	public static final int N_FOLDS = 5;
	/**
	 * Default per user setting
	 */
	public static boolean PER_USER = true;
	/**
	 * Default seed.
	 */
	public static final long SEED = 2048L;
    /**
     * Default relevance threshold.
     */
    public static final double REL_TH = 3.0;
    /**
     * Default cutoff for evaluation metrics.
     */
    public static final int AT = 10;

	private final static String outputFile = "src/resources/main/examples/outputs.txt";

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

	public static void main(String[] args) {
		String enginePath = engines[1];
		System.out.println(enginePath); 

		System.out.println("Size:"+ Runtime.getRuntime().totalMemory());
		EngineParameter eparams = new EngineParameter(enginePath);

		//String splitsPath = prepareSplits(eparams, N_FOLDS);
		//prepareSets();
		//recommend(N_FOLDS, eparams, splitsPath);
		
		String splitsPath = "src/resources/main/temp/crossValid/";
		prepareStrategy(N_FOLDS, splitsPath, splitsPath, splitsPath);
		evaluate(N_FOLDS, splitsPath, splitsPath);

	}

	public static String prepareSplits(EngineParameter eparams, final int nFolds) {
		boolean perUser = PER_USER;
		long seed = SEED;

		// split data
		DataSourceSplitter dss = new DataSourceSplitter(eparams.getDataSouceParams(), nFolds, perUser, seed);

		return dss.split();
	}

    /**
     * Recommends using an UB algorithm.
     *
     * @param nFolds number of folds
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void recommend(final int nFolds, EngineParameter eparams, final String inPath) {
		List<String> trainingFiles;
		String testFile;
		
		for (int curFold = 0; curFold < nFolds; curFold++) {
			trainingFiles = new ArrayList<String>();
			for(int i=0; i< nFolds;++i) {
    			if(i != curFold) {
    				trainingFiles.add(inPath+"split_"+i+".csv");
    			}
    		}
    		testFile = inPath+"split_"+curFold+".csv";

    		DataSourceParams dsp = eparams.getDataSouceParams();
    		eparams.setDataSource(trainingFiles, dsp.getEnclosure(), "	", dsp.hasHeaderLine(), dsp.hasEventTimestamp());
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
