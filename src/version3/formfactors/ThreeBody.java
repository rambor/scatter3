package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ThreeBody extends Model {

    //private double radius1, radius2, radius3;
    private double smallest, middle, largest;
    private double r12, r23, r13;
    private double[] sortedDistances = new double[3];
    private double vol1, vol2, vol3;
    private final double mathPI = 4.0/3.0*Math.PI;
    private double contrast;
    private double number_of_vectors;

    public ThreeBody(int index, double solventContrast, double[] particleContrasts, double[] radii, double r13distance, Double[] qvalues){
        super(index,
                ModelType.SPHERICAL,
                ((4.0/3.0*Math.PI*radii[0]*radii[0]*radii[0]) + (4.0/3.0*Math.PI*radii[1]*radii[1]*radii[1]) + (4.0/3.0*Math.PI*radii[2]*radii[2]*radii[2])),
                solventContrast,
                particleContrasts,
                qvalues.length,
                3);

        this.setFittedParamsByIndex(0, radii[0]); // radii_0
        this.setFittedParamsByIndex(1, radii[1]); // radii_1
        this.setFittedParamsByIndex(2, radii[2]); // radii_2

        Double doubleObject;
        doubleObject = new Double(radii[0]+radii[1]);
        this.r12= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();
        doubleObject = new Double(radii[1]+radii[2]);
        this.r23= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();
        doubleObject = new Double(r13distance);
        this.r13= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();

        sortedDistances[0] = r12;
        sortedDistances[1] = r23;
        sortedDistances[2] = r13;
        Arrays.sort(sortedDistances); // which radii map to each sorted distance
        setOrder();

        this.setFittedParamsByIndex(0, smallest); // radii_0
        this.setFittedParamsByIndex(1, middle);   // radii_1
        this.setFittedParamsByIndex(2, largest);  // radii_2


        this.vol1 = (mathPI*radii[0]*radii[0]*radii[0]);
        this.vol2 = (mathPI*radii[1]*radii[1]*radii[1]);
        this.vol3 = (mathPI*radii[2]*radii[2]*radii[2]);
        this.number_of_vectors = 6.0;

        this.contrast = particleContrasts[0] - solventContrast;
        //double volS2 = 1.0/(vol1*vol1*vol2*vol2*vol3*vol3);
        //this.setConstant(4*Math.PI*9*contrast*contrast/number_of_vectors); // 4*PI*contrast
        this.setConstant(4*Math.PI*9*this.getVolume()*contrast*contrast); // 4*PI*contrast
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
            qr1 = qValue*getFittedParamByIndex(0);
            qr2 = qValue*getFittedParamByIndex(1);
            qr3 = qValue*getFittedParamByIndex(2);
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

            if (Double.isNaN(form)){
                System.out.println(this.getIndex() + " " + qValue + " " + form);
            }
        }
    }

    public double getR13Distance(){return r13;}
    public double getR23Distance(){return r23;}
    public double getR12Distance(){return r12;}
    public double getSortedDistanceByIndex(int index){return sortedDistances[index];}
    public double getRadius1(){return getFittedParamByIndex(0);}
    public double getRadius2(){return getFittedParamByIndex(1);}
    public double getRadius3(){return getFittedParamByIndex(2);}
    public double getSmallest(){ return smallest;}
    public double getLargest(){ return largest;}
    public double getMiddle(){ return middle;}

    public void printOrder(){
        System.out.println(getIndex() + " RAD " + smallest + " <= " + middle + " <= " + largest);
        System.out.println(getIndex() + " DIS " + sortedDistances[0] + " <= " + sortedDistances[1] + " <= " + sortedDistances[2]);
    }

    private void setOrder(){
        double radius1 = getFittedParamByIndex(0);
        double radius2 = getFittedParamByIndex(1);
        double radius3 = getFittedParamByIndex(2);

        if ((radius1 <= radius2) && (radius2 <= radius3)){
            smallest = radius1;
            middle = radius2;
            largest = radius3;
        } else if ((radius2 <= radius3) && (radius3 <= radius1)){
            smallest = radius2;
            middle = radius3;
            largest = radius1;
        } else if ((radius3 <= radius2) && (radius2 <= radius1)){
            smallest = radius3;
            middle = radius2;
            largest = radius1;
        }else if ((radius3 <= radius1) && (radius1 <= radius2)){
            smallest = radius3;
            middle = radius1;
            largest = radius2;
        }else if ((radius2 <= radius1) && (radius1 <= radius3)){
            smallest = radius2;
            middle = radius1;
            largest = radius3;
        }else if ((radius1 <= radius3) && (radius3 <= radius2)){
            smallest = radius1;
            middle = radius3;
            largest = radius2;
        }
    }

}
