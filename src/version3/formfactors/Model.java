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
    private int index; // index of the model in the entire list
    private int totalIntensities;

    private double constant;

    public Model(int index, ModelType modelname, double volume, double solventContrast, double[] particleContrasts, int totalqvalues){
        this.index = index;
        this.modelType = modelname;
        totalIntensities = totalqvalues;
        this.modelIntensities = new double[totalqvalues];
        //this.modelIntensities = new ArrayList<>();
        this.solventContrast = solventContrast;
        this.particleContrasts = particleContrasts;
        this.volume = volume;
    }

    // abstract methods
    // calculate 1D SAXS curve using specified q-values
    abstract void calculateModelIntensities(Double[] qvalues);

    public ModelType getModelType(){ return modelType;}

    public int getTotalIntensities(){return totalIntensities;}

    public void addIntensity(int index, double value){
        modelIntensities[index] = value;
        //System.out.println(index + " " + modelIntensities[index]);
    }

    public double getIntensity(int index){return modelIntensities[index];}
    public double getVolume(){return volume;}
    public void setVolume(double vol){ this.volume = vol;}
    public void setConstant(double value){ this.constant = value;}
    public double getConstant(){return constant;}
    public int getIndex(){return index;}

}
