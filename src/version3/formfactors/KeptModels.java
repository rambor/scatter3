package version3.formfactors;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 09/12/2016.
 */
public class KeptModels {

    private ArrayList<Double> scores;
    private ArrayList<Double> scales;
    private ArrayList<ArrayList<Integer>> indicesKept;

    public KeptModels(){
             scores = new ArrayList<>();
             scales = new ArrayList<>();
        indicesKept = new ArrayList<>();
    }

    public void addModel(double score, ArrayList<Integer> indices, double scale){
        scores.add(score);
        scales.add(scale);
        indicesKept.add(new ArrayList<>());
        int last = indicesKept.size()-1;

        for(int i=0; i<indices.size(); i++){
            indicesKept.get(last).add(indices.get(i));
        }
    }

    public void clear(){
        scores.clear();
        scales.clear();

        int total = indicesKept.size();
        for(int i=0; i<total; i++){
            indicesKept.get(i).clear();
        }
        indicesKept.clear();
    }

    public ArrayList<Integer> getFirst(){
        return indicesKept.get(0);
    }

    public double getBestScore(){ return scores.get(0);}

    public double getScaleByIndex(int index){ return scales.get(index);}

    public int getTotal(){ return scores.size();}

    public ArrayList<Integer> getIndicesByIndex(int index){
        return indicesKept.get(index);
    }

}
