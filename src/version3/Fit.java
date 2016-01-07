package version3;

import java.util.ArrayList;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Fit {
    Dataset data;
    //  Molecule molecule;
    private float chiSquare;
    private float ck;
    private float cw;
    private float cx;
    private boolean hide;
    // return arrayList of values to pass to 2SAXS
    private ArrayList<Float> xValues;
    private ArrayList<Float> yValues;
    private ArrayList<Float> zValues;
    private ArrayList<String> atomTypes;
    private ArrayList<String> resiNames;
    /*
      public Fit(Molecule m){
          this.molecule = m;

          this.xValues = new ArrayList(this.molecule.getTotalItems());
          this.yValues = new ArrayList(this.molecule.getTotalItems());
          this.zValues = new ArrayList(this.molecule.getTotalItems());
          this.atomTypes = new ArrayList(this.molecule.getTotalItems());
          this.resiNames = new ArrayList(this.molecule.getTotalItems());
          ArrayList<Atom> tempAtoms = m.getAtoms();
          Atom tempAtom;
          for(int i=0; i < m.getTotalItems(); i++){
              tempAtom = tempAtoms.get(i);
              this.xValues.add(i, tempAtom.getX());
              this.yValues.add(i, tempAtom.getY());
              this.zValues.add(i, tempAtom.getZ());
              this.atomTypes.add(i, tempAtom.getAtomType());
              this.resiNames.add(i, tempAtom.getRes());
          }

      }
        */
    public void setHide(boolean hide){
        this.hide=hide;
    }
    public void setChiSquare(float chiS){
        this.chiSquare=chiS;
    }
    public void setCk(float ck){
        this.ck=ck;
    }
    public void setCw(float cw){
        this.cw=cw;
    }
    public void setCx(float cx){
        this.cx=cx;
    }
    public boolean isHidden(){
        return hide;
    }
    public float getChiSquare(){
        return chiSquare;
    }


    public float getCk(){
        return ck;
    }
    public float getCw(){
        return cw;
    }
    public float getCx(){
        return cx;
    }

    public ArrayList<Float> getxValues(){
        return xValues;
    }
    public ArrayList<Float> getyValues(){
        return yValues;
    }
    public ArrayList<Float> getzValues(){
        return zValues;
    }

    public ArrayList<String> getAtomTypes(){
        return atomTypes;
    }

    public ArrayList<String> getResiNames(){
        return resiNames;
    }


}