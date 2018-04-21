package version3;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.lang.Math;
import static java.lang.Math.sqrt;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;


public class Monotonic extends XYLineAndShapeRenderer {

    private Vector points;
    private int precision;

    public Monotonic() {
        this(5);
    }

    public Monotonic(int precision) {
        super();
        if (precision <= 0) {
            throw new IllegalArgumentException("Requires precision > 0.");
        }
        this.precision = precision;
    }

    public int getPrecision() {
        return this.precision;
    }

    public void setPrecision(int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("Requires p > 0.");
        }
        this.precision = p;
        fireChangeEvent();
    }

    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
                                          XYPlot plot, XYDataset data, PlotRenderingInfo info) {

        State state = (State) super.initialise(g2, dataArea, plot, data, info);
        state.setProcessVisibleItemsOnly(false);
        this.points = new Vector();
        setDrawSeriesLineAsPath(true);
        return state;
    }

    protected void drawPrimaryLineAsPath(XYItemRendererState state,
                                         Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
                                         int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis,
                                         Rectangle2D dataArea) {

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // collect points
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            ControlPoint p = new ControlPoint(plot.getOrientation()
                    == PlotOrientation.HORIZONTAL ? (float) transY1
                    : (float) transX1, plot.getOrientation()
                    == PlotOrientation.HORIZONTAL ? (float) transX1
                    : (float) transY1);
            if (!this.points.contains(p)) {
                this.points.add(p);
            }
        }
        if (item == dataset.getItemCount(series) - 1) {
            State s = (State) state;
            // construct path
            if (this.points.size() > 1) {
                ControlPoint cp0 = (ControlPoint) this.points.get(0);
                s.seriesPath.moveTo(cp0.x, cp0.y);
                if (this.points.size() == 2) {
                    // we need at least 3 points to spline. Draw simple line
                    ControlPoint cp1 = (ControlPoint) this.points.get(1);
                    s.seriesPath.lineTo(cp1.x, cp1.y);
                }
                else {
                    int np = this.points.size();
                    double[] d = new double[np]; // Newton form coefficients
                    double[] x = new double[np]; // x-coordinates of nodes

                    for (int i = 0; i < np; i++) {
                        ControlPoint cpi = (ControlPoint) this.points.get(i);
                        x[i] = cpi.x;
                        d[i] = cpi.y;
                    }

                    double[] delta = new double[np-1];
                    for (int i = 0; i < np-1; i++) {
                        delta[i] = (d[i+1] - d[i]) / (x[i+1] - x[i]);
                    }

                    double[] fix = new double[np];
                    double[] m = new double[np];
                    for (int i = 1; i < np-1; i++) {
                        m[i] = (delta[i-1] + delta[i]) / 2;
                        fix[i] = 0;
                    }
                    m[0] = delta[0];
                    m[np-1] = delta[np-2];

                    for (int i = 0; i < np-1; i++) {
                        if (delta[i] == 0) {
                            fix[i] = 1.0;
                            m[i] = 0.0;
                            m[i+1] = 0.0;
                        }
                    }

                    double[] alpha = new double[np];
                    double[] beta = new double[np];
                    double[] dist = new double[np];
                    double[] tau = new double[np];
                    for (int i = 0; i <= np-2; i++) {
                        if (fix[i] == 0.0f) {
                            alpha[i] = m[i]/delta[i];
                            beta[i] = m[i+1]/delta[i];
                            dist[i] = alpha[i]*alpha[i] + beta[i]*beta[i];
                            tau[i] = 3 / sqrt(dist[i]);
                        }
                    }
                    for (int i = 0; i < np; i++) {
                        if (dist[i] > 9) {
                            m[i] = tau[i]*alpha[i]*delta[i];
                            m[i+1] = tau[i]*beta[i]*delta[i];
                        }
                    }

                    double oldt = x[0], t1, t2;
                    double oldy = d[0], t, y;
                    double h00, h01, h10, h11;
                    s.seriesPath.moveTo(oldt, oldy);
                    for (int i = 0; i < np - 1; i++) {
                        // loop over intervals between nodes
                        for (int j = 1; j <= this.precision; j++) {
                            double h = x[i+1] - x[i];
                            t1 = (h * j) / this.precision;
                            t2 = x[i] + t1;
                            t = j/(double) (this.precision);
                            h00 = 2*t*t*t - 3*t*t + 1;
                            h10 = t*t*t - 2*t*t + t;
                            h01 = -2*t*t*t + 3*t*t;
                            h11 = t*t*t - t*t;
                            y = h00*d[i] + h10*h*m[i] + h01*d[i+1] + h11*h*m[i+1];
                            s.seriesPath.lineTo(t2, y);
                        }
                    }
                }
                // draw path
                drawFirstPassShape(g2, pass, series, item, s.seriesPath);
            }
            // reset points vector
            this.points = new Vector();
        }
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Monotonic))
            return false;
        Monotonic that = (Monotonic) obj;
        if (this.precision != that.precision)
            return false;
        return super.equals(obj);
    }


    class ControlPoint {
        public float x;
        public float y;

        public ControlPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof ControlPoint))
                return false;
            ControlPoint that = (ControlPoint) obj;
            if (this.x != that.x)
                return false;
            return true;
        }
    }
}