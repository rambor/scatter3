package version3.formfactors;

import org.apache.commons.math3.util.FastMath;
import version3.ceengine.SphericalCurves;

/**
 * Created by robertrambo on 28/10/2016.
 */
public class Sphere extends Model {

    private double radius;
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

        super(index, ModelType.SPHERICAL, (4.0/3.0*Math.PI*radius[0]*radius[0]*radius[0]), solventContrast, particleContrasts, qvalues.length);
        this.radius = radius[0];
        this.contrast = particleContrasts[0] - solventContrast;
        this.setConstant(9.0d*this.getVolume()*contrast*contrast); // 9*V*contrast^2

        this.calculateModelIntensities(qvalues);
    }


    /**
     * calculates model as q^6*I(q)
     * @param qValues
     */
    @Override
    void calculateModelIntensities(Double[] qValues) {

        double qValue;

        double qr, rSixth, sinCos;
        //rSixth=1.0/(radius*radius*radius*radius*radius*radius);

        for(int i=0; i<this.getTotalIntensities(); i++){
            qValue = qValues[i];

            qr=(qValue*radius);
            sinCos=(FastMath.sin(qr) - qr*FastMath.cos(qr));
            //System.out.println(qValue + " qValue " + sinCos + " " + this.getConstant());
            //this.addIntensity(i, this.getConstant()*sinCos*sinCos*rSixth);
            this.addIntensity(i, qValue*this.getConstant()*sinCos*sinCos/(qr*qr*qr*qr*qr*qr));
        }
    }

    public double getRadius(){return radius;}

}
