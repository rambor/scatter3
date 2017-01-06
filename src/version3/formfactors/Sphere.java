package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

import java.util.List;

/**
 * Created by robertrambo on 28/10/2016.
 */
public class Sphere extends Model {

    private double contrast;
    private double invR3;
    private double invR4;
    private double invR6;
    private Double[] qvalues;

    /**
     * if contrast is unknown, set to 1
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radius
     * @param qvalues
     */
    public Sphere(int index, double solventContrast, double[] particleContrasts, double[] radius, List<Double> qvalues) {

        super(index, ModelType.SPHERICAL, (4.0/3.0*Math.PI*radius[0]*radius[0]*radius[0]), solventContrast, particleContrasts, qvalues.size(), 1);
        this.contrast = particleContrasts[0] - solventContrast;
        this.setConstant(4.0*Math.PI*9.0*this.getVolume()*this.getVolume()*contrast*contrast); // 9*V*contrast^2
        this.setFittedParamsByIndex(0, radius[0]);
        this.setString();
        //this.setConstant(4.0*Math.PI*9.0*contrast*contrast);
        this.qvalues = new Double[qvalues.size()];
        this.qvalues = qvalues.toArray(this.qvalues);
        this.calculateModelIntensities(this.qvalues);

        double inv = 1.0/radius[0];
        invR3 = inv*inv*inv;
        invR4 = invR3*inv;
        invR6=invR3*invR3;
    }


    /**
     * calculates model as q^6*I(q)
     * @param qValues
     */
    @Override
    void calculateModelIntensities(Double[] qValues) {

        double qValue;
        double qr, sinCos;
        double radius = this.getFittedParamByIndex(0);

        for(int i=0; i<this.getTotalIntensities(); i++){
            qValue = qValues[i];
            qr=(qValue*radius);
            sinCos=(FastMath.sin(qr) - qr*FastMath.cos(qr));
            this.addIntensity(i, qValue*this.getConstant()*sinCos*sinCos/(qr*qr*qr*qr*qr*qr)); // transform as q*I(q)
        }
    }

    public double getRadius(){return getFittedParamByIndex(0);}

    private void setString(){
        String newLines = String.format("REMARK 265 INDEX %5d RADIUS %.2f%n", getIndex(), getFittedParamByIndex(0));
        this.setStringToPrint(newLines);
    }



    public double calculatePr(double rvalue){
        double s2 = rvalue*rvalue;
        double s3 = s2*rvalue;
        return 3.0*s2*invR3 - 9.0*0.25*s3*invR4 + 3*0.0625*s3*s2*invR6;
    }

    @Override
    String getConstrastString() {
        double c = contrast*contrast;
        return String.format("REMARK 265              SQUARED CONTRAST : %.6f %n", c);
    }


}
