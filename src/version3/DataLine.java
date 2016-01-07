package version3;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class DataLine {
    public double qvalue;
    public double intensity;
    public double error;
    public boolean isData;
    public DataLine (double nqvalue, double nintensity, double nerror, boolean nisData){
        qvalue = nqvalue;
        intensity = nintensity;
        error = nerror;
        isData = nisData;
    }

    //get Methods
    public double getq() {
        return qvalue;
    }

    public double getI() {
        return intensity;
    }

    public double getE() {
        return error;
    }

    public void setE(double nerror) {
        error = nerror;
    }

    public boolean getTest(){
        return isData;
    }
}
