package version3;

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
    private static ArrayList<double[]> coords = new ArrayList<double[]>();
    private static ArrayList<String> coordsAtoms = new ArrayList<String>();
    private int dmax;
    private double qmax;
    private boolean waters;
    private XYSeries pdbdata;
    public PDBFile(File selectedFile, int index, boolean convert, double qmax, boolean exclude){
        FileInputStream fstream = null;
        this.qmax = qmax;
        this.waters = exclude;

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

            int count=0;

            try {
                while ((strLine = br.readLine()) != null) {
                    if (strLine.matches("^ATOM.*") || (strLine.matches("^HETATM.*HOH.*") ) ){
                        coords.add(new double[3]);
                        coords.get(count)[0] = Double.parseDouble(strLine.substring(30,37));
                        coords.get(count)[1] = Double.parseDouble(strLine.substring(38,45));
                        coords.get(count)[2] = Double.parseDouble(strLine.substring(46,53));
                        coordsAtoms.add(strLine.substring(0,5).trim());
                        count++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void calculatePofR() {
        int totalAtoms = coords.size();

        if ((totalAtoms < 10)) {
            System.err.println("Specify Proper PDB file ");
        } else {

            // create working set
            ArrayList<double[]> workingCoords;

            if (waters) {
                // remove waters
                workingCoords = new ArrayList<double[]>();
                for (int i = 0; i < totalAtoms; i++) {
                    if (coordsAtoms.get(i).matches("ATOM")) {
                        workingCoords.add(coords.get(i));
                    }
                }
                totalAtoms = workingCoords.size();

            } else {
                workingCoords = new ArrayList<double[]>(coords);
            }


            // determine dmax of pdb file
            dmax = dmaxFromPDB(workingCoords, totalAtoms);


            // calculate PofR from workingAtoms
            // use resolution to determine number of Shannon bins
            //
            int bins = (int) (dmax * qmax / Math.PI) + 1;
            bins = 31;
            double delta = (double) dmax / (double) bins;
            double inv_delta = 1.0 / (delta);

            int[] histo = new int[bins];
            double[] atom, atom2;
            double refx, refy, refz, difx, dify, difz, distance;
            int startIndex, bin;

            for (int i = 0; i < totalAtoms; i++) {
                atom = workingCoords.get(i);
                refx = atom[0];
                refy = atom[1];
                refz = atom[2];

                startIndex = i + 1;

                while (startIndex < totalAtoms) {
                    atom2 = workingCoords.get(startIndex);
                    difx = refx - atom2[0];
                    dify = refy - atom2[1];
                    difz = refz - atom2[2];

                    distance = Math.sqrt(difx * difx + dify * dify + difz * difz);
                    // which bin?
                    bin = (int) (distance * inv_delta); // casting to int is equivalent to floor

                    histo[(bin >= bins) ? bin - 1 : bin] += 1;
                    startIndex++;
                }
            }

            pdbdata = new XYSeries("PDB");
            pdbdata.add(0, 0);
            for (int i = 0; i < bins; i++) {
                pdbdata.add((2 * i + 1) * delta * 0.5, histo[i]);
                // pdbdata.add((i+1)*delta, histo[i]);
            }
            pdbdata.add(dmax, 0);

            int all = pdbdata.getItemCount();
            System.out.println("POFR POINTS FROM PDB MODEL (UNSCALED)");
            for (int i = 0; i < all; i++) {
                //System.out.println(String.format("%.2f %.6E 0.0", pdbdata.getX(i), pdbdata.getY(i)));
                System.out.println(Constants.Scientific1dot2e1.format(pdbdata.getX(i).doubleValue()) + "\t" + Constants.Scientific1dot2e1.format(pdbdata.getY(i).doubleValue()) + "\t 0.00 ");
                //System.out.println(pdbdata.getX(i) + " " + pdbdata.getY(i));
            }
            System.out.println("END OF POFR POINTS");

            // r, P(r)
            double invarea = 1.0 / Functions.trapezoid_integrate(pdbdata);

            for (int i = 0; i < bins + 1; i++) {
                pdbdata.updateByIndex(i, pdbdata.getY(i).doubleValue() * invarea);
            }
        }
    }

    /**
     * convert
     */
    private void convertPofRToIntensities(){

    }

    /**
 * Given a collection of atoms return largest dimension
 * @param randomAtoms ArrayList<double[3]>
 * @param total total number of atoms in randomAtoms
 * @return
 */
    private int dmaxFromPDB(ArrayList<double[]> randomAtoms, int total) {
        double sumx=0, sumy=0, sumz=0;
        double[] atom;
        double inv_total = 1.0/total;

        Point3d[] points = new Point3d[total];

        for(int i=0; i<total; i++){
            atom = randomAtoms.get(i);
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

}
