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
public class CoreShellQCallable implements Callable{

    private int threadNo, paramLength, qStart, qEnd, qLength;
    private double[] qValues;
    private float[] rValues;

    public CoreShellQCallable(int threadNo, float[] rValues, int qStart, int qEnd, double[] qValues, int paramLength){
        this.threadNo=threadNo;
        this.rValues=rValues;
        this.qStart = qStart;
        this.qEnd = qEnd;
        this.qValues=qValues;
        this.paramLength = paramLength;
        this.qLength = qEnd - qStart;
    }

    @Override
    /**
     * Calculates the curves.
     *
     * @return an array containing the calculated curve
     */
    public float[] call(){
        float[] values=new float[paramLength*qLength+1];
        values[0] = threadNo;
        int counter=1;
        float qValue, qrc, qrs, rSCube, sinCos, rValueAtJ;
        for(int i=qStart; i<qEnd; i++){

            qValue = (float) qValues[i];
            for(int j = 1; j < rValues.length; j += 4){
                rValueAtJ = rValues[j];
                qrc = qValue * rValues[j - 1];
                qrs = qValue * rValueAtJ;
                rSCube = rValueAtJ*rValueAtJ*rValueAtJ;
                sinCos = (float) (rValues[j+1] * (FastMath.sin(qrc) - qrc * FastMath.cos(qrc)) + rValues[j+2] * (FastMath.sin(qrs) - qrs * FastMath.cos(qrs)));
                //values[counter++] = Main.BKG + (CoreShellCurves.constant * sinCos * sinCos / rSCube);
                values[counter++] =  (CoreShellCurves.constant * sinCos * sinCos / rSCube);
            }
        }

        return values;
    }
}
