package version3.formfactors;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ProlateEllipsoid extends Model {

    private double radius_a; // minor axis
    private double radius_c; // major axis
    private double contrast;
    private boolean isOblate=false;
    double ratio2;

    /**
     * Triaxial ellipsoid model
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radii  size 2 Array, [0] minor axis, [1] major axis
     * @param qvalues
     */
    public ProlateEllipsoid(int index, double solventContrast, double[] particleContrasts, double[] radii, Double[] qvalues) {

        super(index, ModelType.PROLATE_ELLIPSOID, (4.0/3.0*Math.PI*radii[0]*radii[0]*radii[1]), solventContrast, particleContrasts, qvalues.length);

        if (radii[0] < radii[1]){
            this.setVolume((4.0/3.0*Math.PI*radii[0]*radii[1]*radii[1]));
            isOblate=true;
        }

        radius_a = radii[0]; // prolate is radius_a > radius_c
        radius_c = radii[1]; //  oblate is radius_c > radius_a
        ratio2 = (radius_a*radius_a)/(radius_c*radius_c);

        this.contrast = particleContrasts[0] - solventContrast;
        this.setConstant(9.0*this.getVolume()*this.contrast*this.contrast);

        this.calculateModelIntensities(qvalues);
    }

    public void printParams(){
        System.out.println(this.getIndex() + " PARAMS MINOR " + radius_a + " MAJOR " + radius_c);
    }

    public double getRadius_a(){ return radius_a;}
    public double getRadius_c(){ return radius_c;}

    @Override
    void calculateModelIntensities(Double[] qValues) {

        // old method below using integrator
        SimpsonIntegrator t = new SimpsonIntegrator();

        double qValue;
        double constant = getConstant();//*radius_c*radius_a*radius_a;

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
            return (radius_c*FastMath.sqrt(1.0d+alpha*alpha*(ratio2 - 1.0d)));
        }
    }


}
