package version3.ceengine;

import java.util.ArrayList;

/**
 * Created by robertrambo on 06/02/2016.
 */
public class EllipsoidModel {

    private double minorAxis;
    private double majorAxis;
    private ArrayList<Double> qValues;
    private ArrayList<Double> intensities;
    private final int totalQvalues;

    public EllipsoidModel(double minor, double major, ArrayList<Double> qvalues){
        this.minorAxis = minor;
        this.majorAxis = major;

        for(Double q:qvalues) {
            qValues.add(q);
        }

        totalQvalues = qValues.size();
    }

    public int getTotalQvalues(){
        return totalQvalues;
    }

    public double getIntensityAt(int index){
        return intensities.get(index);
    }

    public double getMajorAxis(){
        return majorAxis;
    }

    public double getMinorAxis(){
        return minorAxis;
    }

    // populate intensities list given major and minor axis values


}
