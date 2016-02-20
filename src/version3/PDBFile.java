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
    private boolean useWaters = false;
    private XYSeries pdbdata;
    private XYSeries icalc;
    private XYSeries error;
    private ArrayList<PDBAtom> atoms;
    private String filename;
    private double izero, rg;

    public PDBFile(File selectedFile, double qmax, boolean exclude){
        filename = selectedFile.getName();
        FileInputStream fstream = null;
        this.qmax = qmax;
        this.useWaters = exclude;
        icalc = new XYSeries(filename);

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
            pr_bins = (int)Math.ceil((dmax * qmax / Math.PI)) + 1;
            pr_bins = 35;
            delta_r = (double) dmax / (double)pr_bins;
            double inv_delta = 1.0 /delta_r;

            int[] histo = new int[pr_bins];
            double[] atom, atom2;
            double refx, refy, refz, difx, dify, difz, distance;
            int startIndex, bin;

            for (int i = 0; i < totalAtoms; i++) {
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
                    // which bin?
                    bin = (int) (distance * inv_delta); // casting to int is equivalent to floor

                    histo[(bin >= pr_bins) ? bin - 1 : bin] += 1;
                    startIndex++;
                }
            }

            pdbdata = new XYSeries(filename);
            pdbdata.add(0, 0);
            for (int i = 0; i < pr_bins; i++) {
                pdbdata.add((2 * i + 1) * delta_r * 0.5, histo[i]);  // middle position of the histogram bin
            }
            pdbdata.add(dmax, 0);

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

            for (int i = 0; i < pr_bins + 1; i++) {
                pdbdata.updateByIndex(i, pdbdata.getY(i).doubleValue() * invarea);
            }

            System.out.println("POFR POINTS FROM PDB MODEL (UNSCALED) : " + all + " <=> " + pr_bins);
            for (int i = 0; i < all; i++) {
                System.out.println(String.format("%5.2f %.5E", pdbdata.getX(i).doubleValue(), pdbdata.getY(i).doubleValue()));
            }
            System.out.println("END OF POFR POINTS");
        }
    }

    /**
     * convert to I(q) using Integral of P(r) * sin(qr)/qr dr
     */
    private void convertPofRToIntensities(){

        // for a given q calculate intensity
        double qmin = 0.001;
        double delta_q = (qmax - qmin)/500;
        double q_at = qmin, qr;
        double sum;
        int totalr = pdbdata.getItemCount();

        double constant = 0.5*dmax/(totalr-1);

        XYDataItem tempItem;

        while (q_at < qmax){  // integrate using trapezoid rule

            q_at += delta_q;

            sum = 0;
            // for a given q, integrate Debye function from r=0 to r=dmax
            for(int i=1; i<totalr; i++){
                tempItem = pdbdata.getDataItem(i);
                qr = tempItem.getXValue()*q_at;
                sum += 2*( (tempItem.getYValue())* FastMath.sin(qr)/qr);
            }
            icalc.add(q_at, constant*sum);
        }


        double max = icalc.getMaxY();
        int totalcalc = icalc.getItemCount();
        double rescale = 10.0/max;
        error = new XYSeries("error for " + filename);

        for(int i=0; i<totalcalc; i++){
            icalc.update(icalc.getX(i), icalc.getY(i).doubleValue()*rescale);
            error.add(icalc.getX(i), icalc.getY(i).doubleValue()*rescale*0.05);
        }

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

}
