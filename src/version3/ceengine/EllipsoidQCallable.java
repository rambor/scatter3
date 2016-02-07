package version3.ceengine;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import java.util.concurrent.Callable;

/**
 * Created by Nathan on 30/06/2015.
 *
 * Allows multithreaded implementation to pre-calculate curves.
 *
 * @author Nathan
 * @version 1.0
 */
public class EllipsoidQCallable implements Callable{

    private int threadNo, paramLength, qStart, qEnd, qLength;
    private double[] qValues;
    private float[] rValues;
    private final double pi_half = Math.PI*0.5;

    public EllipsoidQCallable(int threadNo, float[] rValues, int qStart, int qEnd, double[] qValues, int paramLength){
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
        SimpsonIntegrator t = new SimpsonIntegrator();
        //System.out.println("Thread " + threadNo + ": " + qValues.length);
        float[] values=new float[paramLength*qLength+1];
        values[0] = threadNo;
        int counter=1;
        float qValue, maj, min;

        for(int i=qStart; i<qEnd; i++){
            qValue = (float)qValues[i];
            for(int j = 0; j < rValues.length; j += 2){
                //t = new TrapezoidIntegrator();
                maj = rValues[j];
                min = rValues[j+1];

                EllipsoidUnivariateFunction f = new EllipsoidUnivariateFunction(maj, min, qValue);
                float integral = (float)t.integrate(100000000, f, 0, pi_half);

                //values[counter++] = Main.BKG + (EllipsoidCurves.constant *maj*min*min*integral);
                values[counter++] =  (EllipsoidCurves.constant *maj*min*min*integral);
            }
            //System.out.println(threadNo + ": " + i);
        }

        return values;
    }
}

