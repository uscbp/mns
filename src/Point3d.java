/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 * <b> 3d point representation. Basis for most of 3d and Vector operations. </b>
*/

class Point3d implements Cloneable
{
/** 3d coordinates */
 public double x,y,z; 
/** projected screen coordinates*/
 public int    a,b  ; 
 
/** Creates zero point/vector */
 public Point3d() 
 { x=0; y=0; z=0; a=0; b=0;  }

/** Creates the (xx,yy,zz) point/vector */
 public Point3d(double xx,double yy,double zz)
 { 
  x=xx; y=yy; z=zz;
 }

/** Returns the string representation of the point*/
 public String str()
 { 
  return ("("+x+","+y+","+z+")");
 }
 
/** Clones itself. Note that a and b fields are not properly cloned.*/
 public Point3d duplicate()
 {
  return new Point3d(x,y,z);
 }

 public void set(Point3d p)
 {
  x=p.x; y=p.y; z=p.z;
 }

 public String nstr()
 { 
  return ("("+Elib.snice(x,1e3,6)+","+Elib.snice(y,1e3,6)+","+Elib.snice(z,1e3,6)+")");
 }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
