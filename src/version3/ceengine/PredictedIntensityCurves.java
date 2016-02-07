package version3.ceengine;

import java.util.ArrayList;

/**
 * Created by Nathan on 23/06/2015.
 *
 * A Class to provide an abstract implementation of Predicted Intensity Curves. Allows greater flexibility and use of
 * multiple models in the code.
 */
public abstract class PredictedIntensityCurves{

    protected float[] values;
    protected static float constant, contrast;
    protected ModelType type;
    protected int paramLength, qLength;

    /**
     * Sets the range of parameters and q. Implementation should also include a calculating of the curves
     *
     * @param paramValues The range of valid parameter sets
     * @param qValues     The range of valid q values
     */
    public abstract void setThings(ArrayList<ParameterSet> paramValues, double[] qValues);

    /**
     * Gets the value of the curve at the specified parameter set and q value
     *
     * @param paramIndex The index of the desired parameter set
     * @param q          The q value the curves are to be evaluated at
     * @return
     */
    public float getValue(int paramIndex, int q) {
        return values[q* paramLength + paramIndex];
    }

    /**
     * Sets the contrast of the curves
     *
     * @param contrast The value of the contrast to be set
     */
    public void setContrast(float contrast){
        this.contrast = contrast;
    }

    /**
     * Gets the type of curves
     *
     * @return The type of the curves
     */
    public ModelType getType(){
        return type;
    }

    /**
     * Gets the number of parameter sets
     *
     * @return The number of parameter sets
     */
    public int getParamLength(){
        return paramLength;
    }

}
