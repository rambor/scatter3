package version3.formfactors;

import java.util.ArrayList;

/**
 * Created by robertrambo on 04/11/2016.
 */
public class KeptModel {

    private int index;
    private ArrayList<Integer> indexOfSelectedModels;
    private double score;

    public KeptModel(int index, int sizeOfList, ArrayList<Integer> list){
        indexOfSelectedModels = new ArrayList<>(sizeOfList);
        for(int i=0; i<sizeOfList; i++){
            indexOfSelectedModels.add(list.get(i));
        }
    }

    public void setScore(double value){
        score = value;
    }


}
