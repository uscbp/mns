 
import java.awt.*;

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 * Class for the camera(or eye). For now camera can be on the Z axis.
 * 
*/
class Eye 
{

/** Position of eye */
 Point3d Fpos;
/** Eye coordinate axis */
 Point3d X,Y,Z;
 Point3d resX,resY,resZ,resF;
/** Screen scale */
 double Mag=100;
 Point3d lock=null;

 public Eye(double flen,double mag)
 {
  Fpos=new Point3d(0,0,-flen);
  X=new Point3d(1,0,0);
  Y=new Point3d(0,1,0);
  Z=new Point3d(0,0,1);
  resX=X.duplicate();
  resY=Y.duplicate();
  resZ=Z.duplicate();
  resF=Fpos.duplicate();
  Mag=mag;
 }

 public void reset()
 {
  X.set(resX);
  Y.set(resY);
  Z.set(resZ);
  Fpos.set(resF);
 }
 public double getMag()
 {
  return Mag;
 }
 public void setMag(double newmag)
 {
  Mag=newmag;
 }

 public void printinner(String s)
 {
  double i0=VA.inner(X,Y);
  double i1=VA.inner(X,Z);
  double i2=VA.inner(Y,Z);
  System.out.println(s+Elib.nice(i0,1e3)+","+Elib.nice(i1,1e3)+","+Elib.nice(i2,1e3));
 }
 public void XrotateViewPlane(double T)
 {

  //printinner("XaxisPRE:");
  VA._add(X,Fpos); VA._add(Y,Fpos); VA._add(Z,Fpos);
  VA._Xrotate(Fpos,T);
  VA._Xrotate(X,T);
  VA._Xrotate(Y,T);
  VA._Xrotate(Z,T);
  VA._subtract(X,Fpos);
  VA._subtract(Y,Fpos);
  VA._subtract(Z,Fpos);
  //printinner("XaxisPST:");

 }

 public void YrotateViewPlane(double T)
 {
  //printinner("YaxisPRE:");
  VA._add(X,Fpos); VA._add(Y,Fpos); VA._add(Z,Fpos);
  VA._Yrotate(Fpos,T);
  VA._Yrotate(X,T);
  VA._Yrotate(Y,T);
  VA._Yrotate(Z,T);
  VA._subtract(X,Fpos);
  VA._subtract(Y,Fpos);
  VA._subtract(Z,Fpos);
  //printinner("YaxisPST:");
 }

 public void ZrotateViewPlane(double T)
 {
  //printinner("ZaxisPRE:");
  VA._add(X,Fpos); VA._add(Y,Fpos); VA._add(Z,Fpos);
  VA._Zrotate(Fpos,T);
  VA._Zrotate(X,T);
  VA._Zrotate(Y,T);
  VA._Zrotate(Z,T);
  VA._subtract(X,Fpos);
  VA._subtract(Y,Fpos);
  VA._subtract(Z,Fpos);
  //printinner("ZaxisPST:");
 }

 public void rotateViewPlane(Point3d L,double T)
 {
  //printinner("ZaxisPRE:");
  VA._add(X,Fpos); VA._add(Y,Fpos); VA._add(Z,Fpos);
  VA._rotate(Fpos,L,T);
  VA._rotate(X,L,T);
  VA._rotate(Y,L,T);
  VA._rotate(Z,L,T);
  VA._subtract(X,Fpos);
  VA._subtract(Y,Fpos);
  VA._subtract(Z,Fpos);
 }


