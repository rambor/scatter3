package version3;

import org.jfree.data.xy.XYSeries;

import java.util.concurrent.Callable;

/**
 * Created by xos81802 on 20/07/2017.
 */
public class CallableRatio implements Callable<Ratio> {

    private int refIndex, tarIndex, order;
    private Number qmin, qmax;
    private XYSeries refXY, tarXY, tarError;

    public CallableRatio(XYSeries referenceSet, XYSeries targetSet, XYSeries targetError, Number qmin, Number qmax, int refIndex, int tarIndex, int order){
        this.refIndex = refIndex;
        this.tarIndex = tarIndex;
        this.qmin = qmin;
        this.qmax = qmax;

        this.refXY = new XYSeries("ref");
        this.tarXY = new XYSeries( "tar");
        this.tarError = new XYSeries("err");

        this.order = order;

        synchronized (referenceSet){
            int total = referenceSet.getItemCount();
            for(int i=0; i<total; i++){
                refXY.add(referenceSet.getDataItem(i));
            }
        }

        synchronized (targetSet){
            int total = targetSet.getItemCount();
            for(int i=0; i<total; i++){
                tarXY.add(targetSet.getDataItem(i));
            }
        }

        synchronized (targetError){
            int total = targetError.getItemCount();
            for(int i=0; i<total; i++){
                tarError.add(targetError.getDataItem(i));
            }
        }
    }

    public int getOrder(){return order;}

    @Override
    public Ratio call() throws Exception {
        Ratio temp = new Ratio(refXY, tarXY, tarError, qmin,  qmax, refIndex, tarIndex, order);
        //temp.printTests(String.format("RATIO r: %d %d", refIndex, tarIndex));
        return temp;
    }
}
