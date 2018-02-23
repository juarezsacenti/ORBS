package br.ufsc.lapesd.sro.tokit;

import java.util.List;

public class PredictedResult {
    private final List<ItemScore> itemScores;

    public PredictedResult(List<ItemScore> itemScores) {
        this.itemScores = itemScores;
    }

    public List<ItemScore> getItemScores() {
        return itemScores;
    }

    @Override
    public String toString() {
        return "PredictedResult{" +
                "itemScores=" + itemScores +
                '}';
    }
}