 public Point3d align(Point3d p,Point3d rot)
 {
  
  Point3d np =VA.Yrotate(p,rot.y);
              VA._Xrotate(np,rot.x);
  return np;
 }

/** Project point p and return the projection as a <b>Point</b>.
  * The projection is done as for instance for x coordinate the
  * projected x coordinate is F*x/(z-Fz).
  * -This projection is redundant and will be replaced by a simpler form-
*/
 public Point project(Point3d p)
 {
  System.exit(0);
/*
   int a=(int)(0.5 + Mag*( (q.x) / (q.y) ));
   int b=(int)(0.5 + Mag*( (q.z) / (q.y) ));
   return new Point(a,b);
*/
   return new Point(0,0);
 }

/** Project point p and store the projection in r.
  * The projection is done as for instance for x coordinate the
  * projected x coordinate is F*x/(z-Fz). 
  * -This projection is redundant and will be replaced by a simpler form-
*/
 public void _project(Point3d p, Point r)
 {
   Point3d q=VA.subtract(p,Fpos);
   //Point3d q=new Point3d(p.x,p.y,p.z);
   Point3d rot=VA.zap2Y(Y);
   Point3d newX=align(X,rot);
   Point3d newY=align(Y,rot);
   Point3d newZ=align(Z,rot);
   //System.out.println("newX:"+newX.nstr());
   //System.out.println("newY:"+newY.nstr());
   //System.out.println("newZ:"+newZ.nstr());

   double last=VA.cosSin(newX.x,newX.z);
   //if (Mars.debug>0)
   {  
      VA._Yrotate(newX,last);
      VA._Yrotate(newY,last);
      VA._Yrotate(newZ,last);
   //System.out.println("  more newX:"+newX.nstr());
   //System.out.println("  more newY:"+newY.nstr());
   //System.out.println("  more newZ:"+newZ.nstr());
      Mars.debug--;
   }
   VA._Yrotate(q,rot.y); 
   VA._Xrotate(q,rot.x); 
   VA._Yrotate(q,last); 

   //System.out.println("q.z:"+q.z);
   r.x=(int)(0.5 + Mag*( (q.x) / (q.z) ));
   r.y=(int)(0.5 + Mag*( (q.y) / (q.z) ));
 }

 public Point3d toWorld(Point3d r)
 {
  Point3d cx=VA.scale(X,r.x);
  Point3d cy=VA.scale(Y,r.y);
  Point3d cz=VA.scale(Z,r.z);
  Point3d w=VA.add(cx,cy);  
  VA._add(w,cz);  
  VA._add(w,Fpos);  // now w is in worltd coordinates
  return w;
 }

 synchronized public void adjustViewPlane(int dx, int dy)
 {
  Point3d r=new Point3d(-dx,dy,0);
  Point3d w=toWorld(r);
  //System.out.println(r.str()+"(local)  ->  (World) "+w.str());
  Point3d cr=VA.cross(w,Z);
  //System.out.println("view axis :"+Z.str());
  //System.out.println("world X view axis:"+cr.str());
  rotateViewPlane(cr,0.05*VA.acos(w,Z));
 }

 public double projectDist(Point3d p1,Point3d p2)
 {
  Point r1=new Point();
  Point r2=new Point();
  _project(p1,r1);
  _project(p2,r2);
  return VA.dist(r1,r2);
 }

 public void setPosition(double x,double y,double z)
 {
  Fpos.x=x;
  Fpos.y=y;
  Fpos.z=z;
  if (lock!=null) lookAt(lock);
 }

/** Construct a new eye coord. system so that p is centered. Note that
    there are infinitely many of these systems. Here we pick the one
    that will have same orientation as the world y-axis if the direction
    given is (0,0,1).
*/

 public void lookAt(Point3d p)
 {
  Z.x=p.x-Fpos.x;
  Z.y=p.y-Fpos.y;
  Z.z=p.x-Fpos.z;
  VA._normalize(Z);

  Point3d k=new Point3d(0,1,0); 
  X=VA.cross(k,Z);
  if (VA.zero(X)) X=VA.cross(new Point3d(1,0,0),Z);
  // now Z perp. X
  Y=VA.cross(Z,X);
  // now pairwise perp.
 }

 public void lookAt(double x,double y,double z)
 {
  lookAt(new Point3d(x,y,z));
 }

 public void lock(double x,double y,double z)
 {
  lock=new Point3d(x,y,z);
 }

 public void unlock()
 { lock=null; }
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
