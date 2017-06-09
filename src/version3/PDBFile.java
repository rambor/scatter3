package version3;

import org.apache.commons.math3.util.FastMath;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertrambo on 08/02/2016.
 */
public class PDBFile {
    private int dmax, totalAtomsRead;
    private double qmax;
    private int pr_bins;
    private double delta_r;
    private ArrayList<Double> distances;
    private boolean useWaters = false;
    private XYSeries pdbdata;
    private XYSeries high_res_pr_data;
    private XYSeries icalc;
    private XYSeries error;
    private ArrayList<PDBAtom> atoms;
    private ArrayList<PDBAtom> centeredAtoms;
    private String filename;
    private String currentWorkingDirectory;
    private double[] mooreCoefficients;
    private double izero, rg;

    private double highresWidth = 2.79d;

    public PDBFile(File selectedFile, double qmax, boolean exclude, String workingDirectoryName){
        filename = selectedFile.getName();
        FileInputStream fstream = null;
        this.qmax = qmax;
        this.useWaters = !exclude;
        icalc = new XYSeries(filename);
        this.currentWorkingDirectory = workingDirectoryName;

        atoms = new ArrayList<>();
        System.out.println("Reading PDB file : " + selectedFile.getName());
        if (selectedFile.length() == 0){
            try {
                throw new Exception("File PDB Empty");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            fstream = new FileInputStream(selectedFile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // need the lines that contain ATOM
            // arraylist of x, y, z

            try {
                while ((strLine = br.readLine()) != null) {

                    if (strLine.matches("^ATOM.*") || (strLine.matches("^HETATM.*HOH.*") ) ){
                        atoms.add(new PDBAtom(strLine));
                    }
                }
                totalAtomsRead = atoms.size();
                System.out.println("Total atoms read " + totalAtomsRead);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        calculatePofR();
        this.convertPofRToIntensities();

        //this.centerCoordinates();
        //this.convertToBeadModel(2.0);
    }

    private void calculatePofR() {

        if ((totalAtomsRead < 10)) {
            System.err.println("Specify Proper PDB file ");
        } else {
            // create working set
            ArrayList<PDBAtom> workingCoords = new ArrayList<>();
            for(int i=0;i<totalAtomsRead; i++){
                PDBAtom tempAtom = atoms.get(i);
                if (useWaters){
                    workingCoords.add(tempAtom);
                } else {
                    if (!tempAtom.isWater()){
                        workingCoords.add(tempAtom);
                    }
                }
            }

            // determine dmax of pdb file
            int totalAtoms = workingCoords.size();
            dmax = dmaxFromPDB(workingCoords, totalAtoms);
            // calculate PofR from workingAtoms
            // use resolution to determine number of Shannon bins
            delta_r = Math.PI/qmax;
            pr_bins = (int)Math.ceil((dmax / delta_r));
            double ns_dmax = pr_bins*delta_r; // dmax corresponding to number of Shannon Bins
            distances = new ArrayList<>();
            // should bin based on ns_dmax
            int high_res_pr_bins = (int)Math.ceil((double)dmax/highresWidth);
            double inv_delta = qmax/Math.PI;

            // high res bin width =>
            // implies dmax/high_res_bin_width
            // int totalBinsHiRes = (int)Math.ceil(dmax/high_res_pr_bins);
            double inv_high_res_delta = 1.0/highresWidth;

            int[] histo = new int[pr_bins];
            int[] highResHisto = new int[high_res_pr_bins];

            double[] atom, atom2;
            double refx, refy, refz, difx, dify, difz, distance;
            int startIndex, bin;

            for (int i = 0; i < totalAtoms; i++) { // n*(n-1)/2 distances
                atom = workingCoords.get(i).getCoords(); // use different weights for atom type here
                refx = atom[0];
                refy = atom[1];
                refz = atom[2];

                startIndex = i + 1;

                while (startIndex < totalAtoms) {
                    atom2 = workingCoords.get(startIndex).getCoords();  // use different weights for atom type here
                    difx = refx - atom2[0];
                    dify = refy - atom2[1];
                    difz = refz - atom2[2];

                    distance = FastMath.sqrt(difx * difx + dify * dify + difz * difz);
                    // which bin low res?
                    // lower < value <= upper
                    bin = (int) Math.floor(distance * inv_delta); // casting to int is equivalent to floor
                    //histo[(bin >= pr_bins) ? bin - 1 : bin] += 1;
                    histo[bin] += 1;

                    // which bin high res?
                    bin = (int) Math.floor(distance * inv_high_res_delta); // casting to int is equivalent to floor
                    //highResHisto[(bin >= high_res_pr_bins) ? bin - 1 : bin] += 1;
                    highResHisto[bin] += 1;
                    startIndex++;
                }
            }

            pdbdata = new XYSeries(filename);
            high_res_pr_data = new XYSeries(filename);
            pdbdata.add(0,0);
            for (int i = 0; i < pr_bins; i++) {
                pdbdata.add((i + 0.5) * delta_r, histo[i]);  // middle position of the histogram bin
                //pdbdata.add((i+1) * delta_r, histo[i]);    // far edge position of the histogram bin
                //pdbdata.add(i * delta_r, histo[i]);        // near edge position of the histogram bin
                XYDataItem tempItem = pdbdata.getDataItem(i);
                System.out.println(tempItem.getXValue() + " " + tempItem.getYValue() + " 0 ");
            }
            // pdbdata.add(delta_r*(pr_bins), 0); // dmax is contained in the last bin
            pdbdata.add(ns_dmax, 0);

            // fill high resolution bins
            System.out.println("PRINTING HIGH RESOLUTION PR DISTRIBUTION");
            System.out.println(String.format("%5.2f 0 0", 0.0));
            high_res_pr_data.add(0, 0);
            for (int i = 0; i < high_res_pr_bins; i++) {
                high_res_pr_data.add((i + 0.5) * highresWidth, highResHisto[i]);  // middle position of the histogram bin
                //high_res_pr_data.add((i + 1) * highresWidth, highResHisto[i]);  // middle position of the histogram bin
                XYDataItem tempItem = high_res_pr_data.getDataItem(i+1);
                System.out.println(String.format("%5.2f %.5E 0", tempItem.getXValue(), tempItem.getYValue()));
            }
            high_res_pr_data.add(highresWidth*high_res_pr_bins, 0);
            System.out.println(String.format("%5.2f 0 0", highresWidth*high_res_pr_bins));
            System.out.println("END");

            int all = pdbdata.getItemCount();

            // r, P(r)
            izero = Functions.trapezoid_integrate(pdbdata);
            double invarea = 1.0 / izero, rvalue;
            XYSeries secondMoment = new XYSeries("second");
            for (int i = 0; i < all; i++) {
                XYDataItem tempItem = pdbdata.getDataItem(i);
                rvalue = tempItem.getXValue();
                secondMoment.add(rvalue, rvalue*rvalue*tempItem.getYValue());  // middle position of the histogram bin
            }

            rg = Math.sqrt(0.5*Functions.trapezoid_integrate(secondMoment)*invarea);

            // normalize Pr distribution
            System.out.println("P(R) InvArea " + invarea + " Rg => " + rg);
            for (int i = 0; i < pdbdata.getItemCount(); i++) {
                pdbdata.updateByIndex(i, pdbdata.getY(i).doubleValue() * invarea);
            }

            System.out.println("POFR POINTS FROM PDB MODEL (UNSCALED) : " + all + " > " + pr_bins);
            for (int i = 0; i < all; i++) {
                System.out.println(String.format("%5.2f %.5E", pdbdata.getX(i).doubleValue(), pdbdata.getY(i).doubleValue()));
            }
            System.out.println("END OF POFR POINTS");
            calculateMooreCoefficients(pr_bins, ns_dmax);
            // calcualte p(r) distribution to the specified resolution
        }
    }

    /**
     * convert to I(q) using Integral of P(r) * sin(qr)/qr dr
     */
    private void convertPofRToIntensities(){

        // for a given q calculate intensity
        double qmin = 0.0005;
        double delta_q = (qmax - qmin)/900.0;
        qmin -= delta_q;

        double q_at = qmin, qr;
        double sum;
        int totalr = pdbdata.getItemCount();

        double constant = 0.5*dmax/(totalr-1); // first bin is just 0,0

        XYDataItem tempItem;
        System.out.println("ICALC");
        while (q_at < qmax){  // integrate using trapezoid rule

            q_at += delta_q;

            sum = 0;
            // for a given q, integrate Debye function from r=0 to r=dmax
            for(int i=1; i<totalr-1; i++){
                tempItem = pdbdata.getDataItem(i);
                qr = (tempItem.getXValue())*q_at;
                //sum += 2*( (tempItem.getYValue())* FastMath.sin(qr)/qr); // trapezoid rule
                sum += ( (tempItem.getYValue())* FastMath.sin(qr)/qr);
            }
            //System.out.println(q_at + " " + sum);
            icalc.add(q_at, constant*sum);
        }
        System.out.println("END");

        double max = icalc.getMaxY();
        int totalcalc = icalc.getItemCount();
        double rescale = 10.0/max;
        error = new XYSeries("error for " + filename);

        for(int i=0; i<totalcalc; i++){
            icalc.update(icalc.getX(i), icalc.getY(i).doubleValue()*rescale);
            error.add(icalc.getX(i), icalc.getY(i).doubleValue()*rescale*0.05);
        }
        System.out.println("Intensities Calculated from PDB");
    }

    /**
 * Given a collection of atoms return largest dimension
 * @param randomAtoms ArrayList<double[3]>
 * @param total total number of atoms in randomAtoms
 * @return
 */
    private int dmaxFromPDB(ArrayList<PDBAtom> randomAtoms, int total) {

        Point3d[] points = new Point3d[total];
        double[] atom;

        for(int i=0; i<total; i++){
            atom = randomAtoms.get(i).getCoords();
            points[i] = new Point3d(atom[0], atom[1], atom[2]);
        }

        QuickHull3D hull = new QuickHull3D();
        hull.build(points);
        Point3d[] vertices = hull.getVertices();
        int totalVertices = vertices.length;
        int startIndex;
        double max=0, distance, refx, refy, refz, difx, dify, difz;

        for (int i=0; i<totalVertices; i++){
            Point3d pnt = vertices[i];
            // calculate dMax
            refx = pnt.x;
            refy = pnt.y;
            refz = pnt.z;
            startIndex=(i+1);

            while(startIndex<totalVertices){
                Point3d pnt2 = vertices[startIndex];
                difx = refx - pnt2.x;
                dify = refy - pnt2.y;
                difz = refz - pnt2.z;

                distance = difx*difx + dify*dify + difz*difz;

                if (distance > max){
                    max = distance;
                }
                startIndex++;
            }
            //System.out.printf("%-6s%5d %4s %3s %1s%4d    %8.3f%8.3f%8.3f\n", "ATOM", (i+1), "CA ", "GLY", "A",(i+1),pnt.x, pnt.y, pnt.z);
        }

        return (int)Math.ceil(Math.sqrt(max));
    }

    public XYSeries getIcalc(){
        return icalc;
    }

    public XYSeries getError(){
        return error;
    }

    public XYSeries getPrDistribution(){
        return pdbdata;
    }

    public double getDmax(){
        return dmax;
    }

    public double getRg(){
        return rg;
    }

    public double getIzero(){
        return izero;
    }


    public void centerCoordinates(){
        ArrayList<PDBAtom> workingCoords = new ArrayList<>();

        for(int i=0;i<totalAtomsRead; i++){
            PDBAtom tempAtom = atoms.get(i);
            if (useWaters){
                workingCoords.add(tempAtom);
            } else {
                if (!tempAtom.isWater()){
                    workingCoords.add(tempAtom);
                }
            }
        }

        int totalWorkingAtoms = workingCoords.size();
        double aveX=0, aveY=0, aveZ=0;

        for(int i=0;i<totalWorkingAtoms; i++){
            double[] coords = workingCoords.get(i).getCoords();
            aveX+= coords[0];
            aveY+= coords[1];
            aveZ+= coords[2];
        }

        double invTotal = 1.0/(double)totalWorkingAtoms;
        aveX *= invTotal;
        aveY *= invTotal;
        aveZ *= invTotal;

        // center coordinates
        centeredAtoms = new ArrayList<>();
        for(int i=0; i<totalWorkingAtoms; i++){
            centeredAtoms.add(new PDBAtom(workingCoords.get(i)));
            double[] coords = centeredAtoms.get(i).getCoords();
            coords[0] -= aveX;
            coords[1] -= aveY;
            coords[2] -= aveZ;
            centeredAtoms.get(i).setCoords(coords);
        }

        // write out centered coordinates
        ArrayList<String > lines = new ArrayList<>();
        for(int i=0; i<totalWorkingAtoms; i++){
            lines.add(centeredAtoms.get(i).getPDBline());
        }
        lines.add(String.format("END %n"));
        writeToFile(lines, "centeredPDB");
    }



    //make HCP lattice
    public void convertToBeadModel(double bead_radius){
        System.out.println("CONVERTING TO BEAD MODEL");
        double volume = 4.0/3.0*Math.PI*bead_radius*bead_radius*bead_radius;
        double radius = dmax*0.5;
        double squaredBeadRadius = 4*bead_radius*bead_radius;
        double limit = radius + radius*0.23;
        double inv_bead_radius = 1.0/bead_radius;
        double invsqrt6 = 1.0/Math.sqrt(6), inv3 = 1.0/3.0d, sqrt6 = Math.sqrt(6), sqrt3=Math.sqrt(3);

        int klimit = (int) (limit*inv_bead_radius*3.0/2.0*invsqrt6);
        int count=0;

        double distance;
        //float * pconvertXYZ = NULL;
        // positive first
        ArrayList<Bead> beads = new ArrayList<>();

        for (int k=-klimit; k<=klimit; k++){
            // for each k index over i and j
            double dz = 2.0*inv3*sqrt6*k;

            if (dz*bead_radius > limit){
                break;
            }

            double inv3kmod2 = inv3*(k%2);

            for(int j=-klimit; j<=klimit; j++){
                double dy = sqrt3*(j + inv3kmod2);

                if (dy*bead_radius <= limit){
                    float jkmod2 = (j+k)%2;

                    for(int i=-klimit; i<=klimit; i++){
                        // compute distance from center
                        double dx = 2*i + jkmod2;

                        distance = bead_radius*Math.sqrt(dx*dx + dy*dy + dz*dz);

                        if (distance <= limit){
                            // add bead to vector
                            beads.add(new Bead(count, dx*bead_radius, dy*bead_radius, dz*bead_radius, bead_radius));
                            count++;
                        }
                    } // end of i loop
                }
            } // end of j loop
        } // end of k loop

        // make overlapping model
        int totalAtoms = centeredAtoms.size();
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<Bead> keepers = new ArrayList<>();

        count=1;
        for(int atom=0; atom<totalAtoms; atom++){
            PDBAtom temp = centeredAtoms.get(atom);
            // if atom is within radius of a bead,keep bead
            int totalBeads = beads.size();
            ArrayList<Bead> removeThese = new ArrayList<>();
            findLoop: // find all beads within bead_radius of atom
            for(int b=0; b<totalBeads; b++){
                Bead tempbead = beads.get(b);
                if (tempbead.getSquaredDistance(temp.getCoords()) <= squaredBeadRadius){
                    lines.add(tempbead.getPDBLine(count));
                    keepers.add(tempbead);
                    //beads.remove(b);
                    removeThese.add(tempbead);
                    count++;
                    //break findLoop;
                }
            }
            beads.removeAll(removeThese);
        }

        totalAtoms = keepers.size();
        double max = 0;
        for(int i=0; i<totalAtoms; i++){
            int next = i+1;
            Bead temp1 = keepers.get(i);
            for(; next<totalAtoms; next++){
                Bead temp2 = keepers.get(next);
                double dis = temp1.getSquaredDistance(temp2);
                if (dis > max){
                    max = dis;
                }
            }
        }
        max = Math.sqrt(max);

        ArrayList<String> output = new ArrayList();
        output.add(String.format("REMARK 265            BEAD VOLUME : %.1f %n", volume));
        output.add(String.format("REMARK 265           TOTAL VOLUME : %.1f %n", volume*lines.size()));
        output.add(String.format("REMARK 265  DMAX CENTER-TO-CENTER : %.1f %n", max));
        output.add(String.format("REMARK 265      DMAX EDGE-TO-EDGE : %.1f %n", max+2*bead_radius));
        output.addAll(lines);
        output.add(String.format("END %n"));
        writeToFile(output, "bead_model");
    }

    public void writeToFile(ArrayList<String> lines, String name){

        try {
            // Create file
            FileWriter fstream = new FileWriter(currentWorkingDirectory + "/"+name+".pdb");
            BufferedWriter out = new BufferedWriter(fstream);
            int total = lines.size();
            for(int i=0; i<total; i++){
                out.write(lines.get(i));
            }
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * Using the histogram, calculate the Moore Coefficients to the Shannon Limit
     * @param totalShannonPoints
     */
    private void calculateMooreCoefficients(int totalShannonPoints, double ns_dmax){
        // pr_bins - does not include 0 and dmax
        double invDmax = Math.PI/dmax, sum, qr, rvalue;
        double invConstant = 0.5/dmax;

        int totalBins = high_res_pr_data.getItemCount();
        mooreCoefficients = new double[totalShannonPoints];  // estimate Moore Coefficients from resolution of the P(r)distribution
        //double constant = 0.5/(double)(totalBins-2)*dmax;
        for(int i=0; i < totalShannonPoints; i++){
            // set the cardinal point
            double qvalue = (i+1)*invDmax; // Shannon points are fixed, so to truncate resolution, just truncate Moore function to requisite resolution
            sum = 0;
            // for a given q, integrate Debye function from r=0 to r=dmax
            // skip first one since it is r=0, area of histogram (binned) data
            for(int j=1; j<(totalBins-1); j++){ // exclude end points where P(r) = 0

                XYDataItem tempItem = high_res_pr_data.getDataItem(j);
                //rvalue = highresWidth*((j-1)+0.5);  // using midpoint ensures Moore coefficients are all positive
                rvalue = highresWidth*j; // get proper distribution but it is shift by a few Angstroms, like 1.8
                //rvalue = tempItem.getXValue();
                //rvalue = highresWidth*(j-1); // at far edge

                qr = rvalue*qvalue;

                if (rvalue == 0){
                    sum += tempItem.getYValue(); // sinc(0) = 1
                } else { // highres is limited to dmax of the molecule
                    sum+=tempItem.getYValue()*Math.sin(qr)/qr;
                }

                //sum += tempItem.getYValue()*Math.sin(qr)/rvalue;
                //sum += highresWidth*tempItem.getYValue()*Math.sin(qr)/rvalue;
            }

            //mooreCoefficients[i] = qvalue*sum/(double)(i+1)*Math.pow(-1, i+2);
            mooreCoefficients[i] = qvalue*sum;
            System.out.println(i + " Moore " + mooreCoefficients[i] + " " + qvalue + " " + sum);
        }

        System.out.println("SYNTHETIC MOORE");
        // excludes background term
        // compare to low-resolution approximation
        // calculate P(r) using Moore Coefficients at r-values in pdbdata
        double resultM, r_value, pi_dmax_r, inv_2d = 0.5/dmax, r_value_moore;
        for (int j=1; j < pdbdata.getItemCount()-1; j++){

            r_value = pdbdata.getX(j).doubleValue();
            r_value_moore = r_value;
            //r_value_moore = delta_r*j; // delta_r based on lower resolution set by pdbdata => ns_dmax
            //pi_dmax_r = invDmax*r_value;

            //if (r_value_moore > dmax){
            //    r_value_moore = dmax;
            //}

            //pi_dmax_r = Math.PI/dmax*r_value_moore; // have to calculate it at the bin_width
            pi_dmax_r = Math.PI/ns_dmax*r_value_moore; // have to calculate it at the bin_width
            resultM = 0;

            for(int m=0; m < totalShannonPoints; m++){
                resultM += mooreCoefficients[m]*Math.sin(pi_dmax_r*(m+1));
            }

            double value = integrateMoore(delta_r*(j-1), delta_r*j); // equilvalent to midpoint
            //System.out.println(r_value + " " + value + " " + pdbdata.getY(j));
            //System.out.println(r_value + " " + (inv_2d * r_value* resultM) + " " + pdbdata.getY(j));
            //System.out.println(r_value + " " + value + " " + " " + pdbdata.getY(j));
            // calculation of the Moore at midpoint of bin is equivalent to the integration between lower and upper
            System.out.println(r_value + " " + value + " " + r_value_moore + " " + (inv_2d * r_value_moore* resultM) + " "  + " " + pdbdata.getY(j));
        }

        System.out.println(dmax + " " + 0.0 + " " + 0.0);

        String newLine = "REMARK 265 \n";
        newLine += "REMARK 265  MOORE COEFFICIENTS (UNSCALED)\n";
        newLine += String.format("REMARK 265      CONSTANT BACKGROUND m(0) : %.3E %n", 0.0);
        for (int i=0; i< totalShannonPoints;i++){
            newLine += String.format("REMARK 265                        m_(%2d) : %.3E %n", (i+1), mooreCoefficients[i]);
        }
        System.out.println(newLine);

    }


    private double integrateMoore(double lower, double upper){

        double lowerSum=0, upperSum=0, inva;
        for(int i=0; i< mooreCoefficients.length; i++){
            int n = i + 1;
            inva = dmax/(Math.PI*n);
            // integrate between lower and upper
            lowerSum+= mooreCoefficients[i]*(-inva*(lower*Math.cos(lower*n*Math.PI/dmax) - inva*Math.sin(lower*n*Math.PI/dmax)));
            upperSum+= mooreCoefficients[i]*(-inva*(upper*Math.cos(upper*n*Math.PI/dmax) - inva*Math.sin(upper*n*Math.PI/dmax)));
        }
        return (upperSum - lowerSum);
    }
}
