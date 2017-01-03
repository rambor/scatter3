package version3.formfactors;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.util.FastMath;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ProlateEllipsoid extends Model {

    //private double radius_a; // minor axis
    //private double radius_c; // major axis
    private double contrast;
    double ratio2;
    double eccentricity;
    boolean amOblate=false;

    /**
     * Triaxial ellipsoid model
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radii  size 2 Array, [0] minor axis, [1] major axis
     * @param qvalues
     */
    public ProlateEllipsoid(int index, double solventContrast, double[] particleContrasts, double[] radii, Double[] qvalues) {

        super(index, ModelType.PROLATE_ELLIPSOID, (4.0/3.0*Math.PI*radii[0]*radii[0]*radii[1]), solventContrast, particleContrasts, qvalues.length, 2);

        if (radii[0] < radii[1]){ //oblate
            // radii[0] is minor axis
            // radii[1] is major axis
            this.setVolume((4.0/3.0*Math.PI*radii[0]*radii[1]*radii[1]));
            eccentricity = Math.sqrt(1.0-(radii[0]*radii[0]/radii[1]*radii[1]));
            amOblate = true;
        }

        // prolate r_a > r_c
        //  oblate r_c > r_a
        this.setFittedParamsByIndex(0, radii[0]); // r_a
        this.setFittedParamsByIndex(1, radii[1]); // r_c
        this.setString();

        ratio2 = (radii[0]*radii[0])/(radii[1]*radii[1]);

        this.contrast = particleContrasts[0] - solventContrast;
        this.setConstant(9.0*this.getVolume()*this.getVolume()*this.contrast*this.contrast);

        this.calculateModelIntensities(qvalues);
    }

    public void printParams(){
        System.out.println(this.getIndex() + " PARAMS MINOR " + this.getFittedParamByIndex(0) + " MAJOR " + this.getFittedParamByIndex(1));
    }

    public double getRadius_a(){ return this.getFittedParamByIndex(0);}
    public double getRadius_c(){ return this.getFittedParamByIndex(1);}

    private void setString(){
        String newLines = String.format("REMARK 265 INDEX %5d RADII %.2f %.2f %n", getIndex(), getFittedParamByIndex(0), getFittedParamByIndex(1));
        this.setStringToPrint(newLines);
    }

    @Override
    void calculateModelIntensities(Double[] qValues) {

        // old method below using integrator
        SimpsonIntegrator t = new SimpsonIntegrator();

        double qValue;
        double constant = getConstant();

         // prolate
        for(int i=0; i<getTotalIntensities(); i++){
            qValue = qValues[i];
            EllipsoidUnivariateFunction func = new EllipsoidUnivariateFunction(qValue);
            float integral = (float)t.integrate(100000000, func, 0, 1);
            this.addIntensity(i, qValue*constant*integral);
        }
    }


    /**
     * Prolate ellipsoid
     * alpha is integrated from 0 to 1
     */
    public class EllipsoidUnivariateFunction implements UnivariateFunction {

        private double q;

        public EllipsoidUnivariateFunction(double q){
            this.q = q;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double value(double alpha){
            double qr = q*effectiveR(alpha);
            double qrCube = qr*qr*qr;
            double sinCos = ((FastMath.sin(qr) - qr * FastMath.cos(qr)));
            double f = sinCos/qrCube;
            return f*f;
        }

        private double effectiveR(double alpha){
            return (getRadius_c()*FastMath.sqrt(1.0d+alpha*alpha*(ratio2 - 1.0d)));
        }
    }


    // return an array of random distances found in the ellipse.
    // use the array to add to other ellipses from selected set
    public double[] calculatePr(int arraySize){

        //
        double[] values = new double[arraySize];
        double inva2, invc2, invb2;
        double diffx, diffy, diffz;
        Double[] axis = new Double[3];

        // in model definition
        // x^2/a^2 + y^2/b^2 + z^2/c^2 = 1
        //
        // oblate a=b > c
        // oblate r_c > r_a
        //
        // prolate a > b = c
        // prolate r_a > r_c

        if (amOblate){ // oblate r_c > r_a, a = b
            axis[0] = getRadius_c(); // a
            axis[1] = getRadius_c(); // b
            axis[2] = getRadius_a(); // c
        } else { // prolate b = c
            axis[0] = getRadius_a(); // a
            axis[1] = getRadius_c(); // b
            axis[2] = getRadius_c(); // c
        }

        inva2 = 1.0/(axis[0]*axis[0]);
        invb2 = 1.0/(axis[1]*axis[1]);
        invc2 = 1.0/(axis[2]*axis[2]);

        double[] xvalues = new double[2];
        double[] yvalues = new double[2];
        double[] zvalues = new double[2];
        for(int i=0; i<arraySize; i++){
            // pick two random positions in ellipse
            // calculate distance
            int points = 0;
            while(points < 2){

                xvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[0], axis[0]);
                yvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[1], axis[1]);
                zvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[2], axis[2]);

                if ((xvalues[points]*xvalues[points]*inva2 + yvalues[points]*yvalues[points]*invb2 + zvalues[points]*zvalues[points]*invc2) <= 1){
                    points++;
                }
            }
            diffx = xvalues[0] - xvalues[1];
            diffy = yvalues[0] - yvalues[1];
            diffz = zvalues[0] - zvalues[1];

            values[i] = Math.sqrt(diffx*diffx + diffy*diffy + diffz*diffz);
        }

        return values;
    }

}
