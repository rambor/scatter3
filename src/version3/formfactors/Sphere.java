package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by robertrambo on 28/10/2016.
 */
public class Sphere extends Model {

    private double contrast;

    /**
     * if contrast is unknown, set to 1
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radius
     * @param qvalues
     */
    public Sphere(int index, double solventContrast, double[] particleContrasts, double[] radius, Double[] qvalues) {

        super(index, ModelType.SPHERICAL, (4.0/3.0*Math.PI*radius[0]*radius[0]*radius[0]), solventContrast, particleContrasts, qvalues.length, 1);
        this.contrast = particleContrasts[0] - solventContrast;
        this.setConstant(4.0*Math.PI*9.0*this.getVolume()*this.getVolume()*contrast*contrast); // 9*V*contrast^2
        this.setFittedParamsByIndex(0, radius[0]);
        this.setString();
        //this.setConstant(4.0*Math.PI*9.0*contrast*contrast);
        this.calculateModelIntensities(qvalues);
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

}
