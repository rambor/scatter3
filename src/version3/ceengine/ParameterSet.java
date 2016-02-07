package version3.ceengine;

/**
 * Created by Nathan Giles Donovan on 24/06/2015.
 * Updated by Rob Rambo 4/02/2016
 * A Class to hold the combination of parameters used in the fitting process
 */
public class ParameterSet {

    private float[] params;

    /**
     * Creates a new paramter set
     *
     * @param setParams The parameters to be included
     */
    public ParameterSet(float[] setParams){
        params = setParams;
    }

    /**
     * Gets the number of parameters
     *
     * @return The number of parameters
     */
    public int getParamNumbers() {
        return params.length;
    }

    /**
     * Returns the 'indexth' parameter
     *
     * @param index The index of the desired parameter
     * @return      The parameter
     */
    public float getParam(int index){
        return params[index];
    }

}
