package version3.ceengine;

/**
 * Created by Nathan on 23/06/2015.
 */
public class CurveFactory {

    /**
     * Creates a {@link PredictedIntensityCurves} object based on the passed parameter.
     *
     * @param type
     * @return
     */
    public static PredictedIntensityCurves create(ModelType type, float scale){
        PredictedIntensityCurves curves = new SphericalCurves(scale);
        switch(type){
            case SPHERICAL:
                break;
            case CORESHELL:
            case CORESHELLBINARY:
                curves = new CoreShellCurves(scale);
                break;
            case ELLIPSOID:
                curves = new EllipsoidCurves(scale);
                break;
           // case CYLINDRICAL:
           //     curves = new CylindricalCurves(scale);
           //     break;
        }
        return curves;
    }
}