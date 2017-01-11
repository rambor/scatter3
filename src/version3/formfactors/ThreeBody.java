package version3.formfactors;

import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class ThreeBody extends Model {

    //private double radius1, radius2, radius3;
    private double smallest, middle, largest;
    private double r12, r23, r13;
    private double vol1, vol2, vol3;
    private double leftmost, center, longest;
    private final double mathPI = 4.0/3.0*Math.PI;
    private double contrast;
    private ArrayList<Body> bodies;

    public ThreeBody(int index, double solventContrast, double[] particleContrasts, double[] radii, double r13distance, Double[] qvalues){
        super(index,
                ModelType.SPHERICAL,
                ((4.0/3.0*Math.PI*radii[0]*radii[0]*radii[0]) + (4.0/3.0*Math.PI*radii[1]*radii[1]*radii[1]) + (4.0/3.0*Math.PI*radii[2]*radii[2]*radii[2])),
                solventContrast,
                particleContrasts,
                qvalues.length,
                3);

        bodies = new ArrayList<>();
//        this.setFittedParamsByIndex(0, radii[0]); // radii_0
//        this.setFittedParamsByIndex(1, radii[1]); // radii_1
//        this.setFittedParamsByIndex(2, radii[2]); // radii_2

        Double doubleObject;
        doubleObject = new Double(radii[0]+radii[1]);
        //this.r12= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();
        bodies.add(new Body(radii[0], radii[1], BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue()));

        doubleObject = new Double(radii[1]+radii[2]);
        //this.r23= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();
        bodies.add(new Body(radii[1], radii[2], BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue()));

        doubleObject = new Double(r13distance);
        //this.r13= BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue();
        bodies.add(new Body(radii[0], radii[2], BigDecimal.valueOf(doubleObject).setScale(3, RoundingMode.CEILING).doubleValue()));

        Collections.sort(bodies, new BodyComp()); // smallest to largest distances
        setOrderOfRadii();

        r12 = bodies.get(0).getDistance(); // left-to-center
        r23 = bodies.get(1).getDistance(); // center-to-largest
        r13 = bodies.get(2).getDistance(); // left-to-largest

//        setOrder();
//        this.setFittedParamsByIndex(0, smallest); // radii_0
//        this.setFittedParamsByIndex(1, middle);   // radii_1
//        this.setFittedParamsByIndex(2, largest);  // radii_2

        // use the distances that define the arrangement, should be sorted
        this.setFittedParamsByIndex(0, bodies.get(0).getDistance());  //
        this.setFittedParamsByIndex(1, bodies.get(1).getDistance());  //
        this.setFittedParamsByIndex(2, bodies.get(2).getDistance());  //

        // leftmost
        // center
        this.vol1 = (mathPI*leftmost*leftmost*leftmost);
        this.vol2 = (mathPI*center*center*center);
        this.vol3 = (mathPI*longest*longest*longest);

        this.contrast = particleContrasts[0] - solventContrast;
        //double volS2 = 1.0/(vol1*vol1*vol2*vol2*vol3*vol3);
        //this.setConstant(4*Math.PI*9*contrast*contrast/number_of_vectors); // 4*PI*contrast
        this.setConstant(4*Math.PI*9*contrast*contrast); // 4*PI*contrast
        this.calculateModelIntensities(qvalues);
    }

    @Override
    void calculateModelIntensities(Double[] qValues) {

        double qValue;

        double qr1, qr2, qr3, form, sphere1, sphere2, sphere3;
        double vol12 = vol1*vol2;
        double vol23 = vol2*vol3;
        double vol13 = vol1*vol3;
        // double invVolSum = 1.0/(vol1+vol2+vol3);
        // rSixth=1.0/(radius*radius*radius*radius*radius*radius);
        // form factor at I(0) should be squared number of electrons in particle
        // (vol_sphere*contrast)^2

        for(int i=0; i<this.getTotalIntensities(); i++){
            qValue = qValues[i];
            //qr1 = qValue*getFittedParamByIndex(0); // should be radius
            //qr2 = qValue*getFittedParamByIndex(1); // radius
            //qr3 = qValue*getFittedParamByIndex(2); //
            qr1 = qValue*leftmost; // should be radius
            qr2 = qValue*center;   // radius
            qr3 = qValue*longest;     //

            // form factor for each sphere
            sphere1=(FastMath.sin(qr1) - qr1*FastMath.cos(qr1))/(qr1*qr1*qr1); // smallest
            sphere2=(FastMath.sin(qr2) - qr2*FastMath.cos(qr2))/(qr2*qr2*qr2); // middle
            sphere3=(FastMath.sin(qr3) - qr3*FastMath.cos(qr3))/(qr3*qr3*qr3); // largest

            // combine using debye formula
            form = sphere1*sphere1*vol1*vol1 + sphere2*sphere2*vol2*vol2 + sphere3*sphere3*vol3*vol3;

            // cross-terms
            form += vol12*sphere1*sphere2*FastMath.sin(qValue*r12)/(qValue*r12) +
                    vol23*sphere2*sphere3*FastMath.sin(qValue*r23)/(qValue*r23) +
                    vol13*sphere1*sphere3*FastMath.sin(qValue*r13)/(qValue*r13);

//            form += sphere1*sphere2*FastMath.sin(qValue*r12)/(qValue*r12) +
//                    sphere2*sphere3*FastMath.sin(qValue*r23)/(qValue*r23) +
//                    sphere1*sphere3*FastMath.sin(qValue*r13)/(qValue*r13);

            this.addIntensity(i, qValue*this.getConstant()*form);

            if (Double.isNaN(form)){
                System.out.println(this.getIndex() + " " + qValue + " " + form);
            }
        }
    }

//    public double getR13Distance(){return r13;}
//    public double getR23Distance(){return r23;}
//    public double getR12Distance(){return r12;}

    public double getSortedDistanceByIndex(int index){return bodies.get(index).getDistance();}

//    public double getRadius1(){return getFittedParamByIndex(0);}
//    public double getRadius2(){return getFittedParamByIndex(1);}
//    public double getRadius3(){return getFittedParamByIndex(2);}
//    public double getSmallest(){ return smallest;}
//    public double getLargest(){ return largest;}
//    public double getMiddle(){ return middle;}

    public double getLeftMost(){ return leftmost;}
    public double getCenter(){ return center;}
    public double getLast(){ return longest;}


    public void printOrder(){
        System.out.println(getIndex() + " RAD " + leftmost + " <= " + center + " <= " + longest);
        System.out.println(getIndex() + " DIS " + bodies.get(0).getDistance() + " <= " + bodies.get(1).getDistance() + " <= " + bodies.get(2).getDistance());
    }

    /**
     * Determine the order of the parameters
     */
//    private void setOrder(){
//
//        double radius1 = getFittedParamByIndex(0);
//        double radius2 = getFittedParamByIndex(1);
//        double radius3 = getFittedParamByIndex(2);
//
//        if ((radius1 <= radius2) && (radius2 <= radius3)){
//            smallest = radius1;
//            middle = radius2;
//            largest = radius3;
//        } else if ((radius2 <= radius3) && (radius3 <= radius1)){
//            smallest = radius2;
//            middle = radius3;
//            largest = radius1;
//        } else if ((radius3 <= radius2) && (radius2 <= radius1)){
//            smallest = radius3;
//            middle = radius2;
//            largest = radius1;
//        }else if ((radius3 <= radius1) && (radius1 <= radius2)){
//            smallest = radius3;
//            middle = radius1;
//            largest = radius2;
//        }else if ((radius2 <= radius1) && (radius1 <= radius3)){
//            smallest = radius2;
//            middle = radius1;
//            largest = radius3;
//        }else if ((radius1 <= radius3) && (radius3 <= radius2)){
//            smallest = radius1;
//            middle = radius3;
//            largest = radius2;
//        }
//    }

    @Override
    String getConstrastString() {
        double c = contrast*contrast;
        return String.format("REMARK 265              SQUARED CONTRAST : %.1f %n", c);
    }



    public class Body{
        private double radius1;
        private double radius2;
        private double distance;

        public Body(double radius1, double radius2, double distance){
            this.radius1 = radius1;
            this.radius2 = radius2;
            this.distance = distance;
        }

        public double getDistance(){
            return this.distance;
        }
    }


    /**
     * do on sorted array
     * leftmost -> center -> longest
     * sort is based on distances between each sphere
     * - align shortest axis along x
     *
     */
    private void setOrderOfRadii(){

        Body first = bodies.get(0);
        Body second = bodies.get(1);
        Body third = bodies.get(2);

        // compare first and second
        // found common radii

        if (first.radius1 <= first.radius2){
            leftmost = first.radius1;
              center = first.radius2;
        } else {
            leftmost = first.radius2;
              center = first.radius1;
        }

        // which is larger
        if (third.radius1 >= center){
            longest = third.radius1;
        } else {
            longest = third.radius2;
        }

    }



    class BodyComp implements Comparator<Body> {

        @Override
        public int compare(Body o1, Body o2) {
            if (o1.getDistance() > o2.getDistance()) return 1;
            if (o1.getDistance() < o2.getDistance()) return -1;
            return 0;
        }
    }

}
