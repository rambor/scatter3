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
    private ArrayList<PDBAtom> centeredAtoms;
    private String filename;
    private String currentWorkingDirectory;
    private double izero, rg;

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

        this.centerCoordinates();
        this.convertToBeadModel(3.6);
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
        double squaredBeadRadius = bead_radius*bead_radius;
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
            findLoop:
            for(int b=0; b<totalBeads; b++){
                Bead tempbead = beads.get(b);
                if (tempbead.getSquaredDistance(temp.getCoords()) <= squaredBeadRadius){
                    lines.add(tempbead.getPDBLine(count));
                    keepers.add(tempbead);
                    beads.remove(b);
                    count++;
                    //break findLoop;
                }
            }
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
}
