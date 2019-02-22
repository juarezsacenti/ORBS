package br.ufsc.lapesd.orbs.tokit;

import java.util.List;

public class AlgorithmParams {
	    private final long seed;
	    private final int rank;
	    private final int iteration;
	    private final double lambda;
	    private final List<String> similarItemEvents;
	    private final boolean unseenOnly;
	    private final List<String> seenItemEvents;
		private boolean useTestSeed;
		private boolean nativeEvaluatorEnabled;
		private int neighborhoodSize;

	    public AlgorithmParams(boolean useTestSeed, long seed, int rank, int iteration, double lambda, List<String> similarItemEvents, boolean unseenOnly, List<String> seenItemEvents) {
	        this.useTestSeed = useTestSeed;
	        this.seed = seed;
	        this.rank = rank;
	        this.iteration = iteration;
	        this.lambda = lambda;
	        this.similarItemEvents = similarItemEvents;
	        this.unseenOnly = unseenOnly;
	        this.seenItemEvents = seenItemEvents;
	    }

	    public long getSeed() {
	        return seed;
	    }

	    public int getRank() {
	        return rank;
	    }

	    public int getIteration() {
	        return iteration;
	    }

	    public double getLambda() {
	        return lambda;
	    }

	    public List<String> getSimilarItemEvents() {
	        return similarItemEvents;
	    }

	    public boolean isUnseenOnly() {
	        return unseenOnly;
	    }

	    public List<String> getSeenItemEvents() {
	        return seenItemEvents;
	    }

	    @Override
	    public String toString() {
	        return "AlgorithmParams{" +
	                "seed=" + seed +
	                ", rank=" + rank +
	                ", iteration=" + iteration +
	                ", lambda=" + lambda +
	                ", similarItemEvents=" + similarItemEvents +
	                ", unseenOnly=" + unseenOnly +
	                ", seenItemEvents=" + seenItemEvents +
	                '}';
	    }

		public boolean isNativeEvaluatorEnabled() {
			return nativeEvaluatorEnabled;
		}

		public int getNeighborhoodSize() {
			return neighborhoodSize;
		}

		public boolean useTestSeed() {
			return useTestSeed;
		}
	}