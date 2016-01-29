package version3;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by robertrambo on 17/01/2016.
 */
public class LowerUpperBoundManager {

    private Collection collection;
    private int numberOfCPUs = 1;
    private JProgressBar bar;
    private JLabel status;

    public LowerUpperBoundManager(int numberOfCPUs, Collection collection, JProgressBar bar, JLabel label){
        this.numberOfCPUs = numberOfCPUs;
        this.collection = collection;
        this.bar = bar;
        status = label;
    }

    public void boundNow(AnalysisModel analysisModel, int column, double limit){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        for (int i=0; i < collection.getDatasetCount(); i++) {
            // create
            if (collection.getDataset(i).getInUse()){
                Runnable bounder = new Bounder(analysisModel, collection.getDataset(i), column, limit);
                executor.execute(bounder);
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            setStatus("Finished Setting q-limits");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException " + e.getMessage());
            setStatus("Failed settings limits, exceeded thread time");
        }
    }


    public void setStatus(String text){
        status.setText(text);
    }


    public class Bounder implements Runnable {

        Dataset data;
        int column;
        int total;
        double limit;
        AnalysisModel analysisModel;

        public Bounder(AnalysisModel analysisModel, Dataset data, int column, double limit){
            this.analysisModel = analysisModel;
            this.data = data;
            this.total = data.getOriginalPositiveOnlyData().getItemCount();
            this.column = column;
            this.limit = limit;
        }

        @Override
        public void run() {
            XYSeries dataXY = data.getOriginalPositiveOnlyData();
            int setLimit=0;
            if (column == 4){
                // find q value that is >= limit
                setLimit = 0;

                for(int j=0; j<total; j++){
                    // sets the index to Original data (non-negative)
                    if (dataXY.getX(j).doubleValue() >= limit){
                        setLimit = j+1;
                        break;
                    }
                }

                //data.setStart(setLimit);
                data.lowBoundPlottedLog10IntensityData(setLimit);
                analysisModel.setValueAt(setLimit, data.getId(), column);
            } else if (column == 5){
                int lowerBound = data.getStart();
                setLimit = total;

                for(int j=(total-1); j>lowerBound; j--){
                    if (dataXY.getX(j).doubleValue() <= limit){
                        setLimit = j;
                        break;
                    }
                }

                //data.setEnd(setLimit);
                data.upperBoundPlottedLog10IntensityData(setLimit);
                analysisModel.setValueAt(setLimit, data.getId(), column);
            }

            //data.scalePlottedLog10IntensityData();
            //analysisModel.fireTableDataChanged();
        }
    }

}
