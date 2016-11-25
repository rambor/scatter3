package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ThreeBody extends Model {

    private double radius1, radius2, radius3;
    private double r12, r23, r13;
    private double vol1, vol2, vol3;
    private final double mathPI = 4.0/3.0*Math.PI;
    private double contrast;
    private double number_of_vectors;

    public ThreeBody(int index, double solventContrast, double[] particleContrasts, double[] radius, double r13distance, Double[] qvalues){
        super(index,
                ModelType.SPHERICAL,
                ((4.0/3.0*Math.PI*radius[0]*radius[0]*radius[0]) + (4.0/3.0*Math.PI*radius[1]*radius[1]*radius[1]) + (4.0/3.0*Math.PI*radius[2]*radius[2]*radius[2])),
                solventContrast,
                particleContrasts,
                qvalues.length);

        this.radius1 = radius[0];
        this.radius2 = radius[1];
        this.radius3 = radius[2];
        this.r12= radius1+radius2;
        this.r23= radius2+radius3;
        this.r13= r13distance; // correlation between first and last spehre
        this.vol1 = (mathPI*radius[0]*radius[0]*radius[0]);
        this.vol2 = (mathPI*radius[1]*radius[1]*radius[1]);
        this.vol3 = (mathPI*radius[2]*radius[2]*radius[2]);
        this.number_of_vectors = 6.0;

        this.contrast = particleContrasts[0] - solventContrast;
        double volS2 = 1.0/(vol1*vol1*vol2*vol2*vol3*vol3);
        this.setConstant(4*Math.PI*9*contrast*contrast/number_of_vectors); // 4*PI*contrast
        // form factor of sphere has 3 in it
        // 4 PI is from integrating theta and phi in spherical coordinates
        //
        //
        //this.setConstant(1.0/(this.getVolume()*this.getVolume())*contrast*contrast); // 9*V*contrast^2
        System.out.println(index + " radii " + radius1 + " " + radius2 + " " + radius3 + " r13 => " + r13);
        //System.out.println(index + " radii " + r12 + " " + r23 + " " + r13 + " ");
        this.calculateModelIntensities(qvalues);
    }

    @Override
    void calculateModelIntensities(Double[] qValues) {

        double qValue;

        double qr1, qr2, qr3, form, sphere1, sphere2, sphere3;
        double vol12 = vol1*vol2;
        double vol23 = vol2*vol3;
        double vol13 = vol1*vol3;
        //double invVolSum = 1.0/(vol1+vol2+vol3);
        //rSixth=1.0/(radius*radius*radius*radius*radius*radius);
        // form factor at I(0) should be squared number of electrons in particle
        // (vol_sphere*contrast)^2

        for(int i=0; i<this.getTotalIntensities(); i++){
            qValue = qValues[i];
            qr1 = qValue*radius1;
            qr2 = qValue*radius2;
            qr3 = qValue*radius3;
            // form factor for each sphere
            sphere1=(FastMath.sin(qr1) - qr1*FastMath.cos(qr1))/(qr1*qr1*qr1);
            sphere2=(FastMath.sin(qr2) - qr2*FastMath.cos(qr2))/(qr2*qr2*qr2);
            sphere3=(FastMath.sin(qr3) - qr3*FastMath.cos(qr3))/(qr3*qr3*qr3);

            // combine using debye formula
            form = sphere1*sphere1*vol1*vol1 + sphere2*sphere2*vol2*vol2 + sphere3*sphere3*vol3*vol3;

            form += vol12*sphere1*sphere2*FastMath.sin(qValue*r12)/(qValue*r12) +
                    vol23*sphere2*sphere3*FastMath.sin(qValue*r23)/(qValue*r23) +
                    vol13*sphere1*sphere3*FastMath.sin(qValue*r13)/(qValue*r13);

            this.addIntensity(i, qValue*this.getConstant()*form);
            //System.out.println(this.getIndex() + " " + qValue + " " + form);
        }
    }

    public double getR13Distance(){return r13;}
    public double getRadius1(){return radius1;}
    public double getRadius2(){return radius2;}
    public double getRadius3(){return radius3;}

}
