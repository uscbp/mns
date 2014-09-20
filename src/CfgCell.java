/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 

public class CfgCell  {
    Point3d rep=new Point3d(0,0,0);
    int     hitc=0;
    double[] cfg=null;
    public CfgCell(Point3d p, double[] c, int cc) {
	cfg=new double[cc];
	for (int i=0;i<cc;i++) cfg[i]=c[i];
	//VA._scale(rep,hitc);
	//VA._add(rep,p);
	//VA._scale(rep,++hitc); //avarge the representative
	rep=p.duplicate();
    }
    
} // CfgCell
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
