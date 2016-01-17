package version3;

import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;

/**
 * Created by robertrambo on 17/01/2016.
 */
public class Medianer {
    private Collection collectionInUse;
    private XYSeries medianSet;
    private XYSeries medianSetError;

    public Medianer(Collection collection){
        this.collectionInUse = collection;

        ArrayList<XYSeries> results = StatMethods.medianDatasets(this.collectionInUse);

        medianSet = results.get(0);
        medianSetError = results.get(1);

    }

    public XYSeries getMedianSet(){
        return medianSet;
    }

    public XYSeries getMedianSetError(){
        return medianSetError;
    }
}
