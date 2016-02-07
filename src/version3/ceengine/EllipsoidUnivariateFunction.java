package version3.ceengine;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by Nathan on 15/07/2015.
 *
 * Provides an implementation of a function required for the numerical integration for the ellipsoid curves.
 *
 * @author Nathan
 * @version 1.0
 */
public class EllipsoidUnivariateFunction implements UnivariateFunction{

    private float maj, min,q;

    public EllipsoidUnivariateFunction(float maj, float min, float q){
        this.maj = maj;
        this.min = min;
        this.q = q;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double value(double v){
        float r = findR(v);
        float qr = q*r;
        float rCube = r*r*r;
        float sinCos = (float) ((FastMath.sin(qr) - qr * FastMath.cos(qr)));
        float f = sinCos/rCube;

        return f*f*Math.sin(v);
    }

    private float findR(double alpha){
        double thing1 = maj*FastMath.cos(alpha);
        double thing2 = min*FastMath.sin(alpha);
        return (float)FastMath.sqrt(thing1*thing1 + thing2*thing2);
    }
}
