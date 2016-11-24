package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ThreeBody extends Model {

    private double radius1, radius2, radius3;
    private double r12, r23, r13;
    private double vol1, vol2, vol3;
    private double contrast;


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
        this.r13= r13distance;
        this.vol1 = (4.0/3.0*Math.PI*radius[0]*radius[0]*radius[0]);
        this.vol2 = (4.0/3.0*Math.PI*radius[1]*radius[1]*radius[1]);
        this.vol3 = (4.0/3.0*Math.PI*radius[2]*radius[2]*radius[2]);

        this.calculateModelIntensities(qvalues);

    }

    @Override
    void calculateModelIntensities(Double[] qValues) {

        double qValue;

        double qr1, qr2, qr3, rSixth, form, sphere1, sphere2, sphere3;
        double vol12 = vol1*vol2;
        double vol23 = vol2*vol3;
        double vol13 = vol1*vol3;
        double invVolSum = 1.0/(vol1+vol2+vol3);

        //rSixth=1.0/(radius*radius*radius*radius*radius*radius);

        for(int i=0; i<this.getTotalIntensities(); i++){
            qValue = qValues[i];
            qr1 = qValue*radius1;
            qr2 = qValue*radius2;
            qr3 = qValue*radius3;


            sphere1=(FastMath.sin(qr1) - qr1*FastMath.cos(qr1))/(qr1*qr1*qr1*qr1*qr1*qr1);
            sphere2=(FastMath.sin(qr2) - qr2*FastMath.cos(qr2))/(qr2*qr2*qr2*qr2*qr2*qr2);
            sphere3=(FastMath.sin(qr3) - qr3*FastMath.cos(qr3))/(qr3*qr3*qr3*qr3*qr3*qr3);

            form = vol12*sphere1*sphere2*FastMath.sin(qValue*r12)/(qValue*r12) +
                   vol23*sphere2*sphere3*FastMath.sin(qValue*r23)/(qValue*r23) +
                   vol13*sphere1*sphere3*FastMath.sin(qValue*r13)/(qValue*r13);


            this.addIntensity(i, qValue*this.getConstant()*form);
        }
    }

    public double getRadius1(){return radius1;}
    public double getRadius2(){return radius2;}
    public double getRadius3(){return radius3;}

}
