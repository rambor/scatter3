package version3.formfactors;

import java.util.Comparator;

/**
 * Created by robertrambo on 09/12/2016.
 */
public class ScoreObject {

    private double scorevalue;
    private int index; // index of model in round

    public ScoreObject(double scorevalue, int index) {
        this.scorevalue = scorevalue;
        this.index = index;
    }

    public double getScorevalue(){return scorevalue;}
    public int getIndex() {return index;}

}
