package version3;

import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by robertrambo on 16/01/2016.
 */
public class Averager {

    private Collection collectionInUse;
    private XYSeries averaged;
    private XYSeries averagedError;

    public Averager(Collection collection, String workingDirectoryName){
        this.collectionInUse = collection;

        ArrayList<XYSeries> results = StatMethods.weightedAverageDatasets(this.collectionInUse);

        averaged = results.get(0);
        averagedError = results.get(1);
        
    }

    public XYSeries getAveraged(){
        return averaged;
    }

    public XYSeries getAveragedError(){
        return averagedError;
    }

}
