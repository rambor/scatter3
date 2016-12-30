package version3.formfactors;

import java.util.Comparator;

/**
 * Created by robertrambo on 09/12/2016.
 */
public class ScoreObject {

    private double scorevalue;
    private double scale;
    private int index; // index of model in round

    public ScoreObject(double scorevalue, int index, double scale) {
        this.scorevalue = scorevalue;
        this.index = index;
        this.scale = scale;
    }

    public double getScale(){return scale;}
    public double getScorevalue(){return scorevalue;}
    public int getIndex() {return index;}

}
