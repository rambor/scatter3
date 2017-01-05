package version3.formfactors;

/**
 * Created by robertrambo on 27/10/2016.
 */
abstract class Model {

    private ModelType modelType;
    private double[] modelIntensities;
    private double volume;
    private double solventContrast;
    private double[] particleContrasts;
    private double[] fittedParams; // fitted values
    private int index; // index of the model in the entire list
    private int totalIntensities;
    private int totalFittedParams;
    private double probability;
    private String stringToPrint;

    private double constant;

    public Model(int index, ModelType modelname, double volume, double solventContrast, double[] particleContrasts, int totalqvalues, int totalFittedParams){
        this.index = index;
        this.modelType = modelname;
        totalIntensities = totalqvalues;
        this.modelIntensities = new double[totalqvalues];
        //this.modelIntensities = new ArrayList<>();
        this.solventContrast = solventContrast;
        this.particleContrasts = particleContrasts;
        this.volume = volume;
        this.totalFittedParams = totalFittedParams;
        this.fittedParams = new double[totalFittedParams];
    }

    // abstract methods
    // calculate 1D SAXS curve using specified q-values
    abstract void calculateModelIntensities(Double[] qvalues);
    abstract String getConstrastString();

    public ModelType getModelType(){ return modelType;}

    public int getTotalIntensities(){return totalIntensities;}

    public void addIntensity(int index, double value){
        modelIntensities[index] = value;
    }

    public double getIntensity(int index){return modelIntensities[index];}

    public double getVolume(){return volume;}
    public void setVolume(double vol){ this.volume = vol;}

    public void setConstant(double value){ this.constant = value;}

    public double getConstant(){return constant;}
    public int getIndex(){return index;}
    public void setFittedParamsByIndex(int index, double value){ fittedParams[index] = value;}
    public double getFittedParamByIndex(int index){return fittedParams[index]; }
    public int getTotalFittedParams(){return totalFittedParams;}
    public void setProbability(double value){ this.probability = value;}
    public double getProbability(){ return this.probability;}

    public void setStringToPrint(String value){
        this.stringToPrint = value;
    }

    public String getParamsToPrint(){
        return stringToPrint;
    }

}
