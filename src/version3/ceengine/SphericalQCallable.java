package version3.ceengine;

import org.apache.commons.math3.util.FastMath;
import java.util.concurrent.Callable;

/**
 * Created by Nathan on 30/06/2015.
 *
 * Allows multithreaded implementation to pre-calculate curves.
 *
 * @author Nathan
 * @version 1.0
 */

public class SphericalQCallable implements Callable{

    private int threadNo,qStart, qEnd, qLength;
    private double[] qValues;
    private float[] rValues;

    public SphericalQCallable(int threadNo, float[] rValues, int qStart, int qEnd, double[] qValues){
        this.threadNo=threadNo;
        this.rValues=rValues;
        this.qValues=qValues;
        this.qStart = qStart;
        this.qEnd = qEnd;
        qLength = qEnd - qStart;
    }

    @Override
    /**
     * Calculates the curves.
     *
     * @return an array containing the calculated curve
     */
    public float[] call(){
        //System.out.println("Thread " + threadNo + ": " + qValues.length);
        int length = rValues.length;
        float[] values=new float[length*qLength+1];
        values[0] = threadNo;
        int counter=1;
        double qValue;
        float rValue, qr, rCube, sinCos;

        for(int i=qStart; i<qEnd; i++){
            qValue = qValues[i];
            for(int j=0; j < length; j++){
                rValue = rValues[j];
                qr=(float) qValue*rValue;
                rCube=rValue*rValue*rValue;
                sinCos=(float) (FastMath.sin(qr)-qr*FastMath.cos(qr));
                //values[counter++]= Main.BKG + (SphericalCurves.constant*sinCos*sinCos/rCube);
                values[counter++] = (SphericalCurves.constant*sinCos*sinCos/rCube);
            }
        }

        return values;
    }
}
