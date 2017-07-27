package version3;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import version3.Functions;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by xos81802 on 22/06/2017.
 */
public class AutoCorrelationOfRatios extends SwingWorker {
    private double qmin, qmax;
    private XYSeriesCollection unratiodData;
    private XYSeriesCollection unratiodError;
    private ArrayList<Ratio> ratios;

    public AutoCorrelationOfRatios(XYSeriesCollection unratiodData, XYSeriesCollection unratiodError, Number qmin, Number qmax) {
        this.unratiodData = unratiodData;
        this.qmin = qmin.doubleValue();
        this.qmax = qmax.doubleValue();
        this.unratiodError = unratiodError;
    }

    @Override
    protected Object doInBackground() throws Exception {

        this.ratios();
        return null;
    }


    private void ratios(){

        ratios = new ArrayList<>();
        int totalSeries = unratiodData.getSeriesCount();
        // for each dataset, compute pairwise ratios and quantitate
        for(int i=0; i<totalSeries; i++){

            XYSeries reference = unratiodData.getSeries(i);
            int totalInReference = reference.getItemCount();
            int startPt=0;
            int endPt=totalInReference-1;

            for(int m=0; m<totalInReference; m++){
                if (reference.getX(m).doubleValue() >= qmin){
                    startPt =m;
                    break;
                }
            }

            for(int m=endPt; m>0; m--){
                if (reference.getX(m).doubleValue() <= qmax){
                    endPt=m;
                    break;
                }
            }

            Number qminStartNumber = reference.getX(startPt);
            Number qmaxStartNumber = reference.getX(endPt);

            int next = i+1;
            int indexOfratios=0;

            for(int j=next; j<totalSeries; j++){
                ratios.add(new Ratio(reference, unratiodData.getSeries(j), unratiodError.getSeries(j), qminStartNumber, qmaxStartNumber, 1,1, 0));
                ratios.get(indexOfratios);
                indexOfratios++;
            }
        }
    }

    // calculate autoCorrelation of the ratio



}
